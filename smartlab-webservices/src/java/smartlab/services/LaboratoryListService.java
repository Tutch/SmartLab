/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartlab.services;

import entities.Laboratory;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Leonardo Oliveira Moreira
 */
public class LaboratoryListService extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        List<Laboratory> laboratoryList = new ArrayList<Laboratory>();
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/smartlab", "postgres", "ufc123");
            Statement statementLaboratories = connection.createStatement();
            ResultSet resultSetLaboratories = statementLaboratories.executeQuery(
                    "SELECT DISTINCT (laboratory_id) "
                    + "FROM laboratory ");
                    //+ "WHERE EXTRACT(EPOCH FROM (NOW() - \"timestamp\")) < (60 * 5);");
            while (resultSetLaboratories != null && resultSetLaboratories.next()) {
                int laboratoryId = resultSetLaboratories.getInt("laboratory_id");
                Laboratory l = new Laboratory();
                l.setId(laboratoryId);
                Statement statementLaboratory = connection.createStatement();
                ResultSet resultSetLaboratory = statementLaboratory.executeQuery(
                        "SELECT \"timestamp\", laboratory_id, temperature, light, presence, proximity, EXTRACT(EPOCH FROM (NOW() - \"timestamp\")) AS \"last_time\" "
                        + "FROM laboratory "
                        + " WHERE laboratory_id = " + laboratoryId + " ORDER BY \"timestamp\" DESC LIMIT 1");
                       // + "WHERE laboratory_id = " + laboratoryId + " AND EXTRACT(EPOCH FROM (NOW() - \"timestamp\")) < (60 * 5);");
                double temperature = 0;
                double light = 0;
                boolean presence = false;
                double proximity = 0;
                int total = 0;
                while (resultSetLaboratory != null && resultSetLaboratory.next()) {
                    temperature += resultSetLaboratory.getDouble("temperature");
                    light += resultSetLaboratory.getDouble("light");
                    if (resultSetLaboratory.getBoolean("presence") == true) {
                        presence = true;
                    }
                    proximity += resultSetLaboratory.getDouble("proximity");
                    total++;
                }
                l.setTemperature(temperature / total);
                l.setLight(light / total);
                l.setPresence(presence);
                l.setProximity(proximity / total);
                laboratoryList.add(l);
                resultSetLaboratory.close();
                statementLaboratory.close();
            }
            resultSetLaboratories.close();
            statementLaboratories.close();
            connection.close();
        } catch (ClassNotFoundException ex) {
        } catch (SQLException ex) {
        }
        try (PrintWriter out = response.getWriter()) {
            out.println("[");
            for (int i = 0; i < laboratoryList.size(); i++) {
                Laboratory l = laboratoryList.get(i);
                out.println(l.toJSON());
                if (i < laboratoryList.size() - 1) {
                    out.println(",");
                }
            }
            out.println("]");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}

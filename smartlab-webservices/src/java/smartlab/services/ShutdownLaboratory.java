/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartlab.services;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Leonardo Oliveira Moreira
 */
public class ShutdownMachineService extends HttpServlet {

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
        int laboratoryId = Integer.parseInt(request.getParameter("laboratoryId"));
        int machineId = Integer.parseInt(request.getParameter("machineId"));
        String machineAddress = null;
        
        System.out.println("chamado");
        
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/smartlab", "postgres", "ufc123");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT id, \"timestamp\", laboratory_id, disk_total, disk_free, mem_total, mem_free, cpu_used, running_process, machine_address "
                    + "FROM monitoring_machines "
                    + "WHERE laboratory_id = " + laboratoryId + " AND id = " + machineId + ";");
            while (resultSet != null && resultSet.next()) {
                machineAddress = resultSet.getString("machine_address");
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (ClassNotFoundException ex) {
        } catch (SQLException ex) {
        }

        boolean successful = false;
        if (machineAddress != null) {
            try {
                Socket socket = new Socket(machineAddress, 8081);
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject("shut down");
                socket.close();
                successful = true;
            } catch (Exception ex) {

            }
        }

        try (PrintWriter out = response.getWriter()) {
            out.println("{");
            out.println("\"id\":\"" + machineId + "\",");
            out.println("\"laboratoryId\":\"" + laboratoryId + "\",");
            out.println("\"shutdown\":\"" + successful + "\"");
            out.println("}");        
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

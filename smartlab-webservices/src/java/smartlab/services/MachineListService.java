/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartlab.services;

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
import smartlab.entities.Machine;

/**
 *
 * @author Leonardo Moreira
 */
public class MachineListService extends HttpServlet {

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
        List<Machine> machineList = new ArrayList<Machine>();
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/smartlab", "postgres", "ufc123");
            Statement statementMachines = connection.createStatement();
            ResultSet resultSetMachines = statementMachines.executeQuery(
                    "SELECT DISTINCT (id) "
                    + "FROM monitoring_machines "
                    //+ "WHERE laboratory_id = " + laboratoryId + " AND EXTRACT(EPOCH FROM (NOW() - \"timestamp\")) < (60 * 5);");
                    + "WHERE laboratory_id = " + laboratoryId);
            while (resultSetMachines != null && resultSetMachines.next()) {
                int machineId = resultSetMachines.getInt("id");
                Machine m = new Machine();
                m.setId(machineId);
                Statement statementMachine = connection.createStatement();
                ResultSet resultSetMachine = statementMachine.executeQuery(
                        "SELECT id, \"timestamp\", laboratory_id, disk_total, disk_free, mem_total, mem_free, cpu_used, running_process, machine_address, EXTRACT(EPOCH FROM (NOW() - \"timestamp\")) AS \"last_time\" "
                        + "FROM monitoring_machines "
                       // + "WHERE id = " + machineId + " AND EXTRACT(EPOCH FROM (NOW() - \"timestamp\")) < (60 * 5);");
                         + "WHERE id = " + machineId + " ORDER BY \"timestamp\" DESC limit 1;");
                String machineAddress = "";
                int runningProcess = 0;
                double freeMemory = 0;
                double totalMemory = 0;
                int total = 0;
                while (resultSetMachine != null && resultSetMachine.next()) {
                    machineAddress = resultSetMachine.getString("machine_address");
                    freeMemory += resultSetMachine.getDouble("mem_free");
                    totalMemory = resultSetMachine.getDouble("mem_total");
                    runningProcess = resultSetMachine.getInt("running_process");
                    total++;
                }
                m.setNetworkAddress(machineAddress);
                m.setRunningProcesses(runningProcess);
                m.setFreeMemory(freeMemory / total);
                m.setTotalMemory(totalMemory);
                machineList.add(m);
                resultSetMachine.close();
                statementMachine.close();
            }
            resultSetMachines.close();
            statementMachines.close();
            connection.close();
        } catch (ClassNotFoundException ex) {
        } catch (SQLException ex) {
        }
        try (PrintWriter out = response.getWriter()) {
            out.println("[");
            for (int i = 0; i < machineList.size(); i++) {
                Machine c = machineList.get(i);
                out.println(c.toJSON());
                if (i < machineList.size() - 1) {
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartlab.coordinator;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import smartlab.entities.Laboratory;

/**
 *
 * @author Leonardo Moreira
 */
public class SmartLabCoordinatorMonitor extends Thread {

    private Connection connection;
    private int laboratoryId;
    private SmartLabCoordinatorArduinoSerialListener serialComm;

    public static String[] getMachineAddressListByLaboratory(Connection connection, int laboratoryId) {
        String[] result = null;
        try {
            List<String> resultSetList = new ArrayList<String>();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT machine_address FROM monitoring_machines WHERE laboratory_id " + laboratoryId + " AND EXTRACT(EPOCH FROM (NOW() - \"timestamp\")) < (60 * 5);");
            while (resultSet != null && resultSet.next()) {
                resultSetList.add(resultSet.getString("machine_address"));
            }
            resultSet.close();
            statement.close();
            result = new String[resultSetList.size()];
            for (int i = 0; i < resultSetList.size(); i++) {
                result[i] = resultSetList.get(i);
            }
                
        } catch (SQLException ex) {

        }
        return result;
    }

    public SmartLabCoordinatorMonitor(Connection connection, int laboratoryId, SmartLabCoordinatorArduinoSerialListener listener) {
        this.connection = connection;
        this.laboratoryId = laboratoryId;
        this.serialComm = listener;
    }

    @Override
    public void run() {
        while (true) {
            try {
                sleep(8000);
            } catch (InterruptedException ex) {

            }

            List<Laboratory> laboratoryList = new ArrayList<Laboratory>();
            try {
                Statement statementLaboratories = connection.createStatement();
                ResultSet resultSetLaboratories = statementLaboratories.executeQuery(
                        "SELECT DISTINCT (laboratory_id) "
                        + "FROM laboratory "
                        + "WHERE EXTRACT(EPOCH FROM (NOW() - \"timestamp\")) < (60 * 5);");
                while (resultSetLaboratories != null && resultSetLaboratories.next()) {
                    int laboratoryId = resultSetLaboratories.getInt("laboratory_id");
                    Laboratory l = new Laboratory();
                    l.setId(laboratoryId);
                    Statement statementLaboratory = connection.createStatement();
                    ResultSet resultSetLaboratory = statementLaboratory.executeQuery(
                            "SELECT \"timestamp\", laboratory_id, temperature, light, presence, proximity, EXTRACT(EPOCH FROM (NOW() - \"timestamp\")) AS \"last_time\" "
                            + "FROM laboratory "
                            + "WHERE laboratory_id = " + laboratoryId + " AND EXTRACT(EPOCH FROM (NOW() - \"timestamp\")) < (60 * 5) ORDER BY \"timestamp\" DESC LIMIT 3;");
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
                for (Laboratory l : laboratoryList) {
                    System.out.println(l.getTemperature());
                    
                    if (l.getPresence() == false && l.getTemperature() >= 26.0) {
                        System.out.println("desligando...");
                        serialComm.sendMessage("shutdown");
                        
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(
                                "SELECT id, \"timestamp\", laboratory_id, disk_total, disk_free, mem_total, mem_free, cpu_used, running_process, machine_address, EXTRACT(EPOCH FROM (NOW() - \"timestamp\")) AS \"last_time\" "
                                + "FROM monitoring_machines "
                                + "WHERE laboratory_id = " + l.getId() + " AND EXTRACT(EPOCH FROM (NOW() - \"timestamp\")) < (60 * 5);");
                        while (resultSet != null && resultSet.next()) {
                            String machineAddress = resultSet.getString("machine_address");
                            try {
                                Socket socket = new Socket(machineAddress, 8081);
                                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                                outputStream.writeObject("shut down");
                                socket.close();
                                System.out.println("SmartLabCoordinator: Desligando a máquina " + machineAddress + " do laboratório " + l.getId());
                            } catch (Exception ex) {
                                System.err.println("SmartLabCoordinator: Problemas ao desligar a máquina " + machineAddress + " do laboratório " + l.getId());
                            }
                        }
                        resultSet.close();
                        statement.close();
                    }
                }
            } catch (SQLException ex) {
            }
        }
    }

}

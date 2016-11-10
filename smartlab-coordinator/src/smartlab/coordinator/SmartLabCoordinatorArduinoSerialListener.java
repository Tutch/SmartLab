/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartlab.coordinator;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TooManyListenersException;

/**
 *
 * @author Davi Cabral de Oliveira
 * @author Leonardo Oliveira Moreira
 * 
 * Classe que implementa a thread que observa os dados enviados pelo Arduino na porta serial
 */
public class SmartLabCoordinatorArduinoSerialListener extends Thread {

    private int laboratoryId;
    private SerialPort serialPort;
    private BufferedReader bufferedReader;
    private Connection connection;

    public SmartLabCoordinatorArduinoSerialListener(Connection connection, int laboratoryId, InputStream inputStream, SerialPort serialPort) {
        this.connection = connection;
        this.laboratoryId = laboratoryId;
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        setSerialEventHandler(serialPort);
    }

    private void readSerial() {
        try {
            if (bufferedReader.ready()) {
                String message = bufferedReader.readLine();
                if (message != null) {
                    System.out.println("SmartLabCoordinator: Leitura do dispositivo: " + message);

                    double temperature = 0;
                    double light = 0;
                    boolean presence = true;
                    double proximity = 0;

                    try {
                        if (message != null && message.indexOf(";") > 0) {
                            String[] array = message.split(";");
                            if (array != null && array.length == 4) {
                                
                                temperature = Double.parseDouble(array[0].split("=")[1]);
                                light = Double.parseDouble(array[1].split("=")[1]);
                                presence = Boolean.parseBoolean(array[2].split("=")[1]);
                                proximity = Double.parseDouble(array[3].split("=")[1]);
                                                                
                            }
                        }
                    } catch (Exception ex) {
                    }

                    String sql = "INSERT INTO laboratory (\"timestamp\", laboratory_id, temperature, light, presence, proximity) VALUES (NOW(), "
                            + laboratoryId + ", "
                            + temperature + ", "
                            + light + ", "
                            + presence + ", "
                            + proximity + ");";
                    System.out.println(">>> " + sql);

                    try {
                        Statement statement = connection.createStatement();
                        statement.executeUpdate(sql);
                        statement.close();
                    } catch (SQLException ex) {
                        System.out.println(ex);
                        System.err.println("SmartLabCoordinator: ERROR: Problemas ao gravar dados no banco de dados");
                    }

                }
            }
        } catch (IOException ex) {
            System.err.println("SmartLabCoordinator: ERROR: Problemas ao receber dados do dispositivo");
        }
    }

    private class SerialEventHandler implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            switch (event.getEventType()) {
                case SerialPortEvent.DATA_AVAILABLE:
                    readSerial();
                    break;
            }

        }
    }

    private void setSerialEventHandler(SerialPort serialPort) {
        try {
            serialPort.addEventListener(new SerialEventHandler());
            serialPort.notifyOnDataAvailable(true);
        } catch (TooManyListenersException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void run() {
        while (true) {
            readSerial();
        }
    }

}

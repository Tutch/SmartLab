/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartlab.coordinator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Davi Cabral
 * @author Leonardo Moreira
 */
public class SmartLabCoordinatorThread extends Thread {

    private Socket socket;
    private Connection connection;
    private ObjectInputStream inputStream;

    public SmartLabCoordinatorThread(Connection connection, Socket socket) {
        this.socket = socket;
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
            while (socket != null && !socket.isClosed()) {
                try {
                    String sql = (String) inputStream.readObject();
                    System.out.println("SmartLabCoordinator: Leitura do monitoramento da máquina " + socket.getInetAddress().getHostAddress());
                    try {
                        Statement statement = connection.createStatement();
                        statement.executeUpdate(sql);
                        System.out.println(">>> " + sql);
                        statement.close();
                    } catch (SQLException ex) {
                        
                    }
                } catch (ClassNotFoundException ex) {

                }
            }
            socket.close();
        } catch (IOException ex) {
        }
    }

}

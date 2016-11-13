/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartlab.coordinator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;

/**
 *
 * @author Davi Cabral
 * @author Leonardo Moreira
 */
public class SmartLabCoordinatorServer extends Thread {

    private ServerSocket serverSocket;
    private int serverPort;
    private Connection connection;

    public SmartLabCoordinatorServer(Connection connection, int serverPort) {
        this.connection = connection;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("SmartLabCoordinator: Server is running on port " + serverPort);
            while (serverSocket != null && !serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                SmartLabCoordinatorThread thread = new SmartLabCoordinatorThread(connection, socket);
                thread.start();
            }
        } catch (IOException ex) {
            System.err.println("SmartLabCoordinator: " + ex.getMessage());
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ex1) {
                    
                }
            }
            System.exit(-1);
        }

    }

}

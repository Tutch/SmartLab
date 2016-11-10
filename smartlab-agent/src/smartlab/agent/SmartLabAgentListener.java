/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartlab.agent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Davi Cabral
 * @author Leonardo Moreira
 */
public class SmartLabAgentListener extends Thread {

    private ServerSocket serverSocket;
    private int serverPort;
    private ObjectInputStream inputStream;

    public SmartLabAgentListener(int port) {
        serverPort = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("SmartLabAgent: aguardando por comandos do coordenador");
            Socket socket = serverSocket.accept();
            inputStream = new ObjectInputStream(socket.getInputStream());
            while (socket != null && !socket.isClosed()) {
                try {
                    String message = (String) inputStream.readObject();
                    if (message != null && message.equals("shut down")) {
                        System.out.println("SmartLabAgent: executando o comando de desligar este computador");
                        SmartLabAgent.executeCommand("shutdown /s");
                    }
                } catch (ClassNotFoundException ex) {

                } catch (Exception ex) {

                }
            }
            socket.close();
            serverSocket.close();
        } catch (IOException ex) {
        }

    }

}

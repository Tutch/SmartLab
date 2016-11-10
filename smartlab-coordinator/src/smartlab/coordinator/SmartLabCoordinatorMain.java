/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartlab.coordinator;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 *
 * @author Davi Cabral
 * @author Leonardo Moreira
 */
public class SmartLabCoordinatorMain {

    public static String[] getCommPortList() {
        List<String> result = new ArrayList<String>();
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
            result.add(portId.getName());
        }
        String[] comPortArray = new String[result.size()];
        for (int i = 0; i < result.size(); i++) {
            comPortArray[i] = result.get(i);
        }
        return comPortArray;
    }

    public static void main(String[] args) {
        String portCOM = "COM3";
        int rate = 9600;
        int serverPort = 8080;
        int laboratoryId = 1;

        if (args == null || args.length != 4) {
            System.err.println("SmartLabCoordinator: ERROR: Número de parâmetros inválidos");
            System.err.println("SmartLabCoordinator: ERROR: Ordem dos parâmetros obrigatórios: PORTA_SERIAL TAXA_TRANSMISSAO PORTA_DO_SERVIDOR ID_DO_LABORATORIO");
            System.exit(-1);
        } else {
            try {
                portCOM = args[0];
                System.out.println(portCOM);
                rate = Integer.parseInt(args[1]);
                serverPort = Integer.parseInt(args[2]);
                laboratoryId = Integer.parseInt(args[3]);
            } catch (Exception ex) {
                System.err.println("SmartLabCoordinator: ERROR: Ordem dos parâmetros obrigatórios: PORTA_SERIAL TAXA_TRANSMISSAO PORTA_DO_SERVIDOR ID_DO_LABORATORIO");
                System.exit(-1);
            }
        }

        String[] ports = getCommPortList();
        System.out.println("SmartLabCoordinator: Número de porta(s) COM encontrada(s): " + ports.length);
        if (ports.length == 0) {
            System.err.println("SmartLabCoordinator: ERROR: Nenhuma porta COM encontrada");
            System.exit(-1);
        }

        boolean portFound = false;
        for (String port : ports) {
            System.out.println("SmartLabCoordinator: Porta " + port + " encontrada");
            if (port.equals(portCOM)) {
                portFound = true;
                System.out.println("SmartLabCoordinator: Porta " + port + " será utilizada para conectar o dispositivo");
            }
        }
        
        if (!portFound) {
            System.err.println("SmartLabCoordinator: ERROR: A porta " + portCOM + " não possui dispositivo conectado");
            System.exit(-1);
        }
        
        try {
            //Define uma variável portId do tipo CommPortIdentifier para realizar a comunicação serial
            CommPortIdentifier portId = null;
            try {
                // Verifica se a porta COM informada existe
                portId = CommPortIdentifier.getPortIdentifier(portCOM);
            } catch (NoSuchPortException ex) {
                System.err.println("SmartLabCoordinator: ERROR: Porta COM não encontrada");
                System.exit(-1);
            }

            // Abre a conexão com o banco de dados
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/smartlab", "postgres", "ufc123");

            //Abre a porta COM
            SerialPort port = (SerialPort) portId.open("SmartLabCoordinator", rate);

            final String innerPortCOM = portCOM;
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (port != null) {
                        port.close();
                    }
                    System.err.println("SmartLabCoordinator: Porta " + innerPortCOM + " fechada com sucesso");
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (SQLException ex) {
                        }
                    }
                    System.err.println("SmartLabCoordinator: Conexão com o banco de dados finalizada com sucesso");
                }
            });

            port.setSerialPortParams(rate, //taxa de transferência da porta serial 
                    SerialPort.DATABITS_8, //taxa de 10 bits 8 (envio)
                    SerialPort.STOPBITS_1, //taxa de 10 bits 1 (recebimento)
                    SerialPort.PARITY_NONE); //receber e enviar dados

            SmartLabCoordinatorArduinoSerialListener listener = new SmartLabCoordinatorArduinoSerialListener(connection, laboratoryId, port.getInputStream(), port);
            listener.start();

            SmartLabCoordinatorServer server = new SmartLabCoordinatorServer(connection, serverPort);
            server.start();
            
            SmartLabCoordinatorMonitor monitor = new SmartLabCoordinatorMonitor(connection, laboratoryId);
            monitor.start();

        } catch (Exception ex) {
            System.err.println("SmartLabCoordinator: ERROR: Problemas ao estabelecer a comunicação com o dispositivo");
            System.exit(-1);
        }

    }
}

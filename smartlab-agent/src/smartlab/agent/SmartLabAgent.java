/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartlab.agent;

/**
 *
 * @author Davi Cabral
 * @author Leonardo Moreira
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 *
 * @author Davi Cabral
 * @author Leonardo Moreira
 */
public class SmartLabAgent extends Thread {

    private long delay;
    private int id;
    private String coordinatorAddress;
    private int coordinatorPort;
    private int laboratoryId;
    
    public SmartLabAgent(long delay, int id, String coordinatorAddress, int coordinatorPort, int laboratoryId) {
        this.delay = delay;
        this.id = id;
        this.laboratoryId = laboratoryId;
        this.coordinatorPort = coordinatorPort;
        this.coordinatorAddress = coordinatorAddress;
    }

    public static String executeCommand(String command) throws Exception {
        if (command == null || command.trim().length() == 0) {
            throw new Exception("No command");
        }
        String[] commands = command.split(" ");
        if (commands == null) {
            commands = new String[]{command};
        }

        return executeCommand(Arrays.asList(commands));
    }

    public static String executeCommand(List<String> commands) throws Exception {
        String result = "";
        ProcessBuilder pb = new ProcessBuilder(commands);
        try {
            Process p = pb.start();
            p.waitFor();
            InputStream is = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            while (br.ready()) {
                result += br.readLine() + "\n";
            }
        } catch (IOException ex) {
            throw new Exception(ex);
        } catch (InterruptedException ex) {
            throw new Exception(ex);
        }
        return result;
    }

    public String getMonitoringData() {
        try {
            String machineAddress = executeCommand("wmic nicconfig get IPAddress");
            machineAddress = machineAddress.trim().replaceAll(" ", "");
            machineAddress = machineAddress.trim().replaceAll("\n", "");
            machineAddress = machineAddress.substring(machineAddress.indexOf("{\"") + 2);
            machineAddress = machineAddress.substring(0, machineAddress.indexOf("\""));

            String memTotal = executeCommand("wmic ComputerSystem get TotalPhysicalMemory");
            memTotal = memTotal.split("\n\n")[1];
            memTotal = memTotal.trim();

            String memFree = executeCommand("wmic OS get FreePhysicalMemory /Value");
            memFree = memFree.trim();
            memFree = memFree.substring(19);

            String cpuUsed = executeCommand("wmic cpu get loadpercentage");
            cpuUsed = cpuUsed.substring(14);
            cpuUsed = cpuUsed.trim();
            
            String diskFree = this.executeCommand("wmic logicaldisk get freespace");
            diskFree = diskFree.split("\n\n")[1];
            diskFree = diskFree.trim();

            String diskTotal = this.executeCommand("wmic logicaldisk get size");
            diskTotal = diskTotal.split("\n\n")[1];
            diskTotal = diskTotal.trim();

            String runningProcess = executeCommand("wmic process get name");

            ArrayList<String> runningProcessList = new ArrayList<String>();

            for (int i = 0; i < runningProcess.split("\n\n").length; i++) {
                runningProcessList.add(runningProcess.split("\n\n")[i]);
            }

            System.out.println("---");
            System.out.println("SmartLabAgent: Endereço da Máquina: " + machineAddress);
            System.out.println("SmartLabAgent: Número de Processos em Execução: " + runningProcessList.size());
            System.out.println("SmartLabAgent: CPU Usada: " + cpuUsed);
            System.out.println("SmartLabAgent: Memória Total: " + memTotal);
            System.out.println("SmartLabAgent: Memória Livre: " + memFree);
            System.out.println("SmartLabAgent: Disco Total: " + diskTotal);
            System.out.println("SmartLabAgent: Disco Livre: " + diskFree);
            System.out.println("---");

            return "INSERT INTO monitoring_machines (id, timestamp, machine_address, disk_total, disk_free, mem_total, mem_free, cpu_used, running_process, laboratory_id) VALUES (" + id + ", " + "now(), '" + machineAddress + "', " + diskTotal + ", " + diskFree + ", " + memTotal + ", " + memFree + ", " + cpuUsed + ", " + runningProcessList.size() + ", " + laboratoryId + ");";
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    public void run() {
        while (true) {
            String sql = getMonitoringData();
            if (sql == null || sql.trim().length() == 0) {
                continue;
            }
            try {
                sleep(delay);
                try {
                    Socket socket = new Socket(coordinatorAddress, coordinatorPort);
                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                    outputStream.writeObject(sql);
                    socket.close();
                } catch (IOException ex) {
                    System.err.println("SmartLabAgent: ERROR: " + ex.getMessage());
                }
            } catch (InterruptedException ex) {
                System.err.println("SmartLabAgent: ERROR: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 6) {
            System.err.println("SmartLabAgent: ERROR: Invalid parameters");
            System.exit(-1);
        }
        try {
            String coordinatorAddress = args[0];
            int coordinatorPort = Integer.parseInt(args[1]);
            int id = Integer.parseInt(args[2]);
            int delay = Integer.parseInt(args[3]);
            int listenerPort = Integer.parseInt(args[4]);
            int laboratoryId = Integer.parseInt(args[5]);

            System.out.println("SmartLabAgent: Endereço do Coordenador: " + coordinatorAddress);
            System.out.println("SmartLabAgent: Porta do Coordenador: " + coordinatorPort);
            System.out.println("SmartLabAgent: ID do Agente: " + id);
            System.out.println("SmartLabAgent: Tempo de Espera do Monitoramento: " + delay);
            System.out.println("SmartLabAgent: Porta de Acesso pelo Coordenador: " + listenerPort);
            System.out.println("SmartLabAgent: ID do Laboratório: " + laboratoryId);
            
            SmartLabAgentListener listener = new SmartLabAgentListener(listenerPort);
            listener.start();

            SmartLabAgent agent = new SmartLabAgent(delay, id, coordinatorAddress, coordinatorPort, laboratoryId);
            agent.start();
        } catch (Exception ex) {
            System.err.println("SmartLabAgent: ERROR: Ordem dos parâmetros obrigatórios: ENDERECO_DO_COORDENADOR PORTA_DO_COORDENADOR ID_DO_AGENTE TEMPO_DE_ESPERA PORTA_DE_ACESSO_PELO_COORDENADOR ID_DO_LABORATORIO");
            System.exit(-1);
        }
    }

}

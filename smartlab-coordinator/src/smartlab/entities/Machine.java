/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartlab.entities;

import java.io.Serializable;

/**
 *
 * @author Leonardo Oliveira Moreira
 */
public class Machine implements Serializable {
    
    public static final int WINDOWS = 0;
    public static final int LINUX = 1;
    
    private Integer id;
    private String networkAddress;
    private String agentPort;
    private Integer osType;
    private Integer runningProcesses;
    private Double freeMemory;
    private Double totalMemory;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNetworkAddress() {
        return networkAddress;
    }

    public void setNetworkAddress(String networkAddress) {
        this.networkAddress = networkAddress;
    }

    public String getAgentPort() {
        return agentPort;
    }

    public void setAgentPort(String agentPort) {
        this.agentPort = agentPort;
    }

    public Integer getOsType() {
        return osType;
    }

    public void setOsType(Integer osType) {
        this.osType = osType;
    }

    public Integer getRunningProcesses() {
        return runningProcesses;
    }

    public void setRunningProcesses(Integer runningProcesses) {
        this.runningProcesses = runningProcesses;
    }

    public Double getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(Double totalMemory) {
        this.totalMemory = totalMemory;
    }

    public Double getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(Double freeMemory) {
        this.freeMemory = freeMemory;
    }
    
    public String toJSON() {
        StringBuilder builder = new StringBuilder();
        // id
        builder = builder.append("\"id\"");
            builder = builder.append(":");
            builder = builder.append("\"");
            builder = builder.append(getId());
            builder = builder.append("\"");
        builder = builder.append(",");
        // os type
        builder = builder.append("\"osType\"");
            builder = builder.append(":");
            builder = builder.append("\"");
            builder = builder.append(getOsType());
            builder = builder.append("\"");
        builder = builder.append(",");
        // running processes
        builder = builder.append("\"runningProcesses\"");
            builder = builder.append(":");
            builder = builder.append("\"");
            builder = builder.append(getRunningProcesses());
            builder = builder.append("\"");
        builder = builder.append(",");
        // network address
        builder = builder.append("\"networkAddress\"");
            builder = builder.append(":");
            builder = builder.append("\"");
            builder = builder.append(getNetworkAddress());
            builder = builder.append("\"");
        builder = builder.append(",");
        // agent port
        builder = builder.append("\"agentPort\"");
            builder = builder.append(":");
            builder = builder.append("\"");
            builder = builder.append(getAgentPort());
            builder = builder.append("\"");
        builder = builder.append(",");
        // total memory
        builder = builder.append("\"totalMemory\"");
            builder = builder.append(":");
            builder = builder.append("\"");
            builder = builder.append(getTotalMemory());
            builder = builder.append("\"");
        builder = builder.append(",");
        // free memory
        builder = builder.append("\"freeMemory\"");
            builder = builder.append(":");
            builder = builder.append("\"");
            builder = builder.append(getFreeMemory());
            builder = builder.append("\"");
        return builder.toString();
    }
    
}

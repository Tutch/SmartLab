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
public class Laboratory implements Serializable {
    
    private Integer id;
    private String description;
    private Double temperature;
    private Double light;
    private Boolean presence;
    private Double proximity;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getLight() {
        return light;
    }

    public void setLight(Double light) {
        this.light = light;
    }

    public Boolean getPresence() {
        return presence;
    }

    public void setPresence(Boolean presence) {
        this.presence = presence;
    }

    public Double getProximity() {
        return proximity;
    }

    public void setProximity(Double proximity) {
        this.proximity = proximity;
    }
     
    public String toJSON() {
        StringBuilder builder = new StringBuilder();
        
        builder = builder.append("\"laboratory\"");
        builder = builder.append(":");
        builder = builder.append("{");
                        
        // id
        builder = builder.append("\"id\"");
            builder = builder.append(":");
            builder = builder.append("\"");
            builder = builder.append(getId());
            builder = builder.append("\"");
        builder = builder.append(",");
        // description
        builder = builder.append("\"description\"");
            builder = builder.append(":");
            builder = builder.append("\"");
            builder = builder.append(getDescription());
            builder = builder.append("\"");
        builder = builder.append(",");
        // temperature
        builder = builder.append("\"temperature\"");
            builder = builder.append(":");
            builder = builder.append("\"");
            builder = builder.append(getTemperature());
            builder = builder.append("\"");
        builder = builder.append(",");
        // light
        builder = builder.append("\"light\"");
            builder = builder.append(":");
            builder = builder.append("\"");
            builder = builder.append(getLight());
            builder = builder.append("\"");
        builder = builder.append(",");
        // presence
        builder = builder.append("\"presence\"");
            builder = builder.append(":");
            builder = builder.append("\"");
            builder = builder.append(getPresence());
            builder = builder.append("\"");
        // proximity
        builder = builder.append("\"proximity\"");
            builder = builder.append(":");
            builder = builder.append("\"");
            builder = builder.append(getProximity());
            builder = builder.append("\"");
        
        builder = builder.append("}");
        return builder.toString();
    }
}

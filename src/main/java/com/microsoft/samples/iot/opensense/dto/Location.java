package com.microsoft.samples.iot.opensense.dto;

import java.util.Date;

/**
 * Location
 */
public class Location {

    private double[] coordinates;

    private String type;

    private Date timestamp;

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
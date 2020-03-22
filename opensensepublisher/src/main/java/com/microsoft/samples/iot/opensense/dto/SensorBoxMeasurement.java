package com.microsoft.samples.iot.opensense.dto;

import java.util.Date;

/**
 * SensorBoxMeasurement
 */
public class SensorBoxMeasurement {

    private String value;

    private Date createdAt;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
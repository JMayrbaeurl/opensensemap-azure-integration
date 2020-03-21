package com.microsoft.samples.iot.opensense.demo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SensorBoxSensorValue
 */
public class SensorBoxSensorValue {

    @JsonProperty("_id")
    private String id;

    private String title;

    private String unit;

    private String sensorType;

    private String icon;

    private SensorBoxMeasurement lastMeasurement;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public SensorBoxMeasurement getLastMeasurement() {
        return lastMeasurement;
    }

    public void setLastMeasurement(SensorBoxMeasurement lastMeasurement) {
        this.lastMeasurement = lastMeasurement;
    }
}
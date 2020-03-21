package com.microsoft.samples.iot.opensense.demo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SenseBoxValues
 */
public class SenseBoxValues {

    @JsonProperty("_id")
    private String id;

    private Date createdAt;

    private Date updatedAt;

    private String name;

    private String grouptag;

    private String exposure;

    private String model;

    private Date lastMeasurementAt;

    private List<SensorBoxSensorValue> sensors;

    private Location currentLocation;

    private List<TypedLocation> loc;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGrouptag() {
        return grouptag;
    }

    public void setGrouptag(String grouptag) {
        this.grouptag = grouptag;
    }

    public String getExposure() {
        return exposure;
    }

    public void setExposure(String exposure) {
        this.exposure = exposure;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Date getLastMeasurementAt() {
        return lastMeasurementAt;
    }

    public void setLastMeasurementAt(Date lastMeasurementAt) {
        this.lastMeasurementAt = lastMeasurementAt;
    }

    public List<SensorBoxSensorValue> getSensors() {
        return sensors;
    }

    public void setSensors(List<SensorBoxSensorValue> sensors) {
        this.sensors = sensors;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public List<TypedLocation> getLoc() {
        return loc;
    }

    public void setLoc(List<TypedLocation> loc) {
        this.loc = loc;
    }
    
}
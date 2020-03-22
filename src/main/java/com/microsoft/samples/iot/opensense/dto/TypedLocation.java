package com.microsoft.samples.iot.opensense.dto;

/**
 * TypedLocation
 */
public class TypedLocation {

    private Location geometry;

    private String type;

    public Location getGeometry() {
        return geometry;
    }

    public void setGeometry(Location geometry) {
        this.geometry = geometry;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    
}
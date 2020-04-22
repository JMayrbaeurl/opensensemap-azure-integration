package com.microsoft.samples.iot.sensorbox;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class SensorBoxValuesMessage {

    private String sensorboxid;

    private Date lastMeasurementAt;

    private Double temperature;

    private Double humidity;

    private Double airpressure;

    private Double illumination;

    private Double uvradiation;

    private Double pm10;

    private Double pm25;

    public String getSensorboxid() {
        return sensorboxid;
    }

    public void setSensorboxid(String sensorboxid) {
        this.sensorboxid = sensorboxid;
    }

    public Date getLastMeasurementAt() {
        return lastMeasurementAt;
    }

    public void setLastMeasurementAt(Date lastMeasurementAt) {
        this.lastMeasurementAt = lastMeasurementAt;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getAirpressure() {
        return airpressure;
    }

    public void setAirpressure(Double airpressure) {
        this.airpressure = airpressure;
    }

    public Double getIllumination() {
        return illumination;
    }

    public void setIllumination(Double illumination) {
        this.illumination = illumination;
    }

    public Double getUvradiation() {
        return uvradiation;
    }

    public void setUvradiation(Double uvradiation) {
        this.uvradiation = uvradiation;
    }

    public Double getPm10() {
        return pm10;
    }

    public void setPm10(Double pm10) {
        this.pm10 = pm10;
    }

    public Double getPm25() {
        return pm25;
    }

    public void setPm25(Double pm25) {
        this.pm25 = pm25;
    }

    public SensorBoxValuesMessage() {
    }

    public SensorBoxValuesMessage(String sensorboxid) {
        this.sensorboxid = sensorboxid;
    }

    public SensorBoxValuesMessage(String sensorboxid, Date lastMeasurementAt, Double temperature, Double humidity,
            Double airpressure, Double illumination, Double uvradiation, Double pm10, Double pm25) {
        this.sensorboxid = sensorboxid;
        this.lastMeasurementAt = lastMeasurementAt;
        this.temperature = temperature;
        this.humidity = humidity;
        this.airpressure = airpressure;
        this.illumination = illumination;
        this.uvradiation = uvradiation;
        this.pm10 = pm10;
        this.pm25 = pm25;
    }

    @Override
    public String toString() {
        return "SensorBoxValuesMessage [airpressure=" + airpressure + ", humidity=" + humidity + ", illumination="
                + illumination + ", lastMeasurementAt=" + lastMeasurementAt + ", pm10=" + pm10 + ", pm25=" + pm25
                + ", temperature=" + temperature + ", uvradiation=" + uvradiation + "]";
    }

}
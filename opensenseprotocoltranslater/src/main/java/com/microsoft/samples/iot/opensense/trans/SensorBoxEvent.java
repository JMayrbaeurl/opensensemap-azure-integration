package com.microsoft.samples.iot.opensense.trans;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;
import com.microsoft.samples.iot.opensense.dto.SensorBoxSensorValue;

/**
 * SensorBoxEvent
 */
public class SensorBoxEvent {

    private String id;

    private Date timestamp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double pm10;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double pm2dot5;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double temperature;

    public SensorBoxEvent(SenseBoxValues values) {
        
        this.id = values.getId();
        this.timestamp = values.getUpdatedAt();

        if(values.getSensors() != null && values.getSensors().size() > 0) {
            for (int i = 0; i < values.getSensors().size(); i++) {
                SensorBoxSensorValue value = values.getSensors().get(i);
                try {
                    if (value != null && value.getLastMeasurement() != null && value.getLastMeasurement().getValue() != null) {
                        double currValue = NumberFormat.getInstance(Locale.US).parse(value.getLastMeasurement().getValue()).doubleValue();
                        if ("PM10".equals(value.getTitle()))
                            this.pm10 = currValue;
                        else if ("PM2.5".equals(value.getTitle()))
                            this.pm2dot5 = currValue;
                        else if ("Temperator".equals(value.getTitle()) || "Temperatur".equals(value.getTitle()))
                            this.temperature = currValue;
                    }
                } catch (ParseException ignoreEx) {}
            }
        }
    }

    public boolean hasAnyValues() {

        return this.pm10 != null || this.pm2dot5 != null || this.temperature != null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Double getPm10() {
        return pm10;
    }

    public void setPm10(Double pm10) {
        this.pm10 = pm10;
    }

    public Double getPm2dot5() {
        return pm2dot5;
    }

    public void setPm2dot5(Double pm2dot5) {
        this.pm2dot5 = pm2dot5;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
}
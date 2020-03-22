package com.microsoft.samples.iot.opensense.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ScheduledPublisher
 */
@Component
public class ScheduledPublisher {

    @Autowired
    private EHSender sender;

    @Autowired 
    private SenseBoxReader reader;

    private String sensorBoxID = "598c24dae3b1fa001000693d";

    @Scheduled(fixedRate = 5000, initialDelay = 5000)
    public void publishLatestValue() {
        
        String latestValue = this.reader.readLatestValuesAsString(this.sensorBoxID);

        this.sender.sendSenseBoxValues(latestValue);
    }
}
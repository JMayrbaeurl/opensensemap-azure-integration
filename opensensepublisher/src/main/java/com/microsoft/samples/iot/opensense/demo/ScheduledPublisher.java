package com.microsoft.samples.iot.opensense.demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ScheduledPublisher
 */
@Component
public class ScheduledPublisher {

    private static final Log logger = LogFactory.getLog(ScheduledPublisher.class);

    @Autowired
    private EHSender sender;

    @Autowired 
    private SenseBoxReader reader;

    @Value("${opensensemap.publisher.boxid:598c24dae3b1fa001000693d}")
    private String sensorBoxID;

    @Scheduled(fixedRateString="${opensensemap.publisher.fixedRate:15000}", initialDelay=5000)
    public void publishLatestValue() {
        
        String latestValue = this.reader.readLatestValuesAsString(this.sensorBoxID);

        if (latestValue != null && latestValue.length() > 0) {
            this.sender.sendSenseBoxValues(latestValue);
            logger.info("Latest values of box " + this.sensorBoxID + " sent to Azure");
        } else
            logger.error("No values could be read from OpenSenseMap for box with id " + this.sensorBoxID);
    }
}
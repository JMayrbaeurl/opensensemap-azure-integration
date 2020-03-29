package com.microsoft.samples.iot.opensense.demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * SingleBoxSchedulerConfiguration
 */
@ConditionalOnProperty(name="opensensemap.publisher.boxid")
@Configuration
public class SingleBoxSchedulerConfiguration {

    private static final Log logger = LogFactory.getLog(SingleBoxSchedulerConfiguration.class);

    @Autowired
    private EHSender sender;

    @Autowired 
    private SenseBoxReader reader;

    @Value("${opensensemap.publisher.boxid}")
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
package com.microsoft.samples.iot.opensense.demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * BBoxSchedulerConfiguration
 */
@ConditionalOnProperty(name="opensensemap.publisher.bbox")
@Configuration
public class BBoxSchedulerConfiguration {

    private static final Log logger = LogFactory.getLog(BBoxSchedulerConfiguration.class);

    @Autowired
    private EHSender sender;

    @Autowired 
    private SenseBoxReader reader;

    @Value("${opensensemap.publisher.bbox}")
    private String boundBoxString;

    @Scheduled(fixedRateString="${opensensemap.publisher.fixedRate:15000}", initialDelay=5000)
    public void publishLatestValueForVienna() {

        String latestValue = this.reader.readLatestValuesInBBoxAsString(this.boundBoxString);

        if (latestValue != null && latestValue.length() > 0) {
            this.sender.sendSenseBoxValues(latestValue);
            logger.info("Latest values for bounding box " + this.boundBoxString + " sent to Azure");
        } else
            logger.error("No values could be read from OpenSenseMap for boxes in bounding box " + this.boundBoxString);
    }
}
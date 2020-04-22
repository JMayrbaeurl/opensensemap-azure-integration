package com.microsoft.samples.iot.sensorbox;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

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
@ConditionalOnProperty(name = "opensensemap.publisher.boxid")
@Configuration
public class SingleBoxSchedulerConfiguration {

    private static final Log logger = LogFactory.getLog(SingleBoxSchedulerConfiguration.class);

    @Autowired
    private IoTHubSender sender;

    @Autowired
    private OpenSenseMapReader reader;

    @Value("${opensensemap.publisher.boxid}")
    private String sensorBoxID;

    @Scheduled(fixedRateString = "${opensensemap.publisher.fixedRate:60000}", initialDelay = 5000)
    public void publishLatestValue() {

        if (this.sensorBoxID == null) {
            logger.error("No Sensor Box ID configured. Please set 'opensensemap.publisher.boxid' accordingly");
            return;
        }

        logger.info("Requesting last measurements for Sensor Box with ID '" + this.sensorBoxID + "'");

        SenseBoxValues latestValue = this.reader.readLatestValues(this.sensorBoxID);
        if (latestValue != null) {
            this.sender.sendLatestValues(latestValue);
        } else {
            logger.error("Could not read latest measurements for Sensor Box with ID '" + this.sensorBoxID + "'");
        }
    }
}
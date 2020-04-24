package com.microsoft.samples.iot.sensorbox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

@ConditionalOnProperty(name = "sensorbox.publisher.dps.boxesCreds")
@Configuration
public class MultipleBoxesSchedulerConfiguration extends AbstractBoxSchedulerConfiguration {

    private static final Log logger = LogFactory.getLog(MultipleBoxesSchedulerConfiguration.class);

    @Value("${sensorbox.publisher.dps.scope}")
    private String dpsScopeID;

    @Value("${sensorbox.publisher.dps.boxesCreds:}")
    private String[] deviceCredentials;
    
    private Map<String, IoTHubSender> boxesSender = new HashMap<String, IoTHubSender>();

    @Scheduled(fixedRateString = "${opensensemap.publisher.fixedRate:60000}", initialDelay = 5000)
    public void publishLatestValue() {

        if (this.boxesSender != null && this.boxesSender.size() > 0) {

            for(String boxID : this.boxesSender.keySet()) {
                logger.info("Requesting last measurements for Sensor Box with ID '" + boxID + "'");
                this.publishLatestValueTo(boxID, this.boxesSender.get(boxID));
            }
        }
    }

    @PostConstruct
    public void setupSenders() {
        if (this.deviceCredentials != null && this.deviceCredentials.length > 0) {
            for(String idAndKey : this.deviceCredentials) {

                SenderConfig config = new SenderConfig(idAndKey);
                if (config.isValid()) {
                    IoTHubSender sender = new IoTHubSender();
                    try {
                        sender.setupWithDPS(this.dpsScopeID, config.deviceID, config.deviceKey);
                        sender.open();

                        SenseBoxValues values = this.getReader().readLatestValues(config.boxID);
                        sender.reportSettings(values);
                    } catch(IOException ex) {
                        logger.error("Could not open connection for device with ID '" + config.deviceID + "'");
                    }
                    this.boxesSender.put(config.boxID, sender);
               }
            }
        }
    }

    private static class SenderConfig {

        private final String boxID;
        private final String deviceID;
        private final String deviceKey;

        public SenderConfig(String fromString) {
            String[] idAndKeyArray = fromString.split(";");
            this.boxID = idAndKeyArray.length == 3 ? idAndKeyArray[0] : null;
            this.deviceID = idAndKeyArray.length == 3 ? idAndKeyArray[1] : null;
            this.deviceKey = idAndKeyArray.length == 3 ? idAndKeyArray[2] : null;
        }

        public boolean isValid() {
            return StringUtils.hasText(this.boxID) && StringUtils.hasText(this.deviceID) && StringUtils.hasText(this.deviceKey);
        }
    }

    public Map<String, IoTHubSender> getBoxesSender() {
        return boxesSender;
    }

    public void setBoxesSender(Map<String, IoTHubSender> boxesSender) {
        this.boxesSender = boxesSender;
    }

    public String getDpsScopeID() {
        return dpsScopeID;
    }

    public void setDpsScopeID(String dpsScopeID) {
        this.dpsScopeID = dpsScopeID;
    }

    public String[] getDeviceCredentials() {
        return deviceCredentials;
    }

    public void setDeviceCredentials(String[] deviceCredentials) {
        this.deviceCredentials = deviceCredentials;
    }
}
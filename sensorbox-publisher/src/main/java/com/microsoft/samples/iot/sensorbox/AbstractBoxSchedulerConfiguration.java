package com.microsoft.samples.iot.sensorbox;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractBoxSchedulerConfiguration {

    private static final Log logger = LogFactory.getLog(AbstractBoxSchedulerConfiguration.class);

    @Autowired
    private OpenSenseMapReader reader;

    public abstract void publishLatestValue();

    protected void publishLatestValueTo(final String boxID, final IoTHubSender boxSender) {

        final SenseBoxValues latestValue = this.reader.readLatestValues(boxID);
        if (latestValue != null) {
            boxSender.sendLatestValues(latestValue);
        } else {
            logger.error("Could not read latest measurements for Sensor Box with ID '" + boxID + "'");
        }
    }

    public OpenSenseMapReader getReader() {
        return reader;
    }

    public void setReader(OpenSenseMapReader reader) {
        this.reader = reader;
    }
}
package com.microsoft.samples.iot.sensorbox;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * SingleBoxSchedulerConfiguration
 */
@ConditionalOnProperty(name = "opensensemap.publisher.boxid")
@Configuration
public class SingleBoxSchedulerConfiguration extends AbstractBoxSchedulerConfiguration {

    private static final Log logger = LogFactory.getLog(SingleBoxSchedulerConfiguration.class);

    @Value("${sensorbox.publisher.iothub.connectionstring:}")
	private String iotHubConnectionString;

	@Value("${sensorbox.publisher.dps.scope:}")
    private String dpsScopeID;

    @Value("${sensorbox.publisher.dps.deviceID:}")
    private String deviceID;

    @Value("${sensorbox.publisher.dps.deviceKey:}")
    private String deviceKey;
    
    @Autowired
    private IoTHubSender sender;

    @Value("${opensensemap.publisher.boxid}")
    private String sensorBoxID;

    @Scheduled(fixedRateString = "${opensensemap.publisher.fixedRate:60000}", initialDelay = 5000)
    @Override
    public void publishLatestValue() {

        if (this.sensorBoxID == null) {
            logger.error("No Sensor Box ID configured. Please set 'opensensemap.publisher.boxid' accordingly");
            return;
        }

        logger.info("Requesting last measurements for Sensor Box with ID '" + this.sensorBoxID + "'");

        this.publishLatestValueTo(this.sensorBoxID, this.sender);
    }

    @PostConstruct
    public void reportCurrentState() throws IllegalArgumentException, IOException {
        SenseBoxValues values = this.getReader().readLatestValues(this.sensorBoxID);
        this.sender.reportSettings(values);
    }

    @Bean
	@ConditionalOnProperty(name="sensorbox.publisher.iothub.connectionstring")
	public IoTHubSender createSenderFromConnectionString() throws IllegalArgumentException, URISyntaxException, IOException {

		IoTHubSender ioTHubSender = new IoTHubSender();
		ioTHubSender.setDeviceClient(new DeviceClient(this.iotHubConnectionString, IotHubClientProtocol.AMQPS));
		ioTHubSender.open();

		return ioTHubSender;
	}

	@Bean 
	@ConditionalOnProperty(name="sensorbox.publisher.dps.scope")
	public IoTHubSender createSenderFromDPS() throws IOException {

		IoTHubSender ioTHubSender = new IoTHubSender();
		ioTHubSender.setupWithDPS(this.dpsScopeID, this.deviceID, this.deviceKey);
		ioTHubSender.open();

		return ioTHubSender;
	}
}
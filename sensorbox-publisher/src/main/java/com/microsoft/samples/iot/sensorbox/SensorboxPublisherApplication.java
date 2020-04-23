package com.microsoft.samples.iot.sensorbox;

import java.io.IOException;
import java.net.URISyntaxException;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SensorboxPublisherApplication {

	@Value("${sensorbox.publisher.iothub.connectionstring:}")
	private String iotHubConnectionString;

	@Value("${sensorbox.publisher.dps.scope:}")
    private String dpsScopeID;

    @Value("${sensorbox.publisher.dps.deviceID:}")
    private String deviceID;

    @Value("${sensorbox.publisher.dps.deviceKey:}")
	private String deviceKey;
	
	public static void main(String[] args) {
		SpringApplication.run(SensorboxPublisherApplication.class, args);
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

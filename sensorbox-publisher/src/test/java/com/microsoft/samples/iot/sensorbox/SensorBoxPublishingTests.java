package com.microsoft.samples.iot.sensorbox;

import java.io.IOException;
import java.net.URISyntaxException;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(SpringExtension.class)
public class SensorBoxPublishingTests {

    @Configuration
    static class Config {

        @Value("${sensorbox.publisher.iothub.connectionstring}")
        private String iotHubConnectionString;

        @Bean
        public IoTHubSender createSender() throws IllegalArgumentException, URISyntaxException, IOException {

            IoTHubSender ioTHubSender = new IoTHubSender();
            ioTHubSender.setDeviceClient(new DeviceClient(iotHubConnectionString, IotHubClientProtocol.AMQPS));
            ioTHubSender.getDeviceClient().open();

            return ioTHubSender;
        }

        @Bean
        public OpenSenseMapReader createReader() {

            return new OpenSenseMapReader(WebClient.builder());
        }
    }

    @Autowired
    private IoTHubSender sender;

    @Autowired
    private OpenSenseMapReader reader;

    @Test
    public void textContext() {
        Assertions.assertNotNull(this.sender);
    }

    @Test
    public void testSendLatestValues() {
        SenseBoxValues values = this.reader.readLatestValues("5e981cb545f937001cca5734");
        Assertions.assertNotNull(values);
        this.sender.sendLatestValues(values);
        Assertions.assertTrue(this.sender.hasAlreadySent());
    }
}
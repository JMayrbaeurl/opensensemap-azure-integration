package com.microsoft.samples.iot.opensense.demo;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * EHSenderTests
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class EHSenderTests {

    @TestConfiguration
    static class EHSenderTestContextConfiguration {
  
        @Bean
        public EHSender ehSender() {
            return new EHSender();
        }
    }
 
    @Autowired
    private EHSender ehSender;

    @Test
    public void testSimpleSend() throws InterruptedException {

        Assertions.assertNotNull(this.ehSender);

        OpenSenseMapReader reader = new OpenSenseMapReader(WebClient.builder());
        SenseBoxValues sensorBox = reader.readLatestValues("598c24dae3b1fa001000693d");
        Assertions.assertNotNull(sensorBox);

        this.ehSender.sendSenseBoxValues(sensorBox);

        Thread.sleep(1000);
    }
}
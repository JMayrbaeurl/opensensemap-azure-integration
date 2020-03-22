package com.microsoft.samples.iot.opensense.demo;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * EHSenderTests
 */
@RunWith(SpringRunner.class)
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

        Assert.assertNotNull(this.ehSender);

        OpenSenseMapReader reader = new OpenSenseMapReader(WebClient.builder());
        SenseBoxValues sensorBox = reader.readLatestValues("598c24dae3b1fa001000693d");
        Assert.assertNotNull(sensorBox);

        this.ehSender.sendSenseBoxValues(sensorBox);

        Thread.sleep(1000);
    }
}
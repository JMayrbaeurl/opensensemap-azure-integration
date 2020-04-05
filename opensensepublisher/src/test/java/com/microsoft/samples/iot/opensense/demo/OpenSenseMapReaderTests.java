package com.microsoft.samples.iot.opensense.demo;

import java.util.List;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * OpenSenseMapReaderTests
 */
@ExtendWith(SpringExtension.class)
public class OpenSenseMapReaderTests {

    @Test
    public void testSimpleCall() {

        OpenSenseMapReader reader = new OpenSenseMapReader(WebClient.builder());
        SenseBoxValues sensorBox = reader.readLatestValues("598c24dae3b1fa001000693d");
        Assertions.assertNotNull(sensorBox);
        Assertions.assertEquals("598c24dae3b1fa001000693d", sensorBox.getId());
    }

    @Test
    public void testGetValuesForVienna() {

        OpenSenseMapReader reader = new OpenSenseMapReader(WebClient.builder());
        List<SenseBoxValues> sensorBoxes = reader.readLatestValuesInBBox("14.813876,47.668097,17.548442,48.877302");
        Assertions.assertNotNull(sensorBoxes);
    }
}
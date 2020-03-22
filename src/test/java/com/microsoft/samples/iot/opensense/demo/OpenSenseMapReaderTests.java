package com.microsoft.samples.iot.opensense.demo;

import java.util.List;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * OpenSenseMapReaderTests
 */
@RunWith(SpringRunner.class)
public class OpenSenseMapReaderTests {

    @Test
    public void testSimpleCall() {

        OpenSenseMapReader reader = new OpenSenseMapReader(WebClient.builder());
        SenseBoxValues sensorBox = reader.readLatestValues("598c24dae3b1fa001000693d");
        Assert.assertNotNull(sensorBox);
        Assert.assertEquals("598c24dae3b1fa001000693d", sensorBox.getId());
    }

    @Test
    public void testGetValuesForVienna() {

        OpenSenseMapReader reader = new OpenSenseMapReader(WebClient.builder());
        List<SenseBoxValues> sensorBoxes = reader.readLatestValuesInBBox("14.813876,47.668097,17.548442,48.877302");
        Assert.assertNotNull(sensorBoxes);
    }
}
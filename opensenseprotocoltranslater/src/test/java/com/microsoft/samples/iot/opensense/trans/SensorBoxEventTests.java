package com.microsoft.samples.iot.opensense.trans;

import java.util.ArrayList;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;
import com.microsoft.samples.iot.opensense.dto.SensorBoxMeasurement;
import com.microsoft.samples.iot.opensense.dto.SensorBoxSensorValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Assert;

/**
 * SensorBoxEventTests
 */
@RunWith(SpringRunner.class)
public class SensorBoxEventTests {

    @Test
    public void simpleConstruction() {

        SenseBoxValues values = new SenseBoxValues();
        values.setId("TestID");
        values.setSensors(new ArrayList<SensorBoxSensorValue>());
        SensorBoxSensorValue value = new SensorBoxSensorValue();
        value.setTitle("PM10");
        SensorBoxMeasurement lastMeasurement = new SensorBoxMeasurement();
        lastMeasurement.setValue(Double.toString(1.1));
        value.setLastMeasurement(lastMeasurement);
        values.getSensors().add(value);
        SensorBoxEvent event = new SensorBoxEvent(values);
        Assert.assertEquals("TestID", event.getId());
        Assert.assertEquals(1.1, event.getPm10(), .01);
    }
}
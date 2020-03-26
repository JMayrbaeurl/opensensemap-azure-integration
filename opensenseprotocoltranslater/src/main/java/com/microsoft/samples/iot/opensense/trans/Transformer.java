package com.microsoft.samples.iot.opensense.trans;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Transformer
 */
@Component
public class Transformer {

    public SensorBoxEvent transform(@NonNull SenseBoxValues values) {

        return new SensorBoxEvent(values);
    }
}
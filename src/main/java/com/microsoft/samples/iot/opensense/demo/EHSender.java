package com.microsoft.samples.iot.opensense.demo;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.Assert;

/**
 * EHSender
 */
@EnableBinding(Source.class)
public class EHSender {

    @Autowired
    private Source source;

    public void sendSenseBoxValues(final SenseBoxValues values) {
        
        Assert.notNull(values, "Parameter values must not be null");

        this.source.output().send(new GenericMessage<SenseBoxValues>(values));
    }

    public void sendSenseBoxValues(final String values) {
        
        Assert.notNull(values, "Parameter values must not be null");

        this.source.output().send(new GenericMessage<>(values));
    }
}
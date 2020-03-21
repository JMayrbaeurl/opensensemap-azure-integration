package com.microsoft.samples.iot.opensense.demo;

/**
 * SenseBoxReader
 */
public interface SenseBoxReader {

    public SenseBoxValues readLatestValues(String id);
}
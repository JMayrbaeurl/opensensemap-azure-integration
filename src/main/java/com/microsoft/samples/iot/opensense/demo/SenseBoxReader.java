package com.microsoft.samples.iot.opensense.demo;

import java.util.List;

/**
 * SenseBoxReader
 */
public interface SenseBoxReader {

    public SenseBoxValues readLatestValues(String id);
    public List<SenseBoxValues> readLatestValuesInBBox(String bbox);
}
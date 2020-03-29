package com.microsoft.samples.iot.opensense.demo;

import java.util.List;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

/**
 * SenseBoxReader
 */
public interface SenseBoxReader {

    public SenseBoxValues readLatestValues(String id);
    public String readLatestValuesAsString(String id);

    public List<SenseBoxValues> readLatestValuesInBBox(String bbox);
    public String readLatestValuesInBBoxAsString(String bbox);
}
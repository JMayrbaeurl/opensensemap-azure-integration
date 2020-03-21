package com.microsoft.samples.iot.opensense.demo;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * OpenSenseMapReader
 */
@Service
public class OpenSenseMapReader implements SenseBoxReader {

    private final WebClient webClient;

    public OpenSenseMapReader(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.opensensemap.org").build();
    }

    @Override
    public SenseBoxValues readLatestValues(String id) {

        SenseBoxValues result = this.webClient.get().uri("/boxes/{id}", id).retrieve().bodyToMono(SenseBoxValues.class)
                .block();
        return result;
    }

    @Override
    public List<SenseBoxValues> readLatestValuesInBBox(String bbox) {
        
        SenseBoxValues[] result = this.webClient.get().uri("/boxes?bbox={bbox}&full=true", bbox)
                .retrieve().bodyToMono(SenseBoxValues[].class)
                .block();
        
        return Arrays.asList(result);
    }
}
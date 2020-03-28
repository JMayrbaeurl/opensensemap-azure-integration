package com.microsoft.samples.iot.opensense.demo;

import java.util.Arrays;
import java.util.List;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * OpenSenseMapReader
 */
@Service
public class OpenSenseMapReader implements SenseBoxReader {

    private static final Log logger = LogFactory.getLog(OpenSenseMapReader.class);

    @Value("${opensensemap.publisher.serviceUrl:https://api.opensensemap.org}")
    private String serviceUrl;

    private final WebClient webClient;

    public OpenSenseMapReader(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(this.serviceUrl != null ? this.serviceUrl : "https://api.opensensemap.org").build();
    }

    @Override
    public SenseBoxValues readLatestValues(final String id) {

        Assert.hasText(id, "Parameter id must contain text");
        logger.info("Calling OpenSenseMap API for box with id " + id);

        SenseBoxValues result = this.webClient.get().uri("/boxes/{id}", id).retrieve().bodyToMono(SenseBoxValues.class)
                .block();
        return result;
    }

    
    @Override
    public String readLatestValuesAsString(final String id) {

        Assert.hasText(id, "Parameter id must contain text");
        logger.info("Calling OpenSenseMap API for box with id " + id);

        String result = this.webClient.get().uri("/boxes/{id}", id).retrieve().bodyToMono(String.class)
                .block();
        return result;
    }

    @Override
    public List<SenseBoxValues> readLatestValuesInBBox(String bbox) {

        SenseBoxValues[] result = this.webClient.get().uri("/boxes?bbox={bbox}&full=true", bbox).retrieve()
                .bodyToMono(SenseBoxValues[].class).block();

        return Arrays.asList(result);
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }
}
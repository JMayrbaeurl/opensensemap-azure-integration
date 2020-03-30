package com.microsoft.samples.iot.opensense.trans;

import java.util.List;

import javax.annotation.PostConstruct;

import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.support.GenericMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.integration.core.AzureHeaders;

/**
 * SenseBoxValuesProcessor
 * 
 * 
 * EHTransformer
@EnableBinding(Processor.class)
public class EHTransformer {

  private Log logger = LogFactory.getLog(EHTransformer.class);

  @Autowired
  private Function<SenseBoxValues, SensorBoxEvent> transformSingle;

  @StreamListener(Processor.INPUT)
  @SendTo(Processor.OUTPUT)
  public SensorBoxEvent handle(SenseBoxValues boxValues) {

    if (boxValues != null)
      logger.info("Translating latest values from box with id " + boxValues.getId());
    else
      logger.error("Transformer called with null value for box values");

    return this.transformSingle.apply(boxValues);
  }
}
 */
@EnableBinding({ Source.class, Sink.class })
public class SenseBoxValuesProcessor {

    private Log logger = LogFactory.getLog(SenseBoxValuesProcessor.class);

    private ObjectMapper objectMapper;

    /*
    @Autowired
    private Function<SenseBoxValues, SensorBoxEvent> transformSingle;
    */

    @Autowired
    private Source source;

    @PostConstruct
    private void initObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper = objectMapper;
    }

    @StreamListener(Sink.INPUT)
    public void handleMessage(String message, @Header(AzureHeaders.CHECKPOINTER) Checkpointer checkpointer) {

        logger.info("New message received");
        logger.debug("Message contents: " + message);

        if (message != null && message.length() > 0) {
            if (message.trim().startsWith("[")) {
                logger.info("Processing values from multiple sensor boxes");
                try {
                    List<SenseBoxValues> listValues = objectMapper.readValue(message, new TypeReference<List<SenseBoxValues>>(){});
                    if(listValues != null && listValues.size() > 0) {
                        for(int i = 0; i < listValues.size(); i++) {
                            SensorBoxEvent event = this.createEventFromValues(listValues.get(i));
                            if (event.hasAnyValues())
                                this.source.output().send(new GenericMessage<SensorBoxEvent>(event));
                        }
                    }
                    logger.info("Translated and forwarded sensor values of " + listValues.size() + " sensor boxes");
                } catch (JsonProcessingException e) {
                    logger.error("Exception on processing Json message '" + message + "'", e);
                }
            } else {
                try {
                    SenseBoxValues values = this.objectMapper.readValue(message, SenseBoxValues.class);
                    if (values != null) {
                        logger.info("Translating latest values from box with id " + values.getId());
                        SensorBoxEvent event = this.createEventFromValues(values);
                        if (event.hasAnyValues())
                            this.source.output().send(new GenericMessage<SensorBoxEvent>(event));
                    }
                } catch (JsonProcessingException e) {
                    logger.error("Exception on processing Json message '" + message + "'", e);
                }
            }
        }

        checkpointer.success().handle((r, ex) -> {
            if (ex == null) {
                logger.info("Last Message successfully checkpointed");
            }
            return null;
        });
    }

    private SensorBoxEvent createEventFromValues(SenseBoxValues values) {

        //return this.transformSingle.apply(values);
        return new SensorBoxEvent(values);
    }
}
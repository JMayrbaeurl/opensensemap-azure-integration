package com.microsoft.samples.iot.opensense.trans;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.handler.annotation.SendTo;

/**
 * EHTransformer
 */
@EnableBinding(Processor.class)
public class EHTransformer {

  private Log logger = LogFactory.getLog(EHTransformer.class);

  @Autowired
  private Transformer tansformer;

  @StreamListener(Processor.INPUT)
  @SendTo(Processor.OUTPUT)
  public SensorBoxEvent handle(SenseBoxValues boxValues) {
    if (boxValues != null)
      logger.info("Translating latest values from box with id " + boxValues.getId());
    return this.tansformer.transform(boxValues);
  }
}
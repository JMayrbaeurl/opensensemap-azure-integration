package com.microsoft.samples.iot.opensense.trans;

import java.util.function.Function;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${opensensemap.translator.multiple:false}")
  private boolean multipleValues;

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

  public boolean isMultipleValues() {
    return multipleValues;
  }

  public void setMultipleValues(boolean multipleValues) {
    this.multipleValues = multipleValues;
  }
}
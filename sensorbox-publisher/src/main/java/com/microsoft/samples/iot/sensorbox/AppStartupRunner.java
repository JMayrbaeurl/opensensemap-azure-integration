package com.microsoft.samples.iot.sensorbox;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AppStartupRunner implements ApplicationRunner {

    private static final Log logger = LogFactory.getLog(SingleBoxSchedulerConfiguration.class);

    @Autowired
    private IoTHubSender sender;

    @Autowired
    private OpenSenseMapReader reader;

    @Value("${opensensemap.publisher.boxid}")
    private String sensorBoxID;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        
        logger.info("Your application started with option names : "+ args.getOptionNames());

        SenseBoxValues values = reader.readLatestValues(this.sensorBoxID);
        this.sender.reportSettings(values);
    }

}
package com.microsoft.samples.iot.opensense.trans;

import java.util.function.Function;

import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EHTransformerApp {

	public static void main(String[] args) {
		SpringApplication.run(EHTransformerApp.class, args);
	}

	@Bean
	public Function<SenseBoxValues, SensorBoxEvent> transformSingle() {
		return values -> new SensorBoxEvent(values);
	}
}

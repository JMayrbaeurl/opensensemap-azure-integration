package com.microsoft.samples.iot.opensense.trans;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EHTransformerApp {

	public static void main(String[] args) {
		SpringApplication.run(EHTransformerApp.class, args);
	}

	/*
	* Dont use as bean, since bean creation will automatically register a subscriber -> doesn't support arrays in JSon
	*
	@Bean
	public Function<SenseBoxValues, SensorBoxEvent> transformSingle() {
		return values -> new SensorBoxEvent(values);
	}
	*/
}

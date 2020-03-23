package com.microsoft.samples.iot.opensense.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EHSenderApp {

	public static void main(String[] args) {
		SpringApplication.run(EHSenderApp.class, args);
	}

}

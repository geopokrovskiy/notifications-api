package com.geopokrovskiy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class NotificationsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationsApiApplication.class, args);
	}

}

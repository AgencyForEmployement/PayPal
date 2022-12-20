package com.agency.paypal;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class PaypalApplication {
	final static Logger log = Logger.getLogger(PaypalApplication.class.getName());

	public static void main(String[] args) {
		SpringApplication.run(PaypalApplication.class, args);
		log.info("Hello from PayPalApp! Welcome back! :)");
	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}

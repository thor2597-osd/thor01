package com.secondhand.trading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.secondhand.trading"})
public class TradingPlatformApplication {
	public static void main(String[] args) {
		SpringApplication.run(TradingPlatformApplication.class, args);
	}

}

package com.trusthold;

import org.springframework.boot.SpringApplication;

public class TestTrustHoldApplication {

	public static void main(String[] args) {
		SpringApplication.from(TrustHoldApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

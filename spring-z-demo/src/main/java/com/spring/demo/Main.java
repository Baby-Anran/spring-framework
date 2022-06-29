package com.spring.demo;

import com.spring.demo.config.TestConfig;
import com.spring.demo.service.TestService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class);
		TestService testService = context.getBean(TestService.class);
		testService.printTest();
	}
}

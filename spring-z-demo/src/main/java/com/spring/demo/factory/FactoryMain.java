package com.spring.demo.factory;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class FactoryMain {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		System.out.println(context.getBean("&demoFactoryBean"));
		System.out.println(context.getBean("demoFactoryBean"));
	}
}

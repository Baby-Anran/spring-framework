package com.spring.demo.depend;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class DependMain {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DependConfig.class);

	}
}

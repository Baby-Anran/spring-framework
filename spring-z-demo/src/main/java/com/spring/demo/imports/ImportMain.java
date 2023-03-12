package com.spring.demo.imports;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ImportMain {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ImportConfig.class);
		System.out.println(context.getBean(ImportSelectBean.class));
	}
}

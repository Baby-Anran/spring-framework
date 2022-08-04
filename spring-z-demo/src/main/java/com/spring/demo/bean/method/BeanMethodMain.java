package com.spring.demo.bean.method;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class BeanMethodMain {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BeanMethodConfig.class);
		System.out.println(context.getBean(BeanMethodComponent.class));
	}
}

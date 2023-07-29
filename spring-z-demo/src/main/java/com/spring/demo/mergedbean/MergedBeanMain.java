package com.spring.demo.mergedbean;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MergedBeanMain {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MergedBeanConfig.class);
		System.out.println(context.getBean(UserService.class).getOrderService());
	}
}

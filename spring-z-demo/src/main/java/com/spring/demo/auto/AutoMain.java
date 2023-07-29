package com.spring.demo.auto;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AutoMain {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AutoConfig.class);
		System.out.println(context.getBean(UserService.class).getOrderService());
	}
}

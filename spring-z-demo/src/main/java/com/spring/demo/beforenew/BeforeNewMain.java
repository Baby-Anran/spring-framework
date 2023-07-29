package com.spring.demo.beforenew;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class BeforeNewMain {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BeforeNewConfig.class);
		UserService userService = context.getBean(UserService.class);
		System.out.println(userService);
		System.out.println(userService.getOrderService());
	}
}

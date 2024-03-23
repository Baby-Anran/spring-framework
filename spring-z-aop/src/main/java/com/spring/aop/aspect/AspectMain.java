package com.spring.aop.aspect;

import com.spring.aop.service.UserService;
import com.spring.aop.service.impl.TestService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AspectMain {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AspectConfig.class);
		TestService testService = context.getBean(TestService.class);
		testService.test();
		testService.testArgs("x", "y");
		UserService userService = context.getBean(UserService.class);
		userService.print();
	}
}

package com.spring.aop.annotation;

import com.spring.aop.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 这种方式只能针对某一个Bean
 */
public class ProxyFactoryBeanMain {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ProxyFactoryBeanConfig.class);
		((UserService) context.getBean("userServiceProxy")).print();
		((UserService) context.getBean("userService")).test();
	}
}

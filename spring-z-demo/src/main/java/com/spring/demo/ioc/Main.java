package com.spring.demo.ioc;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(IOCConfig.class);
		IOCBean iocBean = context.getBean(IOCBean.class);
		iocBean.print();
	}
}

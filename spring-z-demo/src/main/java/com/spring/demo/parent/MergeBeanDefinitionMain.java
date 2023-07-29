package com.spring.demo.parent;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MergeBeanDefinitionMain {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
		System.out.println(context.getBean("child"));
		System.out.println(context.getBean("child"));
		System.out.println(context.getBean("child"));
	}
}

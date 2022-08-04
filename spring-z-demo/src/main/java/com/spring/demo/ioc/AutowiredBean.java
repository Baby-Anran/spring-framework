package com.spring.demo.ioc;

import org.springframework.stereotype.Component;

@Component
public class AutowiredBean {

	public void print() {
		System.out.println("--- autowired bean ---");
	}
}

package com.spring.demo.ioc;

import org.springframework.stereotype.Component;

@Component
public class IOCBean {

	public IOCBean() {
		System.out.println("----- new ioc bean -----");
	}

	public void print() {
		System.out.println("----- ioc bean -----");
	}
}

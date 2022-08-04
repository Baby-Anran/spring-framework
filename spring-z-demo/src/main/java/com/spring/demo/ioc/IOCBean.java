package com.spring.demo.ioc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IOCBean {

	@Autowired
	AutowiredBean autowiredBean;

	public void print() {
		System.out.println("----- ioc bean -----");
	}
}

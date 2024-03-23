package com.spring.aop.service.impl;

import org.springframework.stereotype.Service;

@Service
public class TestService {
	
	public void test() {
		System.out.println("test test");
	}
	
	public void testArgs(String x, String y) {
		System.out.println(x + " ---> " + y);
	}
}

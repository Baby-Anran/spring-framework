package com.spring.aop.service.impl;

import com.spring.aop.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
	@Override
	public void print() {
		System.out.println("print");
	}
	
	@Override
	public void test() {
		System.out.println("test");
	}
}

package com.spring.aop.advice;

import org.springframework.aop.ThrowsAdvice;

import java.lang.reflect.Method;

public class TestThrowsAdvice implements ThrowsAdvice {

	public void afterThrowing(Method method, Object[] args, Object target, NullPointerException ex) {
		System.out.println("after throwing");
	}
}

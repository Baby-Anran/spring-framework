package com.spring.aop.advice;

import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;

public class TestBeforeAdvice implements MethodBeforeAdvice {

	@Override
	public void before(@NonNull Method method, @NonNull Object[] args, Object target) throws Throwable {
		System.out.println("before method invoke");
	}
}

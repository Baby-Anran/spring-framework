package com.spring.aop.advice;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;

public class TestAfterReturningAdvice implements AfterReturningAdvice {

	@Override
	public void afterReturning(Object returnValue, @NonNull Method method, @NonNull Object[] args, Object target) throws Throwable {
		System.out.println("after return method invoke");
	}
}

package com.spring.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TestAspect {

	@Before(value = "execution(public void com.spring.aop.service.impl.TestService.test())")
	public void beforeTest() {
		System.out.println("before test test");
	}

	@Around(value = "execution(public void com.spring.aop.service.impl.TestService.testArgs(..)) && args(x, y)", argNames = "pjp,x,y")
	public Object aroundTestArgs(ProceedingJoinPoint pjp, String x, String y) throws Throwable {
		System.out.println(x);
		Object o = pjp.proceed();
		System.out.println(y);
		return o;
	}
	
	@Before("execution(public void com.spring.aop.service.impl.UserServiceImpl.print())")
	public void before() {
		System.out.println("before");
	}
}

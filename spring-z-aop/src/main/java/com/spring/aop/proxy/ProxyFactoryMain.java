package com.spring.aop.proxy;

import com.spring.aop.advice.TestAfterReturningAdvice;
import com.spring.aop.advice.TestAroundAdvice;
import com.spring.aop.advice.TestBeforeAdvice;
import com.spring.aop.service.UserService;
import com.spring.aop.service.impl.UserServiceImpl;
import org.springframework.aop.framework.ProxyFactory;

/**
 * 使用ProxyFactory添加Advice的方式
 * 这种方式不能做到针对方法级别的拦截
 */
public class ProxyFactoryMain {
	public static void main(String[] args) {
		UserService target = new UserServiceImpl();
		
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(target);
		proxyFactory.addAdvice(new TestAfterReturningAdvice());
		proxyFactory.addAdvice(new TestBeforeAdvice());
		proxyFactory.addAdvice(new TestAroundAdvice());
		((UserService) proxyFactory.getProxy()).print();
		((UserService) proxyFactory.getProxy()).test();
	}
}

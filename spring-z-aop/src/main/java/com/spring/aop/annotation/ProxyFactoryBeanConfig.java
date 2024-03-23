package com.spring.aop.annotation;

import com.spring.aop.advice.TestAroundAdvice;
import com.spring.aop.advice.TestBeforeAdvice;
import com.spring.aop.service.UserService;
import com.spring.aop.service.impl.UserServiceImpl;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyFactoryBeanConfig {
	
	@Bean
	public ProxyFactoryBean userServiceProxy(){
		UserService userService = new UserServiceImpl();
		ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
		proxyFactoryBean.setTarget(userService);
		proxyFactoryBean.addAdvice(new TestBeforeAdvice());
		return proxyFactoryBean;
	}
	
	@Bean
	public MethodInterceptor testAroundAdvice(){
		return new TestAroundAdvice();
	}
	
	@Bean
	public ProxyFactoryBean userService(){
		UserService userService = new UserServiceImpl();
		ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
		proxyFactoryBean.setTarget(userService);
		proxyFactoryBean.setInterceptorNames("testAroundAdvice");
		return proxyFactoryBean;
	}
}

package com.spring.aop.annotation;

import com.spring.aop.advice.TestAroundAdvice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"com.spring.aop.service"})
public class BeanNameProxyConfig {
	
	@Bean
	public MethodInterceptor testAroundAdvice(){
		return new TestAroundAdvice();
	}
	
	@Bean
	public BeanNameAutoProxyCreator beanNameAutoProxyCreator() {
		BeanNameAutoProxyCreator beanNameAutoProxyCreator = new BeanNameAutoProxyCreator();
		beanNameAutoProxyCreator.setBeanNames("userSe*");
		beanNameAutoProxyCreator.setInterceptorNames("testAroundAdvice");
		beanNameAutoProxyCreator.setProxyTargetClass(true);
		return beanNameAutoProxyCreator;
	}
}

package com.spring.aop.annotation;

import com.spring.aop.advice.TestAfterReturningAdvice;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan({"com.spring.aop.service"})
@Import(DefaultAdvisorAutoProxyCreator.class)
public class DefaultAdvisorProxyConfig {
	
	@Bean
	public DefaultPointcutAdvisor defaultPointcutAdvisor(){
		NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
		pointcut.addMethodName("test");
		
		DefaultPointcutAdvisor defaultPointcutAdvisor = new DefaultPointcutAdvisor();
		defaultPointcutAdvisor.setPointcut(pointcut);
		defaultPointcutAdvisor.setAdvice(new TestAfterReturningAdvice());
		
		return defaultPointcutAdvisor;
	}
}

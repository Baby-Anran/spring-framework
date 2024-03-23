package com.spring.aop.annotation;

import com.spring.aop.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 通过BeanNameAutoProxyCreator可以对批量的Bean进行AOP，并且指定了代理逻辑，指定了一个InterceptorName，也就是一个Advice
 * 前提条件是这个Advice也得是一个Bean，这样Spring才能找到的
 * 是BeanNameAutoProxyCreator的缺点很明显，它只能根据beanName来指定想要代理的Bean。
 */
public class BeanNameProxyMain {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BeanNameProxyConfig.class);
		UserService userService = context.getBean(UserService.class);
		userService.print();
		userService.test();
	}
}

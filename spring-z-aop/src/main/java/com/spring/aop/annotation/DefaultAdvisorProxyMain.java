package com.spring.aop.annotation;

import com.spring.aop.service.UserService;
import com.spring.aop.service.impl.TestService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 通过DefaultAdvisorAutoProxyCreator会直接去找所有Advisor类型的Bean，根据Advisor中的PointCut和Advice信息，确定要代理的Bean以及代理逻辑。
 * 通过这种方式，我们得依靠某一个类来实现定义我们的Advisor，或者Advice，或者Pointcut，那么这个步骤能不能更加简化一点呢？注解！
 */
public class DefaultAdvisorProxyMain {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DefaultAdvisorProxyConfig.class);
		UserService userService = context.getBean(UserService.class);
		userService.test();
		userService.print();
		TestService testService = context.getBean(TestService.class);
		testService.test();
	}
}

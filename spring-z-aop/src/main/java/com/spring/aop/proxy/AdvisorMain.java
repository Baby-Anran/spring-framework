package com.spring.aop.proxy;

import com.spring.aop.advice.TestBeforeAdvice;
import com.spring.aop.service.UserService;
import com.spring.aop.service.impl.UserServiceImpl;
import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.ProxyFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

/**
 * ProxyFactory使用Advisor
 * Advisor就相当于PointCut + Advice
 */
public class AdvisorMain {
	public static void main(String[] args) {
		UserService target = new UserServiceImpl();
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(target);
		proxyFactory.setInterfaces(target.getClass().getInterfaces());
		proxyFactory.addAdvisor(new PointcutAdvisor() {
			@Nonnull
			@Override
			public Pointcut getPointcut() {
				return new Pointcut() {
					@Nonnull
					@Override
					public ClassFilter getClassFilter() {
						return clazz -> clazz.getName().equals(target.getClass().getName());
					}
					
					@Nonnull
					@Override
					public MethodMatcher getMethodMatcher() {
						return new MethodMatcher() {
							@Override
							public boolean matches(@Nonnull Method method, @Nonnull Class<?> targetClass) {
								return targetClass.getName().equals(target.getClass().getName()) && method.getName().equals("print");
							}
							
							@Override
							public boolean isRuntime() {
								return false;
							}
							
							@Override
							public boolean matches(@Nonnull Method method, @Nonnull Class<?> targetClass, @Nonnull Object... args) {
								return false;
							}
						};
					}
				};
			}
			
			@Nonnull
			@Override
			public Advice getAdvice() {
				return new TestBeforeAdvice();
			}
			
			@Override
			public boolean isPerInstance() {
				return false;
			}
		});
		((UserService) proxyFactory.getProxy()).print();
		((UserService) proxyFactory.getProxy()).test();
	}
}

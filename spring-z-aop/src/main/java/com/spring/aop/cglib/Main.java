package com.spring.aop.cglib;

import com.spring.aop.service.UserService;
import com.spring.aop.service.impl.UserServiceImpl;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

public class Main {
	
	public static void main(String[] args) {
		UserService userService = new UserServiceImpl();
		Object proxy = getProxy(userService);
		((UserService) proxy).print();
	}
	
	public static Object getProxy(Object target) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(target.getClass());
		enhancer.setCallbacks(new Callback[] {
				(MethodInterceptor) (o, method, objects, methodProxy) -> {
				System.out.println("before...");
				Object result = methodProxy.invoke(target, objects);
				System.out.println("after...");
				return result;
			}
		});
		return enhancer.create();
	}
}

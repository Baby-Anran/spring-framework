package com.spring.aop.jdk;

import com.spring.aop.service.UserService;
import com.spring.aop.service.impl.UserServiceImpl;
import org.springframework.cglib.proxy.Proxy;

public class Main {
	public static void main(String[] args) {
		UserService userService = new UserServiceImpl();
		((UserService) getProxy(userService)).print();
	}
	
	public static Object getProxy(Object target) {
		return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), (proxy1, method, args) -> {
            System.out.println("before...");
            Object result = method.invoke(target, args);
            System.out.println("after...");
            return result;
        });
	}
}

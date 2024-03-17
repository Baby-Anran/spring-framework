package com.spring.mybatis;

import com.spring.mybatis.config.AppConfig;
import com.spring.mybatis.entity.User;
import com.spring.mybatis.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
	public static void main(String[] args) {
//		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
//		UserService userService = context.getBean(UserService.class);
		User user = new User("HHH", 0, 30);
//		userService.insert(user);
		System.out.println(user.getId());
	}
}

package com.spring.demo.auto;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.spring.demo.auto")
public class AutoConfig {

	@Bean(autowire = Autowire.BY_NAME)
	public UserService userService() {
		return new UserService();
	}

	@Bean
	public OrderService orderService() {
		return new OrderService();
	}
}

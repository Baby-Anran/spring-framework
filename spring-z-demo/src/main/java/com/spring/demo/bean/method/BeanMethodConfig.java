package com.spring.demo.bean.method;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.spring.demo.bean.method"})
public class BeanMethodConfig {

	@Bean
	public BeanMethodComponent beanMethodComponent() {
		return new BeanMethodComponent();
	}
}

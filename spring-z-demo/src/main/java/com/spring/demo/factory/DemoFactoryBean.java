package com.spring.demo.factory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component
public class DemoFactoryBean implements FactoryBean<UserService> {
	@Override
	public UserService getObject() throws Exception {
		return new UserService();
	}

	@Override
	public Class<?> getObjectType() {
		return UserService.class;
	}
}

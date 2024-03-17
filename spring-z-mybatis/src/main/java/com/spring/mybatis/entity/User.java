package com.spring.mybatis.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class User {
	
	public User(String name, Integer sex, Integer age) {
		this.name = name;
		this.sex = sex;
		this.age = age;
	}
	
	private Long id;
	
	private String name;
	
	private Integer sex;
	
	private Integer age;
}

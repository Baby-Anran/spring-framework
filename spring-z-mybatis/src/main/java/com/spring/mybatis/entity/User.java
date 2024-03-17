package com.spring.mybatis.entity;

public class User {
	
	public User() {
	}
	
	public User(String name, Integer sex, Integer age) {
		this.name = name;
		this.sex = sex;
		this.age = age;
	}
	
	private Long id;
	
	private String name;
	
	private Integer sex;
	
	private Integer age;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getSex() {
		return sex;
	}
	
	public void setSex(Integer sex) {
		this.sex = sex;
	}
	
	public Integer getAge() {
		return age;
	}
	
	public void setAge(Integer age) {
		this.age = age;
	}
}

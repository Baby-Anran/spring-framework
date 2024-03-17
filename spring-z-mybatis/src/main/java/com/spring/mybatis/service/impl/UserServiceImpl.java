package com.spring.mybatis.service.impl;

import com.spring.mybatis.entity.User;
import com.spring.mybatis.mapper.UserMapper;
import com.spring.mybatis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	private UserMapper userMapper;
	
	@Override
	public Long insert(User user) {
		return userMapper.insert(user);
	}
}

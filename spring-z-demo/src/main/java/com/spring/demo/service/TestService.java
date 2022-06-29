package com.spring.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {

//	@Autowired
//	MustService mustService;

	public void printTest() {
		System.out.println("=== test ===");
	}
}

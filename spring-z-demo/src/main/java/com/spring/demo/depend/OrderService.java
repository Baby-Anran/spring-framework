package com.spring.demo.depend;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn(value = "userService")
public class OrderService {
}

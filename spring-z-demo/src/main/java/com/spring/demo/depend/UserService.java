package com.spring.demo.depend;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn(value = "orderService")
public class UserService {
}

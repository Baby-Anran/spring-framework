package com.spring.aop.aspect;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ComponentScan({"com.spring.aop.service", "com.spring.aop.aspect"})
@EnableAspectJAutoProxy
public class AspectConfig {

}

package com.spring.demo.mergedbean;

import org.springframework.stereotype.Component;

@Component
public class UserService {

	private OrderService orderService;

	public OrderService getOrderService() {
		return orderService;
	}

	public void setOrderService(OrderService orderService) {
		this.orderService = orderService;
	}
}

package com.spring.demo.listener;

public class Main {

    public static void main(String[] args) {
        EventSource eventSource = new EventSource();

        StockPriceListener listener1 = new StockPriceListener("张三");
        StockPriceListener listener2 = new StockPriceListener("李四");

        eventSource.addEventListener(listener1);
        eventSource.addEventListener(listener2);

        StockUpdateEvent event = new StockUpdateEvent("阿里巴巴", 180.00);
        eventSource.fireEvent(event);
    }
}

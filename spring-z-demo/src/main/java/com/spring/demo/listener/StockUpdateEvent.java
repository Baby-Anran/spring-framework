package com.spring.demo.listener;

public class StockUpdateEvent implements Event {

    private final String stockName;
    private final double stockPrice;

    public StockUpdateEvent(String stockName, double stockPrice) {
        this.stockName = stockName;
        this.stockPrice = stockPrice;
    }

    @Override
    public void happen() {
        System.out.println("股票 " + stockName + " 的价格更新为 " + stockPrice);
    }
}

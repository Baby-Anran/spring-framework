package com.spring.demo.listener;

public class StockPriceListener implements EventListener {

    private final String listenerName;

    public StockPriceListener(String listenerName) {
        this.listenerName = listenerName;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof StockUpdateEvent) {
            StockUpdateEvent stockUpdateEvent = (StockUpdateEvent) event;
			event.happen();
            System.out.println("监听器 " + listenerName + " 收到事件：" + stockUpdateEvent);
        }
    }
}

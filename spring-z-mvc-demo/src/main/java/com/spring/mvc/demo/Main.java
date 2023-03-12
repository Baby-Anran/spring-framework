package com.spring.mvc.demo;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.startup.Tomcat;

public class Main {
	public static void main(String[] args) throws Exception {
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(9090);

		//现在找不到静态资源，如果指向我们当前项目，则可以访问到静态资源
		Context context = tomcat.addContext("/", System.getProperty("java.io.tmpdir"));

		context.addLifecycleListener((LifecycleListener) Class.forName(tomcat.getHost().getConfigClass()).newInstance());
		tomcat.start();
		//挂起
		tomcat.getServer().await();
	}
}

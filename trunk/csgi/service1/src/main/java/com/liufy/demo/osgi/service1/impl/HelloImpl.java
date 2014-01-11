package com.liufy.demo.osgi.service1.impl;

import com.liufy.demo.osgi.service1.Hello;

public class HelloImpl implements Hello {

	@Override
	public String getHelloString(String name) {
		return "Hello " + name;
	}

}

package com.framework.spring.dao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface DemoService {
	String query(HttpServletRequest request,HttpServletResponse response,String name);
}

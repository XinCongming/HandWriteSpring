package com.framework.spring.dao.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.framework.spring.annotation.XINService;
import com.framework.spring.dao.DemoService;

@XINService
public class DemoServiceImpl implements DemoService{
	
	public String query(HttpServletRequest request,HttpServletResponse response,String name) {
		System.out.println("query method is runing! name = "+name);
		try {
			response.getWriter().write("query method is runing! name = "+name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "query method is runing! name = "+name;
	}


}

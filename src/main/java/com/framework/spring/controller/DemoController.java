package com.framework.spring.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.framework.spring.annotation.XINAutowired;
import com.framework.spring.annotation.XINController;
import com.framework.spring.annotation.XINRequestMapping;
import com.framework.spring.dao.DemoService;

@XINController
@XINRequestMapping("/demo")
public class DemoController {
	@XINAutowired
	private DemoService demoService ;
	
	@XINRequestMapping("query")
	public void query(HttpServletRequest request,HttpServletResponse response,String name) {
		demoService.query(request,response,name);
	}
}


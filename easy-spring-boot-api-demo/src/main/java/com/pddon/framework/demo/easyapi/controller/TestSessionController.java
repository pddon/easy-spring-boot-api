/**  
* Title TestSessionController.java  
* Description  
* @author danyuan
* @date Dec 17, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.demo.easyapi.controller;

import java.util.HashMap;
import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pddon.framework.easyapi.SessionManager;
import com.pddon.framework.easyapi.annotation.RequiredSession;
import com.pddon.framework.easyapi.dto.Session;

@Api(tags = "测试会话信息")
@RestController
public class TestSessionController {
	
	@Autowired
	@Lazy
	private SessionManager sessionManager;
	
	@GetMapping(value="login",
			name="测试登录获取用户会话ID")
	@ApiOperation(value="测试登录获取用户会话ID", notes="测试登录获取用户会话ID")
	public Session login(String account, String password){
		Session session = sessionManager.getCurrentSession(true);
		Map<String,Object> map = new HashMap<>();
		map.put("account", account);
		session.setExpandParams(map);
		return session;
	}
	
	@GetMapping(value="queryAccountInfo",
			name="测试通过会话ID获取用户账户信息")
	@ApiOperation(value="测试通过会话ID获取用户账户信息", notes="测试通过会话ID获取用户账户信息")
	@RequiredSession
	public String queryAccountInfo(){
		Session session = sessionManager.getCurrentSession(false);
		return session.getExpandParams().get("account").toString();
	}
}

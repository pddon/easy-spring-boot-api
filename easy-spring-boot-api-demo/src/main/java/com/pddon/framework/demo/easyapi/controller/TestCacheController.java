/**  
* Title TestCacheController.java  
* Description  
* @author danyuan
* @date Dec 20, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.demo.easyapi.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.pddon.framework.demo.easyapi.controller.dto.UserInfoDto;
import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.annotation.CacheMethodResult;
import com.pddon.framework.easyapi.annotation.CacheMethodResultEvict;
import com.pddon.framework.easyapi.consts.CacheExpireMode;

@Api(tags = "测试缓存功能")
@RestController
public class TestCacheController {
	
	@Autowired
	@Lazy
	private CacheManager cacheManager;

	@GetMapping(value="testMethodQueryCache",
			name="测试方法查询缓存")
	@ApiOperation(value="测试方法查询缓存", notes="测试方法查询缓存")
	@CacheMethodResult
	public String testMethodQueryCache(){
		
		return "this is testMethodQueryCache result !";
	}
	
	@PostMapping(value="testMethodQueryParamCache",
			name="测试方法参数化查询缓存")
	@ApiOperation(value="测试方法参数化查询缓存", notes="测试方法参数化查询缓存")
	@CacheMethodResult(id = "user.username", expireSeconds = 30L)
	public UserInfoDto testMethodQueryParamCache(@RequestBody UserInfoDto user){
		
		return user;
	}
	
	@GetMapping(value="testGetMethodQueryParamCache",
			name="测试手动获取方法参数化查询缓存")
	@ApiOperation(value="测试手动获取方法参数化查询缓存", notes="测试手动获取方法参数化查询缓存")
	public UserInfoDto testGetMethodQueryParamCache(
			String username
			){
		UserInfoDto user = cacheManager.get("TestCacheController:testMethodQueryParamCache"+":"+username, UserInfoDto.class, 30L, CacheExpireMode.EXPIRE_AFTER_REDA);
		return user;
	}
	
	@GetMapping(value="testEvictMethodQueryParamCache",
			name="测试清除方法参数化查询缓存")
	@ApiOperation(value="测试清除方法参数化查询缓存", notes="测试清除方法参数化查询缓存")
	@CacheMethodResultEvict(id = "username", expireSeconds = 30L, prefix = "TestCacheController:testMethodQueryParamCache")
	public String testEvictMethodQueryParamCache(
			String username
			){
		return "手动清除缓存[TestCacheController:testMethodQueryParamCache:"+username+"]成功!";
	}
	
	
}

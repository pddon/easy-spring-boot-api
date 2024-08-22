/**  
* Title CacheManageController.java  
* Description  管理缓存相关接口
* @author danyuan
* @date Dec 20, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.dto.CacheManagerState;
@Api(tags = "缓存监控和管理接口列表")
@RestController
@RequestMapping("cache")
public class CacheManageController {
	
	@Autowired
	private CacheManager cacheManager;
	
	@GetMapping(value="getAllCaches",
			name="查询所有缓存值列表")
	@ApiOperation(value="查询所有缓存值列表", notes="查询所有缓存值列表")
	public List<Map<String, Map<String, Object>>> getAllCaches(){
		List<Map<String, Map<String, Object>>> list = new ArrayList<>();
		list.add(cacheManager.getAllCaches(CacheExpireMode.EXPIRE_AFTER_REDA));
		list.add(cacheManager.getAllCaches(CacheExpireMode.EXPIRE_AFTER_WRITE));
		return list;
	}
	
	@GetMapping(value="getCaches",
			name="查询某个失效时间的所有缓存值列表")
	@ApiOperation(value="查询某个失效时间的所有缓存值列表", notes="查询某个失效时间的所有缓存值列表")
	public List<Map<String, Object>> getCaches(Long expireSeconds){
		List<Map<String, Object>> list = new ArrayList<>();
		list.add(cacheManager.getCaches(expireSeconds, CacheExpireMode.EXPIRE_AFTER_REDA));
		list.add(cacheManager.getCaches(expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE));
		return list;
	}
	
	@GetMapping(value="stats",
			name="查询缓存统计信息列表")
	@ApiOperation(value="查询缓存统计信息列表", notes="查询缓存统计信息列表")
	public List<Map<String, CacheManagerState>> stats(){
		List<Map<String, CacheManagerState>> list = new ArrayList<>();
		list.add(cacheManager.stats(CacheExpireMode.EXPIRE_AFTER_REDA));
		list.add(cacheManager.stats(CacheExpireMode.EXPIRE_AFTER_WRITE));
		return list;
	}
	
	@GetMapping(value="clearAll",
			name="清除所有缓存信息")
	@ApiOperation(value="清除所有缓存信息", notes="清除所有缓存信息")
	public void clearAll(){
		cacheManager.clear();
	}
	
	@GetMapping(value="clear",
			name="清除某个失效时间的所有缓存信息")
	@ApiOperation(value="清除某个失效时间的所有缓存信息", notes="清除某个失效时间的所有缓存信息")
	public void clear(Long expireSeconds){
		cacheManager.clear(expireSeconds);
	}
	
	@GetMapping(value="get",
			name="获取某个缓存信息")
	@ApiOperation(value="获取某个缓存信息", notes="获取某个缓存信息")
	public Object get(String key, Long expireSeconds){
		Object resp = cacheManager.get(key, expireSeconds, CacheExpireMode.EXPIRE_AFTER_REDA);
		if(resp == null){
			resp = cacheManager.get(key, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);
		}
		return resp;
	}
	
	@GetMapping(value="remove",
			name="清除某个缓存信息")
	@ApiOperation(value="清除某个缓存信息", notes="清除某个缓存信息")
	public void remove(String key, Long expireSeconds){
		cacheManager.remove(key, expireSeconds, CacheExpireMode.EXPIRE_AFTER_REDA);
		cacheManager.remove(key, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);
	}
}

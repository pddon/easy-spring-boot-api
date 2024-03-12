/**  
* Title TestParamAndResponseInvalidController.java  
* Description  
* @author danyuan
* @date Nov 29, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.demo.easyapi.controller;

import java.util.Arrays;

import javax.validation.Valid;
import javax.validation.constraints.Email;

import com.pddon.framework.demo.easyapi.controller.dto.ParamsCheckDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pddon.framework.demo.easyapi.controller.request.CustomRequest;
import com.pddon.framework.easyapi.annotation.InjectSystemParam;
import com.pddon.framework.easyapi.annotation.RequiredCountryCode;
import com.pddon.framework.easyapi.annotation.RequiredCurrency;
import com.pddon.framework.easyapi.annotation.RequiredLocale;
import com.pddon.framework.easyapi.annotation.RequiredNoRepeatSubmit;
import com.pddon.framework.easyapi.annotation.RequiredParam;
import com.pddon.framework.easyapi.annotation.RequiredSession;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.annotation.RequiredTimeZone;
import com.pddon.framework.easyapi.annotation.RequiredTimestamp;
import com.pddon.framework.easyapi.annotation.RequiredVersion;
import com.pddon.framework.easyapi.consts.DenyRepeatSubmitType;
import com.pddon.framework.easyapi.context.RequestContext;

@Api(tags = "测试api请求参数或响应内容的校验")
@RestController
@Validated
public class TestParamAndResponseInvalidController {

	@GetMapping(value="testSystemParams",
			name="测试校验系统参数")
	@ApiOperation(value="测试校验系统参数", notes="测试校验系统参数")
	@RequiredLocale
	@RequiredSign
	@RequiredCountryCode
	@RequiredCurrency
	@RequiredNoRepeatSubmit(mode = DenyRepeatSubmitType.GENERATE_TOKEN)
	@RequiredSession
	@RequiredTimestamp
	@RequiredTimeZone
	@RequiredVersion
	@RequiredParam({"appName","nickName"})
	public void testSystemParams(
			) {
	}
	
	@PostMapping(value="testRequestParamCheck",
			name="测试校验请求参数")
	@ApiOperation(value="测试校验请求参数", notes="测试校验请求参数")
	public void testRequestParamCheck(@RequestBody ParamsCheckDto req, @Valid @RequestParam @Email String adminEmail){
		
	}
	
	@GetMapping(value="testResponseFieldCheck",
			name="测试校验响应参数")
	@ApiOperation(value="测试校验响应参数", notes="测试校验响应参数")
	public ParamsCheckDto testResponseFieldCheck(
			) {
		ParamsCheckDto dto = new ParamsCheckDto();
		dto.setAccount("111222");
		dto.setAge(25);
		dto.setEmail("xxx");
		dto.setLikes(Arrays.asList("football","watch movie","swiming","book"));
		return dto;
	}
	
	@PostMapping(value="testInjectSystemParams",
			name="测试自动注入系统参数")
	@ApiOperation(value="测试自动注入系统参数", notes="测试自动注入系统参数")
	@RequiredLocale
	@RequiredCountryCode
	@RequiredCurrency
	@RequiredNoRepeatSubmit(mode = DenyRepeatSubmitType.GENERATE_TOKEN)
	@RequiredTimestamp
	@RequiredTimeZone
	@RequiredVersion
	@RequiredParam({"appName","nickName"})
	@InjectSystemParam
	public void testInjectSystemParams(
			@RequestBody CustomRequest req
			) {
	}
	
	@GetMapping(value="testAddSystemResponseParams",
			name="测试设置系统响应参数")
	@ApiOperation(value="测试设置系统响应参数", notes="测试设置系统响应参数")
	public void testAddSystemResponseParams() {
		RequestContext.getContext().setResonseSystemParamValue("traceId", "0001");
		ParamsCheckDto dto = new ParamsCheckDto();
		dto.setAccount("111222");
		dto.setAge(25);
		dto.setEmail("xxx");
		dto.setLikes(Arrays.asList("football","watch movie","swiming","book"));
		RequestContext.getContext().setResonseSystemParamValue("userInfo", dto);
	}
}

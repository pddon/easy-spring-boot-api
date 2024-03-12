/**  
* Title ErrorController.java  
* Description  自定义系统未知错误处理接口响应
* @author danyuan
* @date Sep 10, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.controller;



import com.pddon.framework.easyapi.controller.request.QueryErrorCodeRequest;
import com.pddon.framework.easyapi.controller.response.ErrorCodeListResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pddon.framework.easyapi.LanguageTranslateManager;
import com.pddon.framework.easyapi.component.BusinessErrorCodeInterpreter;
import com.pddon.framework.easyapi.dto.ErrorCodeDto;
import com.pddon.framework.easyapi.utils.StringUtils;

@Api(tags = "系统错误码管理")
@RestController
public class ErrorCodeController {
	@Autowired
	private LanguageTranslateManager languageTranslateManager;
	
	@Autowired
	private BusinessErrorCodeInterpreter bussinessErrorCodeInterpreter;
	
	@GetMapping(value="system/errorList",
			name="查询所有错误码列表")
	@ApiOperation(value="查询所有错误码列表", notes="查询所有错误码列表")
	public ErrorCodeListResponse errorList(
			){
		return new ErrorCodeListResponse()
		.setBussinessErrors(bussinessErrorCodeInterpreter.getBussinessCodes())
			.setSystemErrors(bussinessErrorCodeInterpreter.getSystemCodes());
	}
	
	@GetMapping(value="system/queryErrorCode",
			name="查询某错误码的详细信息")
	@ApiOperation(value="查询某错误码的详细信息", notes="查询某错误码的详细信息")
	public ErrorCodeDto queryErrorCode(
			QueryErrorCodeRequest req
			){
		for(ErrorCodeDto error : bussinessErrorCodeInterpreter.getSystemCodes()){
			if(StringUtils.isNotBlank(req.getCode()) && req.getCode().equals(error.getCode())){
				return error.setMsg(error.getError());
			}
			if(StringUtils.isNotBlank(req.getError()) && req.getError().equals(error.getError())){
				return error.setMsg(error.getError());
			}
			if(StringUtils.isNotBlank(req.getHexCode()) && req.getHexCode().equals(error.getHexCode())){
				return error.setMsg(error.getError());
			}
		}
		for(ErrorCodeDto error : bussinessErrorCodeInterpreter.getBussinessCodes()){
			if(StringUtils.isNotBlank(req.getCode()) && req.getCode().equals(error.getCode())){
				return error.setMsg(error.getError());
			}
			if(StringUtils.isNotBlank(req.getError()) && req.getError().equals(error.getError())){
				return error.setMsg(error.getError());
			}
			if(StringUtils.isNotBlank(req.getHexCode()) && req.getHexCode().equals(error.getHexCode())){
				return error.setMsg(error.getError());
			}
		}
		return new ErrorCodeDto().setMsg("未找到该错误码!");
	}
}

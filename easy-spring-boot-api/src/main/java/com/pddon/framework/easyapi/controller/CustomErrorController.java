/**  
* Title ErrorController.java  
* Description  自定义系统未知错误处理接口响应
* @author danyuan
* @date Sep 10, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.controller;




import com.pddon.framework.easyapi.aspect.ApiExceptionAspector;
import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import com.pddon.framework.easyapi.controller.response.ErrorResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;








import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pddon.framework.easyapi.LanguageTranslateManager;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.context.RequestContext;
import springfox.documentation.annotations.ApiIgnore;

@Api(tags = "测试")
@ApiIgnore
@RestController
public class CustomErrorController implements ErrorController{
	@Autowired
	private LanguageTranslateManager languageTranslateManager;

	@Autowired
	private ApiExceptionAspector apiExceptionAspector;
	
	@ApiOperation(value = "默认错误处理接口")
	@ApiIgnore
    @RequestMapping("/error")
    public DefaultResponseWrapper<?> error(HttpServletRequest request, HttpServletResponse response){
		
		DefaultResponseWrapper<ErrorResponse> resp = null;
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String originalUri =  request.getRequestURI();
        if(statusCode != null && (statusCode != 200)){//是否是存在错误地址重定向URL
        	if(RequestContext.getContext().getApiInfo() != null 
        			&& !StringUtils.isEmpty(RequestContext.getContext().getApiInfo().getApiUri())){
        		originalUri = RequestContext.getContext().getApiInfo().getApiUri();
        	}else{
        		originalUri = (String) request.getAttribute("javax.servlet.forward.servlet_path");
        	}        	
        }
		Throwable ex = (Throwable)request.getAttribute("javax.servlet.error.exception");

		if(ex != null && ex instanceof Exception){
			// 异常处理
			DefaultResponseWrapper<?> responseWrapper = apiExceptionAspector.resolveRestControllerException(request, response, null, (Exception) ex);
			return responseWrapper;
		}
        if(statusCode == HttpStatus.NOT_FOUND.value()){        	      	
        	String systemError = languageTranslateManager.get(
    				ErrorCodes.NOT_FOUND.getMsgCode(), 
    				RequestContext.getContext().getLocale(),
    				originalUri);
    		resp = new DefaultResponseWrapper<>(ErrorCodes.NOT_FOUND);
            resp.setMsg(systemError);
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }else{
        	//其他未知错误
        	String systemError = languageTranslateManager.get(
    				ErrorCodes.UNKOWN_ERROR.getMsgCode(), 
    				RequestContext.getContext().getLocale(),
    				originalUri);
    		resp = new DefaultResponseWrapper<>(ErrorCodes.UNKOWN_ERROR);
            resp.setMsg(systemError);
            response.setStatus(statusCode);
        }
		
        return resp;
    }

	/**
	 * @author danyuan
	 */
	@Override
	public String getErrorPath() {
		 return "/error";
	}
    
}

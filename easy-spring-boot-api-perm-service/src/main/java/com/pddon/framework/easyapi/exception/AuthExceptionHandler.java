package com.pddon.framework.easyapi.exception;

import com.pddon.framework.easyapi.LanguageTranslateManager;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import com.pddon.framework.easyapi.exception.handler.CommonExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName: AuthExceptionHandler
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-19 23:46
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class AuthExceptionHandler implements CommonExceptionHandler {
    @Autowired
    private LanguageTranslateManager languageTranslateManager;

    @Override
    public boolean support(Exception e) {
        return e instanceof ShiroException || (e.getCause() instanceof ShiroException);
    }

    @Override
    public DefaultResponseWrapper<?> handle(HttpServletRequest request, HttpServletResponse response, Exception e) {
        ShiroException be = null;
        if(e instanceof ShiroException){
            be = (ShiroException) e;
        }else{
            be = (ShiroException) e.getCause();
        }
        String errorMsgCode = be.getMessage();
        String errorMsg = errorMsgCode;
        String locale = RequestContext.getContext().getLocale();
        if(e instanceof IncorrectCredentialsException){
            errorMsgCode = "账号或密码错误，请检查!";
        } else if(e instanceof UnauthorizedException){
            errorMsgCode = "您没有此功能的操作权限，请向管理员申请，分配权限后重新登录账号生效！";
        }
        errorMsg = languageTranslateManager.get(errorMsgCode, locale);
        ErrorCodes errorCode = ErrorCodes.getByMsgCode(errorMsgCode);
        if(log.isTraceEnabled()){
            log.trace("subError:{},exception:{},locale:{}",errorCode.getCode(), errorCode.getMsgCode(),locale);
        }
        return new DefaultResponseWrapper<>(errorCode.getCode(), errorMsg);
    }
}

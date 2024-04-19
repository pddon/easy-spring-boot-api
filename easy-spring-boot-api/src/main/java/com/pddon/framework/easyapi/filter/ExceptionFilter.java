package com.pddon.framework.easyapi.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pddon.framework.easyapi.aspect.ApiExceptionAspector;
import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import com.pddon.framework.easyapi.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ClassName: ExceptionFilter
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-16 23:10
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class ExceptionFilter implements Filter {

    @Autowired
    private ApiExceptionAspector apiExceptionAspector;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            // 异常处理
            DefaultResponseWrapper<?> responseWrapper = apiExceptionAspector.resolveRestControllerException((HttpServletRequest) request, (HttpServletResponse) response, null, e);
            //返回错误信息
            try{
                if(!response.isCommitted()){
                    ((HttpServletResponse) response).setHeader("Content-Type", "application/json;charset=utf-8");
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().print(objectMapper.writeValueAsString(responseWrapper));
                    response.getWriter().flush();
                }
            }catch (Exception ex){
                log.warn(IOUtils.getThrowableInfo(ex));
                throw ex;
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {

    }
}

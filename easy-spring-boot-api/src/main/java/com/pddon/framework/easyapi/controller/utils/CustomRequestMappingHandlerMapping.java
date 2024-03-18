package com.pddon.framework.easyapi.controller.utils;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @ClassName: CustomRequestMappingHandlerMapping
 * @Description:
 * @Author: Allen
 * @Date: 2024-03-18 21:43
 * @Addr: https://pddon.cn
 */
public class CustomRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
    @Override
    protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
        if(ReplaceRouteUtil.checkExistCover(lookupPath)){
            String redirectRoute = ReplaceRouteUtil.getRedirectRoute(lookupPath);
            request.setAttribute("redirectTag","1");
            request.setAttribute("redirectRoute",redirectRoute);
            request.setAttribute("lookupPath",lookupPath);
            lookupPath = redirectRoute;
        }
        return super.lookupHandlerMethod(lookupPath, request);
    }

    @Override
    protected RequestMappingInfo getMatchingMapping(RequestMappingInfo info, HttpServletRequest request) {
        Object redirectTag = request.getAttribute("redirectTag");
        if(redirectTag != null && redirectTag.toString().equals("1")){
            String redirectRoute = request.getAttribute("redirectRoute").toString();
            boolean check = false;
            if( info.getPatternsCondition()!=null){
                Set<String> set =  info.getPatternsCondition().getPatterns();
                if(set.size()>0){
                    String[] array = new String[set.size()];
                    array = set.toArray(array);
                    String pattern = array[0];
                    if(pattern.equals(redirectRoute)){
                        check = true;
                    }
                }
            }
            if(check){
                return info;
            }else{
                return super.getMatchingMapping(info, request);
            }
        }else{
            return super.getMatchingMapping(info, request);
        }
    }
}

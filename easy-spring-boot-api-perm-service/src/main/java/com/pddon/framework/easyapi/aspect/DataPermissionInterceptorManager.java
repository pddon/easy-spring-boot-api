/**  
* Title DataPermissionInterceptorManager.java
* Description  
* @author Allen
* @date Dec 17, 2023
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.aspect;

import com.pddon.framework.easyapi.DataPermissionMntService;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.annotation.RequireDataPermission;
import com.pddon.framework.easyapi.dao.dto.DataPermDto;
import com.pddon.framework.easyapi.dto.DataPermDtoList;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@Slf4j
public class DataPermissionInterceptorManager implements MethodInterceptor {

	@Autowired
	private DataPermissionMntService dataPermissionMntService;
	
	/**
	 * @param invocation
	 * @return
	 * @throws Throwable
	 * @author danyuan
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if(RequestContext.getContext().isSuperManager()){
			return invocation.proceed();
		}
		if(log.isTraceEnabled()){
			log.trace("进入数据权限拦截处理器切面!");
		}
		Object response = null;
		Class<?> targetClass = invocation.getThis().getClass();
		Method method = invocation.getMethod();
		RequireDataPermission requireDataPermission = AnnotationUtils.findAnnotation(targetClass, RequireDataPermission.class);
		if(requireDataPermission == null){
			requireDataPermission = AnnotationUtils.findAnnotation(method, RequireDataPermission.class);
		}
		if(requireDataPermission != null){
			//获取当前用户拥有的所有数据权限信息并设置到当前环境上下文
			// TODO:
			if(RequestContext.getContext().getSession() != null){
				DataPermDtoList dataPerms = dataPermissionMntService.getDataPermsByUserId(RequestContext.getContext().getSession().getUserId(), true);
				Map<String, Object[]> perms = dataPerms.getItems().stream().collect(Collectors.toMap(dto -> String.format("%s.%s", dto.getResName(), dto.getResField()), dto -> {
					return dto.getValues().toArray();
				}));
				RequestContext.getContext().setDataPermissions(perms);
			}
			RequestContext.getContext().setDataPermissionsEnable(true);
			RequestContext.getContext().setDataPermissionsInfo(requireDataPermission.tableFields(), requireDataPermission.tableFieldAlias());
		}
		return invocation.proceed();
	}

}

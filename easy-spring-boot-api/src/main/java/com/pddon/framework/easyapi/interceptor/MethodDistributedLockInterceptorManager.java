/**  
* Title MethodCacheInterceptorManager.java  
* Description  
* @author danyuan
* @date Dec 17, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.interceptor;

import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.LockDistributedManager;
import com.pddon.framework.easyapi.annotation.LockDistributed;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;
import com.pddon.framework.easyapi.utils.DistributedLockDaemonThreadUtil;
import com.pddon.framework.easyapi.utils.MethodInvokeUtil;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;


@Component
@Slf4j
public class MethodDistributedLockInterceptorManager implements MethodInterceptor {

	@Autowired
	@Lazy
	private LockDistributedManager lockDistributedManager;
	
	private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
	
	/**
	 * @param invocation
	 * @return
	 * @throws Throwable
	 * @author danyuan
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if(AopUtils.isAopProxy(invocation.getThis())){
			return invocation.proceed();
		}
		if(log.isTraceEnabled()){
			log.trace("进入方法分布式锁处理器切面!");
		}
		Object response = null;
        //获取锁注解信息
		CacheManager cacheManager = null;
		Method method = invocation.getMethod();
		LockDistributed lockDistributed = AnnotationUtils.findAnnotation(method, LockDistributed.class);
		if(lockDistributed != null){
			if(this.lockDistributedManager == null){
				if(log.isWarnEnabled()){
					log.warn("not found lockDistributedManager instance, add distributed lock failed.");
				}
				throw new BusinessException(ErrorCodes.NOT_FOUND_CONFIG).setParam(LockDistributedManager.class.getName());
			}
			if(!this.lockDistributedManager.support()){
				log.warn("不支持分布式锁，直接执行业务！");
				return invocation.proceed();
			}
			Class<?> targetClass = invocation.getThis().getClass();
			try{
				targetClass = AopProxyUtils.ultimateTargetClass(invocation.getThis());
			}catch (Exception e){}
			Object [] args=invocation.getArguments();
			String[] parameters = parameterNameDiscoverer.getParameterNames(method);
			String lockName = this.getLockName(lockDistributed, parameters, args, targetClass, method);
			String lockId = this.lockDistributedManager.lock(lockName, lockDistributed.acquireWaitingSeconds(), lockDistributed.timeoutSeconds());
			if(lockId == null){
				//获取锁失败，取消业务逻辑执行
				log.info("Obtain distributed lock failed, lockName:[{}]", lockName);
				return null;
			}
			DistributedLockDaemonThreadUtil.DaemonRefreshThread thread = null;
			try{
				//开启监听线程
				thread = DistributedLockDaemonThreadUtil.startDaemon(this.lockDistributedManager, lockName, lockId, lockDistributed.timeoutSeconds());
				//执行业务
				response = invocation.proceed();
			}finally {
				//否则直接执行正常业务逻辑
				if(log.isTraceEnabled()){
					log.trace("执行锁内业务成功[{}]-[{}]，执行解锁流程!", lockName, lockId);
				}
				if(thread != null){
					thread.terminate();
				}
				this.lockDistributedManager.unlock(lockName, lockId);
			}
		}else{
			log.warn("未找到分布式锁注解，请检查配置！");
			response = invocation.proceed();
		}
		
		return response;
	}

	private String getLockName(LockDistributed lockDistributed, String[] parameters, Object [] args, Class<?> targetClass, Method method){
		//先提取缓存前缀
		String prefix = lockDistributed.prefix();
		if(StringUtils.isBlank(prefix)){
			//自动生成缓存前缀
			StringBuffer buffer = new StringBuffer(targetClass.getSimpleName());
			buffer.append(":")
					.append(method.getName());
			prefix = buffer.toString();
		}
		if(StringUtils.isBlank(lockDistributed.id())){
			return prefix;
		}
		//提取参数信息
		Map<String, String> nameValueMap = new HashMap<>();
		int i = 0;
		String paramName = "";
		for(Object param : args){
			paramName = MethodInvokeUtil.getBaseTypeParamName(method.getParameterAnnotations()[i]);
			if(StringUtils.isBlank(paramName)){
				if(parameters != null){
					paramName = parameters[i];
				}else{
					paramName = "p"+i;
				}
			}
			nameValueMap.putAll(BeanPropertyUtil.objToStringMap(param, paramName));
			i++;
		}
		String id = nameValueMap.get(lockDistributed.id());
		if(StringUtils.isBlank(id)){
			log.warn("分布式锁注解上id参数设置错误，未找到参数信息:[{}]", lockDistributed.id());
			throw new BusinessException(ErrorCodes.NOT_FOUND_CONFIG).setParam("分布式锁ID配置错误:"+lockDistributed.id());
		}
		StringBuffer key = new StringBuffer(prefix).append(":").append(id);
		return key.toString();
	}

}

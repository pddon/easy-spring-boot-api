/**  
* Title ApiLogInterceptor.java  
* Description  打印接口调用日志拦截器
* @author danyuan
* @date Oct 18, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.interceptor.impl;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pddon.framework.easyapi.InvokeApiLogManager;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.ApiInfo;
import com.pddon.framework.easyapi.dto.ApiInvokeLog;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.interceptor.ApiMethodInterceptor;
import com.pddon.framework.easyapi.utils.IOUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class ApiLogInterceptor implements ApiMethodInterceptor {
	/**
	 * 最大允许打印的请求参数日志长度
	 */
	private static final int MAX_REQ_LOG_SIZE = 1024;
	/**
	 * 最大允许打印的响应日志长度
	 */
	private static final int MAX_RESP_LOG_SIZE = 1024;
	private static final String REQUEST_TAIL = "//...";
	private static final String RESPONSE_TAIL = "\n//...\n}";
	private static String INVOKE_REQUEST_LOG = "接口请求：{}({}【{}】),版本号:{},请求参数：[{}]";
	private static String INVOKE_RESPONSE_LOG = "接口响应：{}({}【{}】),版本号:{},请求参数：[{}],响应参数：{}";
	@Autowired
	private InvokeApiLogManager invokeApiLogManager;
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * 日志序列化上限标记异常：超过日志长度上限时中断序列化，
	 * 避免为超大对象构造完整JSON字符串后再丢弃大部分内容
	 */
	private static final class LogLimitExceededException extends IOException {
		private static final long serialVersionUID = 1L;
		LogLimitExceededException() {
			super("log content exceeds limit", null);
		}
	}

	/**
	 * 有界字符缓冲：累计写入超过limit个字符时抛出LogLimitExceededException。
	 * 已写入的前limit个字符与完整序列化的前缀完全一致，因此截断结果与改造前逐字符相同。
	 */
	private static final class BoundedStringWriter extends Writer {
		private final StringBuilder buffer;
		private final int limit;
		private volatile boolean limitExceeded;

		BoundedStringWriter(int limit) {
			this.limit = limit;
			this.buffer = new StringBuilder(Math.min(limit, 1024));
		}

		String content() {
			return buffer.toString();
		}

		boolean isLimitExceeded() {
			return limitExceeded;
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			int remaining = limit - buffer.length();
			if (remaining <= 0) {
				limitExceeded = true;
				throw new LogLimitExceededException();
			}
			int writable = Math.min(len, remaining);
			buffer.append(cbuf, off, writable);
			if (writable < len) {
				limitExceeded = true;
				throw new LogLimitExceededException();
			}
		}

		@Override
		public void write(String str, int off, int len) throws IOException {
			int remaining = limit - buffer.length();
			if (remaining <= 0) {
				limitExceeded = true;
				throw new LogLimitExceededException();
			}
			int writable = Math.min(len, remaining);
			buffer.append(str, off, off + writable);
			if (writable < len) {
				limitExceeded = true;
				throw new LogLimitExceededException();
			}
		}

		@Override
		public void write(int c) throws IOException {
			if (buffer.length() >= limit) {
				limitExceeded = true;
				throw new LogLimitExceededException();
			}
			buffer.append((char) c);
		}

		@Override
		public void flush() {
			//无需刷新：内容直接写入内存缓冲
		}

		@Override
		public void close() {
			//无需关闭：内容直接写入内存缓冲
		}
	}

	/**
	 * 复用的格式化输出Writer，避免每次请求都重新创建ObjectWriter
	 */
	private volatile ObjectWriter prettyWriter;

	private ObjectWriter prettyWriter(){
		ObjectWriter writer = this.prettyWriter;
		if(writer == null){
			writer = objectMapper.writerWithDefaultPrettyPrinter();
			this.prettyWriter = writer;
		}
		return writer;
	}

	/**
	 * 序列化对象并在达到limit字符时立即中断，返回与"完整序列化后截断"逐字符一致的结果。
	 * 与旧实现相比，超长对象的序列化不再构造完整字符串，只保留前limit+1个字符。
	 */
	private String boundedJsonToString(Object value, int limit, String tail) throws JsonProcessingException {
		BoundedStringWriter writer = new BoundedStringWriter(limit + 1);
		try {
			prettyWriter().writeValue(writer, value);
		} catch (JsonProcessingException e) {
			if (writer.isLimitExceeded()) {
				String truncated = writer.content();
				return truncated.length() > limit ? truncated.substring(0, limit) + tail : truncated;
			}
			throw e;
		} catch (IOException e) {
			if (writer.isLimitExceeded()) {
				String truncated = writer.content();
				return truncated.length() > limit ? truncated.substring(0, limit) + tail : truncated;
			}
			throw JsonMappingException.fromUnexpectedIOE(e);
		}
		String json = writer.content();
		return json.length() > limit ? json.substring(0, limit) + tail : json;
	}

	/**
	 * 将单个请求参数序列化追加到buffer，最多写入overallLimit个字符（含buffer中已有内容）。
	 * 单个参数在达到上限时中断序列化并返回true，调用方应立即停止后续参数的序列化。
	 */
	private boolean appendBoundedJson(StringBuilder buffer, Object value, int overallLimit) throws JsonProcessingException {
		int remaining = overallLimit - buffer.length();
		if (remaining <= 0) {
			return true;
		}
		BoundedStringWriter writer = new BoundedStringWriter(remaining);
		try {
			prettyWriter().writeValue(writer, value);
		} catch (JsonProcessingException e) {
			if (writer.isLimitExceeded()) {
				buffer.append(writer.content());
				return true;
			}
			throw e;
		} catch (IOException e) {
			if (writer.isLimitExceeded()) {
				buffer.append(writer.content());
				return true;
			}
			throw JsonMappingException.fromUnexpectedIOE(e);
		}
		buffer.append(writer.content());
		return false;
	}

	/**
	 * 生成已截断的请求参数日志：超过MAX_REQ_LOG_SIZE即停止序列化剩余参数。
	 * 输出与objectArrToJSONString(...)后再截断完全一致（字段顺序、逗号连接、尾部标记均不变）。
	 * 多留1个字符用于末尾逗号，保证"恰好等于上限"时不被误判为截断。
	 */
	private String objectArrToLogString(List<ApiRequestParameter> arr){
		if(arr == null || arr.size() == 0){
			return null;
		}
		int overallLimit = MAX_REQ_LOG_SIZE + 2;
		StringBuilder buffer = new StringBuilder();
		boolean truncated = false;
		for(ApiRequestParameter param : arr){
			if(truncated){
				break;
			}
			if(param.getParam() != null && param.getParam() instanceof MultipartFile){
				MultipartFile file = (MultipartFile) param.getParam();
				buffer.append(file.getOriginalFilename()).append(",");
				if(buffer.length() > MAX_REQ_LOG_SIZE + 1){
					truncated = true;
				}
				continue;
			}
			boolean limitHit;
			try {
				limitHit = appendBoundedJson(buffer, param.getParam(), overallLimit);
			} catch (JsonProcessingException e) {
				if(log.isTraceEnabled()){
					log.trace(IOUtils.getThrowableInfo(e));
				}
				continue;
			}
			if(limitHit){
				truncated = true;
				break;
			}
			buffer.append(",");
			if(buffer.length() > MAX_REQ_LOG_SIZE + 1){
				truncated = true;
			}
		}
		if(truncated){
			return buffer.substring(0, MAX_REQ_LOG_SIZE) + REQUEST_TAIL;
		}
		if(buffer.length() > 1){
			return buffer.substring(0, buffer.length() - 1);
		}
		return buffer.toString();
	}
	/**
	 * 将系统参数转换为日志对象。
	 * convertValue内部同样先序列化再反序列化，但省去了中间字符串；
	 * 转换失败时解包为原来的JsonProcessingException，保持异常语义不变。
	 */
	private ApiInvokeLog toApiInvokeLog(Map<String, Object> systemParams) throws JsonProcessingException {
		try {
			return objectMapper.convertValue(systemParams, ApiInvokeLog.class);
		} catch (IllegalArgumentException e) {
			Throwable cause = e.getCause();
			if (cause instanceof JsonProcessingException) {
				throw (JsonProcessingException) cause;
			}
			throw e;
		}
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void preInvoke(List<ApiRequestParameter> requestParams, Method method,
			Class<?> targetClass) throws Throwable {
		if(log.isTraceEnabled()){
			log.trace("开始打印调用接口入参！");
		}
		
		//ObjectMapper mapper = new ObjectMapper();
		//objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		Map<String, Object> systemParams = RequestContext.getContext().getAttachments();
		//直接做对象转换，避免先序列化成字符串再反序列化的额外开销
		ApiInvokeLog apiLog = toApiInvokeLog(systemParams);
		BeanUtils.copyProperties(RequestContext.getContext().getApiInfo(), apiLog);	
		invokeApiLogManager.save(apiLog);
		if(!log.isInfoEnabled()){
			//关闭INFO日志时无需再拼接请求日志内容
			return;
		}
		ApiInfo info = RequestContext.getContext().getApiInfo();
		String reqStr = requestLogContent(requestParams);
		log.info(INVOKE_REQUEST_LOG, info.getApiName(), info.getApiUri(), info.getApiMethod(), info.getApiVersion(), reqStr);
	}

	/**
	 * @author danyuan
	 */
	@Override
	public Object afterInvoke(List<ApiRequestParameter> requestParams, Object resp,
			Method method, Class<?> targetClass) throws Throwable {
		//outputResonseLog(requestParams, resp, targetClass);
		return resp;
	}
	
	public void outputResponseLog(List<ApiRequestParameter> requestParams, Object resp,
			Class<?> targetClass) throws JsonProcessingException{
		if(log.isTraceEnabled()){
			log.trace("开始打印调用接口返回信息！");
		}
		if(!log.isInfoEnabled()){
			//关闭INFO日志时无需再序列化响应内容
			return;
		}
		ApiInfo info = RequestContext.getContext().getApiInfo();
		//ObjectMapper mapper = new ObjectMapper();
		//mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		// 序列化 
	    String respStr = null;
	    if(resp != null){	    	
	    	respStr = boundedJsonToString(resp, MAX_RESP_LOG_SIZE, RESPONSE_TAIL);
	    }
		String reqStr = requestLogContent(requestParams);
		log.info(INVOKE_RESPONSE_LOG, info.getApiName(), info.getApiUri(), info.getApiMethod(), info.getApiVersion(), reqStr, respStr);
	}

	/**
	 * 请求参数日志只生成一次：preInvoke生成后存入RequestContext，响应阶段直接复用，
	 * 避免同一请求参数被重复序列化。
	 */
	private String requestLogContent(List<ApiRequestParameter> requestParams){
		RequestContext context = RequestContext.getContext();
		String cached = context.getRequestLogContent();
		if(cached != null){
			return cached;
		}
		String content = objectArrToLogString(requestParams);
		context.setRequestLogContent(content);
		return content;
	}

	public String objectArrToJSONString(List<ApiRequestParameter> arr){
		if(arr != null && arr.size() > 0){
			//ObjectMapper mapper = new ObjectMapper();
			//mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
			StringBuilder buffer = new StringBuilder();
			for(ApiRequestParameter param : arr){
				if(param.getParam() != null && param.getParam() instanceof MultipartFile){
					MultipartFile file = (MultipartFile) param.getParam();
					buffer.append(file.getOriginalFilename()).append(",");
					continue;
				}
				try {
					buffer.append(prettyWriter().writeValueAsString(param.getParam())).append(",");
				} catch (JsonProcessingException e) {
					if(log.isTraceEnabled()){
						log.trace(IOUtils.getThrowableInfo(e));
					}
				}
			}
			if(buffer.length() > 1){
				return buffer.substring(0, buffer.length() - 1);
			}
			return buffer.toString();
		}
		return null;
	}
	/**
	 * @author danyuan
	 */
	@Override
	public int order() {
		return 999;//最后才调用这个拦截器，确保所有参数信息都能获取到
	}

}

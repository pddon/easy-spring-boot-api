package com.pddon.framework.easyapi.regression;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;

import com.pddon.framework.easyapi.InvokeApiLogManager;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.ApiInfo;
import com.pddon.framework.easyapi.dto.ApiInvokeLog;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pddon.framework.easyapi.utils.IOUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * HEAD(b766ccd) 中 ApiLogInterceptor#preInvoke 的逐行副本，仅用于回归对照测试。
 */
@Slf4j
final class LegacyApiLogInterceptor {

    private static final int MAX_REQ_LOG_SIZE = 1024;
    private static final String REQUEST_TAIL = "//...";
    private static final String INVOKE_REQUEST_LOG = "接口请求：{}({}【{}】),版本号:{},请求参数：[{}]";

    private LegacyApiLogInterceptor() {
    }

    static ApiInvokeLog preInvoke(InvokeApiLogManager invokeApiLogManager, ObjectMapper objectMapper,
                                  List<ApiRequestParameter> requestParams) throws Throwable {
        Map<String, Object> systemParams = RequestContext.getContext().getAttachments();
        String paramValues = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(systemParams);
        ApiInvokeLog apiLog = objectMapper.readValue(paramValues, ApiInvokeLog.class);
        BeanUtils.copyProperties(RequestContext.getContext().getApiInfo(), apiLog);
        invokeApiLogManager.save(apiLog);

        ApiInfo info = RequestContext.getContext().getApiInfo();
        String reqStr = objectArrToJSONString(objectMapper, requestParams);
        if (reqStr != null && (reqStr.length() > MAX_REQ_LOG_SIZE)) {
            reqStr = reqStr.substring(0, MAX_REQ_LOG_SIZE) + REQUEST_TAIL;
        }
        log.info(INVOKE_REQUEST_LOG, info.getApiName(), info.getApiUri(), info.getApiMethod(), info.getApiVersion(), reqStr);
        return apiLog;
    }

    private static String objectArrToJSONString(ObjectMapper objectMapper, List<ApiRequestParameter> arr) {
        if (arr != null && arr.size() > 0) {
            StringBuilder buffer = new StringBuilder();
            for (ApiRequestParameter param : arr) {
                if (param.getParam() != null && param.getParam() instanceof MultipartFile) {
                    MultipartFile file = (MultipartFile) param.getParam();
                    buffer.append(file.getOriginalFilename()).append(",");
                    continue;
                }
                try {
                    buffer.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(param.getParam())).append(",");
                } catch (Exception e) {
                    if (log.isTraceEnabled()) {
                        log.trace(IOUtils.getThrowableInfo(e));
                    }
                }
            }
            if (buffer.length() > 1) {
                return buffer.substring(0, buffer.length() - 1);
            }
            return buffer.toString();
        }
        return null;
    }
}

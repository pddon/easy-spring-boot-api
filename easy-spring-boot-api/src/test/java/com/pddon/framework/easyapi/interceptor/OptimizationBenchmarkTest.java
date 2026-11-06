package com.pddon.framework.easyapi.interceptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.KotlinReflectionParameterNameDiscoverer;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.PrioritizedParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.pddon.framework.easyapi.dto.ApiInvokeLog;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.dto.ComplextRequest;
import com.pddon.framework.easyapi.dto.SkuCurrency;
import com.pddon.framework.easyapi.dto.SkuItem;
import com.pddon.framework.easyapi.interceptor.impl.ApiLogInterceptor;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;
import com.pddon.framework.easyapi.utils.ClassOriginCheckUtil;
import com.pddon.framework.easyapi.utils.SpringBeanUtil;

/**
 * 轻量级可重复基准测试：warmup后多次迭代，输出中位数与p95（纳秒/次）。
 * 本轮优化后的路径直接调用生产代码；优化前的路径使用保留旧逻辑的测试替身，
 * 并通过 verifyOldImplementationsMatchProduction 做等价性冒烟对比，降低替身漂移风险。
 * 运行方式：mvn -pl easy-spring-boot-api -am test -DskipTests=false -Dtest=OptimizationBenchmarkTest
 */
public class OptimizationBenchmarkTest {

    private static final int WARMUP_ITERATIONS = 5;
    /**
     * 迭代次数取50，p95落在第4大值上，避免少数GC/调度停顿直接决定分位数
     */
    private static final int MEASURE_ITERATIONS = 50;
    private static final int OPS_PER_ITERATION = 2000;

    /** 防止JIT将结果优化掉 */
    private volatile static Object sink;

    /**
     * 优化前生产代码使用的参数名解析器（构造一次后复用），与ApiInvokeMethodInterceptorManager旧实现一致
     */
    private static final PrioritizedParameterNameDiscoverer OLD_DISCOVERER = oldDiscoverer();

    private static PrioritizedParameterNameDiscoverer oldDiscoverer() {
        PrioritizedParameterNameDiscoverer discoverer = new PrioritizedParameterNameDiscoverer();
        discoverer.addDiscoverer(new StandardReflectionParameterNameDiscoverer());
        discoverer.addDiscoverer(new LocalVariableTableParameterNameDiscoverer());
        discoverer.addDiscoverer(new KotlinReflectionParameterNameDiscoverer());
        return discoverer;
    }

    @Test
    public void runAllBenchmarks() throws Exception {
        benchmarkBeanPropertyUtil();
        benchmarkInterceptorMethodMeta();
        benchmarkLogSerialization();
        benchmarkBasePackageScan();
    }

    /**
     * 生产路径与旧实现结果必须一致，防止测试替身与生产实现漂移
     */
    @Test
    public void verifyOldImplementationsMatchProduction() throws Exception {
        ComplextRequest request = sampleRequest();
        Assert.assertEquals(oldObjToStringMap(request, ""), BeanPropertyUtil.objToStringMap(request, ""));

        Method method = OptimizationBenchmarkTest.class.getDeclaredMethod("sampleApi", String.class, Integer.class, String.class);
        ApiInvokeMethodInterceptorManager.MethodMeta meta = ApiInvokeMethodInterceptorManager.getMethodMeta(method);
        Assert.assertArrayEquals(OLD_DISCOVERER.getParameterNames(method), meta.paramNames);
        Assert.assertEquals(Arrays.asList(method.getAnnotations()), Arrays.asList(meta.methodAnnos));
        Assert.assertEquals(Arrays.deepToString(method.getParameterAnnotations()), Arrays.deepToString(meta.parameterAnnotations));
        if (meta.paramNames != null) {
            //参数名可用时必须命中缓存：再次获取应返回同一个元数据实例
            Assert.assertSame(meta, ApiInvokeMethodInterceptorManager.getMethodMeta(method));
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> systemParams = sampleSystemParams();
        Assert.assertEquals(objectMapper.readValue(objectMapper.writeValueAsString(systemParams), ApiInvokeLog.class),
                objectMapper.convertValue(systemParams, ApiInvokeLog.class));

        List<ApiRequestParameter> params = sampleLogParams();
        ApiLogInterceptor logInterceptor = new ApiLogInterceptor();
        ReflectionTestUtils.setField(logInterceptor, "objectMapper", objectMapper);
        Assert.assertEquals(oldObjectArrToJSONString(objectMapper, params), logInterceptor.objectArrToJSONString(params));

        GenericApplicationContext context = startSampleContext();
        ApplicationContext previous = SpringBeanUtil.getApplicationContext();
        try {
            replaceSpringContext(context);
            EasyApiConfig config = new EasyApiConfig();
            Assert.assertEquals(oldAllBasePackages(config), config.getAllBasePackages());
        } finally {
            restoreSpringContext(previous);
            context.close();
        }
    }

    // ------------------------------------------------------------------
    // 1. BeanPropertyUtil 反射转换
    // ------------------------------------------------------------------
    private void benchmarkBeanPropertyUtil() {
        ComplextRequest request = sampleRequest();
        double[] oldNs = measure(() -> oldObjToStringMap(request, ""),
                () -> oldObjToStringMap(request, ""));
        // 预热一次，确保内部缓存已建立后再测量
        BeanPropertyUtil.objToStringMap(request, "");
        double[] newNs = measure(() -> BeanPropertyUtil.objToStringMap(request, ""),
                () -> BeanPropertyUtil.objToStringMap(request, ""));
        print("BeanPropertyUtil.objToStringMap", oldNs, newNs);
    }

    private ComplextRequest sampleRequest() {
        ComplextRequest req = new ComplextRequest();
        List<SkuCurrency> priceInfos = new ArrayList<>();
        priceInfos.add(new SkuCurrency("CNY", new BigDecimal("3.60")));
        priceInfos.add(new SkuCurrency("USD", new BigDecimal("0.56")));
        List<SkuItem> skuList = new ArrayList<>();
        skuList.add(new SkuItem(1, "skuName1", new BigDecimal("3.60"), priceInfos));
        skuList.add(new SkuItem(3, "skuName2", new BigDecimal("88.20"), priceInfos));
        Map<String, SkuItem> skuTagMap = new HashMap<>();
        skuTagMap.put("goodSku 1", new SkuItem(6, "skuName6", new BigDecimal("22.20"), priceInfos));
        skuTagMap.put("goodSku 2", new SkuItem(8, "skuName8", new BigDecimal("33.00"), priceInfos));
        req.setIntroArr(new String[]{"intro test 1", "intro test 2", "intro test 3"});
        req.setSkuList(skuList);
        req.setSkuTagMap(skuTagMap);
        req.setTotalSku(4);
        req.setDesc("这是一个简单的测试!");
        req.setTest("aaa");
        req.setFlag(true);
        return req;
    }

    /**
     * 保留旧逻辑的测试替身：每次调用都重新扫描字段、重新查找getter，不做任何缓存。
     */
    private Map<String, String> oldObjToStringMap(Object object, String parentKey) {
        Set<String> hashCodeSet = new HashSet<>();//防止对象间引用关系存在环状结构
        return oldObjToStringMap(object, parentKey, hashCodeSet);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map<String, String> oldObjToStringMap(Object object, String parentKey, Set<String> hashCodeSet) {
        Map<String, String> resultMap = new HashMap<>();
        if (object == null) {
            return resultMap;
        }
        if (!BeanPropertyUtil.isBaseType(object) && !(object instanceof Map) && !(object instanceof java.util.Collection)
                && !(object instanceof Iterator)) {
            if (hashCodeSet.contains(String.valueOf(object.hashCode()))) {
                return resultMap;
            }
            hashCodeSet.add(String.valueOf(object.hashCode()));
        }
        if (parentKey == null) {
            parentKey = "";
        }
        if (BeanPropertyUtil.isBaseType(object)) {
            if (object.getClass().equals(Date.class)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getDefault());
                resultMap.put(parentKey, sdf.format((Date) object));
            } else {
                resultMap.put(parentKey, object.toString());
            }
        } else if (object instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) object;
            for (Object key : map.keySet()) {
                Object val = map.get(key);
                String currentKey = StringUtils.isEmpty(parentKey) ? key.toString() : parentKey + "." + key;
                if (val != null) {
                    if (BeanPropertyUtil.isBaseType(val)) {
                        resultMap.put(currentKey, val.toString());
                    } else if (val instanceof java.io.Serializable) {
                        resultMap.putAll(oldObjToStringMap(val, currentKey, hashCodeSet));
                    }
                }
            }
        } else if (object.getClass().isArray() && !BeanPropertyUtil.isIgnoreType(object.getClass().getComponentType())) {
            Object[] arr = (Object[]) object;
            for (int i = 0; i < arr.length; i++) {
                String currentKey = StringUtils.isEmpty(parentKey) ? "" : parentKey + "[" + i + "]";
                if (arr[i] != null) {
                    resultMap.putAll(oldObjToStringMap(arr[i], currentKey, hashCodeSet));
                }
            }
        } else if (object instanceof List<?>) {
            List<Object> list = (List<Object>) object;
            for (int i = 0; i < list.size(); i++) {
                String currentKey = StringUtils.isEmpty(parentKey) ? "" : parentKey + "[" + i + "]";
                if (list.get(i) != null) {
                    resultMap.putAll(oldObjToStringMap(list.get(i), currentKey, hashCodeSet));
                }
            }
        } else if (object instanceof Set<?>) {
            Set<Object> set = (Set<Object>) object;
            Set<Object> newSet = new HashSet<>();
            int i = 0;
            for (Object subObj : set) {
                String currentKey = StringUtils.isEmpty(parentKey) ? "" : parentKey + "[" + i++ + "]";
                if (subObj != null) {
                    resultMap.putAll(oldObjToStringMap(subObj, currentKey, hashCodeSet));
                }
            }
            set.removeAll(set);
            set.addAll(newSet);
        } else {
            // 旧实现：每次调用重新扫描继承链上的所有字段
            List<Field> fields = oldAllFields(object.getClass());
            for (Field field : fields) {
                // 旧实现：每个字段都重新反射查找public getter
                if (oldHasPublicGetter(field, object)) {
                    try {
                        field.setAccessible(true);
                        Object subObj = field.get(object);
                        String currentKey = StringUtils.isEmpty(parentKey) ? field.getName() : parentKey + "." + field.getName();
                        if (subObj != null) {
                            resultMap.putAll(oldObjToStringMap(subObj, currentKey, hashCodeSet));
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return resultMap;
    }

    private List<Field> oldAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != null && !"java.lang.Object".equals(currentClass.getName())) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }
        return fields;
    }

    private boolean oldHasPublicGetter(Field field, Object obj) {
        String name = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        if (oldGetMethod(obj.getClass(), "get" + name) != null) {
            return true;
        }
        if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
            return oldGetMethod(obj.getClass(), "is" + name) != null;
        }
        return false;
    }

    private Method oldGetMethod(Class<?> clazz, String methodName) {
        Class<?> currentClass = clazz;
        while (currentClass != null && !"java.lang.Object".equals(currentClass.getName())) {
            try {
                return currentClass.getDeclaredMethod(methodName);
            } catch (Exception ignored) {
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    // ------------------------------------------------------------------
    // 2. 拦截器方法元数据解析
    // ------------------------------------------------------------------
    @SuppressWarnings("unused")
    private String sampleApi(String name, Integer age, String note) {
        return name;
    }

    private void benchmarkInterceptorMethodMeta() throws Exception {
        Method method = OptimizationBenchmarkTest.class.getDeclaredMethod("sampleApi", String.class, Integer.class, String.class);
        // 优化后：直接调用生产代码
        double[] newNs = measure(() -> sink = ApiInvokeMethodInterceptorManager.getMethodMeta(method),
                () -> sink = ApiInvokeMethodInterceptorManager.getMethodMeta(method));
        // 优化前：每次请求都重新解析参数名与注解，解析器实例复用（与旧生产代码一致）
        double[] oldNs = measure(() -> sink = oldMethodMeta(method), () -> sink = oldMethodMeta(method));
        print("ApiInvokeMethodInterceptorManager.methodMeta", oldNs, newNs);
    }

    private Object[] oldMethodMeta(Method method) {
        return new Object[]{OLD_DISCOVERER.getParameterNames(method), method.getAnnotations(), method.getParameterAnnotations()};
    }

    // ------------------------------------------------------------------
    // 3. 日志序列化
    // ------------------------------------------------------------------
    private Map<String, Object> sampleSystemParams() {
        Map<String, Object> systemParams = new HashMap<>();
        systemParams.put("appId", "webApp");
        systemParams.put("channelId", "default");
        systemParams.put("locale", "zh_CN");
        systemParams.put("sessionId", "session-123456");
        systemParams.put("timestamp", System.currentTimeMillis());
        return systemParams;
    }

    private List<ApiRequestParameter> sampleLogParams() {
        List<ApiRequestParameter> params = new ArrayList<>();
        ApiRequestParameter p1 = new ApiRequestParameter();
        p1.setIndex(0);
        p1.setParamName("request");
        p1.setParam(new ApiInvokeLog().setApiName("TestController::sampleApi"));
        params.add(p1);
        ApiRequestParameter p2 = new ApiRequestParameter();
        p2.setIndex(1);
        p2.setParamName("orderId");
        p2.setParam("easy_api_client001");
        params.add(p2);
        return params;
    }

    private void benchmarkLogSerialization() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> systemParams = sampleSystemParams();

        // 优化前：先序列化为JSON字符串，再反序列化为对象
        double[] oldNs = measure(() -> {
            try {
                sink = objectMapper.readValue(objectMapper.writeValueAsString(systemParams), ApiInvokeLog.class);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }, () -> {
            try {
                sink = objectMapper.readValue(objectMapper.writeValueAsString(systemParams), ApiInvokeLog.class);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        // 优化后：生产代码中使用的convertValue直接转换
        double[] newNs = measure(() -> sink = objectMapper.convertValue(systemParams, ApiInvokeLog.class),
                () -> sink = objectMapper.convertValue(systemParams, ApiInvokeLog.class));

        // 日志格式化：直接调用生产方法（内部复用ObjectWriter） vs 旧实现（每次新建ObjectWriter）
        ApiLogInterceptor logInterceptor = new ApiLogInterceptor();
        ReflectionTestUtils.setField(logInterceptor, "objectMapper", objectMapper);
        List<ApiRequestParameter> params = sampleLogParams();
        double[] oldWriterNs = measure(() -> sink = oldObjectArrToJSONString(objectMapper, params),
                () -> sink = oldObjectArrToJSONString(objectMapper, params));
        double[] newWriterNs = measure(() -> sink = logInterceptor.objectArrToJSONString(params),
                () -> sink = logInterceptor.objectArrToJSONString(params));
        print("ApiLogInterceptor.convertValue", oldNs, newNs);
        print("ApiLogInterceptor.prettyWriter", oldWriterNs, newWriterNs);
    }

    private String oldObjectArrToJSONString(ObjectMapper objectMapper, List<ApiRequestParameter> arr) {
        if (arr != null && arr.size() > 0) {
            StringBuffer buffer = new StringBuffer();
            for (ApiRequestParameter param : arr) {
                try {
                    buffer.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(param.getParam())).append(",");
                } catch (Exception ignored) {
                }
            }
            if (buffer.length() > 1) {
                return buffer.substring(0, buffer.length() - 1);
            }
            return buffer.toString();
        }
        return null;
    }

    // ------------------------------------------------------------------
    // 4. 根包/Bean扫描
    // ------------------------------------------------------------------
    @SpringBootApplication
    static class SampleApplication {
    }

    private GenericApplicationContext startSampleContext() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("sampleApplication", SampleApplication.class);
        context.registerBean("anotherBean", String.class, () -> "bean");
        context.refresh();
        return context;
    }

    private void replaceSpringContext(ApplicationContext context) {
        ReflectionTestUtils.setField(SpringBeanUtil.class, "applicationContext", context);
        ReflectionTestUtils.setField(SpringBeanUtil.class, "beanFactory", context.getAutowireCapableBeanFactory());
    }

    private void restoreSpringContext(ApplicationContext previous) {
        ReflectionTestUtils.setField(SpringBeanUtil.class, "applicationContext", previous);
        ReflectionTestUtils.setField(SpringBeanUtil.class, "beanFactory",
                previous == null ? null : previous.getAutowireCapableBeanFactory());
    }

    private Set<String> oldAllBasePackages(EasyApiConfig config) {
        Set<String> set = new HashSet<>();
        if (!StringUtils.isEmpty(config.getBasePackages())) {
            for (String pkg : config.getBasePackages().split(",")) {
                set.add(pkg);
            }
        }
        set.add(ClassOriginCheckUtil.getDeafultBasePackage());
        return set;
    }

    private void benchmarkBasePackageScan() {
        GenericApplicationContext context = startSampleContext();
        ApplicationContext previousContext = SpringBeanUtil.getApplicationContext();
        try {
            replaceSpringContext(context);
            //生产实现：EasyApiConfig.getAllBasePackages()，首次调用后缓存
            EasyApiConfig config = new EasyApiConfig();
            config.getAllBasePackages();
            double[] newNs = measure(() -> sink = config.getAllBasePackages(),
                    () -> sink = config.getAllBasePackages());
            // 优化前：每次调用都重新构造集合，并遍历容器中所有Bean的注解查找启动类包名
            double[] oldNs = measure(() -> sink = oldAllBasePackages(config),
                    () -> sink = oldAllBasePackages(config));
            print("EasyApiConfig.getAllBasePackages", oldNs, newNs);
        } finally {
            restoreSpringContext(previousContext);
            context.close();
        }
    }

    // ------------------------------------------------------------------
    // 计时工具
    // ------------------------------------------------------------------
    private double[] measure(Runnable warmupBody, Runnable body) {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            for (int j = 0; j < OPS_PER_ITERATION; j++) {
                warmupBody.run();
            }
        }
        double[] samples = new double[MEASURE_ITERATIONS];
        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            long start = System.nanoTime();
            for (int j = 0; j < OPS_PER_ITERATION; j++) {
                body.run();
            }
            long elapsed = System.nanoTime() - start;
            samples[i] = (double) elapsed / OPS_PER_ITERATION;
        }
        Arrays.sort(samples);
        return samples;
    }

    private void print(String name, double[] oldSamples, double[] newSamples) {
        double oldMedian = median(oldSamples);
        double newMedian = median(newSamples);
        double oldP95 = percentile(oldSamples, 0.95);
        double newP95 = percentile(newSamples, 0.95);
        double speedup = newMedian > 0 ? oldMedian / newMedian : 0;
        System.out.println(String.format(
                "[BENCH] %-42s old median=%10.1f ns/op p95=%10.1f | new median=%10.1f ns/op p95=%10.1f | speedup=%.2fx",
                name, oldMedian, oldP95, newMedian, newP95, speedup));
    }

    private double median(double[] sorted) {
        int mid = sorted.length / 2;
        if (sorted.length % 2 == 1) {
            return sorted[mid];
        }
        return (sorted[mid - 1] + sorted[mid]) / 2.0;
    }

    private double percentile(double[] sorted, double ratio) {
        int index = (int) Math.ceil(ratio * sorted.length) - 1;
        if (index < 0) {
            index = 0;
        }
        if (index >= sorted.length) {
            index = sorted.length - 1;
        }
        return sorted[index];
    }
}

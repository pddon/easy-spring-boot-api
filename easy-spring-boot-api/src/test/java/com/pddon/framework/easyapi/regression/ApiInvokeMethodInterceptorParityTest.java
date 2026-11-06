package com.pddon.framework.easyapi.regression;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.KotlinReflectionParameterNameDiscoverer;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.PrioritizedParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import com.pddon.framework.easyapi.annotation.IgnoreSign;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.interceptor.ApiInvokeMethodInterceptorManager;
import com.pddon.framework.easyapi.properties.EasyApiConfig;

/**
 * 优化点3的对照测试：方法元数据解析（参数名、注解、拦截判断）旧实现 vs 生产实现。
 */
public class ApiInvokeMethodInterceptorParityTest {

    private static final ParameterNameDiscoverer LEGACY_DISCOVERER = createLegacyDiscoverer();

    @Test
    public void testMetadataMatchesLegacyForFixtureMethods() throws Exception {
        for (Class<?> type : Arrays.asList(RestControllerFixture.class, ResponseBodyFixture.class, PlainFixture.class)) {
            for (Method method : type.getDeclaredMethods()) {
                LegacyMeta legacy = parseLegacy(method);
                Object producedMeta = parseProduced(method);

                Assert.assertArrayEquals("paramNames@" + method, legacy.paramNames,
                        (String[]) field(producedMeta, "paramNames"));
                Assert.assertArrayEquals("methodAnnos@" + method, legacy.methodAnnos,
                        (Annotation[]) field(producedMeta, "methodAnnos"));
                Annotation[][] producedParamAnnos = (Annotation[][]) field(producedMeta, "parameterAnnotations");
                Assert.assertEquals("parameterAnnotations.length@" + method,
                        legacy.parameterAnnotations.length, producedParamAnnos.length);
                for (int i = 0; i < legacy.parameterAnnotations.length; i++) {
                    Assert.assertArrayEquals("parameterAnnotations[" + i + "]@" + method,
                            legacy.parameterAnnotations[i], producedParamAnnos[i]);
                }
            }
        }
    }

    @Test
    public void testMetadataMatchesLegacyForClassWithoutParametersOption() throws Exception {
        File outDir = compileFixture(false);
        Class<?> fixture = loadFixture(outDir, "parityfixture.NoParametersFixture");
        Method method = fixture.getDeclaredMethod("handle", String.class, int.class, String[].class);

        LegacyMeta legacy = parseLegacy(method);
        Object producedMeta = parseProduced(method);
        String[] producedNames = (String[]) field(producedMeta, "paramNames");
        Assert.assertArrayEquals(legacy.paramNames, producedNames);
        Assert.assertArrayEquals(legacy.methodAnnos, (Annotation[]) field(producedMeta, "methodAnnos"));
        Assert.assertArrayEquals(legacy.parameterAnnotations[0],
                ((Annotation[][]) field(producedMeta, "parameterAnnotations"))[0]);
        // 未传 -parameters 时新旧实现结果一致（通常为 null 或 arg0/arg1 一类的占位名）
        System.out.println("no -parameters 解析结果=" + Arrays.toString(producedNames));
    }

    @Test
    public void testMetadataMatchesLegacyForClassWithParametersOption() throws Exception {
        File outDir = compileFixture(true);
        Class<?> fixture = loadFixture(outDir, "parityfixture.NoParametersFixture");
        Method method = fixture.getDeclaredMethod("handle", String.class, int.class, String[].class);

        LegacyMeta legacy = parseLegacy(method);
        Object producedMeta = parseProduced(method);
        String[] producedNames = (String[]) field(producedMeta, "paramNames");
        Assert.assertArrayEquals(legacy.paramNames, producedNames);
        Assert.assertArrayEquals(new String[]{"first", "second", "third"}, producedNames);
    }

    @Test
    public void testInterceptDecisionMatchesLegacyAndCacheInvalidates() throws Exception {
        ApiInvokeMethodInterceptorManager manager = new ApiInvokeMethodInterceptorManager();
        EasyApiConfig config = new EasyApiConfig();
        config.setBasePackages("com.pddon.framework.easyapi.regression");
        ReflectionTestUtils.setField(manager, "easyApiConfig", config);

        try {
            assertInterceptMatchesLegacy(manager, config, new RestControllerFixture(), "handle");
            assertInterceptMatchesLegacy(manager, config, new RestControllerFixture(), "noArgs");
            assertInterceptMatchesLegacy(manager, config, new ResponseBodyFixture(), "handle");
            assertInterceptMatchesLegacy(manager, config, new PlainFixture(), "plain");
            assertInterceptMatchesLegacy(manager, config, new PlainFixture(), "annotated");

            // 根包配置变化后，缓存快照必须更新为新集合，且判断结果仍与旧公式一致
            config.setBasePackages("com.pddon.framework.easyapi.regression,com.pddon.framework.easyapi.other");
            assertInterceptMatchesLegacy(manager, config, new RestControllerFixture(), "handle");
            Assert.assertEquals(config.getAllBasePackages(), interceptCachePackages(RestControllerFixture.class));
        } finally {
            RequestContext.getContext().clear();
        }
    }

    private void assertInterceptMatchesLegacy(ApiInvokeMethodInterceptorManager manager, EasyApiConfig config,
                                              Object bean, String methodName) throws Exception {
        Method method = findMethod(bean.getClass(), methodName);
        HandlerMethod handler = new HandlerMethod(bean, method);
        RequestContext.getContext().setHandler(handler);
        boolean produced = invokeIsApiPackageScopePresent(manager);
        boolean legacy = legacyIsNeedIntercept(method.getDeclaringClass(),
                handler.getReturnType().getParameterType(), method, config.getAllBasePackages());
        Assert.assertEquals("intercept@" + bean.getClass().getSimpleName() + "#" + methodName, legacy, produced);
    }

    /**
     * HEAD 中 ClassOriginCheckUtil#isNeedIntercept 的逐行副本。
     */
    private static boolean legacyIsNeedIntercept(Class<?> targetClass, Class<?> returnValueClass,
                                                 Method method, java.util.Set<String> pkgs) {
        if (targetClass == null) {
            return false;
        }
        if (legacyIsBasePackagesChild(targetClass, pkgs)) {
            RestController restController = AnnotationUtils.findAnnotation(targetClass, RestController.class);
            if (restController != null) {
                return true;
            }
            ResponseBody responseBody = AnnotationUtils.findAnnotation(method, ResponseBody.class);
            if (responseBody == null) {
                responseBody = AnnotationUtils.findAnnotation(targetClass, ResponseBody.class);
            }
            if (responseBody != null) {
                return true;
            }
        }
        return false;
    }

    private static boolean legacyIsBasePackagesChild(Class<?> targetClass, java.util.Set<String> pkgs) {
        if (targetClass == null) {
            return false;
        }
        for (String pkg : pkgs) {
            if (targetClass.getName().startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    private static Method findMethod(Class<?> type, String methodName) {
        for (Method method : type.getDeclaredMethods()) {
            if (!method.isSynthetic() && method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new AssertionError("未找到方法:" + type.getName() + "#" + methodName);
    }

    private static boolean invokeIsApiPackageScopePresent(ApiInvokeMethodInterceptorManager manager) throws Exception {
        Method method = ApiInvokeMethodInterceptorManager.class.getDeclaredMethod("isApiPackageScopePresent");
        method.setAccessible(true);
        return (Boolean) method.invoke(manager);
    }

    @SuppressWarnings("unchecked")
    private static java.util.Set<String> interceptCachePackages(Class<?> type) throws Exception {
        ClassValue<?> classValue = (ClassValue<?>) ReflectionTestUtils.getField(
                ApiInvokeMethodInterceptorManager.class, "INTERCEPT_CACHE");
        Object cache = classValue.get(type);
        Field packages = cache.getClass().getDeclaredField("basePackages");
        packages.setAccessible(true);
        return (java.util.Set<String>) packages.get(cache);
    }

    private static Object parseProduced(Method method) throws Exception {
        Method getMethodMeta = ApiInvokeMethodInterceptorManager.class.getDeclaredMethod("getMethodMeta", Method.class);
        getMethodMeta.setAccessible(true);
        return getMethodMeta.invoke(null, method);
    }

    private static LegacyMeta parseLegacy(Method method) {
        return new LegacyMeta(LEGACY_DISCOVERER.getParameterNames(method),
                method.getAnnotations(), method.getParameterAnnotations());
    }

    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static ParameterNameDiscoverer createLegacyDiscoverer() {
        PrioritizedParameterNameDiscoverer discoverer = new PrioritizedParameterNameDiscoverer();
        discoverer.addDiscoverer(new StandardReflectionParameterNameDiscoverer());
        discoverer.addDiscoverer(new LocalVariableTableParameterNameDiscoverer());
        discoverer.addDiscoverer(new KotlinReflectionParameterNameDiscoverer());
        return discoverer;
    }

    private static File compileFixture(boolean parameters) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Assert.assertNotNull("当前运行环境没有 javac，需要 JDK", compiler);
        File workDir = Files.createTempDirectory("easyapi-parity").toFile();
        File sourceDir = new File(workDir, "src/parityfixture");
        File outDir = new File(workDir, "out");
        Assert.assertTrue(sourceDir.mkdirs());
        Assert.assertTrue(outDir.mkdirs());

        String source = "package parityfixture;\n"
                + "public class NoParametersFixture {\n"
                + "    @Deprecated\n"
                + "    public String handle(String first, int second, String[] third) {\n"
                + "        return first;\n"
                + "    }\n"
                + "}\n";
        File sourceFile = new File(sourceDir, "NoParametersFixture.java");
        Writer writer = new OutputStreamWriter(new FileOutputStream(sourceFile), Charset.forName("UTF-8"));
        try {
            writer.write(source);
        } finally {
            writer.close();
        }

        List<String> options = new ArrayList<>(Arrays.asList(
                "-d", outDir.getAbsolutePath(),
                "-classpath", System.getProperty("java.class.path"),
                "-g:none",
                "-proc:none"));
        if (parameters) {
            options.add("-parameters");
        }
        options.add(sourceFile.getAbsolutePath());
        Assert.assertEquals(0, compiler.run(null, null, null, options.toArray(new String[0])));
        return outDir;
    }

    private static Class<?> loadFixture(File outDir, String className) throws Exception {
        java.net.URLClassLoader loader = new java.net.URLClassLoader(
                new java.net.URL[]{outDir.toURI().toURL()}, ApiInvokeMethodInterceptorParityTest.class.getClassLoader());
        return loader.loadClass(className);
    }

    private static final class LegacyMeta {
        final String[] paramNames;
        final Annotation[] methodAnnos;
        final Annotation[][] parameterAnnotations;

        LegacyMeta(String[] paramNames, Annotation[] methodAnnos, Annotation[][] parameterAnnotations) {
            this.paramNames = paramNames;
            this.methodAnnos = methodAnnos;
            this.parameterAnnotations = parameterAnnotations;
        }
    }

    @RestController
    public static class RestControllerFixture {
        public String handle(@RequestParam("id") String id, @IgnoreSign String token, PlainParam param) {
            return id + token + param;
        }

        public void noArgs() {
        }
    }

    public static class ResponseBodyFixture {
        @ResponseBody
        public String handle(String first, int second) {
            return first + second;
        }
    }

    public static class PlainFixture {
        public String plain(String first) {
            return first;
        }

        @Deprecated
        public String annotated(String first, int second) {
            return first + second;
        }
    }

    public static class PlainParam {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}

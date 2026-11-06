package com.pddon.framework.easyapi.filter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.StreamUtils;

import com.pddon.framework.easyapi.utils.request.BodyReaderHttpServletRequestWrapper;

/**
 * 优化点2的验证：
 * 1）无请求体（contentLength==0）时不再创建包装对象；
 * 2）有请求体时getInputStream/getReader仍可重复读取，字节内容与原始请求一致；
 * 3）multipart请求保持原有"不包装"的行为。
 */
public class RepeatedlyReadFilterTest {

    private final RepeatedlyReadFilter filter = new RepeatedlyReadFilter();

    @Test
    public void testEmptyBodyRequestIsNotWrapped() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/test");
        request.setContent(new byte[0]);
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        Assert.assertSame(request, chain.getRequest());
    }

    @Test
    public void testBodyRequestIsWrappedAndCanBeReadRepeatedly() throws Exception {
        byte[] content = "name=中文&value=1".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/test");
        request.setContent(content);
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        Assert.assertTrue(chain.getRequest() instanceof BodyReaderHttpServletRequestWrapper);
        HttpServletRequest wrapped = (HttpServletRequest) chain.getRequest();

        byte[] first = readAll(wrapped.getInputStream());
        byte[] second = readAll(wrapped.getInputStream());
        Assert.assertArrayEquals("重复读取的字节应一致", first, second);
        Assert.assertArrayEquals("包装后字节应与原始请求体一致", content, first);

        String firstReader = readAll(wrapped.getReader());
        String secondReader = readAll(wrapped.getReader());
        Assert.assertEquals("重复getReader内容应一致", firstReader, secondReader);
        //getReader沿用平台默认字符集解码，与旧实现一致
        Assert.assertEquals(new String(first, Charset.defaultCharset()), firstReader);
    }

    @Test
    public void testMultipartRequestKeepsOriginalBehavior() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/test");
        request.setContentType("multipart/form-data; boundary=----x");
        request.setContent("----x".getBytes(StandardCharsets.UTF_8));
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        Assert.assertSame(request, chain.getRequest());
    }

    private static byte[] readAll(InputStream inputStream) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtils.copy(inputStream, out);
        return out.toByteArray();
    }

    private static String readAll(BufferedReader reader) throws Exception {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}

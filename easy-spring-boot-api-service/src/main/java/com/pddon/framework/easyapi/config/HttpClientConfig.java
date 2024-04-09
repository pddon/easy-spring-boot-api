package com.pddon.framework.easyapi.config;


import com.pddon.framework.easyapi.utils.IdleConnectionEvictor;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
public class HttpClientConfig {

    @Value("${http_max_total:800}")
    private int maxTotal = 800;

    @Value("${http_default_max_perRoute:80}")
    private int defaultMaxPerRoute = 80;

    @Value("${http_validate_after_inactivity:1000}")
    private int validateAfterInactivity = 1000;

    @Value("${http_connection_request_timeout:5000}")
    private int connectionRequestTimeout = 5000;

    @Value("${http_connection_timeout:60000}")
    private int connectTimeout = 10000;

    @Value("${http_socket_timeout:60000}")
    private int socketTimeout = 20000;

    @Value("${waitTime:30000}")
    private int waitTime = 30000;

    @Value("${idleConTime:3}")
    private int idleConTime = 3;

    @Value("${retryCount:3}")
    private int retryCount = 3;

    static class CustomTrustManager implements X509TrustManager, TrustManager{
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }
    }
    /**
     * 绕过验证
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSL");

        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new CustomTrustManager();

        sc.init(null, new TrustManager[] { trustManager }, null);
        return sc;
    }

    @Bean
    public PoolingHttpClientConnectionManager createPoolingHttpClientConnectionManager() throws NoSuchAlgorithmException, KeyManagementException {
        //采用绕过验证的方式处理https请求
        SSLContext sslcontext = HttpClientConfig.createIgnoreVerifySSL();

        // 设置协议http和https对应的处理socket链接工厂的对象
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext))
                .build();
        PoolingHttpClientConnectionManager poolmanager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolmanager.setMaxTotal(maxTotal);
        poolmanager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        poolmanager.setValidateAfterInactivity(validateAfterInactivity);
        return poolmanager;
    }

    @Bean
    public CloseableHttpClient createHttpClient(PoolingHttpClientConnectionManager poolManager) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().setConnectionManager(poolManager);
        httpClientBuilder.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {

            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator iterator = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (iterator.hasNext()) {
                    HeaderElement headerElement = iterator.nextElement();
                    String param = headerElement.getName();
                    String value = headerElement.getValue();
                    if (null != value && param.equalsIgnoreCase("timeout")) {
                        return Long.parseLong(value) * 1000;
                    }
                }
                return 30 * 1000;
            }
        });
        httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(retryCount, false));
        return httpClientBuilder.build();
    }
    @Bean
    public RequestConfig createRequestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(connectionRequestTimeout)     // 从连接池中取连接的超时时间
                .setConnectTimeout(connectTimeout)                        // 连接超时时间
                .setSocketTimeout(socketTimeout)                        // 请求超时时间
                .build();
    }

    @Bean
    public IdleConnectionEvictor createIdleConnectionEvictor(PoolingHttpClientConnectionManager poolManager) {
        IdleConnectionEvictor idleConnectionEvictor = new IdleConnectionEvictor(poolManager, waitTime, idleConTime);
        return idleConnectionEvictor;
    }

}

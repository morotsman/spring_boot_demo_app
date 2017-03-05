package com.morotsman.spring_boot_demo_app;


import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpConfig {

    
    @Value("${httpConfig.getConnectionFromPoolTimeoutInMillis}") 
    private int connectionTimeoutInMillis;
    
    @Value("${httpConfig.establishConnectionToServerTimeoutInMillis}") 
    private int establishConnectionToServerTimeoutInMillis;    
    
    @Value("${httpConfig.requestToServerTimeoutInMillis}") 
    private int requestToServerTimeoutInMillis;      
    
    @Value("${httpConfig.maxTotalConnections}") 
    private int maxTotalConnections;
    
    @Value("${httpConfig.maxTotalConnectionsPerRoute}") 
    private int maxTotalConnectionsPerRoute;
    

    @Bean
    public ClientHttpRequestFactory httpRequestFactory() {
        HttpComponentsClientHttpRequestFactory result =  new HttpComponentsClientHttpRequestFactory(httpClient());
        result.setConnectTimeout(connectionTimeoutInMillis);
        result.setConnectionRequestTimeout(establishConnectionToServerTimeoutInMillis);
        result.setReadTimeout(requestToServerTimeoutInMillis);
        return result;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory());
        return restTemplate;
    }

    @Bean
    public HttpClient httpClient() {
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
        HttpClient defaultHttpClient = new DefaultHttpClient(connectionManager); 
        connectionManager.setMaxTotal(maxTotalConnections);
        connectionManager.setDefaultMaxPerRoute(maxTotalConnectionsPerRoute);
        return defaultHttpClient;
    }
}

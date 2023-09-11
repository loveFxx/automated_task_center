package com.sailvan.dispatchcenter.es.config;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * es连接
 * @date 2022-03
 * @author menghui
 */
@Configuration
@ConditionalOnBean(EsMarkerConfiguration.EsMarker.class)
public class EsConfig {

    @Value("${spring.data.elasticsearch.cluster-hosts}")
    String hosts;

    @Value("${spring.data.elasticsearch.cluster-security}")
    String security;

    @Bean
    public RestHighLevelClient getRestHighLevelClient(){
        String[] hostArray = hosts.split(",");
        HttpHost[] httpHosts = new HttpHost[hostArray.length];
        for (int i = 0; i < hostArray.length; i++) {
            String host = hostArray[i];
            int j = host.indexOf(":");
            HttpHost httpHost = null;
            try {
                httpHost = new HttpHost(InetAddress.getByName(host.substring(0, j)),
                        Integer.parseInt(host.substring(j + 1)));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            httpHosts[i] = httpHost;
        }
        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);
        String nameAndPwd = security;
        if (StringUtils.isNotEmpty(nameAndPwd) && nameAndPwd.contains(":")) {
            String[] nameAndPwdArr = nameAndPwd.split(":");
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(nameAndPwdArr[0], nameAndPwdArr[1]));
            restClientBuilder.setHttpClientConfigCallback(
                    httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                    .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                            .setConnectTimeout(1 * 1000)
                            .setConnectionRequestTimeout(5*60 * 1000)
                            .setSocketTimeout(5*60 * 1000));
            restClientBuilder.setMaxRetryTimeoutMillis(150000);
        }
        return new RestHighLevelClient(restClientBuilder);
    }
}

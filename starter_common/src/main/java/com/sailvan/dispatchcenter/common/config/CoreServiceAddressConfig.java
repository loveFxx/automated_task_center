package com.sailvan.dispatchcenter.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *  远程调用core 地址配置
 *   @author menghui
 *   @date 2021-10
 */
@Component
@ConfigurationProperties(prefix = "core")
public class CoreServiceAddressConfig {

    private String ip;
    private String port;
    private String path;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

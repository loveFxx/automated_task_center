package com.sailvan.dispatchcenter.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class BusSystemConfig {

    @Value("${busSystem.idc.appKey}")
    private String idcAppkey;

    @Value("${busSystem.idc.appSecret}")
    private String idcAppSecret;

    @Value("${busSystem.idc.router}")
    private String idcRouter;

    @Value("${busSystem.hk.appKey}")
    private String hkAppkey;

    @Value("${busSystem.hk.appSecret}")
    private String hkAppSecret;

    @Value("${busSystem.hk.router}")
    private String hkRouter;

    @Value("${busSystem.sz.appKey}")
    private String szAppkey;

    @Value("${busSystem.sz.appSecret}")
    private String szAppSecret;

    @Value("${busSystem.sz.router}")
    private String szRouter;

    public String getIdcAppkey() {
        return idcAppkey;
    }

    public void setIdcAppkey(String idcAppkey) {
        this.idcAppkey = idcAppkey;
    }

    public String getIdcAppSecret() {
        return idcAppSecret;
    }

    public void setIdcAppSecret(String idcAppSecret) {
        this.idcAppSecret = idcAppSecret;
    }

    public String getIdcRouter() {
        return idcRouter;
    }

    public void setIdcRouter(String idcRouter) {
        this.idcRouter = idcRouter;
    }

    public String getHkAppkey() {
        return hkAppkey;
    }

    public void setHkAppkey(String hkAppkey) {
        this.hkAppkey = hkAppkey;
    }

    public String getHkAppSecret() {
        return hkAppSecret;
    }

    public void setHkAppSecret(String hkAppSecret) {
        this.hkAppSecret = hkAppSecret;
    }

    public String getHkRouter() {
        return hkRouter;
    }

    public void setHkRouter(String hkRouter) {
        this.hkRouter = hkRouter;
    }

    public String getSzAppkey() {
        return szAppkey;
    }

    public void setSzAppkey(String szAppkey) {
        this.szAppkey = szAppkey;
    }

    public String getSzAppSecret() {
        return szAppSecret;
    }

    public void setSzAppSecret(String szAppSecret) {
        this.szAppSecret = szAppSecret;
    }

    public String getSzRouter() {
        return szRouter;
    }

    public void setSzRouter(String szRouter) {
        this.szRouter = szRouter;
    }
}

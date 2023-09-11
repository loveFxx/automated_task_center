package com.sailvan.dispatchcenter.es.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 常量类
 * @date 2022-03
 * @author menghui
 */
public class Constant {

    public static final String CONF_DIR = "conf";

    public final static String SRCDATASOURCESES = "srcDataSourcesES";
    public final static Map<String,String> srcDataSourcesESJsonMap = new ConcurrentHashMap<>();
    public static int commitMaxBatchSize = 0;
    public static int nthread = 8;
    public static int perThreadSize = 2000;
}

package com.sailvan.dispatchcenter.data.plugs;

import com.sailvan.dispatchcenter.data.init.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class InitCacheMarkerConfiguration {
    @Bean("initCacheMarker")
    public InitCacheMarker initCacheMarkerBean() {
        return new InitCacheMarker();
    }
    public class InitCacheMarker {
    }

    @Bean
    @Primary
    public InitDataAccountRedisCache initDataAccountRedisCache(){
        return new InitDataAccountRedisCache();
    }

    @Bean
    @Primary
    InitDataMachineRedisCache initDataMachineRedisCache(){
        return new InitDataMachineRedisCache();
    }

    @Bean
    @Primary
    InitDataPlatformRedisCache initDataPlatformRedisCache(){
        return new InitDataPlatformRedisCache();
    }


    @Bean
    @Primary
    InitDataSystemRedisCache initDataSystemRedisCache(){
        return new InitDataSystemRedisCache();
    }


    @Bean
    @Primary
    InitDataTaskRedisCache initDataTaskRedisCache(){
        return new InitDataTaskRedisCache();
    }


    @Bean
    @Primary
    InitDataValidVersionRedisCache initDataValidVersionRedisCache(){
        return new InitDataValidVersionRedisCache();
    }
}

package com.forezp.servicegateway.config;

import com.forezp.servicegateway.filter.MyGlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterFactory {

    /**
     * 全局filter
     * @return
     */
    @Bean
    public MyGlobalFilter myGlobalFilter() {
        return new MyGlobalFilter();
    }
}
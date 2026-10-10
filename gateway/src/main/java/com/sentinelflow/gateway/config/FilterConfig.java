package com.sentinelflow.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelflow.gateway.filter.RateLimiterFilter;
import com.sentinelflow.gateway.filter.RequestIdFilter;
import com.sentinelflow.gateway.service.DistributedRateLimiterService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RequestIdFilter> requestIdFilterRegistration(
            GatewayProperties gatewayProperties) {
        FilterRegistrationBean<RequestIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestIdFilter(gatewayProperties));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<RateLimiterFilter> rateLimiterFilterRegistration(
            DistributedRateLimiterService rateLimiterService,
            ObjectMapper objectMapper) {
        FilterRegistrationBean<RateLimiterFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimiterFilter(rateLimiterService, objectMapper));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}

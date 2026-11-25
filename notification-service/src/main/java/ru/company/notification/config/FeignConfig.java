package ru.company.notification.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class FeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Decoder feignDecoder() {
        return new SpringDecoder(() -> new HttpMessageConverters(new MappingJackson2HttpMessageConverter()));
    }

    @Bean
    public Encoder feignEncoder() {
        return new SpringEncoder(() -> new HttpMessageConverters(new MappingJackson2HttpMessageConverter()));
    }

    @Bean
    public RequestInterceptor internalRequestInterceptor() {
        return template -> {
            template.header("X-Internal-Request", "true");
            System.out.println("🔵 [Feign] Adding X-Internal-Request header to request: " + template.url());
        };
    }
}
package com.bsuir.spolksfive.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableConfigurationProperties
@EnableAsync
public class Config {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

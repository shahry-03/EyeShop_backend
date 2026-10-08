package com.eyeshop.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@org.springframework.boot.context.properties.EnableConfigurationProperties({UniversalAuthProperties.class, AccountLockoutProperties.class})
public class ProjectConfig {
    
}

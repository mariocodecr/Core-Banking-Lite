package com.corebanking.config;

import org.springframework.context.annotation.Configuration;

/**
 * MVC configuration.
 * CORS is handled by Spring Security's CorsConfigurationSource in SecurityConfig.
 * Do not re-add addCorsMappings here — it would conflict with the security CORS filter.
 */
@Configuration
public class WebConfig {
}

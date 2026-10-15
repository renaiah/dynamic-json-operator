package com.json.dynamic_json_operator.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Dynamic JSON Operator API")
                .version("1.0")
                .description("Insert dataset records and query them with dynamic group-by and sort-by"));
    }
}
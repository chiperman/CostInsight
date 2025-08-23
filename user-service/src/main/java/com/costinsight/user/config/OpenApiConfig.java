package com.costinsight.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI costInsightUserAPI() {
        return new OpenAPI()
                .info(new Info().title("CostInsight User API")
                        .description("投资记录管理系统的用户服务 API")
                        .version("v0.0.1"));
    }
}
package com.costinsight.user.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI costInsightUserAPI() {
        return new OpenAPI()
                .info(new Info().title("CostInsight User API")
                        .description("投资记录管理系统的用户服务 API")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("CostInsight Wiki Documentation")
                        .url("https://github.com/your-repo/costinsight/wiki"));
    }
}
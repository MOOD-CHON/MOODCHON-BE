package com.example.moodchon.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Collections;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    // 인증 없이 호출 가능한 공개 API 경로
    private static final Set<String> PUBLIC_PATHS = Set.of();

    @Bean
    public OpenAPI OpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new OpenAPI()
                .info(new Info()
                        .title("Moodchon API")
                        .description("무드촌 서버 API 문서")
                        .version("v1.0"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, securityScheme));
    }

    @Bean
    public OpenApiCustomizer publicPathSecurityCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> {
            if (PUBLIC_PATHS.contains(path)) {
                pathItem.readOperations()
                        .forEach(op -> op.setSecurity(Collections.emptyList()));
            }
        });
    }
}
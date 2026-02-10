package com.transaction.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI configuration
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Transaction Rule Engine API")
                        .version("1.0.0")
                        .description("API for transaction data tagging based on rules using Drools and Flink")
                        .contact(new Contact()
                                .name("Transaction Rule Engine Team")
                                .email("support@transaction-rule-engine.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local Server"),
                        new Server().url("https://api.transaction-rule-engine.com").description("Production Server")
                ));
    }
}

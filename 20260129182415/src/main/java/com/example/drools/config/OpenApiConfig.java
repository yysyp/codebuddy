package com.example.drools.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI droolsOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8080/api");
        server.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setEmail("support@example.com");
        contact.setName("Support Team");

        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");

        Info info = new Info()
                .title("Drools Rule Engine API")
                .version("1.0.0")
                .contact(contact)
                .description("Dynamic Rule Engine API using Drools and Spring Boot 3")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}

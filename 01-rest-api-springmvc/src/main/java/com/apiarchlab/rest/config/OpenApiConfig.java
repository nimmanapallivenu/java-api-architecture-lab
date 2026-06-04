package com.apiarchlab.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order REST API")
                        .version("1.0.0")
                        .description("REST API for Order Management System using Spring MVC\n\n" +
                                "This API demonstrates:\n" +
                                "- REST conventions\n" +
                                "- Request/Response handling\n" +
                                "- Data validation\n" +
                                "- Exception handling\n" +
                                "- Pagination and sorting\n" +
                                "- OpenAPI/Swagger documentation")
                        .contact(new Contact()
                                .name("API Architect Lab")
                                .url("https://github.com/your-org/java-api-architecture-lab"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Local development environment"),
                        new Server()
                                .url("https://api.example.com")
                                .description("Production environment")
                ));
    }
}


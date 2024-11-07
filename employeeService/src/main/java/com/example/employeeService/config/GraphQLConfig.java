package com.example.employeeService.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQLConfig {
    private final UploadScalar uploadScalar;

    public GraphQLConfig(UploadScalar uploadScalar) {
        this.uploadScalar = uploadScalar;
    }

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder.scalar(uploadScalar.buildUploadScalar());
    }
}

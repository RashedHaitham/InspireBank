package com.example.accountService.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ValidationException;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CustomGraphQLExceptionResolver implements DataFetcherExceptionResolver {

    @Override
    public Mono<List<GraphQLError>> resolveException(Throwable exception, DataFetchingEnvironment environment) {
        if (exception instanceof ResourceNotFoundException) {
            return Mono.just(Collections.singletonList(
                    GraphqlErrorBuilder.newError(environment)
                            .message("Resource not found: " + exception.getMessage())
                            .errorType(ErrorType.NOT_FOUND)
                            .build()
            ));
        }

        if (exception instanceof IllegalArgumentException){
            return Mono.just(Collections.singletonList(
                    GraphqlErrorBuilder.newError(environment)
                            .message(exception.getMessage())
                            .errorType(ErrorType.NOT_FOUND)
                            .build()
            ));
        }

        if (exception instanceof DataIntegrityViolationException) {
            return Mono.just(Collections.singletonList(
                    GraphqlErrorBuilder.newError(environment)
                            .message("Duplicate email found. Email must be unique.")
                            .errorType(ErrorType.BAD_REQUEST)
                            .build()
            ));
        }

        if (exception instanceof Exception) {
            return Mono.just(Collections.singletonList(
                    GraphqlErrorBuilder.newError(environment)
                            .message("An unexpected error occurred: " + exception.getMessage())
                            .errorType(ErrorType.INTERNAL_ERROR)
                            .build()
            ));
        }

        if (exception instanceof ValidationException) {
            return Mono.just(Collections.singletonList(
                    GraphqlErrorBuilder.newError(environment)
                            .message("Validation failed: " + exception.getMessage())
                            .errorType(ErrorType.BAD_REQUEST)
                            .build()
            ));
        }

        if (exception instanceof IOException) {
            return Mono.just(Collections.singletonList(
                    GraphqlErrorBuilder.newError(environment)
                            .message("Failed to read and process the file: " + exception.getMessage())
                            .errorType(ErrorType.BAD_REQUEST)
                            .build()
            ));
        }

        if (exception instanceof MethodArgumentNotValidException) {
            Map<String, String> validationErrors = new HashMap<>();
            ((MethodArgumentNotValidException) exception).getBindingResult().getFieldErrors().forEach(error ->
                    validationErrors.put(error.getField(), error.getDefaultMessage()));

            return Mono.just(Collections.singletonList(
                    GraphqlErrorBuilder.newError(environment)
                            .message("Validation failed")
                            .errorType(ErrorType.BAD_REQUEST)
                            .extensions(Collections.singletonMap("validationErrors", validationErrors))
                            .build()
            ));
        }

        return Mono.empty();
    }
}

package com.example.employeeService.config;

import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Component;

@Component
public class UploadScalar {

    public GraphQLScalarType buildUploadScalar() {
        return GraphQLScalarType.newScalar()
                .name("Upload")
                .description("A file upload scalar")
                .coercing(new Coercing<MultipartFile, MultipartFile>() {
                    @Override
                    public MultipartFile serialize(Object dataFetcherResult) {
                        return (MultipartFile) dataFetcherResult;
                    }

                    @Override
                    public MultipartFile parseValue(Object input) {
                        return (MultipartFile) input;
                    }

                    @Override
                    public MultipartFile parseLiteral(Object input) {
                        return null; // Parsing literals is not supported for uploads
                    }
                })
                .build();
    }
}

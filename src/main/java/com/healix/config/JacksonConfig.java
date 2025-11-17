package com.healix.config;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.postConfigurer((ObjectMapper mapper) -> {
            // Be lenient: accept empty strings as null for POJOs if desired
            mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

            // Add a problem handler to coerce empty string to empty collection/array when target expects a collection
            mapper.addHandler(new DeserializationProblemHandler() {
                @Override
                public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType, String valueToConvert, String failureMsg) {
                    if (valueToConvert == null || !valueToConvert.isEmpty()) {
                        return NOT_HANDLED;
                    }

                    // If the target is an array type, return an empty array
                    if (targetType.isArray()) {
                        Class<?> componentType = targetType.getComponentType();
                        return Array.newInstance(componentType, 0);
                    }

                    // If the target is a Collection (List/Set), return an empty mutable list
                    if (List.class.isAssignableFrom(targetType)) {
                        return new ArrayList<>();
                    }

                    // For other collection types, try empty list as fallback
                    if (Iterable.class.isAssignableFrom(targetType)) {
                        return Collections.emptyList();
                    }

                    return NOT_HANDLED;
                }
            });
        });
    }
}

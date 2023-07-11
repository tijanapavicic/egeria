package org.odpi.openmetadata.server.config;

/**
 * Created by YM21WO on egeria
 * on 10/07/2023 at 22:31
 */

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by tijana.pavicic on egeria
 * on 06/07/2023 at 10:03
 */
@ContextConfiguration(classes = {
        Jackson2ObjectMapperBuilder.class,
        ObjectMapperConfiguration.class})
@ExtendWith(SpringExtension.class)
class ObjectMapperConfigurationTest {


    @Autowired
    ObjectMapper objectMapper;

    @Test
    void autowiredObjectMapperIsNotNull() {
        assertNotNull(objectMapper);
    }

    @Test
    void autowiredObjectMapperSerializationProperties() {

        assertFalse(
                objectMapper.getSerializationConfig().isEnabled(
                        SerializationFeature.FAIL_ON_EMPTY_BEANS
                ));
        assertFalse(
                objectMapper.getSerializationConfig().isEnabled(
                        SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED
                ));

        assertTrue(
                objectMapper.getSerializationConfig().isEnabled(
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                ));
        assertTrue(
                objectMapper.getSerializationConfig().isEnabled(
                        SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED
                ));


    }

    @Test
    void autowiredObjectMapperDeserializationProperties() {
        assertFalse(
                objectMapper.getDeserializationConfig().isEnabled(
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
                ));

        assertTrue(
                objectMapper.getDeserializationConfig().isEnabled(
                        MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES
                ));
        assertTrue(
                objectMapper.getDeserializationConfig().isEnabled(
                        DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY
                ));

    }
}
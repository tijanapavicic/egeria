package org.odpi.openmetadata.server.actuator;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.odpi.openmetadata.server.OMAGServer;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by tijana.pavicic on egeria
 * on 10/07/2023 at 16:12
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        classes = {
                OMAGServer.class
        })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.properties")
public class ActuatorEndpointsTest {
    @Autowired
    RestTemplateBuilder restTemplateBuilder;
    @LocalManagementPort
    int managementPort;
    private MockMvc mockMvc;

    @Test
    public void testMetricsActuator() throws JSONException {
        ResponseEntity<String> response;
        response = restTemplateBuilder
                .rootUri("http://localhost:" + managementPort + "/actuator/actuator")
                .build().exchange("/metrics", HttpMethod.GET, new HttpEntity<>(null), String.class);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());

        JSONAssert.assertEquals(response.getBody(), response.getBody().toString(), true);

        // todo add asertions
    }

}

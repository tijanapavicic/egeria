package org.odpi.openmetadata.server.actuator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.odpi.openmetadata.server.OMAGServer;
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

import static org.junit.jupiter.api.Assertions.*;

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
    public static final String URL = "http://localhost:";
    public static final String MANAGMENT_CONTEXT_PATH = "/actuator/actuator";

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    @LocalManagementPort
    int managementPort;
    private MockMvc mockMvc;

    @Test
    public void testMetricsActuator() throws JSONException {
        ResponseEntity<String> response;
        response = restTemplateBuilder
                .rootUri(URL + managementPort + MANAGMENT_CONTEXT_PATH)
                .build().exchange("/metrics", HttpMethod.GET, new HttpEntity<>(null), String.class);

        assertInstanceOf(ResponseEntity.class, response);
        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());

        JSONObject body = new JSONObject(response.getBody());
        assertNotNull(body);

        JSONArray metricsNamesArray = (JSONArray) body.get("names");
        assertEquals(50, metricsNamesArray.length());

    }

    @Test
    public void testMetricsActuatorApplicationStarted() throws JSONException {
        ResponseEntity<String> response;
        response = restTemplateBuilder
                .rootUri(URL + managementPort + MANAGMENT_CONTEXT_PATH)
                .build().exchange("/metrics/application.started.time", HttpMethod.GET, new HttpEntity<>(null), String.class);

        assertInstanceOf(ResponseEntity.class, response);
        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());

        JSONObject body = new JSONObject(response.getBody());
        assertNotNull(body);

    }

    @Test
    public void testMetricsActuatorApplicationReady() throws JSONException {
        ResponseEntity<String> response;
        response = restTemplateBuilder
                .rootUri(URL + managementPort + MANAGMENT_CONTEXT_PATH)
                .build().exchange("/metrics/application.ready.time", HttpMethod.GET, new HttpEntity<>(null), String.class);

        assertInstanceOf(ResponseEntity.class, response);
        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());

        JSONObject body = new JSONObject(response.getBody());
        assertNotNull(body);

    }

}

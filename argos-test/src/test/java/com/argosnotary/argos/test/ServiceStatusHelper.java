/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2021 Gerard Borst <gerard.borst@argosnotary.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.argosnotary.argos.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.argosnotary.argos.argos4j.rest.api.ApiClient;
import com.argosnotary.argos.argos4j.rest.api.client.HierarchyApi;
import com.argosnotary.argos.argos4j.rest.api.client.LayoutApi;
import com.argosnotary.argos.argos4j.rest.api.client.ServiceAccountApi;
import com.argosnotary.argos.argos4j.rest.api.client.PersonalAccountApi;
import com.argosnotary.argos.argos4j.rest.api.client.SupplychainApi;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class ServiceStatusHelper {

    private static Properties properties = Properties.getInstance();
    public static void waitForArgosServiceToStart() {
        log.info("Waiting for argos service start");
        HttpClient client = HttpClient.newHttpClient();
        await().atMost(1, MINUTES).until(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(properties.getApiBaseUrl() + "/actuator/health"))
                        .build();
                HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
                return 200 == send.statusCode();
            } catch (IOException e) {
                //ignore
                return false;
            }
        });

        log.info("argos service started");
    }


    public static void waitForArgosIntegrationTestServiceToStart() {
        log.info("Waiting for argos integration test service start");
        HttpClient client = HttpClient.newHttpClient();
        await().atMost(30, SECONDS).until(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(properties.getIntegrationTestServiceBaseUrl() + "/actuator/health"))
                        .build();
                HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
                return 200 == send.statusCode();
            } catch (IOException e) {
                //ignore
                return false;
            }
        });

        log.info("argos integration test service started");
    }

    public static SupplychainApi getSupplychainApi(String bearerToken) {
        return getApiClient(bearerToken).buildClient(SupplychainApi.class);
    }

    public static String getToken(String name, String lastName, String email) {
        try {
            configureFor(properties.getOauthStubUrl(), Integer.valueOf(properties.getOauthStubPort()));
            String bodyResponse = createBodyResponse(name, lastName, email);
            stubFor(get(urlEqualTo("/v1.0/me"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json; charset=utf-8")
                            .withBody(bodyResponse)
                    ));
            String token = getToken();
            WireMock.resetToDefault();
            return token;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String createBodyResponse(String name, String lastName, String email) throws IOException {
        String bodyTemplate = IOUtils.toString(ServiceStatusHelper.class
                .getResourceAsStream("/testmessages/authentication/response.json"), UTF_8);

        Map<String, String> values = new HashMap<>();
        values.put("name", name);
        values.put("lastName", lastName);
        values.put("email", email);
        values.put("id", UUID.randomUUID().toString());
        return StringSubstitutor.replace(bodyTemplate, values, "${", "}");
    }

    private static String getToken() {
        HttpGet request = new HttpGet(properties.getApiBaseUrl() + "/api/oauth2/authorize/azure?redirect_uri=/authenticated");
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
            String token = new ObjectMapper().readValue(response.getEntity().getContent(), JsonNode.class).get("token").asText();

            return token;

        } catch (IOException e) {
            log.error("exception", e);
            fail(e.getMessage());
            return null;
        }
    }

    public static HierarchyApi getHierarchyApi(String bearerToken) {
        return getApiClient(bearerToken).buildClient(HierarchyApi.class);
    }

    public static PersonalAccountApi getPersonalAccountApi(String bearerToken) {
        return getApiClient(bearerToken).buildClient(PersonalAccountApi.class);
    }

    public static ServiceAccountApi getServiceAccountApi(String bearerToken) {
        return getApiClient(bearerToken).buildClient(ServiceAccountApi.class);
    }

    public static LayoutApi getLayoutApi(String bearerToken) {
        return getApiClient(bearerToken).buildClient(LayoutApi.class);
    }

    private static ApiClient getApiClient(String bearerToken) {
        ApiClient apiClient = new ApiClient("bearerAuth").setBasePath(properties.getApiBaseUrl() + "/api");
        apiClient.setBearerToken(bearerToken);
        return apiClient;
    }

}

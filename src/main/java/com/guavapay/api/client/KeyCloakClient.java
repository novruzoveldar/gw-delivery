package com.guavapay.api.client;

import com.guavapay.api.config.KeyCloakFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@FeignClient(name = "KeycloakClient",
        url = "${keycloak.auth-server-url}/auth/realms/${keycloak.realm}",
        configuration = KeyCloakFeignConfiguration.class)
public interface KeyCloakClient {

    @PostMapping(value = "/protocol/openid-connect/token", consumes = APPLICATION_FORM_URLENCODED_VALUE)
    Map<String, ?> getToken(@RequestBody Map<String, ?> form);
}

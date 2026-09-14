package com.govportal.service;

import com.govportal.dto.LoginRequestDto;
import com.govportal.dto.LoginResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * The Vue SPA never talks to Keycloak directly. Instead it posts credentials to
 * {@code POST /api/auth/login} on this backend, which performs the standard OAuth2
 * "password" grant against Keycloak's token endpoint and relays the resulting
 * access/refresh tokens back to the client. This keeps the Keycloak client secret
 * (if any) off the frontend and matches the README's documented login endpoint.
 *
 * Note: the Resource Owner Password Credentials grant is deprecated by OAuth 2.1 and
 * is used here only to keep the demo self-contained. In production, prefer the
 * Authorization Code + PKCE flow, redirecting the browser to Keycloak directly.
 */
@Slf4j
@Service
public class KeycloakAuthService {

    private final WebClient webClient;
    private final String tokenUri;
    private final String clientId;
    private final String clientSecret;

    public KeycloakAuthService(WebClient.Builder webClientBuilder,
                                @Value("${keycloak.token-uri}") String tokenUri,
                                @Value("${keycloak.client-id}") String clientId,
                                @Value("${keycloak.client-secret:}") String clientSecret) {
        this.webClient = webClientBuilder.build();
        this.tokenUri = tokenUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.isBlank()) {
            form.add("client_secret", clientSecret);
        }
        form.add("username", request.username());
        form.add("password", request.password());
        form.add("scope", "openid");

        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(LoginResponseDto.class)
                .block();
    }
}

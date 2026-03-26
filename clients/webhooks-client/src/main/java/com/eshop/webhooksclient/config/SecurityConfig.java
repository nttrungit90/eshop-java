package com.eshop.webhooksclient.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.client.provider.keycloak.authorization-uri:http://localhost:8180/realms/eshop/protocol/openid-connect/auth}")
    private String authorizationUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Derive end_session_endpoint from authorization-uri (same base path)
        String endSessionEndpoint = authorizationUri.replace("/auth", "/logout");

        LogoutSuccessHandler keycloakLogoutHandler = (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            String idToken = null;
            if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
                idToken = oidcUser.getIdToken().getTokenValue();
            }

            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String redirectUri = URLEncoder.encode(baseUrl + "/", StandardCharsets.UTF_8);

            String logoutUrl = endSessionEndpoint + "?post_logout_redirect_uri=" + redirectUri;
            if (idToken != null) {
                logoutUrl += "&id_token_hint=" + idToken;
            }

            response.sendRedirect(logoutUrl);
        };

        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/webhook-received", "/check")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/check", "/webhook-received", "/api/messages").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/", true)
            )
            .logout(logout -> logout
                .logoutSuccessHandler(keycloakLogoutHandler)
            );

        return http.build();
    }
}

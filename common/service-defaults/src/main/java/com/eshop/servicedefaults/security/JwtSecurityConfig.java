/**
 * Converted from: src/eShop.ServiceDefaults/AuthenticationExtensions.cs
 * .NET Class: eShop.ServiceDefaults.AuthenticationExtensions
 *
 * JWT security configuration for resource servers.
 *
 * All Java services validate JWTs issued by Keycloak.
 * The identity.url must point to the Keycloak realm issuer URI
 * (e.g., http://localhost:8180/realms/eshop) so that the OIDC discovery
 * endpoint is reachable for automatic JWKS URI resolution.
 *
 * Note: Spring Security's BearerTokenAuthenticationFilter runs before authorization checks,
 * so if a Bearer token is present and validation fails, it returns 401 even on permitAll endpoints.
 * To avoid this, we use a custom BearerTokenResolver that only extracts tokens for protected paths.
 */
package com.eshop.servicedefaults.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.AntPathMatcher;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "identity.url")
public class JwtSecurityConfig {

    @Value("${identity.url}")
    private String identityUrl;

    private static final String[] PUBLIC_PATHS = {
        "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**"
    };

    private static final String[] PUBLIC_GET_PATHS = {
        "/api/catalog/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/catalog/**", HttpMethod.GET.name())).permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .bearerTokenResolver(selectiveBearerTokenResolver())
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    /**
     * A BearerTokenResolver that skips token extraction for public endpoints.
     * This prevents Spring Security from failing with 401 on permitAll endpoints
     * when an invalid or unverifiable Bearer token is present in the request.
     */
    @Bean
    public BearerTokenResolver selectiveBearerTokenResolver() {
        DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();
        AntPathMatcher pathMatcher = new AntPathMatcher();

        return (HttpServletRequest request) -> {
            // Skip token resolution for always-public paths
            String path = request.getRequestURI();
            for (String pattern : PUBLIC_PATHS) {
                if (pathMatcher.match(pattern, path)) {
                    return null;
                }
            }
            // Skip token resolution for public GET paths
            if (HttpMethod.GET.name().equalsIgnoreCase(request.getMethod())) {
                for (String pattern : PUBLIC_GET_PATHS) {
                    if (pathMatcher.match(pattern, path)) {
                        return null;
                    }
                }
            }
            return defaultResolver.resolve(request);
        };
    }

    /**
     * JWT decoder using explicit JWKS URI derived from identity.url.
     *
     * We build the JWKS URI directly ({identity.url}/protocol/openid-connect/certs)
     * instead of using OIDC discovery because Keycloak's discovery document returns
     * jwks_uri with KC_HOSTNAME (e.g., http://localhost:8180/...), which is unreachable
     * from Docker containers that access Keycloak via host.docker.internal.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        String jwksUri = identityUrl + "/protocol/openid-connect/certs";
        return NimbusJwtDecoder.withJwkSetUri(jwksUri)
                .jwtProcessorCustomizer(processor -> {
                    // Accept "JWT", "at+jwt" (.NET IdentityServer), and "Bearer" (Keycloak access tokens)
                    processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(
                            JOSEObjectType.JWT, new JOSEObjectType("at+jwt"), new JOSEObjectType("Bearer")));
                })
                .build();
    }

    /**
     * Configure JWT authentication to use preferred_username as the principal name.
     *
     * Keycloak's 'sub' claim is a UUID (e.g., "f47ac10b-58cc-4372-..."), not the username.
     * Services use principal.getName() to identify users (e.g., as Redis key in Basket,
     * as buyerId in Ordering). By mapping preferred_username as the principal claim,
     * principal.getName() returns "alice" instead of a UUID, maintaining compatibility
     * with existing service logic.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }
}

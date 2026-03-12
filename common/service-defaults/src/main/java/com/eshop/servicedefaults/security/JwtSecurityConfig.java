/**
 * Converted from: src/eShop.ServiceDefaults/AuthenticationExtensions.cs
 * .NET Class: eShop.ServiceDefaults.AuthenticationExtensions
 *
 * JWT security configuration for resource servers.
 *
 * During migration, Java services validate JWTs issued by .NET Identity (Duende IdentityServer).
 * The identity.url must point to the .NET Identity HTTP endpoint (e.g., http://localhost:5223)
 * so that the JWKS endpoint is reachable for token signature verification.
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
                .jwt(jwt -> jwt.decoder(jwtDecoder()))
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

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(identityUrl + "/.well-known/openid-configuration/jwks")
            .jwtProcessorCustomizer(processor -> {
                // .NET Identity (Duende IdentityServer) issues access tokens with typ: at+jwt (RFC 9068).
                // Spring's default NimbusJwtDecoder only allows typ: JWT. Accept both.
                processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(
                    JOSEObjectType.JWT, new JOSEObjectType("at+jwt")));
            })
            .build();
    }
}

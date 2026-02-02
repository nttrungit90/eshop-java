/**
 * API versioning configuration to match .NET's api-version query parameter.
 */
package com.eshop.catalog.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApiVersionInterceptor())
                .addPathPatterns("/api/catalog/**");
    }

    public static class ApiVersionInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String apiVersion = request.getParameter("api-version");

            // Skip version check for OpenAPI/Swagger endpoints
            String path = request.getRequestURI();
            if (path.contains("/v3/api-docs") || path.contains("/swagger")) {
                return true;
            }

            if (apiVersion == null || apiVersion.isEmpty()) {
                response.setStatus(400);
                response.setContentType("application/problem+json");
                response.getWriter().write("""
                    {
                        "type": "https://docs.api-versioning.org/problems#unspecified",
                        "title": "Unspecified API version",
                        "status": 400,
                        "detail": "An API version is required, but was not specified."
                    }
                    """);
                return false;
            }

            // Validate version
            if (!apiVersion.equals("1.0") && !apiVersion.equals("2.0")) {
                response.setStatus(400);
                response.setContentType("application/problem+json");
                response.getWriter().write("""
                    {
                        "type": "https://docs.api-versioning.org/problems#unsupported",
                        "title": "Unsupported API version",
                        "status": 400,
                        "detail": "The specified API version is not supported. Supported versions: 1.0, 2.0"
                    }
                    """);
                return false;
            }

            // Store version for use in controllers
            request.setAttribute("api-version", apiVersion);
            return true;
        }
    }
}

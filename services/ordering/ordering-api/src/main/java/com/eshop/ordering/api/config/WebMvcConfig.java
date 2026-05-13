package com.eshop.ordering.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@SuppressWarnings("removal")
public class WebMvcConfig implements WebMvcConfigurer {

    // .NET ASP.NET routing accepts trailing slashes and is case-insensitive.
    // The .NET WebApp calls "/api/Orders/" — keep parity so OrderingService HTTP calls don't 404.
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(true);
    }
}

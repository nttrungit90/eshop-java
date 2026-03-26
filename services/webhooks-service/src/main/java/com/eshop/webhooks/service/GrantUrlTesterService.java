package com.eshop.webhooks.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Service
public class GrantUrlTesterService {

    private static final Logger log = LoggerFactory.getLogger(GrantUrlTesterService.class);

    private final RestClient restClient;

    @Value("${webhooks.grant-url-validation:true}")
    private boolean grantUrlValidationEnabled;

    public GrantUrlTesterService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public boolean testGrantUrl(String destUrl, String grantUrl, String token) {
        if (!grantUrlValidationEnabled) {
            log.info("Grant URL validation disabled, accepting subscription");
            return true;
        }
        if (!checkSameOrigin(destUrl, grantUrl)) {
            log.warn("Url of the hook ({}) and the grant url ({}) do not belong to same origin", destUrl, grantUrl);
            return false;
        }

        log.info("Sending the OPTIONS message to {} with token \"{}\"", grantUrl, token != null ? token : "");

        try {
            ResponseEntity<Void> response = restClient.options()
                    .uri(grantUrl)
                    .header("X-eshop-whtoken", token != null ? token : "")
                    .retrieve()
                    .toBodilessEntity();

            String tokenReceived = response.getHeaders().getFirst("X-eshop-whtoken");
            String tokenExpected = (token == null || token.isBlank()) ? null : token;

            log.info("Response code is {} for url {} and token in header was {} (expected token was {})",
                    response.getStatusCode(), grantUrl, tokenReceived, tokenExpected);

            return java.util.Objects.equals(tokenReceived, tokenExpected);
        } catch (Exception e) {
            log.warn("Exception {} when sending OPTIONS request. Url can't be granted.", e.getClass().getSimpleName());
            return false;
        }
    }

    private static boolean checkSameOrigin(String urlHook, String url) {
        try {
            URI first = URI.create(urlHook);
            URI second = URI.create(url);
            return first.getScheme().equals(second.getScheme())
                    && first.getHost().equals(second.getHost())
                    && first.getPort() == second.getPort();
        } catch (Exception e) {
            return false;
        }
    }
}

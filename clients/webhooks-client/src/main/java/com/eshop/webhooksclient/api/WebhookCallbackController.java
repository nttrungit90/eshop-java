package com.eshop.webhooksclient.api;

import com.eshop.webhooksclient.config.WebhookClientProperties;
import com.eshop.webhooksclient.model.WebhookData;
import com.eshop.webhooksclient.model.WebhookReceived;
import com.eshop.webhooksclient.service.HooksRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
public class WebhookCallbackController {

    private static final Logger log = LoggerFactory.getLogger(WebhookCallbackController.class);
    private static final String TOKEN_HEADER = "X-eshop-whtoken";

    private final HooksRepository hooksRepository;
    private final WebhookClientProperties properties;

    public WebhookCallbackController(HooksRepository hooksRepository, WebhookClientProperties properties) {
        this.hooksRepository = hooksRepository;
        this.properties = properties;
    }

    @RequestMapping(value = "/check", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> checkGrantUrl(
            @RequestHeader(name = TOKEN_HEADER, required = false) String token,
            HttpServletResponse response) {

        if (!properties.isValidateToken() || properties.getToken().equals(token)) {
            if (token != null) {
                response.setHeader(TOKEN_HEADER, token);
            }
            return ResponseEntity.ok().build();
        }

        log.warn("Invalid token received on /check: {}", token);
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/webhook-received")
    public ResponseEntity<?> webhookReceived(
            @RequestBody WebhookData hook,
            @RequestHeader(name = TOKEN_HEADER, required = false) String token) {

        log.info("Received webhook: type={}, token={}", hook.getType(), token);

        if (properties.isValidateToken() && !properties.getToken().equals(token)) {
            log.warn("Invalid token on webhook-received: {}", token);
            return ResponseEntity.badRequest().body("Invalid token");
        }

        var received = new WebhookReceived(Instant.now(), hook.getPayload(), token);
        hooksRepository.addNew(received);

        log.info("Webhook stored successfully");
        return ResponseEntity.ok(received);
    }

    @GetMapping("/api/messages")
    public ResponseEntity<?> getMessages() {
        return ResponseEntity.ok(hooksRepository.getAll());
    }
}

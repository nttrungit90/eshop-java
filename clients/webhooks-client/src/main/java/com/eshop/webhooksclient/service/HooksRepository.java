package com.eshop.webhooksclient.service;

import com.eshop.webhooksclient.model.WebhookReceived;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class HooksRepository {

    private final ConcurrentLinkedDeque<WebhookReceived> hooks = new ConcurrentLinkedDeque<>();

    public void addNew(WebhookReceived hook) {
        hooks.addFirst(hook);
    }

    public List<WebhookReceived> getAll() {
        return List.copyOf(hooks);
    }
}

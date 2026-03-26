package com.eshop.webhooksclient.web;

import com.eshop.webhooksclient.config.WebhookClientProperties;
import com.eshop.webhooksclient.model.WebhookSubscriptionRequest;
import com.eshop.webhooksclient.service.HooksRepository;
import com.eshop.webhooksclient.service.WebhooksApiClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final WebhooksApiClient webhooksApiClient;
    private final HooksRepository hooksRepository;
    private final WebhookClientProperties properties;

    public HomeController(WebhooksApiClient webhooksApiClient,
                          HooksRepository hooksRepository,
                          WebhookClientProperties properties) {
        this.webhooksApiClient = webhooksApiClient;
        this.hooksRepository = hooksRepository;
        this.properties = properties;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("webhooks", webhooksApiClient.listWebhooks());
        model.addAttribute("messages", hooksRepository.getAll());
        return "home";
    }

    @GetMapping("/add-webhook")
    public String addWebhookForm(Model model) {
        model.addAttribute("token", properties.getToken());
        return "add-webhook";
    }

    @PostMapping("/add-webhook")
    public String addWebhook(@RequestParam String token, RedirectAttributes redirectAttributes) {
        String selfUrl = properties.getSelfUrl();
        if (!selfUrl.endsWith("/")) selfUrl += "/";

        var request = new WebhookSubscriptionRequest();
        request.setUrl(selfUrl + "webhook-received");
        request.setGrantUrl(selfUrl + "check");
        request.setEvent("OrderPaid");
        request.setToken(token);

        boolean success = webhooksApiClient.registerWebhook(request);

        if (success) {
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", "Registration was rejected");
            return "redirect:/add-webhook";
        }
    }
}

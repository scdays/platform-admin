package com.vtc.platformadmin.ui.webhook;

import com.vtc.platformadmin.app.service.IWebhookTestAppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

/**
 * Webhook test receiver for local integration.
 * Set Partner defaultCallbackUrl to this endpoint, e.g.
 * http://127.0.0.1:35780/internal/dev/webhook/receive
 */
@RestController
@RequestMapping("/internal/dev/webhook")
@Api(tags = "Webhook test receiver")
public class WebhookTestReceiverUI {

    private final IWebhookTestAppService webhookTestAppService;

    public WebhookTestReceiverUI(IWebhookTestAppService webhookTestAppService) {
        this.webhookTestAppService = webhookTestAppService;
    }

    @ApiOperation("Receive platform webhook (200 OK, stored in memory inbox)")
    @PostMapping("/receive")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> receive(@RequestBody String rawBody, HttpServletRequest request) {
        String signature = request.getHeader("X-Webhook-Signature");
        String timestamp = request.getHeader("X-Webhook-Timestamp");
        webhookTestAppService.receive(rawBody, signature, timestamp);
        return Collections.singletonMap("received", true);
    }
}

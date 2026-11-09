package com.vtc.platformadmin.ui.webhook;

import com.botany.spore.core.result.Result;
import com.botany.spore.ddd.ui.BaseUI;
import com.vtc.platformadmin.infra.config.PlatformAdminProperties;
import com.vtc.platformadmin.infra.dev.WebhookTestInbox;
import com.vtc.platformadmin.ui.dto.WebhookTestInboxPageDto;
import com.vtc.platformadmin.ui.dto.WebhookTestReceiptDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * View or clear the in-memory webhook test inbox (internal admin API).
 */
@RestController
@RequestMapping("/internal/admin/webhook-test")
@Api(tags = "Webhook test inbox")
public class WebhookTestAdminUI extends BaseUI {

    private final WebhookTestInbox inbox;
    private final PlatformAdminProperties properties;

    public WebhookTestAdminUI(WebhookTestInbox inbox, PlatformAdminProperties properties) {
        this.inbox = inbox;
        this.properties = properties;
    }

    @ApiOperation("List webhook test inbox entries")
    @GetMapping("/inbox")
    public WebhookTestInboxPageDto listInbox(
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "size", defaultValue = "20") int size,
            HttpServletRequest request) {
        List<WebhookTestReceiptDto> items = inbox.list(eventType, size).stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        WebhookTestInboxPageDto page = new WebhookTestInboxPageDto();
        page.setTotal(inbox.count(eventType));
        page.setItems(items);
        page.setCallbackUrl(buildCallbackUrl(request));
        return page;
    }

    @ApiOperation("Clear webhook test inbox")
    @DeleteMapping("/inbox")
    public void clearInbox() {
        inbox.clear();
    }

    private WebhookTestReceiptDto toDto(WebhookTestInbox.WebhookTestReceipt receipt) {
        WebhookTestReceiptDto dto = new WebhookTestReceiptDto();
        BeanUtils.copyProperties(receipt, dto);
        return dto;
    }

    private String buildCallbackUrl(HttpServletRequest request) {
        String path = "/internal/dev/webhook/receive";
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
        if (defaultPort) {
            return scheme + "://" + host + path;
        }
        return scheme + "://" + host + ":" + port + path;
    }
}

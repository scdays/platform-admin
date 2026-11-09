package com.vtc.platformadmin.infra.webhook;

import com.vtc.platformadmin.domain.webhook.model.entity.WebhookDeliveryLogDO;
import com.vtc.platformadmin.domain.webhook.repository.IWebhookDeliveryLogRepository;
import com.vtc.platformadmin.domain.webhook.service.business.IWebhookDomainService;
import com.vtc.platformadmin.infra.config.PlatformAdminProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Webhook 退避重试调度器。
 * <p>
 * 定时扫描 webhook_delivery_log 中 status=FAILED 且 nextRetryAt<=now 的记录，
 * 调用 WebhookDomainService 重试，超过 maxRetry 标记永久失败。
 *
 * @author asset-security
 */
@Component
public class WebhookRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(WebhookRetryScheduler.class);

    private final IWebhookDeliveryLogRepository deliveryLogRepository;
    private final IWebhookDomainService webhookDomainService;
    private final PlatformAdminProperties platformAdminProperties;

    public WebhookRetryScheduler(IWebhookDeliveryLogRepository deliveryLogRepository,
                                  IWebhookDomainService webhookDomainService,
                                  PlatformAdminProperties platformAdminProperties) {
        this.deliveryLogRepository = deliveryLogRepository;
        this.webhookDomainService = webhookDomainService;
        this.platformAdminProperties = platformAdminProperties;
    }

    @Scheduled(fixedDelay = 60000) // 每分钟执行一次
    public void retryFailedDeliveries() {
        PlatformAdminProperties.Webhook webhookConfig = platformAdminProperties.getWebhook();
        if (!webhookConfig.isEnabled()) {
            log.debug("Webhook 重试调度器未启用");
            return;
        }

        Date now = Date.from(Instant.now());
        List<WebhookDeliveryLogDO> failedLogs = deliveryLogRepository.findByStatusAndNextRetryAtLessThanEqual("FAILED", now);

        for (WebhookDeliveryLogDO deliveryLog : failedLogs) {
            try {
                retryDelivery(deliveryLog, webhookConfig);
            } catch (Exception e) {
                log.error("Webhook 重试失败: id={}", deliveryLog.getId(), e);
            }
        }
    }

    private void retryDelivery(WebhookDeliveryLogDO deliveryLog, PlatformAdminProperties.Webhook webhookConfig) {
        Long id = deliveryLog.getId();
        int maxRetry = webhookConfig.getMaxRetry();
        int currentRetry = deliveryLog.getRetryCount() == null ? 0 : deliveryLog.getRetryCount();

        // 超过最大重试次数，标记永久失败，不再调度
        if (currentRetry >= maxRetry) {
            log.warn("Webhook 超过最大重试次数 {}，标记永久失败: id={} eventId={}",
                    maxRetry, id, deliveryLog.getEventId());
            deliveryLog.setStatus("DEAD");
            deliveryLog.setNextRetryAt(null);
            try {
                deliveryLogRepository.save(deliveryLog);
            } catch (Exception e) {
                log.error("Webhook 永久失败标记落库失败: id={}", id, e);
            }
            return;
        }

        // 调领域服务重投递
        WebhookDeliveryLogDO retried = webhookDomainService.retryDelivery(id);
        if (retried == null) {
            return;
        }

        // 仍失败则按退避策略设置下次重试时间
        if ("FAILED".equals(retried.getStatus())) {
            long delaySeconds = resolveBackoffSeconds(webhookConfig.getRetryBackoff(), retried.getRetryCount());
            Date nextRetryAt = new Date(System.currentTimeMillis() + delaySeconds * 1000L);
            retried.setNextRetryAt(nextRetryAt);
            try {
                deliveryLogRepository.save(retried);
            } catch (Exception e) {
                log.error("Webhook 下次重试时间落库失败: id={}", id, e);
            }
            log.info("Webhook 安排下次重试: id={} eventId={} retryCount={} nextRetryAt={} delayMs={}",
                    id, retried.getEventId(), retried.getRetryCount(), nextRetryAt, delaySeconds * 1000L);
        }
    }

    /**
     * 解析退避间隔（秒）。retryBackoff 形如 "60,300,900,1800,3600,7200"，
     * 按 retryCount 取对应位（超出范围则取最后一位）。
     */
    private long resolveBackoffSeconds(String retryBackoff, Integer retryCount) {
        if (retryBackoff == null || retryBackoff.isEmpty()) {
            return 60L;
        }
        String[] parts = retryBackoff.split(",");
        int idx = retryCount == null ? 0 : retryCount;
        if (idx < 0) {
            idx = 0;
        }
        if (idx >= parts.length) {
            idx = parts.length - 1;
        }
        try {
            return Long.parseLong(parts[idx].trim());
        } catch (NumberFormatException e) {
            return 60L;
        }
    }
}

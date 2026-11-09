package com.vtc.platformadmin.domain.webhook.service.business.impl;

import com.alibaba.fastjson.JSON;
import com.botany.spore.ddd.domain.service.DomainServiceImpl;
import com.vtc.platformadmin.domain.partner.model.entity.PartnerWebhookConfigDO;
import com.vtc.platformadmin.domain.partner.repository.IPartnerRepository;
import com.vtc.platformadmin.domain.webhook.model.entity.WebhookDeliveryLogDO;
import com.vtc.platformadmin.domain.webhook.model.event.OpenPlatformWebhookEvent;
import com.vtc.platformadmin.domain.invocation.model.support.WebhookDeliverySupport;
import com.vtc.platformadmin.domain.webhook.repository.IWebhookDeliveryLogRepository;
import com.vtc.platformadmin.domain.webhook.service.business.IWebhookDomainService;
import com.vtc.platformadmin.infra.adapter.WebhookDeliveryAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Webhook 投递领域服务实现（继承 DomainServiceImpl，通用 CRUD 由基类提供，仅实现本领域特有方法）。
 * <p>
 * 投递流程：幂等检查 -> 创建 delivery log -> 调用 adapter -> 更新状态。
 *
 * @author asset-security
 */
@Service
public class WebhookDomainServiceImpl
        extends DomainServiceImpl<IWebhookDeliveryLogRepository, WebhookDeliveryLogDO>
        implements IWebhookDomainService {

    private static final Logger log = LoggerFactory.getLogger(WebhookDomainServiceImpl.class);
    private static final DateTimeFormatter ISO_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneId.of("UTC"));

    private final IPartnerRepository partnerRepository;
    private final IWebhookDeliveryLogRepository deliveryLogRepository;
    private final WebhookDeliveryAdapter deliveryAdapter;

    public WebhookDomainServiceImpl(IWebhookDeliveryLogRepository deliveryLogRepository,
                                    IPartnerRepository partnerRepository,
                                    WebhookDeliveryAdapter deliveryAdapter) {
        this.deliveryLogRepository = deliveryLogRepository;
        this.partnerRepository = partnerRepository;
        this.deliveryAdapter = deliveryAdapter;
    }

    @Override
    public void deliver(OpenPlatformWebhookEvent event) {
        if (event == null || event.getPartnerId() == null) {
            return;
        }

        // 1. 获取合作方 Webhook 配置
        PartnerWebhookConfigDO config = partnerRepository.findWebhookConfig(event.getPartnerId());
        String callbackUrl = config != null ? config.getCallbackUrl() : null;
        if (!StringUtils.hasText(callbackUrl)) {
            log.warn("Partner {} 未配置 callbackUrl，跳过 Webhook 投递 eventName={}",
                    event.getPartnerId(), event.getEventName());
            return;
        }

        // Repository transparently decrypts webhookSecretHash into webhookSecret
        String webhookSecret = config.getWebhookSecret();

        // 2. 幂等检查：同一 eventId + partnerId 只投递一次
        String eventId = StringUtils.hasText(event.getEventId())
                ? event.getEventId()
                : "evt-" + UUID.randomUUID().toString().replace("-", "");
        event.setEventId(eventId);

        if (deliveryLogRepository.findByEventId(eventId) != null) {
            log.info("Webhook 已投递过，跳过: eventId={}", eventId);
            return;
        }

        // 3. 构建 envelope payload
        String occurredAt = event.getOccurredAt() != null
                ? ISO_FMT.format(Instant.ofEpochMilli(event.getOccurredAt()))
                : ISO_FMT.format(Instant.now());

        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", eventId);
        envelope.put("eventType", event.getEventName());
        envelope.put("occurredAt", occurredAt);
        envelope.put("partnerId", event.getPartnerId());
        envelope.put("payload", event.getPayload());

        String payloadJson = JSON.toJSONString(envelope);

        // 4. 创建 delivery log（PENDING）
        WebhookDeliveryLogDO deliveryLog = new WebhookDeliveryLogDO();
        deliveryLog.setEventId(eventId);
        deliveryLog.setPartnerId(event.getPartnerId());
        deliveryLog.setEventType(event.getEventName());
        deliveryLog.setResourceType(event.getResourceType());
        deliveryLog.setResourceId(event.getResourceId());
        deliveryLog.setPayloadJson(payloadJson);
        deliveryLog.setCallbackUrl(callbackUrl);
        deliveryLog.setRetryCount(0);
        deliveryLog.setTriggerSource(WebhookDeliverySupport.TRIGGER_FIRST_ATTEMPT);
        deliveryLog.setStatus("PENDING");
        deliveryLog.setCreatedAt(new Date());

        // 5. 调用 adapter 投递
        try {
            int httpStatus = deliveryAdapter.deliver(callbackUrl, payloadJson, webhookSecret);
            deliveryLog.setHttpStatus(httpStatus);
            deliveryLog.setStatus(httpStatus >= 200 && httpStatus < 300 ? "SUCCESS" : "FAILED");
        } catch (Exception ex) {
            log.warn("Webhook 投递失败: partnerId={} eventName={} eventId={}",
                    event.getPartnerId(), event.getEventName(), eventId, ex);
            deliveryLog.setHttpStatus(-1);
            deliveryLog.setStatus("FAILED");
        }

        // 6. 持久化投递日志（通用 save 由仓储基类提供）
        try {
            deliveryLogRepository.save(deliveryLog);
        } catch (Exception ex) {
            log.error("Webhook deliveryLog insert 失败: partnerId={} eventName={}: {}",
                    event.getPartnerId(), event.getEventName(), ex.getMessage(), ex);
        }

        log.info("Webhook 投递完成: eventId={} partnerId={} eventName={} status={}",
                eventId, event.getPartnerId(), event.getEventName(), deliveryLog.getStatus());
    }

    @Override
    public WebhookDeliveryLogDO retryDelivery(Long deliveryLogId) {
        if (deliveryLogId == null) {
            return null;
        }
        WebhookDeliveryLogDO deliveryLog = deliveryLogRepository.getById(deliveryLogId);
        if (deliveryLog == null) {
            log.warn("Webhook 重试：投递日志不存在，deliveryLogId={}", deliveryLogId);
            return null;
        }

        // 重试只处理失败记录，已成功的跳过
        if ("SUCCESS".equals(deliveryLog.getStatus())) {
            return deliveryLog;
        }

        String partnerId = deliveryLog.getPartnerId();
        String callbackUrl = deliveryLog.getCallbackUrl();
        if (!StringUtils.hasText(callbackUrl)) {
            log.warn("Webhook 重试：callbackUrl 为空，跳过 deliveryLogId={} eventId={}",
                    deliveryLogId, deliveryLog.getEventId());
            return deliveryLog;
        }

        // Repository transparently decrypts webhookSecretHash into webhookSecret
        String webhookSecret = null;
        if (StringUtils.hasText(partnerId)) {
            PartnerWebhookConfigDO config = partnerRepository.findWebhookConfig(partnerId);
            if (config != null) {
                webhookSecret = config.getWebhookSecret();
            }
        }

        String payloadJson = deliveryLog.getPayloadJson();
        int retryCount = deliveryLog.getRetryCount() == null ? 0 : deliveryLog.getRetryCount();
        deliveryLog.setRetryCount(retryCount + 1);
        deliveryLog.setTriggerSource(WebhookDeliverySupport.TRIGGER_AUTO_RETRY);

        try {
            int httpStatus = deliveryAdapter.deliver(callbackUrl, payloadJson, webhookSecret);
            deliveryLog.setHttpStatus(httpStatus);
            deliveryLog.setStatus(httpStatus >= 200 && httpStatus < 300 ? "SUCCESS" : "FAILED");
        } catch (Exception ex) {
            log.warn("Webhook 重试投递失败: deliveryLogId={} eventId={}",
                    deliveryLogId, deliveryLog.getEventId(), ex);
            deliveryLog.setHttpStatus(-1);
            deliveryLog.setStatus("FAILED");
        }

        // 失败时清空 nextRetryAt，由调度器按退避策略重新设置；成功时清空避免被重复调度
        deliveryLog.setNextRetryAt(null);

        try {
            deliveryLogRepository.save(deliveryLog);
        } catch (Exception ex) {
            log.error("Webhook 重试 deliveryLog 更新失败: deliveryLogId={}: {}",
                    deliveryLogId, ex.getMessage(), ex);
        }

        log.info("Webhook 重试完成: deliveryLogId={} eventId={} retryCount={} status={}",
                deliveryLogId, deliveryLog.getEventId(), deliveryLog.getRetryCount(), deliveryLog.getStatus());
        return deliveryLog;
    }

    @Override
    public WebhookDeliveryLogDO requireDeliveryLog(Long deliveryId) {
        if (deliveryId == null) {
            return null;
        }
        return deliveryLogRepository.getById(deliveryId);
    }

    @Override
    public List<WebhookDeliveryLogDO> listRelatedAttempts(WebhookDeliveryLogDO source) {
        if (source == null || !StringUtils.hasText(source.getEventId())) {
            return null;
        }
        // 返回同一个 eventId 下的所有投递尝试（包含当前这条）
        return deliveryLogRepository.listByEventId(source.getEventId());
    }
}

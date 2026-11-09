package com.vtc.platformadmin.infra.eventbus;

import com.vtc.asset.security.platform.eventbus.annotation.EventListener;
import com.vtc.asset.security.platform.eventbus.api.EventContext;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventConsumeTraceDO;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventTraceDO;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventConsumeTraceRepository;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventTraceRepository;
import com.vtc.platformadmin.domain.webhook.model.event.OpenPlatformWebhookEvent;
import com.vtc.platformadmin.domain.webhook.service.business.IWebhookDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 开放平台 Webhook 事件消费者。
 * <p>
 * 消费 Webhook 业务事件，写 event_trace 和 event_consume_trace，
 * 并委托 IWebhookDomainService 执行 Webhook 投递。
 *
 * @author asset-security
 */
@Component
public class OpenPlatformWebhookEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OpenPlatformWebhookEventHandler.class);

    private final IEventTraceRepository eventTraceRepository;
    private final IEventConsumeTraceRepository eventConsumeTraceRepository;
    private final IWebhookDomainService webhookDomainService;

    public OpenPlatformWebhookEventHandler(IEventTraceRepository eventTraceRepository,
                                           IEventConsumeTraceRepository eventConsumeTraceRepository,
                                           IWebhookDomainService webhookDomainService) {
        this.eventTraceRepository = eventTraceRepository;
        this.eventConsumeTraceRepository = eventConsumeTraceRepository;
        this.webhookDomainService = webhookDomainService;
    }

    @EventListener(
            topic = "open-platform-webhook",
            eventType = "open-platform.webhook.event",
            group = "platform-admin-webhook-dispatcher"
    )
    public void handle(OpenPlatformWebhookEvent event, EventContext context) {
        Date now = new Date();

        // 1. 写 event_trace（如不存在）
        try {
            EventTraceDO existingTrace = eventTraceRepository.findByEventId(context.getEnvelope().getEventId());
            if (existingTrace == null) {
                EventTraceDO trace = buildTrace(context, now);
                eventTraceRepository.save(trace);
            }
        } catch (Exception e) {
            log.warn("write event_trace failed, eventId={}", context.getEnvelope().getEventId(), e);
        }

        // 2. 执行 Webhook 投递
        try {
            log.info("webhook event received, eventId={}, partnerId={}, eventName={}, resourceType={}",
                    event.getEventId(), event.getPartnerId(), event.getEventName(), event.getResourceType());
            webhookDomainService.deliver(event);
        } catch (Exception e) {
            log.error("webhook event processing failed, eventId={}", event.getEventId(), e);
            consumeTrace(context, now, "FAILED", e.getMessage());
            throw e;
        }

        // 3. 写 event_consume_trace SUCCESS
        consumeTrace(context, now, "SUCCESS", null);
    }

    private EventTraceDO buildTrace(EventContext context, Date now) {
        EventTraceDO trace = new EventTraceDO();
        trace.setEventId(context.getEnvelope().getEventId());
        trace.setEventType(context.getEnvelope().getEventType());
        trace.setEventVersion(context.getEnvelope().getEventVersion());
        trace.setTopic(context.getTopic());
        trace.setDomain(context.getEnvelope().getDomain());
        trace.setSourceService(context.getEnvelope().getSourceService());
        trace.setTenantType(context.getEnvelope().getTenantType());
        trace.setTenantId(context.getEnvelope().getTenantId());
        trace.setSubjectType(context.getEnvelope().getSubjectType());
        trace.setSubjectId(context.getEnvelope().getSubjectId());
        trace.setTraceId(context.getEnvelope().getTraceId());
        trace.setIdempotentKey(context.getEnvelope().getIdempotentKey());
        trace.setOccurredAt(context.getEnvelope().getOccurredAt() != null
                ? new Date(context.getEnvelope().getOccurredAt()) : now);
        trace.setPublishedAt(now);
        trace.setStatus("RECEIVED");
        return trace;
    }

    private void consumeTrace(EventContext context, Date now, String status, String error) {
        try {
            EventConsumeTraceDO trace = new EventConsumeTraceDO();
            trace.setEventId(context.getEnvelope().getEventId());
            trace.setEventType(context.getEnvelope().getEventType());
            trace.setTopic(context.getTopic());
            trace.setConsumerService("platform-admin");
            trace.setConsumerGroup(context.getConsumerGroup());
            trace.setHandler("OpenPlatformWebhookEventHandler");
            trace.setConsumeStatus(status);
            trace.setRetryCount(context.getRetryCount() != null ? context.getRetryCount() : 0);
            trace.setLastError(error);
            trace.setStartedAt(now);
            trace.setFinishedAt(new Date());
            eventConsumeTraceRepository.save(trace);
        } catch (Exception e) {
            log.warn("write consume_trace failed, eventId={}", context.getEnvelope().getEventId(), e);
        }
    }
}
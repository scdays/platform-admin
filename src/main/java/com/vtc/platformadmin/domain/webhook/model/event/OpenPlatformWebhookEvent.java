package com.vtc.platformadmin.domain.webhook.model.event;

import com.vtc.asset.security.platform.eventbus.api.DomainEvent;
import com.vtc.asset.security.platform.eventbus.api.EventContextAware;

/**
 * 开放平台 Webhook 业务事件（EventBus 事件）。
 * <p>
 * 由 vul-pass / open-api-service 在业务完成后发布，
 * platform-admin 消费后执行 Webhook 投递。
 *
 * @author asset-security
 */
public class OpenPlatformWebhookEvent implements DomainEvent, EventContextAware {

    private String eventId;
    private String partnerId;
    private String eventName;
    private String resourceType;
    private String resourceId;
    private String routeMode;
    private Long occurredAt;
    private Object payload;
    private String traceId;

    @Override
    public String eventType() {
        return "open-platform.webhook.event";
    }

    @Override
    public String idempotentKey() {
        return "webhook:" + eventName + ":" + resourceType + ":" + resourceId;
    }

    @Override
    public String partitionKey() {
        return partnerId;
    }

    @Override
    public String tenantType() {
        return "partner";
    }

    @Override
    public String tenantId() {
        return partnerId;
    }

    @Override
    public String subjectType() {
        return resourceType;
    }

    @Override
    public String subjectId() {
        return resourceId;
    }

    // ======== getters / setters ========

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getPartnerId() { return partnerId; }
    public void setPartnerId(String partnerId) { this.partnerId = partnerId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getRouteMode() { return routeMode; }
    public void setRouteMode(String routeMode) { this.routeMode = routeMode; }

    public Long getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Long occurredAt) { this.occurredAt = occurredAt; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}
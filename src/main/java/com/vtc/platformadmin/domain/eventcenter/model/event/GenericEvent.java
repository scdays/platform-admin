package com.vtc.platformadmin.domain.eventcenter.model.event;

import com.vtc.asset.security.platform.eventbus.api.DomainEvent;
import com.vtc.asset.security.platform.eventbus.api.EventContextAware;

import java.util.Map;

/**
 * 通用事件载体。
 * <p>
 * 为 HTTP 事件发布入口提供非 Java 服务的事件承载能力。
 * 不绑定具体业务事件类型，payload 为任意 JSON 结构（Map），
 * 消费端按 {@link #eventType} 过滤后自行解析 payload。
 *
 * @author asset-security
 */
public class GenericEvent implements DomainEvent, EventContextAware {

    /** 事件类型，全局唯一，例如 "vul-pass.vulnerability.created" */
    private String eventType;

    /** 业务幂等键，用于消费端去重，必填 */
    private String idempotentKey;

    /** MQ 分区键，默认使用幂等键 */
    private String partitionKey;

    /** 事件版本，默认 "1.0" */
    private String eventVersion;

    /** 业务数据，任意 JSON 结构 */
    private Map<String, Object> payload;

    /** 租户类型，例如 partner / internal */
    private String tenantType;

    /** 租户 ID */
    private String tenantId;

    /** 事件主体类型，即操作人类型：admin / partner / system */
    private String subjectType;

    /** 事件主体 ID，即操作人标识 */
    private String subjectId;

    // ======== EventContextAware ========

    @Override
    public String tenantType() {
        return tenantType;
    }

    @Override
    public String tenantId() {
        return tenantId;
    }

    @Override
    public String subjectType() {
        return subjectType;
    }

    @Override
    public String subjectId() {
        return subjectId;
    }

    // ======== DomainEvent ========
    public String eventType() {
        return eventType;
    }

    @Override
    public String idempotentKey() {
        return idempotentKey;
    }

    @Override
    public String partitionKey() {
        return partitionKey != null ? partitionKey : idempotentKey();
    }

    @Override
    public String eventVersion() {
        return eventVersion != null ? eventVersion : "1.0";
    }

    // ======== getters / setters ========

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setIdempotentKey(String idempotentKey) {
        this.idempotentKey = idempotentKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public void setEventVersion(String eventVersion) {
        this.eventVersion = eventVersion;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public void setTenantType(String tenantType) {
        this.tenantType = tenantType;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }
}
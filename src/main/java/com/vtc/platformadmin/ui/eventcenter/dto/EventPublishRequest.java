package com.vtc.platformadmin.ui.eventcenter.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

/**
 * 事件发布请求 DTO。
 * <p>
 * 供非 Java 服务通过 HTTP 发布事件，无需实现 {@code DomainEvent} 接口。
 *
 * @author asset-security
 */
@ApiModel(value = "EventPublishRequest", description = "事件发布请求")
public class EventPublishRequest {

    @ApiModelProperty(value = "事件类型，全局唯一，例如 vul-pass.vulnerability.created", required = true)
    private String eventType;

    @ApiModelProperty(value = "目标 topic，不填使用默认 topic")
    private String topic;

    @ApiModelProperty(value = "业务幂等键，用于消费端去重，必填", required = true)
    private String idempotentKey;

    @ApiModelProperty(value = "MQ 分区键，默认使用幂等键")
    private String partitionKey;

    @ApiModelProperty(value = "事件版本，默认 1.0")
    private String eventVersion;

    @ApiModelProperty(value = "业务数据，任意 JSON 结构", required = true)
    private Map<String, Object> payload;

    @ApiModelProperty("操作人 ID（前端不传则后端从 CurrentUser 自动提取）")
    private String operatorId;

    @ApiModelProperty("操作人名称")
    private String operatorName;

    @ApiModelProperty("操作人类型：admin / partner / system")
    private String operatorType;

    @ApiModelProperty("租户类型（前端不传则后端从 CurrentUser 自动提取）")
    private String tenantType;

    @ApiModelProperty("租户 ID")
    private String tenantId;

    // ======== getters / setters ========

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getIdempotentKey() {
        return idempotentKey;
    }

    public void setIdempotentKey(String idempotentKey) {
        this.idempotentKey = idempotentKey;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public String getEventVersion() {
        return eventVersion;
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

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(String operatorType) {
        this.operatorType = operatorType;
    }

    public String getTenantType() {
        return tenantType;
    }

    public void setTenantType(String tenantType) {
        this.tenantType = tenantType;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
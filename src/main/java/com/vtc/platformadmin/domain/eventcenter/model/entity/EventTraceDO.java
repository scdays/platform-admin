package com.vtc.platformadmin.domain.eventcenter.model.entity;

import com.botany.spore.ddd.domain.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 事件发布轨迹。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventTraceDO extends BaseDO {

    private String eventId;
    private String eventType;
    private String eventVersion;
    private String topic;
    private String domain;
    private String sourceService;
    private String tenantType;
    private String tenantId;
    private String subjectType;
    private String subjectId;
    private String traceId;
    private String idempotentKey;
    private Date occurredAt;
    private Date publishedAt;
    private String payloadDigest;
    private String payload;
    private String status;
}
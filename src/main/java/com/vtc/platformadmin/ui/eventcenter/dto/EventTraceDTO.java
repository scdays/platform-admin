package com.vtc.platformadmin.ui.eventcenter.dto;

import com.botany.spore.ddd.ui.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 事件发布轨迹 UI 数据传输对象（不向 UI 暴露 DO）。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventTraceDTO extends BaseDTO {

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

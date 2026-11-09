package com.vtc.platformadmin.infra.dao.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.botany.spore.mybatis.pojo.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 事件发布轨迹 PO。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("event_trace")
public class EventTracePO extends BasePO {

    @TableId
    private Long id;

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
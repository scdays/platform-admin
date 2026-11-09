package com.vtc.platformadmin.domain.eventcenter.model.entity;

import com.botany.spore.ddd.domain.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 死信事件索引。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventDeadLetterDO extends BaseDO {

    private String eventId;
    private String eventType;
    private String originTopic;
    private String dlqTopic;
    private String consumerGroup;
    private String sourceService;
    private String failedService;
    private String failedHandler;
    private String failedReason;
    private String payload;
    private String headers;
    private Integer retryCount;
    private Date deadTime;
    private String replayStatus;
}
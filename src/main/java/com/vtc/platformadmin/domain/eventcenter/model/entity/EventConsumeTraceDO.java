package com.vtc.platformadmin.domain.eventcenter.model.entity;

import com.botany.spore.ddd.domain.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 事件消费轨迹。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventConsumeTraceDO extends BaseDO {

    private String eventId;
    private String eventType;
    private String topic;
    private String consumerService;
    private String consumerGroup;
    private String handler;
    private String consumeStatus;
    private Integer retryCount;
    private String lastError;
    private Date startedAt;
    private Date finishedAt;
}
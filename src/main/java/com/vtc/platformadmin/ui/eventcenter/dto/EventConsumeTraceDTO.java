package com.vtc.platformadmin.ui.eventcenter.dto;

import com.botany.spore.ddd.ui.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 事件消费轨迹 UI 数据传输对象（不向 UI 暴露 DO）。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventConsumeTraceDTO extends BaseDTO {

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

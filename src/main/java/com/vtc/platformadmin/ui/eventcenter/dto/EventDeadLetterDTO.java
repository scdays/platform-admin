package com.vtc.platformadmin.ui.eventcenter.dto;

import com.botany.spore.ddd.ui.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 死信事件 UI 数据传输对象（不向 UI 暴露 DO）。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventDeadLetterDTO extends BaseDTO {

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

package com.vtc.platformadmin.ui.eventcenter.dto;

import lombok.Data;

import java.util.Date;

/**
 * 死信事件分页查询参数。
 *
 * @author asset-security
 */
@Data
public class EventDeadLetterQueryParams {

    private String eventType;
    private String originTopic;
    private String consumerGroup;
    private String failedService;
    private String replayStatus;
    private Date startTime;
    private Date endTime;
    private Integer pageNum;
    private Integer pageSize;
}
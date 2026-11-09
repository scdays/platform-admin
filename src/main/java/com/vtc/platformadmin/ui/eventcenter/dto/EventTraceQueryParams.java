package com.vtc.platformadmin.ui.eventcenter.dto;

import lombok.Data;

import java.util.Date;

/**
 * 事件发布轨迹分页查询参数。
 *
 * @author asset-security
 */
@Data
public class EventTraceQueryParams {

    private String eventType;
    private String topic;
    private String sourceService;
    private String tenantType;
    private String tenantId;
    private String traceId;
    private String status;
    private Date startTime;
    private Date endTime;
    private Integer pageNum;
    private Integer pageSize;
}
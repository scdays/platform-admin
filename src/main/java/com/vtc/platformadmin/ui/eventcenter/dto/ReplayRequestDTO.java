package com.vtc.platformadmin.ui.eventcenter.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 事件重放请求 UI 数据传输对象。
 *
 * @author asset-security
 */
@Data
public class ReplayRequestDTO {

    private List<String> eventIds;
    private String eventType;
    private Date startTime;
    private Date endTime;
    private String targetConsumerGroup;
    private String remark;
}

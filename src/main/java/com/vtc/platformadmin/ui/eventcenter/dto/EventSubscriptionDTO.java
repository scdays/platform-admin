package com.vtc.platformadmin.ui.eventcenter.dto;

import com.botany.spore.ddd.ui.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 事件订阅 UI 数据传输对象。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventSubscriptionDTO extends BaseDTO {

    private String subscriptionId;
    private String subscriberService;
    private String consumerGroup;
    private List<String> eventTypes;
    private String topic;
    private String handler;
    private String status;
    private Date subscribedAt;
    private Date lastConsumedAt;
    private Long consumeCount;
    private Long errorCount;
}

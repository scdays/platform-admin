package com.vtc.platformadmin.infra.dao.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.botany.spore.mybatis.pojo.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 死信事件索引 PO。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("event_dead_letter")
public class EventDeadLetterPO extends BasePO {

    @TableId
    private Long id;

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
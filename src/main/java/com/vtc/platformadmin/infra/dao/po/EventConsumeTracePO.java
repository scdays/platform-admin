package com.vtc.platformadmin.infra.dao.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.botany.spore.mybatis.pojo.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 事件消费轨迹 PO。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("event_consume_trace")
public class EventConsumeTracePO extends BasePO {

    @TableId
    private Long id;

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
package com.vtc.platformadmin.domain.webhook.model.entity;

import com.botany.spore.ddd.domain.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Webhook 投递日志 DO。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WebhookDeliveryLogDO extends BaseDO {

    private Long id;

    private String partnerId;

    private String eventType;

    private String eventId;

    private String resourceType;

    private String resourceId;

    private String resourceIdsJson;

    private String payloadJson;

    private String callbackUrl;

    private Integer httpStatus;

    private Integer retryCount;

    private String triggerSource;

    private String status;

    private Date createdAt;

    private Date nextRetryAt;
}

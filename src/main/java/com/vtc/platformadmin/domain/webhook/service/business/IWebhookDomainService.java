package com.vtc.platformadmin.domain.webhook.service.business;

import com.vtc.platformadmin.domain.webhook.model.entity.WebhookDeliveryLogDO;
import com.vtc.platformadmin.domain.webhook.model.event.OpenPlatformWebhookEvent;

import java.util.List;

/**
 * Webhook 投递领域服务接口。
 *
 * @author asset-security
 */
public interface IWebhookDomainService {

    /**
     * 执行 Webhook 投递：幂等检查 -> 创建 delivery log -> 调用 adapter -> 更新状态。
     *
     * @param event Webhook 业务事件
     */
    void deliver(OpenPlatformWebhookEvent event);

    /**
     * 获取单条 Webhook 投递记录。
     *
     * @param deliveryId 投递记录 ID
     * @return 投递记录
     */
    WebhookDeliveryLogDO requireDeliveryLog(Long deliveryId);

    /**
     * 列出同一条事件的相关投递尝试（不同重试）。
     *
     * @param source 当前投递
     * @return 历史重试列表
     */
    List<WebhookDeliveryLogDO> listRelatedAttempts(WebhookDeliveryLogDO source);

    /**
     * 重试 Webhook 投递。
     *
     * @param deliveryLogId 投递日志 ID
     * @return 新的投递日志
     */
    WebhookDeliveryLogDO retryDelivery(Long deliveryLogId);
}
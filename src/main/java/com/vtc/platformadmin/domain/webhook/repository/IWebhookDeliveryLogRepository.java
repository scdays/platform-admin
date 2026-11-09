package com.vtc.platformadmin.domain.webhook.repository;

import com.botany.spore.ddd.domain.repository.IDatabaseRepository;
import com.vtc.platformadmin.domain.webhook.model.entity.WebhookDeliveryLogDO;

import java.util.Date;
import java.util.List;

/**
 * Webhook 投递日志仓储接口（通用 CRUD 由 IDatabaseRepository 基类提供，仅声明本领域特有方法）。
 *
 * @author asset-security
 */
public interface IWebhookDeliveryLogRepository extends IDatabaseRepository<WebhookDeliveryLogDO> {

    /**
     * 按业务事件标识 eventId 查询投递日志（幂等检查用）。
     *
     * @return 不存在时返回 null
     */
    WebhookDeliveryLogDO findByEventId(String eventId);

    /**
     * 按业务事件标识 eventId 查询所有投递尝试（当前尝试 + 所有历史重试）。
     *
     * @param eventId 业务事件 ID
     * @return 投递尝试列表（按创建时间排序）
     */
    List<WebhookDeliveryLogDO> listByEventId(String eventId);

    /**
     * 按状态和 nextRetryAt <= now 查询投递日志（重试调度用）。
     *
     * @param status 状态
     * @param nextRetryAt 下次重试时间
     * @return 投递日志列表
     */
    List<WebhookDeliveryLogDO> findByStatusAndNextRetryAtLessThanEqual(String status, Date nextRetryAt);

    /**
     * 按合作方 + 资源维度查询投递日志（工作台聚合用）。
     *
     * @param partnerId    合作方 ID
     * @param resourceType 资源类型（可为 null）
     * @param resourceId   资源 ID
     * @param limit        最大条数
     * @return 投递日志列表
     */
    List<WebhookDeliveryLogDO> listByResource(String partnerId, String resourceType, String resourceId, int limit);

    /**
     * 按条件分页查询投递日志（管理面列表用）。
     *
     * @param pageInfo    分页信息
     * @param partnerId   合作方 ID（可为 null）
     * @param eventType   事件类型（可为 null）
     * @param status      投递状态（可为 null）
     * @param resourceType 资源类型（可为 null）
     * @param resourceId  资源 ID（可为 null）
     * @return 投递日志分页
     */
    com.botany.spore.core.page.PageInfo<WebhookDeliveryLogDO> pageByConditions(
            com.botany.spore.core.page.PageInfo<WebhookDeliveryLogDO> pageInfo,
            String partnerId, String eventType, String status,
            String resourceType, String resourceId);
}

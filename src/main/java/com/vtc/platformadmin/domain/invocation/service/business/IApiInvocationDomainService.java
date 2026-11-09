package com.vtc.platformadmin.domain.invocation.service.business;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.domain.service.IDomainService;
import com.vtc.platformadmin.domain.invocation.model.InvocationContext;
import com.vtc.platformadmin.domain.invocation.model.entity.ApiInvocationDO;
import com.vtc.platformadmin.domain.invocation.model.query.InvocationAdminQuery;
import com.vtc.platformadmin.domain.invocation.model.query.WebhookDeliveryLogQuery;
import com.vtc.platformadmin.domain.invocation.model.result.PartnerInvocationStatsResult;
import com.vtc.platformadmin.domain.webhook.model.entity.WebhookDeliveryLogDO;

import java.util.Date;
import java.util.List;

/**
 * API 调用记录领域服务接口（通用 CRUD 由 IDomainService 基类提供，仅声明本领域特有方法）。
 *
 * @author asset-security
 */
public interface IApiInvocationDomainService extends IDomainService<ApiInvocationDO> {

    /**
     * 按条件分页查询调用记录（partnerId / apiCode 等值过滤 + startedAt 区间过滤）。
     *
     * @param pageInfo  分页参数
     * @param partnerId 合作方标识（可空）
     * @param apiCode   接口编码，映射 operationId（可空）
     * @param startTime 起始时间（可空）
     * @param endTime   截止时间（可空）
     * @return 分页结果
     */
    PageInfo<ApiInvocationDO> pageInvocations(PageInfo<ApiInvocationDO> pageInfo,
                                                String partnerId,
                                                String apiCode,
                                                Date startTime,
                                                Date endTime);

    /**
     * 按 InvocationAdminQuery 分页查询调用记录。
     */
    PageInfo<ApiInvocationDO> pageInvocations(InvocationAdminQuery query);

    /**
     * 获取单条调用记录。
     */
    ApiInvocationDO requireInvocation(String invocationId);

    /**
     * 获取 Partner 统计。
     */
    PartnerInvocationStatsResult queryPartnerStats(String partnerId);

    /**
     * 获取 Partner 配额统计。
     */
    com.vtc.platformadmin.domain.invocation.model.result.PartnerQuotaStatResult queryPartnerQuotaStats(String partnerId, Date from, Date to);

    /**
     * 开启一次调用记录。
     */
    void start(InvocationContext ctx);

    /**
     * 完成一次调用记录（带错误码和消息）。
     */
    void finish(InvocationContext ctx, int responseCode, String errorMessage);

    /**
     * 完成一次调用记录（带完整 ApiResponse 数据）。
     */
    void finish(InvocationContext ctx, Integer code, String message, Object data);

    /**
     * 列出关联的 Webhook 投递记录（用于调用详情页关联展示）。
     */
    List<WebhookDeliveryLogDO> listRelatedWebhookDeliveries(ApiInvocationDO invocation);

    /**
     * 分页查询 Webhook 投递记录。
     */
    PageInfo<WebhookDeliveryLogDO> pageWebhookDeliveries(WebhookDeliveryLogQuery query);

    /**
     * 工作台推送记录：按资源维度拉取投递日志（仅投递元数据，不解析 payload）。
     * <p>
     * 匹配主资源 + 关联子任务资源。
     *
     * @param partnerId      合作方 ID
     * @param resourceType   主资源类型（可为 null）
     * @param resourceId     主资源 ID
     * @param relatedTaskIds 关联子任务 ID 集合（可为 null/empty，用于 VerifyFixJob 场景）
     * @param limit          最大条数
     * @return 投递日志列表（按创建时间倒序）
     */
    List<WebhookDeliveryLogDO> listWebhookDeliveriesByResourceScope(String partnerId, String resourceType,
                                                                    String resourceId, java.util.Collection<String> relatedTaskIds,
                                                                    int limit);
}

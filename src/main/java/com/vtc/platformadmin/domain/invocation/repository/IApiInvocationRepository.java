package com.vtc.platformadmin.domain.invocation.repository;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.domain.repository.IDatabaseRepository;
import com.vtc.platformadmin.domain.invocation.model.entity.ApiInvocationDO;
import com.vtc.platformadmin.domain.invocation.model.query.InvocationAdminQuery;
import com.vtc.platformadmin.domain.invocation.model.query.WebhookDeliveryLogQuery;
import com.vtc.platformadmin.domain.invocation.model.result.InvocationDailyTrendStat;
import com.vtc.platformadmin.domain.invocation.model.result.InvocationErrorCodeStat;
import com.vtc.platformadmin.domain.invocation.model.result.PartnerQuotaStatResult;
import com.vtc.platformadmin.domain.webhook.model.entity.WebhookDeliveryLogDO;

import java.util.Date;
import java.util.List;

/**
 * API 调用记录仓储接口（通用 CRUD 由 IDatabaseRepository 基类提供，仅声明本领域特有方法）。
 *
 * @author asset-security
 */
public interface IApiInvocationRepository extends IDatabaseRepository<ApiInvocationDO> {

    void insert(ApiInvocationDO invocation);

    void updateFinish(ApiInvocationDO patch);

    void updateCaseId(String invocationId, String caseId);

    List<ApiInvocationDO> listByCaseId(String caseId, int limit);

    List<ApiInvocationDO> listCaseOperationsWithoutCaseId(String partnerId, List<String> operationIds, int limit);

    PageInfo<ApiInvocationDO> pageInvocations(PageInfo<ApiInvocationDO> pageInfo,
                                                String partnerId,
                                                String apiCode,
                                                Date startTime,
                                                Date endTime);

    PageInfo<ApiInvocationDO> pageInvocations(InvocationAdminQuery query);

    List<ApiInvocationDO> listRecentByPartnerAndOperations(String partnerId, List<String> operationIds,
                                                           Integer responseCode, int limit);

    ApiInvocationDO findByInvocationId(String invocationId);

    ApiInvocationDO findByRequestId(String requestId);

    List<WebhookDeliveryLogDO> listWebhookDeliveriesNear(String partnerId, Date from, Date to, int limit);

    WebhookDeliveryLogDO findWebhookDeliveryById(Long id);

    List<WebhookDeliveryLogDO> listWebhookDeliveriesByPartnerEventNear(
            String partnerId, String eventType, Date from, Date to, int limit);

    List<WebhookDeliveryLogDO> listByEventId(String partnerId, String eventId);

    List<WebhookDeliveryLogDO> listByResource(String partnerId, String resourceType, String resourceId, int limit);

    /**
     * 工作台推送记录：按资源维度拉取投递日志（仅投递元数据，不解析 payload）。
     * <p>
     * 匹配主资源 + 关联子任务资源（resourceId ∈ {resourceId, relatedTaskIds}）。
     *
     * @param partnerId      合作方 ID
     * @param resourceType   主资源类型（可为 null）
     * @param resourceId     主资源 ID
     * @param relatedTaskIds 关联子任务 ID 集合（可为 null/empty，用于 VerifyFixJob 场景）
     * @param limit          最大条数
     * @return 投递日志列表（按创建时间倒序）
     */
    List<WebhookDeliveryLogDO> listByResourceScope(String partnerId, String resourceType,
                                                   String resourceId, java.util.Collection<String> relatedTaskIds,
                                                   int limit);

    /**
     * 任务工作台：按 taskId 拉取 Partner 回调（含 EXPORT_READY，兼容历史 EXPORT/exportId 落库）。
     */
    List<WebhookDeliveryLogDO> listWebhookDeliveriesByTaskScope(String partnerId, String taskId, int limit);

    /**
     * 修复核验工作台：按 verifyFixJobId 拉取 Partner 回调（含 INSTANCE_VERIFY_FIX_COMPLETED / VERIFY_FIX_SCAN 外发与报告产物）。
     */
    List<WebhookDeliveryLogDO> listWebhookDeliveriesByVerifyFixJobScope(String partnerId, String jobId,
                                                                          java.util.Collection<String> relatedTaskIds,
                                                                          int limit);

    List<ApiInvocationDO> listInvocationsByResource(String partnerId, String resourceType, String resourceId, int limit);

    PageInfo<WebhookDeliveryLogDO> pageWebhookDeliveries(WebhookDeliveryLogQuery query);

    long countByPartnerAndTimeRange(String partnerId, Date from, Date to);

    long countSuccessByPartnerAndTimeRange(String partnerId, Date from, Date to);

    List<InvocationErrorCodeStat> listTopErrorCodes(String partnerId, Date from, Date to, int limit);

    List<InvocationDailyTrendStat> listDailyStats(String partnerId, Date from, Date to);

    PartnerQuotaStatResult summarizePartnerQuota(String partnerId, Date from, Date to);

    String findResponseBodyJson(String invocationId);

    long findResponseBodyByteSize(String invocationId);

    String findRequestBodyJson(String invocationId);

    long findRequestBodyByteSize(String invocationId);
}

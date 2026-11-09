package com.vtc.platformadmin.app.service;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.app.service.IAppService;
import com.vtc.platformadmin.ui.dto.PushRecordDTO;
import com.vtc.platformadmin.ui.dto.WebhookDeliveryLogDTO;
import com.vtc.platformadmin.ui.dto.WebhookDeliveryLogPageDto;
import com.vtc.platformadmin.ui.invocation.dto.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * API 调用记录应用服务接口（通用 CRUD 由 IAppService 基类提供，仅声明本领域特有方法）。
 *
 * @author asset-security
 */
public interface IApiInvocationAppService extends IAppService<InvocationDTO> {

    /**
     * 按条件分页查询调用记录，返回 DTO 分页。
     */
    PageInfo<InvocationDTO> pageInvocations(PageInfo<InvocationDTO> pageInfo,
                                            String partnerId,
                                            String apiCode,
                                            Date startTime,
                                            Date endTime);

    /**
     * 按 invocationId 查询调用记录详情，返回 DTO。
     *
     * @return 不存在时返回 null
     */
    InvocationDetailDTO getInvocationDetail(String invocationId);

    /**
     * 获取请求体。
     */
    InvocationRequestBodyDTO getInvocationRequestBody(String invocationId);

    /**
     * 获取响应体。
     */
    InvocationResponseBodyDTO getInvocationResponseBody(String invocationId);

    /**
     * 获取 Partner 统计。
     */
    PartnerInvocationStatsDto getPartnerStats(String partnerId);

    /**
     * 分页查询 Partner 配额。
     */
    PageInfo<PartnerQuotaDTO> listPartnerQuotas(PageInfo<PartnerQuotaDTO> pageInfo,
                                                String partnerId,
                                                String partnerName,
                                                String status,
                                                String startedFrom,
                                                String startedTo);

    /**
     * 按 InvocationAdminQuery 分页查询。
     */
    InvocationPageDto pageInvocationsAdmin(PageInfo<InvocationDTO> pageInfo,
                                           String partnerId,
                                           String operationId,
                                           String domain,
                                           Integer responseCode,
                                           String resourceType,
                                           String resourceId,
                                           String startedFrom,
                                           String startedTo);

    /**
     * 分页查询 Webhook 投递日志。
     */
    WebhookDeliveryLogPageDto listWebhookDeliveries(PageInfo<WebhookDeliveryLogDTO> pageInfo,
                                                         String partnerId,
                                                         String eventType,
                                                         String status,
                                                         String resourceType,
                                                         String resourceId);

    /**
     * 查询 Webhook 投递详情。
     *
     * @return 不存在时返回 null
     */
    WebhookDeliveryLogDTO getWebhookDeliveryDetail(Long deliveryId);

    /**
     * 手动重试 Webhook 投递，返回最新投递记录。
     */
    WebhookDeliveryLogDTO retryWebhookDelivery(Long deliveryId);

    /**
     * 工作台推送记录（投递元数据，不解析业务 payload）。
     * <p>
     * 与 open-api-service /webhook-event-details 配合使用，前端按 eventId 合并。
     *
     * @param partnerId      合作方 ID
     * @param resourceType   主资源类型
     * @param resourceId     主资源 ID
     * @param relatedTaskIds 关联子任务 ID 集合（VerifyFixJob 场景）
     * @param limit          最大条数
     * @return 推送记录列表（按 eventId 去重合并，仅投递元数据）
     */
    List<PushRecordDTO> listPushRecords(String partnerId, String resourceType, String resourceId,
                                        Collection<String> relatedTaskIds, int limit);
}

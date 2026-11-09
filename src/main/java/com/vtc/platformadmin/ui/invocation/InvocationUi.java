package com.vtc.platformadmin.ui.invocation;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.ui.BaseUI;
import com.vtc.platformadmin.app.service.IApiInvocationAppService;
import com.vtc.platformadmin.ui.dto.PushRecordDTO;
import com.vtc.platformadmin.ui.dto.WebhookDeliveryLogDTO;
import com.vtc.platformadmin.ui.dto.WebhookDeliveryLogPageDto;
import com.vtc.platformadmin.ui.invocation.dto.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * API 调用记录管理接口。
 * <p>
 * 仅注入 AppService，返回 DTO，不接触 Repository/DO。
 *
 * @author asset-security
 */
@RestController
@RequestMapping("/internal/admin")
public class InvocationUi extends BaseUI {

    private final IApiInvocationAppService apiInvocationAppService;

    public InvocationUi(IApiInvocationAppService apiInvocationAppService) {
        this.apiInvocationAppService = apiInvocationAppService;
    }

    /**
     * 分页查询 API 调用记录。
     */
    @ApiOperation("分页查询调用记录")
    @GetMapping("/invocations")
    public InvocationPageDto pageInvocationsAdmin(
            @RequestParam(required = false) String partnerId,
            @RequestParam(required = false) String operationId,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false, name = "response_code") Integer responseCode,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String startedFrom,
            @RequestParam(required = false) String startedTo,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        PageInfo<InvocationDTO> pageInfo = getPageInfo(page, size);
        return apiInvocationAppService.pageInvocationsAdmin(pageInfo, partnerId, operationId, domain, responseCode, resourceType, resourceId, startedFrom, startedTo);
    }

    /**
     * 查询单条 API 调用记录详情。
     */
    @ApiOperation("查询单条调用详情")
    @GetMapping("/invocations/{invocationId}")
    public InvocationDetailDTO getInvocationDetail(@PathVariable String invocationId) {
        return apiInvocationAppService.getInvocationDetail(invocationId);
    }

    @ApiOperation("按需获取调用响应报文（大报文二次加载）")
    @GetMapping("/invocations/{invocationId}/response-body")
    public InvocationResponseBodyDTO getInvocationResponseBody(@PathVariable String invocationId) {
        return apiInvocationAppService.getInvocationResponseBody(invocationId);
    }

    @ApiOperation("按需获取调用请求报文（大报文二次加载）")
    @GetMapping("/invocations/{invocationId}/request-body")
    public InvocationRequestBodyDTO getInvocationRequestBody(@PathVariable String invocationId) {
        return apiInvocationAppService.getInvocationRequestBody(invocationId);
    }

    @ApiOperation("分页查询 Partner 配额与调用统计")
    @GetMapping("/quotas")
    public PageInfo<PartnerQuotaDTO> listPartnerQuotas(
            @RequestParam(required = false) String partnerId,
            @RequestParam(required = false) String partnerName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startedFrom,
            @RequestParam(required = false) String startedTo,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        PageInfo<PartnerQuotaDTO> pageInfo = getPageInfo(page, size);
        return apiInvocationAppService.listPartnerQuotas(pageInfo, partnerId, partnerName, status, startedFrom, startedTo);
    }

    // @ApiOperation("获取 Partner 调用统计")
    // @GetMapping("/partners/{partnerId}/stats")
    /**
     * @see @PartnerAdminUI#getPartnerStats
     * @param partnerId
     * @return
     */
    @Deprecated
    public PartnerInvocationStatsDto getPartnerStats(@PathVariable String partnerId) {
        return apiInvocationAppService.getPartnerStats(partnerId);
    }

    // ========== Webhook 投递记录 ==========

    /**
     * 分页查询 Webhook 投递日志，支持按合作方/事件类型/状态/资源维度筛选。
     */
    @ApiOperation("分页查询 Webhook 投递日志")
    @GetMapping("/webhook-deliveries")
    public WebhookDeliveryLogPageDto listWebhookDeliveries(
            @RequestParam(required = false) String partnerId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        PageInfo<WebhookDeliveryLogDTO> pageInfo = getPageInfo(page, size);
        return apiInvocationAppService.listWebhookDeliveries(
                pageInfo, partnerId, eventType, status, resourceType, resourceId);
    }

    /**
     * 查询单条 Webhook 投递记录详情。
     */
    @ApiOperation("查询 Webhook 投递详情")
    @GetMapping("/webhook-deliveries/{deliveryId}")
    public WebhookDeliveryLogDTO getWebhookDeliveryDetail(@PathVariable Long deliveryId) {
        return apiInvocationAppService.getWebhookDeliveryDetail(deliveryId);
    }

    /**
     * 手动重试 Webhook 投递，返回重投递后的最新投递记录。
     */
    @ApiOperation("手动重试 Webhook 投递")
    @PostMapping("/webhook-deliveries/{deliveryId}/retry")
    public WebhookDeliveryLogDTO retryWebhookDelivery(@PathVariable Long deliveryId) {
        return apiInvocationAppService.retryWebhookDelivery(deliveryId);
    }

    // ========== 工作台推送记录（纯投递元数据） ==========

    /**
     * 工作台推送记录（投递元数据，不解析业务 payload）。
     * <p>
     * 前端用 eventId 关联 open-api-service /webhook-event-details 获取业务详情。
     */
    @ApiOperation("工作台推送记录（投递元数据，不解析业务payload）")
    @GetMapping("/push-records")
    public List<PushRecordDTO> listPushRecords(
            @RequestParam String partnerId,
            @RequestParam String resourceType,
            @RequestParam String resourceId,
            @RequestParam(required = false) Set<String> relatedTaskIds,
            @RequestParam(defaultValue = "100") int limit) {
        return apiInvocationAppService.listPushRecords(partnerId, resourceType, resourceId, relatedTaskIds, limit);
    }
}

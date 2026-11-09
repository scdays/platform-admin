package com.vtc.platformadmin.app.service.impl;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.app.service.AppServiceImpl;
import com.botany.spore.ddd.infra.utils.convertor.ConvertHelper;
import com.vtc.platformadmin.app.convert.InvocationAppConvertor;
import com.vtc.platformadmin.app.service.IApiInvocationAppService;
import com.vtc.platformadmin.domain.invocation.OpenApiConstants;
import com.vtc.platformadmin.domain.invocation.OpenApiException;
import com.vtc.platformadmin.domain.invocation.model.entity.ApiInvocationDO;
import com.vtc.platformadmin.domain.invocation.model.query.InvocationAdminQuery;
import com.vtc.platformadmin.domain.invocation.model.result.PartnerInvocationStatsResult;
import com.vtc.platformadmin.domain.invocation.repository.IApiInvocationRepository;
import com.vtc.platformadmin.domain.invocation.service.business.IApiInvocationDomainService;
import com.vtc.platformadmin.domain.partner.model.entity.PartnerDO;
import com.vtc.platformadmin.domain.partner.service.business.IPartnerDomainService;
import com.vtc.platformadmin.domain.webhook.model.entity.WebhookDeliveryLogDO;
import com.vtc.platformadmin.domain.webhook.service.business.IWebhookDomainService;
import com.vtc.platformadmin.ui.dto.ApiResponse;
import com.vtc.platformadmin.ui.dto.PushRecordDTO;
import com.vtc.platformadmin.ui.dto.WebhookDeliveryLogDTO;
import com.vtc.platformadmin.ui.dto.WebhookDeliveryLogDetailDTO;
import com.vtc.platformadmin.ui.dto.WebhookDeliveryLogPageDto;
import com.vtc.platformadmin.ui.invocation.dto.InvocationDetailDTO;
import com.vtc.platformadmin.ui.invocation.dto.InvocationDTO;
import com.vtc.platformadmin.ui.invocation.dto.InvocationPageDto;
import com.vtc.platformadmin.ui.invocation.dto.InvocationRequestBodyDTO;
import com.vtc.platformadmin.ui.invocation.dto.InvocationResponseBodyDTO;
import com.vtc.platformadmin.ui.invocation.dto.PartnerInvocationStatsDto;
import com.vtc.platformadmin.ui.invocation.dto.PartnerQuotaDTO;
import com.vtc.platformadmin.ui.invocation.dto.PartnerQuotaPageDto;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.vtc.platformadmin.domain.invocation.model.support.WebhookDeliverySupport;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API 调用记录应用服务实现（继承 AppServiceImpl，通用 CRUD 由基类提供，仅实现本领域特有方法）。
 * <p>
 * 标准调用链：Ui -> AppServiceImpl -> DomainServiceImpl -> RepositoryImpl -> Mapper/PO。
 *
 * @author asset-security
 */
@Service
public class ApiInvocationAppServiceImpl
        extends AppServiceImpl<IApiInvocationDomainService, ApiInvocationDO, InvocationDTO>
        implements IApiInvocationAppService {

    private static final DateTimeFormatter ISO_UTC =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter SIMPLE_DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter SIMPLE_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    private final IApiInvocationDomainService apiInvocationDomainService;
    private final IPartnerDomainService partnerDomainService;
    private final IWebhookDomainService webhookDomainService;
    private final IApiInvocationRepository apiInvocationRepository;
    private final InvocationAppConvertor invocationAppConvertor;

    public ApiInvocationAppServiceImpl(IApiInvocationDomainService domainService,
                                       IApiInvocationRepository apiInvocationRepository,
                                       InvocationAppConvertor invocationAppConvertor,
                                       IPartnerDomainService partnerDomainService,
                                       IWebhookDomainService webhookDomainService) {
        this.domainService = domainService;
        this.apiInvocationDomainService = domainService;
        this.apiInvocationRepository = apiInvocationRepository;
        this.invocationAppConvertor = invocationAppConvertor;
        this.partnerDomainService = partnerDomainService;
        this.webhookDomainService = webhookDomainService;
    }

    @Override
    public PageInfo<InvocationDTO> pageInvocations(PageInfo<InvocationDTO> pageInfo,
                                                   String partnerId,
                                                   String apiCode,
                                                   Date startTime,
                                                   Date endTime) {
        PageInfo<ApiInvocationDO> doPageInfo = ConvertHelper.convertPageInfo(pageInfo, ApiInvocationDO.class);
        PageInfo<ApiInvocationDO> result = apiInvocationDomainService.pageInvocations(doPageInfo, partnerId, apiCode, startTime, endTime);
        return ConvertHelper.convertPageInfo(result, InvocationDTO.class);
    }

    @Override
    public InvocationPageDto pageInvocationsAdmin(PageInfo<InvocationDTO> pageInfo,
                                                  String partnerId,
                                                  String operationId,
                                                  String domain,
                                                  Integer responseCode,
                                                  String resourceType,
                                                  String resourceId,
                                                  String startedFrom,
                                                  String startedTo) {
        if (pageInfo.getCurrent() < 1 || pageInfo.getSize() < 1) {
            throw new OpenApiException(OpenApiConstants.CODE_PARAM_ERROR, "page/size 无效");
        }

        InvocationAdminQuery query = new InvocationAdminQuery();
        query.setPartnerId(partnerId);
        query.setOperationId(operationId);
        query.setDomain(domain);
        query.setResponseCode(responseCode);
        query.setResourceType(resourceType);
        query.setResourceId(resourceId);
        query.setStartedFrom(parseDate(startedFrom));
        query.setStartedTo(parseDate(startedTo));
        query.setPage((int) pageInfo.getCurrent());
        query.setSize((int) pageInfo.getSize());

        PageInfo<ApiInvocationDO> resultPage = apiInvocationDomainService.pageInvocations(query);
        InvocationPageDto dto = new InvocationPageDto();
        dto.setItems(toInvocationList(resultPage.getRecords()));
        dto.setPage((int) resultPage.getCurrent());
        dto.setSize((int) resultPage.getSize());
        dto.setTotal(resultPage.getTotal());
        return dto;
    }

    @Override
    public InvocationDetailDTO getInvocationDetail(String invocationId) {
        ApiInvocationDO invocation = apiInvocationDomainService.requireInvocation(invocationId);
        List<WebhookDeliveryLogDO> relatedWebhooks = apiInvocationDomainService.listRelatedWebhookDeliveries(invocation);
        return toDetailDto(invocation, relatedWebhooks);
    }

    @Override
    public InvocationResponseBodyDTO getInvocationResponseBody(String invocationId) {
        ApiInvocationDO invocation = apiInvocationDomainService.requireInvocation(invocationId);
        String storedJson = apiInvocationRepository.findResponseBodyJson(invocation.getInvocationId());
        boolean stored = StringUtils.hasText(storedJson);
        String bodyFormatted = stored
                ? formatJson(storedJson)
                : buildFallbackResponseBodyPreview(invocation);

        InvocationResponseBodyDTO dto = new InvocationResponseBodyDTO();
        dto.setInvocationId(invocation.getInvocationId());
        dto.setRequestId(invocation.getRequestId());
        dto.setStored(stored);
        dto.setBodyFormatted(bodyFormatted);
        dto.setByteSize(stored
                ? apiInvocationRepository.findResponseBodyByteSize(invocation.getInvocationId())
                : (long) bodyFormatted.length());
        return dto;
    }

    @Override
    public InvocationRequestBodyDTO getInvocationRequestBody(String invocationId) {
        ApiInvocationDO invocation = apiInvocationDomainService.requireInvocation(invocationId);
        String storedJson = apiInvocationRepository.findRequestBodyJson(invocation.getInvocationId());
        boolean stored = StringUtils.hasText(storedJson);
        String bodyFormatted = stored
                ? formatJson(storedJson)
                : buildFallbackRequestBodyPreview(invocation);

        InvocationRequestBodyDTO dto = new InvocationRequestBodyDTO();
        dto.setInvocationId(invocation.getInvocationId());
        dto.setRequestId(invocation.getRequestId());
        dto.setStored(stored);
        dto.setBodyFormatted(bodyFormatted);
        dto.setByteSize(stored
                ? apiInvocationRepository.findRequestBodyByteSize(invocation.getInvocationId())
                : (long) bodyFormatted.length());
        return dto;
    }

    @Override
    public PartnerInvocationStatsDto getPartnerStats(String partnerId) {
        PartnerInvocationStatsResult result = apiInvocationDomainService.queryPartnerStats(partnerId);
        PartnerInvocationStatsDto dto = new PartnerInvocationStatsDto();
        dto.setPartnerId(result.getPartnerId());
        dto.setTodayTotal(result.getTodayTotal());
        dto.setTodaySuccess(result.getTodaySuccess());
        dto.setTodaySuccessRate(result.getTodaySuccessRate());
        dto.setTopErrorCodes(toTopErrors(result.getTopErrorCodes()));
        dto.setDailyTrend(toDailyTrend(result.getDailyTrend()));
        return dto;
    }

    @Override
    public PageInfo<PartnerQuotaDTO> listPartnerQuotas(PageInfo<PartnerQuotaDTO> pageInfo,
                                                      String partnerId,
                                                      String partnerName,
                                                      String status,
                                                      String startedFrom,
                                                      String startedTo) {
        if (pageInfo.getCurrent() < 1 || pageInfo.getSize() < 1) {
            throw new OpenApiException(OpenApiConstants.CODE_PARAM_ERROR, "page/size 无效");
        }

        Date from = parseDate(startedFrom);
        Date to = parseDate(startedTo);

        PageInfo<PartnerDO> partnerPage = new PageInfo<>();
        partnerPage.setCurrent(pageInfo.getCurrent());
        partnerPage.setSize(pageInfo.getSize());
        PageInfo<PartnerDO> resultPage = partnerDomainService.pagePartners(partnerPage, partnerId, partnerName, status);

        List<PartnerQuotaDTO> items = resultPage.getRecords().stream().map(partner -> {
            com.vtc.platformadmin.domain.invocation.model.result.PartnerQuotaStatResult stat =
                    apiInvocationDomainService.queryPartnerQuotaStats(partner.getPartnerId(), from, to);
            PartnerQuotaDTO dto = ConvertHelper.convert(partner, PartnerQuotaDTO.class);
            dto.setTotalInvocations(stat.getTotalInvocations());
            dto.setSuccessInvocations(stat.getSuccessInvocations());
            dto.setFailedInvocations(stat.getFailedInvocations());
            dto.setSuccessRate(stat.getSuccessRate());
            return dto;
        }).collect(Collectors.toList());

        PageInfo<PartnerQuotaDTO> result = ConvertHelper.convertPageInfo(resultPage, PartnerQuotaDTO.class);
        result.setRecords(items);
        return result;
    }

    @Override
    public WebhookDeliveryLogPageDto listWebhookDeliveries(PageInfo<WebhookDeliveryLogDTO> pageInfo,
                                                                  String partnerId,
                                                                  String eventType,
                                                                  String status,
                                                                  String resourceType,
                                                                  String resourceId) {
        com.vtc.platformadmin.domain.invocation.model.query.WebhookDeliveryLogQuery query =
                new com.vtc.platformadmin.domain.invocation.model.query.WebhookDeliveryLogQuery();
        query.setPartnerId(partnerId);
        query.setEventType(eventType);
        query.setStatus(status);
        query.setResourceType(resourceType);
        query.setResourceId(resourceId);
        query.setPage((int) pageInfo.getCurrent());
        query.setSize((int) pageInfo.getSize());

        PageInfo<WebhookDeliveryLogDO> doPage = apiInvocationDomainService.pageWebhookDeliveries(query);
        PageInfo<WebhookDeliveryLogDTO> dtoPage = ConvertHelper.convertPageInfo(doPage, WebhookDeliveryLogDTO.class);
        // 统一分页响应为 {items, page, size, total}，与 InvocationPageDto 等一致，前端按 data.items 消费
        WebhookDeliveryLogPageDto dto = new WebhookDeliveryLogPageDto();
        dto.setItems(dtoPage.getRecords());
        dto.setPage((int) dtoPage.getCurrent());
        dto.setSize((int) dtoPage.getSize());
        dto.setTotal(dtoPage.getTotal());
        return dto;
    }

    @Override
    public WebhookDeliveryLogDetailDTO getWebhookDeliveryDetail(Long deliveryId) {
        WebhookDeliveryLogDO source = webhookDomainService.requireDeliveryLog(deliveryId);
        List<WebhookDeliveryLogDO> history = webhookDomainService.listRelatedAttempts(source);
        WebhookDeliveryLogDetailDTO dto = invocationAppConvertor.toWebhookDeliveryLogDetailDto(source);
        dto.setAttemptCount(history == null ? 1 : history.size());
        dto.setPayloadJsonFormatted(formatJson(source.getPayloadJson()));
        dto.setRetryHistory(toRetryHistory(history));
        dto.setRelatedInvocations(toRelatedInvocations(source));
        return dto;
    }

    @Override
    public WebhookDeliveryLogDTO retryWebhookDelivery(Long deliveryId) {
        WebhookDeliveryLogDO result = webhookDomainService.retryDelivery(deliveryId);
        return invocationAppConvertor.toWebhookDeliveryLogDto(result);
    }

    @Override
    public List<PushRecordDTO> listPushRecords(String partnerId, String resourceType, String resourceId,
                                               Collection<String> relatedTaskIds, int limit) {
        // 1. 查询投递日志（仅投递元数据，不解析 payload）—— 走 invocation 领域，与其它 Webhook 投递查询同源
        List<WebhookDeliveryLogDO> rows = apiInvocationDomainService.listWebhookDeliveriesByResourceScope(
                partnerId, resourceType, resourceId, relatedTaskIds, limit);

        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 按 eventId 去重合并，保留最新记录，统计投递次数。
        // 历史/无 eventId 的行按行 id 单独成组（不丢弃），保证投递记录可见。
        Map<String, WebhookDeliveryLogDO> latestByKey = new LinkedHashMap<>();
        Map<String, Integer> attemptCountByKey = new LinkedHashMap<>();

        for (WebhookDeliveryLogDO row : rows) {
            String groupKey = resolvePushRecordGroupKey(row);
            attemptCountByKey.merge(groupKey, 1, Integer::sum);
            latestByKey.putIfAbsent(groupKey, row);
        }

        // 3. 解析 Partner 级有效可下载 eventType 白名单（来自 partner_webhook_config，
        //    未配置时为默认 EXPORT_READY / ARTIFACT_READY）。
        List<String> downloadableStages = partnerDomainService.resolveDownloadableStages(partnerId);

        // 4. 转换为 PushRecordDTO，每条按 eventType 计算 downloadable 标识
        List<PushRecordDTO> result = new ArrayList<>();
        for (Map.Entry<String, WebhookDeliveryLogDO> entry : latestByKey.entrySet()) {
            String groupKey = entry.getKey();
            WebhookDeliveryLogDO row = entry.getValue();
            int attemptCount = attemptCountByKey.getOrDefault(groupKey, 1);
            boolean downloadable = downloadableStages != null && row.getEventType() != null
                    && downloadableStages.contains(row.getEventType());
            result.add(invocationAppConvertor.toPushRecordDto(row, attemptCount, downloadable));
        }

        return result;
    }

    /**
     * 推送记录分组键：有 eventId 按 eventId 合并同事件多次投递；无 eventId 按行 id 单独成组。
     */
    private String resolvePushRecordGroupKey(WebhookDeliveryLogDO row) {
        String eventId = row.getEventId();
        if (StringUtils.hasText(eventId)) {
            return "evt:" + eventId;
        }
        return "row:" + row.getId();
    }

    private List<InvocationDTO> toInvocationList(List<ApiInvocationDO> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream().map(this::toListDto).collect(Collectors.toList());
    }

    private InvocationDTO toListDto(ApiInvocationDO row) {
        return invocationAppConvertor.toInvocationDto(row);
    }

    private InvocationDetailDTO toDetailDto(ApiInvocationDO row, List<WebhookDeliveryLogDO> relatedWebhooks) {
        InvocationDetailDTO dto = invocationAppConvertor.toInvocationDetailDto(row);
        dto.setResponseMessage(resolveResponseMessage(row));
        dto.setRequestHeadersPreview(buildRequestHeadersPreview(row));
        long requestBodyByteSize = apiInvocationRepository.findRequestBodyByteSize(row.getInvocationId());
        dto.setRequestBodyByteSize(requestBodyByteSize);
        dto.setHasRequestBody(requestBodyByteSize > 0L || canBuildFallbackRequestBody(row));
        dto.setResponseHeadersPreview(buildResponseHeadersPreview(row));
        long responseBodyByteSize = apiInvocationRepository.findResponseBodyByteSize(row.getInvocationId());
        dto.setResponseBodyByteSize(responseBodyByteSize);
        dto.setHasResponseBody(responseBodyByteSize > 0L || canBuildFallbackResponseBody(row));
        dto.setTimeline(buildTimeline(row, relatedWebhooks));
        dto.setRelatedWebhooks(toRelatedWebhooks(relatedWebhooks));
        return dto;
    }

    private String resolveResponseMessage(ApiInvocationDO row) {
        if (row.getResponseCode() != null && row.getResponseCode() == 0) {
            return "ok";
        }
        return StringUtils.hasText(row.getErrorMessage()) ? row.getErrorMessage() : "failed";
    }

    private String buildRequestHeadersPreview(ApiInvocationDO row) {
        StringBuilder sb = new StringBuilder();
        sb.append("Authorization: Bearer ***\n");
        sb.append("X-Partner-Id: ").append(nullToDash(row.getPartnerId())).append('\n');
        sb.append("Content-Type: application/json\n");
        sb.append("X-Request-Id: ").append(nullToDash(row.getRequestId()));
        return sb.toString();
    }

    private String buildFallbackRequestBodyPreview(ApiInvocationDO row) {
        StringBuilder json = new StringBuilder("{\n");
        json.append("  \"operationId\": \"").append(escapeJson(row.getOperationId())).append("\",\n");
        json.append("  \"httpMethod\": \"").append(escapeJson(row.getHttpMethod())).append("\",\n");
        json.append("  \"requestPath\": \"").append(escapeJson(row.getRequestPath())).append("\"");
        if (StringUtils.hasText(row.getResourceId())) {
            json.append(",\n  \"resourceId\": \"").append(escapeJson(row.getResourceId())).append("\"");
        }
        json.append("\n}");
        return json.toString();
    }

    private String buildResponseHeadersPreview(ApiInvocationDO row) {
        StringBuilder sb = new StringBuilder();
        sb.append("Content-Type: application/json\n");
        sb.append("X-Request-Id: ").append(nullToDash(row.getRequestId()));
        if (row.getHttpStatus() != null) {
            sb.append("\nHTTP-Status: ").append(row.getHttpStatus());
        }
        return sb.toString();
    }

    private boolean canBuildFallbackRequestBody(ApiInvocationDO row) {
        return StringUtils.hasText(row.getOperationId())
                || StringUtils.hasText(row.getRequestPath())
                || StringUtils.hasText(row.getResourceId());
    }

    private boolean canBuildFallbackResponseBody(ApiInvocationDO row) {
        return row.getResponseCode() != null
                || StringUtils.hasText(row.getRequestId())
                || StringUtils.hasText(row.getResourceId());
    }

    private String buildFallbackResponseBodyPreview(ApiInvocationDO row) {
        JSONObject envelope = new JSONObject(true);
        envelope.put("code", row.getResponseCode() == null ? 0 : row.getResponseCode());
        envelope.put("message", resolveResponseMessage(row));
        envelope.put("requestId", row.getRequestId());
        envelope.put("data", buildFallbackResponseData(row));
        return JSON.toJSONString(envelope, com.alibaba.fastjson.serializer.SerializerFeature.PrettyFormat);
    }

    private Object buildFallbackResponseData(ApiInvocationDO row) {
        if (!StringUtils.hasText(row.getResourceType()) && !StringUtils.hasText(row.getResourceId())) {
            return null;
        }
        JSONObject data = new JSONObject(true);
        if (StringUtils.hasText(row.getResourceId())) {
            if ("TASK".equalsIgnoreCase(row.getResourceType())) {
                data.put("taskId", row.getResourceId());
            } else if ("INSTANCE".equalsIgnoreCase(row.getResourceType())) {
                data.put("vulInfoID", row.getResourceId());
            } else if ("EXPORT".equalsIgnoreCase(row.getResourceType())) {
                data.put("exportId", row.getResourceId());
            } else {
                data.put("resourceId", row.getResourceId());
            }
        }
        return data.isEmpty() ? null : data;
    }

    private List<InvocationDetailDTO.InvocationTimelineItemDto> buildTimeline(
            ApiInvocationDO row, List<WebhookDeliveryLogDO> relatedWebhooks) {
        List<InvocationDetailDTO.InvocationTimelineItemDto> timeline = new ArrayList<>();
        timeline.add(timelineItem(formatDateTime(row.getStartedAt()),
                "partner-gateway 鉴权通过，注入 X-Partner-Id", false));
        timeline.add(timelineItem(formatDateTime(row.getStartedAt()),
                "open-api InvocationPipeline · operationId=" + nullToDash(row.getOperationId()), false));
        if (StringUtils.hasText(row.getResourceId())) {
            timeline.add(timelineItem(formatDateTime(row.getFinishedAt() != null ? row.getFinishedAt() : row.getStartedAt()),
                    "资源 " + row.getResourceType() + " / " + row.getResourceId(), false));
        }
        boolean failed = row.getResponseCode() != null && row.getResponseCode() != 0;
        timeline.add(timelineItem(formatDateTime(row.getFinishedAt()),
                "写入 api_invocation · code=" + row.getResponseCode()
                        + (row.getLatencyMs() != null ? " · " + row.getLatencyMs() + "ms" : ""),
                failed));
        if (relatedWebhooks != null) {
            for (WebhookDeliveryLogDO webhook : relatedWebhooks) {
                timeline.add(timelineItem(formatDateTime(webhook.getCreatedAt()),
                        "Webhook " + webhook.getEventType() + " · HTTP "
                                + (webhook.getHttpStatus() == null ? "-" : webhook.getHttpStatus()),
                        webhook.getHttpStatus() != null && webhook.getHttpStatus() >= 400));
            }
        }
        return timeline;
    }

    private InvocationDetailDTO.InvocationTimelineItemDto timelineItem(String at, String message, boolean failed) {
        InvocationDetailDTO.InvocationTimelineItemDto item = new InvocationDetailDTO.InvocationTimelineItemDto();
        item.setOccurredAt(at);
        item.setMessage(message);
        item.setFailed(failed);
        return item;
    }

    private List<InvocationDetailDTO.InvocationRelatedWebhookDto> toRelatedWebhooks(
            List<WebhookDeliveryLogDO> relatedWebhooks) {
        if (relatedWebhooks == null || relatedWebhooks.isEmpty()) {
            return Collections.emptyList();
        }
        return relatedWebhooks.stream().map(webhook -> {
            InvocationDetailDTO.InvocationRelatedWebhookDto dto = new InvocationDetailDTO.InvocationRelatedWebhookDto();
            dto.setId(webhook.getId());
            dto.setEventId(webhook.getEventId());
            dto.setEventType(webhook.getEventType());
            dto.setResourceType(webhook.getResourceType());
            dto.setResourceId(webhook.getResourceId());
            dto.setStatus(webhook.getStatus());
            dto.setHttpStatus(webhook.getHttpStatus());
            dto.setCreatedAt(formatDateTime(webhook.getCreatedAt()));
            return dto;
        }).collect(Collectors.toList());
    }

    private String formatDateTime(Date value) {
        if (value == null) {
            return "-";
        }
        return SIMPLE_DATETIME.format(value.toInstant());
    }

    private String nullToDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String formatJson(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        try {
            Object parsed = JSON.parse(raw);
            return JSON.toJSONString(parsed, com.alibaba.fastjson.serializer.SerializerFeature.PrettyFormat);
        } catch (Exception ex) {
            return raw;
        }
    }

    private List<PartnerInvocationStatsDto.ErrorCodeStatDto> toTopErrors(List<com.vtc.platformadmin.domain.invocation.model.result.InvocationErrorCodeStat> stats) {
        if (stats == null) {
            return null;
        }
        return stats.stream().map(stat -> {
            PartnerInvocationStatsDto.ErrorCodeStatDto dto = new PartnerInvocationStatsDto.ErrorCodeStatDto();
            dto.setResponseCode(stat.getResponseCode());
            dto.setCount(stat.getCount());
            return dto;
        }).collect(Collectors.toList());
    }

    private List<PartnerInvocationStatsDto.DailyTrendDto> toDailyTrend(List<com.vtc.platformadmin.domain.invocation.model.result.InvocationDailyTrendStat> stats) {
        if (stats == null) {
            return null;
        }
        return stats.stream().map(stat -> {
            PartnerInvocationStatsDto.DailyTrendDto dto = new PartnerInvocationStatsDto.DailyTrendDto();
            dto.setDate(stat.getStatDate().toString());
            dto.setTotalCount(stat.getTotalCount());
            dto.setSuccessCount(stat.getSuccessCount());
            dto.setFailCount(stat.getTotalCount() - stat.getSuccessCount());
            dto.setSuccessRate(calcRate(stat.getSuccessCount(), stat.getTotalCount()));
            return dto;
        }).collect(Collectors.toList());
    }

    private double calcRate(long success, long total) {
        if (total <= 0L) {
            return 0D;
        }
        return ((double) success) / (double) total;
    }

    private Date parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        DateTimeParseException parseException = null;
        try {
            return Date.from(ISO_UTC.parse(value, Instant::from));
        } catch (DateTimeParseException ex) {
            parseException = ex;
        }
        try {
            return Date.from(SIMPLE_DATETIME.parse(value, Instant::from));
        } catch (DateTimeParseException ex) {
            parseException = ex;
        }
        try {
            return Date.from(LocalDate.from(SIMPLE_DATE.parse(value))
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ex) {
            parseException = ex;
        }
        throw new OpenApiException(OpenApiConstants.CODE_PARAM_ERROR,
                "时间格式无效，支持 yyyy-MM-dd / yyyy-MM-dd HH:mm:ss / ISO8601 UTC", parseException);
    }

    private List<WebhookDeliveryLogDetailDTO.WebhookDeliveryRetryHistoryDto> toRetryHistory(
            List<WebhookDeliveryLogDO> history) {
        if (history == null || history.isEmpty()) {
            return Collections.emptyList();
        }
        return history.stream().map(row -> {
            WebhookDeliveryLogDetailDTO.WebhookDeliveryRetryHistoryDto item =
                    new WebhookDeliveryLogDetailDTO.WebhookDeliveryRetryHistoryDto();
            item.setId(row.getId());
            item.setRetryCount(row.getRetryCount());
            item.setHttpStatus(row.getHttpStatus());
            item.setStatus(row.getStatus());
            item.setCreatedAt(formatDateTime(row.getCreatedAt()));
            item.setTriggerSource(row.getTriggerSource());
            item.setManualRetry(WebhookDeliverySupport.TRIGGER_MANUAL_RETRY.equals(row.getTriggerSource()));
            return item;
        }).collect(Collectors.toList());
    }

    private List<WebhookDeliveryLogDetailDTO.RelatedInvocationDto> toRelatedInvocations(
            WebhookDeliveryLogDO source) {
        // 统一使用 webhook 域 WebhookDeliveryLogDO（唯一 DO），不再跨类强转。
        // 兼容历史无 eventId/resourceId 的记录：从 payloadJson 回填，用于关联调用记录。
        if (!StringUtils.hasText(source.getEventId())) {
            source.setEventId(WebhookDeliverySupport.parseEventId(source.getPayloadJson()));
        }
        if (!StringUtils.hasText(source.getResourceId())) {
            WebhookDeliverySupport.ResourceBinding binding =
                    WebhookDeliverySupport.extractResource(source.getEventType(), source.getPayloadJson());
            if (binding != null) {
                source.setResourceType(binding.getResourceType());
                source.setResourceId(binding.getResourceId());
                source.setResourceIdsJson(binding.getResourceIdsJson());
            }
        }
        String linkResourceId = WebhookDeliverySupport.resolveInvocationLinkResourceId(
                source.getEventType(), source.getResourceId(), source.getPayloadJson());
        if (!StringUtils.hasText(linkResourceId)) {
            return Collections.emptyList();
        }
        List<ApiInvocationDO> rows = apiInvocationRepository.listInvocationsByResource(
                source.getPartnerId(), null, linkResourceId, 10);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream().map(row -> {
            WebhookDeliveryLogDetailDTO.RelatedInvocationDto dto =
                    new WebhookDeliveryLogDetailDTO.RelatedInvocationDto();
            dto.setInvocationId(row.getInvocationId());
            dto.setOperationId(row.getOperationId());
            dto.setResourceType(row.getResourceType());
            dto.setResourceId(row.getResourceId());
            dto.setResponseCode(row.getResponseCode());
            dto.setStartedAt(formatDateTime(row.getStartedAt()));
            return dto;
        }).collect(Collectors.toList());
    }
}

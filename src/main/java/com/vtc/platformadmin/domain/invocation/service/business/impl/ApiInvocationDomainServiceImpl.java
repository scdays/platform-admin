package com.vtc.platformadmin.domain.invocation.service.business.impl;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.domain.service.DomainServiceImpl;
import com.vtc.platformadmin.domain.invocation.OpenApiConstants;
import com.vtc.platformadmin.domain.invocation.OpenApiException;
import com.vtc.platformadmin.domain.invocation.OpenApiOperations;
import com.vtc.platformadmin.domain.invocation.model.InvocationContext;
import com.vtc.platformadmin.domain.invocation.model.entity.ApiInvocationDO;
import com.vtc.platformadmin.domain.webhook.model.entity.WebhookDeliveryLogDO;
import com.vtc.platformadmin.domain.invocation.model.query.InvocationAdminQuery;
import com.vtc.platformadmin.domain.invocation.model.query.WebhookDeliveryLogQuery;
import com.vtc.platformadmin.domain.invocation.model.result.InvocationDailyTrendStat;
import com.vtc.platformadmin.domain.invocation.model.result.PartnerInvocationStatsResult;
import com.vtc.platformadmin.domain.invocation.model.result.PartnerQuotaStatResult;
import com.vtc.platformadmin.domain.invocation.repository.IApiInvocationRepository;
import com.vtc.platformadmin.domain.invocation.service.business.IApiInvocationDomainService;
import com.vtc.platformadmin.domain.partner.service.business.IPartnerDomainService;
// import moved to app layer, domain doesn't depend on UI
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * API 调用记录领域服务实现（继承 DomainServiceImpl，通用 CRUD 由基类提供，仅实现本领域特有方法）。
 *
 * @author asset-security
 */
@Service
public class ApiInvocationDomainServiceImpl
        extends DomainServiceImpl<IApiInvocationRepository, ApiInvocationDO>
        implements IApiInvocationDomainService {

    private static final Logger log = LoggerFactory.getLogger(ApiInvocationDomainServiceImpl.class);

    private static final int TREND_DAYS = 7;
    private static final int TOP_ERROR_CODE_SIZE = 5;

    private final IApiInvocationRepository apiInvocationRepository;
    private final IPartnerDomainService partnerDomainService;

    public ApiInvocationDomainServiceImpl(IApiInvocationRepository apiInvocationRepository,
                                          IPartnerDomainService partnerDomainService) {
        this.apiInvocationRepository = apiInvocationRepository;
        this.partnerDomainService = partnerDomainService;
    }

    @Override
    public PageInfo<ApiInvocationDO> pageInvocations(PageInfo<ApiInvocationDO> pageInfo,
                                                      String partnerId,
                                                      String apiCode,
                                                      Date startTime,
                                                      Date endTime) {
        return apiInvocationRepository.pageInvocations(pageInfo, partnerId, apiCode, startTime, endTime);
    }

    @Override
    public PageInfo<ApiInvocationDO> pageInvocations(InvocationAdminQuery query) {
        return apiInvocationRepository.pageInvocations(query);
    }

    @Override
    public void start(InvocationContext ctx) {
        String invocationId = "INV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        ctx.setInvocationId(invocationId);

        ApiInvocationDO row = new ApiInvocationDO();
        row.setInvocationId(invocationId);
        row.setRequestId(ctx.getRequestId());
        row.setPartnerId(ctx.getPartnerId());
        row.setOperationId(ctx.getOperationId());
        row.setHttpMethod(ctx.getHttpMethod());
        row.setRequestPath(ctx.getRequestPath());
        row.setClientIp(ctx.getClientIp());
        row.setStartedAt(new Date(ctx.getStartedAtMillis()));
        if (StringUtils.hasText(ctx.getRequestBodyJson())) {
            row.setRequestBodyJson(ctx.getRequestBodyJson());
        }
        apiInvocationRepository.insert(row);
        // OperationCase 创建已回退至 open-api-service 业务执行面
    }

    @Override
    public void finish(InvocationContext ctx, int responseCode, String errorMessage) {
        finishInternal(ctx, responseCode, errorMessage, null, null);
    }

    @Override
    public void finish(InvocationContext ctx, Integer code, String message, Object data) {
        if (code == null) {
            code = OpenApiConstants.CODE_ENGINE_FAILED;
        }
        finishInternal(ctx, code, message, data, serializeResponseBody(data));
    }

    private void finishInternal(InvocationContext ctx, Integer responseCode, String errorMessage, Object data, String responseBodyJson) {
        if (!StringUtils.hasText(ctx.getInvocationId())) {
            return;
        }
        // OperationCase 完成更新已回退至 open-api-service 业务执行面

        int latencyMs = (int) Math.min(Integer.MAX_VALUE,
                System.currentTimeMillis() - ctx.getStartedAtMillis());

        ApiInvocationDO row = new ApiInvocationDO();
        row.setInvocationId(ctx.getInvocationId());
        row.setResponseCode(responseCode);
        row.setHttpStatus(200);
        row.setLatencyMs(latencyMs);
        if (StringUtils.hasText(ctx.getCaseId())) {
            row.setResourceType(OpenApiOperations.RESOURCE_TYPE_CASE);
            row.setResourceId(ctx.getCaseId());
            row.setCaseId(ctx.getCaseId());
        } else {
            row.setResourceType(ctx.getResourceType());
            row.setResourceId(ctx.getResourceId());
        }
        row.setFinishedAt(new Date());
        row.setResponseBodyJson(responseBodyJson);
        if (responseCode != 0 && StringUtils.hasText(errorMessage)) {
            String msg = errorMessage.length() > 512 ? errorMessage.substring(0, 512) : errorMessage;
            row.setErrorMessage(msg);
        }
        apiInvocationRepository.updateFinish(row);
    }

    private String serializeResponseBody(Object response) {
        try {
            return JSON.toJSONString(response);
        } catch (Exception ex) {
            log.warn("serialize response body failed", ex);
            return null;
        }
    }

    @Override
    public ApiInvocationDO requireInvocation(String invocationOrRequestId) {
        if (!StringUtils.hasText(invocationOrRequestId)) {
            throw new OpenApiException(OpenApiConstants.CODE_PARAM_ERROR, "invocationId 不能为空");
        }
        ApiInvocationDO found = apiInvocationRepository.findByInvocationId(invocationOrRequestId);
        if (found == null) {
            found = apiInvocationRepository.findByRequestId(invocationOrRequestId);
        }
        if (found == null) {
            throw new OpenApiException(OpenApiConstants.CODE_PARAM_ERROR, "调用记录不存在");
        }
        return found;
    }

    @Override
    public List<WebhookDeliveryLogDO> listRelatedWebhookDeliveries(ApiInvocationDO invocation) {
        if (invocation == null || !StringUtils.hasText(invocation.getPartnerId())) {
            return Collections.emptyList();
        }
        if (StringUtils.hasText(invocation.getResourceId())) {
            return apiInvocationRepository.listByResource(
                    invocation.getPartnerId(),
                    null,
                    invocation.getResourceId(),
                    10);
        }
        if (invocation.getStartedAt() == null) {
            return Collections.emptyList();
        }
        Date from = invocation.getStartedAt();
        Date to = invocation.getFinishedAt() != null ? invocation.getFinishedAt() : invocation.getStartedAt();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(to);
        calendar.add(Calendar.MINUTE, 2);
        to = calendar.getTime();
        return apiInvocationRepository.listWebhookDeliveriesNear(invocation.getPartnerId(), from, to, 10);
    }

    @Override
    public PageInfo<WebhookDeliveryLogDO> pageWebhookDeliveries(WebhookDeliveryLogQuery query) {
        return apiInvocationRepository.pageWebhookDeliveries(query);
    }

    @Override
    public List<WebhookDeliveryLogDO> listWebhookDeliveriesByResourceScope(String partnerId, String resourceType,
                                                                           String resourceId,
                                                                           java.util.Collection<String> relatedTaskIds,
                                                                           int limit) {
        return apiInvocationRepository.listByResourceScope(partnerId, resourceType, resourceId, relatedTaskIds, limit);
    }

    @Override
    public PartnerInvocationStatsResult queryPartnerStats(String partnerId) {
        if (!StringUtils.hasText(partnerId)) {
            throw new OpenApiException(OpenApiConstants.CODE_PARAM_ERROR, "partnerId 不能为空");
        }
        partnerDomainService.requireByPartnerId(partnerId);

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        Date todayFrom = toDate(today);
        Date tomorrowFrom = toDate(today.plusDays(1));
        LocalDate trendStartDate = today.minusDays(TREND_DAYS - 1L);
        Date trendFrom = toDate(trendStartDate);

        long todayTotal = apiInvocationRepository.countByPartnerAndTimeRange(partnerId, todayFrom, tomorrowFrom);
        long todaySuccess = apiInvocationRepository.countSuccessByPartnerAndTimeRange(partnerId, todayFrom, tomorrowFrom);

        PartnerInvocationStatsResult result = new PartnerInvocationStatsResult();
        result.setPartnerId(partnerId);
        result.setTodayTotal(todayTotal);
        result.setTodaySuccess(todaySuccess);
        result.setTodaySuccessRate(calcRate(todaySuccess, todayTotal));
        result.setTopErrorCodes(apiInvocationRepository.listTopErrorCodes(
                partnerId, trendFrom, tomorrowFrom, TOP_ERROR_CODE_SIZE));
        result.setDailyTrend(completeTrend(
                trendStartDate,
                TREND_DAYS,
                apiInvocationRepository.listDailyStats(partnerId, trendFrom, tomorrowFrom)));
        return result;
    }

    @Override
    public PartnerQuotaStatResult queryPartnerQuotaStats(String partnerId, Date startedFrom, Date startedTo) {
        if (!StringUtils.hasText(partnerId)) {
            throw new OpenApiException(OpenApiConstants.CODE_PARAM_ERROR, "partnerId 不能为空");
        }
        return apiInvocationRepository.summarizePartnerQuota(partnerId, startedFrom, startedTo);
    }

    private List<InvocationDailyTrendStat> completeTrend(LocalDate startDate,
                                                          int days,
                                                          List<InvocationDailyTrendStat> actualStats) {
        Map<LocalDate, InvocationDailyTrendStat> byDay = new HashMap<>();
        if (actualStats != null) {
            for (InvocationDailyTrendStat stat : actualStats) {
                byDay.put(stat.getStatDate(), stat);
            }
        }
        List<InvocationDailyTrendStat> full = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            InvocationDailyTrendStat stat = byDay.get(date);
            if (stat == null) {
                stat = new InvocationDailyTrendStat();
                stat.setStatDate(date);
                stat.setTotalCount(0);
                stat.setSuccessCount(0);
            }
            full.add(stat);
        }
        return full;
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private double calcRate(long success, long total) {
        if (total <= 0L) {
            return 0D;
        }
        return ((double) success) / (double) total;
    }
}

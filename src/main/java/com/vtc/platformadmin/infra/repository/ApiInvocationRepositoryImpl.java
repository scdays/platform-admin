package com.vtc.platformadmin.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.infra.repository.DatabaseRepositoryImpl;
import com.botany.spore.ddd.infra.utils.convertor.ConvertHelper;
import com.vtc.platformadmin.domain.invocation.OpenApiOperations;
import com.vtc.platformadmin.domain.invocation.model.entity.ApiInvocationDO;
import com.vtc.platformadmin.domain.invocation.model.query.InvocationAdminQuery;
import com.vtc.platformadmin.domain.invocation.model.query.WebhookDeliveryLogQuery;
import com.vtc.platformadmin.domain.invocation.model.result.InvocationDailyTrendStat;
import com.vtc.platformadmin.domain.invocation.model.result.InvocationErrorCodeStat;
import com.vtc.platformadmin.domain.invocation.model.result.PartnerQuotaStatResult;
import com.vtc.platformadmin.domain.invocation.model.support.InvocationDomainSupport;
import com.vtc.platformadmin.domain.invocation.model.support.WebhookDeliverySupport;
import com.vtc.platformadmin.domain.invocation.repository.IApiInvocationRepository;
import com.vtc.platformadmin.domain.webhook.model.WebhookEventType;
import com.vtc.platformadmin.domain.webhook.model.entity.WebhookDeliveryLogDO;
import com.vtc.platformadmin.infra.dao.ApiInvocationMapper;
import com.vtc.platformadmin.infra.dao.WebhookDeliveryLogMapper;
import com.vtc.platformadmin.infra.dao.data.InvocationDailyStatRow;
import com.vtc.platformadmin.infra.dao.data.InvocationErrorCodeStatRow;
import com.vtc.platformadmin.infra.dao.po.ApiInvocationPO;
import com.vtc.platformadmin.infra.dao.po.WebhookDeliveryLogPO;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.util.Collections;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ApiInvocationRepositoryImpl
        extends DatabaseRepositoryImpl<ApiInvocationMapper, ApiInvocationDO, ApiInvocationPO>
        implements IApiInvocationRepository {

    private final ApiInvocationMapper apiInvocationMapper;
    private final WebhookDeliveryLogMapper webhookDeliveryLogMapper;

    public ApiInvocationRepositoryImpl(ApiInvocationMapper apiInvocationMapper,
                                       WebhookDeliveryLogMapper webhookDeliveryLogMapper) {
        this.apiInvocationMapper = apiInvocationMapper;
        this.webhookDeliveryLogMapper = webhookDeliveryLogMapper;
    }

    @Override
    public void insert(ApiInvocationDO invocation) {
        apiInvocationMapper.insert(ConvertHelper.convert(invocation, ApiInvocationPO.class));
    }

    @Override
    public void updateFinish(ApiInvocationDO patch) {
        if (!StringUtils.hasText(patch.getInvocationId())) {
            return;
        }
        ApiInvocationPO row = new ApiInvocationPO();
        row.setInvocationId(patch.getInvocationId());
        row.setResponseCode(patch.getResponseCode());
        row.setHttpStatus(patch.getHttpStatus());
        row.setLatencyMs(patch.getLatencyMs());
        row.setResourceType(patch.getResourceType());
        row.setResourceId(patch.getResourceId());
        row.setCaseId(patch.getCaseId());
        row.setFinishedAt(patch.getFinishedAt());
        row.setErrorMessage(patch.getErrorMessage());
        row.setResponseBodyJson(patch.getResponseBodyJson());
        apiInvocationMapper.updateById(row);
    }

    @Override
    public void updateCaseId(String invocationId, String caseId) {
        if (!StringUtils.hasText(invocationId) || !StringUtils.hasText(caseId)) {
            return;
        }
        ApiInvocationPO row = new ApiInvocationPO();
        row.setInvocationId(invocationId);
        row.setCaseId(caseId);
        apiInvocationMapper.updateById(row);
    }

    @Override
    public List<ApiInvocationDO> listByCaseId(String caseId, int limit) {
        if (!StringUtils.hasText(caseId)) {
            return Collections.emptyList();
        }
        int capped = limit > 0 ? Math.min(limit, 100) : 20;
        LambdaQueryWrapper<ApiInvocationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiInvocationPO::getCaseId, caseId);
        wrapper.orderByDesc(ApiInvocationPO::getStartedAt);
        wrapper.last("LIMIT " + capped);
        List<ApiInvocationPO> rows = apiInvocationMapper.selectList(wrapper);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream()
                .map(po -> ConvertHelper.convert(po, ApiInvocationDO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ApiInvocationDO> listCaseOperationsWithoutCaseId(String partnerId,
                                                                 List<String> operationIds,
                                                                 int limit) {
        if (operationIds == null || operationIds.isEmpty()) {
            return Collections.emptyList();
        }
        int capped = limit > 0 ? Math.min(limit, 500) : 200;
        LambdaQueryWrapper<ApiInvocationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ApiInvocationPO::getOperationId, operationIds);
        wrapper.and(w -> w.isNull(ApiInvocationPO::getCaseId).or().eq(ApiInvocationPO::getCaseId, ""));
        if (StringUtils.hasText(partnerId)) {
            wrapper.eq(ApiInvocationPO::getPartnerId, partnerId.trim());
        }
        wrapper.orderByDesc(ApiInvocationPO::getStartedAt);
        wrapper.last("LIMIT " + capped);
        List<ApiInvocationPO> rows = apiInvocationMapper.selectList(wrapper);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return ConvertHelper.convertList(rows, ApiInvocationDO.class);
    }

    @Override
    public PageInfo<ApiInvocationDO> pageInvocations(PageInfo<ApiInvocationDO> pageInfo,
                                                      String partnerId,
                                                      String apiCode,
                                                      Date startTime,
                                                      Date endTime) {
        LambdaQueryWrapper<ApiInvocationPO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(partnerId)) {
            wrapper.eq(ApiInvocationPO::getPartnerId, partnerId);
        }
        if (StringUtils.hasText(apiCode)) {
            wrapper.eq(ApiInvocationPO::getOperationId, apiCode);
        }
        if (startTime != null) {
            wrapper.ge(ApiInvocationPO::getStartedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(ApiInvocationPO::getStartedAt, endTime);
        }
        wrapper.orderByDesc(ApiInvocationPO::getStartedAt);
        Page<ApiInvocationPO> pageResult = apiInvocationMapper.selectPage(
                new Page<>((int) pageInfo.getCurrent(), pageInfo.getSize()), wrapper);

        PageInfo<ApiInvocationDO> result = new PageInfo<>();
        result.setCurrent(pageResult.getCurrent());
        result.setSize(pageResult.getSize());
        result.setTotal(pageResult.getTotal());
        result.setRecords(ConvertHelper.convertList(pageResult.getRecords(), ApiInvocationDO.class));
        return result;
    }

    @Override
    public PageInfo<ApiInvocationDO> pageInvocations(InvocationAdminQuery query) {
        LambdaQueryWrapper<ApiInvocationPO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getPartnerId())) {
            wrapper.eq(ApiInvocationPO::getPartnerId, query.getPartnerId());
        }
        if (StringUtils.hasText(query.getOperationId())) {
            wrapper.eq(ApiInvocationPO::getOperationId, query.getOperationId());
        }
        if (StringUtils.hasText(query.getDomain())) {
            List<String> operationIds = InvocationDomainSupport.operationIdsForDomain(query.getDomain());
            if (!operationIds.isEmpty()) {
                wrapper.in(ApiInvocationPO::getOperationId, operationIds);
            }
        }
        if (query.getResponseCode() != null) {
            wrapper.eq(ApiInvocationPO::getResponseCode, query.getResponseCode());
        }
        if (query.getStartedFrom() != null) {
            wrapper.ge(ApiInvocationPO::getStartedAt, query.getStartedFrom());
        }
        if (query.getStartedTo() != null) {
            wrapper.le(ApiInvocationPO::getStartedAt, query.getStartedTo());
        }
        if (StringUtils.hasText(query.getResourceId())) {
            String resourceId = query.getResourceId().trim();
            wrapper.eq(ApiInvocationPO::getResourceId, resourceId);
        } else if (StringUtils.hasText(query.getResourceType())) {
            wrapper.eq(ApiInvocationPO::getResourceType, query.getResourceType());
        }
        wrapper.orderByDesc(ApiInvocationPO::getStartedAt)
                .orderByDesc(ApiInvocationPO::getFinishedAt);
        Page<ApiInvocationPO> pageResult = apiInvocationMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()), wrapper);

        PageInfo<ApiInvocationDO> pageInfo = new PageInfo<>();
        pageInfo.setCurrent(query.getPage());
        pageInfo.setSize(query.getSize());
        pageInfo.setTotal(pageResult.getTotal());
        pageInfo.setRecords(ConvertHelper.convertList(pageResult.getRecords(), ApiInvocationDO.class));
        return pageInfo;
    }

    @Override
    public List<ApiInvocationDO> listRecentByPartnerAndOperations(String partnerId, List<String> operationIds,
                                                                  Integer responseCode, int limit) {
        if (!StringUtils.hasText(partnerId) || operationIds == null || operationIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ApiInvocationPO> wrapper = new LambdaQueryWrapper<ApiInvocationPO>()
                .eq(ApiInvocationPO::getPartnerId, partnerId.trim())
                .in(ApiInvocationPO::getOperationId, operationIds)
                .orderByDesc(ApiInvocationPO::getStartedAt)
                .orderByDesc(ApiInvocationPO::getFinishedAt);
        if (responseCode != null) {
            wrapper.eq(ApiInvocationPO::getResponseCode, responseCode);
        }
        int capped = Math.max(1, Math.min(limit, 500));
        wrapper.last("LIMIT " + capped);
        return ConvertHelper.convertList(apiInvocationMapper.selectList(wrapper), ApiInvocationDO.class);
    }

    @Override
    public ApiInvocationDO findByInvocationId(String invocationId) {
        if (!StringUtils.hasText(invocationId)) {
            return null;
        }
        ApiInvocationPO po = apiInvocationMapper.selectById(invocationId);
        return po == null ? null : ConvertHelper.convert(po, ApiInvocationDO.class);
    }

    @Override
    public ApiInvocationDO findByRequestId(String requestId) {
        if (!StringUtils.hasText(requestId)) {
            return null;
        }
        ApiInvocationPO po = apiInvocationMapper.selectOne(new LambdaQueryWrapper<ApiInvocationPO>()
                .eq(ApiInvocationPO::getRequestId, requestId)
                .orderByDesc(ApiInvocationPO::getStartedAt)
                .last("LIMIT 1"));
        return po == null ? null : ConvertHelper.convert(po, ApiInvocationDO.class);
    }

    @Override
    public List<WebhookDeliveryLogDO> listWebhookDeliveriesNear(String partnerId, Date from, Date to, int limit) {
        if (!StringUtils.hasText(partnerId) || from == null || to == null || limit <= 0) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<WebhookDeliveryLogPO> wrapper = new LambdaQueryWrapper<WebhookDeliveryLogPO>()
                .eq(WebhookDeliveryLogPO::getPartnerId, partnerId)
                .ge(WebhookDeliveryLogPO::getCreatedAt, from)
                .le(WebhookDeliveryLogPO::getCreatedAt, to)
                .orderByAsc(WebhookDeliveryLogPO::getCreatedAt)
                .last("LIMIT " + limit);
        List<WebhookDeliveryLogPO> rows = webhookDeliveryLogMapper.selectList(wrapper);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return ConvertHelper.convertList(rows, WebhookDeliveryLogDO.class);
    }

    @Override
    public WebhookDeliveryLogDO findWebhookDeliveryById(Long id) {
        if (id == null) {
            return null;
        }
        WebhookDeliveryLogPO po = webhookDeliveryLogMapper.selectById(id);
        return po == null ? null : ConvertHelper.convert(po, WebhookDeliveryLogDO.class);
    }

    @Override
    public List<WebhookDeliveryLogDO> listWebhookDeliveriesByPartnerEventNear(
            String partnerId, String eventType, Date from, Date to, int limit) {
        if (!StringUtils.hasText(partnerId) || from == null || to == null || limit <= 0) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<WebhookDeliveryLogPO> wrapper = new LambdaQueryWrapper<WebhookDeliveryLogPO>()
                .eq(WebhookDeliveryLogPO::getPartnerId, partnerId)
                .ge(WebhookDeliveryLogPO::getCreatedAt, from)
                .le(WebhookDeliveryLogPO::getCreatedAt, to)
                .orderByAsc(WebhookDeliveryLogPO::getCreatedAt)
                .last("LIMIT " + limit);
        if (StringUtils.hasText(eventType)) {
            wrapper.eq(WebhookDeliveryLogPO::getEventType, eventType);
        }
        List<WebhookDeliveryLogPO> rows = webhookDeliveryLogMapper.selectList(wrapper);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return ConvertHelper.convertList(rows, WebhookDeliveryLogDO.class);
    }

    @Override
    public List<WebhookDeliveryLogDO> listByEventId(String partnerId, String eventId) {
        if (!StringUtils.hasText(partnerId) || !StringUtils.hasText(eventId)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<WebhookDeliveryLogPO> wrapper = new LambdaQueryWrapper<WebhookDeliveryLogPO>()
                .eq(WebhookDeliveryLogPO::getPartnerId, partnerId)
                .eq(WebhookDeliveryLogPO::getEventId, eventId)
                .orderByAsc(WebhookDeliveryLogPO::getCreatedAt)
                .orderByAsc(WebhookDeliveryLogPO::getId);
        List<WebhookDeliveryLogPO> rows = webhookDeliveryLogMapper.selectList(wrapper);
        if (rows == null || rows.isEmpty()) {
            LambdaQueryWrapper<WebhookDeliveryLogPO> legacyWrapper = new LambdaQueryWrapper<WebhookDeliveryLogPO>()
                    .eq(WebhookDeliveryLogPO::getPartnerId, partnerId)
                    .like(WebhookDeliveryLogPO::getPayloadJson, "\"eventId\":\"" + eventId + "\"")
                    .orderByAsc(WebhookDeliveryLogPO::getCreatedAt)
                    .orderByAsc(WebhookDeliveryLogPO::getId);
            rows = webhookDeliveryLogMapper.selectList(legacyWrapper);
        }
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return ConvertHelper.convertList(rows, WebhookDeliveryLogDO.class);
    }

    @Override
    public List<WebhookDeliveryLogDO> listByResource(String partnerId, String resourceType, String resourceId, int limit) {
        if (!StringUtils.hasText(partnerId) || !StringUtils.hasText(resourceId) || limit <= 0) {
            return Collections.emptyList();
        }
        String trimmedResourceId = resourceId.trim();
        LambdaQueryWrapper<WebhookDeliveryLogPO> wrapper = new LambdaQueryWrapper<WebhookDeliveryLogPO>()
                .eq(WebhookDeliveryLogPO::getPartnerId, partnerId);
        if (StringUtils.hasText(resourceType)) {
            wrapper.eq(WebhookDeliveryLogPO::getResourceType, resourceType);
        }
        wrapper.and(w -> w.eq(WebhookDeliveryLogPO::getResourceId, trimmedResourceId)
                        .or().like(WebhookDeliveryLogPO::getResourceIdsJson, trimmedResourceId))
                .orderByDesc(WebhookDeliveryLogPO::getCreatedAt)
                .orderByDesc(WebhookDeliveryLogPO::getId)
                .last("LIMIT " + limit);
        List<WebhookDeliveryLogPO> rows = webhookDeliveryLogMapper.selectList(wrapper);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return ConvertHelper.convertList(rows, WebhookDeliveryLogDO.class);
    }

    @Override
    public List<WebhookDeliveryLogDO> listByResourceScope(String partnerId, String resourceType,
                                                          String resourceId, Collection<String> relatedTaskIds,
                                                          int limit) {
        if (!StringUtils.hasText(partnerId) || !StringUtils.hasText(resourceId) || limit <= 0) {
            return Collections.emptyList();
        }
        // 收集主资源 + 关联子任务资源 ID（去空去重）
        String primaryResourceId = resourceId.trim();
        java.util.Set<String> resourceIds = new java.util.LinkedHashSet<>();
        resourceIds.add(primaryResourceId);
        if (relatedTaskIds != null) {
            for (String tid : relatedTaskIds) {
                if (StringUtils.hasText(tid)) {
                    resourceIds.add(tid.trim());
                }
            }
        }
        // 仅按投递路由元数据过滤（resource_id / resourceIdsJson），不解析 payload、不强制 resource_type 等值
        // （resource_type 在多资源路由场景如 INSTANCE_VERIFY_FIX_COMPLETED 下与主资源类型不一致，强制等值会漏数据）。
        // resourceIdsJson 命中用于多资源路由：INSTANCE_VERIFY_FIX_COMPLETED 把 vulInfoId 列表存在 resourceIdsJson，
        // 主资源 resource_id 为 verifyFixJobId，故按 vulInfoId 查询时需走 resourceIdsJson。
        LambdaQueryWrapper<WebhookDeliveryLogPO> wrapper = new LambdaQueryWrapper<WebhookDeliveryLogPO>()
                .eq(WebhookDeliveryLogPO::getPartnerId, partnerId)
                .and(w -> w.in(WebhookDeliveryLogPO::getResourceId, resourceIds)
                        .or().like(WebhookDeliveryLogPO::getResourceIdsJson, primaryResourceId))
                .orderByDesc(WebhookDeliveryLogPO::getCreatedAt)
                .orderByDesc(WebhookDeliveryLogPO::getId)
                .last("LIMIT " + limit);
        List<WebhookDeliveryLogPO> rows = webhookDeliveryLogMapper.selectList(wrapper);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return ConvertHelper.convertList(rows, WebhookDeliveryLogDO.class);
    }

    @Override
    public List<WebhookDeliveryLogDO> listWebhookDeliveriesByTaskScope(String partnerId, String taskId, int limit) {
        if (!StringUtils.hasText(partnerId) || !StringUtils.hasText(taskId) || limit <= 0) {
            return Collections.emptyList();
        }
        String trimmedTaskId = taskId.trim();
        String exportTaskPayloadToken = "\"taskId\":\"" + trimmedTaskId + "\"";
        LambdaQueryWrapper<WebhookDeliveryLogPO> wrapper = new LambdaQueryWrapper<WebhookDeliveryLogPO>()
                .eq(WebhookDeliveryLogPO::getPartnerId, partnerId)
                .and(w -> w.nested(n -> n.eq(WebhookDeliveryLogPO::getResourceType, WebhookDeliverySupport.RESOURCE_TASK)
                                .eq(WebhookDeliveryLogPO::getResourceId, trimmedTaskId))
                        .or()
                        .nested(n -> n.eq(WebhookDeliveryLogPO::getEventType, WebhookEventType.EXPORT_READY)
                                .like(WebhookDeliveryLogPO::getPayloadJson, exportTaskPayloadToken)))
                .orderByDesc(WebhookDeliveryLogPO::getCreatedAt)
                .orderByDesc(WebhookDeliveryLogPO::getId)
                .last("LIMIT " + limit);
        List<WebhookDeliveryLogPO> rows = webhookDeliveryLogMapper.selectList(wrapper);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return ConvertHelper.convertList(rows, WebhookDeliveryLogDO.class);
    }

    @Override
    public List<WebhookDeliveryLogDO> listWebhookDeliveriesByVerifyFixJobScope(String partnerId, String jobId,
                                                                               Collection<String> relatedTaskIds,
                                                                               int limit) {
        if (!StringUtils.hasText(partnerId) || !StringUtils.hasText(jobId) || limit <= 0) {
            return Collections.emptyList();
        }
        String trimmedJobId = jobId.trim();
        String verifyFixJobToken = "\"verifyFixJobId\":\"" + trimmedJobId + "\"";
        String verifyFixScanStageToken = "\"exportStage\":\"" + OpenApiOperations.EXPORT_STAGE_VERIFY_FIX_SCAN + "\"";
        LambdaQueryWrapper<WebhookDeliveryLogPO> wrapper = new LambdaQueryWrapper<WebhookDeliveryLogPO>()
                .eq(WebhookDeliveryLogPO::getPartnerId, partnerId)
                .and(w -> {
                    w.nested(n -> n.eq(WebhookDeliveryLogPO::getResourceType, OpenApiOperations.PRIMARY_RESOURCE_VERIFY_FIX_JOB)
                            .eq(WebhookDeliveryLogPO::getResourceId, trimmedJobId));
                    w.or().nested(n -> n.eq(WebhookDeliveryLogPO::getEventType, WebhookEventType.INSTANCE_VERIFY_FIX_COMPLETED)
                            .like(WebhookDeliveryLogPO::getPayloadJson, verifyFixJobToken));
                    w.or().nested(n -> n.eq(WebhookDeliveryLogPO::getEventType, WebhookEventType.EXPORT_READY)
                            .like(WebhookDeliveryLogPO::getPayloadJson, verifyFixJobToken));
                    w.or().nested(n -> n.eq(WebhookDeliveryLogPO::getEventType, WebhookEventType.ARTIFACT_READY)
                            .like(WebhookDeliveryLogPO::getPayloadJson, verifyFixJobToken));
                    if (relatedTaskIds != null) {
                        for (String taskId : relatedTaskIds) {
                            if (!StringUtils.hasText(taskId)) {
                                continue;
                            }
                            String taskToken = "\"taskId\":\"" + taskId.trim() + "\"";
                            w.or().nested(n -> n.eq(WebhookDeliveryLogPO::getEventType, WebhookEventType.EXPORT_READY)
                                    .like(WebhookDeliveryLogPO::getPayloadJson, verifyFixScanStageToken)
                                    .like(WebhookDeliveryLogPO::getPayloadJson, taskToken));
                            w.or().nested(n -> n.eq(WebhookDeliveryLogPO::getEventType, WebhookEventType.ARTIFACT_READY)
                                    .like(WebhookDeliveryLogPO::getPayloadJson, verifyFixScanStageToken)
                                    .like(WebhookDeliveryLogPO::getPayloadJson, taskToken));
                        }
                    }
                })
                .orderByDesc(WebhookDeliveryLogPO::getCreatedAt)
                .orderByDesc(WebhookDeliveryLogPO::getId)
                .last("LIMIT " + limit);
        List<WebhookDeliveryLogPO> rows = webhookDeliveryLogMapper.selectList(wrapper);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return ConvertHelper.convertList(rows, WebhookDeliveryLogDO.class);
    }

    @Override
    public List<ApiInvocationDO> listInvocationsByResource(String partnerId, String resourceType, String resourceId, int limit) {
        if (!StringUtils.hasText(partnerId) || !StringUtils.hasText(resourceId) || limit <= 0) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ApiInvocationPO> wrapper = new LambdaQueryWrapper<ApiInvocationPO>()
                .eq(ApiInvocationPO::getPartnerId, partnerId)
                .eq(ApiInvocationPO::getResourceId, resourceId.trim())
                .orderByDesc(ApiInvocationPO::getStartedAt)
                .orderByDesc(ApiInvocationPO::getInvocationId)
                .last("LIMIT " + limit);
        if (StringUtils.hasText(resourceType) && !StringUtils.hasText(resourceId)) {
            wrapper.eq(ApiInvocationPO::getResourceType, resourceType);
        }
        List<ApiInvocationPO> rows = apiInvocationMapper.selectList(wrapper);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return ConvertHelper.convertList(rows, ApiInvocationDO.class);
    }

    @Override
    public PageInfo<WebhookDeliveryLogDO> pageWebhookDeliveries(WebhookDeliveryLogQuery query) {
        LambdaQueryWrapper<WebhookDeliveryLogPO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getPartnerId())) {
            wrapper.eq(WebhookDeliveryLogPO::getPartnerId, query.getPartnerId());
        }
        if (StringUtils.hasText(query.getEventType())) {
            wrapper.eq(WebhookDeliveryLogPO::getEventType, query.getEventType());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(WebhookDeliveryLogPO::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getResourceType())) {
            wrapper.eq(WebhookDeliveryLogPO::getResourceType, query.getResourceType());
        }
        if (StringUtils.hasText(query.getResourceId())) {
            String resourceId = query.getResourceId().trim();
            wrapper.and(w -> w.eq(WebhookDeliveryLogPO::getResourceId, resourceId)
                    .or().like(WebhookDeliveryLogPO::getResourceIdsJson, resourceId));
        }
        wrapper.orderByDesc(WebhookDeliveryLogPO::getCreatedAt)
                .orderByDesc(WebhookDeliveryLogPO::getId);

        Page<WebhookDeliveryLogPO> pageResult = webhookDeliveryLogMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()), wrapper);

        PageInfo<WebhookDeliveryLogDO> pageInfo = new PageInfo<>();
        pageInfo.setCurrent(query.getPage());
        pageInfo.setSize(query.getSize());
        pageInfo.setTotal(pageResult.getTotal());
        pageInfo.setRecords(ConvertHelper.convertList(pageResult.getRecords(), WebhookDeliveryLogDO.class));
        return pageInfo;
    }

    @Override
    public long countByPartnerAndTimeRange(String partnerId, Date from, Date to) {
        Long count = apiInvocationMapper.countByPartnerAndTimeRange(partnerId, from, to);
        return count == null ? 0L : count;
    }

    @Override
    public long countSuccessByPartnerAndTimeRange(String partnerId, Date from, Date to) {
        Long count = apiInvocationMapper.countSuccessByPartnerAndTimeRange(partnerId, from, to);
        return count == null ? 0L : count;
    }

    @Override
    public List<InvocationErrorCodeStat> listTopErrorCodes(String partnerId, Date from, Date to, int limit) {
        List<InvocationErrorCodeStatRow> rows = apiInvocationMapper.selectTopErrorCodes(partnerId, from, to, limit);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream().map(this::toErrorCodeStat).collect(Collectors.toList());
    }

    @Override
    public List<InvocationDailyTrendStat> listDailyStats(String partnerId, Date from, Date to) {
        List<InvocationDailyStatRow> rows = apiInvocationMapper.selectDailyStats(partnerId, from, to);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream().map(this::toDailyStat).collect(Collectors.toList());
    }

    @Override
    public PartnerQuotaStatResult summarizePartnerQuota(String partnerId, Date from, Date to) {
        long total = safeLong(apiInvocationMapper.countByPartnerWithRange(partnerId, from, to));
        long success = safeLong(apiInvocationMapper.countSuccessByPartnerWithRange(partnerId, from, to));
        long failed = Math.max(0L, total - success);

        PartnerQuotaStatResult result = new PartnerQuotaStatResult();
        result.setPartnerId(partnerId);
        result.setTotalInvocations(total);
        result.setSuccessInvocations(success);
        result.setFailedInvocations(failed);
        result.setSuccessRate(calcRate(success, total));
        return result;
    }

    private InvocationErrorCodeStat toErrorCodeStat(InvocationErrorCodeStatRow row) {
        InvocationErrorCodeStat stat = new InvocationErrorCodeStat();
        stat.setResponseCode(row.getResponseCode());
        stat.setCount(row.getCount() == null ? 0L : row.getCount());
        return stat;
    }

    private InvocationDailyTrendStat toDailyStat(InvocationDailyStatRow row) {
        InvocationDailyTrendStat stat = new InvocationDailyTrendStat();
        stat.setStatDate(row.getStatDay().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        stat.setTotalCount(row.getTotalCount() == null ? 0L : row.getTotalCount());
        stat.setSuccessCount(row.getSuccessCount() == null ? 0L : row.getSuccessCount());
        return stat;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private double calcRate(long success, long total) {
        if (total <= 0L) {
            return 0D;
        }
        return ((double) success) / (double) total;
    }

    @Override
    public String findResponseBodyJson(String invocationId) {
        if (!StringUtils.hasText(invocationId)) {
            return null;
        }
        return apiInvocationMapper.selectResponseBodyJson(invocationId.trim());
    }

    @Override
    public long findResponseBodyByteSize(String invocationId) {
        if (!StringUtils.hasText(invocationId)) {
            return 0L;
        }
        Long size = apiInvocationMapper.selectResponseBodyByteSize(invocationId.trim());
        return size == null ? 0L : size;
    }

    @Override
    public String findRequestBodyJson(String invocationId) {
        if (!StringUtils.hasText(invocationId)) {
            return null;
        }
        return apiInvocationMapper.selectRequestBodyJson(invocationId.trim());
    }

    @Override
    public long findRequestBodyByteSize(String invocationId) {
        if (!StringUtils.hasText(invocationId)) {
            return 0L;
        }
        Long size = apiInvocationMapper.selectRequestBodyByteSize(invocationId.trim());
        return size == null ? 0L : size;
    }

}

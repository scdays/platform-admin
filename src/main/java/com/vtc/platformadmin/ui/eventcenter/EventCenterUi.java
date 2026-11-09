package com.vtc.platformadmin.ui.eventcenter;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.core.utils.CurrentUser;
import com.botany.spore.ddd.ui.BaseUI;
import com.vtc.asset.security.platform.eventbus.api.EventBus;
import com.vtc.asset.security.platform.eventbus.api.EventPublishResult;
import com.vtc.asset.security.platform.eventbus.config.EventBusProperties;
import com.vtc.platformadmin.app.service.eventcenter.IEventCenterAppService;
import com.vtc.platformadmin.domain.eventcenter.model.event.GenericEvent;
import com.vtc.platformadmin.ui.dto.ApiResponse;
import com.vtc.platformadmin.ui.eventcenter.dto.EventAuditLogDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventAuditLogQueryParams;
import com.vtc.platformadmin.ui.eventcenter.dto.EventCatalogDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventDeadLetterDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventDeadLetterQueryParams;
import com.vtc.platformadmin.ui.eventcenter.dto.EventDetailVO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventPublishRequest;
import com.vtc.platformadmin.ui.eventcenter.dto.EventSchemaDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventSubscriptionDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventTraceDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventTraceQueryParams;
import com.vtc.platformadmin.ui.eventcenter.dto.ReplayRequestDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * EventCenter 事件中心管理接口。
 * <p>
 * 提供事件发布轨迹、消费轨迹、死信事件的查询能力，以及事件目录/Schema/订阅/重放管理能力。
 *
 * @author asset-security
 */
@RestController
@RequestMapping("/internal/admin/event-center")
public class EventCenterUi extends BaseUI {

    private final IEventCenterAppService eventCenterAppService;
    private final EventBus eventBus;
    private final EventBusProperties eventBusProperties;

    public EventCenterUi(IEventCenterAppService eventCenterAppService, EventBus eventBus,
                         EventBusProperties eventBusProperties) {
        this.eventCenterAppService = eventCenterAppService;
        this.eventBus = eventBus;
        this.eventBusProperties = eventBusProperties;
    }

    /**
     * 分页查询事件发布轨迹。
     */
    @GetMapping("/traces")
    public PageInfo<EventTraceDTO> pageTraces(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String sourceService,
            @RequestParam(required = false) String tenantType,
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        EventTraceQueryParams params = new EventTraceQueryParams();
        params.setEventType(eventType);
        params.setTopic(topic);
        params.setSourceService(sourceService);
        params.setTenantType(tenantType);
        params.setTenantId(tenantId);
        params.setTraceId(traceId);
        params.setStatus(status);
        params.setStartTime(startTime);
        params.setEndTime(endTime);
        params.setPageNum(pageNum);
        params.setPageSize(pageSize);
        return eventCenterAppService.pageTraces(params);
    }

    /**
     * 分页查询死信事件。
     */
    @GetMapping("/dead-letters")
    public PageInfo<EventDeadLetterDTO> pageDeadLetters(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String originTopic,
            @RequestParam(required = false) String consumerGroup,
            @RequestParam(required = false) String failedService,
            @RequestParam(required = false) String replayStatus,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        EventDeadLetterQueryParams params = new EventDeadLetterQueryParams();
        params.setEventType(eventType);
        params.setOriginTopic(originTopic);
        params.setConsumerGroup(consumerGroup);
        params.setFailedService(failedService);
        params.setReplayStatus(replayStatus);
        params.setStartTime(startTime);
        params.setEndTime(endTime);
        params.setPageNum(pageNum);
        params.setPageSize(pageSize);
        return eventCenterAppService.pageDeadLetters(params);
    }

    /**
     * 查询事件详情（含发布轨迹、消费轨迹、死信信息）。
     */
    @GetMapping("/events/{eventId}")
    public EventDetailVO getEventDetail(@PathVariable String eventId) {
        return eventCenterAppService.getEventDetail(eventId);
    }

    /**
     * 查询事件原始报文（payload JSON），供前端「查看报文」弹窗使用。
     * <p>
     * 返回 event_trace 表中存储的完整事件载荷；若未存储则返回 null。
     */
    @GetMapping("/events/{eventId}/payload")
    public ApiResponse<Map<String, Object>> getEventPayload(@PathVariable String eventId) {
        String payload = eventCenterAppService.getEventPayload(eventId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("eventId", eventId);
        body.put("payload", payload);
        return ApiResponse.ok(body);
    }

    /**
     * 查询事件时间线（按时间合并轨迹和消费记录）。
     */
    @GetMapping("/events/{eventId}/timeline")
    public Map<String, Object> getEventTimeline(@PathVariable String eventId) {
        EventDetailVO detail = eventCenterAppService.getEventDetail(eventId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("eventId", eventId);

        if (detail.getTrace() != null) {
            Map<String, Object> publishNode = new LinkedHashMap<>();
            publishNode.put("phase", "published");
            publishNode.put("time", detail.getTrace().getPublishedAt());
            publishNode.put("status", detail.getTrace().getStatus());
            publishNode.put("topic", detail.getTrace().getTopic());
            result.put("publish", publishNode);
        }

        if (detail.getConsumeTraces() != null && !detail.getConsumeTraces().isEmpty()) {
            result.put("consumes", detail.getConsumeTraces().stream().map(ct -> {
                Map<String, Object> node = new LinkedHashMap<>();
                node.put("phase", "consumed");
                node.put("consumerService", ct.getConsumerService());
                node.put("consumerGroup", ct.getConsumerGroup());
                node.put("handler", ct.getHandler());
                node.put("status", ct.getConsumeStatus());
                node.put("startedAt", ct.getStartedAt());
                node.put("finishedAt", ct.getFinishedAt());
                node.put("retryCount", ct.getRetryCount());
                node.put("lastError", ct.getLastError());
                return node;
            }).toArray());
        }

        if (detail.getDeadLetter() != null) {
            Map<String, Object> dlqNode = new LinkedHashMap<>();
            dlqNode.put("phase", "dead_letter");
            dlqNode.put("failedService", detail.getDeadLetter().getFailedService());
            dlqNode.put("failedHandler", detail.getDeadLetter().getFailedHandler());
            dlqNode.put("failedReason", detail.getDeadLetter().getFailedReason());
            dlqNode.put("deadTime", detail.getDeadLetter().getDeadTime());
            dlqNode.put("retryCount", detail.getDeadLetter().getRetryCount());
            dlqNode.put("replayStatus", detail.getDeadLetter().getReplayStatus());
            result.put("deadLetter", dlqNode);
        }

        return result;
    }

    /**
     * 查询事件目录列表。
     */
    @GetMapping("/catalogs")
    public List<EventCatalogDTO> listCatalogs() {
        return eventCenterAppService.listCatalogs();
    }

    /**
     * 查询事件 Schema 列表。
     */
    @GetMapping("/schemas")
    public List<EventSchemaDTO> listSchemas() {
        return eventCenterAppService.listSchemas();
    }

    /**
     * 查询事件订阅列表。
     */
    @GetMapping("/subscriptions")
    public List<EventSubscriptionDTO> listSubscriptions() {
        return eventCenterAppService.listSubscriptions();
    }

    /**
     * 提交事件重放请求。
     */
    @PostMapping("/replay")
    public String requestReplay(@RequestBody ReplayRequestDTO request) {
        String replayId = eventCenterAppService.requestReplay(request);
        // 记录审计日志
        CurrentUser currentUser = CurrentUser.get();
        String operatorId = currentUser.getUid();
        String operatorName = currentUser.getUsername();
        String clientIp = currentUser.getUip();
        if (request != null && request.getEventIds() != null) {
            for (String eventId : request.getEventIds()) {
                eventCenterAppService.recordAudit(
                        operatorId, operatorName, "REPLAY", "DEAD_LETTER", eventId,
                        "SUCCESS", "replayId=" + replayId, clientIp);
            }
        }
        return replayId;
    }

    /**
     * 分页查询事件审计日志。
     */
    @GetMapping("/audit-logs")
    public PageInfo<EventAuditLogDTO> pageAuditLogs(
            @RequestParam(required = false) String operatorId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        EventAuditLogQueryParams params = new EventAuditLogQueryParams();
        params.setOperatorId(operatorId);
        params.setOperationType(operationType);
        params.setTargetType(targetType);
        params.setResult(result);
        params.setStartTime(startTime);
        params.setEndTime(endTime);
        params.setPageNum(pageNum);
        params.setPageSize(pageSize);
        return eventCenterAppService.pageAuditLogs(params);
    }

    /**
     * 事件发布入口（供非 Java 服务通过 HTTP 发布事件）。
     * <p>
     * 将请求体中的通用事件数据包装为 {@link GenericEvent}，
     * 经完整 EventBus 链路（信封 → 编码 → transport）发布到目标 topic。
     *
     * @param request 事件发布请求
     * @return 发布结果（eventId / topic）
     */
    @PostMapping("/publish")
    public ApiResponse<Map<String, Object>> publish(@RequestBody EventPublishRequest request) {
        if (request.getEventType() == null || request.getEventType().isEmpty()) {
            return ApiResponse.of(1, "eventType is required", null);
        }
        if (request.getIdempotentKey() == null || request.getIdempotentKey().isEmpty()) {
            return ApiResponse.of(1, "idempotentKey is required", null);
        }
        if (request.getPayload() == null) {
            return ApiResponse.of(1, "payload is required", null);
        }

        // 自动补充操作人/租户身份（前端未传则从 CurrentUser 提取）
        CurrentUser currentUser = CurrentUser.get();
        String operatorId = request.getOperatorId() != null ? request.getOperatorId() : currentUser.getUid();
        String operatorName = request.getOperatorName() != null ? request.getOperatorName() : currentUser.getUsername();
        String operatorType = request.getOperatorType() != null ? request.getOperatorType() : "admin";
        String tenantType = request.getTenantType() != null ? request.getTenantType()
                : (currentUser.getExtra() != null ? (String) currentUser.getExtra().get("tenantType") : null);
        String tenantId = request.getTenantId() != null ? request.getTenantId()
                : (currentUser.getExtra() != null ? (String) currentUser.getExtra().get("tenantId") : null);

        GenericEvent event = new GenericEvent();
        event.setEventType(request.getEventType());
        event.setIdempotentKey(request.getIdempotentKey());
        event.setPartitionKey(request.getPartitionKey());
        event.setEventVersion(request.getEventVersion());
        event.setPayload(request.getPayload());
        event.setSubjectType(operatorType);
        event.setSubjectId(operatorId);
        event.setTenantType(tenantType);
        event.setTenantId(tenantId);

        String topic = request.getTopic() != null && !request.getTopic().isEmpty()
                ? request.getTopic() : eventBusProperties.getDefaultTopic();

        EventPublishResult result = eventBus.publish(topic, event);

        // 记录审计日志
        eventCenterAppService.recordAudit(
                operatorId, operatorName, "PUBLISH", "EVENT", result.getEventId(),
                "SUCCESS", "topic=" + topic, currentUser.getUip());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("eventId", result.getEventId());
        body.put("topic", result.getTopic());
        body.put("messageId", result.getMessageId());
        return ApiResponse.ok(body);
    }
}
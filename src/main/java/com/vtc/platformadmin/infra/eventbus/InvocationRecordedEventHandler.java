package com.vtc.platformadmin.infra.eventbus;

import com.vtc.asset.security.platform.eventbus.annotation.EventListener;
import com.vtc.asset.security.platform.eventbus.api.EventContext;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventConsumeTraceDO;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventTraceDO;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventConsumeTraceRepository;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventTraceRepository;
import com.vtc.platformadmin.domain.invocation.model.entity.ApiInvocationDO;
import com.vtc.platformadmin.domain.invocation.model.event.InvocationRecordedEvent;
import com.vtc.platformadmin.domain.invocation.repository.IApiInvocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 调用记录事件消费者。
 * <p>
 * 消费 partner-gateway 发布的 InvocationRecordedEvent，
 * 按 invocationId 幂等写入 event_trace、event_consume_trace 和 api_invocation。
 *
 * @author asset-security
 */
@Component
public class InvocationRecordedEventHandler {

    private static final Logger log = LoggerFactory.getLogger(InvocationRecordedEventHandler.class);

    private final IEventTraceRepository eventTraceRepository;
    private final IEventConsumeTraceRepository eventConsumeTraceRepository;
    private final IApiInvocationRepository apiInvocationRepository;

    public InvocationRecordedEventHandler(IEventTraceRepository eventTraceRepository,
                                          IEventConsumeTraceRepository eventConsumeTraceRepository,
                                          IApiInvocationRepository apiInvocationRepository) {
        this.eventTraceRepository = eventTraceRepository;
        this.eventConsumeTraceRepository = eventConsumeTraceRepository;
        this.apiInvocationRepository = apiInvocationRepository;
    }

    @EventListener(
            topic = "open-platform-invocation",
            eventType = "open-platform.invocation.recorded",
            group = "platform-admin-invocation"
    )
    public void handle(InvocationRecordedEvent event, EventContext context) {
        Date now = new Date();

        // 1. 写 event_trace（如不存在）
        try {
            EventTraceDO existingTrace = eventTraceRepository.findByEventId(context.getEnvelope().getEventId());
            if (existingTrace == null) {
                EventTraceDO trace = buildTrace(context, now);
                eventTraceRepository.save(trace);
            }
        } catch (Exception e) {
            log.warn("write event_trace failed, eventId={}", context.getEnvelope().getEventId(), e);
        }

        // 2. 按 invocationId 幂等写入 api_invocation（通用 save/getById 由仓储基类提供）
        try {
            if (apiInvocationRepository.getById(event.getInvocationId()) == null) {
                ApiInvocationDO invocation = buildInvocation(event, now);
                apiInvocationRepository.save(invocation);
                log.info("invocation recorded, invocationId={}, partnerId={}, apiCode={}",
                        event.getInvocationId(), event.getPartnerId(), event.getApiCode());
            }
        } catch (Exception e) {
            log.error("write api_invocation failed, invocationId={}", event.getInvocationId(), e);
            consumeTrace(context, now, "FAILED", e.getMessage());
            throw e;
        }

        // 3. 写 event_consume_trace SUCCESS
        consumeTrace(context, now, "SUCCESS", null);
    }

    private EventTraceDO buildTrace(EventContext context, Date now) {
        EventTraceDO trace = new EventTraceDO();
        trace.setEventId(context.getEnvelope().getEventId());
        trace.setEventType(context.getEnvelope().getEventType());
        trace.setEventVersion(context.getEnvelope().getEventVersion());
        trace.setTopic(context.getTopic());
        trace.setDomain(context.getEnvelope().getDomain());
        trace.setSourceService(context.getEnvelope().getSourceService());
        trace.setTenantType(context.getEnvelope().getTenantType());
        trace.setTenantId(context.getEnvelope().getTenantId());
        trace.setSubjectType(context.getEnvelope().getSubjectType());
        trace.setSubjectId(context.getEnvelope().getSubjectId());
        trace.setTraceId(context.getEnvelope().getTraceId());
        trace.setIdempotentKey(context.getEnvelope().getIdempotentKey());
        trace.setOccurredAt(context.getEnvelope().getOccurredAt() != null
                ? new Date(context.getEnvelope().getOccurredAt()) : now);
        trace.setPublishedAt(now);
        trace.setStatus("RECEIVED");
        return trace;
    }

    private ApiInvocationDO buildInvocation(InvocationRecordedEvent event, Date now) {
        ApiInvocationDO invocation = new ApiInvocationDO();
        invocation.setInvocationId(event.getInvocationId());
        invocation.setRequestId(event.getRequestId());
        invocation.setPartnerId(event.getPartnerId());
        invocation.setOperationId(event.getApiCode());
        invocation.setHttpMethod(event.getMethod());
        invocation.setRequestPath(event.getPath());
        // response_code 为业务码（0=成功），http_status 为 HTTP 状态码，二者语义独立
        invocation.setResponseCode(event.getStatusCode());
        invocation.setHttpStatus(event.getHttpStatus());
        invocation.setLatencyMs(event.getCostMillis() != null ? event.getCostMillis().intValue() : null);
        invocation.setClientIp(event.getClientIp());
        invocation.setErrorMessage(event.getErrorMessage());
        invocation.setStartedAt(event.getStartTime() != null ? new Date(event.getStartTime()) : null);
        invocation.setFinishedAt(event.getEndTime() != null ? new Date(event.getEndTime()) : null);
        return invocation;
    }

    private void consumeTrace(EventContext context, Date now, String status, String error) {
        try {
            EventConsumeTraceDO trace = new EventConsumeTraceDO();
            trace.setEventId(context.getEnvelope().getEventId());
            trace.setEventType(context.getEnvelope().getEventType());
            trace.setTopic(context.getTopic());
            trace.setConsumerService("platform-admin");
            trace.setConsumerGroup(context.getConsumerGroup());
            trace.setHandler("InvocationRecordedEventHandler");
            trace.setConsumeStatus(status);
            trace.setRetryCount(context.getRetryCount() != null ? context.getRetryCount() : 0);
            trace.setLastError(error);
            trace.setStartedAt(now);
            trace.setFinishedAt(new Date());
            eventConsumeTraceRepository.save(trace);
        } catch (Exception e) {
            log.warn("write consume_trace failed, eventId={}", context.getEnvelope().getEventId(), e);
        }
    }
}
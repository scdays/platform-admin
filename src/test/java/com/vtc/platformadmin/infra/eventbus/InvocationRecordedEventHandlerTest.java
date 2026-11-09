package com.vtc.platformadmin.infra.eventbus;

import com.vtc.asset.security.platform.eventbus.api.EventContext;
import com.vtc.asset.security.platform.eventbus.api.EventEnvelope;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventConsumeTraceDO;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventTraceDO;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventConsumeTraceRepository;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventTraceRepository;
import com.vtc.platformadmin.domain.invocation.model.entity.ApiInvocationDO;
import com.vtc.platformadmin.domain.invocation.model.event.InvocationRecordedEvent;
import com.vtc.platformadmin.domain.invocation.repository.IApiInvocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link InvocationRecordedEventHandler}.
 *
 * @author asset-security
 */
@ExtendWith(MockitoExtension.class)
class InvocationRecordedEventHandlerTest {

    @Mock
    private IEventTraceRepository eventTraceRepository;

    @Mock
    private IEventConsumeTraceRepository eventConsumeTraceRepository;

    @Mock
    private IApiInvocationRepository apiInvocationRepository;

    @InjectMocks
    private InvocationRecordedEventHandler handler;

    private EventContext createEventContext() {
        EventEnvelope<InvocationRecordedEvent> envelope = new EventEnvelope<>();
        envelope.setEventId("test-event-id");
        envelope.setEventType("open-platform.invocation.recorded");
        envelope.setTopic("open-platform-invocation");
        envelope.setSourceService("partner-gateway");
        envelope.setTraceId("test-trace-id");
        envelope.setIdempotentKey("invocation:test-invocation-id");

        EventContext context = new EventContext();
        context.setEnvelope(envelope);
        context.setTopic("open-platform-invocation");
        context.setConsumerGroup("platform-admin-invocation");
        context.setRetryCount(0);
        return context;
    }

    private InvocationRecordedEvent createEvent() {
        InvocationRecordedEvent event = new InvocationRecordedEvent();
        event.setInvocationId("test-invocation-id");
        event.setRequestId("test-request-id");
        event.setPartnerId("test-partner-id");
        event.setApiCode("test-api-code");
        event.setMethod("POST");
        event.setPath("/api/test");
        event.setStatusCode(200);
        event.setCostMillis(100L);
        event.setStartTime(System.currentTimeMillis() - 100);
        event.setEndTime(System.currentTimeMillis());
        return event;
    }

    @Test
    void testHandle_Success() {
        // given
        InvocationRecordedEvent event = createEvent();
        EventContext context = createEventContext();

        // 模拟 eventTraceRepository.findByEventId 返回 null（新事件）
        when(eventTraceRepository.findByEventId("test-event-id")).thenReturn(null);
        // 模拟 apiInvocationRepository.getById 返回 null（新记录）
        when(apiInvocationRepository.getById("test-invocation-id")).thenReturn(null);

        // when
        handler.handle(event, context);

        // then: 验证 eventTraceRepository.insert 被调用
        ArgumentCaptor<EventTraceDO> traceCaptor = ArgumentCaptor.forClass(EventTraceDO.class);
        verify(eventTraceRepository).save(traceCaptor.capture());
        EventTraceDO insertedTrace = traceCaptor.getValue();
        assertEquals("test-event-id", insertedTrace.getEventId());
        assertEquals("open-platform.invocation.recorded", insertedTrace.getEventType());
        assertEquals("RECEIVED", insertedTrace.getStatus());

        // then: 验证 apiInvocationRepository.insert 被调用
        ArgumentCaptor<ApiInvocationDO> invocationCaptor = ArgumentCaptor.forClass(ApiInvocationDO.class);
        verify(apiInvocationRepository).save(invocationCaptor.capture());
        ApiInvocationDO insertedInvocation = invocationCaptor.getValue();
        assertEquals("test-invocation-id", insertedInvocation.getInvocationId());
        assertEquals("test-partner-id", insertedInvocation.getPartnerId());
        assertEquals("test-api-code", insertedInvocation.getOperationId());

        // then: 验证 eventConsumeTraceRepository.insert 被调用（SUCCESS）
        ArgumentCaptor<EventConsumeTraceDO> consumeTraceCaptor = ArgumentCaptor.forClass(EventConsumeTraceDO.class);
        verify(eventConsumeTraceRepository).save(consumeTraceCaptor.capture());
        EventConsumeTraceDO insertedConsumeTrace = consumeTraceCaptor.getValue();
        assertEquals("test-event-id", insertedConsumeTrace.getEventId());
        assertEquals("SUCCESS", insertedConsumeTrace.getConsumeStatus());
        assertEquals("InvocationRecordedEventHandler", insertedConsumeTrace.getHandler());
        assertEquals("platform-admin", insertedConsumeTrace.getConsumerService());
        assertEquals("platform-admin-invocation", insertedConsumeTrace.getConsumerGroup());
        assertEquals(0, insertedConsumeTrace.getRetryCount());
        assertNull(insertedConsumeTrace.getLastError());
    }

    @Test
    void testHandle_DuplicateInvocation() {
        // given
        InvocationRecordedEvent event = createEvent();
        EventContext context = createEventContext();

        // 模拟 eventTraceRepository.findByEventId 返回 null（新事件）
        when(eventTraceRepository.findByEventId("test-event-id")).thenReturn(null);
        // 模拟 apiInvocationRepository.getById 返回已有 DO（已存在记录）
        ApiInvocationDO existingInvocation = new ApiInvocationDO();
        existingInvocation.setInvocationId("test-invocation-id");
        when(apiInvocationRepository.getById("test-invocation-id")).thenReturn(existingInvocation);

        // when
        handler.handle(event, context);

        // then: 验证 eventTraceRepository.insert 仍然被调用
        verify(eventTraceRepository).save(any(EventTraceDO.class));

        // then: 验证 apiInvocationRepository.insert 未被调用（已存在记录）
        verify(apiInvocationRepository, never()).save(any(ApiInvocationDO.class));

        // then: 验证 eventConsumeTraceRepository.insert 仍然被调用（SUCCESS）
        ArgumentCaptor<EventConsumeTraceDO> consumeTraceCaptor = ArgumentCaptor.forClass(EventConsumeTraceDO.class);
        verify(eventConsumeTraceRepository).save(consumeTraceCaptor.capture());
        assertEquals("SUCCESS", consumeTraceCaptor.getValue().getConsumeStatus());
    }

    @Test
    void testHandle_ExistingEventTrace() {
        // given
        InvocationRecordedEvent event = createEvent();
        EventContext context = createEventContext();

        // 模拟 eventTraceRepository.findByEventId 返回已有 trace（事件已存在）
        EventTraceDO existingTrace = new EventTraceDO();
        existingTrace.setEventId("test-event-id");
        when(eventTraceRepository.findByEventId("test-event-id")).thenReturn(existingTrace);
        // 模拟 apiInvocationRepository.getById 返回 null
        when(apiInvocationRepository.getById("test-invocation-id")).thenReturn(null);

        // when
        handler.handle(event, context);

        // then: 验证 eventTraceRepository.insert 未被调用（事件 trace 已存在）
        verify(eventTraceRepository, never()).save(any(EventTraceDO.class));

        // then: apiInvocationRepository.insert 仍然被调用
        verify(apiInvocationRepository).save(any(ApiInvocationDO.class));

        // then: consume trace 仍然写入
        verify(eventConsumeTraceRepository).save(any(EventConsumeTraceDO.class));
    }
}
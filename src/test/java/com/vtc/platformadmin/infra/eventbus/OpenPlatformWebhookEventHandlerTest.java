package com.vtc.platformadmin.infra.eventbus;

import com.vtc.asset.security.platform.eventbus.api.EventContext;
import com.vtc.asset.security.platform.eventbus.api.EventEnvelope;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventConsumeTraceDO;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventTraceDO;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventConsumeTraceRepository;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventTraceRepository;
import com.vtc.platformadmin.domain.webhook.model.event.OpenPlatformWebhookEvent;
import com.vtc.platformadmin.domain.webhook.service.business.IWebhookDomainService;
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
 * Unit tests for {@link OpenPlatformWebhookEventHandler}.
 *
 * @author asset-security
 */
@ExtendWith(MockitoExtension.class)
class OpenPlatformWebhookEventHandlerTest {

    @Mock
    private IEventTraceRepository eventTraceRepository;

    @Mock
    private IEventConsumeTraceRepository eventConsumeTraceRepository;

    @Mock
    private IWebhookDomainService webhookDomainService;

    @InjectMocks
    private OpenPlatformWebhookEventHandler handler;

    private EventContext createEventContext() {
        EventEnvelope<OpenPlatformWebhookEvent> envelope = new EventEnvelope<>();
        envelope.setEventId("test-webhook-event-id");
        envelope.setEventType("open-platform.webhook.event");
        envelope.setTopic("open-platform-webhook");
        envelope.setSourceService("vul-pass");
        envelope.setTraceId("test-trace-id");
        envelope.setIdempotentKey("webhook:test.event:VulReport:res-001");

        EventContext context = new EventContext();
        context.setEnvelope(envelope);
        context.setTopic("open-platform-webhook");
        context.setConsumerGroup("platform-admin-webhook-dispatcher");
        context.setRetryCount(0);
        return context;
    }

    private OpenPlatformWebhookEvent createEvent() {
        OpenPlatformWebhookEvent event = new OpenPlatformWebhookEvent();
        event.setEventId("test-webhook-event-id");
        event.setPartnerId("test-partner-id");
        event.setEventName("test.event");
        event.setResourceType("VulReport");
        event.setResourceId("res-001");
        event.setTraceId("test-trace-id");
        event.setOccurredAt(System.currentTimeMillis());
        return event;
    }

    @Test
    void testHandle_Success() {
        // given
        OpenPlatformWebhookEvent event = createEvent();
        EventContext context = createEventContext();

        // 模拟 eventTraceRepository.findByEventId 返回 null（新事件）
        when(eventTraceRepository.findByEventId("test-webhook-event-id")).thenReturn(null);
        // 模拟 webhookDomainService.deliver 正常执行
        doNothing().when(webhookDomainService).deliver(event);

        // when
        handler.handle(event, context);

        // then: 验证 eventTraceRepository.insert 被调用
        ArgumentCaptor<EventTraceDO> traceCaptor = ArgumentCaptor.forClass(EventTraceDO.class);
        verify(eventTraceRepository).save(traceCaptor.capture());
        EventTraceDO insertedTrace = traceCaptor.getValue();
        assertEquals("test-webhook-event-id", insertedTrace.getEventId());
        assertEquals("open-platform.webhook.event", insertedTrace.getEventType());
        assertEquals("RECEIVED", insertedTrace.getStatus());

        // then: 验证 webhookDomainService.deliver 被调用
        verify(webhookDomainService).deliver(event);

        // then: 验证 eventConsumeTraceRepository.insert 被调用（SUCCESS）
        ArgumentCaptor<EventConsumeTraceDO> consumeTraceCaptor = ArgumentCaptor.forClass(EventConsumeTraceDO.class);
        verify(eventConsumeTraceRepository).save(consumeTraceCaptor.capture());
        EventConsumeTraceDO insertedConsumeTrace = consumeTraceCaptor.getValue();
        assertEquals("test-webhook-event-id", insertedConsumeTrace.getEventId());
        assertEquals("SUCCESS", insertedConsumeTrace.getConsumeStatus());
        assertEquals("OpenPlatformWebhookEventHandler", insertedConsumeTrace.getHandler());
        assertEquals("platform-admin", insertedConsumeTrace.getConsumerService());
        assertEquals("platform-admin-webhook-dispatcher", insertedConsumeTrace.getConsumerGroup());
        assertEquals(0, insertedConsumeTrace.getRetryCount());
        assertNull(insertedConsumeTrace.getLastError());
    }

    @Test
    void testHandle_ExistingEventTrace() {
        // given
        OpenPlatformWebhookEvent event = createEvent();
        EventContext context = createEventContext();

        // 模拟 eventTraceRepository.findByEventId 返回已有 trace
        EventTraceDO existingTrace = new EventTraceDO();
        existingTrace.setEventId("test-webhook-event-id");
        when(eventTraceRepository.findByEventId("test-webhook-event-id")).thenReturn(existingTrace);
        doNothing().when(webhookDomainService).deliver(event);

        // when
        handler.handle(event, context);

        // then: 验证 eventTraceRepository.insert 未被调用（事件 trace 已存在）
        verify(eventTraceRepository, never()).save(any(EventTraceDO.class));

        // then: webhookDomainService.deliver 仍然被调用
        verify(webhookDomainService).deliver(event);

        // then: consume trace 仍然写入
        verify(eventConsumeTraceRepository).save(any(EventConsumeTraceDO.class));
    }

    @Test
    void testHandle_DeliverFails() {
        // given
        OpenPlatformWebhookEvent event = createEvent();
        EventContext context = createEventContext();

        // 模拟 eventTraceRepository.findByEventId 返回 null
        when(eventTraceRepository.findByEventId("test-webhook-event-id")).thenReturn(null);
        // 模拟 webhookDomainService.deliver 抛出异常
        doThrow(new RuntimeException("delivery failed")).when(webhookDomainService).deliver(event);

        // when / then: 期望抛出异常
        assertThrows(RuntimeException.class, () -> handler.handle(event, context));

        // then: eventTraceRepository.insert 仍然被调用
        verify(eventTraceRepository).save(any(EventTraceDO.class));

        // then: 验证 consume trace 写入 FAILED 状态
        ArgumentCaptor<EventConsumeTraceDO> consumeTraceCaptor = ArgumentCaptor.forClass(EventConsumeTraceDO.class);
        verify(eventConsumeTraceRepository).save(consumeTraceCaptor.capture());
        EventConsumeTraceDO insertedConsumeTrace = consumeTraceCaptor.getValue();
        assertEquals("FAILED", insertedConsumeTrace.getConsumeStatus());
        assertEquals("delivery failed", insertedConsumeTrace.getLastError());
    }
}
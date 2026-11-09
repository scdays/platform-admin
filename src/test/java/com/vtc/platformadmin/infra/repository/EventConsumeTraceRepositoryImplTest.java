package com.vtc.platformadmin.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.botany.spore.ddd.infra.utils.convertor.ConvertHelper;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventConsumeTraceDO;
import com.vtc.platformadmin.infra.dao.EventConsumeTraceMapper;
import com.vtc.platformadmin.infra.dao.po.EventConsumeTracePO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EventConsumeTraceRepositoryImpl}.
 *
 * @author asset-security
 */
@ExtendWith(MockitoExtension.class)
class EventConsumeTraceRepositoryImplTest {

    @Mock
    private EventConsumeTraceMapper eventConsumeTraceMapper;

    @InjectMocks
    private EventConsumeTraceRepositoryImpl repository;

    private MockedStatic<ConvertHelper> convertHelperMock;
    private MockedConstruction<LambdaQueryWrapper> lambdaQueryWrapperMock;

    @BeforeEach
    void setUp() {
        convertHelperMock = mockStatic(ConvertHelper.class);
        lambdaQueryWrapperMock = mockConstruction(
                LambdaQueryWrapper.class,
                withSettings().defaultAnswer(invocation -> {
                    Object mock = invocation.getMock();
                    Class<?> returnType = invocation.getMethod().getReturnType();
                    if (returnType.isInstance(mock)) {
                        return mock;
                    }
                    return RETURNS_DEFAULTS.answer(invocation);
                }));
    }

    @AfterEach
    void tearDown() {
        convertHelperMock.close();
        lambdaQueryWrapperMock.close();
    }

    @Test
    void testFindByEventId() {
        // given
        String eventId = "event-001";
        EventConsumeTracePO po = new EventConsumeTracePO();
        po.setEventId(eventId);
        po.setEventType("test.type");
        po.setTopic("test-topic");
        po.setConsumerService("consumer-svc");
        po.setConsumeStatus("SUCCESS");
        List<EventConsumeTracePO> poList = Collections.singletonList(po);

        EventConsumeTraceDO expectedDO = new EventConsumeTraceDO();
        expectedDO.setEventId(eventId);
        expectedDO.setEventType("test.type");
        expectedDO.setTopic("test-topic");
        expectedDO.setConsumerService("consumer-svc");
        expectedDO.setConsumeStatus("SUCCESS");
        List<EventConsumeTraceDO> doList = Collections.singletonList(expectedDO);

        when(eventConsumeTraceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(poList);
        convertHelperMock.when(() -> ConvertHelper.convertList(poList, EventConsumeTraceDO.class))
                .thenReturn(doList);

        // when
        List<EventConsumeTraceDO> result = repository.findByEventId(eventId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(eventId, result.get(0).getEventId());
        assertEquals("test.type", result.get(0).getEventType());
        assertEquals("consumer-svc", result.get(0).getConsumerService());
        assertEquals("SUCCESS", result.get(0).getConsumeStatus());
        verify(eventConsumeTraceMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testFindByEventId_NotFound() {
        // given
        String eventId = "event-999";
        when(eventConsumeTraceMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());
        convertHelperMock.when(() -> ConvertHelper.convertList(anyList(), eq(EventConsumeTraceDO.class)))
                .thenReturn(Collections.emptyList());

        // when
        List<EventConsumeTraceDO> result = repository.findByEventId(eventId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(eventConsumeTraceMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testFindByEventId_NullInput() {
        // when
        List<EventConsumeTraceDO> result = repository.findByEventId(null);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(eventConsumeTraceMapper, never()).selectList(any());
    }

    @Test
    void testFindByEventId_EmptyInput() {
        // when
        List<EventConsumeTraceDO> result = repository.findByEventId("");

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(eventConsumeTraceMapper, never()).selectList(any());
    }

    @Test
    void testFindByEventId_MultipleResults() {
        // given
        String eventId = "event-001";

        EventConsumeTracePO po1 = new EventConsumeTracePO();
        po1.setEventId(eventId);
        po1.setConsumerService("consumer-svc-1");
        po1.setConsumeStatus("SUCCESS");

        EventConsumeTracePO po2 = new EventConsumeTracePO();
        po2.setEventId(eventId);
        po2.setConsumerService("consumer-svc-2");
        po2.setConsumeStatus("FAILED");

        List<EventConsumeTracePO> poList = Arrays.asList(po1, po2);

        EventConsumeTraceDO do1 = new EventConsumeTraceDO();
        do1.setEventId(eventId);
        do1.setConsumerService("consumer-svc-1");
        do1.setConsumeStatus("SUCCESS");

        EventConsumeTraceDO do2 = new EventConsumeTraceDO();
        do2.setEventId(eventId);
        do2.setConsumerService("consumer-svc-2");
        do2.setConsumeStatus("FAILED");

        List<EventConsumeTraceDO> doList = Arrays.asList(do1, do2);

        when(eventConsumeTraceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(poList);
        convertHelperMock.when(() -> ConvertHelper.convertList(poList, EventConsumeTraceDO.class))
                .thenReturn(doList);

        // when
        List<EventConsumeTraceDO> result = repository.findByEventId(eventId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("SUCCESS", result.get(0).getConsumeStatus());
        assertEquals("FAILED", result.get(1).getConsumeStatus());
        verify(eventConsumeTraceMapper).selectList(any(LambdaQueryWrapper.class));
    }
}
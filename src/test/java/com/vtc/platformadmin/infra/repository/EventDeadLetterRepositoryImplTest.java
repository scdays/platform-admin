package com.vtc.platformadmin.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.infra.utils.convertor.ConvertHelper;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventDeadLetterDO;
import com.vtc.platformadmin.infra.dao.EventDeadLetterMapper;
import com.vtc.platformadmin.infra.dao.po.EventDeadLetterPO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventDeadLetterQueryParams;
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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EventDeadLetterRepositoryImpl}.
 *
 * @author asset-security
 */
@ExtendWith(MockitoExtension.class)
class EventDeadLetterRepositoryImplTest {

    @Mock
    private EventDeadLetterMapper eventDeadLetterMapper;

    @InjectMocks
    private EventDeadLetterRepositoryImpl repository;

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
    void testFindByEventId_Found() {
        // given
        String eventId = "event-001";
        EventDeadLetterPO po = new EventDeadLetterPO();
        po.setEventId(eventId);
        po.setEventType("test.type");
        po.setOriginTopic("test-topic");
        po.setFailedService("failed-svc");
        po.setFailedReason("Connection timeout");
        po.setReplayStatus("PENDING");

        EventDeadLetterDO expectedDO = new EventDeadLetterDO();
        expectedDO.setEventId(eventId);
        expectedDO.setEventType("test.type");
        expectedDO.setOriginTopic("test-topic");
        expectedDO.setFailedService("failed-svc");
        expectedDO.setFailedReason("Connection timeout");
        expectedDO.setReplayStatus("PENDING");

        when(eventDeadLetterMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(po);
        convertHelperMock.when(() -> ConvertHelper.convert(po, EventDeadLetterDO.class))
                .thenReturn(expectedDO);

        // when
        EventDeadLetterDO result = repository.findByEventId(eventId);

        // then
        assertNotNull(result);
        assertEquals(eventId, result.getEventId());
        assertEquals("test.type", result.getEventType());
        assertEquals("test-topic", result.getOriginTopic());
        assertEquals("failed-svc", result.getFailedService());
        assertEquals("Connection timeout", result.getFailedReason());
        assertEquals("PENDING", result.getReplayStatus());
        verify(eventDeadLetterMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void testFindByEventId_NotFound() {
        // given
        String eventId = "event-999";
        when(eventDeadLetterMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // when
        EventDeadLetterDO result = repository.findByEventId(eventId);

        // then
        assertNull(result);
        verify(eventDeadLetterMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void testFindByEventId_NullInput() {
        // when
        EventDeadLetterDO result = repository.findByEventId(null);

        // then
        assertNull(result);
        verify(eventDeadLetterMapper, never()).selectOne(any());
    }

    @Test
    void testPageDeadLetters() {
        // given
        EventDeadLetterQueryParams params = new EventDeadLetterQueryParams();
        params.setEventType("test.type");
        params.setOriginTopic("test-topic");
        params.setConsumerGroup("consumer-group");
        params.setFailedService("failed-svc");
        params.setReplayStatus("PENDING");
        params.setStartTime(new Date(100000L));
        params.setEndTime(new Date(200000L));
        params.setPageNum(1);
        params.setPageSize(10);

        EventDeadLetterPO po = new EventDeadLetterPO();
        po.setEventId("event-001");
        po.setEventType("test.type");
        po.setOriginTopic("test-topic");
        po.setFailedService("failed-svc");
        po.setReplayStatus("PENDING");
        List<EventDeadLetterPO> poList = Collections.singletonList(po);

        EventDeadLetterDO expectedDO = new EventDeadLetterDO();
        expectedDO.setEventId("event-001");
        expectedDO.setEventType("test.type");
        expectedDO.setOriginTopic("test-topic");
        expectedDO.setFailedService("failed-svc");
        expectedDO.setReplayStatus("PENDING");
        List<EventDeadLetterDO> doList = Collections.singletonList(expectedDO);

        Page<EventDeadLetterPO> page = new Page<>(1, 10);
        page.setRecords(poList);
        page.setCurrent(1);
        page.setSize(10);
        page.setTotal(1);

        when(eventDeadLetterMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);
        convertHelperMock.when(() -> ConvertHelper.convertList(poList, EventDeadLetterDO.class))
                .thenReturn(doList);

        // when
        PageInfo<EventDeadLetterDO> result = repository.pageDeadLetters(params);

        // then
        assertNotNull(result);
        assertEquals(1, result.getCurrent());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("event-001", result.getRecords().get(0).getEventId());
        assertEquals("test.type", result.getRecords().get(0).getEventType());
        assertEquals("test-topic", result.getRecords().get(0).getOriginTopic());
        assertEquals("failed-svc", result.getRecords().get(0).getFailedService());
        assertEquals("PENDING", result.getRecords().get(0).getReplayStatus());
        verify(eventDeadLetterMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void testPageDeadLetters_DefaultPagination() {
        // given
        EventDeadLetterQueryParams params = new EventDeadLetterQueryParams();
        // pageNum and pageSize are null, should default to 1 and 20

        Page<EventDeadLetterPO> page = new Page<>(1, 20);
        page.setRecords(Collections.emptyList());
        page.setCurrent(1);
        page.setSize(20);
        page.setTotal(0);

        when(eventDeadLetterMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);
        convertHelperMock.when(() -> ConvertHelper.convertList(anyList(), eq(EventDeadLetterDO.class)))
                .thenReturn(Collections.emptyList());

        // when
        PageInfo<EventDeadLetterDO> result = repository.pageDeadLetters(params);

        // then
        assertNotNull(result);
        assertEquals(1, result.getCurrent());
        assertEquals(20, result.getSize());
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        verify(eventDeadLetterMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }
}
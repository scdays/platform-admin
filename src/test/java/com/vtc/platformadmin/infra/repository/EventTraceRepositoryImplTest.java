package com.vtc.platformadmin.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.infra.utils.convertor.ConvertHelper;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventTraceDO;
import com.vtc.platformadmin.infra.dao.EventTraceMapper;
import com.vtc.platformadmin.infra.dao.po.EventTracePO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventTraceQueryParams;
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
 * Unit tests for {@link EventTraceRepositoryImpl}.
 *
 * @author asset-security
 */
@ExtendWith(MockitoExtension.class)
class EventTraceRepositoryImplTest {

    @Mock
    private EventTraceMapper eventTraceMapper;

    @InjectMocks
    private EventTraceRepositoryImpl repository;

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
        EventTracePO po = new EventTracePO();
        po.setEventId(eventId);
        po.setEventType("test.type");
        po.setTopic("test-topic");

        EventTraceDO expectedDO = new EventTraceDO();
        expectedDO.setEventId(eventId);
        expectedDO.setEventType("test.type");
        expectedDO.setTopic("test-topic");

        when(eventTraceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(po);
        convertHelperMock.when(() -> ConvertHelper.convert(po, EventTraceDO.class))
                .thenReturn(expectedDO);

        // when
        EventTraceDO result = repository.findByEventId(eventId);

        // then
        assertNotNull(result);
        assertEquals(eventId, result.getEventId());
        assertEquals("test.type", result.getEventType());
        assertEquals("test-topic", result.getTopic());
        verify(eventTraceMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void testFindByEventId_NotFound() {
        // given
        String eventId = "event-999";
        when(eventTraceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // when
        EventTraceDO result = repository.findByEventId(eventId);

        // then
        assertNull(result);
        verify(eventTraceMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void testFindByEventId_NullInput() {
        // when
        EventTraceDO result = repository.findByEventId(null);

        // then
        assertNull(result);
        verify(eventTraceMapper, never()).selectOne(any());
    }

    @Test
    void testFindByTraceId() {
        // given
        String traceId = "trace-001";
        EventTracePO po = new EventTracePO();
        po.setEventId("event-001");
        po.setTraceId(traceId);
        List<EventTracePO> poList = Collections.singletonList(po);

        EventTraceDO expectedDO = new EventTraceDO();
        expectedDO.setEventId("event-001");
        expectedDO.setTraceId(traceId);
        List<EventTraceDO> doList = Collections.singletonList(expectedDO);

        when(eventTraceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(poList);
        convertHelperMock.when(() -> ConvertHelper.convertList(poList, EventTraceDO.class))
                .thenReturn(doList);

        // when
        List<EventTraceDO> result = repository.findByTraceId(traceId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("event-001", result.get(0).getEventId());
        assertEquals(traceId, result.get(0).getTraceId());
        verify(eventTraceMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testPageTraces() {
        // given
        EventTraceQueryParams params = new EventTraceQueryParams();
        params.setEventType("test.type");
        params.setTopic("test-topic");
        params.setSourceService("test-service");
        params.setTenantType("tenant-type");
        params.setTenantId("tenant-001");
        params.setTraceId("trace-001");
        params.setStatus("PUBLISHED");
        params.setStartTime(new Date(100000L));
        params.setEndTime(new Date(200000L));
        params.setPageNum(1);
        params.setPageSize(10);

        EventTracePO po = new EventTracePO();
        po.setEventId("event-001");
        po.setEventType("test.type");
        po.setTopic("test-topic");
        List<EventTracePO> poList = Collections.singletonList(po);

        EventTraceDO expectedDO = new EventTraceDO();
        expectedDO.setEventId("event-001");
        expectedDO.setEventType("test.type");
        expectedDO.setTopic("test-topic");
        List<EventTraceDO> doList = Collections.singletonList(expectedDO);

        Page<EventTracePO> page = new Page<>(1, 10);
        page.setRecords(poList);
        page.setCurrent(1);
        page.setSize(10);
        page.setTotal(1);

        when(eventTraceMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);
        convertHelperMock.when(() -> ConvertHelper.convertList(poList, EventTraceDO.class))
                .thenReturn(doList);

        // when
        PageInfo<EventTraceDO> result = repository.pageTraces(params);

        // then
        assertNotNull(result);
        assertEquals(1, result.getCurrent());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("event-001", result.getRecords().get(0).getEventId());
        assertEquals("test.type", result.getRecords().get(0).getEventType());
        assertEquals("test-topic", result.getRecords().get(0).getTopic());
        verify(eventTraceMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void testPageTraces_DefaultPagination() {
        // given
        EventTraceQueryParams params = new EventTraceQueryParams();
        // pageNum and pageSize are null, should default to 1 and 20

        Page<EventTracePO> page = new Page<>(1, 20);
        page.setRecords(Collections.emptyList());
        page.setCurrent(1);
        page.setSize(20);
        page.setTotal(0);

        when(eventTraceMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);
        convertHelperMock.when(() -> ConvertHelper.convertList(anyList(), eq(EventTraceDO.class)))
                .thenReturn(Collections.emptyList());

        // when
        PageInfo<EventTraceDO> result = repository.pageTraces(params);

        // then
        assertNotNull(result);
        assertEquals(1, result.getCurrent());
        assertEquals(20, result.getSize());
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        verify(eventTraceMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }
}
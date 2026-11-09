package com.vtc.platformadmin.infra.repository;

import com.botany.spore.ddd.infra.utils.convertor.ConvertHelper;
import com.vtc.platformadmin.domain.webhook.model.entity.WebhookDeliveryLogDO;
import com.vtc.platformadmin.infra.dao.WebhookDeliveryLogMapper;
import com.vtc.platformadmin.infra.dao.po.WebhookDeliveryLogPO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WebhookDeliveryLogRepositoryImpl}.
 * <p>
 * 仓储继承 DatabaseRepositoryImpl 后，通用 save 由基类提供；本测试覆盖
 * 基类 save 行为以及本领域特有方法 findByEventId。
 *
 * @author asset-security
 */
@ExtendWith(MockitoExtension.class)
class WebhookDeliveryLogRepositoryImplTest {

    @Mock
    private WebhookDeliveryLogMapper mapper;

    private WebhookDeliveryLogRepositoryImpl repository;

    private MockedStatic<ConvertHelper> convertHelperMock;

    @BeforeEach
    void setUp() {
        // 基类 baseMapper 为字段注入，手动反射注入 mock Mapper
        repository = new WebhookDeliveryLogRepositoryImpl();
        ReflectionTestUtils.setField(repository, "baseMapper", mapper);
        convertHelperMock = mockStatic(ConvertHelper.class);
    }

    @AfterEach
    void tearDown() {
        convertHelperMock.close();
    }

    @Test
    void testSave() {
        // given
        WebhookDeliveryLogDO logDO = new WebhookDeliveryLogDO();
        logDO.setId(1L);
        logDO.setEventId("event-001");
        WebhookDeliveryLogPO logPO = new WebhookDeliveryLogPO();
        logPO.setId(1L);
        logPO.setEventId("event-001");
        WebhookDeliveryLogDO savedDO = new WebhookDeliveryLogDO();
        savedDO.setId(1L);
        savedDO.setEventId("event-001");

        convertHelperMock.when(() -> ConvertHelper.convert(logDO, WebhookDeliveryLogPO.class))
                .thenReturn(logPO);
        when(mapper.insert(logPO)).thenReturn(1);
        convertHelperMock.when(() -> ConvertHelper.convert(logPO, WebhookDeliveryLogDO.class))
                .thenReturn(savedDO);

        // when
        WebhookDeliveryLogDO result = repository.save(logDO);

        // then
        ArgumentCaptor<WebhookDeliveryLogPO> captor = ArgumentCaptor.forClass(WebhookDeliveryLogPO.class);
        verify(mapper).insert(captor.capture());
        assertEquals("event-001", captor.getValue().getEventId());
        assertNotNull(result);
        assertEquals("event-001", result.getEventId());
    }

    @Test
    void testFindByEventId() {
        // given
        String eventId = "event-001";
        WebhookDeliveryLogPO po = new WebhookDeliveryLogPO();
        po.setId(1L);
        po.setEventId(eventId);
        WebhookDeliveryLogDO expectedDO = new WebhookDeliveryLogDO();
        expectedDO.setId(1L);
        expectedDO.setEventId(eventId);

        when(mapper.selectOne(any())).thenReturn(po);
        convertHelperMock.when(() -> ConvertHelper.convert(po, WebhookDeliveryLogDO.class))
                .thenReturn(expectedDO);

        // when
        WebhookDeliveryLogDO result = repository.findByEventId(eventId);

        // then
        assertNotNull(result);
        assertEquals(eventId, result.getEventId());
        verify(mapper).selectOne(any());
    }

    @Test
    void testFindByEventId_NullInput() {
        // when
        WebhookDeliveryLogDO result = repository.findByEventId(null);

        // then
        assertNull(result);
        verify(mapper, never()).selectOne(any());
    }
}

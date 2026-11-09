package com.vtc.platformadmin.infra.repository;

import com.botany.spore.ddd.infra.utils.convertor.ConvertHelper;
import com.vtc.platformadmin.domain.partner.model.entity.PartnerDO;
import com.vtc.platformadmin.infra.dao.PartnerMapper;
import com.vtc.platformadmin.infra.crypto.WebhookSecretCipher;
import com.vtc.platformadmin.infra.dao.po.PartnerPO;
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
 * Unit tests for {@link PartnerRepositoryImpl}.
 * <p>
 * 仓储继承 DatabaseRepositoryImpl 后，通用 save/getById 由基类提供；本测试覆盖
 * 基类 save/getById 行为以及本领域特有方法 findByPartnerId。
 *
 * @author asset-security
 */
@ExtendWith(MockitoExtension.class)
class PartnerRepositoryImplTest {

    @Mock
    private PartnerMapper partnerMapper;

    private PartnerRepositoryImpl repository;

    private MockedStatic<ConvertHelper> convertHelperMock;

    @BeforeEach
    void setUp() {
        // 基类 baseMapper 为字段注入，手动反射注入 mock Mapper
        repository = new PartnerRepositoryImpl(null, null, null,
                new WebhookSecretCipher("eWxtZkRseFhLcm04Skc0OWdGYlUwUT09"));
        ReflectionTestUtils.setField(repository, "baseMapper", partnerMapper);
        convertHelperMock = mockStatic(ConvertHelper.class);
    }

    @AfterEach
    void tearDown() {
        convertHelperMock.close();
    }

    @Test
    void testSave() {
        // given
        PartnerDO partnerDO = new PartnerDO();
        partnerDO.setId(1L);
        partnerDO.setPartnerId("partner-001");
        PartnerPO partnerPO = new PartnerPO();
        partnerPO.setId(1L);
        partnerPO.setPartnerId("partner-001");
        PartnerDO savedDO = new PartnerDO();
        savedDO.setId(1L);
        savedDO.setPartnerId("partner-001");

        convertHelperMock.when(() -> ConvertHelper.convert(partnerDO, PartnerPO.class))
                .thenReturn(partnerPO);
        when(partnerMapper.insert(partnerPO)).thenReturn(1);
        convertHelperMock.when(() -> ConvertHelper.convert(partnerPO, PartnerDO.class))
                .thenReturn(savedDO);

        // when
        PartnerDO result = repository.save(partnerDO);

        // then
        ArgumentCaptor<PartnerPO> captor = ArgumentCaptor.forClass(PartnerPO.class);
        verify(partnerMapper).insert(captor.capture());
        assertEquals("partner-001", captor.getValue().getPartnerId());
        assertNotNull(result);
        assertEquals("partner-001", result.getPartnerId());
    }

    @Test
    void testGetById_Found() {
        // given
        Long id = 1L;
        PartnerPO po = new PartnerPO();
        po.setId(id);
        po.setPartnerId("partner-001");
        PartnerDO expectedDO = new PartnerDO();
        expectedDO.setId(id);
        expectedDO.setPartnerId("partner-001");

        when(partnerMapper.selectById(id)).thenReturn(po);
        convertHelperMock.when(() -> ConvertHelper.convert(po, PartnerDO.class))
                .thenReturn(expectedDO);

        // when
        PartnerDO result = repository.getById(id);

        // then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("partner-001", result.getPartnerId());
        verify(partnerMapper).selectById(id);
    }

    @Test
    void testGetById_NotFound() {
        // given
        Long id = 999L;
        when(partnerMapper.selectById(id)).thenReturn(null);

        // when
        PartnerDO result = repository.getById(id);

        // then
        assertNull(result);
        verify(partnerMapper).selectById(id);
    }

    @Test
    void testFindByPartnerId() {
        // given
        String partnerId = "partner-001";
        PartnerPO po = new PartnerPO();
        po.setId(1L);
        po.setPartnerId(partnerId);
        PartnerDO expectedDO = new PartnerDO();
        expectedDO.setId(1L);
        expectedDO.setPartnerId(partnerId);

        when(partnerMapper.selectOne(any())).thenReturn(po);
        convertHelperMock.when(() -> ConvertHelper.convert(po, PartnerDO.class))
                .thenReturn(expectedDO);

        // when
        PartnerDO result = repository.findByPartnerId(partnerId);

        // then
        assertNotNull(result);
        assertEquals(partnerId, result.getPartnerId());
        verify(partnerMapper).selectOne(any());
    }
}

package com.vtc.platformadmin.infra.repository;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.infra.utils.convertor.ConvertHelper;
import com.vtc.platformadmin.domain.invocation.model.entity.ApiInvocationDO;
import com.vtc.platformadmin.infra.dao.ApiInvocationMapper;
import com.vtc.platformadmin.infra.dao.po.ApiInvocationPO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ApiInvocationRepositoryImpl}.
 * <p>
 * 仓储继承 DatabaseRepositoryImpl 后，通用 save/getById 由基类提供；本测试覆盖
 * 基类 save/getById 行为以及本领域特有方法 pageInvocations（PageInfo 新签名）。
 *
 * @author asset-security
 */
@ExtendWith(MockitoExtension.class)
class ApiInvocationRepositoryImplTest {

    @Mock
    private ApiInvocationMapper apiInvocationMapper;

    private ApiInvocationRepositoryImpl repository;

    private MockedStatic<ConvertHelper> convertHelperMock;

    @BeforeAll
    static void initTableInfo() {
        // LambdaQueryWrapper 依赖 MyBatis-Plus TableInfo 缓存；纯单测无 SqlSessionFactory，
        // 需手动初始化，否则 ApiInvocationPO::getXxx 解析会抛 can not find lambda cache
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                ApiInvocationPO.class);
    }

    @BeforeEach
    void setUp() {
        // 基类 baseMapper 为字段注入，手动反射注入 mock Mapper
        repository = new ApiInvocationRepositoryImpl(apiInvocationMapper,  null);
        ReflectionTestUtils.setField(repository, "baseMapper", apiInvocationMapper);
        convertHelperMock = mockStatic(ConvertHelper.class);
    }

    @AfterEach
    void tearDown() {
        convertHelperMock.close();
    }

    @Test
    void testSave() {
        // given
        ApiInvocationDO invocationDO = new ApiInvocationDO();
        invocationDO.setInvocationId("inv-001");
        ApiInvocationPO invocationPO = new ApiInvocationPO();
        invocationPO.setInvocationId("inv-001");
        ApiInvocationDO savedDO = new ApiInvocationDO();
        savedDO.setInvocationId("inv-001");

        convertHelperMock.when(() -> ConvertHelper.convert(invocationDO, ApiInvocationPO.class))
                .thenReturn(invocationPO);
        when(apiInvocationMapper.insert(invocationPO)).thenReturn(1);
        convertHelperMock.when(() -> ConvertHelper.convert(invocationPO, ApiInvocationDO.class))
                .thenReturn(savedDO);

        // when
        ApiInvocationDO result = repository.save(invocationDO);

        // then
        ArgumentCaptor<ApiInvocationPO> captor = ArgumentCaptor.forClass(ApiInvocationPO.class);
        verify(apiInvocationMapper).insert(captor.capture());
        assertEquals("inv-001", captor.getValue().getInvocationId());
        assertNotNull(result);
        assertEquals("inv-001", result.getInvocationId());
    }

    @Test
    void testGetById_Found() {
        // given
        String invocationId = "inv-001";
        ApiInvocationPO po = new ApiInvocationPO();
        po.setInvocationId(invocationId);
        ApiInvocationDO expectedDO = new ApiInvocationDO();
        expectedDO.setInvocationId(invocationId);

        when(apiInvocationMapper.selectById(invocationId)).thenReturn(po);
        convertHelperMock.when(() -> ConvertHelper.convert(po, ApiInvocationDO.class))
                .thenReturn(expectedDO);

        // when
        ApiInvocationDO result = repository.getById(invocationId);

        // then
        assertNotNull(result);
        assertEquals(invocationId, result.getInvocationId());
        verify(apiInvocationMapper).selectById(invocationId);
    }

    @Test
    void testGetById_NotFound() {
        // given
        String invocationId = "inv-999";
        when(apiInvocationMapper.selectById(invocationId)).thenReturn(null);

        // when
        ApiInvocationDO result = repository.getById(invocationId);

        // then
        assertNull(result);
        verify(apiInvocationMapper).selectById(invocationId);
    }

    @Test
    void testPageInvocations() {
        // given
        String partnerId = "partner-001";
        Date startTime = new Date(100000L);
        Date endTime = new Date(200000L);
        PageInfo<ApiInvocationDO> inputPage = new PageInfo<>(1, 10);

        ApiInvocationPO po = new ApiInvocationPO();
        po.setInvocationId("inv-001");
        PageInfo<ApiInvocationPO> poPage = new PageInfo<>(1, 10, 1);
        poPage.setRecords(Collections.singletonList(po));

        ApiInvocationDO expectedDO = new ApiInvocationDO();
        expectedDO.setInvocationId("inv-001");
        PageInfo<ApiInvocationDO> doPage = new PageInfo<>(1, 10, 1);
        doPage.setRecords(Collections.singletonList(expectedDO));

        convertHelperMock.when(() -> ConvertHelper.convertPageInfo(inputPage, ApiInvocationPO.class))
                .thenReturn(poPage);
        when(apiInvocationMapper.selectPage(any(PageInfo.class), any())).thenReturn(poPage);
        convertHelperMock.when(() -> ConvertHelper.convertPageInfo(poPage, ApiInvocationDO.class))
                .thenReturn(doPage);

        // when
        PageInfo<ApiInvocationDO> result = repository.pageInvocations(inputPage, partnerId, null, startTime, endTime);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("inv-001", result.getRecords().get(0).getInvocationId());
        verify(apiInvocationMapper).selectPage(any(PageInfo.class), any());
    }
}

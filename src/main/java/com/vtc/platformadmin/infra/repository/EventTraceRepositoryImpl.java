package com.vtc.platformadmin.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.infra.repository.DatabaseRepositoryImpl;
import com.botany.spore.ddd.infra.utils.convertor.ConvertHelper;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventTraceDO;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventTraceRepository;
import com.vtc.platformadmin.infra.dao.EventTraceMapper;
import com.vtc.platformadmin.infra.dao.po.EventTracePO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventTraceQueryParams;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 事件发布轨迹仓储实现。
 * <p>
 * 继承 {@link DatabaseRepositoryImpl}，通用 save/getById/list/page 等由基类提供；
 * 此处仅实现本领域特有方法。
 *
 * @author asset-security
 */
@Repository
public class EventTraceRepositoryImpl
        extends DatabaseRepositoryImpl<EventTraceMapper, EventTraceDO, EventTracePO>
        implements IEventTraceRepository {

    @Override
    public EventTraceDO findByEventId(String eventId) {
        if (!StringUtils.hasText(eventId)) {
            return null;
        }
        EventTracePO po = baseMapper.selectOne(new LambdaQueryWrapper<EventTracePO>()
                .eq(EventTracePO::getEventId, eventId));
        return po == null ? null : ConvertHelper.convert(po, EventTraceDO.class);
    }

    @Override
    public List<EventTraceDO> findByTraceId(String traceId) {
        if (!StringUtils.hasText(traceId)) {
            return Collections.emptyList();
        }
        List<EventTracePO> rows = baseMapper.selectList(new LambdaQueryWrapper<EventTracePO>()
                .eq(EventTracePO::getTraceId, traceId));
        return ConvertHelper.convertList(rows, EventTraceDO.class);
    }

    @Override
    public PageInfo<EventTraceDO> pageTraces(EventTraceQueryParams params) {
        LambdaQueryWrapper<EventTracePO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(params.getEventType())) {
            wrapper.eq(EventTracePO::getEventType, params.getEventType());
        }
        if (StringUtils.hasText(params.getTopic())) {
            wrapper.eq(EventTracePO::getTopic, params.getTopic());
        }
        if (StringUtils.hasText(params.getSourceService())) {
            wrapper.eq(EventTracePO::getSourceService, params.getSourceService());
        }
        if (StringUtils.hasText(params.getTenantType())) {
            wrapper.eq(EventTracePO::getTenantType, params.getTenantType());
        }
        if (StringUtils.hasText(params.getTenantId())) {
            wrapper.eq(EventTracePO::getTenantId, params.getTenantId());
        }
        if (StringUtils.hasText(params.getTraceId())) {
            wrapper.eq(EventTracePO::getTraceId, params.getTraceId());
        }
        if (StringUtils.hasText(params.getStatus())) {
            wrapper.eq(EventTracePO::getStatus, params.getStatus());
        }
        if (params.getStartTime() != null) {
            wrapper.ge(EventTracePO::getPublishedAt, params.getStartTime());
        }
        if (params.getEndTime() != null) {
            wrapper.le(EventTracePO::getPublishedAt, params.getEndTime());
        }
        wrapper.orderByDesc(EventTracePO::getPublishedAt);

        int pageNum = params.getPageNum() != null ? params.getPageNum() : 1;
        int pageSize = params.getPageSize() != null ? params.getPageSize() : 20;
        Page<EventTracePO> page = new Page<>(pageNum, pageSize);
        Page<EventTracePO> result = baseMapper.selectPage(page, wrapper);

        PageInfo<EventTraceDO> pageInfo = new PageInfo<>();
        pageInfo.setCurrent(result.getCurrent());
        pageInfo.setSize(result.getSize());
        pageInfo.setTotal(result.getTotal());
        pageInfo.setRecords(ConvertHelper.convertList(result.getRecords(), EventTraceDO.class));
        return pageInfo;
    }
}

package com.vtc.platformadmin.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.infra.repository.DatabaseRepositoryImpl;
import com.botany.spore.ddd.infra.utils.convertor.ConvertHelper;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventDeadLetterDO;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventDeadLetterRepository;
import com.vtc.platformadmin.infra.dao.EventDeadLetterMapper;
import com.vtc.platformadmin.infra.dao.po.EventDeadLetterPO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventDeadLetterQueryParams;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 死信事件索引仓储实现。
 * <p>
 * 继承 {@link DatabaseRepositoryImpl}，通用 save/getById/list/page 等由基类提供；
 * 此处仅实现本领域特有方法。
 *
 * @author asset-security
 */
@Repository
public class EventDeadLetterRepositoryImpl
        extends DatabaseRepositoryImpl<EventDeadLetterMapper, EventDeadLetterDO, EventDeadLetterPO>
        implements IEventDeadLetterRepository {

    @Override
    public EventDeadLetterDO findByEventId(String eventId) {
        if (!StringUtils.hasText(eventId)) {
            return null;
        }
        EventDeadLetterPO po = baseMapper.selectOne(new LambdaQueryWrapper<EventDeadLetterPO>()
                .eq(EventDeadLetterPO::getEventId, eventId));
        return po == null ? null : ConvertHelper.convert(po, EventDeadLetterDO.class);
    }

    @Override
    public PageInfo<EventDeadLetterDO> pageDeadLetters(EventDeadLetterQueryParams params) {
        LambdaQueryWrapper<EventDeadLetterPO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(params.getEventType())) {
            wrapper.eq(EventDeadLetterPO::getEventType, params.getEventType());
        }
        if (StringUtils.hasText(params.getOriginTopic())) {
            wrapper.eq(EventDeadLetterPO::getOriginTopic, params.getOriginTopic());
        }
        if (StringUtils.hasText(params.getConsumerGroup())) {
            wrapper.eq(EventDeadLetterPO::getConsumerGroup, params.getConsumerGroup());
        }
        if (StringUtils.hasText(params.getFailedService())) {
            wrapper.eq(EventDeadLetterPO::getFailedService, params.getFailedService());
        }
        if (StringUtils.hasText(params.getReplayStatus())) {
            wrapper.eq(EventDeadLetterPO::getReplayStatus, params.getReplayStatus());
        }
        if (params.getStartTime() != null) {
            wrapper.ge(EventDeadLetterPO::getDeadTime, params.getStartTime());
        }
        if (params.getEndTime() != null) {
            wrapper.le(EventDeadLetterPO::getDeadTime, params.getEndTime());
        }
        wrapper.orderByDesc(EventDeadLetterPO::getDeadTime);

        int pageNum = params.getPageNum() != null ? params.getPageNum() : 1;
        int pageSize = params.getPageSize() != null ? params.getPageSize() : 20;
        Page<EventDeadLetterPO> page = new Page<>(pageNum, pageSize);
        Page<EventDeadLetterPO> result = baseMapper.selectPage(page, wrapper);

        PageInfo<EventDeadLetterDO> pageInfo = new PageInfo<>();
        pageInfo.setCurrent(result.getCurrent());
        pageInfo.setSize(result.getSize());
        pageInfo.setTotal(result.getTotal());
        pageInfo.setRecords(ConvertHelper.convertList(result.getRecords(), EventDeadLetterDO.class));
        return pageInfo;
    }
}

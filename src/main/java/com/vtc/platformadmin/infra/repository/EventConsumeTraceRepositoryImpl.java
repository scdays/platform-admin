package com.vtc.platformadmin.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.botany.spore.ddd.infra.repository.DatabaseRepositoryImpl;
import com.botany.spore.ddd.infra.utils.convertor.ConvertHelper;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventConsumeTraceDO;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventConsumeTraceRepository;
import com.vtc.platformadmin.infra.dao.EventConsumeTraceMapper;
import com.vtc.platformadmin.infra.dao.po.EventConsumeTracePO;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 事件消费轨迹仓储实现。
 * <p>
 * 继承 {@link DatabaseRepositoryImpl}，通用 save/getById/list/page 等由基类提供；
 * 此处仅实现本领域特有方法。
 *
 * @author asset-security
 */
@Repository
public class EventConsumeTraceRepositoryImpl
        extends DatabaseRepositoryImpl<EventConsumeTraceMapper, EventConsumeTraceDO, EventConsumeTracePO>
        implements IEventConsumeTraceRepository {

    @Override
    public List<EventConsumeTraceDO> findByEventId(String eventId) {
        if (!StringUtils.hasText(eventId)) {
            return Collections.emptyList();
        }
        List<EventConsumeTracePO> rows = baseMapper.selectList(new LambdaQueryWrapper<EventConsumeTracePO>()
                .eq(EventConsumeTracePO::getEventId, eventId));
        return ConvertHelper.convertList(rows, EventConsumeTraceDO.class);
    }
}

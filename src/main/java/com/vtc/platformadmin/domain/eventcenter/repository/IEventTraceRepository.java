package com.vtc.platformadmin.domain.eventcenter.repository;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.domain.repository.IDatabaseRepository;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventTraceDO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventTraceQueryParams;

import java.util.List;

/**
 * 事件发布轨迹仓储接口。
 * <p>
 * 继承 {@link IDatabaseRepository}，通用 save/getById/page 等由基类提供；
 * 此处仅声明本领域特有方法。
 *
 * @author asset-security
 */
public interface IEventTraceRepository extends IDatabaseRepository<EventTraceDO> {

    EventTraceDO findByEventId(String eventId);

    List<EventTraceDO> findByTraceId(String traceId);

    PageInfo<EventTraceDO> pageTraces(EventTraceQueryParams params);
}

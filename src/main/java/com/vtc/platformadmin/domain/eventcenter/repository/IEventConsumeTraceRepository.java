package com.vtc.platformadmin.domain.eventcenter.repository;

import com.botany.spore.ddd.domain.repository.IDatabaseRepository;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventConsumeTraceDO;

import java.util.List;

/**
 * 事件消费轨迹仓储接口。
 * <p>
 * 继承 {@link IDatabaseRepository}，通用 save/getById/page 等由基类提供；
 * 此处仅声明本领域特有方法。
 *
 * @author asset-security
 */
public interface IEventConsumeTraceRepository extends IDatabaseRepository<EventConsumeTraceDO> {

    List<EventConsumeTraceDO> findByEventId(String eventId);
}

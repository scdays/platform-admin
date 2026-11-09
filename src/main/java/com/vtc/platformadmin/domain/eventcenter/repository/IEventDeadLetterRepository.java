package com.vtc.platformadmin.domain.eventcenter.repository;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.domain.repository.IDatabaseRepository;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventDeadLetterDO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventDeadLetterQueryParams;

/**
 * 死信事件索引仓储接口。
 * <p>
 * 继承 {@link IDatabaseRepository}，通用 save/getById/page 等由基类提供；
 * 此处仅声明本领域特有方法。
 *
 * @author asset-security
 */
public interface IEventDeadLetterRepository extends IDatabaseRepository<EventDeadLetterDO> {

    EventDeadLetterDO findByEventId(String eventId);

    PageInfo<EventDeadLetterDO> pageDeadLetters(EventDeadLetterQueryParams params);
}

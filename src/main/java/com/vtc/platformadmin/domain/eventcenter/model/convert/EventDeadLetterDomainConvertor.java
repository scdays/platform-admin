package com.vtc.platformadmin.domain.eventcenter.model.convert;

import com.botany.spore.ddd.domain.model.convertor.IDomainConvertor;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventDeadLetterDO;
import com.vtc.platformadmin.infra.dao.po.EventDeadLetterPO;
import org.springframework.stereotype.Component;

/**
 * 死信事件索引 DO ↔ PO 转换（ConvertHelper 注册）。
 *
 * @author asset-security
 */
@Component
public class EventDeadLetterDomainConvertor implements IDomainConvertor<EventDeadLetterDO, EventDeadLetterPO> {

    @Override
    public EventDeadLetterPO doToPo(EventDeadLetterDO domainObject) {
        return IDomainConvertor.super.doToPo(domainObject);
    }

    @Override
    public EventDeadLetterDO poToDo(EventDeadLetterPO po) {
        return IDomainConvertor.super.poToDo(po);
    }
}

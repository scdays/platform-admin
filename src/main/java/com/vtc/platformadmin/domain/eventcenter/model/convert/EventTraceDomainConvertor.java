package com.vtc.platformadmin.domain.eventcenter.model.convert;

import com.botany.spore.ddd.domain.model.convertor.IDomainConvertor;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventTraceDO;
import com.vtc.platformadmin.infra.dao.po.EventTracePO;
import org.springframework.stereotype.Component;

/**
 * 事件发布轨迹 DO ↔ PO 转换（ConvertHelper 注册）。
 *
 * @author asset-security
 */
@Component
public class EventTraceDomainConvertor implements IDomainConvertor<EventTraceDO, EventTracePO> {

    @Override
    public EventTracePO doToPo(EventTraceDO domainObject) {
        return IDomainConvertor.super.doToPo(domainObject);
    }

    @Override
    public EventTraceDO poToDo(EventTracePO po) {
        return IDomainConvertor.super.poToDo(po);
    }
}

package com.vtc.platformadmin.domain.eventcenter.model.convert;

import com.botany.spore.ddd.domain.model.convertor.IDomainConvertor;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventConsumeTraceDO;
import com.vtc.platformadmin.infra.dao.po.EventConsumeTracePO;
import org.springframework.stereotype.Component;

/**
 * 事件消费轨迹 DO ↔ PO 转换（ConvertHelper 注册）。
 *
 * @author asset-security
 */
@Component
public class EventConsumeTraceDomainConvertor implements IDomainConvertor<EventConsumeTraceDO, EventConsumeTracePO> {

    @Override
    public EventConsumeTracePO doToPo(EventConsumeTraceDO domainObject) {
        return IDomainConvertor.super.doToPo(domainObject);
    }

    @Override
    public EventConsumeTraceDO poToDo(EventConsumeTracePO po) {
        return IDomainConvertor.super.poToDo(po);
    }
}

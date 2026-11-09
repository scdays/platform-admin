package com.vtc.platformadmin.domain.eventcenter.model.convert;

import com.botany.spore.ddd.domain.model.convertor.IDomainConvertor;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventAuditLogDO;
import com.vtc.platformadmin.infra.dao.po.EventAuditLogPO;
import org.springframework.stereotype.Component;

/**
 * 事件审计日志 DO ↔ PO 转换（ConvertHelper 注册）。
 *
 * @author asset-security
 */
@Component
public class EventAuditLogDomainConvertor implements IDomainConvertor<EventAuditLogDO, EventAuditLogPO> {

    @Override
    public EventAuditLogPO doToPo(EventAuditLogDO domainObject) {
        return IDomainConvertor.super.doToPo(domainObject);
    }

    @Override
    public EventAuditLogDO poToDo(EventAuditLogPO po) {
        return IDomainConvertor.super.poToDo(po);
    }
}
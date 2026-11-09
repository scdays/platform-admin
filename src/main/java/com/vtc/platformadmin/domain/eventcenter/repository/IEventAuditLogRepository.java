package com.vtc.platformadmin.domain.eventcenter.repository;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.domain.repository.IDatabaseRepository;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventAuditLogDO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventAuditLogQueryParams;

/**
 * 事件审计日志仓储接口。
 *
 * @author asset-security
 */
public interface IEventAuditLogRepository extends IDatabaseRepository<EventAuditLogDO> {

    PageInfo<EventAuditLogDO> pageAuditLogs(EventAuditLogQueryParams params);
}
package com.vtc.platformadmin.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.infra.repository.DatabaseRepositoryImpl;
import com.botany.spore.ddd.infra.utils.convertor.ConvertHelper;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventAuditLogDO;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventAuditLogRepository;
import com.vtc.platformadmin.infra.dao.EventAuditLogMapper;
import com.vtc.platformadmin.infra.dao.po.EventAuditLogPO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventAuditLogQueryParams;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 事件审计日志仓储实现。
 *
 * @author asset-security
 */
@Repository
public class EventAuditLogRepositoryImpl
        extends DatabaseRepositoryImpl<EventAuditLogMapper, EventAuditLogDO, EventAuditLogPO>
        implements IEventAuditLogRepository {

    @Override
    public PageInfo<EventAuditLogDO> pageAuditLogs(EventAuditLogQueryParams params) {
        LambdaQueryWrapper<EventAuditLogPO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(params.getOperatorId())) {
            wrapper.eq(EventAuditLogPO::getOperatorId, params.getOperatorId());
        }
        if (StringUtils.hasText(params.getOperationType())) {
            wrapper.eq(EventAuditLogPO::getOperationType, params.getOperationType());
        }
        if (StringUtils.hasText(params.getTargetType())) {
            wrapper.eq(EventAuditLogPO::getTargetType, params.getTargetType());
        }
        if (StringUtils.hasText(params.getResult())) {
            wrapper.eq(EventAuditLogPO::getResult, params.getResult());
        }
        if (params.getStartTime() != null) {
            wrapper.ge(EventAuditLogPO::getCreatedTime, params.getStartTime());
        }
        if (params.getEndTime() != null) {
            wrapper.le(EventAuditLogPO::getCreatedTime, params.getEndTime());
        }
        wrapper.orderByDesc(EventAuditLogPO::getCreatedTime);

        int pageNum = params.getPageNum() != null ? params.getPageNum() : 1;
        int pageSize = params.getPageSize() != null ? params.getPageSize() : 20;
        Page<EventAuditLogPO> page = new Page<>(pageNum, pageSize);
        Page<EventAuditLogPO> result = baseMapper.selectPage(page, wrapper);

        PageInfo<EventAuditLogDO> pageInfo = new PageInfo<>();
        pageInfo.setCurrent(result.getCurrent());
        pageInfo.setSize(result.getSize());
        pageInfo.setTotal(result.getTotal());
        pageInfo.setRecords(ConvertHelper.convertList(result.getRecords(), EventAuditLogDO.class));
        return pageInfo;
    }
}
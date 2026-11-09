package com.vtc.platformadmin.domain.eventcenter.model.entity;

import com.botany.spore.ddd.domain.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 事件中心操作审计日志（领域实体）。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventAuditLogDO extends BaseDO {

    private String operatorId;
    private String operatorName;
    private String operationType;
    private String targetType;
    private String targetId;
    private String result;
    private String detail;
    private String clientIp;
    private Date createdTime;
    private Date updatedTime;
}
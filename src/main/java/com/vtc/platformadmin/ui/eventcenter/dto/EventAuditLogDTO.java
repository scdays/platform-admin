package com.vtc.platformadmin.ui.eventcenter.dto;

import com.botany.spore.ddd.ui.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 事件审计日志 UI 数据传输对象。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventAuditLogDTO extends BaseDTO {

    private String operatorId;
    private String operatorName;
    private String operationType;
    private String targetType;
    private String targetId;
    private String result;
    private String detail;
    private String clientIp;
    private Date createdTime;
}
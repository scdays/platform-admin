package com.vtc.platformadmin.infra.dao.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.botany.spore.mybatis.pojo.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 事件中心操作审计日志 PO。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("event_audit_log")
public class EventAuditLogPO extends BasePO {

    @TableId
    private Long id;

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
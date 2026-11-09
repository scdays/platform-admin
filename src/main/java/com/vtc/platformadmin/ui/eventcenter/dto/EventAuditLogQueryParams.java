package com.vtc.platformadmin.ui.eventcenter.dto;

import java.util.Date;

/**
 * 事件审计日志查询参数。
 *
 * @author asset-security
 */
public class EventAuditLogQueryParams {

    private String operatorId;
    private String operationType;
    private String targetType;
    private String result;
    private Date startTime;
    private Date endTime;
    private Integer pageNum;
    private Integer pageSize;

    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
package com.vtc.platformadmin.ui.dto;

import lombok.Data;

import java.util.Date;

/**
 * 工作台推送记录 DTO（仅投递元数据，不解析业务 payload）。
 * <p>
 * 前端用 eventId 关联 open-api-service 的 /webhook-event-details 获取业务详情。
 *
 * @author asset-security
 */
@Data
public class PushRecordDTO {

    private Long id;

    /** 业务事件ID（关联业务侧的桥梁） */
    private String eventId;

    /** 合作方ID */
    private String partnerId;

    /** 事件类型（TASK_COMPLETED / TASK_FAILED / INSTANCE_VERIFY_FIX_COMPLETED / EXPORT_READY / ARTIFACT_READY） */
    private String eventType;

    /** 投递状态 PENDING / SUCCESS / FAILED */
    private String status;

    /** HTTP 响应码 */
    private Integer httpStatus;

    /** 投递次数（同 eventId 去重合并） */
    private int attemptCount;

    /** 触发来源 FIRST_ATTEMPT / AUTO_RETRY / MANUAL_RETRY */
    private String triggerSource;

    /** 创建时间 */
    private Date createdAt;

    /** 是否可下载（按 partner downloadableStages 是否包含本条 eventType 判断） */
    private Boolean downloadable;
}
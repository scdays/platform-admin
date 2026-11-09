package com.vtc.platformadmin.ui.invocation.dto;

import com.botany.spore.ddd.ui.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * API 调用记录 DTO（字段镜像 ApiInvocationDO，继承 BaseDTO）。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InvocationDTO extends BaseDTO {

    private String invocationId;

    private String requestId;

    private String partnerId;

    private String operationId;

    private String httpMethod;

    private String requestPath;

    private Integer responseCode;

    private Integer httpStatus;

    private Integer latencyMs;

    private String clientIp;

    private String errorMessage;

    private String resourceType;

    private String resourceId;

    private String caseId;

    private Date startedAt;

    private Date finishedAt;
}
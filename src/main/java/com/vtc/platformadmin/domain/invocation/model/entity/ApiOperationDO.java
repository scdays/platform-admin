package com.vtc.platformadmin.domain.invocation.model.entity;

import com.botany.spore.ddd.domain.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiOperationDO extends BaseDO {

    private String operationId;

    private String apiVersion;

    private String httpMethod;

    private String pathPattern;

    private String requiredCapability;

    private String domain;

    private String status;

    private Date publishedAt;

    private String openapiTag;

    private String summary;
}

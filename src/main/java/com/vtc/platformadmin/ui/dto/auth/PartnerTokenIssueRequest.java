package com.vtc.platformadmin.ui.dto.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("PartnerTokenIssueRequest")
public class PartnerTokenIssueRequest {

    @ApiModelProperty(value = "固定 client_credentials", required = true, example = "client_credentials")
    @NotBlank(message = "grantType must not be blank")
    private String grantType;

    @ApiModelProperty(value = "客户端 ID", required = true)
    @NotBlank(message = "clientId must not be blank")
    private String clientId;

    @ApiModelProperty(value = "客户端密钥", required = true)
    @NotBlank(message = "clientSecret must not be blank")
    private String clientSecret;
}

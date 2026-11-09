package com.vtc.platformadmin.ui.dto.auth;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PartnerTokenIntrospectRequest {

    @NotBlank(message = "token must not be blank")
    private String token;
}

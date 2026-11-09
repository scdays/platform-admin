package com.vtc.platformadmin.ui.auth;

import com.botany.spore.ddd.ui.BaseUI;
import com.vtc.platformadmin.app.service.IPartnerTokenAppService;
import com.vtc.platformadmin.ui.dto.ApiResponse;
import com.vtc.platformadmin.ui.dto.auth.PartnerTokenIntrospectRequest;
import com.vtc.platformadmin.ui.dto.auth.PartnerTokenIntrospectResponse;
import com.vtc.platformadmin.ui.dto.auth.PartnerTokenIssueRequest;
import com.vtc.platformadmin.ui.dto.auth.PartnerTokenIssueResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author scday
 */
@RestController
@Validated
@Api(tags = "Partner Token")
public class PartnerTokenUI extends BaseUI {

    private final IPartnerTokenAppService partnerTokenAppService;

    public PartnerTokenUI(IPartnerTokenAppService partnerTokenAppService) {
        this.partnerTokenAppService = partnerTokenAppService;
    }

    @ApiOperation(value = "OAuth2 client_credentials 换 Token", notes = "路径：/oauth/token 或 /api/open/v1/oauth/token")
    @PostMapping({"/oauth/token", "/api/open/v1/oauth/token"})
    public ApiResponse<PartnerTokenIssueResponse> issueToken(@Valid @RequestBody PartnerTokenIssueRequest request) {
        return partnerTokenAppService.issueToken(request);
    }

    @ApiOperation(value = "Token introspect", notes = "供 partner-gateway Feign 降级；入参 token")
    @PostMapping("/internal/token/introspect")
    public ApiResponse<PartnerTokenIntrospectResponse> introspect(@Valid @RequestBody PartnerTokenIntrospectRequest request) {
        return partnerTokenAppService.introspect(request);
    }
}

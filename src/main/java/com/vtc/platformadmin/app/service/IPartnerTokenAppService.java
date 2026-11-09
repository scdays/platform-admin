package com.vtc.platformadmin.app.service;

import com.vtc.platformadmin.ui.dto.ApiResponse;
import com.vtc.platformadmin.ui.dto.auth.PartnerTokenIntrospectRequest;
import com.vtc.platformadmin.ui.dto.auth.PartnerTokenIntrospectResponse;
import com.vtc.platformadmin.ui.dto.auth.PartnerTokenIssueRequest;
import com.vtc.platformadmin.ui.dto.auth.PartnerTokenIssueResponse;

public interface IPartnerTokenAppService {

    ApiResponse<PartnerTokenIssueResponse> issueToken(PartnerTokenIssueRequest request);

    ApiResponse<PartnerTokenIntrospectResponse> introspect(PartnerTokenIntrospectRequest request);
}

package com.vtc.platformadmin.app.service.impl;

import com.vtc.platformadmin.app.service.IPartnerTokenAppService;
import com.vtc.platformadmin.domain.invocation.OpenApiConstants;
import com.vtc.platformadmin.domain.partner.model.PartnerConstants;
import com.vtc.platformadmin.domain.partner.model.entity.PartnerCredentialDO;
import com.vtc.platformadmin.domain.partner.model.entity.PartnerDO;
import com.vtc.platformadmin.domain.partner.repository.IPartnerRepository;
import com.vtc.platformadmin.infra.config.PlatformAdminProperties;
import com.vtc.platformadmin.infra.redis.PartnerTokenRedisStore;
import com.vtc.platformadmin.ui.dto.ApiResponse;
import com.vtc.platformadmin.ui.dto.auth.PartnerTokenIntrospectRequest;
import com.vtc.platformadmin.ui.dto.auth.PartnerTokenIntrospectResponse;
import com.vtc.platformadmin.ui.dto.auth.PartnerTokenIssueRequest;
import com.vtc.platformadmin.ui.dto.auth.PartnerTokenIssueResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PartnerTokenAppServiceImpl implements IPartnerTokenAppService {

    private static final String GRANT_CLIENT_CREDENTIALS = "client_credentials";

    private final IPartnerRepository partnerRepository;
    private final PartnerTokenRedisStore tokenRedisStore;
    private final PlatformAdminProperties properties;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public PartnerTokenAppServiceImpl(IPartnerRepository partnerRepository,
                                      PartnerTokenRedisStore tokenRedisStore,
                                      PlatformAdminProperties properties) {
        this.partnerRepository = partnerRepository;
        this.tokenRedisStore = tokenRedisStore;
        this.properties = properties;
    }

    @Override
    public ApiResponse<PartnerTokenIssueResponse> issueToken(PartnerTokenIssueRequest request) {
        if (request == null) {
            return ApiResponse.of(OpenApiConstants.CODE_PARAM_ERROR, "请求体不能为空", null);
        }
        if (!GRANT_CLIENT_CREDENTIALS.equalsIgnoreCase(request.getGrantType())) {
            return ApiResponse.of(OpenApiConstants.CODE_PARAM_ERROR, "grantType 必须为 client_credentials", null);
        }

        PartnerCredentialDO credential = partnerRepository.findCredentialByClientId(request.getClientId());
        if (credential == null || !PartnerConstants.STATUS_ACTIVE.equals(credential.getStatus())) {
            return ApiResponse.of(OpenApiConstants.CODE_AUTH_FAILED, "Invalid client credentials", null);
        }
        if (!passwordEncoder.matches(request.getClientSecret(), credential.getClientSecretHash())) {
            return ApiResponse.of(OpenApiConstants.CODE_AUTH_FAILED, "Invalid client credentials", null);
        }

        PartnerDO partner = partnerRepository.findByPartnerId(credential.getPartnerId());
        if (partner == null || !PartnerConstants.STATUS_ACTIVE.equals(partner.getStatus())) {
            return ApiResponse.of(OpenApiConstants.CODE_AUTH_FAILED, "Partner 未激活或不存在", null);
        }

        List<String> capabilityCodes = partnerRepository.listCapabilities(partner.getPartnerId());

        long expiresInSeconds = properties.getToken().getExpiresInSeconds();
        long nowEpoch = System.currentTimeMillis() / 1000;
        long expiresAt = nowEpoch + expiresInSeconds;

        PartnerTokenIntrospectResponse cache = new PartnerTokenIntrospectResponse();
        cache.setSubjectType(OpenApiConstants.SUBJECT_TYPE_PARTNER);
        cache.setPartnerId(partner.getPartnerId());
        cache.setCapabilities(capabilityCodes);
        cache.setClientId(credential.getClientId());
        cache.setIssuedAt(nowEpoch);
        cache.setExpiresAt(expiresAt);

        String accessToken = UUID.randomUUID().toString();
        tokenRedisStore.saveToken(accessToken, cache, expiresInSeconds);

        PartnerTokenIssueResponse response = new PartnerTokenIssueResponse();
        response.setAccessToken(accessToken);
        response.setTokenType("Bearer");
        response.setExpiresIn((int) expiresInSeconds);
        response.setPartnerId(partner.getPartnerId());
        return ApiResponse.ok(response);
    }

    @Override
    public ApiResponse<PartnerTokenIntrospectResponse> introspect(PartnerTokenIntrospectRequest request) {
        if (request == null || !StringUtils.hasText(request.getToken())) {
            return ApiResponse.of(OpenApiConstants.CODE_AUTH_FAILED, "token 无效", null);
        }
        String bearer = stripBearerPrefix(request.getToken());
        PartnerTokenIntrospectResponse cached = tokenRedisStore.getByToken(bearer);
        if (cached == null) {
            return ApiResponse.of(OpenApiConstants.CODE_AUTH_FAILED, "token 无效或已过期", null);
        }
        Long expiresAt = cached.getExpiresAt();
        if (expiresAt != null && expiresAt > 0 && expiresAt < System.currentTimeMillis() / 1000) {
            return ApiResponse.of(OpenApiConstants.CODE_AUTH_FAILED, "token 已过期", null);
        }
        if (!OpenApiConstants.SUBJECT_TYPE_PARTNER.equalsIgnoreCase(cached.getSubjectType())) {
            return ApiResponse.of(OpenApiConstants.CODE_AUTH_FAILED, "subjectType 无效", null);
        }
        return ApiResponse.ok(cached);
    }

    private String stripBearerPrefix(String token) {
        String trimmed = token.trim();
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return trimmed.substring(7).trim();
        }
        return trimmed;
    }
}

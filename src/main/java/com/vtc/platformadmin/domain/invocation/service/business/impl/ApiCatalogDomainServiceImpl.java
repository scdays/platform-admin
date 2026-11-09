package com.vtc.platformadmin.domain.invocation.service.business.impl;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.domain.service.DomainServiceImpl;
import com.vtc.platformadmin.domain.invocation.OpenApiConstants;
import com.vtc.platformadmin.domain.invocation.OpenApiException;
import com.vtc.platformadmin.domain.invocation.model.entity.ApiOperationDO;
import com.vtc.platformadmin.domain.invocation.model.query.ApiOperationAdminQuery;
import com.vtc.platformadmin.domain.invocation.repository.IApiOperationRepository;
import com.vtc.platformadmin.domain.invocation.service.business.IApiCatalogDomainService;
import org.springframework.stereotype.Service;

@Service
public class ApiCatalogDomainServiceImpl
        extends DomainServiceImpl<IApiOperationRepository, ApiOperationDO>
        implements IApiCatalogDomainService {

    private static final String STATUS_PUBLISHED = "PUBLISHED";

    private final IApiOperationRepository apiOperationRepository;

    public ApiCatalogDomainServiceImpl(IApiOperationRepository apiOperationRepository) {
        this.apiOperationRepository = apiOperationRepository;
    }

    @Override
    public void requirePublished(String operationId) {
        ApiOperationDO op = apiOperationRepository.findByOperationId(operationId);
        if (op == null) {
            throw new OpenApiException(OpenApiConstants.CODE_ENGINE_FAILED,
                    "API operation 未登记: " + operationId);
        }
        if (!STATUS_PUBLISHED.equals(op.getStatus())) {
            throw new OpenApiException(OpenApiConstants.CODE_ENGINE_FAILED,
                    "API operation 未发布: " + operationId);
        }
    }

    @Override
    public PageInfo<ApiOperationDO> pageApiOperations(ApiOperationAdminQuery query) {
        return apiOperationRepository.pageApiOperations(query);
    }
}

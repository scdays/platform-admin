package com.vtc.platformadmin.app.service.impl;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.app.service.AppServiceImpl;
import com.botany.spore.ddd.infra.utils.convertor.ConvertHelper;
import com.vtc.platformadmin.app.service.IApiCatalogAdminAppService;
import com.vtc.platformadmin.domain.invocation.model.entity.ApiOperationDO;
import com.vtc.platformadmin.domain.invocation.model.query.ApiOperationAdminQuery;
import com.vtc.platformadmin.domain.invocation.service.business.IApiCatalogDomainService;
import com.vtc.platformadmin.ui.invocation.dto.ApiOperationDTO;
import com.vtc.platformadmin.ui.invocation.dto.ApiOperationPageDto;
import org.springframework.stereotype.Service;

@Service
public class ApiCatalogAdminAppServiceImpl
        extends AppServiceImpl<IApiCatalogDomainService, ApiOperationDO, ApiOperationDTO>
        implements IApiCatalogAdminAppService {

    @Override
    public ApiOperationPageDto listApiOperations(PageInfo<ApiOperationDTO> pageInfo,
                                                  String requiredCapability,
                                                  String status,
                                                  String openapiTag,
                                                  String domain,
                                                  String operationId,
                                                  String keyword) {
        ApiOperationAdminQuery query = new ApiOperationAdminQuery();
        query.setRequiredCapability(requiredCapability);
        query.setStatus(status);
        query.setOpenapiTag(openapiTag);
        query.setDomain(domain);
        query.setOperationId(operationId);
        query.setKeyword(keyword);
        query.setPage((int) pageInfo.getCurrent());
        query.setSize((int) pageInfo.getSize());

        PageInfo<ApiOperationDO> resultPage = domainService.pageApiOperations(query);
        ApiOperationPageDto dto = new ApiOperationPageDto();
        dto.setItems(ConvertHelper.convertList(resultPage.getRecords(), ApiOperationDTO.class));
        dto.setPage((int) resultPage.getCurrent());
        dto.setSize((int) resultPage.getSize());
        dto.setTotal(resultPage.getTotal());
        return dto;
    }
}

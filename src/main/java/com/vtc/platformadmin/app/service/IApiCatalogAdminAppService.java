package com.vtc.platformadmin.app.service;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.app.service.IAppService;
import com.vtc.platformadmin.ui.invocation.dto.ApiOperationDTO;
import com.vtc.platformadmin.ui.invocation.dto.ApiOperationPageDto;

public interface IApiCatalogAdminAppService extends IAppService<ApiOperationDTO> {

    ApiOperationPageDto listApiOperations(PageInfo<ApiOperationDTO> pageInfo,
                                           String requiredCapability,
                                           String status,
                                           String openapiTag,
                                           String domain,
                                           String operationId,
                                           String keyword);
}

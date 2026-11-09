package com.vtc.platformadmin.domain.invocation.service.business;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.domain.service.IDomainService;
import com.vtc.platformadmin.domain.invocation.model.entity.ApiOperationDO;
import com.vtc.platformadmin.domain.invocation.model.query.ApiOperationAdminQuery;

public interface IApiCatalogDomainService extends IDomainService<ApiOperationDO> {

    /**
     * 检查 operation 是否已发布。
     */
    void requirePublished(String operationId);

    PageInfo<ApiOperationDO> pageApiOperations(ApiOperationAdminQuery query);
}

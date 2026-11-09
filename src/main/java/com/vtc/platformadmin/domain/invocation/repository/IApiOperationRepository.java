package com.vtc.platformadmin.domain.invocation.repository;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.domain.repository.IDatabaseRepository;
import com.vtc.platformadmin.domain.invocation.model.entity.ApiOperationDO;
import com.vtc.platformadmin.domain.invocation.model.query.ApiOperationAdminQuery;

public interface IApiOperationRepository extends IDatabaseRepository<ApiOperationDO> {

    ApiOperationDO findByOperationId(String operationId);

    PageInfo<ApiOperationDO> pageApiOperations(ApiOperationAdminQuery query);
}

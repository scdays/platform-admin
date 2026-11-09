package com.vtc.platformadmin.domain.invocation.model.convert;

import com.botany.spore.ddd.domain.model.convertor.IDomainConvertor;
import com.vtc.platformadmin.domain.invocation.model.entity.ApiInvocationDO;
import com.vtc.platformadmin.infra.dao.po.ApiInvocationPO;
import org.springframework.stereotype.Component;

/**
 * ApiInvocation DO ↔ PO 转换（ConvertHelper 注册）。
 *
 * @author asset-security
 */
@Component
public class ApiInvocationDomainConvertor implements IDomainConvertor<ApiInvocationDO, ApiInvocationPO> {

    @Override
    public ApiInvocationPO doToPo(ApiInvocationDO domainObject) {
        return IDomainConvertor.super.doToPo(domainObject);
    }

    @Override
    public ApiInvocationDO poToDo(ApiInvocationPO po) {
        return IDomainConvertor.super.poToDo(po);
    }
}

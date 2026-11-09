package com.vtc.platformadmin.domain.partner.model.convert;

import com.botany.spore.ddd.domain.model.convertor.IDomainConvertor;
import com.vtc.platformadmin.domain.partner.model.entity.PartnerCredentialDO;
import com.vtc.platformadmin.infra.dao.po.PartnerCredentialPO;
import org.springframework.stereotype.Component;

/**
 * PartnerCredential DO ↔ PO 转换（ConvertHelper 注册）。
 *
 * @author asset-security
 */
@Component
public class PartnerCredentialDomainConvertor implements IDomainConvertor<PartnerCredentialDO, PartnerCredentialPO> {

    @Override
    public PartnerCredentialPO doToPo(PartnerCredentialDO domainObject) {
        return IDomainConvertor.super.doToPo(domainObject);
    }

    @Override
    public PartnerCredentialDO poToDo(PartnerCredentialPO po) {
        return IDomainConvertor.super.poToDo(po);
    }
}

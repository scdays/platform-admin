package com.vtc.platformadmin.domain.partner.model.convert;

import com.botany.spore.ddd.domain.model.convertor.IDomainConvertor;
import com.vtc.platformadmin.domain.partner.model.entity.PartnerCapabilityDO;
import com.vtc.platformadmin.infra.dao.po.PartnerCapabilityPO;
import org.springframework.stereotype.Component;

/**
 * PartnerCapability DO ↔ PO 转换（ConvertHelper 注册）。
 *
 * @author asset-security
 */
@Component
public class PartnerCapabilityDomainConvertor implements IDomainConvertor<PartnerCapabilityDO, PartnerCapabilityPO> {

    @Override
    public PartnerCapabilityPO doToPo(PartnerCapabilityDO domainObject) {
        return IDomainConvertor.super.doToPo(domainObject);
    }

    @Override
    public PartnerCapabilityDO poToDo(PartnerCapabilityPO po) {
        return IDomainConvertor.super.poToDo(po);
    }
}

package com.vtc.platformadmin.domain.partner.model.convert;

import com.botany.spore.ddd.domain.model.convertor.IDomainConvertor;
import com.vtc.platformadmin.domain.partner.model.entity.PartnerWebhookConfigDO;
import com.vtc.platformadmin.infra.dao.po.PartnerWebhookConfigPO;
import org.springframework.stereotype.Component;

@Component
public class PartnerWebhookConfigDomainConvertor implements IDomainConvertor<PartnerWebhookConfigDO, PartnerWebhookConfigPO> {

    @Override
    public PartnerWebhookConfigPO doToPo(PartnerWebhookConfigDO domainObject) {
        return IDomainConvertor.super.doToPo(domainObject);
    }

    @Override
    public PartnerWebhookConfigDO poToDo(PartnerWebhookConfigPO po) {
        return IDomainConvertor.super.poToDo(po);
    }
}

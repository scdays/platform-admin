package com.vtc.platformadmin.domain.webhook.model.convert;

import com.botany.spore.ddd.domain.model.convertor.IDomainConvertor;
import com.vtc.platformadmin.domain.webhook.model.entity.WebhookDeliveryLogDO;
import com.vtc.platformadmin.infra.dao.po.WebhookDeliveryLogPO;
import org.springframework.stereotype.Component;

/**
 * WebhookDeliveryLog DO ↔ PO 转换（ConvertHelper 注册）。
 *
 * @author asset-security
 */
@Component
public class WebhookDeliveryLogDomainConvertor implements IDomainConvertor<WebhookDeliveryLogDO, WebhookDeliveryLogPO> {

    @Override
    public WebhookDeliveryLogPO doToPo(WebhookDeliveryLogDO domainObject) {
        return IDomainConvertor.super.doToPo(domainObject);
    }

    @Override
    public WebhookDeliveryLogDO poToDo(WebhookDeliveryLogPO po) {
        return IDomainConvertor.super.poToDo(po);
    }
}

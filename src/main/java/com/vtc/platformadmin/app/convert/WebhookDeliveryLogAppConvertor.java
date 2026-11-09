package com.vtc.platformadmin.app.convert;

import com.botany.spore.ddd.app.convertor.IAppConvertor;
import com.vtc.platformadmin.domain.webhook.model.entity.WebhookDeliveryLogDO;
import com.vtc.platformadmin.ui.dto.WebhookDeliveryLogDTO;
import org.springframework.stereotype.Component;

@Component
public class WebhookDeliveryLogAppConvertor implements IAppConvertor<WebhookDeliveryLogDTO, WebhookDeliveryLogDO> {

    @Override
    public WebhookDeliveryLogDTO doToDto(WebhookDeliveryLogDO domainObject) {
        return IAppConvertor.super.doToDto(domainObject);
    }

    @Override
    public WebhookDeliveryLogDO dtoToDo(WebhookDeliveryLogDTO dto) {
        return IAppConvertor.super.dtoToDo(dto);
    }
}

package com.vtc.platformadmin.app.convert;

import com.botany.spore.ddd.app.convertor.IAppConvertor;
import com.vtc.platformadmin.domain.partner.model.entity.PartnerDO;
import com.vtc.platformadmin.ui.dto.PartnerDTO;
import org.springframework.stereotype.Component;

/**
 * Partner DTO ↔ DO 转换（ConvertHelper 注册）。
 *
 * @author asset-security
 */
@Component
public class PartnerAppConvertor implements IAppConvertor<PartnerDTO, PartnerDO> {

    @Override
    public PartnerDTO doToDto(PartnerDO domainObject) {
        return IAppConvertor.super.doToDto(domainObject);
    }

    @Override
    public PartnerDO dtoToDo(PartnerDTO dto) {
        return IAppConvertor.super.dtoToDo(dto);
    }
}

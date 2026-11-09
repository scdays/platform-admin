package com.vtc.platformadmin.app.convert;

import com.botany.spore.ddd.app.convertor.IAppConvertor;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventDeadLetterDO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventDeadLetterDTO;
import org.springframework.stereotype.Component;

/**
 * 死信事件 DO ↔ DTO 转换（ConvertHelper 注册）。
 *
 * @author asset-security
 */
@Component
public class EventDeadLetterAppConvertor implements IAppConvertor<EventDeadLetterDTO, EventDeadLetterDO> {

    @Override
    public EventDeadLetterDTO doToDto(EventDeadLetterDO domainObject) {
        return IAppConvertor.super.doToDto(domainObject);
    }

    @Override
    public EventDeadLetterDO dtoToDo(EventDeadLetterDTO dto) {
        return IAppConvertor.super.dtoToDo(dto);
    }
}

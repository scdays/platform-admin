package com.vtc.platformadmin.app.convert;

import com.botany.spore.ddd.app.convertor.IAppConvertor;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventConsumeTraceDO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventConsumeTraceDTO;
import org.springframework.stereotype.Component;

/**
 * 事件消费轨迹 DO ↔ DTO 转换（ConvertHelper 注册）。
 *
 * @author asset-security
 */
@Component
public class EventConsumeTraceAppConvertor implements IAppConvertor<EventConsumeTraceDTO, EventConsumeTraceDO> {

    @Override
    public EventConsumeTraceDTO doToDto(EventConsumeTraceDO domainObject) {
        return IAppConvertor.super.doToDto(domainObject);
    }

    @Override
    public EventConsumeTraceDO dtoToDo(EventConsumeTraceDTO dto) {
        return IAppConvertor.super.dtoToDo(dto);
    }
}

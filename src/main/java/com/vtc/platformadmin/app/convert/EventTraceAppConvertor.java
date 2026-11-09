package com.vtc.platformadmin.app.convert;

import com.botany.spore.ddd.app.convertor.IAppConvertor;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventTraceDO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventTraceDTO;
import org.springframework.stereotype.Component;

/**
 * 事件发布轨迹 DO ↔ DTO 转换（ConvertHelper 注册）。
 *
 * @author asset-security
 */
@Component
public class EventTraceAppConvertor implements IAppConvertor<EventTraceDTO, EventTraceDO> {

    @Override
    public EventTraceDTO doToDto(EventTraceDO domainObject) {
        return IAppConvertor.super.doToDto(domainObject);
    }

    @Override
    public EventTraceDO dtoToDo(EventTraceDTO dto) {
        return IAppConvertor.super.dtoToDo(dto);
    }
}

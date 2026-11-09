package com.vtc.platformadmin.ui.eventcenter.dto;

import lombok.Data;

import java.util.List;

/**
 * 事件详情聚合 VO（仅持有 UI 层 DTO，不泄漏领域 DO）。
 *
 * @author asset-security
 */
@Data
public class EventDetailVO {

    private EventTraceDTO trace;
    private List<EventConsumeTraceDTO> consumeTraces;
    private EventDeadLetterDTO deadLetter;
}

package com.vtc.platformadmin.app.service.eventcenter;

import com.botany.spore.core.page.PageInfo;
import com.vtc.platformadmin.ui.eventcenter.dto.EventCatalogDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventDeadLetterDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventDeadLetterQueryParams;
import com.vtc.platformadmin.ui.eventcenter.dto.EventDetailVO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventAuditLogDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventAuditLogQueryParams;
import com.vtc.platformadmin.ui.eventcenter.dto.EventSchemaDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventSubscriptionDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventTraceDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventTraceQueryParams;
import com.vtc.platformadmin.ui.eventcenter.dto.ReplayRequestDTO;

import java.util.List;

/**
 * EventCenter 应用服务接口。
 * <p>
 * 聚合查询服务（跨 3 个 Repository），返回 UI 层 DTO，不返回 DO。
 *
 * @author asset-security
 */
public interface IEventCenterAppService {

    EventDetailVO getEventDetail(String eventId);

    PageInfo<EventTraceDTO> pageTraces(EventTraceQueryParams params);

    PageInfo<EventDeadLetterDTO> pageDeadLetters(EventDeadLetterQueryParams params);

    List<EventCatalogDTO> listCatalogs();

    List<EventSchemaDTO> listSchemas();

    List<EventSubscriptionDTO> listSubscriptions();

    String requestReplay(ReplayRequestDTO request);

    PageInfo<EventAuditLogDTO> pageAuditLogs(EventAuditLogQueryParams params);

    void recordAudit(String operatorId, String operatorName, String operationType,
                     String targetType, String targetId, String result, String detail, String clientIp);

    /**
     * 获取事件原始报文（payload JSON），供前端「查看报文」弹窗使用。
     */
    String getEventPayload(String eventId);
}

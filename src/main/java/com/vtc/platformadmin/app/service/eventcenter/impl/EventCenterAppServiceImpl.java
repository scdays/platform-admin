package com.vtc.platformadmin.app.service.eventcenter.impl;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.infra.utils.convertor.ConvertHelper;
import com.vtc.asset.security.platform.eventbus.admin.catalog.EventCatalogDO;
import com.vtc.asset.security.platform.eventbus.admin.catalog.IEventCatalogRepository;
import com.vtc.asset.security.platform.eventbus.admin.replay.EventReplayRecordDO;
import com.vtc.asset.security.platform.eventbus.admin.replay.IEventReplayRecordRepository;
import com.vtc.asset.security.platform.eventbus.admin.subscription.EventSubscriptionDO;
import com.vtc.asset.security.platform.eventbus.admin.subscription.IEventSubscriptionRepository;
import com.vtc.platformadmin.app.service.eventcenter.IEventCenterAppService;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventAuditLogDO;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventConsumeTraceDO;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventDeadLetterDO;
import com.vtc.platformadmin.domain.eventcenter.model.entity.EventTraceDO;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventAuditLogRepository;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventConsumeTraceRepository;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventDeadLetterRepository;
import com.vtc.platformadmin.domain.eventcenter.repository.IEventTraceRepository;
import com.vtc.platformadmin.ui.eventcenter.dto.EventAuditLogDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventAuditLogQueryParams;
import com.vtc.platformadmin.ui.eventcenter.dto.EventCatalogDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventConsumeTraceDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventDeadLetterDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventDeadLetterQueryParams;
import com.vtc.platformadmin.ui.eventcenter.dto.EventDetailVO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventSchemaDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventSubscriptionDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventTraceDTO;
import com.vtc.platformadmin.ui.eventcenter.dto.EventTraceQueryParams;
import com.vtc.platformadmin.ui.eventcenter.dto.ReplayRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * EventCenter 应用服务实现。
 * <p>
 * 聚合查询服务（跨 3 个 Repository），负责 DO/DTO 转换，向 UI 返回 DTO。
 * 直接注入 Repository（非 Mapper），不违反分层规则。
 *
 * @author asset-security
 */
@Service
public class EventCenterAppServiceImpl implements IEventCenterAppService {

    private final IEventTraceRepository eventTraceRepository;
    private final IEventConsumeTraceRepository eventConsumeTraceRepository;
    private final IEventDeadLetterRepository eventDeadLetterRepository;
    private final IEventCatalogRepository eventCatalogRepository;
    private final IEventSubscriptionRepository eventSubscriptionRepository;
    private final IEventReplayRecordRepository eventReplayRecordRepository;
    private final IEventAuditLogRepository eventAuditLogRepository;

    public EventCenterAppServiceImpl(IEventTraceRepository eventTraceRepository,
                                     IEventConsumeTraceRepository eventConsumeTraceRepository,
                                     IEventDeadLetterRepository eventDeadLetterRepository,
                                     IEventCatalogRepository eventCatalogRepository,
                                     IEventSubscriptionRepository eventSubscriptionRepository,
                                     IEventReplayRecordRepository eventReplayRecordRepository,
                                     IEventAuditLogRepository eventAuditLogRepository) {
        this.eventTraceRepository = eventTraceRepository;
        this.eventConsumeTraceRepository = eventConsumeTraceRepository;
        this.eventDeadLetterRepository = eventDeadLetterRepository;
        this.eventCatalogRepository = eventCatalogRepository;
        this.eventSubscriptionRepository = eventSubscriptionRepository;
        this.eventReplayRecordRepository = eventReplayRecordRepository;
        this.eventAuditLogRepository = eventAuditLogRepository;
    }

    @Override
    public EventDetailVO getEventDetail(String eventId) {
        if (!StringUtils.hasText(eventId)) {
            return null;
        }
        EventDetailVO vo = new EventDetailVO();
        EventTraceDO trace = eventTraceRepository.findByEventId(eventId);
        vo.setTrace(ConvertHelper.convert(trace, EventTraceDTO.class));
        List<EventConsumeTraceDO> consumeTraces = eventConsumeTraceRepository.findByEventId(eventId);
        vo.setConsumeTraces(ConvertHelper.convertList(consumeTraces, EventConsumeTraceDTO.class));
        EventDeadLetterDO deadLetter = eventDeadLetterRepository.findByEventId(eventId);
        vo.setDeadLetter(ConvertHelper.convert(deadLetter, EventDeadLetterDTO.class));
        return vo;
    }

    @Override
    public PageInfo<EventTraceDTO> pageTraces(EventTraceQueryParams params) {
        PageInfo<EventTraceDO> doPage = eventTraceRepository.pageTraces(params);
        return ConvertHelper.convertPageInfo(doPage, EventTraceDTO.class);
    }

    @Override
    public PageInfo<EventDeadLetterDTO> pageDeadLetters(EventDeadLetterQueryParams params) {
        PageInfo<EventDeadLetterDO> doPage = eventDeadLetterRepository.pageDeadLetters(params);
        return ConvertHelper.convertPageInfo(doPage, EventDeadLetterDTO.class);
    }

    @Override
    public List<EventCatalogDTO> listCatalogs() {
        List<EventCatalogDO> catalogs = eventCatalogRepository.findAll();
        if (catalogs == null || catalogs.isEmpty()) {
            return Collections.emptyList();
        }
        return catalogs.stream().map(this::toCatalogDto).collect(Collectors.toList());
    }

    @Override
    public List<EventSchemaDTO> listSchemas() {
        // Schema 暂无独立列表端点需求，按 catalog 维度聚合 eventType/version
        List<EventCatalogDO> catalogs = eventCatalogRepository.findAll();
        if (catalogs == null || catalogs.isEmpty()) {
            return Collections.emptyList();
        }
        List<EventSchemaDTO> list = new ArrayList<>();
        for (EventCatalogDO catalog : catalogs) {
            EventSchemaDTO dto = new EventSchemaDTO();
            dto.setEventType(catalog.getEventType());
            dto.setSchemaVersion(catalog.getVersion());
            list.add(dto);
        }
        return list;
    }

    @Override
    public List<EventSubscriptionDTO> listSubscriptions() {
        List<EventSubscriptionDO> subscriptions = eventSubscriptionRepository.findAll();
        if (subscriptions == null || subscriptions.isEmpty()) {
            return Collections.emptyList();
        }
        return subscriptions.stream().map(this::toSubscriptionDto).collect(Collectors.toList());
    }

    @Override
    public String requestReplay(ReplayRequestDTO request) {
        // 为每个待重放事件创建一条 replay 记录，返回首个 replayId
        String replayId = "replay-" + UUID.randomUUID().toString().replace("-", "");
        if (request == null || request.getEventIds() == null || request.getEventIds().isEmpty()) {
            return replayId;
        }
        for (String eventId : request.getEventIds()) {
            if (!StringUtils.hasText(eventId)) {
                continue;
            }
            EventReplayRecordDO record = new EventReplayRecordDO();
            record.setReplayId("replay-" + UUID.randomUUID().toString().replace("-", ""));
            record.setEventId(eventId);
            record.setEventType(request.getEventType());
            record.setReplayBy("platform-admin");
            record.setReplayReason(request.getRemark());
            record.setStatus("PENDING");
            record.setCreatedAt(System.currentTimeMillis());
            try {
                eventReplayRecordRepository.save(record);
            } catch (Exception ignored) {
                // 单条记录失败不阻断整体重放申请
            }
        }
        return replayId;
    }

    private EventCatalogDTO toCatalogDto(EventCatalogDO catalog) {
        EventCatalogDTO dto = new EventCatalogDTO();
        dto.setEventType(catalog.getEventType());
        dto.setDomain(catalog.getDomain());
        dto.setDescription(catalog.getDescription());
        dto.setSourceService(catalog.getSourceService());
        return dto;
    }

    @Override
    public PageInfo<EventAuditLogDTO> pageAuditLogs(EventAuditLogQueryParams params) {
        PageInfo<EventAuditLogDO> doPage = eventAuditLogRepository.pageAuditLogs(params);
        return ConvertHelper.convertPageInfo(doPage, EventAuditLogDTO.class);
    }

    @Override
    public void recordAudit(String operatorId, String operatorName, String operationType,
                            String targetType, String targetId, String result, String detail, String clientIp) {
        EventAuditLogDO auditLog = new EventAuditLogDO();
        auditLog.setOperatorId(operatorId);
        auditLog.setOperatorName(operatorName);
        auditLog.setOperationType(operationType);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);
        auditLog.setResult(result);
        auditLog.setDetail(detail);
        auditLog.setClientIp(clientIp);
        auditLog.setCreatedTime(new java.util.Date());
        auditLog.setUpdatedTime(new java.util.Date());
        try {
            eventAuditLogRepository.save(auditLog);
        } catch (Exception ignored) {
            // 审计日志写入失败不阻断主流程
        }
    }

    @Override
    public String getEventPayload(String eventId) {
        if (!StringUtils.hasText(eventId)) {
            return null;
        }
        EventTraceDO trace = eventTraceRepository.findByEventId(eventId);
        if (trace == null) {
            return null;
        }
        return trace.getPayload();
    }

    private EventSubscriptionDTO toSubscriptionDto(EventSubscriptionDO subscription) {
        EventSubscriptionDTO dto = new EventSubscriptionDTO();
        dto.setSubscriptionId(subscription.getSubscriptionId());
        dto.setSubscriberService(subscription.getSubscriberService());
        dto.setConsumerGroup(subscription.getConsumerGroup());
        dto.setTopic(subscription.getTopic());
        dto.setHandler(subscription.getHandler());
        dto.setStatus(subscription.getStatus());
        // eventTypes 按 eventType 单值聚合为单元素列表
        if (StringUtils.hasText(subscription.getEventType())) {
            List<String> eventTypes = new ArrayList<>();
            eventTypes.add(subscription.getEventType());
            dto.setEventTypes(eventTypes);
        }
        return dto;
    }
}

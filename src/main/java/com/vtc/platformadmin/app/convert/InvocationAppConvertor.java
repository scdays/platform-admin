package com.vtc.platformadmin.app.convert;

import com.botany.spore.ddd.app.convertor.IAppConvertor;
import com.vtc.platformadmin.domain.invocation.model.entity.ApiInvocationDO;
import com.vtc.platformadmin.domain.webhook.model.entity.WebhookDeliveryLogDO;
import com.vtc.platformadmin.ui.dto.PushRecordDTO;
import com.vtc.platformadmin.ui.dto.WebhookDeliveryLogDTO;
import com.vtc.platformadmin.ui.dto.WebhookDeliveryLogDetailDTO;
import com.vtc.platformadmin.ui.invocation.dto.InvocationDTO;
import com.vtc.platformadmin.ui.invocation.dto.InvocationDetailDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Invocation DTO ↔ DO 转换（ConvertHelper 注册）。
 *
 * @author asset-security
 */
@Component
public class InvocationAppConvertor implements IAppConvertor<InvocationDTO, ApiInvocationDO> {

    @Override
    public InvocationDTO doToDto(ApiInvocationDO domainObject) {
        return IAppConvertor.super.doToDto(domainObject);
    }

    @Override
    public ApiInvocationDO dtoToDo(InvocationDTO dto) {
        return IAppConvertor.super.dtoToDo(dto);
    }

    public InvocationDTO toInvocationDto(ApiInvocationDO row) {
        // 不调 ConvertHelper.convert(row, InvocationDTO.class)：本类即注册的 IConvertor<ApiInvocationDO, InvocationDTO>，
        // getMethod 会自匹配本方法 → 无限递归 → StackOverflow。走基类 doToDto（BeanUtils 拷贝）。
        return doToDto(row);
    }

    public InvocationDetailDTO toInvocationDetailDto(ApiInvocationDO row) {
        // 无注册的 IConvertor<ApiInvocationDO, InvocationDetailDTO>（子类不匹配 isIConvertor），
        // ConvertHelper.convert 会抛 getConvertClass。手动 new + BeanUtils。
        InvocationDetailDTO dto = new InvocationDetailDTO();
        BeanUtils.copyProperties(row, dto);
        return dto;
    }

    public WebhookDeliveryLogDTO toWebhookDeliveryLogDto(WebhookDeliveryLogDO row) {
        // 无稳定的 IConvertor<WebhookDeliveryLogDO, WebhookDeliveryLogDTO> 注册
        // （WebhookDeliveryLogAppConvertor 只注册 webhook 包且 dest 是 DTO 父类），ConvertHelper 易抛 getConvertClass。
        // 统一走手动 new + BeanUtils，规避注册依赖与自匹配风险。
        if (row == null) {
            return null;
        }
        WebhookDeliveryLogDTO dto = new WebhookDeliveryLogDTO();
        BeanUtils.copyProperties(row, dto);
        return dto;
    }

    public List<WebhookDeliveryLogDTO> toCollapsedWebhookDeliveryLogDtoList(List<? extends WebhookDeliveryLogDO> rows) {
        return rows.stream().map(this::toWebhookDeliveryLogDto).collect(Collectors.toList());
    }

    public WebhookDeliveryLogDetailDTO toWebhookDeliveryLogDetailDto(WebhookDeliveryLogDO row) {
        // 无注册的 IConvertor<*, WebhookDeliveryLogDetailDTO>，手动 new + BeanUtils。
        if (row == null) {
            return null;
        }
        WebhookDeliveryLogDetailDTO dto = new WebhookDeliveryLogDetailDTO();
        BeanUtils.copyProperties(row, dto);
        return dto;
    }

    public PushRecordDTO toPushRecordDto(WebhookDeliveryLogDO row, int attemptCount, boolean downloadable) {
        if (row == null) {
            return null;
        }
        PushRecordDTO dto = new PushRecordDTO();
        dto.setId(row.getId());
        dto.setEventId(row.getEventId());
        dto.setPartnerId(row.getPartnerId());
        dto.setEventType(row.getEventType());
        dto.setStatus(row.getStatus());
        dto.setHttpStatus(row.getHttpStatus());
        dto.setAttemptCount(attemptCount);
        dto.setTriggerSource(row.getTriggerSource());
        dto.setCreatedAt(row.getCreatedAt());
        dto.setDownloadable(downloadable);
        return dto;
    }
}

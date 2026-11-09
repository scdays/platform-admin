package com.vtc.platformadmin.app.convert;

import com.vtc.platformadmin.domain.invocation.model.entity.ApiInvocationDO;
import com.vtc.platformadmin.ui.invocation.dto.InvocationDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdminGovernanceAppConvertor {

    public InvocationDTO toInvocationDto(ApiInvocationDO row) {
        if (row == null) {
            return null;
        }
        InvocationDTO dto = new InvocationDTO();
        dto.setInvocationId(row.getInvocationId());
        dto.setRequestId(row.getRequestId());
        dto.setPartnerId(row.getPartnerId());
        dto.setOperationId(row.getOperationId());
        dto.setHttpMethod(row.getHttpMethod());
        dto.setResponseCode(row.getResponseCode());
        dto.setHttpStatus(row.getHttpStatus());
        dto.setLatencyMs(row.getLatencyMs());
        dto.setRequestPath(row.getRequestPath());
        dto.setResourceType(row.getResourceType());
        dto.setResourceId(row.getResourceId());
        dto.setCaseId(row.getCaseId());
        dto.setErrorMessage(row.getErrorMessage());
        dto.setStartedAt(row.getStartedAt());
        dto.setFinishedAt(row.getFinishedAt());
        return dto;
    }

    public List<InvocationDTO> toInvocationDtoList(List<ApiInvocationDO> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return Collections.emptyList();
        }
        return rows.stream().map(this::toInvocationDto).collect(Collectors.toList());
    }
}

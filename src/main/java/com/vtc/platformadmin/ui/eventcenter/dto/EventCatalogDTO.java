package com.vtc.platformadmin.ui.eventcenter.dto;

import com.botany.spore.ddd.ui.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 事件目录 UI 数据传输对象。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventCatalogDTO extends BaseDTO {

    private String eventType;
    private String domain;
    private String description;
    private String sourceService;
    private String schemaVersion;
    private Date registeredAt;
    private Date lastPublishedAt;
    private Integer publishCount;
}

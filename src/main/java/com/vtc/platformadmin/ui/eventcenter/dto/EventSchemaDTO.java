package com.vtc.platformadmin.ui.eventcenter.dto;

import com.botany.spore.ddd.ui.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.Map;

/**
 * 事件 Schema UI 数据传输对象。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventSchemaDTO extends BaseDTO {

    private String eventType;
    private String schemaVersion;
    private String schemaFormat;
    private String schemaContent;
    private Map<String, Object> metadata;
    private Date registeredAt;
    private String registeredBy;
}

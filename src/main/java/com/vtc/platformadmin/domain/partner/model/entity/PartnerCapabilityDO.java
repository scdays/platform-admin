package com.vtc.platformadmin.domain.partner.model.entity;

import com.botany.spore.ddd.domain.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Partner Capability 领域对象。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PartnerCapabilityDO extends BaseDO {

    private Long id;

    private String partnerId;

    private String capability;

    private Date createdAt;
}

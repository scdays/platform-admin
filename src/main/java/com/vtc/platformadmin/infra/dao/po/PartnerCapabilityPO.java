package com.vtc.platformadmin.infra.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.botany.spore.mybatis.pojo.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * PartnerCapability 持久化对象。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("partner_capability")
public class PartnerCapabilityPO extends BasePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String partnerId;

    private String capability;

    private Date createdAt;
}

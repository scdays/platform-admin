package com.vtc.platformadmin.infra.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.botany.spore.mybatis.pojo.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * PartnerCredential 持久化对象。
 *
 * @author asset-security
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("partner_credential")
public class PartnerCredentialPO extends BasePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String partnerId;

    private String clientId;

    private String clientSecretHash;

    private String status;

    private Date expiresAt;

    private Date createdAt;

    private Date updatedAt;
}

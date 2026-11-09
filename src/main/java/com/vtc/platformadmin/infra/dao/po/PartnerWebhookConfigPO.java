package com.vtc.platformadmin.infra.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.botany.spore.mybatis.pojo.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("partner_webhook_config")
public class PartnerWebhookConfigPO extends BasePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String partnerId;

    private String callbackUrl;

    private String webhookSecretHash;

    /** Legacy plaintext column; cleared when webhookSecretHash is written */
    private String webhookSecret;
    /** 允许下载的 exportStage 逗号分隔；NULL 继承全局默认 */
    private String downloadableStages;

    private Date updatedAt;
}

package com.vtc.platformadmin.domain.partner.model.entity;

import com.botany.spore.ddd.domain.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Partner Webhook 配置（领域对象）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PartnerWebhookConfigDO extends BaseDO {

    private Long id;

    private String partnerId;

    private String callbackUrl;

    /** Webhook secret ciphertext (AES-GCM, encrypted at rest) */
    private String webhookSecretHash;

    /** Webhook signing secret (plaintext; populated by repository after decrypting webhookSecretHash) */
    private String webhookSecret;
    /** 允许下载的 exportStage 逗号分隔；NULL 继承全局默认 */
    private String downloadableStages;

    private Date updatedAt;
}

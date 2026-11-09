package com.vtc.platformadmin.domain.partner.service.business;

import com.botany.spore.core.page.PageInfo;
import com.botany.spore.ddd.domain.service.IDomainService;
import com.vtc.platformadmin.domain.partner.model.entity.PartnerCredentialDO;
import com.vtc.platformadmin.domain.partner.model.entity.PartnerDO;

import java.util.List;

/**
 * Partner 管理领域服务。
 */
public interface IPartnerDomainService extends IDomainService<PartnerDO> {

    PartnerDO createPartner(PartnerDO partner, List<String> capabilities, String defaultCallbackUrl, String downloadableStages);

    PartnerDO requireByPartnerId(String partnerId);

    PageInfo<PartnerDO> pagePartners(PageInfo<PartnerDO> pageInfo);

    PageInfo<PartnerDO> pagePartners(PageInfo<PartnerDO> pageInfo, String partnerId, String partnerName, String status);

    PartnerDO updatePartner(String partnerId, PartnerDO patch, List<String> capabilities, String defaultCallbackUrl, String downloadableStages);

    List<String> listCapabilities(String partnerId);

    String findCallbackUrl(String partnerId);

    String findDownloadableStages(String partnerId);

    /**
     * 解析 Partner 级有效可下载 exportStage 集合（CSV 解析 + 去空）。
     * <p>
     * 未配置（null/空）时返回默认白名单（TASK_COMPLETED / VERIFY_SCAN / VERIFY_FIX_SCAN），
     * 与 open-api-service ExportStage 默认语义一致。
     *
     * @param partnerId 合作方 ID
     * @return 有效可下载 exportStage 列表
     */
    List<String> resolveDownloadableStages(String partnerId);

    PartnerCredentialDO createCredential(String partnerId, String clientId, String clientSecretHash);

    List<PartnerCredentialDO> listCredentials(String partnerId);

    PartnerCredentialDO findCredentialByClientId(String clientId);

    String assignWebhookSecretIfAbsent(String partnerId);

    String rotateWebhookSecret(String partnerId);

    boolean hasWebhookSecret(String partnerId);
}

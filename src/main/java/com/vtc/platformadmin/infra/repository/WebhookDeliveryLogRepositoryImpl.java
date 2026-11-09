package com.vtc.platformadmin.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.botany.spore.ddd.infra.repository.DatabaseRepositoryImpl;
import com.botany.spore.ddd.infra.utils.convertor.ConvertHelper;
import com.vtc.platformadmin.domain.webhook.model.entity.WebhookDeliveryLogDO;
import com.vtc.platformadmin.domain.webhook.repository.IWebhookDeliveryLogRepository;
import com.vtc.platformadmin.infra.dao.WebhookDeliveryLogMapper;
import com.vtc.platformadmin.infra.dao.po.WebhookDeliveryLogPO;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * Webhook 投递日志仓储实现（继承 DatabaseRepositoryImpl，通用 CRUD 由基类提供，仅实现本领域特有方法）。
 * <p>
 * insert 等通用方法已由基类 save 提供，不再重复实现。
 *
 * @author asset-security
 */
@Repository
public class WebhookDeliveryLogRepositoryImpl
        extends DatabaseRepositoryImpl<WebhookDeliveryLogMapper, WebhookDeliveryLogDO, WebhookDeliveryLogPO>
        implements IWebhookDeliveryLogRepository {

    @Override
    public WebhookDeliveryLogDO findByEventId(String eventId) {
        if (!StringUtils.hasText(eventId)) {
            return null;
        }
        WebhookDeliveryLogPO po = baseMapper.selectOne(
                new LambdaQueryWrapper<WebhookDeliveryLogPO>()
                        .eq(WebhookDeliveryLogPO::getEventId, eventId));
        return po == null ? null : ConvertHelper.convert(po, WebhookDeliveryLogDO.class);
    }

    @Override
    public List<WebhookDeliveryLogDO> listByEventId(String eventId) {
        if (!StringUtils.hasText(eventId)) {
            return java.util.Collections.emptyList();
        }
        List<WebhookDeliveryLogPO> pos = baseMapper.selectList(
                new LambdaQueryWrapper<WebhookDeliveryLogPO>()
                        .eq(WebhookDeliveryLogPO::getEventId, eventId)
                        .orderByAsc(WebhookDeliveryLogPO::getCreatedAt));
        return ConvertHelper.convertList(pos, WebhookDeliveryLogDO.class);
    }

    @Override
    public List<WebhookDeliveryLogDO> findByStatusAndNextRetryAtLessThanEqual(String status, Date nextRetryAt) {
        List<WebhookDeliveryLogPO> pos = baseMapper.selectList(
                new LambdaQueryWrapper<WebhookDeliveryLogPO>()
                        .eq(WebhookDeliveryLogPO::getStatus, status)
                        .le(WebhookDeliveryLogPO::getNextRetryAt, nextRetryAt));
        return ConvertHelper.convertList(pos, WebhookDeliveryLogDO.class);
    }

    @Override
    public List<WebhookDeliveryLogDO> listByResource(String partnerId, String resourceType, String resourceId, int limit) {
        if (!StringUtils.hasText(partnerId) || !StringUtils.hasText(resourceId)) {
            return java.util.Collections.emptyList();
        }
        LambdaQueryWrapper<WebhookDeliveryLogPO> wrapper = new LambdaQueryWrapper<WebhookDeliveryLogPO>()
                .eq(WebhookDeliveryLogPO::getPartnerId, partnerId)
                .eq(WebhookDeliveryLogPO::getResourceId, resourceId)
                .orderByDesc(WebhookDeliveryLogPO::getCreatedAt);
        if (StringUtils.hasText(resourceType)) {
            wrapper.eq(WebhookDeliveryLogPO::getResourceType, resourceType);
        }
        if (limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        List<WebhookDeliveryLogPO> pos = baseMapper.selectList(wrapper);
        return ConvertHelper.convertList(pos, WebhookDeliveryLogDO.class);
    }

    @Override
    public com.botany.spore.core.page.PageInfo<WebhookDeliveryLogDO> pageByConditions(
            com.botany.spore.core.page.PageInfo<WebhookDeliveryLogDO> pageInfo,
            String partnerId, String eventType, String status,
            String resourceType, String resourceId) {
        com.botany.spore.core.page.PageInfo<WebhookDeliveryLogPO> poPage =
                ConvertHelper.convertPageInfo(pageInfo, WebhookDeliveryLogPO.class);
        LambdaQueryWrapper<WebhookDeliveryLogPO> wrapper = new LambdaQueryWrapper<WebhookDeliveryLogPO>();
        if (StringUtils.hasText(partnerId)) {
            wrapper.eq(WebhookDeliveryLogPO::getPartnerId, partnerId);
        }
        if (StringUtils.hasText(eventType)) {
            wrapper.eq(WebhookDeliveryLogPO::getEventType, eventType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(WebhookDeliveryLogPO::getStatus, status);
        }
        if (StringUtils.hasText(resourceType)) {
            wrapper.eq(WebhookDeliveryLogPO::getResourceType, resourceType);
        }
        if (StringUtils.hasText(resourceId)) {
            wrapper.eq(WebhookDeliveryLogPO::getResourceId, resourceId);
        }
        wrapper.orderByDesc(WebhookDeliveryLogPO::getCreatedAt);
        com.botany.spore.core.page.PageInfo<WebhookDeliveryLogPO> result =
                baseMapper.selectPage(poPage, wrapper);
        return ConvertHelper.convertPageInfo(result, WebhookDeliveryLogDO.class);
    }
}

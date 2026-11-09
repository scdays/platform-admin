package com.vtc.platformadmin;

import com.botany.spore.core.utils.MessageUtil;
import com.botany.spore.liquibase.autoconfig.EnableLiquibase;
import com.vtc.platformadmin.infra.config.PlatformAdminProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * platform-admin 统一控制面启动类。
 * <p>
 * 承载资产安全产品部网络安全管理平台的 Partner、EventCenter、TaskCenter、
 * Webhook、Invocation、Audit 等管理域。
 *
 * @author asset-security
 */
@EnableAsync
@EnableScheduling
@EnableLiquibase
@EnableFeignClients({"com.vtc.platformadmin"})
@SpringCloudApplication
@MapperScan("com.vtc.platformadmin.infra.dao")
@EnableConfigurationProperties(PlatformAdminProperties.class)
public class ApplicationStart {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStart.class);

    public static void main(String[] args) {
        SpringApplication.run(ApplicationStart.class, args);
        logger.info("{} started, result: {}", ApplicationStart.class.getSimpleName(),
                MessageUtil.get("application.start.success"));
    }
}
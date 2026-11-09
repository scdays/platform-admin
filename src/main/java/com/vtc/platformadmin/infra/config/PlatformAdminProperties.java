package com.vtc.platformadmin.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * platform-admin 配置属性。
 *
 * @author asset-security
 */
@ConfigurationProperties(prefix = PlatformAdminProperties.PREFIX)
public class PlatformAdminProperties {

    public static final String PREFIX = "platform-admin";

    /** 调用记录事件消费 */
    private Invocation invocation = new Invocation();

    /** Webhook 投递 */
    private Webhook webhook = new Webhook();

    /** EventCenter 管理 */
    private EventCenter eventcenter = new EventCenter();

    /** Token 签发 */
    private Token token = new Token();

    /** 内网管理 API 鉴权 */
    private Admin admin = new Admin();

    public static class Invocation {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Webhook {
        private boolean enabled = false;
        private int maxRetry = 10;
        /** 退避间隔（秒），逗号分隔 */
        private String retryBackoff = "60,300,900,1800,3600,7200";
        /** 是否启用内置测试接收端 */
        private boolean testReceiverEnabled = false;
        /** 测试接收端 Base URL */
        private String testReceiverBaseUrl;
        /** 测试接收端 Path */
        private String testReceiverPath;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxRetry() {
            return maxRetry;
        }

        public void setMaxRetry(int maxRetry) {
            this.maxRetry = maxRetry;
        }

        public String getRetryBackoff() {
            return retryBackoff;
        }

        public void setRetryBackoff(String retryBackoff) {
            this.retryBackoff = retryBackoff;
        }

        public boolean isTestReceiverEnabled() {
            return testReceiverEnabled;
        }

        public void setTestReceiverEnabled(boolean testReceiverEnabled) {
            this.testReceiverEnabled = testReceiverEnabled;
        }

        public String getTestReceiverBaseUrl() {
            return testReceiverBaseUrl;
        }

        public void setTestReceiverBaseUrl(String testReceiverBaseUrl) {
            this.testReceiverBaseUrl = testReceiverBaseUrl;
        }

        public String getTestReceiverPath() {
            return testReceiverPath;
        }

        public void setTestReceiverPath(String testReceiverPath) {
            this.testReceiverPath = testReceiverPath;
        }
    }

    public static class EventCenter {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public Invocation getInvocation() {
        return invocation;
    }

    public void setInvocation(Invocation invocation) {
        this.invocation = invocation;
    }

    public Webhook getWebhook() {
        return webhook;
    }

    public void setWebhook(Webhook webhook) {
        this.webhook = webhook;
    }

    public EventCenter getEventcenter() {
        return eventcenter;
    }

    public void setEventcenter(EventCenter eventcenter) {
        this.eventcenter = eventcenter;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public static class Token {
        /** accessToken 有效秒数，默认 24h */
        private long expiresInSeconds = 86400L;

        public long getExpiresInSeconds() {
            return expiresInSeconds;
        }

        public void setExpiresInSeconds(long expiresInSeconds) {
            this.expiresInSeconds = expiresInSeconds;
        }
    }

    /** 内网管理 API 鉴权配置 */
    public static class Admin {
        /** 内网管理 API 鉴权密钥，请求头 X-Internal-Admin-Key 须与此一致 */
        private String apiKey;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
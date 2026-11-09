package com.vtc.platformadmin.domain.partner.context;

public final class PartnerContext {

    private static final ThreadLocal<String> PARTNER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();

    private PartnerContext() {
    }

    public static void set(String partnerId, String requestId) {
        PARTNER_ID.set(partnerId);
        REQUEST_ID.set(requestId);
    }

    public static String requirePartnerId() {
        String partnerId = PARTNER_ID.get();
        if (partnerId == null || partnerId.trim().isEmpty()) {
            throw new IllegalArgumentException("缺少请求头 X-Partner-Id");
        }
        return partnerId.trim();
    }

    public static String getPartnerId() {
        String partnerId = PARTNER_ID.get();
        return partnerId != null && !partnerId.trim().isEmpty() ? partnerId.trim() : null;
    }

    public static String getRequestId() {
        String requestId = REQUEST_ID.get();
        return requestId != null ? requestId : "req-" + System.currentTimeMillis();
    }

    public static void clear() {
        PARTNER_ID.remove();
        REQUEST_ID.remove();
    }
}

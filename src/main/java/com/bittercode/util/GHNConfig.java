package com.bittercode.util;

/**
 * Configuration loader for GHN (Giao Hang Nhanh) API.
 * Reads from system environment variables first, then .env via EmailConfig.
 */
public final class GHNConfig {

    private static final String DEFAULT_BASE_URL = "https://dev-online-gateway.ghn.vn";

    private GHNConfig() {
    }

    public static String getBaseUrl() {
        String url = EmailConfig.get("GHN_BASE_URL", DEFAULT_BASE_URL);
        if (url == null || url.trim().isEmpty()) {
            return DEFAULT_BASE_URL;
        }
        String trimmed = url.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    public static String getToken() {
        String token = EmailConfig.get("GHN_TOKEN", "");
        return token != null ? token.trim() : "";
    }

    public static int getShopId() {
        String raw = EmailConfig.get("GHN_SHOP_ID", "0");
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getFromDistrictId() {
        String raw = EmailConfig.get("GHN_FROM_DISTRICT_ID", "0");
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getFromWardCode() {
        String ward = EmailConfig.get("GHN_FROM_WARD_CODE", "");
        return ward != null ? ward.trim() : "";
    }

    public static String getWebhookSecret() {
        String secret = EmailConfig.get("GHN_WEBHOOK_SECRET", "");
        return secret != null ? secret.trim() : "";
    }

    public static boolean isConfigured() {
        return !getToken().isEmpty() && getShopId() > 0;
    }

    public static boolean isOriginConfigured() {
        return getFromDistrictId() > 0 && !getFromWardCode().isEmpty();
    }
}

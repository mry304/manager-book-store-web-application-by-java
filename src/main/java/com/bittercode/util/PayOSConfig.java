package com.bittercode.util;

import java.io.InputStream;
import java.util.Properties;

import vn.payos.PayOS;

public final class PayOSConfig {

    private static volatile PayOS payOSInstance = null;
    private static final Properties appProperties = new Properties();

    static {
        try (InputStream input = PayOSConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                appProperties.load(input);
            }
        } catch (Exception e) {
            System.err.println("[PayOSConfig] Could not load application.properties: " + e.getMessage());
        }
    }

    private PayOSConfig() {
    }

    public static String getProperty(String envKey, String propKey, String defaultValue) {
        // 1. Check System environment
        String value = System.getenv(envKey);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        // 2. Check Dotenv via EmailConfig
        try {
            value = EmailConfig.get(envKey, null);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        } catch (Exception ignored) {
        }

        // 3. Check application.properties
        if (propKey != null) {
            value = appProperties.getProperty(propKey);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }

        return defaultValue;
    }

    public static String getClientId() {
        return getProperty("PAYOS_CLIENT_ID", "payos.clientId", "");
    }

    public static String getApiKey() {
        return getProperty("PAYOS_API_KEY", "payos.apiKey", "");
    }

    public static String getChecksumKey() {
        return getProperty("PAYOS_CHECKSUM_KEY", "payos.checksumKey", "");
    }

    public static String getReturnUrl() {
        return getProperty("PAYOS_RETURN_URL", "payos.returnUrl", "http://localhost:8080/onlinebookstore/payment-success.jsp");
    }

    public static String getCancelUrl() {
        return getProperty("PAYOS_CANCEL_URL", "payos.cancelUrl", "http://localhost:8080/onlinebookstore/checkout");
    }

    public static boolean isConfigured() {
        return !getClientId().isEmpty() && !getApiKey().isEmpty() && !getChecksumKey().isEmpty();
    }

    public static PayOS getPayOS() {
        if (payOSInstance == null) {
            synchronized (PayOSConfig.class) {
                if (payOSInstance == null) {
                    String clientId = getClientId();
                    String apiKey = getApiKey();
                    String checksumKey = getChecksumKey();
                    if (!clientId.isEmpty() && !apiKey.isEmpty() && !checksumKey.isEmpty()) {
                        payOSInstance = new PayOS(clientId, apiKey, checksumKey);
                        System.out.println("[PayOSConfig] PayOS SDK initialized successfully with Client ID: " + clientId);
                        System.out.println("[PayOSConfig] checksumKey (first 8 chars): " + (checksumKey.length() >= 8 ? checksumKey.substring(0, 8) : checksumKey) + "...");
                    } else {
                        System.err.println("[PayOSConfig] Warning: PayOS credentials are not fully configured.");
                    }
                }
            }
        }
        return payOSInstance;
    }

    /**
     * Reset singleton instance — gọi khi credentials thay đổi trong lúc dev/test.
     * Không cần dùng trong production.
     */
    public static synchronized void resetInstance() {
        payOSInstance = null;
        System.out.println("[PayOSConfig] PayOS instance has been reset. Will re-initialize on next call.");
    }
}

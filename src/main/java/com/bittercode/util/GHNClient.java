package com.bittercode.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Server-side HTTP client for GHN APIs (Master Data, Shipping Fee, Create/Cancel/Detail Order).
 */
public final class GHNClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final long CACHE_TTL_MS = 24L * 60L * 60L * 1000L;
    private static volatile CacheEntry<JsonNode> provinceCache;
    private static final Map<Integer, CacheEntry<JsonNode>> districtCache = new ConcurrentHashMap<>();
    private static final Map<Integer, CacheEntry<JsonNode>> wardCache = new ConcurrentHashMap<>();

    private GHNClient() {
    }

    private static final class CacheEntry<T> {
        final T value;
        final long expiresAt;

        CacheEntry(T value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    public static final class ShippingFeeResult {
        public final int total;
        public final int serviceFee;
        public final int insuranceFee;

        public ShippingFeeResult(int total, int serviceFee, int insuranceFee) {
            this.total = total;
            this.serviceFee = serviceFee;
            this.insuranceFee = insuranceFee;
        }
    }

    public static final class CreateOrderResult {
        public final String orderCode;
        public final int totalFee;
        public final String expectedDeliveryTime;

        public CreateOrderResult(String orderCode, int totalFee, String expectedDeliveryTime) {
            this.orderCode = orderCode;
            this.totalFee = totalFee;
            this.expectedDeliveryTime = expectedDeliveryTime;
        }
    }

    public static final class OrderItemPayload {
        public final String name;
        public final int quantity;
        public final int weight;
        public final int price;

        public OrderItemPayload(String name, int quantity, int weight, int price) {
            this.name = name;
            this.quantity = quantity;
            this.weight = weight;
            this.price = price;
        }
    }

    public static final class CreateOrderRequest {
        public String clientOrderCode;
        public String toName;
        public String toPhone;
        public String toAddress;
        public String toWardCode;
        public int toDistrictId;
        public int codAmount;
        public int insuranceValue;
        public int weight;
        public int length = 20;
        public int width = 15;
        public int height = 5;
        public int serviceTypeId = 2;
        public int paymentTypeId = 1; // 1 = Shop pays shipping (Freeship)
        public String requiredNote = "CHOXEMHANGKHONGTHU";
        public String note;
        public List<OrderItemPayload> items;
    }

    public static JsonNode getProvinces() throws IOException, InterruptedException {
        CacheEntry<JsonNode> cached = provinceCache;
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }
        JsonNode data = sendRequest("GET", "/shiip/public-api/master-data/province", null, false);
        provinceCache = new CacheEntry<>(data, System.currentTimeMillis() + CACHE_TTL_MS);
        return data;
    }

    public static JsonNode getDistricts(int provinceId) throws IOException, InterruptedException {
        CacheEntry<JsonNode> cached = districtCache.get(provinceId);
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }
        JsonNode data = sendRequest("POST", "/shiip/public-api/master-data/district",
                Map.of("province_id", provinceId), false);
        districtCache.put(provinceId, new CacheEntry<>(data, System.currentTimeMillis() + CACHE_TTL_MS));
        return data;
    }

    public static JsonNode getWards(int districtId) throws IOException, InterruptedException {
        CacheEntry<JsonNode> cached = wardCache.get(districtId);
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }
        JsonNode data = sendRequest("POST", "/shiip/public-api/master-data/ward",
                Map.of("district_id", districtId), false);
        wardCache.put(districtId, new CacheEntry<>(data, System.currentTimeMillis() + CACHE_TTL_MS));
        return data;
    }

    public static ShippingFeeResult calculateFee(int toDistrictId, String toWardCode, int weightGrams, int insuranceValue)
            throws IOException, InterruptedException {
        if (!GHNConfig.isOriginConfigured()) {
            throw new IllegalStateException("Chưa cấu hình GHN_FROM_DISTRICT_ID / GHN_FROM_WARD_CODE.");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from_district_id", GHNConfig.getFromDistrictId());
        body.put("from_ward_code", GHNConfig.getFromWardCode());
        body.put("to_district_id", toDistrictId);
        body.put("to_ward_code", toWardCode);
        body.put("service_type_id", 2);
        body.put("weight", Math.max(100, weightGrams));
        body.put("length", 20);
        body.put("width", 15);
        body.put("height", 5);
        if (insuranceValue > 0) {
            body.put("insurance_value", insuranceValue);
        }

        JsonNode data = sendRequest("POST", "/shiip/public-api/v2/shipping-order/fee", body, true);
        return new ShippingFeeResult(
                data.path("total").asInt(0),
                data.path("service_fee").asInt(0),
                data.path("insurance_fee").asInt(0)
        );
    }

    public static CreateOrderResult createShippingOrder(CreateOrderRequest req)
            throws IOException, InterruptedException {
        Map<String, Object> body = new LinkedHashMap<>();
        if (req.clientOrderCode != null && !req.clientOrderCode.isEmpty()) {
            body.put("client_order_code", req.clientOrderCode);
        }
        if (GHNConfig.isOriginConfigured()) {
            body.put("from_district_id", GHNConfig.getFromDistrictId());
            body.put("from_ward_code", GHNConfig.getFromWardCode());
        }
        body.put("to_name", req.toName);
        body.put("to_phone", req.toPhone);
        body.put("to_address", req.toAddress);
        body.put("to_ward_code", req.toWardCode);
        body.put("to_district_id", req.toDistrictId);
        body.put("cod_amount", Math.max(0, req.codAmount));
        if (req.insuranceValue > 0) {
            body.put("insurance_value", Math.min(req.insuranceValue, 5000000));
        }
        body.put("weight", Math.max(100, req.weight));
        body.put("length", req.length);
        body.put("width", req.width);
        body.put("height", req.height);
        body.put("service_type_id", req.serviceTypeId);
        body.put("payment_type_id", req.paymentTypeId);
        body.put("required_note", req.requiredNote);
        if (req.note != null && !req.note.isEmpty()) {
            body.put("note", req.note);
        }
        if (req.items != null && !req.items.isEmpty()) {
            body.put("items", req.items);
        }

        JsonNode data = sendRequest("POST", "/shiip/public-api/v2/shipping-order/create", body, true);
        return new CreateOrderResult(
                data.path("order_code").asText(""),
                data.path("total_fee").asInt(0),
                data.path("expected_delivery_time").asText("")
        );
    }

    public static void cancelShippingOrder(String ghnOrderCode) throws IOException, InterruptedException {
        Map<String, Object> body = Map.of("order_codes", List.of(ghnOrderCode));
        sendRequest("POST", "/shiip/public-api/v2/shipping-order/cancel", body, true);
    }

    public static JsonNode getOrderDetail(String ghnOrderCode) throws IOException, InterruptedException {
        Map<String, Object> body = Map.of("order_code", ghnOrderCode);
        return sendRequest("POST", "/shiip/public-api/v2/shipping-order/detail", body, true);
    }

    private static JsonNode sendRequest(String method, String path, Object body, boolean includeShopId)
            throws IOException, InterruptedException {
        if (!GHNConfig.isConfigured()) {
            throw new IllegalStateException("Chưa cấu hình GHN_TOKEN hoặc GHN_SHOP_ID.");
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(GHNConfig.getBaseUrl() + path))
                .timeout(Duration.ofSeconds(15))
                .header("Token", GHNConfig.getToken())
                .header("Content-Type", "application/json");

        if (includeShopId) {
            builder.header("ShopId", String.valueOf(GHNConfig.getShopId()));
        }

        if ("GET".equalsIgnoreCase(method)) {
            builder.GET();
        } else {
            String json = body != null ? MAPPER.writeValueAsString(body) : "{}";
            builder.method(method, HttpRequest.BodyPublishers.ofString(json));
        }

        HttpResponse<String> response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        JsonNode root = MAPPER.readTree(response.body());
        int ghnCode = root.path("code").asInt(response.statusCode());
        if (response.statusCode() < 200 || response.statusCode() >= 300 || ghnCode != 200) {
            String message = root.path("message").asText("Lỗi kết nối GHN (HTTP " + response.statusCode() + ")");
            throw new IOException("GHN API error [" + path + "]: " + message);
        }
        return root.path("data");
    }
}

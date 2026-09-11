package com.foodtracking.usda;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;

import com.foodtracking.model.Barcodes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * USDA FoodData Central branded-food search. There is no dedicated barcode
 * endpoint, so this posts a search and then confirms {@code gtinUpc}.
 */
public final class UsdaFoodClient implements FoodDataSource {

    static final String DEFAULT_ENDPOINT = "https://api.nal.usda.gov/fdc/v1/foods/search";
    private static final int TIMEOUT_MS = 8000;
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final String apiKey;
    private final String endpoint;

    public UsdaFoodClient(String apiKey) {
        this(apiKey, DEFAULT_ENDPOINT);
    }

    UsdaFoodClient(String apiKey, String endpoint) {
        this.apiKey = apiKey;
        this.endpoint = endpoint;
    }

    @Override
    public FoodRecord findByBarcode(String barcode) throws UsdaUnavailableException {
        String body = searchBody(barcode);
        String json = post(body);
        return firstMatchingFood(json, barcode);
    }

    static String searchBody(String barcode) {
        String digits = Barcodes.digitsOnly(barcode);
        return "{\"query\":\"" + jsonEscape(digits) + "\","
                + "\"dataType\":[\"Branded\"],"
                + "\"pageSize\":25,"
                + "\"pageNumber\":1}";
    }

    static FoodRecord firstMatchingFood(String json, String barcode) throws UsdaUnavailableException {
        JsonElement rootElement;
        try {
            rootElement = new JsonParser().parse(json);
        } catch (RuntimeException e) {
            throw new UsdaUnavailableException("Could not parse FoodData Central response", e);
        }
        if (!rootElement.isJsonObject()) {
            throw new UsdaUnavailableException("FoodData Central returned an unexpected payload");
        }
        JsonObject root = rootElement.getAsJsonObject();
        JsonArray foods = root.getAsJsonArray("foods");
        if (foods == null) {
            return null;
        }
        for (int i = 0; i < foods.size(); i++) {
            JsonElement element = foods.get(i);
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject food = element.getAsJsonObject();
            String gtin = text(food, "gtinUpc");
            if (Barcodes.matches(barcode, gtin)) {
                return toRecord(food, gtin);
            }
        }
        return null;
    }

    private static FoodRecord toRecord(JsonObject food, String gtin) {
        FoodRecord.Builder builder = FoodRecord.builder()
                .fdcId(text(food, "fdcId"))
                .description(text(food, "description"))
                .brandOwner(text(food, "brandOwner"))
                .brandName(text(food, "brandName"))
                .gtinUpc(gtin)
                .ingredients(text(food, "ingredients"))
                .servingSize(text(food, "servingSize"))
                .servingSizeUnit(text(food, "servingSizeUnit"))
                .category(firstNonEmpty(text(food, "foodCategory"), text(food, "brandedFoodCategory")))
                .dataType(text(food, "dataType"))
                .publicationDate(firstNonEmpty(text(food, "publicationDate"), text(food, "publishedDate")));

        JsonArray nutrients = food.getAsJsonArray("foodNutrients");
        if (nutrients != null) {
            for (int i = 0; i < nutrients.size(); i++) {
                if (!nutrients.get(i).isJsonObject()) {
                    continue;
                }
                JsonObject nutrient = nutrients.get(i).getAsJsonObject();
                String name = firstNonEmpty(text(nutrient, "nutrientName"), text(nutrient, "name"));
                String value = text(nutrient, "value");
                String unit = firstNonEmpty(text(nutrient, "unitName"), text(nutrient, "unit"));
                if (keepNutrient(name) && !value.isEmpty()) {
                    String label = name;
                    String display = unit.isEmpty() ? value : value + " " + unit.toLowerCase(Locale.US);
                    builder.nutrient(label, display);
                }
            }
        }
        return builder.build();
    }

    private static boolean keepNutrient(String name) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase(Locale.US);
        return lower.contains("energy")
                || lower.equals("protein")
                || lower.contains("total lipid")
                || lower.contains("carbohydrate")
                || lower.contains("sugars")
                || lower.equals("sodium")
                || lower.contains("fiber");
    }

    private String post(String body) throws UsdaUnavailableException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(endpoint + "?api_key=" + urlEncode(apiKey));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            OutputStream out = connection.getOutputStream();
            try {
                out.write(body.getBytes(UTF8));
            } finally {
                out.close();
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String response = readFully(stream);
            if (status >= 400) {
                throw new UsdaUnavailableException("HTTP " + status);
            }
            return response;
        } catch (UsdaUnavailableException e) {
            throw e;
        } catch (IOException e) {
            throw new UsdaUnavailableException(e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readFully(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int n;
        while ((n = stream.read(chunk)) != -1) {
            buffer.write(chunk, 0, n);
        }
        return new String(buffer.toByteArray(), UTF8);
    }

    private static String text(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return "";
        }
        JsonElement element = object.get(key);
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        return "";
    }

    private static String firstNonEmpty(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a;
        }
        return b == null ? "" : b;
    }

    private static String jsonEscape(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') {
                escaped.append('\\');
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    private static String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return value;
        }
    }
}

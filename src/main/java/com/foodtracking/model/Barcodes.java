package com.foodtracking.model;

/**
 * Normalized GTIN/UPC comparison. USDA pads barcodes inconsistently, so
 * leading zeros are ignored.
 */
public final class Barcodes {

    private Barcodes() {
    }

    public static String digitsOnly(String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c >= '0' && c <= '9') {
                digits.append(c);
            }
        }
        return digits.toString();
    }

    public static String stripLeadingZeros(String digits) {
        if (digits == null || digits.isEmpty()) {
            return "";
        }
        int i = 0;
        while (i < digits.length() - 1 && digits.charAt(i) == '0') {
            i++;
        }
        return digits.substring(i);
    }

    public static String normalize(String raw) {
        return stripLeadingZeros(digitsOnly(raw));
    }

    public static boolean matches(String requested, String gtinUpc) {
        String left = normalize(requested);
        String right = normalize(gtinUpc);
        return !left.isEmpty() && left.equals(right);
    }

    public static boolean isBlank(String raw) {
        return raw == null || raw.trim().isEmpty();
    }
}

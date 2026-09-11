package com.foodtracking.usda;

import com.foodtracking.model.Barcodes;
import com.foodtracking.model.ProductTable;

public final class FoodLookupService {

    public static final String SOURCE_MOCK = "MOCK";
    public static final String SOURCE_USDA = "USDA";

    private final FoodDataSource source;
    private final String sourceName;

    public FoodLookupService(FoodDataSource source, String sourceName) {
        this.source = source;
        this.sourceName = sourceName;
    }

    public static FoodLookupService fromEnvironment() {
        String key = System.getenv("FDC_API_KEY");
        if (key == null || key.trim().isEmpty()) {
            return new FoodLookupService(new MockFoodCatalog(), SOURCE_MOCK);
        }
        return new FoodLookupService(new UsdaFoodClient(key.trim()), SOURCE_USDA);
    }

    public String getSourceName() {
        return sourceName;
    }

    public boolean isMock() {
        return SOURCE_MOCK.equals(sourceName);
    }

    public ProductTable lookup(String barcode) {
        if (Barcodes.isBlank(barcode) || Barcodes.digitsOnly(barcode).isEmpty()) {
            return ProductTable.missingBarcode();
        }
        String digits = Barcodes.digitsOnly(barcode);
        try {
            FoodRecord record = source.findByBarcode(digits);
            if (record == null) {
                return ProductTable.notFound(digits, sourceName);
            }
            return ProductTable.ok(digits, sourceName, record.toRows());
        } catch (UsdaUnavailableException e) {
            return ProductTable.usdaError(digits, e.getMessage());
        }
    }
}

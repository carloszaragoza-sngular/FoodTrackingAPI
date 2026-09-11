package com.foodtracking.usda;

import org.junit.Test;

import com.foodtracking.model.ProductTable;
import com.foodtracking.model.TableRow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FoodLookupServiceTest {

    @Test
    public void missingBarcodeWhenBlankOrNonNumeric() {
        FoodLookupService service = new FoodLookupService(new MockFoodCatalog(), FoodLookupService.SOURCE_MOCK);
        assertEquals(ProductTable.MISSING_BARCODE, service.lookup(null).getStatus());
        assertEquals(ProductTable.MISSING_BARCODE, service.lookup("   ").getStatus());
        assertEquals(ProductTable.MISSING_BARCODE, service.lookup("abc").getStatus());
        assertEquals("A barcode is required.", service.lookup("").getMessage());
    }

    @Test
    public void mockCatalogFindsCocaCola() {
        FoodLookupService service = new FoodLookupService(new MockFoodCatalog(), FoodLookupService.SOURCE_MOCK);
        ProductTable table = service.lookup("049000006461");
        assertEquals(ProductTable.OK, table.getStatus());
        assertEquals("MOCK", table.getSource());
        assertTrue(containsField(table, "Description", "COCA-COLA CLASSIC"));
        assertTrue(containsField(table, "Brand owner", "The Coca-Cola Company"));
    }

    @Test
    public void mockCatalogNotFound() {
        FoodLookupService service = new FoodLookupService(new MockFoodCatalog(), FoodLookupService.SOURCE_MOCK);
        ProductTable table = service.lookup("000000000000");
        assertEquals(ProductTable.NOT_FOUND, table.getStatus());
        assertEquals("No branded food matched barcode 000000000000.", table.getMessage());
        assertTrue(table.getRows().isEmpty());
    }

    @Test
    public void usdaFailureBecomesErrorTable() {
        FoodLookupService service = new FoodLookupService(new FoodDataSource() {
            @Override
            public FoodRecord findByBarcode(String barcode) throws UsdaUnavailableException {
                throw new UsdaUnavailableException("HTTP 503");
            }
        }, FoodLookupService.SOURCE_USDA);
        ProductTable table = service.lookup("049000006461");
        assertEquals(ProductTable.USDA_ERROR, table.getStatus());
        assertTrue(table.getMessage().contains("HTTP 503"));
        assertTrue(table.getRows().isEmpty());
    }

    private static boolean containsField(ProductTable table, String field, String value) {
        for (TableRow row : table.getRows()) {
            if (field.equals(row.getField()) && value.equals(row.getValue())) {
                return true;
            }
        }
        return false;
    }
}

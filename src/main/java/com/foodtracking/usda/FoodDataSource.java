package com.foodtracking.usda;

public interface FoodDataSource {

    /**
     * @return matching branded food, or {@code null} when the barcode is unknown
     */
    FoodRecord findByBarcode(String barcode) throws UsdaUnavailableException;
}

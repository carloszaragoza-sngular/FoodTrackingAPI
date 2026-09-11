package com.foodtracking.usda;

import java.util.LinkedHashMap;
import java.util.Map;

import com.foodtracking.model.Barcodes;

public final class MockFoodCatalog implements FoodDataSource {

    private final Map<String, FoodRecord> byNormalizedBarcode = new LinkedHashMap<String, FoodRecord>();

    public MockFoodCatalog() {
        add(FoodRecord.builder()
                .fdcId("1103277")
                .description("COCA-COLA CLASSIC")
                .brandOwner("The Coca-Cola Company")
                .brandName("Coca-Cola")
                .gtinUpc("049000006461")
                .ingredients("CARBONATED WATER, HIGH FRUCTOSE CORN SYRUP, CARAMEL COLOR, PHOSPHORIC ACID, NATURAL FLAVORS, CAFFEINE.")
                .servingSize("360")
                .servingSizeUnit("ml")
                .category("Soda")
                .dataType("Branded")
                .publicationDate("2020-10-30")
                .nutrient("Energy", "140 kcal")
                .nutrient("Total sugars", "39 g")
                .nutrient("Sodium", "45 mg")
                .build());

        add(FoodRecord.builder()
                .fdcId("1750347")
                .description("CHEERIOS CEREAL")
                .brandOwner("GENERAL MILLS SALES INC.")
                .brandName("Cheerios")
                .gtinUpc("016000275287")
                .ingredients("WHOLE GRAIN OATS, CORN STARCH, SUGAR, SALT, TRIPOTASSIUM PHOSPHATE, VITAMIN E (MIXED TOCOPHEROLS) ADDED TO PRESERVE FRESHNESS.")
                .servingSize("39")
                .servingSizeUnit("g")
                .category("Cereal")
                .dataType("Branded")
                .publicationDate("2021-04-08")
                .nutrient("Energy", "140 kcal")
                .nutrient("Protein", "5 g")
                .nutrient("Total carbohydrate", "29 g")
                .build());

        add(FoodRecord.builder()
                .fdcId("1697612")
                .description("NESTLE TOLL HOUSE SEMI-SWEET CHOCOLATE MORSELS")
                .brandOwner("Nestle USA Inc.")
                .brandName("Toll House")
                .gtinUpc("028000661407")
                .ingredients("SEMI-SWEET CHOCOLATE (SUGAR, CHOCOLATE, COCOA BUTTER, MILKFAT, SOY LECITHIN, NATURAL FLAVORS).")
                .servingSize("14")
                .servingSizeUnit("g")
                .category("Chocolate")
                .dataType("Branded")
                .publicationDate("2021-02-26")
                .nutrient("Energy", "70 kcal")
                .nutrient("Total lipid (fat)", "4 g")
                .nutrient("Total sugars", "8 g")
                .build());
    }

    private void add(FoodRecord record) {
        byNormalizedBarcode.put(Barcodes.normalize(record.getGtinUpc()), record);
    }

    @Override
    public FoodRecord findByBarcode(String barcode) {
        return byNormalizedBarcode.get(Barcodes.normalize(barcode));
    }
}

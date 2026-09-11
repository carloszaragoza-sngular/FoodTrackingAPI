package com.foodtracking.usda;

import java.util.ArrayList;
import java.util.List;

import com.foodtracking.model.TableRow;

public final class FoodRecord {

    private final String fdcId;
    private final String description;
    private final String brandOwner;
    private final String brandName;
    private final String gtinUpc;
    private final String ingredients;
    private final String servingSize;
    private final String servingSizeUnit;
    private final String category;
    private final String dataType;
    private final String publicationDate;
    private final List<TableRow> extraNutrients;

    private FoodRecord(Builder builder) {
        this.fdcId = builder.fdcId;
        this.description = builder.description;
        this.brandOwner = builder.brandOwner;
        this.brandName = builder.brandName;
        this.gtinUpc = builder.gtinUpc;
        this.ingredients = builder.ingredients;
        this.servingSize = builder.servingSize;
        this.servingSizeUnit = builder.servingSizeUnit;
        this.category = builder.category;
        this.dataType = builder.dataType;
        this.publicationDate = builder.publicationDate;
        this.extraNutrients = builder.extraNutrients;
    }

    public String getGtinUpc() {
        return gtinUpc;
    }

    public List<TableRow> toRows() {
        List<TableRow> rows = new ArrayList<TableRow>();
        add(rows, "FDC ID", fdcId);
        add(rows, "Description", description);
        add(rows, "Brand owner", brandOwner);
        add(rows, "Brand name", brandName);
        add(rows, "GTIN/UPC", gtinUpc);
        add(rows, "Ingredients", ingredients);
        add(rows, "Serving size", servingSize);
        add(rows, "Serving size unit", servingSizeUnit);
        add(rows, "Category", category);
        add(rows, "Data type", dataType);
        add(rows, "Publication date", publicationDate);
        rows.addAll(extraNutrients);
        return rows;
    }

    private static void add(List<TableRow> rows, String field, String value) {
        if (value != null && !value.trim().isEmpty()) {
            rows.add(new TableRow(field, value));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String fdcId;
        private String description;
        private String brandOwner;
        private String brandName;
        private String gtinUpc;
        private String ingredients;
        private String servingSize;
        private String servingSizeUnit;
        private String category;
        private String dataType;
        private String publicationDate;
        private final List<TableRow> extraNutrients = new ArrayList<TableRow>();

        public Builder fdcId(String fdcId) {
            this.fdcId = fdcId;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder brandOwner(String brandOwner) {
            this.brandOwner = brandOwner;
            return this;
        }

        public Builder brandName(String brandName) {
            this.brandName = brandName;
            return this;
        }

        public Builder gtinUpc(String gtinUpc) {
            this.gtinUpc = gtinUpc;
            return this;
        }

        public Builder ingredients(String ingredients) {
            this.ingredients = ingredients;
            return this;
        }

        public Builder servingSize(String servingSize) {
            this.servingSize = servingSize;
            return this;
        }

        public Builder servingSizeUnit(String servingSizeUnit) {
            this.servingSizeUnit = servingSizeUnit;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder dataType(String dataType) {
            this.dataType = dataType;
            return this;
        }

        public Builder publicationDate(String publicationDate) {
            this.publicationDate = publicationDate;
            return this;
        }

        public Builder nutrient(String name, String value) {
            if (name != null && value != null && !value.trim().isEmpty()) {
                extraNutrients.add(new TableRow(name, value));
            }
            return this;
        }

        public FoodRecord build() {
            return new FoodRecord(this);
        }
    }
}

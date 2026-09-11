package com.foodtracking.usda;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UsdaFoodClientTest {

    @Test
    public void ignoresFuzzyHitsThatDoNotMatchGtin() throws Exception {
        String json = "{\"foods\":[{"
                + "\"fdcId\":1,"
                + "\"description\":\"WRONG FOOD\","
                + "\"gtinUpc\":\"999999999999\""
                + "}]}";
        assertNull(UsdaFoodClient.firstMatchingFood(json, "049000006461"));
    }

    @Test
    public void selectsExactGtinDespitePadding() throws Exception {
        String json = "{\"foods\":[{"
                + "\"fdcId\":1103277,"
                + "\"description\":\"COCA-COLA CLASSIC\","
                + "\"brandOwner\":\"The Coca-Cola Company\","
                + "\"gtinUpc\":\"00049000006461\","
                + "\"foodNutrients\":[{\"nutrientName\":\"Energy\",\"value\":140,\"unitName\":\"KCAL\"}]"
                + "}]}";
        FoodRecord record = UsdaFoodClient.firstMatchingFood(json, "049000006461");
        assertNotNull(record);
        assertEquals("00049000006461", record.getGtinUpc());
        assertTrue(record.toRows().size() >= 3);
    }

    @Test
    public void searchBodyStaysJava8Json() {
        String body = UsdaFoodClient.searchBody("049000006461");
        assertTrue(body.contains("\"query\":\"049000006461\""));
        assertTrue(body.contains("\"dataType\":[\"Branded\"]"));
    }
}

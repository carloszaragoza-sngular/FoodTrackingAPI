package com.foodtracking.html;

import org.junit.Test;

import com.foodtracking.model.ProductTable;
import com.foodtracking.usda.FoodLookupService;
import com.foodtracking.usda.MockFoodCatalog;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HtmlLookupHandlerTest {

    @Test
    public void emptyStateListsMockExamples() {
        HtmlLookupHandler handler = new HtmlLookupHandler(
                new FoodLookupService(new MockFoodCatalog(), FoodLookupService.SOURCE_MOCK));
        String html = handler.render(null, null);
        assertTrue(html.contains("No lookup yet"));
        assertTrue(html.contains("049000006461"));
        assertTrue(html.contains("mock catalog"));
    }

    @Test
    public void productTableRendersRows() {
        FoodLookupService service = new FoodLookupService(new MockFoodCatalog(), FoodLookupService.SOURCE_MOCK);
        HtmlLookupHandler handler = new HtmlLookupHandler(service);
        ProductTable table = service.lookup("049000006461");
        String html = handler.render("049000006461", table);
        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("COCA-COLA CLASSIC"));
        assertFalse(html.contains("Not found"));
    }

    @Test
    public void errorStatesRenderMessages() {
        FoodLookupService service = new FoodLookupService(new MockFoodCatalog(), FoodLookupService.SOURCE_MOCK);
        HtmlLookupHandler handler = new HtmlLookupHandler(service);
        assertTrue(handler.render("", service.lookup("")).contains("Missing barcode"));
        assertTrue(handler.render("000000000000", service.lookup("000000000000")).contains("Not found"));
        assertTrue(handler.render("<script>", service.lookup("<script>")).contains("&lt;script&gt;")
                || handler.render("<script>", service.lookup("<script>")).contains("Missing barcode"));
    }

    @Test
    public void escapesHtmlInValues() {
        assertTrue(HtmlLookupHandler.esc("<b>x</b>").equals("&lt;b&gt;x&lt;/b&gt;"));
    }
}

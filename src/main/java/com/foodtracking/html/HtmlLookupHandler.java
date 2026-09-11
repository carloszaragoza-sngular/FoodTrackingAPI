package com.foodtracking.html;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.foodtracking.model.ProductTable;
import com.foodtracking.model.TableRow;
import com.foodtracking.usda.FoodLookupService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public final class HtmlLookupHandler implements HttpHandler {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final FoodLookupService lookupService;

    public HtmlLookupHandler(FoodLookupService lookupService) {
        this.lookupService = lookupService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            if ("/favicon.ico".equals(path)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                write(exchange, 405, "text/plain; charset=utf-8", "Method not allowed");
                return;
            }
            Map<String, String> query = parseQuery(exchange.getRequestURI());
            String barcode = query.get("barcode");
            ProductTable table = null;
            if (barcode != null) {
                table = lookupService.lookup(barcode);
            }
            String html = render(barcode, table);
            write(exchange, 200, "text/html; charset=utf-8", html);
        } finally {
            exchange.close();
        }
    }

    String render(String requestedBarcode, ProductTable table) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        html.append("<meta charset=\"utf-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
        html.append("<title>Food Tracking — barcode lookup</title>\n");
        html.append("<style>\n");
        html.append(css());
        html.append("</style>\n</head>\n<body>\n");
        html.append("<main class=\"page\">\n");
        html.append("<header>\n<h1>Food Tracking</h1>\n");
        html.append("<p class=\"lede\">Look up a branded food by GTIN/UPC. Results come from USDA FoodData Central, or from the built-in mock catalog when no API key is set.</p>\n");
        html.append("</header>\n");
        html.append(sourceBanner());
        html.append("<form class=\"lookup\" method=\"get\" action=\"/lookup\" role=\"search\">\n");
        html.append("<label for=\"barcode\">Barcode</label>\n");
        html.append("<div class=\"row\">\n");
        html.append("<input id=\"barcode\" name=\"barcode\" inputmode=\"numeric\" autocomplete=\"off\" ");
        html.append("placeholder=\"049000006461\" value=\"").append(esc(requestedBarcode == null ? "" : requestedBarcode)).append("\">\n");
        html.append("<button type=\"submit\">Look up</button>\n");
        html.append("</div>\n</form>\n");

        if (table == null) {
            html.append("<section class=\"panel empty\" aria-live=\"polite\">\n");
            html.append("<h2>No lookup yet</h2>\n");
            html.append("<p>Enter a barcode to see product details as a table. Mock catalog examples:</p>\n");
            html.append("<ul class=\"examples\">\n");
            html.append("<li><a href=\"/lookup?barcode=049000006461\">049000006461</a> — Coca-Cola Classic</li>\n");
            html.append("<li><a href=\"/lookup?barcode=016000275287\">016000275287</a> — Cheerios</li>\n");
            html.append("<li><a href=\"/lookup?barcode=028000661407\">028000661407</a> — Toll House morsels</li>\n");
            html.append("</ul>\n</section>\n");
        } else if (ProductTable.MISSING_BARCODE.equals(table.getStatus())) {
            html.append(messagePanel("missing", "Missing barcode", table.getMessage()));
        } else if (ProductTable.NOT_FOUND.equals(table.getStatus())) {
            html.append(messagePanel("not-found", "Not found", table.getMessage()));
        } else if (ProductTable.USDA_ERROR.equals(table.getStatus())) {
            html.append(messagePanel("error", "USDA API failure", table.getMessage()));
        } else {
            html.append("<section class=\"panel\">\n");
            html.append("<h2>Product</h2>\n");
            html.append("<table>\n<thead><tr><th>Field</th><th>Value</th></tr></thead>\n<tbody>\n");
            List<TableRow> rows = table.getRows();
            for (int i = 0; i < rows.size(); i++) {
                TableRow row = rows.get(i);
                html.append("<tr><th scope=\"row\">").append(esc(row.getField())).append("</th>");
                html.append("<td>").append(esc(row.getValue())).append("</td></tr>\n");
            }
            html.append("</tbody></table>\n</section>\n");
        }

        html.append("<footer><p>SOAP: <code>POST /FoodTrackingService</code> operation <code>lookupByBarcode</code>. WSDL at <a href=\"/FoodTrackingService?wsdl\">/FoodTrackingService?wsdl</a>.</p></footer>\n");
        html.append("</main>\n</body>\n</html>\n");
        return html.toString();
    }

    private String sourceBanner() {
        if (lookupService.isMock()) {
            return "<p class=\"banner mock\">Using the built-in mock catalog because <code>FDC_API_KEY</code> is not set.</p>\n";
        }
        return "<p class=\"banner live\">Looking up live branded foods with USDA FoodData Central.</p>\n";
    }

    private static String messagePanel(String kind, String title, String message) {
        StringBuilder html = new StringBuilder();
        html.append("<section class=\"panel ").append(kind).append("\" aria-live=\"polite\">\n");
        html.append("<h2>").append(esc(title)).append("</h2>\n");
        html.append("<p>").append(esc(message)).append("</p>\n");
        html.append("</section>\n");
        return html.toString();
    }

    private static String css() {
        return "body{margin:0;font-family:Georgia,serif;background:#f6efe4;color:#24180f;}"
                + ".page{max-width:720px;margin:0 auto;padding:32px 20px 64px;}"
                + "h1{font-size:2rem;margin:0 0 8px;}"
                + ".lede{margin:0 0 20px;line-height:1.45;}"
                + ".banner{padding:10px 12px;border-radius:8px;margin:0 0 20px;}"
                + ".banner.mock{background:#f3d9a4;}"
                + ".banner.live{background:#d7ead3;}"
                + "form.lookup label{display:block;font-weight:700;margin-bottom:6px;}"
                + ".row{display:flex;gap:8px;flex-wrap:wrap;}"
                + "input{flex:1 1 220px;padding:10px 12px;border:1px solid #b9a48c;border-radius:6px;font-size:1rem;}"
                + "button{padding:10px 16px;border:0;border-radius:6px;background:#c23b22;color:#fff;font-size:1rem;cursor:pointer;}"
                + ".panel{background:#fffaf3;border:1px solid #e2d3c0;border-radius:10px;padding:18px;margin-top:22px;}"
                + ".panel.missing,.panel.not-found{border-color:#e0c48a;background:#fff6e4;}"
                + ".panel.error{border-color:#e3a29a;background:#fdeeee;}"
                + "table{width:100%;border-collapse:collapse;}"
                + "th,td{text-align:left;vertical-align:top;padding:8px 6px;border-bottom:1px solid #eadfce;}"
                + "tbody th{width:34%;color:#5b4636;font-weight:600;}"
                + ".examples{padding-left:18px;}"
                + "footer{margin-top:28px;color:#6b5646;font-size:.95rem;}"
                + "a{color:#8b1e12;}"
                + "@media (max-width:600px){tbody th{width:auto;display:block;padding-bottom:0;} td{display:block;padding-top:2px;}}";
    }

    static String esc(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&':
                    out.append("&amp;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                case '"':
                    out.append("&quot;");
                    break;
                case '\'':
                    out.append("&#39;");
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }

    static Map<String, String> parseQuery(URI uri) throws IOException {
        Map<String, String> values = new HashMap<String, String>();
        String raw = uri.getRawQuery();
        if (raw == null || raw.isEmpty()) {
            return values;
        }
        String[] pairs = raw.split("&");
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            int eq = pair.indexOf('=');
            String key;
            String value;
            if (eq < 0) {
                key = decode(pair);
                value = "";
            } else {
                key = decode(pair.substring(0, eq));
                value = decode(pair.substring(eq + 1));
            }
            values.put(key, value);
        }
        return values;
    }

    private static String decode(String value) throws IOException {
        return URLDecoder.decode(value, "UTF-8");
    }

    private static void write(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(UTF8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream out = exchange.getResponseBody();
        out.write(bytes);
        out.close();
    }
}

package com.foodtracking;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.foodtracking.usda.FoodLookupService;
import com.foodtracking.usda.MockFoodCatalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FoodTrackingServerTest {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private FoodTrackingServer server;
    private int port;

    @Before
    public void startServer() throws Exception {
        port = 18444;
        server = new FoodTrackingServer(port, new FoodLookupService(new MockFoodCatalog(), FoodLookupService.SOURCE_MOCK));
        server.start();
    }

    @After
    public void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void htmlLookupReturnsProductTable() throws Exception {
        String html = get("http://127.0.0.1:" + port + "/lookup?barcode=049000006461");
        assertTrue(html.contains("COCA-COLA CLASSIC"));
        assertTrue(html.contains("<table>"));
    }

    @Test
    public void htmlMissingAndNotFound() throws Exception {
        String missing = get("http://127.0.0.1:" + port + "/lookup?barcode=");
        assertTrue(missing.contains("Missing barcode"));
        String unknown = get("http://127.0.0.1:" + port + "/lookup?barcode=111111111111");
        assertTrue(unknown.contains("Not found"));
    }

    @Test
    public void soapLookupReturnsTableRows() throws Exception {
        String envelope = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
                + " xmlns:ft=\"http://foodtracking.carlosz/\">"
                + "<soapenv:Body><ft:lookupByBarcode><barcode>049000006461</barcode></ft:lookupByBarcode>"
                + "</soapenv:Body></soapenv:Envelope>";
        String response = postSoap(envelope);
        assertTrue(response.contains("COCA-COLA CLASSIC"));
        assertTrue(response.contains("<status>OK</status>") || response.contains("OK"));
        assertTrue(response.contains("row") || response.contains("field"));
    }

    @Test
    public void wsdlIsPublished() throws Exception {
        String wsdl = get("http://127.0.0.1:" + port + "/FoodTrackingService?wsdl");
        assertTrue(wsdl.contains("lookupByBarcode"));
        assertTrue(wsdl.toLowerCase().contains("definitions"));
    }

    private String get(String spec) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(spec).openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        try {
            assertEquals(200, connection.getResponseCode());
            return read(connection.getInputStream());
        } finally {
            connection.disconnect();
        }
    }

    private String postSoap(String body) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(
                "http://127.0.0.1:" + port + "/FoodTrackingService").openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setRequestProperty("SOAPAction", "\"lookupByBarcode\"");
        OutputStream out = connection.getOutputStream();
        try {
            out.write(body.getBytes(UTF8));
        } finally {
            out.close();
        }
        try {
            InputStream stream = connection.getResponseCode() >= 400
                    ? connection.getErrorStream()
                    : connection.getInputStream();
            String response = read(stream);
            assertEquals(200, connection.getResponseCode());
            return response;
        } finally {
            connection.disconnect();
        }
    }

    private static String read(InputStream stream) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[2048];
        int n;
        while ((n = stream.read(chunk)) != -1) {
            buffer.write(chunk, 0, n);
        }
        return new String(buffer.toByteArray(), UTF8);
    }
}

package com.foodtracking;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.xml.ws.Endpoint;

import com.foodtracking.html.HtmlLookupHandler;
import com.foodtracking.soap.FoodTrackingPort;
import com.foodtracking.usda.FoodLookupService;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

public final class FoodTrackingServer {

    public static final int DEFAULT_PORT = 18443;

    private final HttpServer httpServer;
    private final Endpoint soapEndpoint;
    private final int port;

    public FoodTrackingServer(int port, FoodLookupService lookupService) throws java.io.IOException {
        this.port = port;
        this.httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        this.soapEndpoint = Endpoint.create(new FoodTrackingPort(lookupService));
        HttpContext soapContext = httpServer.createContext("/FoodTrackingService");
        soapEndpoint.publish(soapContext);
        httpServer.createContext("/", new HtmlLookupHandler(lookupService));
        httpServer.setExecutor(Executors.newFixedThreadPool(8));
    }

    public void start() {
        httpServer.start();
        System.out.println("Food Tracking API");
        System.out.println("  HTML  http://127.0.0.1:" + port + "/");
        System.out.println("  SOAP  http://127.0.0.1:" + port + "/FoodTrackingService");
        System.out.println("  WSDL  http://127.0.0.1:" + port + "/FoodTrackingService?wsdl");
    }

    public void stop() {
        soapEndpoint.stop();
        httpServer.stop(0);
    }

    public static int portFromEnvironment() {
        String raw = System.getenv("FOOD_TRACKING_PORT");
        if (raw == null || raw.trim().isEmpty()) {
            return DEFAULT_PORT;
        }
        return Integer.parseInt(raw.trim());
    }

    public static void main(String[] args) throws Exception {
        FoodLookupService lookup = FoodLookupService.fromEnvironment();
        if (lookup.isMock()) {
            System.out.println("FDC_API_KEY is unset; serving the mock catalog.");
        } else {
            System.out.println("Using USDA FoodData Central with FDC_API_KEY.");
        }
        FoodTrackingServer server = new FoodTrackingServer(portFromEnvironment(), lookup);
        server.start();
    }
}

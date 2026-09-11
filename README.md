# Food Tracking API

Java 8 JAX-WS webservice that looks up branded foods by barcode (GTIN/UPC) in [USDA FoodData Central](https://fdc.nal.usda.gov/api-guide/). The same lookup is available as one SOAP operation and as a simple HTML table.

This project targets **Java 8 only**. It uses APIs that ship with JDK 8 (`javax.jws`, `HttpURLConnection`, `com.sun.net.httpserver`). Do not compile or run it on Java 9+.

## Requirements

- JDK 8 (`java -version` should print `1.8.0_…`)
- Apache Maven 3.6–3.8 (3.8.x still runs on Java 8)

Confirm both use the same JDK:

```bash
java -version
mvn -version
```

If Maven reports a newer Java, set `JAVA_HOME` to a JDK 8 install before continuing.

## Configuration

| Variable | Purpose |
| --- | --- |
| `FDC_API_KEY` | USDA FoodData Central API key. When unset or blank, the service uses a built-in mock catalog. |
| `FOOD_TRACKING_PORT` | Listen port (default `18443`). Bind address is `0.0.0.0`. |

Request a key at [api.data.gov](https://api.data.gov/signup/) if you want live USDA results. The mock catalog is enough to exercise SOAP and HTML locally.

## Run

```bash
mvn -q package
java -jar target/food-tracking-api-1.0.0.jar
```

Or:

```bash
mvn -q exec:java
```

Then open:

- HTML lookup: http://127.0.0.1:18443/
- SOAP endpoint: http://127.0.0.1:18443/FoodTrackingService
- WSDL: http://127.0.0.1:18443/FoodTrackingService?wsdl

### Mock barcodes (no API key)

| Barcode | Product |
| --- | --- |
| `049000006461` | Coca-Cola Classic |
| `016000275287` | Cheerios |
| `028000661407` | Nestlé Toll House morsels |

## SOAP operation

One operation: `lookupByBarcode`. Input is a barcode string. Output is a `productTable` with `status`, `message`, `barcode`, `source`, and `row` entries (`field` / `value`).

Statuses:

- `OK` — product found
- `MISSING_BARCODE` — barcode was blank
- `NOT_FOUND` — no branded food matched the GTIN/UPC
- `USDA_ERROR` — FoodData Central request failed

Example (mock catalog):

```bash
curl -s -X POST http://127.0.0.1:18443/FoodTrackingService \
  -H 'Content-Type: text/xml; charset=utf-8' \
  -H 'SOAPAction: "lookupByBarcode"' \
  --data-binary @- <<'EOF'
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ft="http://foodtracking.carlosz/">
  <soapenv:Body>
    <ft:lookupByBarcode>
      <barcode>049000006461</barcode>
    </ft:lookupByBarcode>
  </soapenv:Body>
</soapenv:Envelope>
EOF
```

## HTML lookup

```
http://127.0.0.1:18443/lookup?barcode=049000006461
```

Empty, not-found, and USDA failure states render as page messages instead of a product table.

## Tests

```bash
mvn -q test
```

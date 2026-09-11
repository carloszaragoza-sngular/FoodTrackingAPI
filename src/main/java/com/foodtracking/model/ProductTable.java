package com.foodtracking.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "productTable")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProductTable", propOrder = {
        "status", "message", "barcode", "source", "rows"
})
public class ProductTable {

    public static final String OK = "OK";
    public static final String MISSING_BARCODE = "MISSING_BARCODE";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String USDA_ERROR = "USDA_ERROR";

    private String status;
    private String message;
    private String barcode;
    private String source;

    @XmlElementWrapper(name = "rows")
    @XmlElement(name = "row")
    private List<TableRow> rows = new ArrayList<TableRow>();

    public ProductTable() {
    }

    public static ProductTable missingBarcode() {
        ProductTable table = new ProductTable();
        table.status = MISSING_BARCODE;
        table.message = "A barcode is required.";
        table.barcode = "";
        table.source = "";
        return table;
    }

    public static ProductTable notFound(String barcode, String source) {
        ProductTable table = new ProductTable();
        table.status = NOT_FOUND;
        table.barcode = barcode;
        table.source = source;
        table.message = "No branded food matched barcode " + barcode + ".";
        return table;
    }

    public static ProductTable usdaError(String barcode, String detail) {
        ProductTable table = new ProductTable();
        table.status = USDA_ERROR;
        table.barcode = barcode;
        table.source = "USDA";
        if (detail == null || detail.trim().isEmpty()) {
            table.message = "USDA FoodData Central did not respond. Try again later.";
        } else {
            table.message = "USDA FoodData Central request failed: " + detail;
        }
        return table;
    }

    public static ProductTable ok(String barcode, String source, List<TableRow> rows) {
        ProductTable table = new ProductTable();
        table.status = OK;
        table.barcode = barcode;
        table.source = source;
        table.message = "Product found.";
        if (rows != null) {
            table.rows = rows;
        }
        return table;
    }

    public boolean isOk() {
        return OK.equals(status);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public void setRows(List<TableRow> rows) {
        this.rows = rows;
    }
}

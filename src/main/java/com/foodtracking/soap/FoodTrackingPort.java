package com.foodtracking.soap;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.foodtracking.model.ProductTable;
import com.foodtracking.usda.FoodLookupService;

@WebService(
        name = "FoodTrackingPort",
        serviceName = "FoodTrackingService",
        portName = "FoodTrackingPort",
        targetNamespace = "http://foodtracking.carlosz/")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL,
        parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public class FoodTrackingPort {

    private final FoodLookupService lookupService;

    public FoodTrackingPort() {
        this(FoodLookupService.fromEnvironment());
    }

    public FoodTrackingPort(FoodLookupService lookupService) {
        this.lookupService = lookupService;
    }

    @WebMethod(operationName = "lookupByBarcode")
    @WebResult(name = "productTable")
    public ProductTable lookupByBarcode(@WebParam(name = "barcode") String barcode) {
        return lookupService.lookup(barcode);
    }
}

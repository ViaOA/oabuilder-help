package com.oreillyauto.storepurchaseorder.remote;

import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestClass;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod.MethodType;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam.ParamType;
import com.viaoa.util.OADate;

/**
 * Interface for Retail-Manual-PO REST API.
 * <p>
 * http://localhost:18080/retail-manual-po/swagger-ui.html<br>
 * <p>
 * Use RestClient<ManualPOInterface> to create local instance.
 */
@RestClass()
public interface ManualPOInterface {
	public final static String ContextName = "retail-manual-po";
	public final static String SingularName = "manualPurchaseOrder";
	public final static String PluralName = "manualPurchaseOrders";

	@RestMethod(methodType = MethodType.PATCH, urlPath = SingularName)
	void updateManualPurchaseOrder(
			@RestParam(paramType = ParamType.BodyJson, name = "manualPurchaseOrder") String jsonMPO);

	@RestMethod(methodType = MethodType.PUT, urlPath = SingularName)
	String createManualPurchaseOrder(
			@RestParam(paramType = ParamType.BodyJson) String jsonMPO);

	@RestMethod(methodType = MethodType.GET, urlPath = SingularName)
	String getPurchaseOrderBySupplierIdPurchaseOrderNumber(
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "supplierId") int supplierId,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "purchaseOrderNumber") int purchaseOrderNumber);

	@RestMethod(methodType = MethodType.GET, urlPath = PluralName, urlQuery = "sorting=Id")
	String getManualPurchaseOrders(
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "fromOrderDate") OADate dateFrom,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "toOrderDate") OADate dateTo,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "pageNumber") int pageNumber,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "rowsPerPage") int rowsPerPage);

	@RestMethod(methodType = MethodType.GET, urlPath = PluralName, urlQuery = "sorting=Id")
	String getPurchaseOrdersByPurchaseOrderNumber(
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "purchaseOrderNumber") int purchaseOrderNumber,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "pageNumber") int pageNumber,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "rowsPerPage") int rowsPerPage);

	@RestMethod(methodType = MethodType.GET, urlPath = PluralName, urlQuery = "sorting=Id")
	String getPurchaseOrdersBySupplierId(
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "supplierId") int supplierId,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "pageNumber") int pageNumber,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "rowsPerPage") int rowsPerPage);
}

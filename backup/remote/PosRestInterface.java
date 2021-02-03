package com.oreillyauto.storepurchaseorder.remote;

import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestClass;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod.MethodType;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam.ParamType;
import com.viaoa.util.OADate;

@RestClass()
public interface PosRestInterface {

	// posrest:18080/posrest/manualpo/manualPurchaseOrders/getByDate?fromOrderDate=2019-01-01&toOrderDate=2022-01-01
	@RestMethod(methodType = MethodType.GET, urlPath = "manualpo/manualPurchaseOrders/getByDate")
	String getByDate(
			@RestParam(paramType = ParamType.UrlQueryParam, name = "fromOrderDate") OADate dateFrom,
			@RestParam(paramType = ParamType.UrlQueryParam, name = "toOrderDate") OADate dateTo);

	@RestMethod(methodType = MethodType.POST, urlPath = "manualpo/manualPurchaseOrder")
	String update(
			@RestParam(paramType = ParamType.PathVariable, name = "id") int id,
			@RestParam(paramType = ParamType.BodyJson, name = "manualPurchaseOrder") String jsonMPO);

	/*  ========================================================================
	  	Retail-Manual-PO
		http://localhost:18080/retail-manual-po/swagger-ui.html
		http://localhost:18080/retail-manual-po/
	 */
	// http://localhost:18080/retail-manual-po/manualPurchaseOrders{?pageNumber,rowsPerPage,sorting,supplierId}
	@RestMethod(methodType = MethodType.GET, urlPath = "manualpo/manualPurchaseOrders", extraUrlQueryParams = "sorting=Id")
	String getManualPOsForSupplier(
			@RestParam(paramType = ParamType.UrlQueryParam, name = "pageNumber") int pageNumber,
			@RestParam(paramType = ParamType.UrlQueryParam, name = "rowsPerPage") int rowsPerPage,
			// @RestParam(paramType = ParamType.UrlQueryParam, name = "sorting") String sorting,
			@RestParam(paramType = ParamType.UrlQueryParam, name = "supplierId") int supplierId);

}

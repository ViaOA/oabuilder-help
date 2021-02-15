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
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "fromOrderDate") OADate dateFrom,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "toOrderDate") OADate dateTo);

	@RestMethod(methodType = MethodType.POST, urlPath = "manualpo/manualPurchaseOrder")
	String update(
			@RestParam(paramType = ParamType.UrlPathTagValue, name = "id") int id,
			@RestParam(paramType = ParamType.BodyJson, name = "manualPurchaseOrder") String jsonMPO);
	//qqqqqqq needs itemAdjustments	

	/*  ========================================================================
	  	Retail-Manual-PO
		http://localhost:18080/retail-manual-po/swagger-ui.html
		http://localhost:18080/retail-manual-po/
	 */
	// http://localhost:18080/retail-manual-po/manualPurchaseOrders{?pageNumber,rowsPerPage,sorting,supplierId}
	@RestMethod(methodType = MethodType.GET, urlPath = "manualpo/manualPurchaseOrders", urlQuery = "sorting=Id")
	String getManualPOsForSupplier(
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "pageNumber") int pageNumber,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "rowsPerPage") int rowsPerPage,
			// @RestParam(paramType = ParamType.UrlQueryParam, name = "sorting") String sorting,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "supplierId") int supplierId);

}

package com.oreillyauto.storepurchaseorder.remote;

import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestClass;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod.MethodType;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam.ParamType;
import com.viaoa.util.OADate;

@RestClass()
public interface TestInterface {
	/* VVVVVVV WORKS
	PurchaseOrder getPurchaseOrder(int id);
	
	@RestMethod(urlPath = "purchaseOrder", pathTemplate = "/{id}")
	PurchaseOrder getPurchaseOrder1(@RestParam(name = "id") int id);
	
	@RestMethod(urlPath = "purchaseOrder", includePropertyPaths = { "purchaseOrderItems.item", "supplier" })
	PurchaseOrder getPurchaseOrder2(int id);
	
	@RestMethod(urlPath = "purchaseOrder", includeReferenceLevelAmount = 1)
	PurchaseOrder getPurchaseOrder3(int id);
	
	//VVVVVVVVVVVV
	@RestMethod(methodType = MethodType.GetObjectsUsingQuery)
	PurchaseOrder[] getPurchaseOrders(
			@RestParam(paramType = ParamType.QueryParam, name = "id") int[] ids,
			@RestParam(paramType = ParamType.QueryParam, name = "supplier") int[] sids,
			@RestParam(paramType = ParamType.QueryParam, name = "purchaseOrderItems.item") int itemId);
	
	@RestMethod(methodType = MethodType.GetObjectsUsingIDs)
	PurchaseOrder[] getPurchaseOrders(
			@RestParam(paramType = ParamType.QueryParam, name = "id") int... ids);
	
	@RestMethod()
	PurchaseOrder updatePurchaseOrders(
			@RestParam(paramType = ParamType.BodyObject, name = "purchaseOrder") PurchaseOrder po,
			@RestParam(paramType = ParamType.BodyObject, name = "amount") int amount,
			@RestParam(paramType = ParamType.BodyObject, name = "date") OADate date,
			@RestParam(paramType = ParamType.BodyObject, name = "purchaseOrder2") PurchaseOrder po2,
			@RestParam(paramType = ParamType.BodyObject, name = "purchaseOrder3") PurchaseOrder po3);
	
	@RestMethod()
	PurchaseOrder updatePurchaseOrders(
			@RestParam(paramType = ParamType.BodyObject, name = "purchaseOrder") PurchaseOrder po,
			@RestParam(paramType = ParamType.BodyObject, name = "amount") int amount,
			@RestParam(paramType = ParamType.BodyObject, name = "date") OADate date,
			@RestParam(paramType = ParamType.BodyObject, name = "purchaseOrder2") PurchaseOrder po2,
			@RestParam(paramType = ParamType.BodyObject, name = "purchaseOrders") PurchaseOrder[] pos);
	
	@RestMethod()
	PurchaseOrder updatePurchaseOrders(
			@RestParam(paramType = ParamType.BodyObject, name = "purchaseOrder") PurchaseOrder po);
	
	int[] savePurchaseOrder(PurchaseOrder po);
	
	void savePurchaseOrder2(PurchaseOrder po);
	
	VVVVVVV WORKS WORKS WORKS WORKS WORKS */

	/*qqqqqqqqqqq
	
	@RestMethod()
	PurchaseOrder updatePurchaseOrders(
			@RestParam(paramType = ParamType.BodyObject, name = "purchaseOrder") PurchaseOrder po,
			@RestParam(paramType = ParamType.BodyObject, name = "amount") int amount,
			@RestParam(paramType = ParamType.BodyObject, name = "date") OADate date,
			@RestParam(paramType = ParamType.BodyObject, name = "purchaseOrder2") PurchaseOrder po2,
			@RestParam(paramType = ParamType.BodyObject, name = "purchaseOrders") List<PurchaseOrder> pos);
	
	
	@RestMethod()
	PurchaseOrder updatePurchaseOrders(
			@RestParam(paramType = ParamType.BodyObject, name = "purchaseOrder") PurchaseOrder[] pos,
			@RestParam(paramType = ParamType.BodyObject, name = "purchaseOrder2") PurchaseOrder po2,
			@RestParam(paramType = ParamType.BodyObject, name = "purchaseOrder3") PurchaseOrder po3);
	
	
	
	
		@RestMethod(urlPath = "purchaseOrders")
		PurchaseOrder[] getPurchaseOrders2(
				@RestParam(paramType = ParamType.QueryParam, paramClass = Integer.class, name = "id") List<Integer> ids);
	
		@RestMethod(urlPath = "purchaseOrders")
		List<PurchaseOrder> getPurchaseOrders3(
				@RestParam(paramType = ParamType.QueryParam, name = "id") int[] ids);
	
		@RestMethod(urlPath = "purchaseOrders", includePropertyPath = PurchaseOrder.P_Supplier)
		List<PurchaseOrder> getPurchaseOrders3(
				@RestParam(paramType = ParamType.QueryParam, paramClass = Integer.class, name = "id") List<Integer> ids);
	
		@RestMethod(urlPath = "purchaseOrders", includeReferenceLevelAmount = 1, includePropertyPaths = { "purchaseOrderItems.item",
				"supplier" })
		List<PurchaseOrder> getPurchaseOrders4(
				@RestParam(paramType = ParamType.QueryParam, paramClass = Integer.class, name = "id") List<Integer> ids);
	
	
	qqq */

	/*
	/*  ========================================================================
		posrest
		http://posrest:18080/posrest/manualpo?_wadl
	*/

	// posrest:18080/posrest/manualpo/manualPurchaseOrders/getByDate?fromOrderDate=2019-01-01&toOrderDate=2022-01-01
	@RestMethod(methodType = MethodType.GET, urlPath = "manualPurchaseOrders/getByDate")
	String getByDate(
			@RestParam(paramType = ParamType.UrlQueryParam, name = "fromOrderDate") OADate dateFrom,
			@RestParam(paramType = ParamType.UrlQueryParam, name = "toOrderDate") OADate dateTo);

	@RestMethod(methodType = MethodType.POST, urlPath = "manualPurchaseOrder")
	void update(
			@RestParam(paramType = ParamType.PathVariable, name = "id") int id,
			@RestParam(paramType = ParamType.BodyJson, name = "manualPurchaseOrder") String jsonMPO);

	/*  ========================================================================
	  	Retail-Manual-PO
		http://localhost:18080/retail-manual-po/swagger-ui.html
		http://localhost:18080/retail-manual-po/
	 */
	// http://localhost:18080/retail-manual-po/manualPurchaseOrders{?pageNumber,rowsPerPage,sorting,supplierId}
	@RestMethod(methodType = MethodType.GET, urlPath = "manualPurchaseOrders", extraUrlQueryParams = "sorting=Id")
	String getManualPOsForSupplier(
			@RestParam(paramType = ParamType.UrlQueryParam, name = "pageNumber") int pageNumber,
			@RestParam(paramType = ParamType.UrlQueryParam, name = "rowsPerPage") int rowsPerPage,
			// @RestParam(paramType = ParamType.UrlQueryParam, name = "sorting") String sorting,
			@RestParam(paramType = ParamType.UrlQueryParam, name = "supplierId") int supplierId);

	//vvvvvvvvvvvvvvv

	// works with OARestServlet VVVVVVVVVVVVVVVV ======== VVVVVVVVVVVVVVVVVVVV
	/*
	@RestMethod(methodType = MethodType.GET)
	<T> List<T> select(
			@RestParam(paramType = ParamType.MethodReturnClass) Class<T> methodReturnClass,
			@RestParam(paramType = ParamType.UrlPath) String urlPath,
			@RestParam(paramType = ParamType.QueryWhereClause) String queryClause,
			@RestParam(paramType = ParamType.QueryParam) Object[] queryParams,
			@RestParam(paramType = ParamType.QueryOrderBy) String queryOrderBy,
			@RestParam(paramType = ParamType.PageNumber) int pageNumber,
			@RestParam(paramType = ParamType.ResponseIncludePropertyPaths) String... includePropertyPaths);
	
	/*
	@RestMethod(methodType = MethodType.POST)
	<T> List<T> select(
			@RestParam(paramType = ParamType.MethodReturnClass) Class<T> methodReturnClass,
			@RestParam(paramType = ParamType.UrlPath) String urlPath,
			@RestParam(paramType = ParamType.QueryWhereClause) String queryClause,
			@RestParam(paramType = ParamType.QueryParam) Object[] queryValues,
			@RestParam(paramType = ParamType.QueryOrderBy) String queryOrderBy,
			@RestParam(paramType = ParamType.PageNumber) int pageNumber,
			@RestParam(paramType = ParamType.ResponseIncludePropertyPaths) String... includePropertyPaths);
	*/
	/* TEST THIS qqqqqqqqqqq
		@RestMethod(methodType = MethodType.POST)
		<T> List<T> getReference(
				@RestParam(paramType = ParamType.MethodReturnClass) Class<T> methodReturnClass,
				@RestParam(paramType = ParamType.UrlPath) String urlPath,
				@RestParam() Class<?> parentClass,
				@RestParam() String parentId, // separated by "-" for multipart
				@RestParam() String refPropertyName,
				@RestParam() String[] otherRefPropertyNames,
				@RestParam() String[] siblingIds, // separated by "-" for multipart
				@RestParam(paramType = ParamType.ResponseIncludePropertyPath) String... includePropertyPaths);
	*/
	/* qqqqqqqqqqq
	
	// ====== Query
	
	qqqqqqqqqqqq */

	// qqqqqqqqqqqqqqqqq testing

	/*
	path template qqqqqqq
	
	 insert, update, save ... etc ...
	
	PurchaseOrder[] getSupplierPurchaseOrders(@RestParam(name = "supplierId") int supplierId);
	
	@RestMethod(includePropertyPaths = { "PurchaseOrderItems.Item", PurchaseOrder.P_Supplier })
	PurchaseOrder getPurchaseOrder(int id, @RestParam(paramType = ParamType.ResponseIncludePropertyPath) String[]... pp);
	
	@RestMethod(path = "purchaseOrders", query = "supplier.id = ?", includePropertyPaths = { "PurchaseOrderItems.Item",
			PurchaseOrder.P_Supplier })
	List<PurchaseOrder> getSupplierPurchaseOrders(@RestParam(paramType = ParamType.QueryArgument) int supplierId);
	
	int savePurchaseOrder(@RestParam(includePropertyPath = "PurchaseOrderItems") PurchaseOrder purchaseOrder);
	
	@RestMethod(includePropertyPaths = { "PurchaseOrderItems.Item", PurchaseOrder.P_Supplier })
	List<PurchaseOrder> selectPurchaseOrders(
			@RestParam(paramType = ParamType.Query) String query,
			@RestParam(paramType = ParamType.QueryOrderBy) String orderBy,
			@RestParam(paramType = ParamType.QueryArguments) Object... args);
	
	@RestMethod(includePropertyPaths = { "PurchaseOrderItems.Item", PurchaseOrder.P_Supplier })
	List<PurchaseOrder> selectPurchaseOrders(
			@RestParam(paramType = ParamType.PageNumber) int pageNumber,
			@RestParam(paramType = ParamType.Query) String query,
			@RestParam(paramType = ParamType.QueryOrderBy) String orderBy,
			@RestParam(paramType = ParamType.QueryArguments) Object... args);
	`*/
	/*
	
	
	@RestMethod(methodType = MethodType.GET, paramsType = RequestType.Query, path = "{?query=supplierId=%supplierId%}")
	List<PurchaseOrder> getPurchaseOrders(@RestParameter(name = "supplierId") int supplierId);
	
	@RestMethod(methodType = MethodType.GET, urlPath = "/purchaseOrders", paramsType = RequestType.Query, path = "?query=supplierId=%supplierId%")
	List<PurchaseOrder> getSupplierPurchaseOrders(@RestParameter(name = "supplierId") int supplierId);
	*/

	/*
	
	
	List<PurchaseOrder> getPurchaseOrders(@RestParameter(name = "supplierId") int supplierId);
	
	@RestMethod(sqlQuery = "orderDate >= ? AND orderDate <= ?", sqlOrder = "Id")
	List<PurchaseOrder> selectPurchaseOrders(
			@RestParameter(name = "beginDate") LocalDateTime dBegin,
			@RestParameter(name = "endDate") LocalDateTime dEnd,
			@RestParameter(name = "pageNumber") int pageNumber);
	
	// --
	
	PurchaseOrder putPurchaseOrder(PurchaseOrder po);
	
	void createPurchaseOrder(PurchaseOrder po);
	
	void insertPurchaseOrder(PurchaseOrder po);
	
	void savePurchaseOrder(PurchaseOrder po);
	
	void updatePurchaseOrder(PurchaseOrder po);
	
	String test(String test);
	
	int ping(int x);
	*/
}

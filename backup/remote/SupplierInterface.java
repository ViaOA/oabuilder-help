package com.oreillyauto.storepurchaseorder.remote;

import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestClass;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod.MethodType;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam.ParamType;

/**
 * Interface for Retail-Supplier REST API.
 * <p>
 * http://localhost:18080/retail-supplier/swagger-ui.html<br>
 * <p>
 * Use RestClient<SupplierInterface> to create local instance.
 */
@RestClass()
public interface SupplierInterface {
	public final static String ContextName = "retail-supplier";
	public final static String SingularName = "supplier";
	public final static String PluralName = "suppliers";

	@RestMethod(methodType = MethodType.GET, urlPath = SingularName)
	String getSupplier(
			@RestParam(paramType = ParamType.UrlQueryParam, name = "supplierCode") int supplierCode,
			@RestParam(paramType = ParamType.UrlQueryParam, name = "pageNumber") boolean active);

	@RestMethod(methodType = MethodType.GET, urlPath = PluralName, extraUrlQueryParams = "sorting=supplier_Code")
	String getSuppliersByCorporateWarehouseAndCorporateOilSupplier(
			@RestParam(paramType = ParamType.UrlQueryParam, name = "active") boolean active,
			@RestParam(paramType = ParamType.UrlQueryParam, name = "corporateOilSupplier") String corporateOilSupplier,
			@RestParam(paramType = ParamType.UrlQueryParam, name = "corporateWarehouse") String corporateWarehouse,
			@RestParam(paramType = ParamType.UrlQueryParam, name = "pageNumber") int pageNumber,
			@RestParam(paramType = ParamType.UrlQueryParam, name = "rowsPerPage") int rowsPerPage);

	@RestMethod(methodType = MethodType.GET, urlPath = PluralName, extraUrlQueryParams = "sorting=supplier_Code")
	String getSuppliers(
			@RestParam(paramType = ParamType.UrlQueryParam, name = "active") boolean active,
			@RestParam(paramType = ParamType.UrlQueryParam, name = "pageNumber") int pageNumber,
			@RestParam(paramType = ParamType.UrlQueryParam, name = "rowsPerPage") int rowsPerPage);

}

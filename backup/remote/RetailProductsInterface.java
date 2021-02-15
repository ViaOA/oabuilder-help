package com.oreillyauto.storepurchaseorder.remote;

import java.util.List;

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
public interface RetailProductsInterface {
	public final static String ContextName = "products";
	public final static String SingularName = "product";
	public final static String PluralName = "products";

	@RestMethod(methodType = MethodType.GET, urlPath = PluralName, urlQuery = "sorting=line&sorting=item")
	String getBySupplier(
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "supplierCode") String supplierCode,

			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "active") boolean active,

			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "stocking") boolean stocking,

			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "pageNumber") int pageNumber,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "rowsPerPage") int rowsPerPage);

	@RestMethod(methodType = MethodType.GET, urlPath = PluralName, urlQuery = "sorting=line&sorting=item")
	String getItems(
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "productIds") List<String> productIds,

			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "active") boolean active,

			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "stocking") boolean stocking,

			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "pageNumber") int pageNumber,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "rowsPerPage") int rowsPerPage);

}

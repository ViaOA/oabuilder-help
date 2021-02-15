package com.oreillyauto.storepurchaseorder.remote;

import java.util.List;

import com.oreillyauto.storepurchaseorder.model.oa.PurchaseOrder;
import com.oreillyauto.storepurchaseorder.model.oa.Supplier;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestClass;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod.MethodType;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam.ParamType;
import com.viaoa.util.OADate;

/**
 * Interface for Retail-Supplier REST API.
 * <p>
 * http://localhost:18080/retail-supplier/swagger-ui.html<br>
 * <p>
 * Use RestClient<SupplierInterface> to create local instance.
 */
@RestClass()
public interface RetailSupplierInterface {
	public final static String ContextName = "retail-supplier";
	public final static String SingularName = "supplier";
	public final static String PluralName = "suppliers";

	// ============ Use custom endpts ===============

	@RestMethod(methodType = MethodType.GET, urlPath = SingularName)
	String getSupplier(
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "supplierCode") int supplierCode,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "pageNumber") boolean active);

	@RestMethod(methodType = MethodType.GET, urlPath = PluralName, urlQuery = "sorting=supplier_code")
	String getSuppliersByCorporateWarehouseAndCorporateOilSupplier(
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "active") boolean active,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "corporateOilSupplier") String corporateOilSupplier,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "corporateWarehouse") String corporateWarehouse,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "pageNumber") int pageNumber,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "rowsPerPage") int rowsPerPage);

	@RestMethod(methodType = MethodType.GET, urlPath = PluralName, urlQuery = "sorting=supplier_code")
	String getSuppliers(
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "active") boolean active,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "pageNumber") int pageNumber,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "rowsPerPage") int rowsPerPage);

	// ============ Using OARest ===============

	@RestMethod(methodType = MethodType.OASearch)
	List<Supplier> select(
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "active") boolean active,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "corporateOilSupplier") boolean corpOilSupplier,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "corporateWarehouse") boolean corporateWarehouse,
			@RestParam(paramType = ParamType.PageNumber) int pageNumber);

	@RestMethod(methodType = MethodType.OASearch)
	List<Supplier> select(
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "active") boolean active,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "corporateOilSupplier") boolean corpOilSupplier,
			@RestParam(paramType = ParamType.UrlQueryNameValue, name = "corporateWarehouse") boolean corporateWarehouse);

	@RestMethod(methodType = MethodType.OAQueryIDs)
	List<Supplier> select(int... Ids);

	@RestMethod(methodType = MethodType.OAUpdate)
	int update(Supplier supplier);

	@RestMethod(methodType = MethodType.OAInsert)
	int insert(Supplier supplier);

	@RestMethod(methodType = MethodType.OADelete)
	int delete(Supplier supplier);

	// ============ Use OARest for Remote method calls on OAObject (Supplier) ===============

	@RestMethod(methodType = MethodType.OAObjectMethodCall, objectMethodName = Supplier.M_AutoGeneratePurchaseOrder)
	PurchaseOrder autoGeneratePurchaseOrder(Supplier supplier, int x, OADate dx, String sx, Supplier spx, boolean bx);
	//was: PurchaseOrder autoGeneratePurchaseOrder(Supplier supplier);

	// po = supplier.autoGeneratePurchaseOrder(supx, 1, new OADate(), "test", supx, true);

	// ============ Use OARest for Remote method calls, to invoke SupplierImpl ===============

	//qqqqqq these are the only ones that need to have Impl

	@RestMethod(methodType = MethodType.OARemote)
	int getSupplierCount(boolean active);

	@RestMethod(methodType = MethodType.OARemote)
	Supplier getBulkSupplier();

	@RestMethod(methodType = MethodType.OARemote)
	void displayOnServer(Supplier supplier);

}

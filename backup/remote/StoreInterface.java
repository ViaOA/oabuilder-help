package com.oreillyauto.storepurchaseorder.remote;

import java.util.List;

import com.oreillyauto.storepurchaseorder.model.oa.Store;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestClass;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod.MethodType;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam.ParamType;

/**
 * Interface for OARest
 * <p>
 * http://localhost:8082/servlet/oarest/store/1<br>
 * http://localhost:8082/swagger-ui/index.html<br>
 * <p>
 * Use RestClient<SupplierInterface> to create local instance.
 */
@RestClass()
public interface StoreInterface {

	// VERIFY verifyMethodType =========================================
	@RestMethod()
	String verifyMethodTypeA();
	// hs.add("method name=verifyMethodTypeA, type=Unassigned, methodType can not be 'Unassigned'");
	// hs.add("method name=verifyMethodTypeA, type=Unassigned, urlPath is required, either: Method.urlPath, or param MethodUrlPath");

	// VERIFY UrlPath =========================================
	@RestMethod(methodType = MethodType.OAGet, urlPath = "/store/?")
	Store verifyUrlPathA(@RestParam(paramType = ParamType.OAObjectId) int id);
	// hs.add("method name=verifyUrlPathA, type=OAGet, creates it's own UrlPath and should not have a urlPath defined");

	@RestMethod(methodType = MethodType.OAGet)
	Store verifyUrlPathB(@RestParam(paramType = ParamType.UrlPathTagValue) int idx, @RestParam(paramType = ParamType.OAObjectId) int id);
	// hs.add("method name=verifyUrlPathB, type=OAGet, creates it's own UrlPath, should not have paramType=UrlPathValue");
	// hs.add("method name=verifyUrlPathB, type=OAGet, paramType=UrlPathValue not allowed with OAGet");

	@RestMethod(methodType = MethodType.OAGet)
	Store verifyUrlPathC(@RestParam(paramType = ParamType.MethodUrlPath) int id, @RestParam(paramType = ParamType.OAObjectId) int idx);
	// hs.add("method name=verifyUrlPathC, type=OAGet, paramType=MethodUrlPath, type needs to be String");
	// hs.add("method name=verifyUrlPathC, type=OAGet, creates it's own UrlPath, should not have paramType=MethodUrlPath");
	// hs.add("method name=verifyUrlPathC, type=OAGet, paramType=MethodUrlPath not allowed with OAGet");

	@RestMethod(methodType = MethodType.OAGet)
	Integer verifyUrlPathD(@RestParam(paramType = ParamType.OAObjectId) int id);
	// hs.add("method name=verifyUrlPathD, type=OAGet, return class type can not be derived, needs to have return as an OAObject class, method returnClass, or param MethodReturnClass");
	// hs.add("method name=verifyUrlPathD, type=OAGet, return value must be an OAObject");
	// hs.add("method name=verifyUrlPathD, type=OAGet, cant derive urlPath, return class must be of type OAObject.class");

	@RestMethod(methodType = MethodType.GET, urlPath = "/store/?")
	Store verifyUrlPathE(@RestParam(paramType = ParamType.UrlPathTagValue) int id);

	@RestMethod(methodType = MethodType.GET)
	Store verifyUrlPathF(@RestParam(paramType = ParamType.UrlPathTagValue) int id);
	// hs.add("method name=verifyUrlPathF, type=GET, urlPath is required, either: Method.urlPath, or param MethodUrlPath");
	// hs.add("method name=verifyUrlPathF, type=GET, urlPath null, template=, param path value 'arg0' not found in template tag(s)");
	// hs.add("method name=verifyUrlPathF, type=GET, urlPath null, has 0 tag value(s), does not match 1 param(s) with paramType=urlPathValue");

	@RestMethod(methodType = MethodType.GET, urlPath = "/")
	Store verifyUrlQueryA(@RestParam(paramType = ParamType.UrlQueryNameValue) int id);
	// hs.add("method name=verifyUrlQueryA, type=GET, param type=UrlQueryValue needs to define a name");

	@RestMethod(methodType = MethodType.OASearch)
	List<Store> verifyOASearchA();
	// hs.add("method name=verifyOASearchA, type=OASearch, requires SearchWhere, param methodSearchWhere, param searchWhereNameValue");

	@RestMethod(methodType = MethodType.OASearch, searchWhere = "adsf")
	List<Store> verifyOASearchB();

	@RestMethod(methodType = MethodType.OASearch)
	List<Store> verifyOASearchC(@RestParam(paramType = ParamType.SearchWhereTagValue) String val);
	// hs.add("method name=verifyOASearchC, type=OASearch, requires SearchWhere, param methodSearchWhere, param searchWhereNameValue");

	@RestMethod(methodType = MethodType.OASearch, searchWhere = "adsf")
	List<Store> verifyOASearchD(@RestParam(paramType = ParamType.SearchWhereTagValue) String val);

	@RestMethod(methodType = MethodType.OASearch, searchWhere = "adsf", searchOrderBy = "")
	List<Store> verifyOASearchE(@RestParam(paramType = ParamType.SearchWhereTagValue) String val);

	@RestMethod(methodType = MethodType.OASearch, searchWhere = "adsf", searchOrderBy = "")
	Store verifyOASearchF(@RestParam(paramType = ParamType.SearchWhereTagValue) String val);
	// hs.add("method name=verifyOASearchF, type=OASearch, returnClassType must be for (array, list, hub)");
	// hs.add("method name=verifyOASearchF, type=OASearch, cant derive urlPath, return class must be array, List or Hub of type OAObject.class");

	@RestMethod(methodType = MethodType.OASearch, searchWhere = "adsf", searchOrderBy = "", methodName = "adf")
	List<Store> verifyOASearchG(@RestParam(paramType = ParamType.SearchWhereTagValue) String val);
	// hs.add("method name=verifyOASearchG, type=OASearch, methodName only valid for methodType=OAObjectMethodCall");

	@RestMethod(methodType = MethodType.OASearch, urlPath = "wrong", searchWhere = "adsf", searchOrderBy = "")
	List<Store> verifyOASearchH(@RestParam(paramType = ParamType.SearchWhereTagValue) String val);
	// hs.add("method name=verifyOASearchH, type=OASearch, creates it's own UrlPath and should not have a urlPath defined");

	@RestMethod(methodType = MethodType.OASearch, searchOrderBy = "")
	List<Store> verifyOASearchI(@RestParam(paramType = ParamType.SearchWhereAddNameValue) String val);

	@RestMethod(methodType = MethodType.OASearch, searchOrderBy = "")
	List<Store> verifyOASearchJ(@RestParam(paramType = ParamType.MethodSearchOrderBy) String val);
	// hs.add("method name=verifyOASearchJ, type=OASearch, requires SearchWhere, param methodSearchWhere, param searchWhereNameValue");

	@RestMethod(methodType = MethodType.POST)
	List<Store> verifyMethodTypeXA();
	// hs.add("method name=verifyMethodTypeXA, type=POST, urlPath is required, either: Method.urlPath, or param MethodUrlPath");

	@RestMethod(methodType = MethodType.POST, urlPath = "asdf")
	List<Store> verifyMethodTypeXB();

	@RestMethod(methodType = MethodType.POST, urlPath = "asdf")
	void verifyMethodTypeXC();

	@RestMethod(methodType = MethodType.POST, urlPath = "asdf")
	int verifyMethodTypeXD();

	@RestMethod(methodType = MethodType.PUT)
	List<Store> verifyMethodTypeXE(@RestParam(paramType = ParamType.MethodUrlPath) String val);

	@RestMethod(methodType = MethodType.PUT)
	List<Store> verifyMethodTypeXF(@RestParam(paramType = ParamType.MethodUrlPath) String val,
			@RestParam(paramType = ParamType.UrlPathTagValue) String val2);
	// hs.add("method name=verifyMethodTypeXF, type=PUT, urlPath null, template=, param path value 'arg1' not found in template tag(s)");
	// hs.add("method name=verifyMethodTypeXF, type=PUT, urlPath null, has 0 tag value(s), does not match 1 param(s) with paramType=urlPathValue");

	@RestMethod(methodType = MethodType.POST, urlPath = "asdf", searchWhere = "adsf", searchOrderBy = "asdf")
	int verifyMethodTypeXG();
	// hs.add("method name=verifyMethodTypeXG, type=POST, searchWhere only valid for methodType=OASearch");
	// hs.add("method name=verifyMethodTypeXG, type=POST, searchOrderBy only valid for methodType=OASearch");

	@RestMethod(methodType = MethodType.PATCH, urlPath = "asdf")
	int verifyMethodTypeXH(@RestParam(paramType = ParamType.MethodSearchWhere) int a,
			@RestParam(paramType = ParamType.SearchWhereAddNameValue) int b,
			@RestParam(paramType = ParamType.OAObject) int d,
			@RestParam(paramType = ParamType.OAObjectId) int e,
			@RestParam(paramType = ParamType.OAObjectMethodCallArg) int f,
			@RestParam(paramType = ParamType.BodyObject) int g,
			@RestParam(paramType = ParamType.BodyJson) int h,
			@RestParam(paramType = ParamType.Header) int i,
			@RestParam(paramType = ParamType.Cookie) int j,
			@RestParam(paramType = ParamType.PageNumber) int k,
			@RestParam(paramType = ParamType.ResponseIncludePropertyPaths) int l);
	// hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=MethodSearchWhere, type needs to be String");

	// hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=OAObject, type should be of class type OAObject");
	// hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=BodyJson, type needs to be String");
	// hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=ResponseIncludePropertyPaths, type should be of class type String or String[]");
	// hs.add("");

	// hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=MethodSearchWhere not allowed with PATCH");
	// hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=SearchWhereNameValue not allowed with PATCH");
	// hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=OAObject not allowed with PATCH");
	// hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=OAObjectId not allowed with PATCH");
	// hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=OAObjectMethodCallArg not allowed with PATCH");

	@RestMethod(methodType = MethodType.OAObjectMethodCall, methodName = "adsfa")
	int verifyMethodTypeOAObjectMethodCallA(@RestParam(paramType = ParamType.OAObject) int x);
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallA, type=OAObjectMethodCall, paramType=OAObject, type should be of class type OAObject");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallA, type=OAObjectMethodCall, cant derive urlPath, ParamType.OAObject must be of type OAObject.class");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallA, type=OAObjectMethodCall, urlPath can not be derived, needs to have a paramtType=OAObject for class type=OAObject.class");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallA, type=OAObjectMethodCall, paramType=OAObject must be for an OAObject");

	@RestMethod(methodType = MethodType.OAObjectMethodCall, methodName = "adsfa")
	int verifyMethodTypeOAObjectMethodCallB(@RestParam(paramType = ParamType.OAObject) Store x);

	@RestMethod(methodType = MethodType.OAObjectMethodCall, methodName = "adsfa")
	int verifyMethodTypeOAObjectMethodCallC(
			@RestParam(paramType = ParamType.OAObject) Store x,
			@RestParam(paramType = ParamType.OAObjectMethodCallArg) int a,
			@RestParam(paramType = ParamType.OAObjectMethodCallArg) Store b,
			@RestParam(paramType = ParamType.OAObjectMethodCallArg) boolean c,
			@RestParam(paramType = ParamType.OAObjectMethodCallArg) int[] d);

	@RestMethod(methodType = MethodType.OAObjectMethodCall, methodName = "adsfa")
	int verifyMethodTypeOAObjectMethodCallD(
			@RestParam(paramType = ParamType.OAObject) Store x,
			@RestParam(paramType = ParamType.OAObjectMethodCallArg) int a,
			@RestParam(paramType = ParamType.OAObjectMethodCallArg) Store b,
			@RestParam(paramType = ParamType.OAObjectMethodCallArg) boolean c,
			int[] d);
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallD, type=OAObjectMethodCall, paramType=Unassigned not allowed with OAObjectMethodCall");

	@RestMethod(methodType = MethodType.OAObjectMethodCall, urlPath = "asdf", includePropertyPath = "d", includePropertyPaths = {
			"ad" }, includeReferenceLevelAmount = 3, methodName = "adfa", returnClass = Integer.class, searchOrderBy = "x", searchWhere = "adf", urlQuery = "adf")
	int verifyMethodTypeOAObjectMethodCallE(@RestParam(paramType = ParamType.MethodSearchWhere) int a,
			@RestParam(paramType = ParamType.SearchWhereAddNameValue) int b,
			@RestParam(paramType = ParamType.OAObject) int d,
			@RestParam(paramType = ParamType.OAObject) int dx,
			@RestParam(paramType = ParamType.OAObjectId) int e,
			@RestParam(paramType = ParamType.OAObjectMethodCallArg) int f,
			@RestParam(paramType = ParamType.BodyObject) int g,
			@RestParam(paramType = ParamType.BodyJson) int h,
			@RestParam(paramType = ParamType.Header) int i,
			@RestParam(paramType = ParamType.Cookie) int j,
			@RestParam(paramType = ParamType.PageNumber) int k,
			@RestParam(paramType = ParamType.ResponseIncludePropertyPaths) int l);
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, includePropertyPaths not needed, since return class is not OAObject");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, includeReferenceLevelAmount > 0, since return class is not OAObject");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, returnClass is known, dont need to use methodType.ReturnClass");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, searchWhere only valid for methodType=OASearch");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, searchOrderBy only valid for methodType=OASearch");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=MethodSearchWhere not allowed with OAObjectMethodCall");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=SearchWhereNameValue not allowed with OAObjectMethodCall");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=OAObject must be for an OAObject");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=OAObjectId not allowed with OAObjectMethodCall");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, creates it's own UrlPath and should not have a urlPath defined");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, cant derive urlPath, ParamType.OAObject must be of type OAObject.class");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, urlPath can not be derived, needs to have a paramtType=OAObject for class type=OAObject.class");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, cant derive urlPath, ParamType.OAObject must be of type OAObject.class");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=OAObject must be for an OAObject");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, only one paramType=OAObject is allowed");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=MethodSearchWhere, type needs to be String");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=OAObject, type should be of class type OAObject");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=OAObject, type should be of class type OAObject");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=BodyJson, type needs to be String");
	// hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=ResponseIncludePropertyPaths, type should be of class type String or String[]");

	@RestMethod(methodType = MethodType.OARemote)
	int verifyMethodTypeOARemoteA();
	// hs.add("method name=verifyMethodTypeOARemoteA, type=OARemote, for internal use only, used by proxy oninvoke to send remote method call to OARestServlet");

	@RestMethod(methodType = MethodType.OAInsert)
	int verifyMethodTypeOAInsertA(@RestParam(paramType = ParamType.OAObject) Store x);

	@RestMethod(methodType = MethodType.OAInsert)
	int verifyMethodTypeOAInsertB(@RestParam(paramType = ParamType.OAObject) Store x,
			@RestParam(paramType = ParamType.OAObject) Store z);
	// hs.add("method name=verifyMethodTypeOAInsertB, type=OAInsert, only one paramType=OAObject is allowed");

	@RestMethod(methodType = MethodType.GET, urlPath = "/store/?")
	String getA(@RestParam(paramType = ParamType.BodyObject) int id);
	// hs.add("method name=getA, type=GET, urlPath /store/?, has 1 expected values, but only 0 params with paramType=urlPathValue");

	@RestMethod(methodType = MethodType.GET, urlPath = "/store/?")
	String getB(@RestParam(paramType = ParamType.UrlPathTagValue) int id);

	@RestMethod(methodType = MethodType.GET, urlPath = "/store/{id}")
	String getC(@RestParam(paramType = ParamType.UrlPathTagValue) int id);
	// hs.add("method name=getC, type=GET, urlPath /store/{id}, template=/store/<%=$id%>, param path value 'arg0' not found");

	@RestMethod(methodType = MethodType.GET, urlPath = "/store/{id}")
	String getD(@RestParam(name = "idx", paramType = ParamType.UrlPathTagValue) int id);
	// hs.add("method name=getD, type=GET, urlPath /store/{id}, template=/store/<%=$id%>, param path value 'idx' not found");

	@RestMethod(methodType = MethodType.GET, urlPath = "/store/{id}")
	String getE(@RestParam(name = "id", paramType = ParamType.UrlPathTagValue) int id);

	@RestMethod(methodType = MethodType.GET, urlPath = "/store/{id}", returnClass = List.class)
	String getF(@RestParam(name = "id", paramType = ParamType.UrlPathTagValue) int id);
	// hs.add("method name=getF, type=GET, returnClass is known, dont need to use methodType.ReturnClass");

	// hs.add("");
	// hs.add("");
	// hs.add("");
	// hs.add("");
	// hs.add("");
	// hs.add("");
	// hs.add("");
	// hs.add("");
	// hs.add("");
	// hs.add("");
	// hs.add("");

}

package com.oreillyauto.storepurchaseorder.remote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.oreillyauto.storepurchaseorder.model.oa.PurchaseOrder;
import com.oreillyauto.storepurchaseorder.model.oa.Store;
import com.oreillyauto.storepurchaseorder.remote.rest.RestClient;
import com.viaoa.hub.Hub;
import com.viaoa.json.OAJson;
import com.viaoa.json.node.OAJsonArrayNode;
import com.viaoa.json.node.OAJsonObjectNode;

public class StoreTest {

	public void test() throws Exception {
		RestClient restClient = new RestClient();

		restClient.setBaseUrl("http://localhost:8082");
		restClient.setUserPw("admin", "admin");

		HashSet<String> hs = new HashSet();

		StoreInterface storeAPI = restClient.getInstance(StoreInterface.class);

		ArrayList<String> al = restClient.verify(StoreInterface.class);

		// expected error messages
		hs.add("method name=verifyMethodTypeA, type=Unassigned, methodType can not be 'Unassigned'");
		hs.add("method name=verifyMethodTypeA, type=Unassigned, urlPath is required, either: Method.urlPath, or param MethodUrlPath");

		hs.add("method name=verifyUrlPathA, type=OAGet, creates it's own UrlPath and should not have a urlPath defined");
		hs.add("method name=verifyUrlPathB, type=OAGet, creates it's own UrlPath, should not have paramType=UrlPathValue");
		hs.add("method name=verifyUrlPathB, type=OAGet, paramType=UrlPathValue not allowed with OAGet");
		hs.add("method name=verifyUrlPathC, type=OAGet, paramType=MethodUrlPath, type needs to be String");
		hs.add("method name=verifyUrlPathC, type=OAGet, creates it's own UrlPath, should not have paramType=MethodUrlPath");
		hs.add("method name=verifyUrlPathC, type=OAGet, paramType=MethodUrlPath not allowed with OAGet");

		hs.add("method name=verifyUrlPathD, type=OAGet, return value must be an OAObject");
		hs.add("method name=verifyUrlPathD, type=OAGet, cant derive urlPath, return class must be of type OAObject.class");

		hs.add("method name=verifyUrlQueryA, type=GET, param type=UrlQueryValue needs to define a name");

		hs.add("method name=verifyOASearchA, type=OASearch, requires SearchWhere, param methodSearchWhere, param searchWhereNameValue");
		hs.add("method name=verifyOASearchC, type=OASearch, requires SearchWhere, param methodSearchWhere, param searchWhereNameValue");
		hs.add("method name=verifyOASearchF, type=OASearch, returnClassType must be for (array, list, hub)");
		hs.add("method name=verifyOASearchF, type=OASearch, cant derive urlPath, return class must be array, List or Hub of type OAObject.class");
		hs.add("method name=verifyOASearchG, type=OASearch, methodName only valid for methodType=OAObjectMethodCall");
		hs.add("method name=verifyOASearchH, type=OASearch, creates it's own UrlPath and should not have a urlPath defined");

		hs.add("method name=verifyMethodTypeXA, type=POST, urlPath is required, either: Method.urlPath, or param MethodUrlPath");
		hs.add("method name=verifyMethodTypeXF, type=PUT, urlPath null, template=, param path value 'arg1' not found in template tag(s)");
		hs.add("method name=verifyMethodTypeXF, type=PUT, urlPath null, has 0 tag value(s), does not match 1 param(s) with paramType=urlPathValue");
		hs.add("method name=verifyMethodTypeXG, type=POST, searchWhere only valid for methodType=OASearch");
		hs.add("method name=verifyMethodTypeXG, type=POST, searchOrderBy only valid for methodType=OASearch");

		hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=MethodSearchWhere, type needs to be String");
		hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=OAObject, type should be of class type OAObject");
		hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=BodyJson, type needs to be String");
		hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=ResponseIncludePropertyPaths, type should be of class type String or String[]");

		hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=MethodSearchWhere not allowed with PATCH");
		hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=SearchWhereNameValue not allowed with PATCH");
		hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=OAObject not allowed with PATCH");
		hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=OAObjectId not allowed with PATCH");
		hs.add("method name=verifyMethodTypeXH, type=PATCH, paramType=OAObjectMethodCallArg not allowed with PATCH");

		hs.add("method name=getA, type=GET, urlPath /store/?, has 1 tag value(s), does not match 0 param(s) with paramType=urlPathValue");
		hs.add("method name=getC, type=GET, urlPath /store/{id}, template=/store/<%=$id%>, param path value 'arg0' not found in template tag(s)");
		hs.add("method name=getD, type=GET, urlPath /store/{id}, template=/store/<%=$id%>, param path value 'idx' not found in template tag(s)");
		hs.add("method name=getF, type=GET, returnClass is known, dont need to use methodType.ReturnClass");
		hs.add("method name=verifyUrlPathF, type=GET, urlPath is required, either: Method.urlPath, or param MethodUrlPath");
		hs.add("method name=verifyUrlPathF, type=GET, urlPath null, template=, param path value 'arg0' not found in template tag(s)");
		hs.add("method name=verifyUrlPathF, type=GET, urlPath null, has 0 tag value(s), does not match 1 param(s) with paramType=urlPathValue");
		hs.add("method name=verifyOASearchJ, type=OASearch, requires SearchWhere, param methodSearchWhere, param searchWhereNameValue");

		hs.add("method name=verifyMethodTypeOAObjectMethodCallA, type=OAObjectMethodCall, paramType=OAObject, type should be of class type OAObject");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallA, type=OAObjectMethodCall, cant derive urlPath, ParamType.OAObject must be of type OAObject.class");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallA, type=OAObjectMethodCall, urlPath can not be derived, needs to have a paramtType=OAObject for class type=OAObject.class");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallA, type=OAObjectMethodCall, paramType=OAObject must be for an OAObject");

		hs.add("method name=verifyMethodTypeOAObjectMethodCallD, type=OAObjectMethodCall, paramType=Unassigned not allowed with OAObjectMethodCall");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, includePropertyPaths not needed, since return class is not OAObject");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, includeReferenceLevelAmount > 0, since return class is not OAObject");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, returnClass is known, dont need to use methodType.ReturnClass");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, searchWhere only valid for methodType=OASearch");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, searchOrderBy only valid for methodType=OASearch");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=MethodSearchWhere not allowed with OAObjectMethodCall");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=SearchWhereNameValue not allowed with OAObjectMethodCall");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=OAObject must be for an OAObject");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=OAObjectId not allowed with OAObjectMethodCall");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, creates it's own UrlPath and should not have a urlPath defined");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, cant derive urlPath, ParamType.OAObject must be of type OAObject.class");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, urlPath can not be derived, needs to have a paramtType=OAObject for class type=OAObject.class");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, only one paramType=OAObject is allowed");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=MethodSearchWhere, type needs to be String");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=OAObject, type should be of class type OAObject");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=BodyJson, type needs to be String");
		hs.add("method name=verifyMethodTypeOAObjectMethodCallE, type=OAObjectMethodCall, paramType=ResponseIncludePropertyPaths, type should be of class type String or String[]");

		hs.add("method name=verifyMethodTypeOARemoteA, type=OARemote, for internal use only, used by proxy oninvoke to send remote method call to OARestServlet");

		hs.add("method name=verifyMethodTypeOAInsertB, type=OAInsert, only one paramType=OAObject is allowed");

		HashSet<String> hs2 = new HashSet(hs);

		for (String s : al) {
			if (!hs.remove(s)) {
				if (!hs2.contains(s)) {
					System.out.println(s);
				}
			}
		}
		if (hs.size() > 0) {
			System.out.println("Expected Errors not found ============================= ");
		}
		for (String s : hs) {
			System.out.println(s);
		}

		Store store;
		Store[] stores;
		PurchaseOrder po, po2;
		String s, s2;
		String json;
		OAJsonObjectNode objNode;
		OAJsonArrayNode arrayNode;
		OAJsonArrayNode arrayNodeAll;
		OAJson oajson = new OAJson();
		List<Store> lstStore;
		Hub<Store> hub;

		// OAGet
		// storeAPI.getStore(1);

		/*
		lstStore = storeAPI.getStores(new int[] { 1 });
		
		lstStore = storeAPI.selectStores(0, "test");
		stores = storeAPI.selectStores2(1);
		hub = storeAPI.selectStores3(1);
		*/

		int xx = 4;
		xx++;

	}

	public static void main(String[] args) throws Exception {
		StoreTest test = new StoreTest();
		test.test();
		System.out.println("DONE");
	}

}

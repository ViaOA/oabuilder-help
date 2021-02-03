package com.oreillyauto.storepurchaseorder.remote.rest;

import java.util.ArrayList;

import com.oreillyauto.storepurchaseorder.model.oa.PurchaseOrder;
import com.oreillyauto.storepurchaseorder.model.oa.PurchaseOrderItem;
import com.oreillyauto.storepurchaseorder.remote.ManualPOInterface;
import com.viaoa.json.OAJson;
import com.viaoa.json.node.OAJsonArrayNode;
import com.viaoa.json.node.OAJsonObjectNode;

public class RestClientTest {

	public void test() {
		RestClient<ManualPOInterface> restClient = new RestClient<ManualPOInterface>() {
			//qqqqqq
		};

		restClient.setBaseUrl("http://localhost:8082/servlet/oarest");

		restClient.setUserPw("admin", "admin");

		ManualPOInterface mpo = restClient.getInstance();

		PurchaseOrder po, po2;
		PurchaseOrder[] pos;
		RestClient.InvokeInfo invokeInfo;

		po = new PurchaseOrder();
		po.getPurchaseOrderItems().add(new PurchaseOrderItem());
		po.setId(5);

		// Tests ==================

		/*
		restClient.setExpectedResult(po);
		po2 = mpo.getPurchaseOrder(5);
		invokeInfo = restClient.getLastInvokeInfo();
		boolean b = invokeInfo.urlPath.endsWith("purchaseOrder/5");
		
		// @RestMethod(urlPath = "purchaseOrder", pathTemplate = "/{id}")
		// PurchaseOrder getPurchaseOrder1(@RestParam(name = "id") int id);
		restClient.setExpectedResult(po);
		po = mpo.getPurchaseOrder1(5);
		invokeInfo = restClient.getLastInvokeInfo();
		b = invokeInfo.urlPath.endsWith("purchaseOrder/5");
		
		// @RestMethod(urlPath = "purchaseOrder", includePropertyPaths = { "purchaseOrderItems.item", "supplier" })
		// PurchaseOrder getPurchaseOrder2(int id);
		restClient.setExpectedResult(po);
		po = mpo.getPurchaseOrder2(5);
		invokeInfo = restClient.getLastInvokeInfo();
		b = invokeInfo.urlPath.endsWith("purchaseOrder/5");
		
		restClient.setExpectedResult(new PurchaseOrder[] { po });
		pos = mpo.getPurchaseOrders(1, 2, 3, 4);
		invokeInfo = restClient.getLastInvokeInfo();
		
		restClient.setExpectedResult(new PurchaseOrder[] { po, po2 });
		pos = mpo.getPurchaseOrders(new int[] { 1, 2, 3, 4 }, new int[] { 12, 13 }, 12);
		invokeInfo = restClient.getLastInvokeInfo();
		
		restClient.setExpectedResult(po);
		po = mpo.updatePurchaseOrders(po, 15, new OADate(), po2, new PurchaseOrder[] { po, po2 });
		invokeInfo = restClient.getLastInvokeInfo();
		
		ArrayList<PurchaseOrder> lst = new ArrayList<>();
		lst.add(po);
		restClient.setExpectedResult(lst);
		*/

		//qqqqqqq REmote call ... server side can accept and run query

		/*
		
		@RestMethod(methodType = MethodType.POST)
		<T> List<T> select(
			@RestParam(paramType = ParamType.MethodReturnClass) Class<T> methodReturnClass,
			@RestParam(paramType = ParamType.UrlPath) String urlPath,
			@RestParam(paramType = ParamType.QueryWhereClause) String queryClause,
			@RestParam(paramType = ParamType.QueryParam) Object[] queryValues,
			@RestParam(paramType = ParamType.QueryOrderBy) String queryOrderBy,
			@RestParam(paramType = ParamType.PageNumber) int pageNumber,
			@RestParam(paramType = ParamType.ResponseIncludePropertyPath) String... includePropertyPaths);
		*/

		String s, s2;
		String json;
		OAJson oajson;
		OAJsonObjectNode objNode;
		OAJsonArrayNode arrayNode;

		ArrayList<PurchaseOrder> lst = new ArrayList<>();
		lst.add(po);
		restClient.setExpectedResult(lst);

		/* VVVVVVVVV  works for querying any objects on my Local VVVVVVVVVV
		List<PurchaseOrder> lst2 = mpo.select(
												PurchaseOrder.class, // return value type
												"purchaseOrders", // url to use
												"id = ? && " + PurchaseOrder.P_PONumber + " = ? AND "
														+ PurchaseOrderPP.purchaseOrderItems().item().pp + " = ?",
												new Object[] { 1, 12345, 4 }, // query params
												"id", // sortBy
												0, // page
												"supplier", // include PPs..
												"purchaseOrderItems.item");
		*/

		/*
		// posrest
		restClient.setBaseUrl("http://posrest:18080/posrest/manualpo");
		
		json = mpo.getByDate((OADate) (new OADate()).addDays(-180), new OADate());
		System.out.println(json);
		
		OAJson oajson = new OAJson();
		OAJsonArrayNode arrayNode = (OAJsonArrayNode) oajson.load(json);
		
		OAJsonNode node = arrayNode.get(0);
		
		int id = 6;
		mpo.update(id, node.toJson());
		*/

		// manualpo
		restClient.setBaseUrl("http://localhost:18080/retail-manual-po");
		/*
		json = mpo.getManualPOsForSupplier(1, 10, 35);
		
		oajson = new OAJson();
		objNode = oajson.loadObject(json);
		arrayNode = objNode.getArray("results");
		Number num = arrayNode.getObject(0).getNumber("purchaseOrderNumber");
		s = arrayNode.getObject(0).toJson();
		s = arrayNode.getObject(0).getArray("purchaseOrderItems").getObject(0).getString("item");
		*/

		int xx = 4;
		xx++;
	}

	public static void main(String[] args) {
		RestClientTest test = new RestClientTest();
		test.test();
		System.out.println("DONE");
	}

}

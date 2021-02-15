package com.oreillyauto.storepurchaseorder.remote;

import com.oreillyauto.storepurchaseorder.model.oa.PurchaseOrder;
import com.oreillyauto.storepurchaseorder.remote.rest.RestClient;
import com.viaoa.json.OAJson;
import com.viaoa.json.node.OAJsonArrayNode;
import com.viaoa.json.node.OAJsonObjectNode;

public class RetailSupplierTest {

	/**
	 * Notes: pageNumbers start at 1
	 * <p>
	 * results that are Many will have the following (json object)<br>
	 * Response properties
	 * <p>
	 * "results":[{..}, ..] <br>
	 * "paging":{"rowsPerPage":10,"pageNumber":1,"totalCount":3}
	 */

	public void test() throws Exception {
		RestClient restClient = new RestClient();

		RetailSupplierInterface supplier = restClient.getInstance(RetailSupplierInterface.class);

		restClient.setBaseUrl("http://localhost:8082/servlet/oarest");
		restClient.setUserPw("admin", "admin");
		// restClient.setBaseUrl("http://localhost:18080/retail-supplier");
		// restClient.setUserPw("admin", "admin");

		PurchaseOrder po, po2;
		String s, s2;
		String json;
		OAJsonObjectNode objNode;
		OAJsonArrayNode arrayNode;
		OAJsonArrayNode arrayNodeAll;

		OAJson oajson = new OAJson();
		/*
				json = supplier.getSupplier(12, true);

				json = supplier.getSuppliers(true, 1, 20);

				objNode = oajson.loadObject(json);
				arrayNodeAll = arrayNode = objNode.getArray("results");

				objNode = arrayNode.getObject(0);
				json = objNode.toJson();
		*/

		int x = supplier.getSupplierCount(true);

		/*
		Supplier supx = new Supplier();
		supx.setId(1);
		po = supplier.autoGeneratePurchaseOrder(supx, 1, new OADate(), "test", supx, true);
		*/
		int xx = 4;
		xx++;

	}

	public static void main(String[] args) throws Exception {
		RetailSupplierTest test = new RetailSupplierTest();
		test.test();
		System.out.println("DONE");
	}

}

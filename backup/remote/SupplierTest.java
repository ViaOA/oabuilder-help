package com.oreillyauto.storepurchaseorder.remote;

import com.oreillyauto.storepurchaseorder.model.oa.PurchaseOrder;
import com.oreillyauto.storepurchaseorder.remote.rest.RestClient;
import com.viaoa.json.OAJson;
import com.viaoa.json.node.OAJsonArrayNode;
import com.viaoa.json.node.OAJsonObjectNode;

public class SupplierTest {

	/**
	 * Notes: pageNumbers start at 1
	 * <p>
	 * results that are Many will have the following (json object)<br>
	 * Response properties
	 * <p>
	 * "results":[{..}, ..] <br>
	 * "paging":{"rowsPerPage":10,"pageNumber":1,"totalCount":3}
	 */

	public void test() {
		RestClient<SupplierInterface> restClient = new RestClient<SupplierInterface>() {
		};

		restClient.setBaseUrl("http://localhost:18080/retail-supplier");
		// restClient.setUserPw("admin", "admin");

		final SupplierInterface supplier = restClient.getInstance();

		PurchaseOrder po, po2;
		String s, s2;
		String json;
		OAJsonObjectNode objNode;
		OAJsonArrayNode arrayNode;
		OAJsonArrayNode arrayNodeAll;

		OAJson oajson = new OAJson();

		json = supplier.getSupplier(12, true);

		json = supplier.getSuppliers(true, 1, 20);

		objNode = oajson.loadObject(json);
		arrayNodeAll = arrayNode = objNode.getArray("results");

		objNode = arrayNode.getObject(0);
		json = objNode.toJson();

		int xx = 4;
		xx++;
	}

	public static void main(String[] args) {
		SupplierTest test = new SupplierTest();
		test.test();
		System.out.println("DONE");
	}

}

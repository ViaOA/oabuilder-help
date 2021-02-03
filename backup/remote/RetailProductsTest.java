package com.oreillyauto.storepurchaseorder.remote;

import java.util.ArrayList;
import java.util.List;

import com.oreillyauto.storepurchaseorder.remote.rest.RestClient;
import com.viaoa.json.OAJson;
import com.viaoa.json.node.OAJsonArrayNode;
import com.viaoa.json.node.OAJsonObjectNode;

public class RetailProductsTest {

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
		RestClient<RetailProductsInterface> restClient = new RestClient<RetailProductsInterface>() {
		};

		restClient.setBaseUrl("http://localhost:18080/retail-products");
		// restClient.setUserPw("admin", "admin");

		final RetailProductsInterface retailProducts = restClient.getInstance();

		String s, s2;
		String json;
		OAJsonObjectNode objNode;
		OAJsonArrayNode arrayNode;
		OAJsonArrayNode arrayNodeAll;

		OAJson oajson = new OAJson();

		json = retailProducts.getBySupplier("35", true, true, 1, 50);

		List<String> lst = new ArrayList();
		lst.add("WIX_51515");
		lst.add("WIX_51514");
		json = retailProducts.getItems(lst, true, true, 1, 10);

		objNode = oajson.loadObject(json);
		arrayNodeAll = arrayNode = objNode.getArray("results");

		objNode = arrayNode.getObject(0);
		json = objNode.toJson();

		int xx = 4;
		xx++;
	}

	public static void main(String[] args) {
		RetailProductsTest test = new RetailProductsTest();
		test.test();
		System.out.println("DONE");
	}

}

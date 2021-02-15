package com.oreillyauto.storepurchaseorder.remote;

import com.oreillyauto.storepurchaseorder.model.oa.PurchaseOrder;
import com.oreillyauto.storepurchaseorder.remote.rest.RestClient;
import com.oreillyauto.storepurchaseorder.remote.rest.RestClient.InvokeInfo;
import com.viaoa.json.OAJson;
import com.viaoa.json.node.OAJsonArrayNode;
import com.viaoa.json.node.OAJsonObjectNode;
import com.viaoa.util.OADate;

public class ManualPOTest {

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

		restClient.setBaseUrl("http://localhost:18080/retail-manual-po");
		// restClient.setUserPw("admin", "admin");

		final ManualPOInterface mpo = restClient.getInstance(ManualPOInterface.class);

		PurchaseOrder po, po2;
		String s, s2;
		String json;
		OAJsonObjectNode objNode;
		OAJsonArrayNode arrayNode;
		OAJsonArrayNode arrayNodeAll;

		OAJson oajson = new OAJson();

		json = mpo.getManualPurchaseOrders((OADate) (new OADate()).addDays(-180), new OADate(), 1, 50);
		objNode = oajson.loadObject(json);
		arrayNodeAll = arrayNode = objNode.getArray("results");

		int x = arrayNode.getSize();
		for (int i = 0; i < x; i++) {
			objNode = arrayNode.getObject(i);
			objNode.set("orderedByUser", "testD");
			int id = objNode.getInt("id");
			json = objNode.toJson();
			try {
				mpo.updateManualPurchaseOrder(json);
			} catch (Exception e) {

			}
		}

		objNode = arrayNode.getObject(0);
		json = objNode.toJson();

		// objNode.set("id", value);
		objNode.set("orderedByUser", "testC");
		json = objNode.toJson();

		mpo.updateManualPurchaseOrder(json);
		InvokeInfo ii = restClient.getLastInvokeInfo();

		objNode.remove("id");
		objNode.set("orderedByUser", "newA");
		int poNum = objNode.getInt("purchaseOrderNumber");
		objNode.set("purchaseOrderNumber", poNum + 1);
		json = objNode.toJson();
		// json = mpo.createManualPurchaseOrder(json);

		json = mpo.getPurchaseOrderBySupplierIdPurchaseOrderNumber(35, 19263);

		json = mpo.getPurchaseOrdersByPurchaseOrderNumber(19264, 1, 10);

		json = mpo.getPurchaseOrdersBySupplierId(35, 1, 10);

		int xx = 4;
		xx++;
	}

	public static void main(String[] args) throws Exception {
		ManualPOTest test = new ManualPOTest();
		test.test();
		System.out.println("DONE");
	}

}

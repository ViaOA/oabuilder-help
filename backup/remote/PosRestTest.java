package com.oreillyauto.storepurchaseorder.remote;

import com.oreillyauto.storepurchaseorder.remote.rest.RestClient;
import com.viaoa.json.OAJson;
import com.viaoa.json.node.OAJsonArrayNode;
import com.viaoa.json.node.OAJsonObjectNode;
import com.viaoa.util.OADate;

public class PosRestTest {

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

		restClient.setBaseUrl("http://localhost:18080/posrest");
		// restClient.setUserPw("admin", "admin");

		final PosRestInterface posRest = restClient.getInstance(PosRestInterface.class);

		String s, s2;
		String json;
		OAJsonObjectNode objNode;
		OAJsonArrayNode arrayNode;
		OAJsonArrayNode arrayNodeAll;

		OAJson oajson = new OAJson();

		json = posRest.getByDate((OADate) (new OADate()).addDays(-180), new OADate());
		arrayNode = oajson.loadArray(json);
		int x = arrayNode.getSize();
		for (int i = 0; i < x; i++) {
			objNode = arrayNode.getObject(i);
			objNode.set("orderedByUser", "testE");
			int id = objNode.getInt("id");
			json = objNode.toJson();
			String json2 = "";
			try {
				json2 = posRest.update(id, json);
			} catch (Exception e) {
				e.printStackTrace();
				int xz = 1;
				xz++;
			}
			int xxx = 4;
			xxx++;
		}

		int xx = 4;
		xx++;
	}

	public static void main(String[] args) throws Exception {
		PosRestTest test = new PosRestTest();
		test.test();
		System.out.println("DONE");
	}

}

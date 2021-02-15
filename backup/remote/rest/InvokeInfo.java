package com.oreillyauto.storepurchaseorder.remote.rest;

import java.util.ArrayList;
import java.util.HashMap;

import com.viaoa.json.node.OAJsonRootNode;
import com.viaoa.template.OATemplate;

public class InvokeInfo {

	public MethodInfo methodInfo;

	public Object[] args;

	public String urlPath;
	public String urlQuery;
	public String searchQuery; // whereClause and orderBy

	public String jsonBody;

	public HashMap<String, String> hsHeader;
	public HashMap<String, String> hsCookie;

	public Class methodReturnClass;

	public InvokeInfo() {

	}

	//qqqqqqqqqqqq

	public OATemplate pathTemplate;
	public String objectMethodName;
	public OAJsonRootNode jsonNodeBody;
	public int pageNumber;
	public ArrayList<String> alQueryWhereParams;
	public String[] responseIncludePropertyPaths;
	public ArrayList<String> alUrlQueryParams;

	public String response;
	public int responseCode;

}
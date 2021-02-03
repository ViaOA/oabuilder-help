package com.oreillyauto.storepurchaseorder.remote.rest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod;

class MethodInfo {

	Method method;

	String name;
	String urlPath;
	String extraUrlQueryParams;
	ArrayList<ParamInfo> alParamInfo = new ArrayList();

	Class origReturnClass;
	Class returnClass;

	RestMethod.MethodType methodType;
	ReturnClassType returnClassType;

	int includeReferenceLevelAmount;
	List<String> lstIncludePropertyPaths;

	String queryWhereClause;
	String queryOrderBy;

	String pathTemplate;

	public MethodInfo(Method method) {
		this.method = method;
	}

	public static enum ReturnClassType {
		Unassigned,
		Void,
		String,
		Array,
		List,
		JsonNode
	}

}

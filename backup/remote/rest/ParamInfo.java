package com.oreillyauto.storepurchaseorder.remote.rest;

import java.util.ArrayList;

import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam;

class ParamInfo {

	String name;
	boolean bNameAssigned;

	Class rpParamClass;
	Class origParamClass; // could be array or list, etc

	Class paramClass; // could be null

	String format;

	ClassType classType;

	ArrayList<String> alIncludePropertyPaths;
	int includeReferenceLevelAmount;

	RestParam.ParamType paramType;

	public static enum ClassType {
		Unassigned,
		String,
		Date,
		DateTime,
		Time,
		Array,
		List,
		JsonNode
	}

}
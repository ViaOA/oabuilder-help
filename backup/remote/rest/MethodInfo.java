package com.oreillyauto.storepurchaseorder.remote.rest;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod.MethodType;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam.ParamType;
import com.viaoa.json.OAJson;
import com.viaoa.json.OAJsonUtil;
import com.viaoa.json.node.OAJsonArrayNode;
import com.viaoa.json.node.OAJsonNode;
import com.viaoa.json.node.OAJsonObjectNode;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.template.OATemplate;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAReflect;
import com.viaoa.util.OAString;

class MethodInfo {

	RestMethod restMethod;

	Method method;

	String name;
	String urlPath;
	String derivedUrlPath;
	public OATemplate urlPathTemplate;

	String urlQuery;

	ArrayList<ParamInfo> alParamInfo = new ArrayList();

	String objectMethodName;

	Class origReturnClass;
	Class rmReturnClass;
	Class returnClass;

	RestMethod.MethodType methodType;
	ReturnClassType returnClassType;

	public static enum ReturnClassType {
		Unassigned,
		Void,
		String,
		Array,
		List,
		Hub,
		JsonNode
	}

	int includeReferenceLevelAmount;
	List<String> alIncludePropertyPaths;

	String searchWhere;
	String searchOrderBy;

	public MethodInfo(Method method) {
		this.method = method;
		this.restMethod = method.getAnnotation(RestMethod.class);
	}

	public void verify(List<String> alErrors) {
		if (alErrors == null) {
			return;
		}

		if (restMethod == null) {
			alErrors.add("RestMethod annotation is missing");
			return;
		}

		String msgPrefix = String.format("method name=%s, type=%s, ", name, methodType);

		verifyMethodType(msgPrefix, alErrors);

		verifyUrlPath(msgPrefix, alErrors);
		verifyDerviedUrlPath(msgPrefix, alErrors);

		verifyUrlQuery(msgPrefix, alErrors);

		verifyIncludePropertyPaths(msgPrefix, alErrors);
		verifyIncludeReferenceLevelAmount(msgPrefix, alErrors);

		verifyMethodReturnClass(msgPrefix, alErrors);

		verifyMethodTypeGET(msgPrefix, alErrors);
		verifyMethodTypeOAGet(msgPrefix, alErrors);
		verifyMethodTypeOASearch(msgPrefix, alErrors);
		verifyMethodTypePOST(msgPrefix, alErrors);
		verifyMethodTypePUT(msgPrefix, alErrors);
		verifyMethodTypePATCH(msgPrefix, alErrors);
		verifyMethodTypeOAObjectMethodCall(msgPrefix, alErrors);
		verifyMethodTypeOARemote(msgPrefix, alErrors);
		verifyMethodTypeOAInsert(msgPrefix, alErrors);
		verifyMethodTypeOAUpdate(msgPrefix, alErrors);
		verifyMethodTypeOADelete(msgPrefix, alErrors);

		verifyParamAmounts(msgPrefix, alErrors);
		verifyRestParams(msgPrefix, alErrors);

		//qqq remove: verifyParmOAObject(msgPrefix, alErrors);
	}

	public void verifyRestParams(String msgPrefix, List<String> alErrors) {
		String origMsgPrefix = msgPrefix;
		for (ParamInfo pi : alParamInfo) {
			msgPrefix = origMsgPrefix + "paramType=" + pi.paramType + ", ";

			verifyParamType(msgPrefix, alErrors, pi, ParamType.Ignore, false, false, false, false);
			verifyParamType(msgPrefix, alErrors, pi, ParamType.MethodUrlPath, false, false, false, false, true);
			verifyParamType(msgPrefix, alErrors, pi, ParamType.MethodSearchWhere, false, false, false, false, true);
			verifyParamType(msgPrefix, alErrors, pi, ParamType.MethodSearchOrderBy, false, false, false, false, true);
			verifyParamType(msgPrefix, alErrors, pi, ParamType.UrlPathTagValue, true, false, false, false);
			verifyParamType(msgPrefix, alErrors, pi, ParamType.UrlQueryNameValue, true, false, false, false);

			if (verifyParamType(msgPrefix, alErrors, pi, ParamType.MethodReturnClass, false, false, false, false)) {
				if (!Class.class.equals(pi.paramClass)) {
					String s = "type should be of type Class";
					alErrors.add(msgPrefix + s);
				}
			}

			verifyParamType(msgPrefix, alErrors, pi, ParamType.SearchWhereTagValue, false, false, false, false);
			verifyParamType(msgPrefix, alErrors, pi, ParamType.SearchWhereAddNameValue, true, false, false, false);

			if (verifyParamType(msgPrefix, alErrors, pi, ParamType.OAObject, false, false, false, true)) {
				if (!OAObject.class.isAssignableFrom(pi.paramClass)) {
					String s = "type should be of class type OAObject";
					alErrors.add(msgPrefix + s);
				}
			}

			verifyParamType(msgPrefix, alErrors, pi, ParamType.OAObjectId, true, false, false, false);
			verifyParamType(msgPrefix, alErrors, pi, ParamType.OAObjectMethodCallArg, false, false, false, true);

			boolean bCheckName = false;
			if (verifyParamType(msgPrefix, alErrors, pi, ParamType.BodyObject, true, false, false, true)) {
				bCheckName = true;
			}

			if (verifyParamType(msgPrefix, alErrors, pi, ParamType.BodyJson, false, false, false, true, true)) {
				bCheckName = true;
			}
			if (bCheckName) {
				int cnt = 0;
				boolean b = false;
				for (ParamInfo pix : alParamInfo) {
					if (pix.paramType == ParamType.BodyObject || pix.paramType == ParamType.BodyJson) {
						cnt++;
						if (!pix.bNameAssigned) {
							b = true;
						}
					}
				}
				if (b && cnt > 1) {
					String s = "more then one BodyObject/BodyJson used, must have name assigned";
					alErrors.add(msgPrefix + s);
				}
			}

			verifyParamType(msgPrefix, alErrors, pi, ParamType.Header, false, false, false, false);
			verifyParamType(msgPrefix, alErrors, pi, ParamType.Cookie, false, false, false, false);
			if (verifyParamType(msgPrefix, alErrors, pi, ParamType.PageNumber, false, false, false, false)) {
				if (!OAReflect.isNumber(pi.paramClass)) {
					String s = "type should be of type Number";
					alErrors.add(msgPrefix + s);
				}
			}
			if (verifyParamType(msgPrefix, alErrors, pi, ParamType.ResponseIncludePropertyPaths, false, false, false, false)) {
				if (!String.class.equals(returnClass)) {
					String s = "type should be of class type String or String[]";
					alErrors.add(msgPrefix + s);
				}
			}
		}
	}

	public boolean verifyParamType(String msgPrefix, List<String> alErrors, ParamInfo pi, ParamType ptCheck,
			boolean bUsesName,
			boolean bUsesParamClass,
			boolean bUsesFormat,
			boolean bUsesIncludePPs) {
		return verifyParamType(msgPrefix, alErrors, pi, ptCheck, bUsesName, bUsesParamClass, bUsesFormat, bUsesIncludePPs, false);
	}

	public boolean verifyParamType(String msgPrefix, List<String> alErrors, ParamInfo pi, ParamType ptCheck,
			boolean bUsesName,
			boolean bUsesParamClass,
			boolean bUsesFormat,
			boolean bUsesIncludePPs,
			boolean bTypeString) {
		if (pi.paramType != ptCheck) {
			return false;
		}

		if (bTypeString) {
			if (pi.classType == null || !String.class.equals(pi.paramClass)) {
				String s = "type needs to be String";
				alErrors.add(msgPrefix + s);
			}
		}

		if (!bUsesName && pi.bNameAssigned && OAString.isNotEmpty(pi.name)) {
			String s = "does not need name " + pi.name;
			alErrors.add(msgPrefix + s);
		}
		if (!bUsesParamClass && pi.rpParamClass != null) {
			String s = "does not need paramClass " + pi.rpParamClass.getSimpleName();
			alErrors.add(msgPrefix + s);
		}
		if (!bUsesFormat && OAString.isNotEmpty(pi.format)) {
			String s = "does not need param.format";
			alErrors.add(msgPrefix + s);
		}
		if (!bUsesIncludePPs && pi.alIncludePropertyPaths != null && pi.alIncludePropertyPaths.size() > 0) {
			String s = "does not need param.includePropertyPath(s)";
			alErrors.add(msgPrefix + s);
		}
		if (!bUsesIncludePPs && pi.includeReferenceLevelAmount > 0) {
			String s = "does not need param.includePropertyPath(s)";
			alErrors.add(msgPrefix + s);
		}
		return true;
	}

	public void verifyParamAmounts(String msgPrefix, List<String> alErrors) {
		HashSet<String> hs = new HashSet();
		for (ParamInfo pi : alParamInfo) {
			if (
			// || pi.paramType == ParamType.Ignore
			pi.paramType == ParamType.MethodUrlPath
					|| pi.paramType == ParamType.MethodSearchWhere
					|| pi.paramType == ParamType.MethodSearchOrderBy
					// || pi.paramType == ParamType.UrlPathValue
					// || pi.paramType == ParamType.UrlQueryValue
					|| pi.paramType == ParamType.MethodReturnClass
					|| pi.paramType == ParamType.SearchWhereTagValue
					|| pi.paramType == ParamType.SearchWhereAddNameValue
					|| pi.paramType == ParamType.OAObject
					|| pi.paramType == ParamType.OAObjectId
					// || pi.paramType == ParamType.OAObjectMethodCallArg
					|| pi.paramType == ParamType.BodyObject
					|| pi.paramType == ParamType.BodyJson
					// || pi.paramType == ParamType.Header
					// || pi.paramType == ParamType.Cookie
					|| pi.paramType == ParamType.PageNumber
					|| pi.paramType == ParamType.ResponseIncludePropertyPaths) {

				String s = pi.paramType.toString();
				if (hs.contains(s)) {
					s = String.format("only one paramType=%s is allowed", s);
					alErrors.add(msgPrefix + s);

				}
				hs.add(s);
			}
		}
	}

	protected void verifyMethodType(String msgPrefix, List<String> alErrors) {
		if (methodType == null) {
			String s = "methodType can not be null";
			alErrors.add(msgPrefix + s);
		}
		if (methodType == MethodType.Unassigned) {
			String s = "methodType can not be 'Unassigned'";
			alErrors.add(msgPrefix + s);
		}
	}

	protected void verifyMethodTypeGET(String msgPrefix, List<String> alErrors) {
		if (methodType != MethodType.GET) {
			return;
		}

		// done by verifyUrlPath
		// if (OAString.isNotEmpty(urlPath)) {

		// no validation
		// if (OAString.isNotEmpty(urlQuery)) {

		// no validation, done by verifyMethodReturnClass
		// if (!OAObject.class.isAssignableFrom(origReturnClass)) {

		if (OAString.isNotEmpty(searchWhere)) {
			String s = "searchWhere only valid for methodType=OASearch";
			alErrors.add(msgPrefix + s);
		}

		if (OAString.isNotEmpty(searchOrderBy)) {
			String s = "searchWhere only valid for methodType=OASearch";
			alErrors.add(msgPrefix + s);
		}

		if (OAString.isNotEmpty(objectMethodName)) {
			String s = "methodName only valid for methodType=OAObjectMethodCall";
			alErrors.add(msgPrefix + s);
		}

		for (ParamInfo pi : alParamInfo) {
			if (false
					|| pi.paramType == ParamType.Ignore
					|| pi.paramType == ParamType.MethodUrlPath
					// || pi.paramType == ParamType.MethodSearchWhere
					// || pi.paramType == ParamType.MethodSearchOrderBy
					|| pi.paramType == ParamType.UrlPathTagValue
					|| pi.paramType == ParamType.UrlQueryNameValue
					|| pi.paramType == ParamType.MethodReturnClass
					// || pi.paramType == ParamType.SearchWhereValue
					// || pi.paramType == ParamType.SearchWhereNameValue
					// || pi.paramType == ParamType.SearchWhereValue
					// || pi.paramType == ParamType.SearchWhereNameValue
					// || pi.paramType == ParamType.OAObject
					// || pi.paramType == ParamType.OAObjectId
					// || pi.paramType == ParamType.OAObjectMethodCallArg
					|| pi.paramType == ParamType.BodyObject
					|| pi.paramType == ParamType.BodyJson
					|| pi.paramType == ParamType.Header
					|| pi.paramType == ParamType.Cookie
					|| pi.paramType == ParamType.PageNumber
					|| pi.paramType == ParamType.ResponseIncludePropertyPaths) {
				// valid
			} else {
				String s = String
						.format("paramType=%s not allowed with %s",
								pi.paramType, methodType);
				alErrors.add(msgPrefix + s);
			}
		}
	}

	protected void verifyMethodTypePOST(String msgPrefix, List<String> alErrors) {
		if (methodType != MethodType.POST) {
			return;
		}
		_verifyMethodTypeX(msgPrefix, alErrors);
	}

	protected void verifyMethodTypePUT(String msgPrefix, List<String> alErrors) {
		if (methodType != MethodType.PUT) {
			return;
		}
		_verifyMethodTypeX(msgPrefix, alErrors);
	}

	protected void verifyMethodTypePATCH(String msgPrefix, List<String> alErrors) {
		if (methodType != MethodType.PATCH) {
			return;
		}
		_verifyMethodTypeX(msgPrefix, alErrors);
	}

	protected void _verifyMethodTypeX(String msgPrefix, List<String> alErrors) {

		// done by verifyUrlPath
		// if (OAString.isNotEmpty(urlPath)) {

		// no validation
		// if (OAString.isNotEmpty(urlQuery)) {

		// no validation, done by verifyMethodReturnClass
		// if (!OAObject.class.isAssignableFrom(origReturnClass)) {

		if (OAString.isNotEmpty(searchWhere)) {
			String s = "searchWhere only valid for methodType=OASearch";
			alErrors.add(msgPrefix + s);
		}

		if (OAString.isNotEmpty(searchOrderBy)) {
			String s = "searchOrderBy only valid for methodType=OASearch";
			alErrors.add(msgPrefix + s);
		}

		if (OAString.isNotEmpty(objectMethodName)) {
			String s = "methodName only valid for methodType=OAObjectMethodCall";
			alErrors.add(msgPrefix + s);
		}

		for (ParamInfo pi : alParamInfo) {
			if (false
					|| pi.paramType == ParamType.Ignore
					|| pi.paramType == ParamType.MethodUrlPath
					// || pi.paramType == ParamType.MethodSearchWhere
					// || pi.paramType == ParamType.MethodSearchOrderBy
					|| pi.paramType == ParamType.UrlPathTagValue
					|| pi.paramType == ParamType.UrlQueryNameValue
					|| pi.paramType == ParamType.MethodReturnClass
					// || pi.paramType == ParamType.SearchWhereValue
					// || pi.paramType == ParamType.SearchWhereNameValue
					// || pi.paramType == ParamType.SearchWhereValue
					// || pi.paramType == ParamType.SearchWhereNameValue
					// || pi.paramType == ParamType.OAObject
					// || pi.paramType == ParamType.OAObjectId
					// || pi.paramType == ParamType.OAObjectMethodCallArg
					|| pi.paramType == ParamType.BodyObject
					|| pi.paramType == ParamType.BodyJson
					|| pi.paramType == ParamType.Header
					|| pi.paramType == ParamType.Cookie
					|| pi.paramType == ParamType.PageNumber
					|| pi.paramType == ParamType.ResponseIncludePropertyPaths) {
				// valid
			} else {
				String s = String
						.format("paramType=%s not allowed with %s",
								pi.paramType, methodType);
				alErrors.add(msgPrefix + s);
			}
		}
	}

	protected void verifyMethodTypeOAGet(String msgPrefix, List<String> alErrors) {
		if (methodType != MethodType.OAGet) {
			return;
		}

		// done by verifyUrlPath
		// if (OAString.isNotEmpty(urlPath)) {

		// no validation
		// if (OAString.isNotEmpty(urlQuery)) {

		// also by verifyMethodReturnClass
		if (!OAObject.class.isAssignableFrom(origReturnClass)) {
			String s = "return value must be an OAObject";
			alErrors.add(msgPrefix + s);
		}

		if (OAString.isNotEmpty(searchWhere)) {
			String s = "searchWhere only valid for methodType=OASearch";
			alErrors.add(msgPrefix + s);
		}

		if (OAString.isNotEmpty(searchOrderBy)) {
			String s = "searchOrderBy only valid for methodType=OASearch";
			alErrors.add(msgPrefix + s);
		}

		// no validation
		// lstIncludePropertyPaths

		// no validation
		// includeReferenceLevelAmount

		if (OAString.isNotEmpty(objectMethodName)) {
			String s = "methodName only valid for methodType=OAObjectMethodCall";
			alErrors.add(msgPrefix + s);
		}

		boolean b = false;
		for (ParamInfo pi : alParamInfo) {
			b |= (pi.paramType == ParamType.OAObjectId);

			if (false
					|| pi.paramType == ParamType.Ignore
					// || pi.paramType == ParamType.MethodUrlPath
					// || pi.paramType == ParamType.MethodSearchWhere
					// || pi.paramType == ParamType.MethodSearchOrderBy
					// || pi.paramType == ParamType.UrlPathValue
					|| pi.paramType == ParamType.UrlQueryNameValue
					// || pi.paramType == ParamType.MethodReturnClass
					// || pi.paramType == ParamType.SearchWhereValue
					// || pi.paramType == ParamType.SearchWhereNameValue
					// || pi.paramType == ParamType.OAObject
					|| pi.paramType == ParamType.OAObjectId
					// || pi.paramType == ParamType.OAObjectMethodCallArg
					// || pi.paramType == ParamType.BodyObject
					// || pi.paramType == ParamType.BodyJson
					|| pi.paramType == ParamType.Header
					|| pi.paramType == ParamType.Cookie
					// || pi.paramType == ParamType.PageNumber
					|| pi.paramType == ParamType.ResponseIncludePropertyPaths) {
				// valid
			} else {
				String s = String
						.format("paramType=%s not allowed with %s",
								pi.paramType, methodType);
				alErrors.add(msgPrefix + s);
			}
		}
		if (!b) {
			String s = "requires param with ParamType=OAObjectId";
			alErrors.add(msgPrefix + s);
		}
	}

	protected void verifyMethodTypeOASearch(String msgPrefix, List<String> alErrors) {
		if (methodType != MethodType.OASearch) {
			return;
		}

		// done by verifyUrlPath
		// if (OAString.isNotEmpty(urlPath)) {

		// no validation
		// if (OAString.isNotEmpty(urlQuery)) {

		// also by verifyMethodReturnClass
		if (returnClassType != ReturnClassType.Array && returnClassType != ReturnClassType.List && returnClassType != ReturnClassType.Hub) {
			String s = "returnClassType must be for (array, list, hub)";
			alErrors.add(msgPrefix + s);
		}
		if (!OAObject.class.isAssignableFrom(returnClass)) {
			String s = "returnClassType must be for (array, list, hub) of OAObjects";
			alErrors.add(msgPrefix + s);
		}

		boolean bSearchFound = OAString.isNotEmpty(searchWhere);

		// if (OAString.isNotEmpty(searchWhere)) {

		// if (OAString.isNotEmpty(searchOrderBy)) {

		// no validation
		// lstIncludePropertyPaths

		// no validation
		// includeReferenceLevelAmount

		if (OAString.isNotEmpty(objectMethodName)) {
			String s = "methodName only valid for methodType=OAObjectMethodCall";
			alErrors.add(msgPrefix + s);
		}

		boolean b = false;
		for (ParamInfo pi : alParamInfo) {
			bSearchFound |= (pi.paramType == ParamType.MethodSearchWhere);
			bSearchFound |= (pi.paramType == ParamType.SearchWhereAddNameValue);

			if (false
					|| pi.paramType == ParamType.Ignore
					// || pi.paramType == ParamType.MethodUrlPath
					|| pi.paramType == ParamType.MethodSearchWhere
					|| pi.paramType == ParamType.MethodSearchOrderBy
					// || pi.paramType == ParamType.UrlPathValue
					|| pi.paramType == ParamType.UrlQueryNameValue
					// || pi.paramType == ParamType.MethodReturnClass
					|| pi.paramType == ParamType.SearchWhereTagValue
					|| pi.paramType == ParamType.SearchWhereAddNameValue
					// || pi.paramType == ParamType.OAObject
					// || pi.paramType == ParamType.OAObjectId
					// || pi.paramType == ParamType.OAObjectMethodCallArg
					// || pi.paramType == ParamType.BodyObject
					// || pi.paramType == ParamType.BodyJson
					|| pi.paramType == ParamType.Header
					|| pi.paramType == ParamType.Cookie
					|| pi.paramType == ParamType.PageNumber
					|| pi.paramType == ParamType.ResponseIncludePropertyPaths) {
				// valid
			} else {
				String s = String
						.format("paramType=%s not allowed with %s",
								pi.paramType, methodType);
				alErrors.add(msgPrefix + s);
			}
		}
		if (!bSearchFound) {
			String s = "requires SearchWhere, param methodSearchWhere, param searchWhereNameValue";
			alErrors.add(msgPrefix + s);
		}
	}

	protected void verifyMethodTypeOAObjectMethodCall(String msgPrefix, List<String> alErrors) {
		if (methodType != MethodType.OAObjectMethodCall) {
			return;
		}

		// done by verifyUrlPath
		// if (OAString.isNotEmpty(urlPath)) {

		// no validation
		// if (OAString.isNotEmpty(urlQuery)) {

		// also by verifyMethodReturnClass
		// if (!OAObject.class.isAssignableFrom(origReturnClass)) {

		if (OAString.isNotEmpty(searchWhere)) {
			String s = "searchWhere only valid for methodType=OASearch";
			alErrors.add(msgPrefix + s);
		}

		if (OAString.isNotEmpty(searchOrderBy)) {
			String s = "searchOrderBy only valid for methodType=OASearch";
			alErrors.add(msgPrefix + s);
		}

		if (OAString.isEmpty(objectMethodName)) {
			String s = "methodName is required";
			alErrors.add(msgPrefix + s);
		}

		boolean b = false;
		for (ParamInfo pi : alParamInfo) {
			if (pi.paramType == ParamType.OAObject) {
				if (!OAObject.class.isAssignableFrom(pi.origParamClass)) {
					String s = String
							.format("paramType=%s must be for an OAObject",
									pi.paramType);
					alErrors.add(msgPrefix + s);
				}
				b = true;
			}

			if (false
					|| pi.paramType == ParamType.Ignore
					// || pi.paramType == ParamType.MethodUrlPath
					// || pi.paramType == ParamType.MethodSearchWhere
					// || pi.paramType == ParamType.MethodSearchOrderBy
					// || pi.paramType == ParamType.UrlPathValue
					|| pi.paramType == ParamType.UrlQueryNameValue
					|| pi.paramType == ParamType.MethodReturnClass
					// || pi.paramType == ParamType.SearchWhereValue
					// || pi.paramType == ParamType.SearchWhereNameValue
					|| pi.paramType == ParamType.OAObject
					// || pi.paramType == ParamType.OAObjectId
					|| pi.paramType == ParamType.OAObjectMethodCallArg
					|| pi.paramType == ParamType.BodyObject
					|| pi.paramType == ParamType.BodyJson
					|| pi.paramType == ParamType.Header
					|| pi.paramType == ParamType.Cookie
					|| pi.paramType == ParamType.PageNumber
					|| pi.paramType == ParamType.ResponseIncludePropertyPaths) {
				// valid
			} else {
				String s = String
						.format("paramType=%s not allowed with %s",
								pi.paramType, methodType);
				alErrors.add(msgPrefix + s);
			}
		}
		if (!b) {
			String s = "requires param with ParamType=OAObject";
			alErrors.add(msgPrefix + s);
		}
	}

	protected void verifyMethodTypeOARemote(String msgPrefix, List<String> alErrors) {
		if (methodType != MethodType.OARemote) {
			return;
		}
		String s = "for internal use only, used by proxy oninvoke to send remote method call to OARestServlet";
		alErrors.add(msgPrefix + s);
	}

	protected void verifyMethodTypeOAInsert(String msgPrefix, List<String> alErrors) {
		if (methodType != MethodType.OAInsert) {
			return;
		}

		// done by verifyUrlPath
		// if (OAString.isNotEmpty(urlPath)) {

		// no validation
		// if (OAString.isNotEmpty(urlQuery)) {

		// also by verifyMethodReturnClass
		// if (!OAObject.class.isAssignableFrom(origReturnClass)) {

		if (OAString.isNotEmpty(searchWhere)) {
			String s = "searchWhere only valid for methodType=OASearch";
			alErrors.add(msgPrefix + s);
		}

		if (OAString.isNotEmpty(searchOrderBy)) {
			String s = "searchOrderBy only valid for methodType=OASearch";
			alErrors.add(msgPrefix + s);
		}

		if (OAString.isNotEmpty(objectMethodName)) {
			String s = "methodName is only used for OAObjectMethodCall";
			alErrors.add(msgPrefix + s);
		}

		boolean b = false;
		for (ParamInfo pi : alParamInfo) {
			if (pi.paramType == ParamType.OAObject) {
				if (!OAObject.class.isAssignableFrom(pi.origParamClass)) {
					String s = String
							.format("paramType=%s must be for an OAObject",
									pi.paramType);
					alErrors.add(msgPrefix + s);
				}
				b = true;
			}

			if (false
					|| pi.paramType == ParamType.Ignore
					// || pi.paramType == ParamType.MethodUrlPath
					// || pi.paramType == ParamType.MethodSearchWhere
					// || pi.paramType == ParamType.MethodSearchOrderBy
					// || pi.paramType == ParamType.UrlPathValue
					|| pi.paramType == ParamType.UrlQueryNameValue
					// || pi.paramType == ParamType.MethodReturnClass
					// || pi.paramType == ParamType.SearchWhereValue
					// || pi.paramType == ParamType.SearchWhereNameValue
					|| pi.paramType == ParamType.OAObject
					// || pi.paramType == ParamType.OAObjectId
					// || pi.paramType == ParamType.OAObjectMethodCallArg
					// || pi.paramType == ParamType.BodyObject
					// || pi.paramType == ParamType.BodyJson
					|| pi.paramType == ParamType.Header
					|| pi.paramType == ParamType.Cookie
					// || pi.paramType == ParamType.PageNumber
					|| pi.paramType == ParamType.ResponseIncludePropertyPaths) {
				// valid
			} else {
				String s = String
						.format("paramType=%s not allowed with %s",
								pi.paramType, methodType);
				alErrors.add(msgPrefix + s);
			}
		}
		if (!b) {
			String s = "requires param with ParamType=OAObject";
			alErrors.add(msgPrefix + s);
		}
	}

	protected void verifyMethodTypeOAUpdate(String msgPrefix, List<String> alErrors) {
		if (methodType != MethodType.OAUpdate) {
			return;
		}

		// done by verifyUrlPath
		// if (OAString.isNotEmpty(urlPath)) {

		// no validation
		// if (OAString.isNotEmpty(urlQuery)) {

		// also by verifyMethodReturnClass
		// if (!OAObject.class.isAssignableFrom(origReturnClass)) {

		if (OAString.isNotEmpty(searchWhere)) {
			String s = "searchWhere only valid for methodType=OASearch";
			alErrors.add(msgPrefix + s);
		}

		if (OAString.isNotEmpty(searchOrderBy)) {
			String s = "searchOrderBy only valid for methodType=OASearch";
			alErrors.add(msgPrefix + s);
		}

		if (OAString.isNotEmpty(objectMethodName)) {
			String s = "methodName is only used for OAObjectMethodCall";
			alErrors.add(msgPrefix + s);
		}

		boolean b = false;
		for (ParamInfo pi : alParamInfo) {
			if (pi.paramType == ParamType.OAObject) {
				if (!OAObject.class.isAssignableFrom(pi.origParamClass)) {
					String s = String
							.format("paramType=%s must be for an OAObject",
									pi.paramType);
					alErrors.add(msgPrefix + s);
				}
				b = true;
			}

			if (false
					|| pi.paramType == ParamType.Ignore
					// || pi.paramType == ParamType.MethodUrlPath
					// || pi.paramType == ParamType.MethodSearchWhere
					// || pi.paramType == ParamType.MethodSearchOrderBy
					// || pi.paramType == ParamType.UrlPathValue
					|| pi.paramType == ParamType.UrlQueryNameValue
					// || pi.paramType == ParamType.MethodReturnClass
					// || pi.paramType == ParamType.SearchWhereValue
					// || pi.paramType == ParamType.SearchWhereNameValue
					|| pi.paramType == ParamType.OAObject
					// || pi.paramType == ParamType.OAObjectId
					// || pi.paramType == ParamType.OAObjectMethodCallArg
					// || pi.paramType == ParamType.BodyObject
					// || pi.paramType == ParamType.BodyJson
					|| pi.paramType == ParamType.Header
					|| pi.paramType == ParamType.Cookie
					// || pi.paramType == ParamType.PageNumber
					|| pi.paramType == ParamType.ResponseIncludePropertyPaths) {
				// valid
			} else {
				String s = String
						.format("paramType=%s not allowed with %s",
								pi.paramType, methodType);
				alErrors.add(msgPrefix + s);
			}
		}
		if (!b) {
			String s = "requires param with ParamType=OAObject";
			alErrors.add(msgPrefix + s);
		}
	}

	protected void verifyMethodTypeOADelete(String msgPrefix, List<String> alErrors) {
		if (methodType != MethodType.OAUpdate) {
			return;
		}

		// done by verifyUrlPath
		// if (OAString.isNotEmpty(urlPath)) {

		// no validation
		// if (OAString.isNotEmpty(urlQuery)) {

		// also by verifyMethodReturnClass
		// if (!OAObject.class.isAssignableFrom(origReturnClass)) {

		if (OAString.isNotEmpty(searchWhere)) {
			String s = "searchWhere only valid for methodType=OASearch";
			alErrors.add(msgPrefix + s);
		}

		if (OAString.isNotEmpty(searchOrderBy)) {
			String s = "searchOrderBy only valid for methodType=OASearch";
			alErrors.add(msgPrefix + s);
		}

		if (alIncludePropertyPaths != null && alIncludePropertyPaths.size() > 0) {
			String s = "IncludePropertyPaths not valid for OADelete";
			alErrors.add(msgPrefix + s);
		}

		// no validation
		// includeReferenceLevelAmount

		if (OAString.isNotEmpty(objectMethodName)) {
			String s = "methodName is only used for OAObjectMethodCall";
			alErrors.add(msgPrefix + s);
		}

		boolean b = false;
		for (ParamInfo pi : alParamInfo) {
			if (pi.paramType == ParamType.OAObject) {
				if (!OAObject.class.isAssignableFrom(pi.origParamClass)) {
					String s = String
							.format("paramType=%s must be for an OAObject",
									pi.paramType);
					alErrors.add(msgPrefix + s);
				}
				b = true;
			}
			b |= (pi.paramType == ParamType.OAObjectId);

			if (false
					|| pi.paramType == ParamType.Ignore
					// || pi.paramType == ParamType.MethodUrlPath
					// || pi.paramType == ParamType.MethodSearchWhere
					// || pi.paramType == ParamType.MethodSearchOrderBy
					// || pi.paramType == ParamType.UrlPathValue
					|| pi.paramType == ParamType.UrlQueryNameValue
					// || pi.paramType == ParamType.MethodReturnClass
					// || pi.paramType == ParamType.SearchWhereValue
					// || pi.paramType == ParamType.SearchWhereNameValue
					|| pi.paramType == ParamType.OAObject
					|| pi.paramType == ParamType.OAObjectId
					// || pi.paramType == ParamType.OAObjectMethodCallArg
					// || pi.paramType == ParamType.BodyObject
					// || pi.paramType == ParamType.BodyJson
					|| pi.paramType == ParamType.Header
					|| pi.paramType == ParamType.Cookie
					// || pi.paramType == ParamType.PageNumber
					|| pi.paramType == ParamType.ResponseIncludePropertyPaths) {
				// valid
			} else {
				String s = String
						.format("paramType=%s not allowed with %s",
								pi.paramType, methodType);
				alErrors.add(msgPrefix + s);
			}
		}
		if (!b) {
			String s = "requires param with ParamType=OAObject";
			alErrors.add(msgPrefix + s);
		}
	}

	protected void verifyUrlQuery(String msgPrefix, List<String> alErrors) {
		int cnt = 0;
		for (ParamInfo pi : alParamInfo) {
			if (pi.paramType != ParamType.UrlQueryNameValue) {
				continue;
			}
			cnt++;
			if (!pi.bNameAssigned) {
				String s = "param type=UrlQueryValue needs to define a name";
				alErrors.add(msgPrefix + s);
			}
		}
	}

	protected void verifyIncludePropertyPaths(String msgPrefix, List<String> alErrors) {
		if (alIncludePropertyPaths == null || alIncludePropertyPaths.size() == 0) {
			return;
		}

		if (!OAObject.class.isAssignableFrom(returnClass)) {
			String s = "includePropertyPaths not needed, since return class is not OAObject";
			alErrors.add(msgPrefix + s);
		}
	}

	protected void verifyIncludeReferenceLevelAmount(String msgPrefix, List<String> alErrors) {
		if (includeReferenceLevelAmount == 0) {
			return;
		}

		if (!OAObject.class.isAssignableFrom(returnClass)) {
			String s = "includeReferenceLevelAmount > 0, since return class is not OAObject";
			alErrors.add(msgPrefix + s);
		}
	}

	/* not needed or accurate qqqqqq
	protected void verifyParmOAObject(String msgPrefix, List<String> alErrors) {
		boolean bUsesOAObject = methodType.name().startsWith("OA");
	
		int cnt = 0;
		for (ParamInfo pi : alParamInfo) {
			if (pi.paramType == ParamType.OAObject) {
				cnt++;
			}
		}
		if (cnt != (bUsesOAObject ? 1 : 0)) {
			String s = String.format("expected %d params that are paramType=OAObject, found %d", (bUsesOAObject ? 1 : 0), cnt);
			alErrors.add(msgPrefix + s);
		}
	}
	*/

	protected void verifyMethodReturnClass(String msgPrefix, List<String> alErrors) {
		boolean bFoundParam = false;
		for (ParamInfo pi : alParamInfo) {
			if (pi.paramType == ParamType.MethodReturnClass) {
				if (bFoundParam) {
					String s = "paramType == ParamType.MethodReturnClass, not more then one is permitted";
					alErrors.add(msgPrefix + s);
				}
				if (!pi.paramClass.equals(Class.class)) {
					String s = "paramType == ParamType.MethodReturnClass, but param class type is not Class";
					alErrors.add(msgPrefix + s);
				} else {
					bFoundParam = true;
				}
			}
		}

		if (returnClass == null && !bFoundParam && returnClass == null) {
			String s = "returnClass is not known, need to use one of the following: array, collection<generic>, return class, specify using method.returnClass, or param.methodReturnClass";
			alErrors.add(msgPrefix + s);
		}

		if (returnClass != null && bFoundParam) {
			String s = "returnClass is known, dont need to use param.methodReturnClass";
			alErrors.add(msgPrefix + s);
		}
		if (returnClass != null && rmReturnClass != null) {
			String s = "returnClass is known, dont need to use methodType.ReturnClass";
			alErrors.add(msgPrefix + s);
		}
	}

	protected void verifyUrlPath(String msgPrefix, List<String> alErrors) {
		if (!restMethod.methodType().requiresUrlPath) {
			if (OAString.isNotEmpty(urlPath)) {
				String s = "creates it's own UrlPath and should not have a urlPath defined";
				alErrors.add(msgPrefix + s);
			}
			for (ParamInfo pi : alParamInfo) {
				if (pi.paramType == ParamType.MethodUrlPath) {
					String s = "creates it's own UrlPath, should not have paramType=MethodUrlPath";
					alErrors.add(msgPrefix + s);
				}
			}
			for (ParamInfo pi : alParamInfo) {
				if (pi.paramType == ParamType.UrlPathTagValue) {
					String s = "creates it's own UrlPath, should not have paramType=UrlPathValue";
					alErrors.add(msgPrefix + s);
				}
			}
			return;
		}

		// URL path is required

		if (OAString.isEmpty(urlPath)) {
			boolean b = false;
			for (ParamInfo pi : alParamInfo) {
				if (pi.paramType == ParamType.MethodUrlPath) {
					b = true;
					break;
				}
			}
			if (!b) {
				String s = "urlPath is required, either: Method.urlPath, or param MethodUrlPath";
				alErrors.add(msgPrefix + s);
			}
		} else {
			for (ParamInfo pi : alParamInfo) {
				if (pi.paramType == ParamType.MethodUrlPath) {
					String s = "also has a param of type=methodUrlParam, and paramType=MethodUrlPath, cant have both defined";
					alErrors.add(msgPrefix + s);
				}
			}
		}

		// make sure that matching ? {} param vars
		derivedUrlPath = urlPath;
		if (derivedUrlPath != null && derivedUrlPath.indexOf("{") < 0 && derivedUrlPath.indexOf("}") < 0) {
			// convert each ? to {name}
			for (ParamInfo pi : alParamInfo) {
				if (pi.paramType != ParamType.UrlPathTagValue) {
					continue;
				}
				int pos = derivedUrlPath.indexOf("?");
				if (pos < 0) {
					continue;
				}
				if (pos == 0) {
					derivedUrlPath = "{" + pi.name + "}" + derivedUrlPath.substring(1);
				} else {
					derivedUrlPath = derivedUrlPath.substring(0, pos) + "{" + pi.name + "}" + derivedUrlPath.substring(pos + 1);
				}
			}
		}
		derivedUrlPath = OAString.convert(derivedUrlPath, "{", "<%=$");
		derivedUrlPath = OAString.convert(derivedUrlPath, "}", "%>");

		int x = OAString.count(derivedUrlPath, "<%=$");
		x += OAString.count(derivedUrlPath, "?");
		int cnt = 0;
		for (ParamInfo pi : alParamInfo) {
			if (pi.paramType == ParamType.UrlPathTagValue) {
				cnt++;
				if (derivedUrlPath.indexOf("$" + pi.name) < 0) {
					String s = String
							.format("urlPath %s, template=%s, param path value '%s' not found in template tag(s)",
									urlPath, derivedUrlPath, pi.name);
					alErrors.add(msgPrefix + s);
				}
			}
		}
		if (x != cnt) {
			String s = String
					.format("urlPath %s, has %d tag value(s), does not match %d param(s) with paramType=urlPathValue",
							urlPath, x, cnt);
			alErrors.add(msgPrefix + s);
		}
	}

	protected void verifyDerviedUrlPath(String msgPrefix, List<String> alErrors) {
		// make sure that it can derive urlPath
		if (methodType == MethodType.OAObjectMethodCall || methodType == MethodType.OAInsert || methodType == MethodType.OAUpdate
				|| methodType == MethodType.OADelete) {
			// requires paramType=OAObject
			boolean b = false;
			for (ParamInfo pi : alParamInfo) {
				if (pi.paramType != ParamType.OAObject) {
					continue;
				}
				if (!OAObject.class.isAssignableFrom(pi.origParamClass)) {
					String s = "cant derive urlPath, ParamType.OAObject must be of type OAObject.class";
					alErrors.add(msgPrefix + s);
				} else {
					derivedUrlPath = "/" + OAString.mfcl(pi.origParamClass.getSimpleName());
					OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(pi.origParamClass);
					int cnt = 0;
					for (String s : oi.getKeyProperties()) {
						cnt++;
						derivedUrlPath += "/<%=$ID" + cnt + "%>";
					}
					b = true;
					break;
				}
			}
			if (!b) {
				String s = "urlPath can not be derived, needs to have a paramtType=OAObject for class type=OAObject.class";
				alErrors.add(msgPrefix + s);
			}
		} else if (methodType == MethodType.OAGet) {
			// requires return oaobject
			if (this.origReturnClass == null || !OAObject.class.isAssignableFrom(this.origReturnClass)) {
				String s = "cant derive urlPath, return class must be of type OAObject.class";
				alErrors.add(msgPrefix + s);
			} else {
				derivedUrlPath = "/" + OAString.mfcl(this.origReturnClass.getSimpleName());
				OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(this.origReturnClass);
				int cnt = 0;
				for (String s : oi.getKeyProperties()) {
					cnt++;
					derivedUrlPath += "/<%=$ID" + cnt + "%>";
				}
				// make sure that there are param=OAObjectId
				int cnt2 = 0;
				for (ParamInfo pi : alParamInfo) {
					if (pi.paramType == ParamType.OAObjectId) {
						cnt2++;
					}
				}
				if (cnt != cnt2) {
					String s = String.format("cant derive urlPath, needs to have %d paramType=OAObjectId", cnt);
					alErrors.add(msgPrefix + s);
				}
			}
		} else if (methodType == MethodType.OASearch) {
			// requires return oaobject collection
			boolean b = (returnClassType == ReturnClassType.Array || returnClassType == ReturnClassType.List
					|| returnClassType == ReturnClassType.Hub);
			if (!b || this.returnClass == null || !OAObject.class.isAssignableFrom(this.returnClass)) {
				String s = "cant derive urlPath, return class must be array, List or Hub of type OAObject.class";
				alErrors.add(msgPrefix + s);
			} else {
				OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(this.origReturnClass);
				derivedUrlPath = "/" + OAString.mfcl(oi.getPluralName());
			}
		} else if (methodType == MethodType.OARemote) {
			ParamInfo pi = alParamInfo.get(0);
			//qqqqqqqqqqqqqq ??
			derivedUrlPath = "/" + OAString.mfcl(pi.origParamClass.getSimpleName());
			derivedUrlPath += "/<%=$ID%>";
		}
	}

	public InvokeInfo getInvokeInfo(Object[] args) throws Exception {
		InvokeInfo invokeInfo = new InvokeInfo();

		invokeInfo.urlPath = getUrlPath(args);

		invokeInfo.urlQuery = getUrlQuery(args);

		invokeInfo.searchQuery = getSearchWhere(args); //qqq ?? make sure orderBy is with it

		OAJsonArrayNode searchArgsArrayNode = getSearchWhereTagValues(args);

		invokeInfo.jsonBody = getJsonBody(args);

		invokeInfo.methodReturnClass = getMethodReturnClass(args);

		String url = urlPath;
		boolean b1 = OAString.isNotEmpty(invokeInfo.urlQuery);
		boolean b2 = OAString.isNotEmpty(invokeInfo.searchQuery);
		if (b1 || b2) {
			url += "?";
		}
		if (b1) {
			url += urlQuery;
		}
		if (b2) {
			if (b1) {
				url += "&";
			}
			url += invokeInfo.searchQuery;
		}

		if (OAString.isNotEmpty(invokeInfo.searchQuery)) {
			if (searchArgsArrayNode != null) {
				invokeInfo.jsonBody = searchArgsArrayNode.toJson();
			} else {
				invokeInfo.jsonBody = null;
			}
		}

		HashMap<String, String> hsHeader = null;
		HashMap<String, String> hsCookie = null;

		int pos = -1;
		for (ParamInfo pi : alParamInfo) {
			pos++;
			if (pi.paramType == ParamType.Header) {
				if (hsHeader == null) {
					hsHeader = new HashMap();
				}
				hsHeader.put(pi.name.toUpperCase(), OAConv.toString(args[pos]));
			} else if (pi.paramType == ParamType.Cookie) {
				if (hsCookie == null) {
					hsCookie = new HashMap();
				}
				hsCookie.put(pi.name.toUpperCase(), OAConv.toString(args[pos]));
			}
		}
		return invokeInfo;
	}

	public OATemplate getUrlPathTemplate() {
		if (urlPathTemplate != null) {
			return urlPathTemplate;
		}
		urlPathTemplate = new OATemplate(derivedUrlPath);
		return urlPathTemplate;
	}

	public String getUrlPath(Object[] args) {
		getUrlPathTemplate();

		//qqq urlPathTemplate.clearProperties();
		String result = null;

		if (methodType == MethodType.OAObjectMethodCall || methodType == MethodType.OAInsert || methodType == MethodType.OAUpdate
				|| methodType == MethodType.OADelete) {
			// requires paramType=OAObject
			int pos = -1;
			for (ParamInfo pi : alParamInfo) {
				if (pi.paramType == ParamType.OAObject) {
					OAObject oaobj = (OAObject) args[pos];
					if (oaobj != null) {
						OAObjectKey oakey = oaobj.getObjectKey();
						Object[] ids = oakey.getObjectIds();
						if (ids != null) {
							int i = 0;
							for (Object id : ids) {
								i++;
								urlPathTemplate.setProperty("ID" + i, id);
							}
						}
						break;
					}
				}
			}
		} else if (methodType == MethodType.OAGet) {
			int pos = -1;
			int cnt = 0;
			for (ParamInfo pi : alParamInfo) {
				pos++;
				if (pi.paramType == ParamType.OAObjectId) {
					urlPathTemplate.setProperty("ID" + (++cnt), args[pos]);
				}
			}
		} else if (methodType == MethodType.OASearch) {
			result = "";
		} else if (OAString.isNotEmpty(urlPath)) {
			int pos = -1;
			for (ParamInfo pi : alParamInfo) {
				if (pi.paramType == ParamType.UrlPathTagValue) {
					urlPathTemplate.setProperty(pi.name, OAConv.toString(args[pos]));
				}
			}
		} else if (methodType == MethodType.OARemote) {
			//qqqqqqqqqqqqqqqqqq
		} else {
			int i = -1;
			for (ParamInfo pi : alParamInfo) {
				i++;
				if (pi.paramType == ParamType.MethodUrlPath) {
					result = OAConv.toString(args[i]);
				}
			}
		}
		if (urlPathTemplate != null) {
			result = urlPathTemplate.process();
		}
		return result;
	}

	public String getUrlQuery(Object[] args) throws Exception {
		String urlQuery = "";

		urlQuery = this.urlQuery;

		for (int argPos = 0; argPos < alParamInfo.size(); argPos++) {
			ParamInfo pi = alParamInfo.get(argPos);
			final Object objArg = args[argPos];

			if (pi.paramType == RestParam.ParamType.PageNumber) {
				int val = OAConv.toInt(objArg);
				if (urlQuery.length() > 0) {
					urlQuery += "&";
				}
				urlQuery += "pageNumber=" + val; // qqqq need to know how to use page# (urlQuery name?, heading, etc) ??
			} else if (pi.paramType == RestParam.ParamType.UrlQueryNameValue) {
				if (objArg == null) {
					continue;
				}
				if (pi.classType == ParamInfo.ClassType.Array) {
					int x = Array.getLength(objArg);
					for (int i = 0; i < x; i++) {
						Object obj = Array.get(objArg, i);

						if (urlQuery.length() > 0) {
							urlQuery += "&";
						}

						String val = OAConv.toString(obj, pi.format);
						if (val == null) {
							val = "";
						} else {
							val = URLEncoder.encode(val, "UTF-8");
						}
						urlQuery += pi.name + "=" + val;
					}
				} else if (pi.classType == ParamInfo.ClassType.List) {
					final List list = (List) objArg;
					for (Object arg : list) {

						if (urlQuery.length() > 0) {
							urlQuery += "&";
						}

						String val = OAConv.toString(arg, pi.format);
						if (val == null) {
							val = "";
						} else {
							val = URLEncoder.encode(val, "UTF-8"); //qqqqqqqq ?? recheck on query encoding
							//qqqqqqqqq encode rules: ?? encode URL,  and query needs to only have value encoded
						}
						urlQuery += pi.name + "=" + val;
					}
				}
			}
		}
		return urlQuery; //qqqqqqqqqqq
	}

	public String getSearchWhere(Object[] args) throws Exception {
		String search = searchWhere;
		String orderBy = searchOrderBy;

		for (int argPos = 0; argPos < alParamInfo.size(); argPos++) {
			ParamInfo pi = alParamInfo.get(argPos);
			final Object objArg = args[argPos];

			if (pi.paramType == RestParam.ParamType.MethodSearchWhere) {
				if (search != null) {
					search += " AND ";
				}
				String val = OAConv.toString(objArg);
				search += val;
			} else if (pi.paramType == RestParam.ParamType.MethodSearchOrderBy) {
				String val = OAConv.toString(objArg);
				orderBy = val; // qqqqqqqq ??

			} else if (pi.paramType == RestParam.ParamType.SearchWhereAddNameValue) {
				if (objArg == null) {
					continue;
				}
				if (pi.classType == ParamInfo.ClassType.Array) {
					int x = Array.getLength(objArg);
					for (int i = 0; i < x; i++) {
						Object obj = Array.get(objArg, i);

						if (i == 0) {
							if (search == null) {
								search = "";
							} else {
								search += " AND ";
							}
						}

						if (urlQuery.length() > 0) {
							if (i > 0) {
								search += " OR ";
							}
						}

						if (i == 0) {
							search += "(";
						}
						String val = OAConv.toString(obj, pi.format);
						if (val == null) {
							val = "NULL";
						}
						search += pi.name + "=" + val;
					}
					if (x > 0) {
						search += ")";
					}
				} else if (pi.classType == ParamInfo.ClassType.List) {
					final List list = (List) objArg;
					if (list.size() > 0) {
						if (urlQuery.length() > 0) {
							urlQuery += URLEncoder.encode(" AND ", "UTF-8");
						}
					}
					int i = 0;
					for (Object arg : list) {
						if (i == 0) {
							if (search == null) {
								search = "";
							} else {
								search += " AND ";
							}
						}

						if (i > 0) {
							search += " OR ";
						}
						if (i++ == 0) {
							search += "(";
						}

						String val = OAConv.toString(arg, pi.format);
						if (val == null) {
							val = "NULL";
						} else {
						}
						search += pi.name + "=" + val;
					}
					if (list.size() > 0) {
						search += ")";
					}
				} else {
					if (search == null) {
						search = "";
					} else {
						search += " AND ";
					}

					String val = OAConv.toString(objArg, pi.format);
					if (val == null) {
						val = "NULL";
					}
					search += pi.name + "=" + val;
				}

			} else if (pi.paramType == RestParam.ParamType.SearchWhereTagValue) {
				//qqqqqqqqqqqqq ?? another Method to get searchWhere args

			} else if (pi.paramType == RestParam.ParamType.UrlQueryNameValue) {

				if (objArg == null) {
					continue;
				}
				if (pi.classType == ParamInfo.ClassType.Array) {
					int x = Array.getLength(objArg);
					for (int i = 0; i < x; i++) {
						Object obj = Array.get(objArg, i);

						if (urlQuery.length() > 0) {
							urlQuery += "&";
						}

						String val = OAConv.toString(obj, pi.format);
						if (val == null) {
							val = "";
						} else {
							val = URLEncoder.encode(val, "UTF-8");
						}
						urlQuery += pi.name + "=" + val;
					}
				} else if (pi.classType == ParamInfo.ClassType.List) {
					final List list = (List) objArg;
					for (Object arg : list) {

						if (urlQuery.length() > 0) {
							urlQuery += "&";
						}

						String val = OAConv.toString(arg, pi.format);
						if (val == null) {
							val = "";
						} else {
							val = URLEncoder.encode(val, "UTF-8"); //qqqqqqqq ?? recheck on query encoding
							//qqqqqqqqq encode rules: ?? encode URL,  and query needs to only have value encoded
						}
						urlQuery += pi.name + "=" + val;
					}
				}
			}
		}

		if (orderBy != null) {
			search += " " + orderBy;
		}

		return search;
		/* qqqqqqqqqqqqq
				if (search != null) {
					if (urlQuery.length() > 1) {
						urlQuery += "&";
					}
					urlQuery += "query=";
					urlQuery += URLEncoder.encode(search, "UTF-8");
				}


				return urlQuery; //qqqqqqqqqqq
		*/
	}

	//qqqqqqqqqq verify search params (?) and matching arguments qqqqqqq

	// search tag values need to go in json body as a json array
	public OAJsonArrayNode getSearchWhereTagValues(Object[] args) throws Exception {

		OAJsonArrayNode arrayNode = null;
		for (int argPos = 0; argPos < alParamInfo.size(); argPos++) {
			ParamInfo pi = alParamInfo.get(argPos);

			if (pi.paramType != RestParam.ParamType.SearchWhereTagValue) {
				continue;
			}
			if (arrayNode == null) {
				arrayNode = new OAJsonArrayNode();
			}

			final Object objArg = args[argPos];

			OAJsonNode node = OAJsonUtil.convertObjectToJsonNode(objArg, null);
			arrayNode.add(node);
		}

		return arrayNode;
	}

	/*
	 *  Build http body using Json.
	*/
	public String getJsonBody(Object[] args) throws Exception {

		if (methodType == MethodType.GET) {
			// fall thru
		} else if (methodType == MethodType.OAGet) {
			// fall thru
		} else if (methodType == MethodType.OASearch) {
			OAJsonArrayNode node = getSearchWhereTagValues(args);
			if (node == null) {
				return null;
			}
			return node.toJson();
		} else if (methodType == MethodType.POST) {
			// fall thru
		} else if (methodType == MethodType.PUT) {
			// fall thru
		} else if (methodType == MethodType.PATCH) {
			// fall thru
		} else if (methodType == MethodType.OAObjectMethodCall) {
			List<String>[] lstIncludePropertyPathss = new ArrayList[alParamInfo.size()];
			int ix = -1;
			for (ParamInfo pix : alParamInfo) {
				ix++;
				lstIncludePropertyPathss[ix] = pix.alIncludePropertyPaths;
			}
			OAJsonArrayNode node = OAJsonUtil.convertMethodArgumentsToJson(method, args, lstIncludePropertyPathss, true);
			if (node == null) {
				return null;
			}
			return node.toJson();
		} else if (methodType == MethodType.OARemote) {
			List<String>[] lstIncludePropertyPathss = new ArrayList[alParamInfo.size()];
			int ix = -1;
			for (ParamInfo pix : alParamInfo) {
				ix++;
				lstIncludePropertyPathss[ix] = pix.alIncludePropertyPaths;
			}
			OAJsonArrayNode node = OAJsonUtil.convertMethodArgumentsToJson(method, args, lstIncludePropertyPathss, false);
			if (node == null) {
				return null;
			}
			return node.toJson();
		} else if (methodType == MethodType.OAInsert || methodType == MethodType.OAUpdate || methodType == MethodType.OADelete) {
			if (args == null || args.length == 0) {
				return null;
			}

			ParamInfo pi = alParamInfo.get(0);
			OAJsonNode node = OAJsonUtil.convertObjectToJsonNode(args[0], pi.alIncludePropertyPaths);
			if (node == null) {
				return null;
			}
			return node.toJson();
		}

		// fall thru and find all OAObject, BodyObject, BodyJson
		OAJsonObjectNode jsonNodeBody = new OAJsonObjectNode();

		for (int argPos = 0; argPos < alParamInfo.size(); argPos++) {
			ParamInfo pi = alParamInfo.get(argPos);
			final Object objArg = args[argPos];

			if (pi.paramType == RestParam.ParamType.BodyJson) {
				if (objArg instanceof String) {
					OAJson oaJson = new OAJson();
					OAJsonNode node = oaJson.load((String) objArg);
					jsonNodeBody.set(pi.name, node);
				} else if (objArg instanceof OAJsonNode) {
					jsonNodeBody.set(pi.name, (OAJsonNode) objArg);
				}
			} else if (pi.paramType == RestParam.ParamType.BodyObject) {
				OAJsonNode nodex = OAJsonUtil.convertObjectToJsonNode(objArg, pi.alIncludePropertyPaths);
				//  qqq todo: includeReferenceLevelAmount
				jsonNodeBody.set(pi.name, nodex);
			}
		}

		String jsonBody = null;
		int x = jsonNodeBody.getChildrenPropertyNames().size();
		if (x == 1) {
			String s = jsonNodeBody.getChildrenPropertyNames().get(0);
			jsonBody = jsonNodeBody.getChildNode(s).toJson();
		} else if (x > 1) {
			// simulate an object based on the params that are paramType.BodyObject
			jsonBody = jsonNodeBody.toJson();
		}

		return jsonBody;
	}

	public Class getMethodReturnClass(Object[] args) {
		Class result = returnClass;
		if (returnClass == null) {
			for (int argPos = 0; argPos < alParamInfo.size(); argPos++) {
				ParamInfo pi = alParamInfo.get(argPos);
				if (pi.paramType != ParamType.MethodReturnClass) {
					final Object objArg = args[argPos];
					if (objArg instanceof Class) {
						result = (Class) objArg;
						break;
					}
				}
			}
		}
		return result;
	}
}

package com.oreillyauto.storepurchaseorder.remote.rest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.JAXBContext;

import org.eclipse.persistence.jaxb.JAXBContextFactory;

import com.oreillyauto.storepurchaseorder.remote.rest.MethodInfo.ReturnClassType;
import com.oreillyauto.storepurchaseorder.remote.rest.ParamInfo.ClassType;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestClass;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod.MethodType;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam.ParamType;
import com.viaoa.hub.Hub;
import com.viaoa.json.OAJson;
import com.viaoa.json.OAJsonUtil;
import com.viaoa.json.node.OAJsonArrayNode;
import com.viaoa.json.node.OAJsonNode;
import com.viaoa.json.node.OAJsonRootNode;
import com.viaoa.object.OAObject;
import com.viaoa.template.OATemplate;
import com.viaoa.util.Base64;
import com.viaoa.util.OAConv;
import com.viaoa.util.OADate;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAString;
import com.viaoa.util.OATime;

/* Demos

oarest model objects
	object by id
		servlet/oarest/manualPurchaseOrder/{id}?pp
		with pp
		?? query params
		multipart id
			combined into 1 using "-" or "_"
			separate path params "/WIX/51515
	select using object query
		using filter
	extra params
		PPs to include
		sort
	insert
		object
	update
		object
		partial using json/map of name=value
		partial using query name=value
	delete
		id
	remote object method calls

oarest remote using Java Interface
	methods to define all of the above

	select using params as name=values
		single
		list, array, hub

	call method on oaobject

	call remote method on registered object
		../oaremote?remoteclassname=className&remotemethodname=methodName

*/

//qqqqqqqqqqqqqqqqq

// ** make remote calls to a registered Impl, from a client.proxy/interface object

// Content-Type and Content-Length

// SPEC: https://tools.ietf.org/html/rfc2616

// String response = restTemplate.getForObject(DUMMY_URL, String.class);

// response body
//    https://developer.mozilla.org/en-US/docs/Web/HTTP/Messages

// keep-alive support

// multipart support

// rename to OAHttpRemoteClient  qqqqqqqqq

// note: works with OARestServlet, and has annotations for specific functionality.

// allow param (and annotations) to get extra data:   Map for send headers, Response for headers and return code, etc
// allow adding PP hints for additional data (store in cache ?? for additonal requests)
// allow response to be like getDetail, that has wrapper object to hold object, additional prop data, and additional (sibling) objects
// create
// add CORS support

// create abstract methods:  convert string to class, convert object to/from json

/**
 * Client for Remote Java Methods to be sent and invoked on an HTTP REST API Server.
 * <p>
 * Works with OARestServlet to allow secure access to object model data.
 * <p>
 * This takes an annotated Java interface and creates an implementation that will use HTTP REST API when invoking methods on the webserver.
 * <p>
 * This is also set up to work with OARestServlet to be able to query, update, save, delete Model objects.
 * <p>
 * see also: RestClass, RestMethod, RestParam annotations
 *
 * @author vvia
 */
public class RestClient {

	private String protocol; // http, https
	private String baseUrl; // www.test.com:8080

	//qqqqq have these 2 use oarestservlet, so that the security & "boundries" can be used in both
	private String defaultOARestUrl = "servlet/oarest"; // when MethodType=OA* and urlPath annotation is not defined.

	private String defaultIdSeperator = "/"; // example:   "-", "_", "/"

	private String userId;
	private transient String password;
	private String cookie;

	private JAXBContext jaxbContext;

	private Object expectedResult;

	private final HashMap<Class, ClassInfo> hmClassInfo = new HashMap<>();
	private final HashMap<Method, MethodInfo> hmMethodInfo = new HashMap<>();

	private InvokeInfo lastInvokeInfo;

	private final HashMap<Class, Object> hmRemoteObjectInstance = new HashMap<>();

	public RestClient() {
	}

	public void setExpectedResult(Object result) {
		this.expectedResult = result;
	}

	public InvokeInfo getLastInvokeInfo() {
		return lastInvokeInfo;
	}

	public void setUserPw(String userId, String pw) {
		this.userId = userId;
		this.password = pw;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setProtocol(String p) {
		this.protocol = p;
	}

	public String getProtocol() {
		return this.protocol;
	}

	public void setDefaultOARestUrl(String defaultOARestUrl) {
		this.defaultOARestUrl = defaultOARestUrl;
	}

	public String getDefaultOARestUrl() {
		return defaultOARestUrl;
	}

	public void setDefaultIdSeperator(String defaultIdSeperator) {
		this.defaultIdSeperator = defaultIdSeperator;
	}

	public String getDefaultIdSeperator() {
		return defaultIdSeperator;
	}

	protected JAXBContext getJAXBContext() throws Exception {
		if (jaxbContext != null) {
			return jaxbContext;
		}

		// create using Moxy Factory
		HashMap hm = new HashMap<>();
		jaxbContext = JAXBContextFactory.createContext(new Class[] { Object.class, HashMap.class }, hm);
		return jaxbContext;
	}

	protected ClassInfo getClassInfo(Class clazz) {
		if (clazz == null) {
			return null;
		}
		ClassInfo ci = hmClassInfo.get(clazz);
		return ci;
	}

	public <API> API getInstance(Class<API> clazz) throws Exception {
		if (clazz == null) {
			return null;
		}

		API obj = (API) hmRemoteObjectInstance.get(clazz);
		if (obj != null) {
			return obj;
		}

		if (!clazz.isInterface()) {
			throw new Exception("Class (" + clazz + ") must be a java interface");
		}

		loadMetaData(clazz);

		InvocationHandler handler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				Object result;
				try {
					result = onInvoke(method, args);
				} catch (Exception e) {
					throw new RuntimeException("Invoke exception, method=" + method, e);
				}
				return result;
			}
		};

		obj = (API) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, handler);

		return obj;
	}

	protected Object onInvoke(Method method, Object[] args) throws Throwable {
		final MethodInfo mi = hmMethodInfo.get(method);

		System.out.println("---> method=" + method + ",  methodInfo=" + mi);

		final InvokeInfo invokeInfo = createInvokeInfo(method, args, mi);
		this.lastInvokeInfo = invokeInfo;

		afterCreateInvokeInfo(invokeInfo, method, args, mi);

		String strUrl = getBaseUrl();
		if (OAString.isNotEmpty(invokeInfo.urlPath)) {
			if (invokeInfo.urlPath.charAt(0) == '/') {
				strUrl += invokeInfo.urlPath.substring(1);
			} else {
				strUrl += "/" + invokeInfo.urlPath;
			}
		}
		if (OAString.isNotEmpty(invokeInfo.urlQuery)) {
			strUrl += "?" + invokeInfo.urlQuery;
		}

		String mt;
		switch (mi.methodType) {
		case OAGet:
		case OASearch:
			mt = "GET";
			break;
		case OARemote:
		case OAObjectMethodCall:
			mt = "POST";
			break;
		case OAInsert:
			mt = "POST";
			break;
		case OAUpdate:
			mt = "POST";
			break;
		case OADelete:
			mt = "DELETE";
			break;
		default:
			mt = mi.methodType.toString();
		}

		//qqqqqqqqqqqqqqqqqqqq if methodType is OA*, then need to prepare body with OARestServlet required data

		// get json object for body
		String jsonBody = null;
		if (invokeInfo.jsonNodeBody instanceof OAJsonArrayNode) {
			jsonBody = invokeInfo.jsonNodeBody.toJson();
		} else {
			int x = invokeInfo.jsonNodeBody.getChildrenPropertyNames().size();
			if (x == 1) {
				String s = invokeInfo.jsonNodeBody.getChildrenPropertyNames().get(0);
				jsonBody = invokeInfo.jsonNodeBody.getChildNode(s).toJson();
			} else if (x > 1) {
				// simulate an object based on the params that are paramType.BodyObject
				jsonBody = invokeInfo.jsonNodeBody.toJson();
			}
		}
		// ================== now CALL the endpoint ==================
		String jsonResult = callHttpEndPoint(invokeInfo, strUrl, mt, jsonBody);

		invokeInfo.response = jsonResult;

		Object obj = OAJsonUtil.convertJsonToObject(jsonResult, mi.origReturnClass, mi.returnClass);

		return obj;
	}

	protected InvokeInfo createInvokeInfo(final Method method, final Object[] args, final MethodInfo mi) throws Throwable {
		final InvokeInfo invokeInfo = new InvokeInfo();
		invokeInfo.methodInfo = mi;

		invokeInfo.pathTemplate = mi.urlPathTemplate;
		//qqqqqqqqqqqq
		if (OAString.isNotEmpty(mi.urlPath)) {
			invokeInfo.pathTemplate = new OATemplate(mi.urlPath);
		}

		final boolean bIsSearch = (mi.methodType == MethodType.OASearch);
		final boolean bIsObjectRemote = (mi.methodType == MethodType.OAObjectMethodCall);
		final boolean bIsRemote = (mi.methodType == MethodType.OARemote);

		String search = null;
		for (int argPos = 0; argPos < mi.alParamInfo.size(); argPos++) {
			ParamInfo pi = mi.alParamInfo.get(argPos);
			final Object objArg = args[argPos];

			if (bIsObjectRemote && argPos == 0) {
				// first param is the object, which needs to get the Id used to set the path param.  Remainder args will be in json body
				String id = ((OAObject) objArg).getJaxbSinglePartId();
				invokeInfo.pathTemplate.setProperty(pi.name, id);

				List<String>[] lstIncludePropertyPathss = new ArrayList[mi.alParamInfo.size()];
				int ix = -1;
				for (ParamInfo pix : mi.alParamInfo) {
					ix++;
					lstIncludePropertyPathss[ix] = pix.alIncludePropertyPaths;
				}
				invokeInfo.jsonNodeBody = (OAJsonRootNode) OAJsonUtil.convertMethodArgumentsToJson(	method, args,
																									lstIncludePropertyPathss,
																									true);
			} else if (bIsObjectRemote && argPos <= mi.method.getParameterCount()) {
				// no-op, already added to jsonNodeBody
			} else if (bIsRemote && argPos == 0) {
				List<String>[] lstIncludePropertyPathss = new ArrayList[mi.alParamInfo.size()];
				int ix = -1;
				for (ParamInfo pix : mi.alParamInfo) {
					ix++;
					lstIncludePropertyPathss[ix] = pix.alIncludePropertyPaths;
				}
				invokeInfo.jsonNodeBody = (OAJsonRootNode) OAJsonUtil.convertMethodArgumentsToJson(	method, args,
																									lstIncludePropertyPathss,
																									false);
			} else if (bIsRemote && argPos <= mi.method.getParameterCount()) {
				// no-op, already added to jsonNodeBody
			} else if (pi.paramType == RestParam.ParamType.UrlPathTagValue) {
				Object objx = OAConv.toString(args[argPos], pi.format);
				invokeInfo.pathTemplate.setProperty(pi.name, objx);
			} else if (pi.paramType == RestParam.ParamType.SearchWhereAddNameValue) {
				if (objArg != null) {
					if (pi.classType == ParamInfo.ClassType.Array) {
						int x = Array.getLength(objArg);
						for (int i = 0; i < x; i++) {
							Object obj = Array.get(objArg, i);

							if (bIsSearch && i == 0) {
								if (search == null) {
									search = "";
								} else {
									search += " AND ";
								}
							}

							if (invokeInfo.urlQuery.length() > 0) {
								if (bIsSearch) {
									if (i > 0) {
										search += " OR ";
									}
								} else {
									invokeInfo.urlQuery += "&";
								}
							}

							if (bIsSearch && i == 0) {
								search += "(";
							}
							String val = OAConv.toString(obj, pi.format);
							if (val == null) {
								if (bIsSearch) {
									val = "NULL";
								} else {
									val = "";
								}
							} else {
								if (!bIsSearch) {
									val = URLEncoder.encode(val, "UTF-8");
								}
							}
							if (bIsSearch) {
								search += pi.name + "=" + val;
							} else {
								invokeInfo.urlQuery += pi.name + "=" + val;
							}
						}
						if (bIsSearch && x > 0) {
							search += ")";
						}
					} else if (pi.classType == ParamInfo.ClassType.List) {
						final List list = (List) objArg;
						if (list.size() > 0) {
							if (invokeInfo.urlQuery.length() > 0) {
								invokeInfo.urlQuery += URLEncoder.encode(" AND ", "UTF-8");
							}
						}
						int i = 0;
						for (Object arg : list) {
							if (bIsSearch && i == 0) {
								if (search == null) {
									search = "";
								} else {
									search += " AND ";
								}
							}

							if (invokeInfo.urlQuery.length() == 0) {
							} else {
								if (bIsSearch) {
									if (i > 0) {
										search += " OR ";
									}
								} else {
									invokeInfo.urlQuery += "&";
								}
							}
							if (i++ == 0 && bIsSearch) {
								search += "(";
							}

							String val = OAConv.toString(arg, pi.format);
							if (val == null) {
								if (bIsSearch) {
									val = "NULL";
								} else {
									val = "";
								}
							} else {
								if (!bIsSearch) {
									val = URLEncoder.encode(val, "UTF-8");
								}
							}
							if (bIsSearch) {
								search += pi.name + "=" + val;
							} else {
								invokeInfo.urlQuery += pi.name + "=" + val;
							}
						}
						if (bIsSearch && list.size() > 0) {
							search += ")";
						}
					} else {
						if (bIsSearch) {
							if (search == null) {
								search = "";
							} else {
								search += " AND ";
							}
						}
						if (invokeInfo.urlQuery.length() > 0) {
							invokeInfo.urlQuery += "&";
						}

						String val = OAConv.toString(objArg, pi.format);
						if (val == null) {
							if (bIsSearch) {
								val = "NULL";
							} else {
								val = "";
							}
						} else {
							if (!bIsSearch) {
								val = URLEncoder.encode(val, "UTF-8");
							}
						}
						if (bIsSearch) {
							search += pi.name + "=" + val;
						} else {
							invokeInfo.urlQuery += pi.name + "=" + val;
						}
					}
				}
			} else if (pi.paramType == RestParam.ParamType.BodyJson) {
				if (objArg instanceof String) {
					OAJson oaJson = new OAJson();
					OAJsonNode node = oaJson.load((String) objArg);
					invokeInfo.jsonNodeBody.set(pi.name, node);
				} else if (objArg instanceof OAJsonNode) {
					invokeInfo.jsonNodeBody.set(pi.name, (OAJsonNode) objArg);
				}
			} else if (pi.paramType == RestParam.ParamType.BodyObject) {
				OAJsonNode nodex = OAJsonUtil.convertObjectToJsonNode(objArg, pi.alIncludePropertyPaths);
				invokeInfo.jsonNodeBody.set(pi.name, nodex);
			} else if (pi.paramType == RestParam.ParamType.Header) {
				String val = OAConv.toString(objArg, pi.format);
				invokeInfo.hsHeader.put(pi.name, val);
			} else if (pi.paramType == RestParam.ParamType.Cookie) {
				String val = OAConv.toString(objArg, pi.format);
				invokeInfo.hsCookie.put(pi.name, val);
			} else if (pi.paramType == RestParam.ParamType.PageNumber) {
				int val = OAConv.toInt(objArg);
				invokeInfo.pageNumber = val;
			} else if (pi.paramType == RestParam.ParamType.ResponseIncludePropertyPaths) {
				if (objArg == null) {
					invokeInfo.responseIncludePropertyPaths = null;
				} else if (objArg.getClass().isArray()) {
					int x = Array.getLength(objArg);
					ArrayList<String> al = new ArrayList();
					for (int i = 0; i < x; i++) {
						Object obj = Array.get(objArg, i);
						al.add(OAConv.toString(obj));
					}
					invokeInfo.responseIncludePropertyPaths = new String[al.size()];
					String[] ss = new String[al.size()];
					al.toArray(ss);
					invokeInfo.responseIncludePropertyPaths = ss;
				} else if (objArg instanceof List) {
					String[] ss = new String[((List) objArg).size()];
					((List) objArg).toArray(ss);
					invokeInfo.responseIncludePropertyPaths = ss;
				} else {
					invokeInfo.responseIncludePropertyPaths = new String[] { OAConv.toString(objArg) };
				}

			} else if (pi.paramType == RestParam.ParamType.MethodSearchOrderBy) {
				invokeInfo.queryOrderBy = OAConv.toString(objArg);
			} else if (pi.paramType == RestParam.ParamType.MethodSearchWhere) {
				invokeInfo.queryWhereClause = OAConv.toString(objArg);
			} else if (pi.paramType == RestParam.ParamType.SearchWhereTagValue) {

				if (invokeInfo.alQueryWhereParams == null) {
					invokeInfo.alQueryWhereParams = new ArrayList();
				}

				if (objArg == null) {
				} else if (objArg.getClass().isArray()) {
					int x = Array.getLength(objArg);
					for (int i = 0; i < x; i++) {
						Object obj = Array.get(objArg, i);
						invokeInfo.alQueryWhereParams.add(OAConv.toString(obj, pi.format));
					}
				} else if (objArg instanceof List) {
					List list = (List) objArg;
					int x = list.size();
					for (int i = 0; i < x; i++) {
						invokeInfo.alQueryWhereParams.add(OAConv.toString(list.get(i), pi.format));
					}
				} else {
					invokeInfo.alQueryWhereParams.add(OAConv.toString(objArg, pi.format));
				}
			} else if (pi.paramType == RestParam.ParamType.UrlQueryNameValue) {
				if (invokeInfo.alUrlQueryParams == null) {
					invokeInfo.alUrlQueryParams = new ArrayList();
				}
				if (objArg == null) {
				} else if (objArg.getClass().isArray()) {
					int x = Array.getLength(objArg);
					for (int i = 0; i < x; i++) {
						Object obj = Array.get(objArg, i);
						invokeInfo.alUrlQueryParams.add(pi.name + "=" + URLEncoder.encode(OAConv.toString(obj, pi.format), "UTF-8"));
					}
				} else if (objArg instanceof List) {
					List list = (List) objArg;
					int x = list.size();
					for (int i = 0; i < x; i++) {
						invokeInfo.alUrlQueryParams
								.add(pi.name + "=" + URLEncoder.encode(OAConv.toString(list.get(i), pi.format), "UTF-8"));
					}
				} else {
					invokeInfo.alUrlQueryParams
							.add(pi.name + "=" + URLEncoder.encode(OAConv.toString(OAConv.toString(objArg, pi.format)), "UTF-8"));
				}

			} else if (pi.paramType == RestParam.ParamType.MethodReturnClass) {
				invokeInfo.methodReturnClass = (Class) objArg;
			} else if (pi.paramType == RestParam.ParamType.MethodUrlPath) {
				invokeInfo.urlPath = (String) objArg;
			}
		}

		if (search != null) {
			if (invokeInfo.urlQuery.length() > 1) {
				invokeInfo.urlQuery += "&";
			}
			invokeInfo.urlQuery += "query=";
			invokeInfo.urlQuery += URLEncoder.encode(search, "UTF-8");
		}

		if (invokeInfo.methodReturnClass == null) {
			invokeInfo.methodReturnClass = mi.returnClass;
		}

		return invokeInfo;
	}

	protected void afterCreateInvokeInfo(final InvokeInfo invokeInfo, final Method method, final Object[] args, final MethodInfo mi)
			throws Throwable {

		if (invokeInfo.pathTemplate != null) {
			invokeInfo.urlPath = invokeInfo.pathTemplate.process();
		}

		if (OAString.isNotEmpty(invokeInfo.queryWhereClause)) {
			if (invokeInfo.urlQuery.length() > 0) {
				invokeInfo.urlQuery += "&";
			}
			invokeInfo.urlQuery += "query=" + URLEncoder.encode(invokeInfo.queryWhereClause, "UTF-8");

			if (invokeInfo.alQueryWhereParams != null) {
				for (String s : invokeInfo.alQueryWhereParams) {
					invokeInfo.urlQuery += "&queryParam=";
					invokeInfo.urlQuery += URLEncoder.encode(s, "UTF-8");
				}
			}
		}
		if (OAString.isNotEmpty(invokeInfo.queryOrderBy)) {
			if (invokeInfo.urlQuery.length() > 0) {
				invokeInfo.urlQuery += "&";
			}
			invokeInfo.urlQuery += "orderBy=" + URLEncoder.encode(invokeInfo.queryOrderBy, "UTF-8");
		}

		if (invokeInfo.alUrlQueryParams != null) {
			for (String s : invokeInfo.alUrlQueryParams) {
				if (invokeInfo.urlQuery.length() > 0) {
					invokeInfo.urlQuery += "&";
				}
				invokeInfo.urlQuery += s;
			}
		}

		if (invokeInfo.responseIncludePropertyPaths != null) {
			for (String s : invokeInfo.responseIncludePropertyPaths) {
				if (invokeInfo.urlQuery.length() > 0) {
					invokeInfo.urlQuery += "&";
				}
				invokeInfo.urlQuery += "pp=" + s;
			}
		}

		if (mi.alIncludePropertyPaths != null) {
			for (String s : mi.alIncludePropertyPaths) {
				if (invokeInfo.urlQuery.length() > 0) {
					invokeInfo.urlQuery += "&";
				}
				invokeInfo.urlQuery += "pp=" + s;
			}
		}

		if (OAString.isNotEmpty(mi.urlQuery)) {
			if (invokeInfo.urlQuery.length() > 0) {
				invokeInfo.urlQuery += "&";
			}
			invokeInfo.urlQuery += mi.urlQuery;
		}

		if (invokeInfo.pageNumber > 0) {
			invokeInfo.hsHeader.put("pageNumber", Integer.toString(invokeInfo.pageNumber));
		}
	}

	/*
	protected Class<API> getProxyClass() {
		if (classProxy != null) {
			return classProxy;
		}
		Class c = this.getClass();
		for (; c != null; c = c.getSuperclass()) {
			Type type = c.getGenericSuperclass();
			if (type instanceof ParameterizedType) {
				classProxy = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
				break;
			}
		}
		if (classProxy == null || !classProxy.isInterface()) {
			throw new RuntimeException("Type <API> must be a Java Iterface, class found=" + classProxy);
		}
		try {
			loadClassMetaData();
		} catch (Exception e) {
			throw new RuntimeException("Error while loading MethodInfo from " + classProxy, e);
		}
	
		for (MethodInfo mi : hmMethodInfo.values()) {
			for (ParamInfo pi : mi.alParamInfo) {
				int xx = 4;
				xx++;
			}
		}
	
		return classProxy;
	}
	*/

	public ArrayList<String> verify(Class interfaceClass) {
		ClassInfo ci = hmClassInfo.get(interfaceClass);
		if (ci == null) {
			return null;
		}

		ArrayList<String> alErrors = ci.verify();

		/*
		if (alErrors.size() > 0) {
			String msg = "";
			for (int i = 0; i < 5; i++) {
				if (msg.length() > 0) {
					msg += "  ";
				}
				msg += alErrors.get(i);
			}
			throw new RuntimeException(msg);
		}
		*/
		return alErrors;
	}

	protected void loadMetaData(Class interfaceClass) throws Exception {
		if (interfaceClass == null) {
			return;
		}

		final ClassInfo classInfo = new ClassInfo(interfaceClass);
		hmClassInfo.put(interfaceClass, classInfo);

		RestClass rc = (RestClass) interfaceClass.getAnnotation(RestClass.class);
		if (rc != null) {
			if (getBaseUrl() == null) {
				setBaseUrl(rc.urlPath());
			}
			if (protocol == null) {
				protocol = rc.protocol();
			}
		}

		Method[] methods = interfaceClass.getMethods();
		for (Method method : methods) {
			MethodInfo mi = new MethodInfo(method);
			classInfo.alMethodInfo.add(mi);

			hmMethodInfo.put(method, mi);

			mi.name = method.getName();
			mi.origReturnClass = mi.returnClass = method.getReturnType();

			if (mi.origReturnClass.isArray()) {
				mi.returnClassType = MethodInfo.ReturnClassType.Array;
				mi.returnClass = mi.origReturnClass.getComponentType();
			} else if (List.class.isAssignableFrom(mi.origReturnClass)) {
				mi.returnClassType = MethodInfo.ReturnClassType.List;
				Type type = method.getGenericReturnType();
				if (type instanceof ParameterizedType) {
					Type typex = ((ParameterizedType) type).getActualTypeArguments()[0];
					if (typex instanceof Class) {
						mi.returnClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
					}
				} else {
					mi.returnClass = null; // needs to be defined by method returnClas or param.paramType=MethodReturnClass
				}
			} else if (Hub.class.isAssignableFrom(mi.origReturnClass)) {
				mi.returnClassType = MethodInfo.ReturnClassType.Hub;
				Type type = method.getGenericReturnType();
				if (type instanceof ParameterizedType) {
					Type typex = ((ParameterizedType) type).getActualTypeArguments()[0];
					if (typex instanceof Class) {
						mi.returnClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
					}
				} else {
					mi.returnClass = null; // needs to be defined by method paramType=MethodReturnClass
				}
			} else if (OAJsonNode.class.isAssignableFrom(mi.origReturnClass)) {
				mi.returnClassType = MethodInfo.ReturnClassType.JsonNode;
			} else if (mi.origReturnClass.equals(String.class)) {
				mi.returnClassType = MethodInfo.ReturnClassType.String;
			} else if (mi.origReturnClass.equals(void.class) || mi.origReturnClass.equals(Void.class)) {
				mi.returnClassType = MethodInfo.ReturnClassType.Void;
			}

			Parameter[] parameters = method.getParameters();
			for (int i = 0; parameters != null && i < parameters.length; i++) {
				ParamInfo pi = new ParamInfo();
				mi.alParamInfo.add(pi);
				pi.paramType = RestParam.ParamType.Unassigned;

				pi.name = parameters[i].getName();
				pi.origParamClass = pi.paramClass = parameters[i].getType();

				if (pi.origParamClass.isArray()) {
					pi.classType = ParamInfo.ClassType.Array;
					pi.paramClass = pi.origParamClass.getComponentType();
				} else if (List.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.Array.List;
					Type type = method.getGenericReturnType();
					if (type instanceof ParameterizedType) {
						pi.paramClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
					} else {
						pi.paramClass = null;
					}
				} else if (OAJsonNode.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.JsonNode;
				} else if (pi.origParamClass.equals(String.class)) {
					pi.classType = ParamInfo.ClassType.String;
				} else if (OADate.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.Date;
				} else if (OADateTime.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.DateTime;
				} else if (OATime.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.Time;
				} else if (LocalDate.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.Date;
				} else if (LocalDateTime.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.DateTime;
				} else if (Date.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.Date;
				} else {
					pi.classType = ParamInfo.ClassType.Unassigned;
				}

				RestParam rp = (RestParam) parameters[i].getAnnotation(RestParam.class);
				if (rp != null) {
					if (rp.name().length() > 0) {
						pi.name = rp.name();
						pi.bNameAssigned = true;
					}

					if (rp.format().length() > 0) {
						pi.format = rp.format();
					}
					if (rp.paramType() != null && rp.paramType() != RestParam.ParamType.Unassigned) {
						pi.paramType = rp.paramType();
					}
					if (!rp.paramClass().equals(Void.class)) {
						pi.rpParamClass = pi.paramClass = rp.paramClass();
					}

					if (pi.paramType == RestParam.ParamType.MethodReturnClass) {
						mi.returnClass = null; // assigned at runtime using this param's class value
					}

					pi.includeReferenceLevelAmount = rp.includeReferenceLevelAmount();

					pi.alIncludePropertyPaths = new ArrayList();

					String sx = rp.includePropertyPath();
					if (sx != null && sx.length() > 0) {
						pi.alIncludePropertyPaths.add(rp.includePropertyPath());
					}
					String[] ss = rp.includePropertyPaths();
					if (ss != null && ss.length > 0) {
						for (String s : rp.includePropertyPaths()) {
							if (s.length() > 0) {
								pi.alIncludePropertyPaths.add(s);
							}
						}
					}
				}

				if (OAString.isEmpty(pi.format)) {
					if (pi.classType == ParamInfo.ClassType.Date) {
						pi.format = OADate.JsonFormat;
					}
					if (pi.classType == ParamInfo.ClassType.DateTime) {
						pi.format = OADateTime.JsonFormat;
					}
					if (pi.classType == ParamInfo.ClassType.Time) {
						pi.format = OATime.JsonFormat;
					}
				}
			}

			boolean bUsesBody = false;
			for (ParamInfo pi : mi.alParamInfo) {
				if (pi.paramType == RestParam.ParamType.BodyObject) {
					bUsesBody = true;
					break;
				}
			}

			// methodType & urlPath
			mi.methodType = RestMethod.MethodType.Unassigned;

			boolean bFoundUrlPath = false;

			RestMethod rm = (RestMethod) mi.method.getAnnotation(RestMethod.class);
			if (rm != null) {
				if (rm.methodName() != null && rm.methodName().length() > 0) {
					mi.objectMethodName = rm.methodName();
				}

				if (rm.name() != null && rm.name().length() > 0) {
					mi.name = rm.name();
				}

				if (rm.urlQuery() != null && rm.urlQuery().length() > 0) {
					mi.urlQuery = rm.urlQuery();
				}

				if (rm.methodType() != RestMethod.MethodType.Unassigned) {
					mi.methodType = rm.methodType();
				}
				if (rm.urlPath().length() > 0) {
					mi.urlPath = rm.urlPath();
					bFoundUrlPath = true;
				}

				if (!rm.returnClass().equals(Void.class)) {
					mi.rmReturnClass = rm.returnClass();
				}

				mi.includeReferenceLevelAmount = rm.includeReferenceLevelAmount();

				mi.alIncludePropertyPaths = new ArrayList();

				if (rm.includePropertyPath() != null && rm.includePropertyPath().length() > 0) {
					mi.alIncludePropertyPaths.add(rm.includePropertyPath());
				}
				if (rm.includePropertyPaths() != null && rm.includePropertyPaths().length > 0) {
					for (String s : rm.includePropertyPaths()) {
						if (s.length() > 0) {
							mi.alIncludePropertyPaths.add(s);
						}
					}
				}

				if (rm.urlPath() != null && rm.urlPath().length() > 0) {
					mi.urlPath = rm.urlPath();
				}

				if (rm.searchWhere() != null && rm.searchWhere().length() > 0) {
					mi.searchWhere = rm.searchWhere();
				}
				if (rm.searchOrderBy() != null && rm.searchOrderBy().length() > 0) {
					mi.searchOrderBy = rm.searchOrderBy();
				}
			}
		}
	}

	protected void loadMetaData_OLD_(Class interfaceClass) throws Exception {
		if (interfaceClass == null) {
			return;
		}

		RestClass rc = (RestClass) interfaceClass.getAnnotation(RestClass.class);
		if (rc != null) {
			if (getBaseUrl() == null) {
				setBaseUrl(rc.urlPath());
			}
			if (protocol == null) {
				protocol = rc.protocol();
			}
		}

		Method[] methods = interfaceClass.getMethods();
		for (Method method : methods) {
			MethodInfo mi = new MethodInfo(method);
			hmMethodInfo.put(method, mi);

			mi.name = method.getName();
			mi.origReturnClass = mi.returnClass = method.getReturnType();

			if (mi.origReturnClass.isArray()) {
				mi.returnClassType = MethodInfo.ReturnClassType.Array;
				mi.returnClass = mi.origReturnClass.getComponentType();
			} else if (List.class.isAssignableFrom(mi.origReturnClass)) {
				mi.returnClassType = MethodInfo.ReturnClassType.List;
				Type type = method.getGenericReturnType();
				if (type instanceof ParameterizedType) {
					Type typex = ((ParameterizedType) type).getActualTypeArguments()[0];
					if (typex instanceof Class) {
						mi.returnClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
					}
				} else {
					mi.returnClass = null; // needs to be defined by method returnClas or param.paramType=MethodReturnClass
				}
			} else if (Hub.class.isAssignableFrom(mi.origReturnClass)) {
				mi.returnClassType = MethodInfo.ReturnClassType.Hub;
				Type type = method.getGenericReturnType();
				if (type instanceof ParameterizedType) {
					Type typex = ((ParameterizedType) type).getActualTypeArguments()[0];
					if (typex instanceof Class) {
						mi.returnClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
					}
				} else {
					mi.returnClass = null; // needs to be defined by method paramType=MethodReturnClass
				}
			} else if (OAJsonNode.class.isAssignableFrom(mi.origReturnClass)) {
				mi.returnClassType = MethodInfo.ReturnClassType.JsonNode;
			} else if (mi.origReturnClass.equals(String.class)) {
				mi.returnClassType = MethodInfo.ReturnClassType.String;
			} else if (mi.origReturnClass.equals(void.class) || mi.origReturnClass.equals(Void.class)) {
				mi.returnClassType = MethodInfo.ReturnClassType.Void;
			}

			Parameter[] parameters = method.getParameters();
			for (int i = 0; parameters != null && i < parameters.length; i++) {
				ParamInfo pi = new ParamInfo();
				mi.alParamInfo.add(pi);
				pi.paramType = RestParam.ParamType.Unassigned;

				pi.name = parameters[i].getName();
				pi.origParamClass = pi.paramClass = parameters[i].getType();

				if (pi.origParamClass.isArray()) {
					pi.classType = ParamInfo.ClassType.Array;
					pi.paramClass = pi.origParamClass.getComponentType();
				} else if (List.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.Array.List;
					Type type = method.getGenericReturnType();
					if (type instanceof ParameterizedType) {
						pi.paramClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
					} else {
						pi.paramClass = null;
					}
				} else if (OAJsonNode.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.JsonNode;
				} else if (pi.origParamClass.equals(String.class)) {
					pi.classType = ParamInfo.ClassType.String;
				} else if (OADate.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.Date;
				} else if (OADateTime.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.DateTime;
				} else if (OATime.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.Time;
				} else if (LocalDate.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.Date;
				} else if (LocalDateTime.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.DateTime;
				} else if (Date.class.isAssignableFrom(pi.origParamClass)) {
					pi.classType = ParamInfo.ClassType.Date;
				} else {
					pi.classType = ParamInfo.ClassType.Unassigned;
				}

				RestParam rp = (RestParam) parameters[i].getAnnotation(RestParam.class);
				if (rp != null) {
					if (rp.name().length() > 0) {
						pi.name = rp.name();
						pi.bNameAssigned = true;
					}

					if (rp.format().length() > 0) {
						pi.format = rp.format();
					}
					if (rp.paramType() != null && rp.paramType() != RestParam.ParamType.Unassigned) {
						pi.paramType = rp.paramType();
					}
					if (!rp.paramClass().equals(Void.class)) {
						pi.paramClass = rp.paramClass();
					}

					if (pi.paramType == RestParam.ParamType.MethodReturnClass) {
						mi.returnClass = null; // assigned at runtime using this param's class value
					}

					pi.includeReferenceLevelAmount = rp.includeReferenceLevelAmount();

					pi.alIncludePropertyPaths = new ArrayList();

					String sx = rp.includePropertyPath();
					if (sx != null && sx.length() > 0) {
						pi.alIncludePropertyPaths.add(rp.includePropertyPath());
					}
					String[] ss = rp.includePropertyPaths();
					if (ss != null && ss.length > 0) {
						for (String s : rp.includePropertyPaths()) {
							if (s.length() > 0) {
								pi.alIncludePropertyPaths.add(s);
							}
						}
					}
				}

				if (OAString.isEmpty(pi.format)) {
					if (pi.classType == ParamInfo.ClassType.Date) {
						pi.format = OADate.JsonFormat;
					}
					if (pi.classType == ParamInfo.ClassType.DateTime) {
						pi.format = OADateTime.JsonFormat;
					}
					if (pi.classType == ParamInfo.ClassType.Time) {
						pi.format = OATime.JsonFormat;
					}
				}
			}

			boolean bUsesBody = false;
			for (ParamInfo pi : mi.alParamInfo) {
				if (pi.paramType == RestParam.ParamType.BodyObject) {
					bUsesBody = true;
					break;
				}
			}

			// methodType & urlPath
			mi.methodType = RestMethod.MethodType.Unassigned;

			/*qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq
			
						mi.urlPath = method.getName(); // default
			
						String lowerName = method.getName().toLowerCase();
			
						if (lowerName.startsWith("select")) {
							mi.urlPath = method.getName().substring(6);
							mi.urlPath = OAString.mfcl(mi.urlPath);
							if (bUsesBody) {
								mi.methodType = RestMethod.MethodType.POST;
							} else {
								mi.methodType = RestMethod.MethodType.GET;
							}
						} else if (lowerName.startsWith("get")) {
							mi.urlPath = method.getName().substring(3);
							mi.urlPath = OAString.mfcl(mi.urlPath);
							if (bUsesBody) {
								mi.methodType = RestMethod.MethodType.POST;
							} else {
								mi.methodType = RestMethod.MethodType.GET;
							}
						} else if (lowerName.startsWith("post")) {
							mi.methodType = RestMethod.MethodType.POST;
							mi.urlPath = method.getName().substring(4);
							mi.urlPath = OAString.mfcl(mi.urlPath);
						} else if (lowerName.startsWith("post") || lowerName.startsWith("update")) {
							mi.methodType = RestMethod.MethodType.POST;
							mi.urlPath = method.getName().substring(6);
							mi.urlPath = OAString.mfcl(mi.urlPath);
						} else if (lowerName.startsWith("put")) {
							mi.methodType = RestMethod.MethodType.PUT;
							mi.urlPath = method.getName().substring(3);
							mi.urlPath = OAString.mfcl(mi.urlPath);
						} else if (lowerName.startsWith("insert")) {
							mi.methodType = RestMethod.MethodType.PUT;
							mi.urlPath = method.getName().substring(6);
							mi.urlPath = OAString.mfcl(mi.urlPath);
						}
			
						if (mi.methodType == null || mi.methodType == RestMethod.MethodType.Unassigned) {
							mi.methodType = RestMethod.MethodType.GET;
						}
			
			qqqqqqq */

			boolean bFoundUrlPath = false;

			RestMethod rm = (RestMethod) mi.method.getAnnotation(RestMethod.class);
			if (rm != null) {
				if (rm.methodName() != null && rm.methodName().length() > 0) {
					mi.objectMethodName = rm.methodName();
				}

				if (rm.name() != null && rm.name().length() > 0) {
					mi.name = rm.name();
				}

				if (rm.urlQuery() != null && rm.urlQuery().length() > 0) {
					mi.urlQuery = rm.urlQuery();
				}

				if (rm.methodType() != RestMethod.MethodType.Unassigned) {
					mi.methodType = rm.methodType();
				}
				if (rm.urlPath().length() > 0) {
					//qqq mi.origUrlPath = mi.urlPath = rm.urlPath();
					bFoundUrlPath = true;
				}

				if (!rm.returnClass().equals(Void.class)) {
					mi.rmReturnClass = rm.returnClass();
				}

				mi.includeReferenceLevelAmount = rm.includeReferenceLevelAmount();

				mi.alIncludePropertyPaths = new ArrayList();

				if (rm.includePropertyPath() != null && rm.includePropertyPath().length() > 0) {
					mi.alIncludePropertyPaths.add(rm.includePropertyPath());
				}
				if (rm.includePropertyPaths() != null && rm.includePropertyPaths().length > 0) {
					for (String s : rm.includePropertyPaths()) {
						if (s.length() > 0) {
							mi.alIncludePropertyPaths.add(s);
						}
					}
				}

				if (rm.urlPath() != null && rm.urlPath().length() > 0) {
					mi.urlPath = rm.urlPath();
				}

				if (rm.searchWhere() != null && rm.searchWhere().length() > 0) {
					mi.searchWhere = rm.searchWhere();
				}
				if (rm.searchOrderBy() != null && rm.searchOrderBy().length() > 0) {
					mi.searchOrderBy = rm.searchOrderBy();
				}
			}

			//qqqqqqqqqqqqqqqqqqqqqqqqqqqqqq

			/*
			boolean bHasPathVariable = false;
			boolean bHasQueryNameValue = false;
			int cntUnassigned = 0;
			for (ParamInfo pi : mi.alParamInfo) {
				if (pi.paramType == null || pi.paramType == ParamType.Unassigned) {
					cntUnassigned++;
				} else {
					if (pi.paramType == ParamType.UrlPathValue) {
						bHasPathVariable = true;
						break;
					}
					if (pi.paramType == ParamType.SearchWhereNameValue) {
						bHasQueryNameValue = true;
						break;
					}
					continue;
				}
			}
			
			if (!bHasPathVariable && !bHasQueryNameValue) {
				int cnt = 0;
				for (ParamInfo pi : mi.alParamInfo) {
					if (pi.paramType != null && pi.paramType != ParamType.Unassigned) {
						continue;
					}
			
					if (mi.methodType == MethodType.OAGet && OAString.isEmpty(mi.urlPath)) {
						pi.paramType = ParamType.UrlPathValue;
						if (!pi.bNameAssigned) {
							pi.name = "id";
							if (cnt++ > 0) {
								pi.name += cnt;
							}
						}
					} else if (mi.methodType == MethodType.OASearch) {
						pi.paramType = ParamType.SearchWhereNameValue;
					} else if (pi.bNameAssigned) {
						if (mi.urlPath != null && cntUnassigned == 1) {
							pi.paramType = ParamType.UrlPathValue;
						} else {
							pi.paramType = ParamType.UrlQueryValue;
						}
					}
				}
			}
			*/

			// Verify =====================================

			ArrayList<String> alErrors = new ArrayList<>();
			mi.verify(alErrors);

			if (alErrors.size() > 0) {
				String msg = "";
				for (int i = 0; i < 5; i++) {
					if (msg.length() > 0) {
						msg += "  ";
					}
					msg += alErrors.get(i);
				}
				throw new RuntimeException(msg);
			}

			//qqqqqqqqqqqqqqqqqqqqqqq =================== qqqqqqqqqqq

			if (mi.methodType == MethodType.OASearch) {
				if (OAString.isEmpty(mi.urlPath)) {
					String s = String
							.format("method name=%s methodType=%s, requires urlPath.  ex: /customers",
									mi.name, mi.methodType);
					throw new RuntimeException(s);
				}

				if (mi.returnClassType != ReturnClassType.Array && mi.returnClassType != ReturnClassType.List
						&& mi.returnClassType != ReturnClassType.Hub) {
					boolean b = false;
					for (ParamInfo pi : mi.alParamInfo) {
						if (pi.paramType == ParamType.MethodReturnClass) {
							b = true;
						}
					}
					if (!b) {
						String s = String
								.format("method name=%s methodType=%s, return value must be an Array or List or Hub",
										mi.name, mi.methodType);
						throw new RuntimeException(s);
					}
				}

				if (!OAObject.class.isAssignableFrom(mi.returnClass)) {
					String s = String
							.format("method name=%s methodType=%s, returnClass=%s, must be an array/list/hub of asssignable from OAObject",
									mi.name, mi.methodType, mi.returnClass);
					throw new RuntimeException(s);
				}

				for (ParamInfo pi : mi.alParamInfo) {

					if (pi.paramType == ParamType.OAObject
							|| pi.paramType == ParamType.OAObjectMethodCallArg) {
						String s = String
								.format("method name=%s methodType=%s, paramType=%s not needed/allowed with OAGet",
										mi.name, mi.methodType, pi.paramType);
						throw new RuntimeException(s);
					}
				}
			}

			if (mi.methodType == MethodType.OAObjectMethodCall) {
				if (OAString.isEmpty(mi.objectMethodName)) {
					String s = String
							.format("method name=%s methodType=%s, requires MethodName",
									mi.name, mi.methodType);
					throw new RuntimeException(s);
				}

				if (OAString.isEmpty(mi.urlPath)) {
					String s = String
							.format("method name=%s methodType=%s, requires urlPath.  ex: /customer",
									mi.name, mi.methodType);
					throw new RuntimeException(s);
				}
				/*qq
				if (mi.origUrlPath.indexOf('?') >= 0 || mi.origUrlPath.indexOf('{') >= 0) {
					String s = String
							.format("method name=%s methodType=%s, urlPath should not have any tags (?, {}).",
									mi.name, mi.methodType);
					throw new RuntimeException(s);
				}
				*/
				if (mi.alParamInfo.size() == 0 || mi.alParamInfo.get(0).paramType != ParamType.OAObject
						|| !OAObject.class.isAssignableFrom(mi.alParamInfo.get(0).origParamClass)) {
					String s = String
							.format("method name=%s methodType=%s, first param must be an OAObject",
									mi.name, mi.methodType);
					throw new RuntimeException(s);
				}

				for (ParamInfo pi : mi.alParamInfo) {
					if (pi.paramType == ParamType.Unassigned) {
						String s = String
								.format("method name=%s methodType=%s, params cant be ParamType.Unassigned, use OAObjectMethodCallArg, or Ignore",
										mi.name, mi.methodType);
						throw new RuntimeException(s);
					}
				}

				boolean b = false;
				for (ParamInfo pi : mi.alParamInfo) {
					if (pi.paramType == ParamType.SearchWhereTagValue
							|| pi.paramType == ParamType.SearchWhereAddNameValue
							|| pi.paramType == ParamType.MethodUrlPath
							|| pi.paramType == ParamType.MethodSearchWhere
							|| pi.paramType == ParamType.MethodSearchOrderBy
							|| pi.paramType == ParamType.SearchWhereTagValue
							|| pi.paramType == ParamType.SearchWhereAddNameValue
							// || pi.paramType == ParamType.OAObjectMethodCallObject
							// || pi.paramType == ParamType.OAObjectMethodCallArg
							|| pi.paramType == ParamType.BodyObject
							|| pi.paramType == ParamType.BodyJson
							|| pi.paramType == ParamType.PageNumber) {
						String s = String
								.format("method name=%s methodType=%s, paramType=%s not needed/allowed with OAObjectMethodCall",
										mi.name, mi.methodType, pi.paramType);
						throw new RuntimeException(s);
					}
				}
			}

			if (mi.methodType == MethodType.OARemote) {
				String s = String
						.format("method name=%s methodType=%s, is for internal (remote instances) use only.",
								mi.name, mi.methodType);
				throw new RuntimeException(s);
			}

			if (mi.methodType == MethodType.OAInsert || mi.methodType == MethodType.OAUpdate || mi.methodType == MethodType.OADelete) {
				if (OAString.isEmpty(mi.urlPath)) {
					String s = String
							.format("method name=%s methodType=%s, requires urlPath.  ex: /customer",
									mi.name, mi.methodType);
					throw new RuntimeException(s);
				}
				/*qqq
				if (mi.origUrlPath.indexOf('?') >= 0 || mi.origUrlPath.indexOf('{') >= 0) {
					String s = String
							.format("method name=%s methodType=%s, urlPath should not have any tags (?, {}).",
									mi.name, mi.methodType);
					throw new RuntimeException(s);
				}
				*/
				if (mi.alParamInfo.size() == 0 || mi.alParamInfo.get(0).paramType != ParamType.OAObject
						|| !OAObject.class.isAssignableFrom(mi.alParamInfo.get(0).origParamClass)) {
					String s = String
							.format("method name=%s methodType=%s, first param must be an OAObject",
									mi.name, mi.methodType);
					throw new RuntimeException(s);
				}

				boolean b = false;
				for (ParamInfo pi : mi.alParamInfo) {
					if (pi.paramType == ParamType.SearchWhereTagValue
							|| pi.paramType == ParamType.SearchWhereAddNameValue
							|| pi.paramType == ParamType.MethodUrlPath
							|| pi.paramType == ParamType.MethodSearchWhere
							|| pi.paramType == ParamType.MethodSearchOrderBy
							|| pi.paramType == ParamType.SearchWhereTagValue
							|| pi.paramType == ParamType.SearchWhereAddNameValue
							// || pi.paramType == ParamType.OAObject
							|| pi.paramType == ParamType.OAObjectMethodCallArg
							|| pi.paramType == ParamType.BodyObject
							|| pi.paramType == ParamType.BodyJson
							|| pi.paramType == ParamType.PageNumber) {
						String s = String
								.format("method name=%s methodType=%s, paramType=%s not needed/allowed with OAObjectMethodCall",
										mi.name, mi.methodType, pi.paramType);
						throw new RuntimeException(s);
					}
				}
			}

			// final checks
			for (ParamInfo pi : mi.alParamInfo) {
				if (pi.paramType == ParamType.Unassigned) {
					String s = !pi.bNameAssigned ? " (name of param not defined)" : "";
					s = "method param=" + pi.name + s + " is not defined (ParamType) and is required, method=" + mi.method;
					throw new RuntimeException(s);
				}

				if (pi.paramType == ParamType.MethodUrlPath) {
					if (pi.classType != ClassType.String) {
						String s = "method param " + pi.name + " type=UrlPath must be a String, method=" + mi.method;
						throw new RuntimeException(s);
					}
				}

				if (pi.paramType == ParamType.SearchWhereAddNameValue) {
					if (!pi.bNameAssigned) {
						String s = "method param " + pi.name + " type=QueryWhereNameValue must have a name assigned, method=" + mi.method;
						throw new RuntimeException(s);
					}
				}
			}

			if (mi.returnClass == null) {
				boolean b = false;
				for (ParamInfo pi : mi.alParamInfo) {
					if (pi.paramType == RestParam.ParamType.MethodReturnClass) {
						b = true;
						break;
					}
				}
				if (!b) {
					String s = "method return type is not known, method=" + mi.method;
					throw new RuntimeException(s);
				}
			}

			/* qqqqqqqqqqq removed qqqqqqqqq
			
			// make changes based on methodType
			boolean bUseSingularParamName = false;
			boolean bUsePluralReturnName = false;
			boolean bUseSingularReturnName = false;
			if (mi.methodType == MethodType.OAObjectMethodCall) {
				// ex:  "servlet/oarest/supplier/12?objectMethodName=displayOnserver"
			
				// first param must be OAObject, and methodName needs to be a valid oamethod
				if (mi.alParamInfo.size() == 0 || !OAObject.class.isAssignableFrom(mi.alParamInfo.get(0).paramClass)) {
					if (mi.objectMethodName == null) {
						String s = "method type is OAObjectRemote and requires first param to be an OAObject, method=" + mi.method;
						throw new RuntimeException(s);
					}
				}
			
				// all params are json
				if (mi.objectMethodName == null) {
					String s = "method type is OAObjectRemote and requires a objectMethodName, method=" + mi.method;
					throw new RuntimeException(s);
				}
				mi.urlQuery = OAString.append(mi.urlQuery, "objectMethodName=" + mi.objectMethodName, "&");
				mi.urlPath = "<%=$id%>";
				bUseSingularParamName = true;
			} else if (mi.methodType == MethodType.OAGet) {
				bUseSingularReturnName = true;
			} else if (mi.methodType == MethodType.OASearch) {
			
				if (mi.returnClassType != ReturnClassType.Array && mi.returnClassType != ReturnClassType.List
						&& mi.returnClassType != ReturnClassType.Hub) {
					String s = "method type is OAQuery, and must return an Array/List/Hub, MethodName, method=" + mi.method;
					throw new RuntimeException(s);
				}
				bUsePluralReturnName = true;
			} else if (mi.methodType == MethodType.OAInsert) {
				bUseSingularParamName = true;
			} else if (mi.methodType == MethodType.OAUpdate) {
				bUseSingularParamName = true;
			} else if (mi.methodType == MethodType.OADelete) {
				bUseSingularParamName = true;
			} else if (mi.methodType == MethodType.OARemote) {
				mi.urlQuery = OAString.append(mi.urlQuery, "remoteClassName=" + interfaceClass.getSimpleName(), "&");
				mi.urlQuery = OAString.append(mi.urlQuery, "remoteMethodName=" + mi.objectMethodName, "&");
				mi.urlPath = getDefaultOARestUrl() + "/oaremote"; // oaRestServlet will check "classname" for this
			}
			
			if (bUseSingularParamName) {
				mi.urlPath = getDefaultOARestUrl() + "/" + OAString.mfcl(mi.alParamInfo.get(0).paramClass.getSimpleName());
			} else if (bUseSingularReturnName) {
				mi.urlPath = getDefaultOARestUrl() + "/" + OAString.mfcl(mi.returnClass.getSimpleName());
			} else if (bUsePluralReturnName) {
				OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(mi.returnClass);
				String s = oi.getPluralName();
				mi.urlPath = getDefaultOARestUrl() + "/" + OAString.mfcl(s);
			}
			*/
		}

	}

	public String convertToString(Object obj) {
		// todo: use conversion library
		return "" + obj;
	}

	// needed for PATCH support
	private static java.lang.reflect.Field fieldHttpURLConnectMethod;
	private static java.lang.reflect.Field fieldHttpsURLConnectMethod1;
	private static java.lang.reflect.Field fieldHttpsURLConnectMethod2;

	protected String callHttpEndPoint(InvokeInfo invokeInfo, String httpUrl, final String httpMethodName, final String requestBodyJson)
			throws Exception {

		//qqqqqqqqqqqq add invokeInfo for all of this qqqqq get responseCode, etc

		if (httpUrl.indexOf("://") < 0) {
			if (OAString.isEmpty(protocol)) {
				httpUrl = "http://" + httpUrl;
			} else {
				httpUrl = protocol + "://" + httpUrl;
			}
		}

		URL url = new URL(httpUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setUseCaches(false);
		conn.setRequestProperty("User-Agent", "RestClient");

		if ("PATCH".equalsIgnoreCase(httpMethodName)) {
			// Hack for PATCH
			// https://stackoverflow.com/questions/25163131/httpurlconnection-invalid-http-method-patch

			try {
				if (fieldHttpURLConnectMethod == null) {
					fieldHttpURLConnectMethod = HttpURLConnection.class.getDeclaredField("method");
					fieldHttpURLConnectMethod.setAccessible(true);
				}
				fieldHttpURLConnectMethod.set(conn, httpMethodName);
			} catch (Throwable t) {
				conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
				conn.setRequestMethod("POST");
			}
			if (conn instanceof HttpsURLConnection) {
				try {
					if (fieldHttpsURLConnectMethod1 == null) {
						fieldHttpsURLConnectMethod1 = HttpsURLConnection.class.getDeclaredField("delegate");
						fieldHttpsURLConnectMethod1.setAccessible(true);
					}
					Object conx = fieldHttpsURLConnectMethod1.get(conn);
					if (conx instanceof HttpURLConnection) {
						fieldHttpURLConnectMethod.setAccessible(true);
						fieldHttpURLConnectMethod.set(conx, httpMethodName);
					}

					if (fieldHttpsURLConnectMethod2 == null) {
						fieldHttpsURLConnectMethod2 = conx.getClass().getDeclaredField("httpsURLConnection");
						fieldHttpsURLConnectMethod2.setAccessible(true);
					}
					HttpsURLConnection con2 = (HttpsURLConnection) fieldHttpsURLConnectMethod2.get(conx);

					fieldHttpURLConnectMethod.set(con2, httpMethodName);
				} catch (Throwable t) {
					//ignore
				}
			}
		} else {
			conn.setRequestMethod(httpMethodName.toUpperCase());
		}

		conn.setDoOutput(true);

		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);
		if (OAString.isNotEmpty(requestBodyJson)) {
			conn.setRequestProperty("Content-Type", "application/json");
		}
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("Accept", "application/json"); // "application/json, text/*;q=0.7");

		if (OAString.isNotEmpty(cookie)) {
			conn.addRequestProperty("cookie", cookie);
		}

		if (OAString.isNotEmpty(userId)) {
			String s = userId + ":" + password;
			conn.setRequestProperty("Authorization", "Basic " + Base64.encode(s));
		}

		if (OAString.isNotEmpty(requestBodyJson)) {
			OutputStream out = conn.getOutputStream();
			Writer writer = new OutputStreamWriter(out, "UTF-8");

			writer.write(requestBodyJson);
			writer.close();
			out.close();
		}

		for (Map.Entry<String, List<String>> me : conn.getHeaderFields().entrySet()) {
			String s = me.getKey() + "=";
			boolean b = false;
			for (String s2 : me.getValue()) {
				if (!b) {
					b = true;
				} else {
					s += ", ";
				}
				s += s2;
			}
			// System.out.println(s);
		}

		String setcookie = conn.getHeaderField("Set-Cookie");
		if (OAString.isNotEmpty(setcookie)) {
			this.cookie = OAString.field(setcookie, ";", 1);
		}

		// https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
		invokeInfo.responseCode = conn.getResponseCode();

		StringBuilder sb = new StringBuilder();
		InputStream inputStream = null;
		try {
			inputStream = conn.getInputStream();
		} catch (Exception e) {
			throw new RuntimeException(
					"Exception getting inputStream from connection, responseCode=" + invokeInfo.responseCode + ", responseMessage="
							+ conn.getResponseMessage(),
					e);
		}

		// HTTP Response
		// https://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html

		if (inputStream != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			/*
			for (;;) {
				int ch = br.read();
				if (ch < 0) {
					break;
				}
				sb.append((char) ch);
			}
			*/

			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();
		}
		conn.disconnect();

		String result = sb.toString();
		if (invokeInfo.responseCode < 200 || invokeInfo.responseCode > 299) {
			String s = String.format(	"Error Response code: %d, msg=%s, result=%s",
										invokeInfo.responseCode,
										conn.getResponseMessage(),
										result);

			throw new RuntimeException(s);
		}
		return result;
	}

	/**
	 * Manually call an end point.
	 *
	 * @param url
	 * @param query
	 * @param httpMethod
	 * @param jsonBody
	 */
	public String callJsonEndpoint(String url, String query, String httpMethod, String jsonBody) throws Exception {
		String strUrl = url;
		if (OAString.isNotEmpty(query)) {
			strUrl += "?" + query;
		}
		InvokeInfo invokeInfo = new InvokeInfo();
		String result = callHttpEndPoint(invokeInfo, strUrl, httpMethod, jsonBody);
		return result;
	}
}

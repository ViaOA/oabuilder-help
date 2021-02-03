package com.oreillyauto.storepurchaseorder.remote.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
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

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.MarshallerProperties;

import com.oreillyauto.storepurchaseorder.remote.rest.MethodInfo.ReturnClassType;
import com.oreillyauto.storepurchaseorder.remote.rest.ParamInfo.ClassType;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestClass;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestMethod.MethodType;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam;
import com.oreillyauto.storepurchaseorder.remote.rest.annotation.RestParam.ParamType;
import com.viaoa.jaxb.OAJaxb;
import com.viaoa.json.OAJson;
import com.viaoa.json.node.OAJsonArrayNode;
import com.viaoa.json.node.OAJsonBooleanNode;
import com.viaoa.json.node.OAJsonNode;
import com.viaoa.json.node.OAJsonNullNode;
import com.viaoa.json.node.OAJsonNumberNode;
import com.viaoa.json.node.OAJsonObjectNode;
import com.viaoa.json.node.OAJsonRootNode;
import com.viaoa.json.node.OAJsonStringNode;
import com.viaoa.object.OAObject;
import com.viaoa.template.OATemplate;
import com.viaoa.util.Base64;
import com.viaoa.util.OAConv;
import com.viaoa.util.OADate;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAString;
import com.viaoa.util.OATime;

//qqqqqqqqqqqqqqqqq

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
public abstract class RestClient<API> {

	private String protocol; // http, https
	private String baseUrl; // www.test.com:8080
	private Class<API> classProxy;
	private API apiInstance;

	private String userId;
	private transient String password;
	private String cookie;

	private JAXBContext jaxbContext;

	private Object expectedResult;

	private final HashMap<Method, MethodInfo> hmMethodInfo;

	private InvokeInfo lastInvokeInfo;

	public RestClient() {
		hmMethodInfo = new HashMap();
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

	public JAXBContext getJAXBContext() throws Exception {
		if (jaxbContext != null) {
			return jaxbContext;
		}

		// MOXY
		HashMap hm = new HashMap<>();

		// create using Moxy Factory
		jaxbContext = JAXBContextFactory.createContext(new Class[] { Object.class, HashMap.class }, hm);
		return jaxbContext;
	}

	public API getInstance() {
		if (apiInstance != null) {
			return apiInstance;
		}

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

		Class<API> clazz = getProxyClass();
		apiInstance = (API) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, handler);
		return apiInstance;
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

		String mt = mi.methodType.toString();
		if (mt.startsWith("Get")) {
			mt = "GET";
		} else if (mt.startsWith("Post")) {
			mt = "POST";
		}

		// get json object for body
		String jsonBody = null;
		int x = invokeInfo.jsonNodeBody.getChildrenPropertyNames().size();
		if (x == 1) {
			String s = invokeInfo.jsonNodeBody.getChildrenPropertyNames().get(0);
			jsonBody = invokeInfo.jsonNodeBody.getChildNode(s).toJson();
		} else if (x > 1) {
			// simulate an object based on the params that are paramType.BodyObject
			jsonBody = invokeInfo.jsonNodeBody.toJson();
		}

		// VVVVVVVqqqqqqq real deal
		String jsonResult = callHttpEndPoint(strUrl, mt, jsonBody);
		invokeInfo.response = jsonResult;
		Object obj;

		/* qqqqqqqqq TEST
		Object obj = expectedResult; // qqqqqqqqqq
		
		OAJaxb jaxbx = new OAJaxb(invokeInfo.methodReturnClass);
		String jsonResult = jaxbx.convertToJson(obj);
		*/

		if (mi.returnClassType == MethodInfo.ReturnClassType.JsonNode) {
			OAJson json = new OAJson();
			obj = json.load((String) jsonResult);
		} else if (OAObject.class.isAssignableFrom(mi.returnClass)) {
			if (mi.returnClassType == MethodInfo.ReturnClassType.Array) {
				OAJaxb jaxb = new OAJaxb(mi.returnClass);
				obj = jaxb.convertArrayFromJSON(jsonResult);
			} else if (mi.returnClassType == MethodInfo.ReturnClassType.List) {
				OAJaxb jaxb = new OAJaxb(mi.returnClass);
				obj = jaxb.convertListFromJSON(jsonResult);
			} else {
				OAJaxb jaxb = new OAJaxb(mi.returnClass);
				obj = jaxb.convertFromJSON(jsonResult);
			}
		} else if (mi.returnClassType == ReturnClassType.Void) {
			obj = null;
		} else if (mi.returnClassType == ReturnClassType.String) {
			obj = jsonResult;
		} else {
			OAJson json = new OAJson();
			obj = json.load((String) jsonResult);

			if (mi.returnClassType == MethodInfo.ReturnClassType.Array || mi.returnClassType == MethodInfo.ReturnClassType.List) {
				if (obj instanceof OAJsonArrayNode) {
					ArrayList al = new ArrayList();
					for (OAJsonNode node : ((OAJsonArrayNode) obj).getArray()) {
						obj = json.load(node.toJson());

						if (obj == null || obj instanceof OAJsonNullNode) {
							continue;
						} else if (obj instanceof OAJsonStringNode) {
							obj = ((OAJsonStringNode) obj).getValue();
							obj = OAConv.convert(mi.returnClass, obj);
						} else if (obj instanceof OAJsonBooleanNode) {
							obj = ((OAJsonBooleanNode) obj).getValue();
							obj = OAConv.convert(mi.returnClass, obj);
						} else if (obj instanceof OAJsonNumberNode) {
							obj = ((OAJsonNumberNode) obj).getValue();
							obj = OAConv.convert(mi.returnClass, obj);
						} else {
							// jaxb
							OAJaxb jaxb = new OAJaxb(mi.returnClass);
							obj = jaxb.convertFromJSON(jsonResult);
						}
						al.add(obj);
					}
					if (mi.returnClassType == MethodInfo.ReturnClassType.Array) {
						obj = Array.newInstance(mi.returnClass, al.size());
						int i = 0;
						for (Object objx : al) {
							Array.set(obj, i++, objx);
						}
					} else {
						obj = al;
					}
				} else {
					obj = null;
				}
			} else {
				if (obj == null || obj instanceof OAJsonNullNode || mi.returnClassType == ReturnClassType.Void) {
					obj = null;
				} else if (obj instanceof OAJsonStringNode) {
					obj = ((OAJsonStringNode) obj).getValue();
					obj = OAConv.convert(mi.returnClass, obj);
				} else if (obj instanceof OAJsonBooleanNode) {
					obj = ((OAJsonBooleanNode) obj).getValue();
					obj = OAConv.convert(mi.returnClass, obj);
				} else if (obj instanceof OAJsonNumberNode) {
					obj = ((OAJsonNumberNode) obj).getValue();
					obj = OAConv.convert(mi.returnClass, obj);
				} else {
					OAJaxb jaxb = new OAJaxb(mi.returnClass);
					obj = jaxb.convertFromJSON(jsonResult);
				}
			}
		}
		return obj;
	}

	public static class InvokeInfo {
		MethodInfo methodInfo;
		OATemplate pathTemplate;
		String urlPath;
		String urlQuery = "";

		final OAJsonRootNode jsonNodeBody = new OAJsonObjectNode();
		final HashMap<String, String> hsHeader = new HashMap<>();
		final HashMap<String, String> hsCookie = new HashMap<>();
		int pageNumber;

		Class methodReturnClass;

		String queryWhereClause;
		ArrayList<String> alQueryWhereParams;
		String queryOrderBy;
		String[] responseIncludePropertyPaths;
		ArrayList<String> alUrlQueryParams; // name=value (utf-8)
		String response;
	}

	protected InvokeInfo createInvokeInfo(final Method method, final Object[] args, final MethodInfo mi) throws Throwable {
		final InvokeInfo invokeInfo = new InvokeInfo();
		invokeInfo.methodInfo = mi;

		invokeInfo.urlPath = mi.urlPath;

		if (OAString.isNotEmpty(mi.pathTemplate)) {
			invokeInfo.pathTemplate = new OATemplate(mi.pathTemplate);
		}

		final boolean bIsObjectQuery = (mi.methodType == MethodType.GetObjectsUsingQuery);
		String objectQuery = null;
		for (int argPos = 0; argPos < mi.alParamInfo.size(); argPos++) {
			ParamInfo pi = mi.alParamInfo.get(argPos);
			final Object objArg = args[argPos];

			if (pi.paramType == RestParam.ParamType.PathVariable) {
				Object objx = OAConv.toString(args[argPos], pi.format);
				invokeInfo.pathTemplate.setProperty(pi.name, objx);
			} else if (pi.paramType == RestParam.ParamType.QueryWhereNameValue) {
				if (objArg != null) {
					if (pi.classType == ParamInfo.ClassType.Array) {
						int x = Array.getLength(objArg);
						for (int i = 0; i < x; i++) {
							Object obj = Array.get(objArg, i);

							if (bIsObjectQuery && i == 0) {
								if (objectQuery == null) {
									objectQuery = "";
								} else {
									objectQuery += " AND ";
								}
							}

							if (invokeInfo.urlQuery.length() > 0) {
								if (bIsObjectQuery) {
									if (i > 0) {
										objectQuery += " OR ";
									}
								} else {
									invokeInfo.urlQuery += "&";
								}
							}

							if (bIsObjectQuery && i == 0) {
								objectQuery += "(";
							}
							String val = OAConv.toString(obj, pi.format);
							if (val == null) {
								if (bIsObjectQuery) {
									val = "NULL";
								} else {
									val = "";
								}
							} else {
								if (!bIsObjectQuery) {
									val = URLEncoder.encode(val, "UTF-8");
								}
							}
							if (bIsObjectQuery) {
								objectQuery += pi.name + "=" + val;
							} else {
								invokeInfo.urlQuery += pi.name + "=" + val;
							}
						}
						if (bIsObjectQuery && x > 0) {
							objectQuery += ")";
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
							if (bIsObjectQuery && i == 0) {
								if (objectQuery == null) {
									objectQuery = "";
								} else {
									objectQuery += " AND ";
								}
							}

							if (invokeInfo.urlQuery.length() == 0) {
							} else {
								if (bIsObjectQuery) {
									if (i > 0) {
										objectQuery += " OR ";
									}
								} else {
									invokeInfo.urlQuery += "&";
								}
							}
							if (i++ == 0 && bIsObjectQuery) {
								objectQuery += "(";
							}

							String val = OAConv.toString(arg, pi.format);
							if (val == null) {
								if (bIsObjectQuery) {
									val = "NULL";
								} else {
									val = "";
								}
							} else {
								if (!bIsObjectQuery) {
									val = URLEncoder.encode(val, "UTF-8");
								}
							}
							if (bIsObjectQuery) {
								objectQuery += pi.name + "=" + val;
							} else {
								invokeInfo.urlQuery += pi.name + "=" + val;
							}
						}
						if (bIsObjectQuery && list.size() > 0) {
							objectQuery += ")";
						}
					} else {
						if (invokeInfo.urlQuery.length() == 0) {
						} else {
							if (bIsObjectQuery) {
								objectQuery += " AND ";
							} else {
								invokeInfo.urlQuery += "&";
							}
						}
						String val = OAConv.toString(objArg, pi.format);
						if (val == null) {
							if (bIsObjectQuery) {
								val = "NULL";
							} else {
								val = "";
							}
						} else {
							if (!bIsObjectQuery) {
								val = URLEncoder.encode(val, "UTF-8");
							}
						}
						if (bIsObjectQuery) {
							objectQuery += pi.name + "=" + val;
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
				final Object obj = objArg;
				if (obj == null) {
					invokeInfo.jsonNodeBody.set(pi.name, new OAJsonNullNode());
					continue;
				}

				if (obj instanceof OAJsonNode) {
					invokeInfo.jsonNodeBody.set(pi.name, (OAJsonNode) obj);
					continue;
				}

				Class c = pi.paramClass != null ? pi.paramClass : pi.origParamClass;
				if (OAObject.class.isAssignableFrom(c)) {
					OAJaxb jaxb = new OAJaxb(obj.getClass());
					jaxb.setUseReferences(false);
					jaxb.setIncludeGuids(false);
					if (pi.lstIncludePropertyPaths != null) {
						for (String s : pi.lstIncludePropertyPaths) {
							jaxb.addPropertyPath(s);
						}
					}

					if (pi.classType == ParamInfo.ClassType.Array) {
						OAJsonArrayNode arrayNode = new OAJsonArrayNode();
						invokeInfo.jsonNodeBody.set(pi.name, arrayNode);

						int x = Array.getLength(obj);
						for (int i = 0; i < x; i++) {
							Object objx = Array.get(obj, i);

							String s = jaxb.convertToJSON((OAObject) objx);

							OAJson oaJson = new OAJson();
							OAJsonNode node = oaJson.load(s);

							arrayNode.add(node);
						}
					} else if (pi.classType == ParamInfo.ClassType.List) {
						OAJsonArrayNode arrayNode = new OAJsonArrayNode();
						invokeInfo.jsonNodeBody.set(pi.name, arrayNode);

						for (Object objx : (List) obj) {
							String s = jaxb.convertToJSON((OAObject) objx);

							OAJson oaJson = new OAJson();
							OAJsonNode node = oaJson.load(s);
							arrayNode.add(node);
						}
					} else {
						String s = jaxb.convertToJSON((OAObject) obj);
						OAJson oaJson = new OAJson();
						OAJsonNode node = oaJson.load(s);
						invokeInfo.jsonNodeBody.set(pi.name, node);
					}
				} else if (pi.classType == ClassType.Array) {
					OAJsonArrayNode arrayNode = new OAJsonArrayNode();
					invokeInfo.jsonNodeBody.set(pi.name, arrayNode);

					int x = Array.getLength(obj);
					for (int i = 0; i < x; i++) {
						Object objx = Array.get(obj, i);
						if (objx == null) {
							arrayNode.add(new OAJsonNullNode());
						} else if (objx instanceof String) {
							arrayNode.add(new OAJsonStringNode((String) objx));
						} else if (objx instanceof Boolean) {
							arrayNode.add(new OAJsonBooleanNode((Boolean) objx));
						} else if (objx instanceof Number) {
							arrayNode.add(new OAJsonNumberNode((Number) objx));
						} else if (objx instanceof OADateTime) {
							arrayNode.add(new OAJsonStringNode(((OADateTime) objx).toString("yyyy-MM-dd'T'HH:mm:ss")));
						} else if (objx instanceof OADate) {
							arrayNode.add(new OAJsonStringNode(((OADate) objx).toString("yyyy-MM-dd")));
						} else if (objx instanceof OATime) {
							arrayNode.add(new OAJsonStringNode(((OATime) objx).toString("HH:mm:ss")));
						} else {
							StringWriter writer = new StringWriter();

							Marshaller marshaller = getJAXBContext().createMarshaller();
							marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

							marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
							marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
							marshaller.setProperty(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);

							marshaller.marshal(objx, writer);

							OAJson oaJson = new OAJson();
							OAJsonNode node = oaJson.load(writer.toString());
							arrayNode.add(node);
						}
					}
				} else if (pi.classType == ClassType.List) {
					OAJsonArrayNode arrayNode = new OAJsonArrayNode();
					invokeInfo.jsonNodeBody.set(pi.name, arrayNode);
					for (Object objx : (List) obj) {
						if (objx == null) {
							arrayNode.add(new OAJsonNullNode());
						} else if (objx instanceof String) {
							arrayNode.add(new OAJsonStringNode((String) objx));
						} else if (objx instanceof Boolean) {
							arrayNode.add(new OAJsonBooleanNode((Boolean) objx));
						} else if (objx instanceof Number) {
							arrayNode.add(new OAJsonNumberNode((Number) objx));
						} else if (objx instanceof OADateTime) {
							arrayNode.add(new OAJsonStringNode(((OADateTime) objx).toString("yyyy-MM-dd'T'HH:mm:ss")));
						} else if (objx instanceof OADate) {
							arrayNode.add(new OAJsonStringNode(((OADate) objx).toString("yyyy-MM-dd")));
						} else if (objx instanceof OATime) {
							arrayNode.add(new OAJsonStringNode(((OATime) objx).toString("HH:mm:ss")));
						} else {
							StringWriter writer = new StringWriter();

							Marshaller marshaller = getJAXBContext().createMarshaller();
							marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

							marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
							marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
							marshaller.setProperty(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);

							marshaller.marshal(objx, writer);

							OAJson oaJson = new OAJson();
							OAJsonNode node = oaJson.load(writer.toString());
							arrayNode.add(node);
						}
					}
				} else {
					if (obj == null) {
						invokeInfo.jsonNodeBody.set(pi.name, new OAJsonNullNode());
					} else if (obj instanceof String) {
						invokeInfo.jsonNodeBody.set(pi.name, new OAJsonStringNode((String) obj));
					} else if (obj instanceof OAJsonNode) {
						invokeInfo.jsonNodeBody.set(pi.name, (OAJsonNode) obj);
					} else if (obj instanceof Boolean) {
						invokeInfo.jsonNodeBody.set(pi.name, new OAJsonBooleanNode((Boolean) obj));
					} else if (obj instanceof Number) {
						invokeInfo.jsonNodeBody.set(pi.name, new OAJsonNumberNode((Number) obj));
					} else if (obj instanceof OADateTime) {
						invokeInfo.jsonNodeBody.set(pi.name, new OAJsonStringNode(((OADateTime) obj).toString("yyyy-MM-dd'T'HH:mm:ss")));
					} else if (obj instanceof OADate) {
						invokeInfo.jsonNodeBody.set(pi.name, new OAJsonStringNode(((OADate) obj).toString("yyyy-MM-dd")));
					} else if (obj instanceof OATime) {
						invokeInfo.jsonNodeBody.set(pi.name, new OAJsonStringNode(((OATime) obj).toString("HH:mm:ss")));
					} else {
						StringWriter writer = new StringWriter();

						Marshaller marshaller = getJAXBContext().createMarshaller();
						marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

						marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
						marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
						marshaller.setProperty(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);

						marshaller.marshal(obj, writer);

						OAJson oaJson = new OAJson();
						OAJsonNode node = oaJson.load(writer.toString());
						invokeInfo.jsonNodeBody.set(pi.name, node);
					}
				}
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

			} else if (pi.paramType == RestParam.ParamType.QueryOrderBy) {
				invokeInfo.queryOrderBy = OAConv.toString(objArg);
			} else if (pi.paramType == RestParam.ParamType.QueryWhereClause) {
				invokeInfo.queryWhereClause = OAConv.toString(objArg);
			} else if (pi.paramType == RestParam.ParamType.QueryWhereParam) {

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
			} else if (pi.paramType == RestParam.ParamType.UrlQueryParam) {
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
			} else if (pi.paramType == RestParam.ParamType.UrlPath) {
				invokeInfo.urlPath = (String) objArg;
			}
		}

		if (objectQuery != null) {
			if (invokeInfo.urlQuery.length() > 1) {
				invokeInfo.urlQuery += "&";
			}
			invokeInfo.urlQuery += "query=";
			invokeInfo.urlQuery += URLEncoder.encode(objectQuery, "UTF-8");
		}

		if (invokeInfo.methodReturnClass == null) {
			invokeInfo.methodReturnClass = mi.returnClass;
		}

		return invokeInfo;
	}

	protected void afterCreateInvokeInfo(final InvokeInfo invokeInfo, final Method method, final Object[] args, final MethodInfo mi)
			throws Throwable {

		if (invokeInfo.pathTemplate != null) {
			String s = invokeInfo.pathTemplate.process();
			if (OAString.isNotEmpty(s)) {
				if (s.length() > 1 && s.charAt(0) == '/') {
					s = s.substring(1);
				}
				invokeInfo.urlPath = OAString.concat(invokeInfo.urlPath, s, "/");
			}
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

		if (mi.lstIncludePropertyPaths != null) {
			for (String s : mi.lstIncludePropertyPaths) {
				if (invokeInfo.urlQuery.length() > 0) {
					invokeInfo.urlQuery += "&";
				}
				invokeInfo.urlQuery += "pp=" + s;
			}
		}

		if (OAString.isNotEmpty(mi.extraUrlQueryParams)) {
			if (invokeInfo.urlQuery.length() > 0) {
				invokeInfo.urlQuery += "&";
			}
			invokeInfo.urlQuery += mi.extraUrlQueryParams;
		}

		if (invokeInfo.pageNumber > 0) {
			invokeInfo.hsHeader.put("pageNumber", Integer.toString(invokeInfo.pageNumber));
		}
	}

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

	protected void loadClassMetaData() throws Exception {
		Class interfaceClass = getProxyClass();

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

					pi.lstIncludePropertyPaths = new ArrayList();

					String sx = rp.includePropertyPath();
					if (sx != null && sx.length() > 0) {
						pi.lstIncludePropertyPaths.add(rp.includePropertyPath());
					}
					String[] ss = rp.includePropertyPaths();
					if (ss != null && ss.length > 0) {
						for (String s : rp.includePropertyPaths()) {
							if (s.length() > 0) {
								pi.lstIncludePropertyPaths.add(s);
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
			mi.urlPath = method.getName(); // default

			String lowerName = method.getName().toLowerCase();

			if (lowerName.startsWith("select")) {
				lowerName = "get" + method.getName().substring(6);
			}
			if (lowerName.startsWith("get")) {
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

			RestMethod rm = (RestMethod) mi.method.getAnnotation(RestMethod.class);
			if (rm != null) {

				if (rm.name() != null && rm.name().length() > 0) {
					mi.name = rm.name();
				}

				if (rm.extraUrlQueryParams() != null && rm.extraUrlQueryParams().length() > 0) {
					mi.extraUrlQueryParams = rm.extraUrlQueryParams();
				}

				if (rm.methodType() != RestMethod.MethodType.Unassigned) {
					mi.methodType = rm.methodType();
				}
				if (rm.urlPath().length() > 0) {
					mi.urlPath = rm.urlPath();
				}

				mi.includeReferenceLevelAmount = rm.includeReferenceLevelAmount();

				mi.lstIncludePropertyPaths = new ArrayList();

				if (rm.includePropertyPath() != null && rm.includePropertyPath().length() > 0) {
					mi.lstIncludePropertyPaths.add(rm.includePropertyPath());
				}
				if (rm.includePropertyPaths() != null && rm.includePropertyPaths().length > 0) {
					for (String s : rm.includePropertyPaths()) {
						if (s.length() > 0) {
							mi.lstIncludePropertyPaths.add(s);
						}
					}
				}

				if (rm.pathTemplate() != null && rm.pathTemplate().length() > 0) {
					mi.pathTemplate = rm.pathTemplate();
				}

				if (rm.queryWhereClause() != null && rm.queryWhereClause().length() > 0) {
					mi.queryWhereClause = rm.queryWhereClause();
				}
				if (rm.queryOrderBy() != null && rm.queryOrderBy().length() > 0) {
					mi.queryOrderBy = rm.queryOrderBy();
				}
			}

			// ?? should be using template, query, body

			boolean bHasPathVariable = false;
			boolean bHasQueryNameValue = false;
			int cntUnassigned = 0;
			for (ParamInfo pi : mi.alParamInfo) {
				if (pi.paramType == null || pi.paramType == ParamType.Unassigned) {
					cntUnassigned++;
				} else {
					if (pi.paramType == ParamType.PathVariable) {
						bHasPathVariable = true;
						break;
					}
					if (pi.paramType == ParamType.QueryWhereNameValue) {
						bHasQueryNameValue = true;
						break;
					}
					continue;
				}
			}

			if (!bHasPathVariable && !bHasQueryNameValue) {
				for (ParamInfo pi : mi.alParamInfo) {
					if (pi.paramType != null && pi.paramType != ParamType.Unassigned) {
						continue;
					}

					if (pi.bNameAssigned) {
						if (mi.pathTemplate != null && cntUnassigned == 1) {
							pi.paramType = ParamType.PathVariable;
						} else {
							pi.paramType = ParamType.UrlQueryParam;
						}
					}
				}
			}

			if (mi.pathTemplate == null) {
				String newPathTemplate = "";
				for (ParamInfo pi : mi.alParamInfo) {
					if (pi.paramType == ParamType.Unassigned) {
						pi.paramType = ParamType.PathVariable;
					}
					if (pi.paramType == ParamType.PathVariable) {
						newPathTemplate += "/{" + pi.name + "}";
					}
				}
				if (newPathTemplate.length() > 0) {
					mi.pathTemplate = newPathTemplate;
				}
			} else {
				for (ParamInfo pi : mi.alParamInfo) {
					if (pi.paramType == RestParam.ParamType.PathVariable && !pi.bNameAssigned) {
						String s = "method param " + pi.name + " is not able to be used as PathVar since name is not defined, method="
								+ mi.method;
						throw new RuntimeException(s);

					}
				}
			}

			if (OAString.isNotEmpty(mi.pathTemplate)) {
				String s = mi.pathTemplate;
				s = OAString.convert(s, "{", "<%=$");
				s = OAString.convert(s, "}", "%>");
				mi.pathTemplate = s;
			}

			for (ParamInfo pi : mi.alParamInfo) {
				if (pi.paramType == ParamType.Unassigned) {
					String s = !pi.bNameAssigned ? " (name of param not defined)" : "";
					s = "method param=" + pi.name + s + " is not defined (ParamType) and is not able to be used, method=" + mi.method;
					throw new RuntimeException(s);
				}

				if (pi.paramType == ParamType.UrlPath) {
					if (pi.classType != ClassType.String) {
						String s = "method param " + pi.name + " type=UrlPath must be a String, method=" + mi.method;
						throw new RuntimeException(s);
					}
				}

				if (pi.paramType == ParamType.PathVariable && pi.classType != ClassType.Unassigned) {
					if (pi.classType != ClassType.String && pi.classType != ClassType.Date && pi.classType != ClassType.DateTime) {
						String s = "method param " + pi.name + " can not be used for pathVariable, method=" + mi.method;
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

		}
	}

	public String convertToString(Object obj) {
		// todo: use conversion library
		return "" + obj;
	}

	private static java.lang.reflect.Field fieldHttpURLConnectMethod;
	private static java.lang.reflect.Field fieldHttpsURLConnectMethod1;
	private static java.lang.reflect.Field fieldHttpsURLConnectMethod2;

	protected String callHttpEndPoint(String httpUrl, final String httpMethodName, final String requestBodyJson) throws Exception {

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

		String setcookie = conn.getHeaderField("Set-Cookie");
		if (OAString.isNotEmpty(setcookie)) {
			this.cookie = OAString.field(setcookie, ";", 1);
		}

		// https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
		int responseCode = conn.getResponseCode();

		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		for (;;) {
			int ch = br.read();
			if (ch < 0) {
				break;
			}
			sb.append((char) ch);
		}

		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();

		conn.disconnect();

		String result = sb.toString();
		if (responseCode < 200 || responseCode > 299) {
			String s = String.format(	"Error Response code: %d, msg=%s, result=%s",
										responseCode,
										conn.getResponseMessage(),
										result);

			throw new RuntimeException(s);
		}

		return result;
	}

	public String callJsonEndpoint(String url, String query, String httpMethod, String jsonBody) throws Exception {
		String strUrl = url;
		if (OAString.isNotEmpty(query)) {
			strUrl += "?" + query;
		}

		String jsonResult = callHttpEndPoint(strUrl, httpMethod, jsonBody);
		return jsonResult;
	}

}

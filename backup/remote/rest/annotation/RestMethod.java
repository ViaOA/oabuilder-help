/*  Copyright 1999 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.oreillyauto.storepurchaseorder.remote.rest.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Remoting information about remote methods.
 * <p>
 * Important: this annotation needs to be added to the Interface, not the Impl class.
 *
 * @author vvia
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestMethod {
	MethodType methodType() default MethodType.Unassigned;

	String name() default "";

	/**
	 * URL path for endpoint.
	 * <p>
	 * Supports path template, example: "/emp/{id}/{name}"<br>
	 * tags (case insensitive) are filled in from method params with paramType==UrlPathValue, and must have the @RestParam.name defined to
	 * match name used in template.
	 * <p>
	 * Supports path template, example: "/emp/?/?"<br>
	 * ? tags will use method params with paramType==UrlPathValue, to fill in from left to right.
	 * <p>
	 * <p>
	 * verify: if tags names, that there are matching names for all {x} tags<br>
	 * verify: if ?tags, that there are matching paramType==UrlPathValue<br>
	 * verify: cant mix {} and ? tags<br>
	 */
	String urlPath() default "";

	/**
	 * http query string.
	 * <p>
	 * verify: ignore starting '?'
	 */
	String urlQuery() default "";

	/**
	 * Query to use, with ? placeholders from params.
	 * <p>
	 * if ? is used for holders, then query will be formed in the order of the method params that have
	 * <p>
	 * requires OARestServlet on server.
	 * <p>
	 * verify: RestParam.queryWhere has exact amount of matching tags
	 */
	String searchWhere() default "";

	/**
	 * Query OrderBy to use.
	 * <p>
	 * if ? is used for holders, then query will be formed in the order of the method params that have
	 * <p>
	 * requires OARestServlet on server.
	 * <p>
	 * verify: RestMethod.queryWhere is not empty, or params MethodQueryOrderBy is not empty
	 */
	String searchOrderBy() default "";

	/**
	 * PropertyPath to include in result.
	 * <p>
	 * requires OARestServlet on server.
	 */
	String includePropertyPath() default "";

	/**
	 * PropertyPaths to include in result.
	 * <p>
	 * requires OARestServlet on server.
	 */
	String[] includePropertyPaths() default {}; // PP to include in result, supported by OARestServlet

	/**
	 * Number of reference levels to include in result.
	 * <p>
	 * requires OARestServlet on server.
	 */
	int includeReferenceLevelAmount() default 0;

	/**
	 * The method name to call, when methodType=OAObjectMethodCall, or methodType=OARemote
	 * <p>
	 * requires OARestServlet on server.
	 * <p>
	 * verify: methodType=OAObjectMethodCall
	 */
	String methodName() default "";

	Class returnClass() default Void.class;

	public static enum MethodType {
		/**
		 * Will produced an error.
		 * <p>
		 * verify: throws exception<br>
		 */
		Unassigned,
		/**
		 * Use http GET
		 * <p>
		 * verify: method must have urlPath, ex: /customers<br>
		 */
		GET(),

		/**
		 * Use http GET to get an OAObject/Id
		 * <p>
		 * automatically adds default oarestservlet url.
		 * <p>
		 * requires OARestServlet on server.
		 * <p>
		 * derives: urlPath as "/customer/12"
		 * <p>
		 * verify: return value is OAObject<br>
		 * verify: 1+ params are object Id<br>
		 * verify: that url can be derived<br>
		 * verify: make sure param types are valid for get, and not search, etc<br>
		 */
		OAGet(false),

		/**
		 * Use http POST to call oarestservlet to query model objects.
		 * <p>
		 * requires OARestServlet on server.
		 * <p>
		 * Supports:<br>
		 * searchWhere, searchOrderBy, includePropertyPath(s), includeReferenceLevelAmount
		 * <p>
		 * RestParam:<br>
		 * MethodSearchWhere, MethodSearchOrderBy, SearchWhereValue, SearchWhereNameValue
		 * <p>
		 * requires OARestServlet on server.
		 * <p>
		 * verify: method must have urlPath, ex: /customers<br>
		 * verify: return value must be array, list, Hub<br>
		 * verify: return value object type must be OAObject<br>
		 * verify: make sure param types are valid for search<br>
		 * verify: that url can be derived<br>
		 */
		OASearch(false),

		POST,

		PUT,

		PATCH,

		/**
		 * Use http POST to call oarestservlet to call an OAObject method.
		 * <p>
		 * First method argument is the OAObject,
		 * <p>
		 * automatically adds default oarestservlet url.
		 * <p>
		 * Supports:<br>
		 * objectMethodName, includePropertyPath(s), includeReferenceLevelAmount
		 * <p>
		 * RestParam:<br>
		 * OAObjectRemoteArg
		 * <p>
		 * requires OARestServlet on server.
		 * <p>
		 * qqqqqqqqqqqq automatically set if objectMethodName is not empty qqqqqqqqqqqqqq verify: require objectMethodName qqqqqqqqqqq
		 * qqqqqqqqq verify RestParam.OAObjectMethodCallObject is not empty
		 * <p>
		 * verify: must have method name</br>
		 * verify: must have url, without tags, ex: /customer</br>
		 * verify: first param must be type OAObjectMethodCallObject<br>
		 * verify: first param must be OAObject<br>
		 * verify: dont allow params for type Unassigned, need to be OAObjectMethodCallArg or Ignore</br>
		 * verify: make sure param types are valid for search<br>
		 * verify: that url can be derived<br>
		 */
		OAObjectMethodCall(false),

		/**
		 * Used internally when calling methods on a remote object.
		 * <p>
		 * requires OARestServlet on server.
		 * <p>
		 * verify: dont allow, only used internally for proxy.onInvoke</br>
		 * verify: that url can be derived<br>
		 */
		OARemote(false),

		/**
		 * Uses http PUT to create new OAObject.
		 * <p>
		 * automatically adds default oarestservlet url.
		 * <p>
		 * Supports:<br>
		 * <p>
		 * requires OARestServlet on server.
		 * <p>
		 * verify: urlPath, ex: /customer</br>
		 * verify: first param is OAObject</br>
		 * verify: that url can be derived<br>
		 */
		OAInsert(false),

		/**
		 * Use http POST to create new OAObject.
		 * <p>
		 * automatically adds default oarestservlet url.
		 * <p>
		 * Supports:<br>
		 * <p>
		 * requires OARestServlet on server.
		 * <p>
		 * verify: same as OAInsert</br>
		 * verify: that url can be derived<br>
		 */
		OAUpdate(false),

		/**
		 * Use http DELETE to delete an OAObject.
		 * <p>
		 * automatically adds default oarestservlet url.
		 * <p>
		 * Supports:<br>
		 * <p>
		 * requires OARestServlet on server.
		 * <p>
		 * verify: same as OAInsert</br>
		 * verify: that url can be derived<br>
		 */
		OADelete(false);

		public boolean requiresUrlPath = true;

		MethodType() {
		}

		MethodType(boolean requiresUrlPath) {
			this.requiresUrlPath = requiresUrlPath;
		}

		boolean isOA() {
			String s = this.toString();
			return "OA".equals(s);
		}
	}

}

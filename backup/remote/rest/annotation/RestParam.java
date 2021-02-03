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
 * Information about remote method parameters.
 * <p>
 * Important: this annotation needs to be added to the Interface, not the Impl class.
 *
 * @author vvia
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestParam {
	String name() default ""; // default is to use the name of the param defined in the method.

	Class paramClass() default Void.class;

	String format() default "";

	/**
	 * property path to include when serializing this value.
	 */
	String includePropertyPath() default ""; // supported by OARestServlet

	/**
	 * property paths to include when serializing this value.
	 */
	String[] includePropertyPaths() default {}; // supported by OARestServlet

	/**
	 * property path levels to include when serializing this value.
	 */
	int includeReferenceLevelAmount() default 0; // supported by OARestServlet

	/**
	 * The type of param that this is used for.
	 */
	ParamType paramType() default ParamType.Unassigned;

	public static enum ParamType {
		Unassigned,

		UrlPath, // use the value to be the urlPath

		UrlQueryParam, // url query parameter

		MethodReturnClass, // defines the type of return for the method.  Used when using generics, ex: List<T>

		PathVariable, // value is used in the url path.  See RestMethod.urlPath template

		QueryWhereClause, // value of param is the queryWhereClause.  Supported by OARestServlet

		QueryWhereParam, // use the value(s) of this param for the queryWhereClause inputs

		QueryOrderBy, // value of param is the queryOrderBy.  Supported by OARestServlet

		QueryWhereNameValue, // use name=value to add to where clause.

		BodyObject, // convert to json and send in body (will be default to any passed objs)

		BodyJson, // use this json in the body.

		Header, // put value in http header

		Cookie, // put value in http cookie

		/**
		 * value is to be used as the page number. a value <= 0 is for all pages. Can be used as header or query string (controlled by
		 * client config)
		 */
		PageNumber,

		/**
		 * value is property path(s) to include in response, and String[] array, or List<String>
		 */
		ResponseIncludePropertyPaths
	}

}

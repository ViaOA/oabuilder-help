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
 * Remoting information about remote methods. Important: this annotation needs to be added to the Interface, not the Impl class.
 *
 * @author vvia
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestMethod {
	MethodType methodType() default MethodType.Unassigned;

	String name() default "";

	String urlPath() default ""; // includes path param template,  example: "/emp/{id}"

	String queryWhereClause() default ""; // supported by OARestServlet

	String queryOrderBy() default ""; // supported by OARestServlet

	String extraUrlQueryParams() default "";

	// int timeoutSeconds() default 0;

	String includePropertyPath() default ""; // supported by OARestServlet

	String[] includePropertyPaths() default {}; // supported by OARestServlet

	int includeReferenceLevelAmount() default 0; // number of levels of references to include in return object. Supported by OARestServlet

	// use template to define url path. ex:  /{id}
	String pathTemplate() default "";

	public static enum MethodType {
		Unassigned,
		GetObjectsUsingQuery, // create object query using queryParams.  Supported by OARestServlet "?query=.."
		GetObjectsUsingIDs, // returns list of objects using queryParams.  Supported by OARestServlet "?query=.."
		GET,
		POST,
		PUT,
		PATCH
	}

}

/*
 * YARN Simplified API layer for services
 * Bringing a new service on YARN today is not a simple experience. The APIs of existing frameworks are either too low level (native YARN), require writing new code (for frameworks with programmatic APIs) or writing a complex spec (for declarative frameworks).  This simplified REST API can be used to create and manage the lifecycle of YARN services. In most cases, the application owner will not be forced to make any changes to their applications. This is primarily true if the application is packaged with containerization technologies like Docker.  This document describes the API specifications (aka. YarnFile) for deploying/managing containerized services on YARN. The same JSON spec can be used for both REST API and CLI to manage the services.
 *
 * OpenAPI spec version: 1.0.0
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package org.apache.cb.yarn.service.api;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-08-08T16:59:40.572+02:00")
public class StringUtil {
    /**
     * Check if the given array contains the given value (with case-insensitive comparison).
     *
     * @param array The array
     * @param value The value to search
     * @return true if the array contains the value
     */
    public static boolean containsIgnoreCase(String[] array, String value) {
        for (String str : array) {
            if (value == null && str == null) return true;
            if (value != null && value.equalsIgnoreCase(str)) return true;
        }
        return false;
    }

    /**
     * Join an array of strings with the given separator.
     * <p>
     * Note: This might be replaced by utility method from commons-lang or guava someday
     * if one of those libraries is added as dependency.
     * </p>
     *
     * @param array     The array of strings
     * @param separator The separator
     * @return the resulting string
     */
    public static String join(String[] array, String separator) {
        int len = array.length;
        if (len == 0) return "";

        StringBuilder out = new StringBuilder();
        out.append(array[0]);
        for (int i = 1; i < len; i++) {
            out.append(separator).append(array[i]);
        }
        return out.toString();
    }
}

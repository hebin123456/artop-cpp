/**
 * <copyright>
 *  
 * Copyright (c) 2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *  
 * Contributors: 
 *     itemis - Initial API and implementation
 *  
 * </copyright>
 */
package org.eclipse.sphinx.emf.serialization.generators.util;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("nls")
public class JavaXSDPrimitiveTypeMapping {
	// a map of java primitive type class and XSD primitive type class name, initialized at the same time as initSchema
	public static Map<Class<?>, String> javaXsdPrimitiveTypeMapping;
	static {
		javaXsdPrimitiveTypeMapping = new HashMap<Class<?>, String>();

		javaXsdPrimitiveTypeMapping.put(java.lang.String.class, "string");
		javaXsdPrimitiveTypeMapping.put(char.class, "string");
		javaXsdPrimitiveTypeMapping.put(java.lang.Character.class, "string");
		javaXsdPrimitiveTypeMapping.put(boolean.class, "boolean");
		javaXsdPrimitiveTypeMapping.put(java.lang.Boolean.class, "boolean");
		javaXsdPrimitiveTypeMapping.put(byte.class, "byte");
		javaXsdPrimitiveTypeMapping.put(java.lang.Byte.class, "byte");
		javaXsdPrimitiveTypeMapping.put(short.class, "short");
		javaXsdPrimitiveTypeMapping.put(java.lang.Short.class, "short");
		javaXsdPrimitiveTypeMapping.put(int.class, "int");
		javaXsdPrimitiveTypeMapping.put(java.lang.Integer.class, "int");
		javaXsdPrimitiveTypeMapping.put(long.class, "long");
		javaXsdPrimitiveTypeMapping.put(java.lang.Long.class, "long");
		javaXsdPrimitiveTypeMapping.put(float.class, "float");
		javaXsdPrimitiveTypeMapping.put(java.lang.Float.class, "float");
		javaXsdPrimitiveTypeMapping.put(double.class, "double");
		javaXsdPrimitiveTypeMapping.put(java.lang.Double.class, "double");
		javaXsdPrimitiveTypeMapping.put(java.math.BigInteger.class, "integer");
		javaXsdPrimitiveTypeMapping.put(java.math.BigDecimal.class, "decimal");
		javaXsdPrimitiveTypeMapping.put(java.util.Date.class, "dateTime");
	}
}

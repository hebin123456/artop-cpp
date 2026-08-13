/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.tests.platform.util;

import java.util.List;

public class Data {
	public String publicField;
	private String privateField;
	protected String protectedField;

	public static final String pubSField = "public static field"; //$NON-NLS-1$
	protected static final String proSField = "protected static field"; //$NON-NLS-1$
	private static final String priSField = "private static field"; //$NON-NLS-1$

	private List<String> strList;

	public Data(String strPublicValue, String strPrivateValue, String strProtectedValue) {
		publicField = strPublicValue;
		setPrivateField(strPrivateValue);
		protectedField = strProtectedValue;
	}

	public static String getPrisfield() {
		return priSField;
	}

	public static String getProsfield() {
		return proSField;
	}

	public void setPrivateField(String privateField) {
		this.privateField = privateField;
	}

	public String getPrivateField() {
		return privateField;
	}

	protected static String proctectedStaticMethod() {
		return proSField;
	}

	private static String privateStaticMethod() {
		return priSField;
	}
}

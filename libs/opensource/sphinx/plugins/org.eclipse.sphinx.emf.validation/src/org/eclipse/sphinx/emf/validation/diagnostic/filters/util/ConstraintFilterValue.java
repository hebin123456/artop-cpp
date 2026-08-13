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
package org.eclipse.sphinx.emf.validation.diagnostic.filters.util;

public enum ConstraintFilterValue {

	WIZARD("WIZARD", 1), PROPERTY_SHEET("PROPERTY_SHEET", 2); //$NON-NLS-1$ //$NON-NLS-2$

	static private ConstraintFilterValue enums[] = { WIZARD, PROPERTY_SHEET };

	private final String literal;
	private final int value;

	private ConstraintFilterValue(String literal, int value) {
		this.literal = literal;
		this.value = value;
	}

	public String literal() {
		return literal;
	}

	public double value() {
		return value;
	}

	static public ConstraintFilterValue convert(String v) {
		for (ConstraintFilterValue current : enums) {
			if (current.literal().equals(v)) {
				return current;
			}
		}
		return null;
	}
}

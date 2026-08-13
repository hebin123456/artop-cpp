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
package org.eclipse.sphinx.emf.internal.ecore;

import java.util.Comparator;

import org.eclipse.emf.common.util.Enumerator;

/**
 * Enumerator comparator.
 * <p>
 * Compares two Enumerator by comparing their respective labels.
 * 
 * @param <T>
 *            The type of Enumerator that can be compared.
 */
public class EnumeratorComparator<T extends Enumerator> implements Comparator<T> {

	@Override
	public int compare(T e1, T e2) {
		return compareNames(e1.getName(), e2.getName());
	}

	private int compareNames(String n1, String n2) {
		if (n1 == null && n2 == null) {
			return 0;
		} else if (n1 == null) {
			return 1;
		} else if (n2 == null) {
			return -1;
		} else {
			return n1.compareTo(n2);
		}
	}

}

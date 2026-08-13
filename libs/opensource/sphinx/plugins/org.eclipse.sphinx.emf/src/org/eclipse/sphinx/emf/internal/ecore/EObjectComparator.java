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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * EObject comparator.
 * <p>
 * Compares two EObject by comparing their respective IDs.
 * 
 * @param <T>
 *            The type of EObject that can be compared.
 */
public class EObjectComparator<T extends EObject> implements Comparator<T> {

	@Override
	public int compare(T o1, T o2) {
		return compareIDs(EcoreUtil.getID(o1), EcoreUtil.getID(o2));
	}

	private int compareIDs(String id1, String id2) {
		if (id1 == null && id2 == null) {
			return 0;
		} else if (id1 == null) {
			return 1;
		} else if (id2 == null) {
			return -1;
		} else {
			return id1.compareTo(id2);
		}
	}

}

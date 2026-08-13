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
package org.eclipse.sphinx.emf.check.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public class DiagnosticLocation {

	private EObject object;

	private EStructuralFeature feature;

	private int index;

	public DiagnosticLocation(EObject object, EStructuralFeature feature, int index) {
		setObject(object);
		setFeature(feature);
		setIndex(index);
	}

	public EObject getObject() {
		return object;
	}

	public void setObject(EObject object) {
		this.object = object;
	}

	public EStructuralFeature getFeature() {
		return feature;
	}

	public void setFeature(EStructuralFeature feature) {
		this.feature = feature;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}

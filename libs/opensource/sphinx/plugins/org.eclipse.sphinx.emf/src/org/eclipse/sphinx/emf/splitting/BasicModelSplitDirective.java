/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [474952] Replication of Ancestor Feature filters to much
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.splitting;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public class BasicModelSplitDirective implements IModelSplitDirective {

	protected EObject eObject;
	protected URI targetResourceURI;
	protected boolean stripAncestors;

	public BasicModelSplitDirective(EObject eObject, URI targetResourceURI) {
		this(eObject, targetResourceURI, false);
	}

	public BasicModelSplitDirective(EObject eObject, URI targetResourceURI, boolean stripAncestors) {
		this.eObject = eObject;
		this.targetResourceURI = targetResourceURI;
		this.stripAncestors = stripAncestors;
	}

	/*
	 * @see org.eclipse.sphinx.emf.splitting.IModelSplitDirective#getEObject()
	 */
	@Override
	public EObject getEObject() {
		return eObject;
	}

	/*
	 * @see org.eclipse.sphinx.emf.splitting.IModelSplitDirective#getTargetResourceURI()
	 */
	@Override
	public URI getTargetResourceURI() {
		return targetResourceURI;
	}

	/*
	 * @see org.eclipse.sphinx.emf.splitting.IModelSplitDirective#isIgnoreAncestorAttributes()
	 */
	@Override
	public boolean stripAncestors() {
		return stripAncestors;
	}

	/*
	 * @see org.eclipse.sphinx.emf.splitting.IModelSplitDirective#shouldReplicateAncestorFeature(org.eclipse.emf.ecore.
	 * EObject , org.eclipse.emf.ecore.EStructuralFeature)
	 */
	@Override
	public boolean shouldReplicateAncestorFeature(EObject ancestor, EStructuralFeature feature) {
		// Ancestor objects to be replicated including their intrinsic properties?
		if (!stripAncestors) {
			// Replicate all attributes
			return feature instanceof EAttribute;
		} else {
			return false;
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.splitting.IModelSplitDirective#isValid()
	 */
	@Override
	public boolean isValid() {
		if (eObject == null) {
			return false;
		}
		if (targetResourceURI == null) {
			return false;
		}
		// EObject to be split already in specified target resource?
		if (eObject.eResource() != null && eObject.eResource().getURI() == targetResourceURI) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return getClass().getName() + " [eObject=" + eObject + ", targetResourceURI=" + targetResourceURI + ", stripAncestors=" + stripAncestors
				+ "]";
	}
}

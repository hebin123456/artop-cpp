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
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.splitting;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sphinx.emf.splitting.BasicModelSplitDirective;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Package;

public class Hummingbird20ModelSplitDirective extends BasicModelSplitDirective {

	public Hummingbird20ModelSplitDirective(EObject eObject, URI targetResourceURI) {
		super(eObject, targetResourceURI);
	}

	public Hummingbird20ModelSplitDirective(EObject eObject, URI targetResourceURI, boolean stripAncestors) {
		super(eObject, targetResourceURI, stripAncestors);
	}

	@Override
	public boolean shouldReplicateAncestorFeature(EObject ancestor, EStructuralFeature feature) {
		// Ancestor objects to be replicated including their intrinsic properties?
		if (!stripAncestors()) {
			// Replicate all attributes and Description objects
			return feature instanceof EAttribute || feature == Common20Package.eINSTANCE.getIdentifiable_Description();
		} else {
			// Always replicate name attribute at least
			return feature == Common20Package.eINSTANCE.getIdentifiable_Name();
		}
	}
}

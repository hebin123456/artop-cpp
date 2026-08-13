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
package org.eclipse.sphinx.emf.ecore;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * An interface providing methods for collecting selected EObjects from an EMF model.
 */
public interface EcoreTraversalHelper {

	/**
	 * Returns all EObjects reachable from a specified EObject with a given EReference. An EObject is reachable if a
	 * connection from the specified <code>referenceSource</code> to the EObject can be established with the provided
	 * <code>reference</code>.
	 * 
	 * @param referenceSource
	 *            The EObject from which a direct connection to the returned EObjects could be established by the
	 *            <code>reference</code> provided.
	 * @param reference
	 *            The EReference describing the type of reference.
	 * @return All EObjects which can be connected to the <code>referenceSource</code> directly via the given
	 *         EReference.
	 * @see org.eclipse.sphinx.emf.resource.IResourceFilter
	 * @since 0.7.0
	 */
	Collection<EObject> getReachableEObjects(EObject referenceSource, EReference reference);

	List<? extends EStructuralFeature> getFeaturesToTraverseFor(EObject object, EClassifier type);

	/**
	 * Collects all EObjects of a specified type which are descendants of the given EObject.
	 * 
	 * @param result
	 *            The EObjects of the specified <code>type</code> found beneath the <code>root</code>.
	 * @param root
	 *            The EObject from which the descending search starts.
	 * @param type
	 *            The type of which the collected EObjects are to be of.
	 * @return <tt>true</tt> if the method handled the collection, else <tt>false</tt>.
	 * @since 0.7.0
	 */
	boolean collectReachableObjectsOfTypeUnderObject(Collection<EObject> result, EObject root, EClassifier type);

}

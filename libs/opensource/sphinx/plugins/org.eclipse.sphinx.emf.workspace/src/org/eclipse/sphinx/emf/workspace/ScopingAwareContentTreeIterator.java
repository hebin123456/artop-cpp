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
package org.eclipse.sphinx.emf.workspace;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil.ContentTreeIterator;
import org.eclipse.sphinx.emf.resource.ScopingResourceSet;

/**
 * A content tree iterator that is aware of scoping resource sets and only considers resources that can be reached
 * (transitively) through the resource scopes.
 */
public class ScopingAwareContentTreeIterator<E> extends ContentTreeIterator<E> {

	private static final long serialVersionUID = -3507911756978908177L;

	protected Object contextObject;

	/**
	 * Similar to super, only takes as a parameter the context that should be taken into account.
	 *
	 * @param emfObjects
	 *            the collection of objects to iterate over.
	 * @param contextObject
	 *            the context object.
	 */
	protected ScopingAwareContentTreeIterator(Collection<?> emfObjects, Object contextObject) {
		super(emfObjects);
		this.contextObject = contextObject;
	}

	/**
	 * Similar to super, only takes as a parameter the context that should be taken into account.
	 *
	 * @param object
	 *            the collection of objects to iterate over.
	 * @param isResolveProxies
	 *            whether proxies should be resolved during the traversal.
	 * @param contextObject
	 *            the context object.
	 */
	public ScopingAwareContentTreeIterator(Object object, boolean isResolveProxies, Object contextObject) {
		super(object, isResolveProxies);
		this.contextObject = contextObject;
	}

	@Override
	protected Iterator<Resource> getResourceSetChildren(ResourceSet resourceSet) {
		if (object instanceof ScopingResourceSet && contextObject != null) {
			return resourceSetIterator = new ResourcesIterator(((ScopingResourceSet) object).getResourcesInScope(contextObject));
		}
		return super.getResourceSetChildren(resourceSet);
	}
}

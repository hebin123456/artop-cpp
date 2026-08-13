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
package org.eclipse.sphinx.emf.resource;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * An {@link Adapter adapter} for {@link EObject}s that implements {@link OldResourceProvider}.
 */
public class OldResourceProviderAdapter extends AdapterImpl implements OldResourceProvider {

	private Resource oldResource;

	public OldResourceProviderAdapter(Resource oldResource) {
		this.oldResource = oldResource;
	}

	/*
	 * @see org.eclipse.sphinx.emf.resource.OldResourceProvider#getOldResource()
	 */
	@Override
	public Resource getOldResource() {
		return oldResource;
	}

	/*
	 * @see org.eclipse.emf.common.notify.impl.AdapterImpl#isAdapterForType(java.lang.Object)
	 */
	@Override
	public boolean isAdapterForType(Object type) {
		return type == OldResourceProvider.class;
	}

	/*
	 * @see org.eclipse.emf.common.notify.impl.AdapterImpl#setTarget(org.eclipse.emf.common.notify.Notifier)
	 */
	@Override
	public void setTarget(Notifier newTarget) {
		Assert.isLegal(newTarget == null || newTarget instanceof EObject);
		super.setTarget(newTarget);
	}
}

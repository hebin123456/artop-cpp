/**
 * <copyright>
 * 
 * Copyright (c) 2013 itemis and others.
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
package org.eclipse.sphinx.emf.workspace.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.sphinx.emf.loading.IModelLoadService;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadService;

public class MetaModelDescriptorAdapterFactory implements IAdapterFactory {

	private ModelLoadService modelLoadService = new ModelLoadService();

	/*
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(final Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {

		if (adapterType.equals(IModelLoadService.class) && adaptableObject instanceof IMetaModelDescriptor) {
			return modelLoadService;
		}
		return null;
	}

	/*
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class<?>[] { IModelLoadService.class };
	}
}

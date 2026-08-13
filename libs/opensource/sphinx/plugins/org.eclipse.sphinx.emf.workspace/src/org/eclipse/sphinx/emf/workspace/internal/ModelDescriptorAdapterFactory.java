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
package org.eclipse.sphinx.emf.workspace.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.saving.IModelSaveIndicator;
import org.eclipse.sphinx.emf.workspace.internal.saving.ModelSaveIndicator;

public class ModelDescriptorAdapterFactory implements IAdapterFactory {

	IModelSaveIndicator saveIndicator = new ModelSaveIndicator();

	@Override
	public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
		if (IModelSaveIndicator.class.equals(adapterType) && adaptableObject instanceof IModelDescriptor) {
			return saveIndicator;
		}
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class<?>[] { IModelSaveIndicator.class };
	}

}

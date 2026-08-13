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
package org.eclipse.sphinx.examples.hummingbird20.edit;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;

public class Hummingbird20MMDescriptorItemProviderAdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
		if (adapterType.equals(IItemLabelProvider.class)) {
			if (adaptableObject instanceof Hummingbird20MMDescriptor) {
				return new Hummingbird20MMDescriptorItemLabelProvider();
			}
		}
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class<?>[] { IItemLabelProvider.class };
	}
}

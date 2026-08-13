/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys and others.
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
package org.eclipse.sphinx.xtendxpand.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;

public class EObjectAdapaterFactory implements IAdapterFactory {

	public static final EObjectAdapaterFactory INSTANCE = new EObjectAdapaterFactory();

	@Override
	public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
		if (IResource.class.equals(adapterType)) {
			return EcorePlatformUtil.getFile(adaptableObject);
		}
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IResource.class };
	}
}

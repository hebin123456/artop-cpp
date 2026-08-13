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
package org.eclipse.sphinx.examples.codegen.xpand.ui.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.sphinx.examples.codegen.xpand.ui.preferences.OutletsPreferencePage;
import org.eclipse.sphinx.platform.ui.preferences.IPropertyPageIdProvider;
import org.eclipse.sphinx.xtendxpand.preferences.OutletsPreference;

public class OutletsPreferenceAdapterFactory implements IAdapterFactory {

	/*
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(final Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
		if (adapterType.equals(IPropertyPageIdProvider.class)) {
			if (adaptableObject instanceof OutletsPreference) {
				return new IPropertyPageIdProvider() {
					@Override
					public String getPropertyPageId() {
						return OutletsPreferencePage.PROPERTY_PAGE_ID;
					}
				};
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class<?>[] { IPropertyPageIdProvider.class };
	}
}

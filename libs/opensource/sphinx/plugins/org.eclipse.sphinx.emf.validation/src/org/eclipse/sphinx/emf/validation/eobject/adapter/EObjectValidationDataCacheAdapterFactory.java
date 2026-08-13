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
package org.eclipse.sphinx.emf.validation.eobject.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

/**
 * The Adapter factory for the {@link EObjectValidationDataCacheAdapter} adapters.
 */
public class EObjectValidationDataCacheAdapterFactory extends AdapterFactoryImpl {

	private static ArrayList<WeakReference<EObjectValidationDataCacheAdapter>> vAdapters = new ArrayList<WeakReference<EObjectValidationDataCacheAdapter>>();

	@Override
	public boolean isFactoryForType(Object type) {
		return type == EObjectValidationDataCacheAdapter.class;
	}

	@Override
	protected Adapter createAdapter(Notifier target) {
		EObjectValidationDataCacheAdapter e = new EObjectValidationDataCacheAdapter();
		vAdapters.add(new WeakReference<EObjectValidationDataCacheAdapter>(e));
		return e;
	}

	public static EObjectValidationDataCacheAdapterFactory INSTANCE = new EObjectValidationDataCacheAdapterFactory();

	/**
	 * Tools to invalidate {@link EObjectValidationDataCacheAdapter} adapter
	 */
	public static void initVAdapters() {
		for (Iterator<WeakReference<EObjectValidationDataCacheAdapter>> i = vAdapters.listIterator(); i.hasNext();) {
			EObjectValidationDataCacheAdapter current = i.next().get();
			if (current == null) {
				// NdSam: this fixes a major memory leak trouble. In the previous implementation the list of
				// vAdapters wont reduce, and each adapter contained a reference to a model object via the target field
				// preventing the objects from being garbage collected.
				i.remove();
			} else {
				current.setSeverityDataOk(EObjectValidationDataCacheAdapter.DEFAULT_SEVERITY_OK);
			}
		}
	}
}

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
package org.eclipse.sphinx.emf.validation.listeners;

import org.eclipse.emf.validation.service.IValidationListener;
import org.eclipse.emf.validation.service.ValidationEvent;
import org.eclipse.sphinx.emf.validation.eobject.adapter.EObjectValidationDataCacheAdapterFactory;

/**
 * A listener listening for {@link org.eclipse.emf.validation.service.ValidationEvent validation events} in order to
 * clean an {@link org.eclipse.sphinx.emf.validation.eobject.adapter.EObjectValidationDataCacheAdapter
 * EObjectValidationDataCacheAdapter}.
 */
public class CachedDataCleaner implements IValidationListener {

	@Override
	public void validationOccurred(ValidationEvent event) {
		EObjectValidationDataCacheAdapterFactory.initVAdapters();
	}
}

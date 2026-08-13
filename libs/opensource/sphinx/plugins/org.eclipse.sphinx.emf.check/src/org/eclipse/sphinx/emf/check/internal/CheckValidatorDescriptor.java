/**
 * <copyright>
 *
 * Copyright (c) 2014-2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [458976] Validators are not singleton when they implement checks for different EPackages
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.emf.common.util.URI;
import org.eclipse.sphinx.emf.check.ICheckValidator;
import org.eclipse.sphinx.platform.util.ExtensionClassDescriptor;

public class CheckValidatorDescriptor extends ExtensionClassDescriptor<ICheckValidator> {

	private static final String ATTR_CATALOG = "catalog"; //$NON-NLS-1$

	private String catalog = null;

	public CheckValidatorDescriptor(IConfigurationElement configurationElement) {
		super(configurationElement);
		catalog = configurationElement.getAttribute(ATTR_CATALOG);
	}

	public URI getCatalogURI() {
		if (catalog != null) {
			URI catalogURI = URI.createURI(catalog);
			if (!catalogURI.isPlatformPlugin()) {
				// Create URI of contributor plug-in
				/*
				 * !! Important Note !! Make sure that the plug-in URI has a trailing separator; otherwise its last
				 * segment, i.e. the plug-in id, gets discarded when it is used as base URI in URI resolve operations
				 * (see org.eclipse.emf.common.util.URI.Hierarchical#mergePath(URI, boolean) for details).
				 */
				URI contributorPluginURI = URI.createPlatformPluginURI(getContributorPluginId(), true).appendSegment(""); //$NON-NLS-1$

				// Resolve relative catalog URI against contributor plug-in
				catalogURI = catalogURI.resolve(contributorPluginURI);
			}
			return catalogURI;
		}
		return null;
	}
}

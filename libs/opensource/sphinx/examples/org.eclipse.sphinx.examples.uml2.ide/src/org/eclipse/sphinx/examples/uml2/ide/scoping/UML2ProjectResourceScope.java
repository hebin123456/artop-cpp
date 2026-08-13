/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
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
package org.eclipse.sphinx.examples.uml2.ide.scoping;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.sphinx.emf.scoping.ProjectResourceScope;

public class UML2ProjectResourceScope extends ProjectResourceScope {

	private static final Pattern UML_PACKAGE_URI_PATTERN = Pattern.compile("http://www\\.eclipse\\.org/uml2/[0-9]\\.[0-9]\\.[0-9]/UML*"); //("http://(schema|www)\\.omg\\.org(/spec)?/XMI.*"); //$NON-NLS-1$

	public UML2ProjectResourceScope(IResource resource) {
		super(resource);
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.AbstractResourceScope#isShared(org.eclipse.emf.common.util.URI)
	 */
	@Override
	public boolean isShared(URI uri) {
		if (super.isShared(uri)) {
			return true;
		}

		// Consider URIs matching UML_PACKAGE_URI_PATTERN as shared as well
		if (uri != null) {
			return UML_PACKAGE_URI_PATTERN.matcher(uri.toString()).matches();
		}

		return false;
	}
}

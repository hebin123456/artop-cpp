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
package org.eclipse.sphinx.emf.scoping;

import org.eclipse.core.resources.IResource;

public class FileResourceScopeProvider extends AbstractResourceScopeProvider {

	/*
	 * @see
	 * org.eclipse.sphinx.emf.scoping.AbstractResourceScopeProvider#createScope(org.eclipse.core.resources.IResource)
	 */
	@Override
	protected FileResourceScope createScope(IResource resource) {
		return new FileResourceScope(resource);
	}
}
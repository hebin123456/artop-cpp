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

import org.eclipse.core.resources.IResource;
import org.eclipse.sphinx.emf.scoping.ProjectResourceScope;
import org.eclipse.sphinx.emf.scoping.ProjectResourceScopeProvider;

public class UML2ProjectResourceScopeProvider extends ProjectResourceScopeProvider {

	/*
	 * @see
	 * org.eclipse.sphinx.emf.scoping.ProjectResourceScopeProvider#createScope(org.eclipse.core.resources.IResource)
	 */
	@Override
	protected ProjectResourceScope createScope(IResource resource) {
		return new UML2ProjectResourceScope(resource);
	}
}

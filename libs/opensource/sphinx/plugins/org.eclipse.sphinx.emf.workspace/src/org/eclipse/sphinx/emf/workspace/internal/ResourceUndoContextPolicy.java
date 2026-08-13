/**
 * <copyright>
 * 
 * Copyright (c) 2008-2013 Continental Engineering Services (CES) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     CES - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.internal;

import org.eclipse.emf.workspace.AbstractResourceUndoContextPolicy;
import org.eclipse.emf.workspace.IResourceUndoContextPolicy;

/**
 * An {@link IResourceUndoContextPolicy} implementation that disables pessimistic handling of cross-resource references.
 */
public class ResourceUndoContextPolicy extends AbstractResourceUndoContextPolicy {

	public static final ResourceUndoContextPolicy INSTANCE = new ResourceUndoContextPolicy();

	/**
	 * Returns whether to consider changes to directed cross-resource references as affecting the referenced resource.
	 * This implementation returns false
	 * 
	 * @return always return false
	 */
	@Override
	protected boolean pessimisticCrossReferences() {
		return false;
	}

}

/**
 * <copyright>
 *
 * Copyright (c) 2008-2010 BMW Car IT, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     BMW Car IT - Initial API and implementation
 *     See4sys - Added support for EPackage URIs
 *     BMW Car IT - Added robustness and support for singleton instantiation of descriptors
 *     See4sys - Added facilities for retrieving descriptor(s) from identifier, name, ordinal, object, etc.
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.saving;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.sphinx.emf.model.IModelDescriptor;

/**
 * Provides methods for determining and changing the save status of {@link IModelDescriptor model}s.
 */
public interface IModelSaveIndicator {

	/**
	 * Determines if the given {@link IModelDescriptor model} is dirty. A {@link IModelDescriptor model} is considered
	 * dirty if it has {@link Resource resource}s whose content has been modified but not been saved yet.
	 * 
	 * @param modelDescriptor
	 *            The {@link IModelDescriptor model} to be investigated.
	 * @return <code>true</code> if specified {@link IModelDescriptor model} has dirty {@link Resource resource}s, or
	 *         <code>false</code> otherwise.
	 * @see #setSaved(IModelDescriptor)
	 */
	boolean isDirty(IModelDescriptor modelDescriptor);

	/**
	 * Clears dirty state of given {@link IModelDescriptor model} and remembers it as having just been saved. This
	 * method needs to be called by all clients which perform a save operation of some {@link IModelDescriptor model}
	 * right after the save operation has been completed. Clients can then call {@link #isSaved(EditingDomain, URI)} to
	 * determine if subsequently raised {@link IResourceChangeEvent resource change event}s are just a consequence of
	 * the preceding save operation or if the underlying {@link IFile file}s' content has been changed otherwise (e.g.,
	 * via a text editor or some other tool affecting the {@link Resource resource} in its serialized form).
	 * 
	 * @param modelDescriptor
	 *            The {@link IModelDescriptor model} to be handled.
	 * @see #isDirty(IModelDescriptor)
	 */
	void setSaved(IModelDescriptor modelDescriptor);
}

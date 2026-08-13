/**
 * <copyright>
 * 
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [419466] Enable models to be modified programmatically without causing them to become dirty
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.saving;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer.Delegate;

/**
 * Provides methods for determining and changing the save status of {@link Resource resource}s.
 */
public interface IResourceSaveIndicator extends Delegate {

	/**
	 * Determines if the given {@link Resource resource} is dirty. A {@link Resource resource} is considered dirty if
	 * its content has been modified but not been saved yet.
	 * 
	 * @param resource
	 *            The {@link Resource resource} to be investigated.
	 * @return <code>true</code> if specified {@link Resource resource} is dirty, or <code>false</code> otherwise.
	 * @see #setDirty(Resource)
	 * @see #getDirtyResources()
	 * @see #setSaved(Resource)
	 */
	boolean isDirty(Resource resource);

	/**
	 * Makes the given {@link Resource resource} dirty.
	 * 
	 * @param resource
	 *            The {@link Resource resource} to be handled.
	 * @see #isDirty(Resource)
	 * @see #unsetDirty(Resource)
	 * @see #getDirtyResources()
	 */
	void setDirty(Resource resource);

	/**
	 * Makes the given {@link Resource resource} un-dirty.
	 * 
	 * @param resource
	 *            The {@link Resource resource} to be handled.
	 * @see #isDirty(Resource)
	 * @see #setDirty(Resource)
	 * @see #getDirtyResources()
	 */
	void unsetDirty(Resource resource);

	/**
	 * Returns all {@link Resource resource}s in underlying {@link EditingDomain editing domain} which are currently
	 * dirty.
	 * 
	 * @return A collection with {@link Resource resource}s with are currently dirty, or and empty collection if no
	 *         dirty resources exist.
	 * @see #isDirty(Resource)
	 * @see #setDirty(Resource)
	 * @see #setSaved(Collection)
	 */
	Collection<Resource> getDirtyResources();

	/**
	 * Test if the {@link Resource resource} behind given {@link URI} has just been saved. This method typically needs
	 * to be called when an {@link IResourceChangeEvent resource change event} indicates that the content of the
	 * {@link IFile file} behind given {@link URI} has changed. Based on the returned result the caller can determine if
	 * a {@link IResourceChangeEvent resource change event} is just a consequence of a preceding save operation or if
	 * the {@link IFile file}'s content has been changed otherwise (e.g., via a text editor or some other tool affecting
	 * the {@link Resource resource} in its serialized form).
	 * 
	 * @param uri
	 *            The {@link URI} of the {@link Resource resource} to be investigated.
	 * @return <code>true</code> if given {@link Resource resource} has just been saved, or <code>false</code>
	 *         otherwise.
	 * @see #setSaved(Resource)
	 * @see #setSaved(Collection)
	 */
	boolean isSaved(URI uri);

	/**
	 * Clears dirty state of given {@link Resource resource} and remembers it as having just been saved. This method
	 * needs to be called by all clients which perform a save operation of some {@link Resource resource} right after
	 * the save operation has been completed. Clients can then call {@link #isSaved(URI)} to determine if subsequently
	 * raised {@link IResourceChangeEvent resource change event}s are just a consequence of the preceding save operation
	 * or if the underlying {@link IFile file}'s content has been changed otherwise (e.g., via a text editor or some
	 * other tool affecting the {@link Resource resource} in its serialized form).
	 * 
	 * @param resource
	 *            The {@link Resource resource} to be handled.
	 * @see #isDirty(Resource)
	 * @see #isSaved(URI)
	 * @see #setSaved(Collection)
	 */
	void setSaved(Resource resource);

	/**
	 * Clears dirty state of given collection of {@link Resource resource}s and remembers them as having just been
	 * saved. This method needs to be called by all clients which perform a save operation of some collection of
	 * {@link Resource resource}s right after the save operation has been completed. Clients can then call
	 * {@link #isSaved(URI)} to determine if subsequently raised {@link IResourceChangeEvent resource change event}s are
	 * just a consequence of the preceding save operation or if the underlying {@link IFile file}s' content has been
	 * changed otherwise (e.g., via a text editor or some other tool affecting the {@link Resource resource} in its
	 * serialized form).
	 * 
	 * @param resource
	 *            The collection of {@link Resource resource}s to be handled.
	 * @see #isDirty(Resource)
	 * @see #isSaved(URI)
	 * @see #setSaved(Collection)
	 */
	void setSaved(Collection<Resource> resources);
}
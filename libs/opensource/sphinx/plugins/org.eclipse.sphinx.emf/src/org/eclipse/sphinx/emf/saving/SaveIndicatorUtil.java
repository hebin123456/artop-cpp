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
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.sphinx.emf.model.IModelDescriptor;

/**
 * A utility class providing convenience methods for determining and changing the save status of individual
 * {@link Resource resource}s or entire {@link IModelDescriptor model}s.
 */
public class SaveIndicatorUtil {

	// Prevent from instantiation
	private SaveIndicatorUtil() {
		// Nothing to do
	}

	/**
	 * Returns an {@link IResourceSaveIndicator} for the provided {@link EditingDomain editing domain}.
	 * 
	 * @param editingDomain
	 *            The {@link EditingDomain editing domain} for which the {@link IResourceSaveIndicator} is to be
	 *            returned.
	 * @return An {@link IResourceSaveIndicator} for the given {@link EditingDomain editing domain}, or
	 *         <code>null</code> if no such is available.
	 */
	public static IResourceSaveIndicator getResourceSaveIndicator(EditingDomain editingDomain) {
		if (editingDomain != null) {
			return (IResourceSaveIndicator) Platform.getAdapterManager().loadAdapter(editingDomain, IResourceSaveIndicator.class.getName());
		}
		return null;
	}

	/**
	 * Returns an {@link IModelSaveIndicator} for the provided {@link IModelDescriptor model}.
	 * 
	 * @param modelDescriptor
	 *            The {@link IModelDescriptor model} for which the {@link IModelSaveIndicator} is to be returned.
	 * @return An {@link IModelSaveIndicator} for the given {@link IModelDescriptor model}, or <code>null</code> if no
	 *         such is available.
	 */
	public static IModelSaveIndicator getModelSaveIndicator(IModelDescriptor modelDescriptor) {
		if (modelDescriptor != null) {
			return (IModelSaveIndicator) Platform.getAdapterManager().loadAdapter(modelDescriptor, IModelSaveIndicator.class.getName());
		}
		return null;
	}

	/**
	 * Determines if the specified {@link Resource resource} in given {@link EditingDomain editing domain} is dirty. A
	 * {@link Resource resource} is considered dirty if its content has been modified but not been saved yet.
	 * 
	 * @param editingDomain
	 *            The {@link EditingDomain editing domain} the {@link Resource resource} in question is in.
	 * @param resource
	 *            The {@link Resource resource} to be investigated.
	 * @return <code>true</code> if specified {@link Resource resource} is present in given {@link EditingDomain editing
	 *         domain} and is dirty, or <code>false</code> otherwise.
	 * @see #setDirty(EditingDomain, Resource)
	 * @see #getDirtyResources(EditingDomain)
	 * @see #setSaved(EditingDomain, Resource)
	 */
	public static boolean isDirty(EditingDomain editingDomain, Resource resource) {
		IResourceSaveIndicator indicator = getResourceSaveIndicator(editingDomain);
		if (indicator != null) {
			return indicator.isDirty(resource);
		}
		return false;
	}

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
	public static boolean isDirty(IModelDescriptor modelDescriptor) {
		IModelSaveIndicator indicator = getModelSaveIndicator(modelDescriptor);
		if (indicator != null) {
			return indicator.isDirty(modelDescriptor);
		}
		return false;
	}

	/**
	 * Makes the specified {@link Resource resource} in given {@link EditingDomain editing domain} dirty.
	 * 
	 * @param editingDomain
	 *            The {@link EditingDomain editing domain} the {@link Resource resource} in question is in.
	 * @param resource
	 *            The {@link Resource resource} to be handled.
	 * @see #isDirty(EditingDomain, Resource)
	 * @see #unsetDirty(EditingDomain, Resource)
	 * @see #getDirtyResources(EditingDomain)
	 */
	public static void setDirty(EditingDomain editingDomain, Resource resource) {
		IResourceSaveIndicator indicator = getResourceSaveIndicator(editingDomain);
		if (indicator != null) {
			indicator.setDirty(resource);
		}
	}

	/**
	 * Makes the specified {@link Resource resource} in given {@link EditingDomain editing domain} un-dirty.
	 * 
	 * @param editingDomain
	 *            The {@link EditingDomain editing domain} the {@link Resource resource} in question is in.
	 * @param resource
	 *            The {@link Resource resource} to be handled.
	 * @see #isDirty(EditingDomain, Resource)
	 * @see #setDirty(EditingDomain, Resource)
	 * @see #getDirtyResources(EditingDomain)
	 */
	public static void unsetDirty(EditingDomain editingDomain, Resource resource) {
		IResourceSaveIndicator indicator = getResourceSaveIndicator(editingDomain);
		if (indicator != null) {
			indicator.unsetDirty(resource);
		}
	}

	/**
	 * Returns all {@link Resource resource}s in specified {@link EditingDomain editing domain} which are currently
	 * dirty.
	 * 
	 * @param editingDomain
	 *            The {@link EditingDomain editing domain} the potentially {@link Resource resource}s are in.
	 * @return A collection with {@link Resource resource}s which are present in given {@link EditingDomain editing
	 *         domain} and are currently dirty, or and empty collection if no dirty resources exist in given
	 *         {@link EditingDomain editing domain}.
	 * @see #isDirty(IModelDescriptor)
	 * @see #setDirty(EditingDomain, Resource)
	 * @see #setSaved(EditingDomain, Collection)
	 */
	public static Collection<Resource> getDirtyResources(EditingDomain editingDomain) {
		IResourceSaveIndicator indicator = getResourceSaveIndicator(editingDomain);
		if (indicator != null) {
			return indicator.getDirtyResources();
		}
		return Collections.emptySet();
	}

	/**
	 * Test if the {@link Resource resource} behind given {@link URI} in specified {@link EditingDomain editing domain}
	 * has just been saved. This method typically needs to be called when an {@link IResourceChangeEvent resource change
	 * event} indicates that the content of the {@link IFile file} behind given {@link URI} has changed. Based on the
	 * returned result the caller can determine if a {@link IResourceChangeEvent resource change event} is just a
	 * consequence of a preceding save operation or if the {@link IFile file}'s content has been changed otherwise
	 * (e.g., via a text editor or some other tool affecting the {@link Resource resource} in its serialized form).
	 * 
	 * @param editingDomain
	 *            The {@link EditingDomain editing domain} the {@link Resource resource} behind given {@link URI} is in.
	 * @param uri
	 *            The {@link URI} of the {@link Resource resource} to be investigated.
	 * @return <code>true</code> if given {@link Resource resource} is present in given {@link EditingDomain editing
	 *         domain} and has just been saved, or <code>false</code> otherwise.
	 * @see #setSaved(EditingDomain, Resource)
	 * @see #setSaved(EditingDomain, Collection)
	 */
	public static boolean isSaved(EditingDomain editingDomain, URI uri) {
		IResourceSaveIndicator indicator = getResourceSaveIndicator(editingDomain);
		if (indicator != null) {
			return indicator.isSaved(uri);
		}
		return false;
	}

	/**
	 * Clears dirty state of given {@link Resource resource} in specified {@link EditingDomain editing domain} and
	 * remembers it as having just been saved. This method needs to be called by all clients which perform a save
	 * operation of some {@link Resource resource} right after the save operation has been completed. It enables clients
	 * to determine if subsequently raised {@link IResourceChangeEvent resource change event}s are just a consequence of
	 * the preceding save operation or if the underlying {@link IFile file}'s content has been changed otherwise (e.g.,
	 * via a text editor or some other tool affecting the {@link Resource resource} in its serialized form).
	 * 
	 * @param editingDomain
	 *            The {@link EditingDomain editing domain} the {@link Resource resource} in question is in.
	 * @param resource
	 *            The {@link Resource resource} to be handled.
	 * @see #isDirty(EditingDomain, Resource)
	 * @see #isSaved(EditingDomain, URI)
	 * @see #setSaved(EditingDomain, Collection)
	 */
	public static void setSaved(EditingDomain editingDomain, Resource resource) {
		IResourceSaveIndicator indicator = getResourceSaveIndicator(editingDomain);
		if (indicator != null) {
			indicator.setSaved(resource);
		}
	}

	/**
	 * Clears dirty state of given collection of {@link Resource resource}s in specified {@link EditingDomain editing
	 * domain} and remembers them as having just been saved. This method needs to be called by all clients which perform
	 * a save operation of some collection of {@link Resource resource}s right after the save operation has been
	 * completed. Clients can then call {@link #isSaved(EditingDomain, URI)} to determine if subsequently raised
	 * {@link IResourceChangeEvent resource change event}s are just a consequence of the preceding save operation or if
	 * the underlying {@link IFile file}s' content has been changed otherwise (e.g., via a text editor or some other
	 * tool affecting the {@link Resource resource} in its serialized form).
	 * 
	 * @param editingDomain
	 *            The {@link EditingDomain editing domain} the {@link Resource resource}s in question are in.
	 * @param resource
	 *            The collection of {@link Resource resource}s to be handled.
	 * @see #isDirty(EditingDomain, Resource)
	 * @see #isSaved(EditingDomain, URI)
	 * @see #setSaved(EditingDomain, Resource)
	 */
	public static void setSaved(EditingDomain editingDomain, Collection<Resource> resources) {
		IResourceSaveIndicator indicator = getResourceSaveIndicator(editingDomain);
		if (indicator != null) {
			indicator.setSaved(resources);
		}
	}

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
	public static void setSaved(IModelDescriptor modelDescriptor) {
		IModelSaveIndicator indicator = getModelSaveIndicator(modelDescriptor);
		if (indicator != null) {
			indicator.setSaved(modelDescriptor);
		}
	}
}

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
package org.eclipse.sphinx.emf.model;

/**
 * The listener is in charge of handling actions to perform when a model descriptor is added or removed.
 * 
 * @see org.eclipse.sphinx.emf.model.IModelDescriptor
 */
public interface IModelDescriptorChangeListener {

	/**
	 * Notifies clients that a {@link IModelDescriptor model descriptor} has been added.
	 * 
	 * @param modelDescriptor
	 *            The {@link IModelDescriptor model descriptor} that has been created.
	 */
	void handleModelAdded(IModelDescriptor modelDescriptor);

	/**
	 * Notifies clients that an {@link IModelDescriptor model descriptor} has been removed.
	 * 
	 * @param modelDescriptor
	 *            The {@link IModelDescriptor model descriptor} removed.
	 */
	void handleModelRemoved(IModelDescriptor modelDescriptor);
}

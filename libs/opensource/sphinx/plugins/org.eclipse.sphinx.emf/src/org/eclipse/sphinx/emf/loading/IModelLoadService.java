/**
 * <copyright>
 *
 * Copyright (c) 2013 itemis and others.
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
package org.eclipse.sphinx.emf.loading;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.sphinx.emf.model.IModelDescriptor;

public interface IModelLoadService {

	/**
	 * Loads all resources owned by the provided {@link IModelDescriptor model descriptor} (i.e all the persisted
	 * resources owned by the model)
	 * 
	 * @param modelDescriptor
	 *            The {@link IModelDescriptor model Descriptor} describing the model to load.
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadModel(IModelDescriptor modelDescriptor, boolean async, IProgressMonitor monitor);

	/**
	 * Loads in memory all persisted resources owned by the model described by the {@link IModelDescriptor model
	 * Descriptor}s provided in argument.
	 * 
	 * @param modelDescriptors
	 *            {@link IModelDescriptor model Descriptor}s describing the models to load.
	 * @param async
	 *            <code>true</code> if this operation is required to be run asynchronously, or <code>false</code> if
	 *            synchronous execution is desired.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	void loadModels(Collection<IModelDescriptor> modelDescriptors, boolean async, IProgressMonitor monitor);

}

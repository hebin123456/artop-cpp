/**
 * <copyright>
 *
 * Copyright (c) 2021 Siemens and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     Siemens - [574930] Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.loading.operations;

import java.util.Collection;
import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;

/**
 * Interface for ModelLoadManager ModelUnloadOpertaion.
 */
public interface IModelUnloadOperation extends ILoadOperation {

	/**
	 * @return the resources to unload and their corresponding EditingDomain. It should be set in the constructor of the
	 *         operation.
	 */
	Map<TransactionalEditingDomain, Collection<Resource>> getResourcesToUnload();

	/**
	 * Memory optimization option for unloading the resource. This is only available if the resource is an XMLResource.
	 * It should be set in the constructor of the operation.
	 *
	 * @return true if the optimization is activated, otherwise false.
	 */
	boolean isMemoryOptimized();

}
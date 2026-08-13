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

import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.model.IModelDescriptor;

/**
 * Interface for ModelLoadManager ModelLoadOpertaion.
 */
public interface IModelLoadOperation extends ILoadOperation {

	/**
	 * Retrieves the ModelDescriptor used in the operation. It should be set in the constructor of the operation. The
	 * {@link IMetaModelDescriptor} of the ModelDescriptor shall be used in the operation by the implementation.
	 *
	 * @return the ModelDescriptor.
	 */
	IModelDescriptor getModelDescriptor();

	/**
	 * Flag to also include files from the referenced scopes in the operation. It should be set in the constructor of
	 * the operation.
	 *
	 * @return true if the referenced scopes are also included, otherwise false.
	 */
	boolean isIncludeReferencedScopes();

}
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
package org.eclipse.sphinx.emf.metamodelgen.operations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.sphinx.platform.operations.IWorkspaceOperation;

public interface IGenerateFromEcoreOperation extends IWorkspaceOperation {

	void generate(EPackage ecoreModel, IProgressMonitor monitor) throws CoreException, OperationCanceledException;

}
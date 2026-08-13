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
package org.eclipse.sphinx.examples.hummingbird.metamodelgen.ui.handlers;

import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.resources.IFile;
import org.eclipse.sphinx.emf.metamodelgen.ui.handlers.AbstractGenerateFromEcoreHandler;
import org.eclipse.sphinx.examples.hummingbird.metamodelgen.operations.GenerateXSDOperation;
import org.eclipse.sphinx.platform.operations.IWorkspaceOperation;

/**
 * A {@link IHandler2 command handler} for generating an XSD schema from an Ecore model with XML Persistence Mapping
 * annotations.
 */
public class GenerateXSDHandler extends AbstractGenerateFromEcoreHandler {

	/*
	 * @see
	 * org.eclipse.sphinx.emf.metamodelgen.ui.handlers.AbstractGenerateFromEcoreHandler#createGenerateFromEcoreOperation
	 * (org.eclipse.core.resources.IFile)
	 */
	@Override
	protected IWorkspaceOperation createGenerateFromEcoreOperation(IFile ecoreFile) {
		return new GenerateXSDOperation(ecoreFile);
	}
}

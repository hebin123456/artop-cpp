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
package org.eclipse.sphinx.examples.hummingbird.metamodelgen.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.sphinx.emf.metamodelgen.ui.actions.AbstractGenerateFromEcoreAction;
import org.eclipse.sphinx.examples.hummingbird.metamodelgen.internal.messages.Messages;
import org.eclipse.sphinx.examples.hummingbird.metamodelgen.operations.GenerateXMLPersistenceMappingsOperation;
import org.eclipse.sphinx.platform.operations.IWorkspaceOperation;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * An {@link BaseSelectionListenerAction action} for generating an Ecore model with XML Persistence Mapping annotations
 * from a non-annotated source Ecore model.
 */
public class GenerateXMLPersistenceMappingsAction extends AbstractGenerateFromEcoreAction {

	public GenerateXMLPersistenceMappingsAction() {
		super(Messages.operation_generateXMLPersistenceMappings_label);
	}

	public GenerateXMLPersistenceMappingsAction(String text) {
		super(text);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.metamodelgen.ui.actions.AbstractGenerateFromEcoreAction#createGenerateFromEcoreOperation
	 * (org.eclipse.core.resources.IFile)
	 */
	@Override
	protected IWorkspaceOperation createGenerateFromEcoreOperation(IFile ecoreFile) {
		return new GenerateXMLPersistenceMappingsOperation(ecoreFile);
	}
}

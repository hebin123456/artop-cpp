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
package org.eclipse.sphinx.examples.hummingbird.metamodelgen.operations;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.sphinx.emf.metamodelgen.operations.AbstractGenerateFromEcoreOperation;
import org.eclipse.sphinx.emf.metamodelgen.operations.IGenerateFromEcoreOperation;
import org.eclipse.sphinx.examples.hummingbird.metamodelgen.internal.messages.Messages;

/**
 * An {@link IGenerateFromEcoreOperation operation} for generating an Ecore model with XML Persistence Mapping
 * annotations and an XSD schema from a non-annotated source Ecore model.
 */
public class GenerateXMLPersistenceMappingsAndXSDOperation extends AbstractGenerateFromEcoreOperation {

	public GenerateXMLPersistenceMappingsAndXSDOperation(IFile ecoreFile) {
		super(Messages.operation_generateXMLPersistenceMappingsAndXSD_label, ecoreFile);
	}

	public GenerateXMLPersistenceMappingsAndXSDOperation(String label, IFile ecoreFile) {
		super(label, ecoreFile);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.metamodelgen.operations.AbstractGenerateFromEcoreOperation#generate(org.eclipse.emf.ecore
	 * .EPackage, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void generate(EPackage ecoreModel, IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		GenerateXMLPersistenceMappingsOperation generateMappedEcoreOperation = new GenerateXMLPersistenceMappingsOperation(ecoreFile);
		generateMappedEcoreOperation.generate(ecoreModel, progress.newChild(30));

		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		GenerateXSDOperation generateXSDOperation = new GenerateXSDOperation(ecoreFile);
		generateXSDOperation.generate(generateMappedEcoreOperation.getMappedEcoreModel(), progress.newChild(70));
	}
}

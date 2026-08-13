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

import java.util.regex.Matcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.sphinx.emf.metamodelgen.operations.AbstractGenerateFromEcoreOperation;
import org.eclipse.sphinx.emf.metamodelgen.operations.IGenerateFromEcoreOperation;
import org.eclipse.sphinx.emf.serialization.generators.xsd.Ecore2XSDGenerator;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.examples.hummingbird.metamodelgen.internal.messages.Messages;
import org.eclipse.xsd.XSDPackage;

/**
 * A {@link IGenerateFromEcoreOperation operation} for generating an XSD schema from an Ecore model with XML Persistence
 * Mapping annotations.
 */
public class GenerateXSDOperation extends AbstractGenerateFromEcoreOperation {

	public GenerateXSDOperation(IFile ecoreFile) {
		super(Messages.operation_generateXSD_label, ecoreFile);
	}

	public GenerateXSDOperation(String label, IFile ecoreFile) {
		super(label, ecoreFile);
	}

	public URI getXSDFileURI() {
		// Compute XSD schema file base name by reusing base name of provided Ecore file and removing generated
		// "-mapped" postfix
		String xsdFileBaseName;
		Matcher matcher = GenerateXMLPersistenceMappingsOperation.ORIGINAL_ECORE_FILE_BASE_NAME_PATTERN.matcher(ecoreFile.getName());
		if (matcher.matches()) {
			xsdFileBaseName = matcher.group(GenerateXMLPersistenceMappingsOperation.ORIGINAL_ECORE_FILE_BASE_NAME_GROUP_IDX);
		} else {
			xsdFileBaseName = ecoreFile.getFullPath().removeFileExtension().lastSegment();
		}

		// Compute XSD schema path by using same project/folder as provide Ecore file, previously computed XSD schema
		// file base name and a ".xsd" file extension
		IPath xsdPath = ecoreFile.getParent().getFullPath().append(xsdFileBaseName).addFileExtension(XSDPackage.eNAME);
		return EcorePlatformUtil.createURI(xsdPath);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.metamodelgen.operations.AbstractGenerateFromEcoreOperation#generate(org.eclipse.emf.ecore
	 * .EPackage, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void generate(EPackage ecoreModel, IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		// TODO Move this into Ecore2XSDGenerator
		monitor.setTaskName("Generating XSD schema");

		// Run Ecore2XSDGenerator to generate XSD schema for selected Ecore model with XML Persistence Mapping
		// annotations
		Ecore2XSDGenerator ecore2XSDGenerator = createEcore2XSDGenerator(ecoreModel);
		ecore2XSDGenerator.run(monitor);
	}

	protected Ecore2XSDGenerator createEcore2XSDGenerator(EPackage ecoreModel) {
		return new Ecore2XSDGenerator(getXSDFileURI(), ecoreModel);
	}
}

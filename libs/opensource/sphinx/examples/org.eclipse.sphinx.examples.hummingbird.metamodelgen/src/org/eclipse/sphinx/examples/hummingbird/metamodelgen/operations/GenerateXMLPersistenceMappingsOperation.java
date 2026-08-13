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

import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.sphinx.emf.metamodelgen.operations.AbstractGenerateFromEcoreOperation;
import org.eclipse.sphinx.emf.metamodelgen.operations.IGenerateFromEcoreOperation;
import org.eclipse.sphinx.emf.serialization.generators.persistencemapping.XMLPersistenceMappingGenerator;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.examples.hummingbird.metamodelgen.internal.messages.Messages;

/**
 * A {@link IGenerateFromEcoreOperation operation} for generating an Ecore model with XML Persistence Mapping
 * annotations from a non-annotated source Ecore model.
 */
public class GenerateXMLPersistenceMappingsOperation extends AbstractGenerateFromEcoreOperation {

	public static final String MAPPED_ECORE_FILE_NAME_POSTFIX = "-mapped"; //$NON-NLS-1$
	public static final Pattern ORIGINAL_ECORE_FILE_BASE_NAME_PATTERN = Pattern
			.compile("(.*)" + MAPPED_ECORE_FILE_NAME_POSTFIX + "(\\." + EcorePackage.eNAME + ")?$"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	public static final int ORIGINAL_ECORE_FILE_BASE_NAME_GROUP_IDX = 1;

	private EPackage mappedEcoreModel = null;

	public GenerateXMLPersistenceMappingsOperation(IFile ecoreFile) {
		super(Messages.operation_generateXMLPersistenceMappings_label, ecoreFile);
	}

	public GenerateXMLPersistenceMappingsOperation(String label, IFile ecoreFile) {
		super(label, ecoreFile);
	}

	public URI getMappedEcoreFileURI() {
		// Compute mapped Ecore file base name by reusing base name of provided Ecore file and appending a "-mapped"
		// postfix
		String mapppedEcoreFileBaseName = ecoreFile.getFullPath().removeFileExtension().lastSegment() + MAPPED_ECORE_FILE_NAME_POSTFIX;

		// Compute mapped Ecore path by using same project/folder as provided Ecore file, previously computed mapped
		// Ecore file base name and a ".ecore" file extension
		IPath mappedEcorePath = ecoreFile.getParent().getFullPath().append(mapppedEcoreFileBaseName).addFileExtension(EcorePackage.eNAME);
		return EcorePlatformUtil.createURI(mappedEcorePath);
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

		// Run XMLPersistenceMappingGenerator to generate Ecore model with XML Persistence Mapping annotations from
		// selected non-annotated source Ecore model
		// TODO Imporove progress monitor inside XMLPersistenceMappingGenerator
		XMLPersistenceMappingGenerator xmlPersistenceMappingGenerator = createXMLPersistenceMappingGenerator(ecoreModel);
		mappedEcoreModel = (EPackage) xmlPersistenceMappingGenerator.execute(progress.newChild(10));

		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		saveEcoreModel(getMappedEcoreFileURI(), mappedEcoreModel);
		progress.worked(90);
	}

	protected XMLPersistenceMappingGenerator createXMLPersistenceMappingGenerator(EPackage ecoreModel) {
		return new XMLPersistenceMappingGenerator(ecoreModel);
	}

	public EPackage getMappedEcoreModel() {
		return mappedEcoreModel;
	}
}

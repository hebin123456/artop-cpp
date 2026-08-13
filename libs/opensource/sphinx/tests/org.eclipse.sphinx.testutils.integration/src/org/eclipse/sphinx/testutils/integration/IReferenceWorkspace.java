/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.testutils.integration;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;

public interface IReferenceWorkspace {

	IFile getReferenceFile(String projectName, String fileName);

	Set<IFile> getReferenceFiles(String projectName);

	List<String> getReferenceFileNames(String projectName);

	Set<IFile> getReferenceFiles(IMetaModelDescriptor metaModelDescriptor);

	Set<IFile> getReferenceFiles(String projectName, IMetaModelDescriptor metaModelDescriptor);

	List<String> getReferenceFileNames(String projectName, IMetaModelDescriptor metamodeldescriptor);

	Set<IFile> getAllReferenceFiles();

	IProject getReferenceProject(String projectName);

	int getInitialReferenceEditingDomainCount();

	int getInitialResourcesInReferenceEditingDomainCount(IMetaModelDescriptor metaModeldescriptor);

	int getInitialResourcesInAllReferenceEditingDomainCount();
}

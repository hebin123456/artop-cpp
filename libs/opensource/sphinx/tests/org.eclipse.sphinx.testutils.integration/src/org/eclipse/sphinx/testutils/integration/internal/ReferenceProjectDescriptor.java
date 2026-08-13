/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.testutils.integration.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

public class ReferenceProjectDescriptor {

	private IProject project;
	private Map<IMetaModelDescriptor, Set<IFile>> referenceFiles = new HashMap<IMetaModelDescriptor, Set<IFile>>();
	private Set<ReferenceModelDescriptor> referenceModelDescriptors = new HashSet<ReferenceModelDescriptor>();

	public ReferenceProjectDescriptor(String projectName) {
		Assert.isNotNull(projectName);

		project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}

	public IProject getProject() {
		return project;
	}

	public String getProjectName() {
		return project.getName();
	}

	public void addFile(String relativeFilePath, IMetaModelDescriptor metaModelDescriptor) {
		addFile(project.getFullPath().append(relativeFilePath), metaModelDescriptor);
	}

	private void addFile(IPath path, IMetaModelDescriptor metaModelDescriptor) {
		Assert.isNotNull(path);

		if (metaModelDescriptor == null) {
			metaModelDescriptor = MetaModelDescriptorRegistry.NO_MM;
		}

		Set<IFile> modelFiles = referenceFiles.get(metaModelDescriptor);
		if (modelFiles == null) {
			modelFiles = new HashSet<IFile>();
			referenceFiles.put(metaModelDescriptor, modelFiles);
		}

		for (IFile modelFile : modelFiles) {
			if (modelFile.getFullPath().equals(path)) {
				return;
			}
		}
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		modelFiles.add(file);
	}

	public Set<IFile> getAllFiles() {
		Set<IFile> allFiles = new HashSet<IFile>();
		Collection<Set<IFile>> metaModelFiles = referenceFiles.values();
		for (Set<IFile> modelFiles : metaModelFiles) {
			allFiles.addAll(modelFiles);
		}
		return allFiles;
	}

	public List<String> getAllFileNames() {
		List<String> filesNames = new ArrayList<String>();
		Set<IMetaModelDescriptor> metaModelDescriptors = referenceFiles.keySet();

		for (IMetaModelDescriptor metaModelDescriptor : metaModelDescriptors) {
			filesNames.addAll(getFileNames(metaModelDescriptor));

		}
		return filesNames;
	}

	public Map<IMetaModelDescriptor, Set<IFile>> getFiles() {
		return referenceFiles;
	}

	public Set<IFile> getFiles(IMetaModelDescriptor metaModelDescriptor) {
		if (metaModelDescriptor == null) {
			metaModelDescriptor = MetaModelDescriptorRegistry.NO_MM;
		}
		Set<IFile> modelFiles = referenceFiles.get(metaModelDescriptor);
		if (modelFiles != null) {
			return modelFiles;
		}
		return Collections.emptySet();
	}

	public List<String> getFileNames(IMetaModelDescriptor metaModelDescriptor) {
		List<String> filesNames = new ArrayList<String>();
		Set<IFile> modelFiles = referenceFiles.get(metaModelDescriptor);
		if (modelFiles != null) {
			for (IFile file : modelFiles) {
				filesNames.add(file.getName());
			}
		}
		return filesNames;

	}

	public IFile getFile(String fileName) {
		if (fileName != null) {
			Collection<Set<IFile>> metaModelFiles = referenceFiles.values();
			for (Set<IFile> modelFiles : metaModelFiles) {
				for (IFile modelFile : modelFiles) {
					if (modelFile.getName().equals(fileName)) {
						return modelFile;
					}
				}
			}
		}
		return null;
	}

	public IFile getFile(String fileName, IMetaModelDescriptor metaModelDescriptor) {
		if (fileName != null) {
			if (metaModelDescriptor == null) {
				metaModelDescriptor = MetaModelDescriptorRegistry.NO_MM;
			}
			Set<IFile> modelFiles = referenceFiles.get(metaModelDescriptor);
			if (modelFiles != null) {
				for (IFile modelFile : modelFiles) {
					if (modelFile.getName().equals(fileName)) {
						return modelFile;
					}
				}
			}
		}
		return null;
	}

	public void addReferenceModelDescriptor(ReferenceModelDescriptor referenceModelDescriptor) {
		if (!referenceModelDescriptors.contains(referenceModelDescriptor)) {
			referenceModelDescriptors.add(referenceModelDescriptor);
		}
	}

	public ReferenceModelDescriptor getReferenceModelDescriptor(ReferenceModelDescriptor referenceModelDescriptor) {
		if (referenceModelDescriptors.contains(referenceModelDescriptor)) {
			return referenceModelDescriptor;
		}
		return null;
	}

	public Set<ReferenceModelDescriptor> getReferenceModelDescriptors() {
		return referenceModelDescriptors;
	}

	public Set<ReferenceModelDescriptor> getAccessibleReferenceModelDescriptors() {
		Set<ReferenceModelDescriptor> accessibleReferenceModelDescriptors = new HashSet<ReferenceModelDescriptor>();
		for (ReferenceModelDescriptor referenceModelDescriptor : referenceModelDescriptors) {
			boolean empty = true;
			for (IFile file : ExtendedPlatform.getAllFiles(project, true)) {
				TransactionalEditingDomain currentEditingDomain = WorkspaceEditingDomainUtil.getCurrentEditingDomain(file);
				if (currentEditingDomain != null && referenceModelDescriptor.getEditingDomainName().equals(currentEditingDomain.getID())) {
					empty = false;
					break;
				}
			}
			if (!empty) {
				accessibleReferenceModelDescriptors.add(referenceModelDescriptor);
			}
		}
		return accessibleReferenceModelDescriptors;
	}

	@Override
	public String toString() {
		return project.getName();
	}
}

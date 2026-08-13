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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.workspace.domain.mapping.AbstractWorkspaceEditingDomainMapping;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.testutils.integration.internal.IInternalReferenceWorkspace;
import org.eclipse.sphinx.testutils.integration.internal.ReferenceEditingDomainDescriptor;
import org.eclipse.sphinx.testutils.integration.internal.ReferenceModelDescriptor;
import org.eclipse.sphinx.testutils.integration.internal.ReferenceProjectDescriptor;

/**
 *
 */
public abstract class AbstractReferenceWorkspace implements IInternalReferenceWorkspace {

	private Map<IMetaModelDescriptor, ReferenceEditingDomainDescriptor> referenceEditingDomainsDescriptors = new HashMap<IMetaModelDescriptor, ReferenceEditingDomainDescriptor>();
	private Set<ReferenceProjectDescriptor> referenceProjectDescriptors = new HashSet<ReferenceProjectDescriptor>();

	public AbstractReferenceWorkspace(Set<String> referenceProjectSubset) {
		initContentDescriptors(referenceProjectSubset);
		initContentAccessors();
	}

	protected final void initContentDescriptors(Set<String> referenceProjectSubset) {
		initReferenceProjectDescriptors(referenceProjectSubset);
		initReferenceFileDescriptors();
	}

	protected void initContentAccessors() {
		// Do nothing by default
	}

	private void initReferenceProjectDescriptors(Set<String> referenceProjectSubset) {
		Assert.isNotNull(referenceProjectSubset);

		if (referenceProjectSubset.isEmpty()) {
			referenceProjectSubset.addAll(Arrays.asList(getReferenceProjectsNames()));
		}
		for (String referenceProjectName : referenceProjectSubset) {
			addReferenceProjectDescriptor(referenceProjectName);
		}
	}

	private void addReferenceProjectDescriptor(String projectName) {
		if (referenceProjectDescriptors == null) {
			referenceProjectDescriptors = new HashSet<ReferenceProjectDescriptor>();
		}
		for (ReferenceProjectDescriptor projectDescriptor : referenceProjectDescriptors) {
			if (projectDescriptor.getProjectName().equals(projectName)) {
				return;
			}
		}
		referenceProjectDescriptors.add(new ReferenceProjectDescriptor(projectName));
	}

	protected abstract void initReferenceFileDescriptors();

	protected void addFileDescriptors(String projectName, String[] filesPath) {
		if (projectName != null && filesPath != null) {
			for (String relativeFilePath : filesPath) {
				addFileDescriptor(projectName, relativeFilePath);
			}
		}
	}

	protected void addFileDescriptor(String projectName, String relativeFilePath) {
		if (projectName != null && relativeFilePath != null) {
			ReferenceProjectDescriptor projectDescriptor = getReferenceProjectDescriptor(projectName);
			if (projectDescriptor != null) {
				projectDescriptor.addFile(relativeFilePath, null);
			}
		}
	}

	protected void addFileDescriptors(String projectName, String[] filesPath, IMetaModelDescriptor metaModeldescriptor) {
		if (projectName != null && filesPath != null) {
			for (String relativeFilePath : filesPath) {
				addFileDescriptor(projectName, relativeFilePath, metaModeldescriptor);
			}
		}
	}

	protected void addFileDescriptor(String projectName, String relativeFilePath, IMetaModelDescriptor metaModelDescriptor) {
		ReferenceEditingDomainDescriptor referenceEditingDomainDescriptor = getReferenceEditingDomainDescriptor(metaModelDescriptor);
		if (referenceEditingDomainDescriptor == null) {
			String editingDomainId = AbstractWorkspaceEditingDomainMapping.getDefaultEditingDomainId(Collections.singleton(metaModelDescriptor));
			addEditingDomainDescriptor(metaModelDescriptor, editingDomainId);
			referenceEditingDomainDescriptor = getReferenceEditingDomainDescriptor(metaModelDescriptor);
		}

		ReferenceProjectDescriptor referenceProjectDescriptor = getReferenceProjectDescriptor(projectName);
		if (referenceProjectDescriptor != null) {
			referenceProjectDescriptor.addFile(relativeFilePath, metaModelDescriptor);
			IPath referenceProjectPath = referenceProjectDescriptor.getProject().getFullPath();
			referenceEditingDomainDescriptor
					.addResourceURI(URI.createPlatformResourceURI(referenceProjectPath.append(relativeFilePath).toString(), true));
			addReferenceModelDescriptor(metaModelDescriptor, referenceEditingDomainDescriptor, referenceProjectDescriptor);
		}
	}

	private void addEditingDomainDescriptor(IMetaModelDescriptor metaModelDescriptor, String editingDomainName) {
		if (referenceEditingDomainsDescriptors != null) {
			if (getReferenceEditingDomainDescriptor(metaModelDescriptor) == null) {
				ReferenceEditingDomainDescriptor referenceEditingDomainsDescriptor = referenceEditingDomainsDescriptors.get(metaModelDescriptor);
				if (referenceEditingDomainsDescriptor == null) {
					referenceEditingDomainsDescriptors.put(metaModelDescriptor, new ReferenceEditingDomainDescriptor(editingDomainName));
				}
			}
		}
	}

	private void addReferenceModelDescriptor(IMetaModelDescriptor metaModelDescriptor,
			ReferenceEditingDomainDescriptor referenceEditingDomainDescriptor, ReferenceProjectDescriptor referenceProjectDescriptor) {
		Assert.isNotNull(metaModelDescriptor);
		Assert.isNotNull(referenceEditingDomainDescriptor);
		Assert.isNotNull(referenceProjectDescriptor);

		ReferenceModelDescriptor referenceModelDescriptor = new ReferenceModelDescriptor(metaModelDescriptor, referenceEditingDomainDescriptor.name,
				referenceProjectDescriptor.getProject());
		referenceProjectDescriptor.addReferenceModelDescriptor(referenceModelDescriptor);

		for (IProject referencingProject : ExtendedPlatform.getAllReferencingProjects(referenceProjectDescriptor.getProject())) {
			ReferenceProjectDescriptor referencingReferenceProjectDescriptor = getReferenceProjectDescriptor(referencingProject.getName());
			if (referencingReferenceProjectDescriptor != null) {
				ReferenceModelDescriptor forwardedReferenceModelDescriptor = new ReferenceModelDescriptor(metaModelDescriptor,
						referenceEditingDomainDescriptor.name, referencingProject);
				referencingReferenceProjectDescriptor.addReferenceModelDescriptor(forwardedReferenceModelDescriptor);
			}
		}
	}

	@Override
	public void addResourceSetProblemListener(ResourceProblemListener resourceProblemListener) {
		for (IMetaModelDescriptor metaModelDescriptor : referenceEditingDomainsDescriptors.keySet()) {
			TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(ResourcesPlugin.getWorkspace().getRoot(),
					metaModelDescriptor);
			if (editingDomain != null) {
				editingDomain.addResourceSetListener(resourceProblemListener);
			}
		}
	}

	@Override
	public void removeResourceSetProblemListener(ResourceProblemListener resourceProblemListener) {
		for (IMetaModelDescriptor metaModelDescriptor : referenceEditingDomainsDescriptors.keySet()) {
			TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(ResourcesPlugin.getWorkspace().getRoot(),
					metaModelDescriptor);
			if (editingDomain != null) {
				editingDomain.removeResourceSetListener(resourceProblemListener);
			}
		}
	}

	@Override
	public void addReferenceWorkspaceChangeListener(ReferenceWorkspaceChangeListener referenceWorkspaceChangeListener) {
		ServiceCaller.callOnce(getClass(), IWorkspace.class, ws -> {
			ws.addResourceChangeListener(referenceWorkspaceChangeListener);
		});
	}

	@Override
	public void removeReferenceWorkspaceChangeListener(ReferenceWorkspaceChangeListener referenceWorkspaceChangeListener) {
		ServiceCaller.callOnce(getClass(), IWorkspace.class, ws -> {
			ws.removeResourceChangeListener(referenceWorkspaceChangeListener);
		});
	}

	@Override
	public ReferenceEditingDomainDescriptor getReferenceEditingDomainDescriptor(IMetaModelDescriptor metaModeldescriptor) {
		if (referenceEditingDomainsDescriptors != null && metaModeldescriptor != null) {
			return referenceEditingDomainsDescriptors.get(metaModeldescriptor);
		}
		return null;
	}

	@Override
	public Map<IMetaModelDescriptor, ReferenceEditingDomainDescriptor> getReferenceEditingDomainDescritpors() {
		return referenceEditingDomainsDescriptors;
	}

	/**
	 * return the first editing domain descriptor of the list corresponding to the given MetaModelDescriptor
	 */

	@Override
	public int getInitialReferenceEditingDomainCount() {
		if (referenceEditingDomainsDescriptors != null) {
			return referenceEditingDomainsDescriptors.size();
		}
		return 0;
	}

	@Override
	public int getInitialResourcesInReferenceEditingDomainCount(IMetaModelDescriptor metaModeldescriptor) {
		ReferenceEditingDomainDescriptor referenceEditingDomainDescriptor = getReferenceEditingDomainDescriptor(metaModeldescriptor);
		if (referenceEditingDomainDescriptor != null) {
			return referenceEditingDomainDescriptor.getResourceURIs().size();
		}
		return 0;
	}

	@Override
	public int getInitialResourcesInAllReferenceEditingDomainCount() {
		int count = 0;
		if (referenceEditingDomainsDescriptors != null) {
			for (IMetaModelDescriptor metaModelDescriptor : referenceEditingDomainsDescriptors.keySet()) {
				count += getInitialResourcesInReferenceEditingDomainCount(metaModelDescriptor);
			}
		}
		return count;
	}

	@Override
	public Set<IFile> getAllReferenceFiles() {
		Assert.isNotNull(referenceProjectDescriptors);

		Set<IFile> results = new HashSet<IFile>();
		for (ReferenceProjectDescriptor referenceProjectDescriptor : referenceProjectDescriptors) {
			if (referenceProjectDescriptor != null) {
				results.addAll(referenceProjectDescriptor.getAllFiles());
			}
		}
		return results;
	}

	@Override
	public Set<IFile> getReferenceFiles(IMetaModelDescriptor metaModelDescriptor) {
		Assert.isNotNull(referenceProjectDescriptors);

		Set<IFile> results = new HashSet<IFile>();
		for (ReferenceProjectDescriptor referenceProjectDescriptor : referenceProjectDescriptors) {
			if (referenceProjectDescriptor != null) {
				results.addAll(referenceProjectDescriptor.getFiles(metaModelDescriptor));
			}
		}
		return results;
	}

	@Override
	public Set<String> getReferenceFileNames(IMetaModelDescriptor metaModelDescriptor) {
		Assert.isNotNull(referenceProjectDescriptors);

		Set<String> results = new HashSet<String>();
		for (ReferenceProjectDescriptor referenceProjectDescriptor : referenceProjectDescriptors) {
			if (referenceProjectDescriptor != null) {
				for (IFile file : referenceProjectDescriptor.getFiles(metaModelDescriptor)) {
					results.add(file.getName());
				}
			}
		}
		return results;
	}

	@Override
	public Set<IFile> getReferenceFiles(String projectName) {
		ReferenceProjectDescriptor referenceProjectdescriptor = getReferenceProjectDescriptor(projectName);
		if (referenceProjectdescriptor != null) {
			return referenceProjectdescriptor.getAllFiles();
		}
		return new HashSet<IFile>();
	}

	@Override
	public Set<IFile> getReferenceFiles(String projectName, IMetaModelDescriptor metaModelDescriptor) {
		ReferenceProjectDescriptor referenceProjectdescriptor = getReferenceProjectDescriptor(projectName);
		if (referenceProjectdescriptor != null) {
			return referenceProjectdescriptor.getFiles(metaModelDescriptor);
		}
		return new HashSet<IFile>();
	}

	// TODO reduce API to only use IPath
	@Override
	public IFile getReferenceFile(String projectName, String fileName) {
		ReferenceProjectDescriptor referenceProjectDescriptor = getReferenceProjectDescriptor(projectName);
		if (referenceProjectDescriptor != null) {
			return referenceProjectDescriptor.getFile(fileName);
		}
		return null;
	}

	public IFile getReferenceFile(IPath filePath) {
		return ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
	}

	protected abstract String[] getReferenceProjectsNames();

	@Override
	public IProject getReferenceProject(String projectName) {
		ReferenceProjectDescriptor referenceProjectdescriptor = getReferenceProjectDescriptor(projectName);
		if (referenceProjectdescriptor != null) {
			return referenceProjectdescriptor.getProject();
		}
		return null;
	}

	@Override
	public List<String> getReferenceFileNames(String projectName, IMetaModelDescriptor metaModelDescriptor) {
		ReferenceProjectDescriptor referenceProjectDescriptor = getReferenceProjectDescriptor(projectName);
		if (referenceProjectDescriptor != null) {
			return referenceProjectDescriptor.getFileNames(metaModelDescriptor);
		}
		return new ArrayList<String>();
	}

	@Override
	public List<String> getReferenceFileNames(String projectName) {
		ReferenceProjectDescriptor referenceProjectDescriptor = getReferenceProjectDescriptor(projectName);
		if (referenceProjectDescriptor != null) {
			return referenceProjectDescriptor.getAllFileNames();
		}
		return new ArrayList<String>();
	}

	@Override
	public ReferenceProjectDescriptor getReferenceProjectDescriptor(String projectName) {
		Assert.isNotNull(referenceProjectDescriptors);

		for (ReferenceProjectDescriptor projectDescriptor : referenceProjectDescriptors) {
			if (projectDescriptor.getProjectName().equals(projectName)) {
				return projectDescriptor;
			}
		}
		return null;
	}

	public Map<String, IProject> getReferenceProjectAccessors() {
		Assert.isNotNull(referenceProjectDescriptors);

		Map<String, IProject> projects = new HashMap<String, IProject>();
		for (ReferenceProjectDescriptor projcetDescriptor : referenceProjectDescriptors) {
			projects.put(projcetDescriptor.getProjectName(), projcetDescriptor.getProject());
		}
		return projects;
	}

	@Override
	public Set<ReferenceProjectDescriptor> getReferenceProjectDescriptors() {
		return referenceProjectDescriptors;
	}

	/**
	 * Create an empty eclipse project.
	 *
	 * @param project
	 * @throws CoreException
	 */
	protected final IProject createSimpleProject(final String projectName) throws CoreException {
		final IProject[] project = new IProject[1];
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				project[0] = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (!project[0].exists()) {
					project[0].create(null);
					project[0].open(null);
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, null);
		return project[0];
	}
}

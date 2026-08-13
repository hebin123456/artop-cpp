/**
 * <copyright>
 *
 * Copyright (c) 2011-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [406564] BasicWorkspaceResourceLoader#getResource should not delegate to super
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.mwe.resources;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.mwe.core.resources.AbstractResourceLoader;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.mwe.IXtendXpandConstants;

public class BasicWorkspaceResourceLoader extends AbstractResourceLoader implements IWorkspaceResourceLoader {

	protected static final String DEFAULT_TEMPLATE_FOLDER_NAME = "template"; //$NON-NLS-1$

	protected static final String DEFAULT_EXTENSION_FOLDER_NAME = "extension"; //$NON-NLS-1$

	protected static final String DEFAULT_CHECK_FOLDER_NAME = "check"; //$NON-NLS-1$

	protected IProject contextProject = null;
	protected IModelDescriptor contextModel = null;

	protected Set<IProject> projectsInScope = new HashSet<IProject>();
	protected ClassLoader workspaceClassLoader = null;

	protected boolean searchArchives = true;

	@Override
	public IProject getContextProject() {
		return contextProject;
	}

	@Override
	public void setContextProject(IProject contextProject) {
		resetContext();
		this.contextProject = contextProject;
	}

	@Override
	public IModelDescriptor getContextModel() {
		return contextModel;
	}

	@Override
	public void setContextModel(IModelDescriptor contextModel) {
		resetContext();
		this.contextModel = contextModel;
	}

	@Override
	public void setSearchArchives(boolean searchArchives) {
		if (this.searchArchives != searchArchives) {
			// Set workspaceClassLoader to null in order to force it to be recreated according to new value of
			// searchArchives
			workspaceClassLoader = null;
		}
		this.searchArchives = searchArchives;
	}

	protected void resetContext() {
		contextProject = null;
		contextModel = null;
		projectsInScope.clear();
		workspaceClassLoader = null;
	}

	protected Collection<IProject> getProjectsInScope() {
		if (projectsInScope.isEmpty()) {
			if (contextProject != null) {
				projectsInScope.add(contextProject);
			} else if (contextModel != null) {
				projectsInScope.addAll(collectReachableProjects(contextModel));
			}
		}
		return projectsInScope;
	}

	protected Collection<IProject> collectReachableProjects(IModelDescriptor model) {
		Set<IProject> reachableProjects = new HashSet<IProject>();
		reachableProjects.add(model.getRoot().getProject());
		for (IResource referencedModelRoot : model.getReferencedRoots()) {
			if (referencedModelRoot instanceof IFile) {
				IModelDescriptor referencedModel = ModelDescriptorRegistry.INSTANCE.getModel((IFile) referencedModelRoot);
				reachableProjects.addAll(collectReachableProjects(referencedModel));
			} else if (referencedModelRoot instanceof IContainer) {
				for (IModelDescriptor referencedModel : ModelDescriptorRegistry.INSTANCE.getModels((IContainer) referencedModelRoot)) {
					reachableProjects.addAll(collectReachableProjects(referencedModel));
				}
			}
		}
		return reachableProjects;
	}

	protected Collection<IModelDescriptor> getModelsInScope() {
		Set<IModelDescriptor> modelsInScope = new HashSet<IModelDescriptor>();
		if (contextModel != null) {
			modelsInScope.add(contextModel);
		} else if (contextProject != null) {
			modelsInScope.addAll(ModelDescriptorRegistry.INSTANCE.getModels(contextProject));
		}
		return modelsInScope;
	}

	protected ClassLoader getWorkspaceClassLoader() {
		if (workspaceClassLoader == null) {
			workspaceClassLoader = createWorkspaceClassLoader();
		}
		return workspaceClassLoader;
	}

	protected ClassLoader createWorkspaceClassLoader() {
		Set<URL> outputURLs = new HashSet<URL>();
		for (IProject project : getProjectsInScope()) {
			try {
				IPath outputPath = getJavaOutputPath(project);
				if (outputPath != null) {
					IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
					IFolder javaOutputFolder = workspaceRoot.getFolder(outputPath);
					URL outputURL = javaOutputFolder.getLocation().toFile().toURI().toURL();
					outputURLs.add(outputURL);
				}
			} catch (Exception ex) {
				// Ignore exception
			}
		}
		return !outputURLs.isEmpty() ? new URLClassLoader(outputURLs.toArray(new URL[outputURLs.size()]), searchArchives ? Thread.currentThread()
				.getContextClassLoader() : null) : null;
	}

	protected IPath getJavaOutputPath(IProject project) {
		try {
			IJavaProject javaProject = JavaCore.create(project);
			return javaProject.getOutputLocation();
		} catch (JavaModelException ex) {
			// Ignore exception
		}
		return null;
	}

	@Override
	protected Class<?> tryLoadClass(final String clazzName) throws ClassNotFoundException {
		// Risk to run into uncaught NullPointerException or ClassNotFoundException in order to trigger
		// delegation to org.eclipse.emf.mwe.core.resources.AbstractResourceLoader.internalLoadClass(String)
		// in that case
		return getWorkspaceClassLoader().loadClass(clazzName);
	}

	@Override
	public URL getResource(String path) {
		URL url = resolveAgainstProjectsInScope(path);
		if (url != null) {
			return url;
		}
		if (path.endsWith(IXtendXpandConstants.TEMPLATE_EXTENSION)) {
			url = resolveAgainstSpecialFoldersInScope(getTemplateFolderName(), path);
			if (url != null) {
				return url;
			}
		}
		if (path.endsWith(IXtendXpandConstants.EXTENSION_EXTENSION)) {
			url = resolveAgainstSpecialFoldersInScope(getExtensionFolderName(), path);
			if (url != null) {
				return url;
			}
		}
		if (path.endsWith(IXtendXpandConstants.CHECK_EXTENSION)) {
			url = resolveAgainstSpecialFoldersInScope(getCheckFolderName(), path);
			if (url != null) {
				return url;
			}
		}
		url = resolveAgainstWorkspaceClasspath(path);
		if (url != null) {
			return url;
		}
		url = resolveAgainstModelFilesInScope(path);
		if (url != null) {
			return url;
		}

		if (searchArchives) {
			return super.getResource(path);
		}
		return null;
	}

	protected URL resolveAgainstProjectsInScope(String path) {
		for (IProject project : getProjectsInScope()) {
			IFile file = project.getFile(new Path(path));
			if (file != null && file.exists()) {
				try {
					return file.getLocation().toFile().toURI().toURL();
				} catch (Exception ex) {
					// Ignore exception
				}
			}
		}
		return null;
	}

	protected URL resolveAgainstSpecialFoldersInScope(String specialFolderName, String path) {
		for (IProject project : getProjectsInScope()) {
			if (specialFolderName != null && specialFolderName.length() > 0) {
				IFolder specialFolder = project.getFolder(specialFolderName);
				IFile templateFile = specialFolder.getFile(new Path(path));
				if (templateFile != null && templateFile.exists()) {
					try {
						return templateFile.getLocation().toFile().toURI().toURL();
					} catch (Exception ex) {
						// Ignore exception
					}
				}
			}
		}
		return null;
	}

	protected String getTemplateFolderName() {
		return DEFAULT_TEMPLATE_FOLDER_NAME;
	}

	protected String getExtensionFolderName() {
		return DEFAULT_EXTENSION_FOLDER_NAME;
	}

	protected String getCheckFolderName() {
		return DEFAULT_CHECK_FOLDER_NAME;
	}

	protected URL resolveAgainstWorkspaceClasspath(String path) {
		try {
			ClassLoader classLoader = getWorkspaceClassLoader();
			if (classLoader != null) {
				return classLoader.getResource(path);
			}
		} catch (Exception ex) {
			// Ignore exception
		}
		return null;
	}

	protected URL resolveAgainstModelFilesInScope(String path) {
		Set<IContainer> modelFileContainers = new HashSet<IContainer>();
		for (IModelDescriptor model : getModelsInScope()) {
			for (IFile modelFile : model.getPersistedFiles(true)) {
				modelFileContainers.add(modelFile.getParent());
			}
		}
		for (IContainer container : modelFileContainers) {
			IFile file = container.getFile(new Path(path));
			if (file != null && file.exists()) {
				try {
					return file.getLocation().toFile().toURI().toURL();
				} catch (Exception ex) {
					// Ignore exception
				}
			}
		}
		return null;
	}
}

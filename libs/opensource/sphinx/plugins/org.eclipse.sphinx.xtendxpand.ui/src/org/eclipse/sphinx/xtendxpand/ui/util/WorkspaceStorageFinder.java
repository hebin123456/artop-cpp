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
 *     itemis - [358131] Make Xtend/Xpand/CheckJobs more robust against template file encoding mismatches
 *     itemis - [406564] BasicWorkspaceResourceLoader#getResource should not delegate to super
 *     itemis - [406562] WorkspaceStorageFinder#getPriority
 *
 * </copyright>
 */
package org.eclipse.sphinx.xtendxpand.ui.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.sphinx.emf.mwe.resources.BasicWorkspaceResourceLoader;
import org.eclipse.sphinx.emf.mwe.resources.IWorkspaceResourceLoader;
import org.eclipse.sphinx.xtendxpand.util.XtendXpandUtil;
import org.eclipse.xtend.shared.ui.StorageFinder2;
import org.eclipse.xtend.shared.ui.core.internal.ResourceID;

public class WorkspaceStorageFinder implements StorageFinder2 {

	protected IWorkspaceResourceLoader workspaceResourceLoader;

	public WorkspaceStorageFinder() {
		workspaceResourceLoader = createWorkspaceResourceLoader();
	}

	protected IWorkspaceResourceLoader createWorkspaceResourceLoader() {
		return new BasicWorkspaceResourceLoader();
	}

	/*
	 * @see org.eclipse.xtend.shared.ui.StorageFinder2#findXtendXpandResourceID(org.eclipse.jdt.core.IJavaProject,
	 * org.eclipse.core.resources.IStorage)
	 */
	@Override
	public ResourceID findXtendXpandResourceID(IJavaProject javaProject, IStorage storage) {
		workspaceResourceLoader.setContextProject(javaProject.getProject());

		if (storage instanceof IFile) {
			IFile file = (IFile) storage;

			// Ignore copies of Xtend/Xpand files under java output folder
			IPath javaOutputPath = getJavaOutputPath(javaProject.getProject());
			if (javaOutputPath != null && javaOutputPath.isPrefixOf(file.getFullPath())) {
				return null;
			}
			return new ResourceID(XtendXpandUtil.getQualifiedName(file, null), file.getFileExtension());
		}
		return null;
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

	/*
	 * @see org.eclipse.xtend.shared.ui.StorageFinder#findStorage(org.eclipse.jdt.core.IJavaProject,
	 * org.eclipse.xtend.shared.ui.core.internal.ResourceID, boolean)
	 */
	@Override
	public IStorage findStorage(IJavaProject javaProject, ResourceID resourceID, boolean searchJars) {
		workspaceResourceLoader.setContextProject(javaProject.getProject());
		workspaceResourceLoader.setSearchArchives(false);

		return XtendXpandUtil.getUnderlyingFile(resourceID.name, resourceID.extension, workspaceResourceLoader);

		// TODO Add support (and tests) for cases where resource is not a workspace file but shipped as a plug-in
		// resource. This becomes relevant when a template/extension file that is edited in the workspace refers to a
		// template/extension resource in a plug-in (e.g., an Xpand advice in a workspace template that enhances an
		// Xpand definition in a plug-in template). However, keep in mind that switching on
		// BasicWorkspaceResourceLoader#setSearchArchives(Boolean) may entail a significant runtime performance
		// overhead, in particular in workpaces with many references to template/extension files that don't exist.
		// Consider to provide some sort of (optional) plug-in white list mechanism that can be used to avoid that all
		// plug-ins get crawled for each and every reference to a non-existing template/extension file.
	}

	/*
	 * @see org.eclipse.xtend.shared.ui.StorageFinder#getPriority()
	 */
	@Override
	public int getPriority() {
		return 2;
	}
}

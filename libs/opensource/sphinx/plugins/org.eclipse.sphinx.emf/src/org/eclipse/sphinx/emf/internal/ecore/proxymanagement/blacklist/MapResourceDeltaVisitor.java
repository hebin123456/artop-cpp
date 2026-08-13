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
package org.eclipse.sphinx.emf.internal.ecore.proxymanagement.blacklist;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.resource.Resource;

final class MapResourceDeltaVisitor implements IResourceDeltaVisitor {

	public static final Set<String> REGISTERED_EXTENSIONS = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().keySet();

	public MapResourceDeltaVisitor() {
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		if (!resource.isDerived()) {
			int type = resource.getType();
			int kind = delta.getKind();
			switch (type) {
			case IResource.PROJECT:
				return handleProjectChange((IProject) resource, kind);
			case IResource.ROOT:
			case IResource.FOLDER:
				return true;
			case IResource.FILE:
				return handleFileChange((IFile) resource, kind);
			default:
				// do nothing
			}
		}
		return false;
	}

	private boolean handleFileChange(IFile file, int kind) {
		String fileExtension = file.getFileExtension();
		IProject project = file.getProject();

		// May when .project change also ???
		if (project != null && ("classpath".equals(fileExtension) || "MANIFEST.MF".equals(file.getName()))) { //$NON-NLS-1$ //$NON-NLS-2$
			// Re-index complete java project on classpath changes
			// workspaceModelIndexer.indexProject(project, Collections.<Integer> emptyList());
		} else if (REGISTERED_EXTENSIONS.contains(fileExtension)) {
			if (project != null) {
				return false;
			}
			switch (kind) {
			case IResourceDelta.ADDED:
				//				System.out.println("File added :" + file.toString()); //$NON-NLS-1$
			case IResourceDelta.CHANGED:
				// System.out.println("File changed :" + file.toString());
				// workspaceModelIndexer.indexModelFile(file);
				break;
			case IResourceDelta.REMOVED:
				// System.out.println("File removed :" + file.toString());
				// workspaceModelIndexer.removeModelFile(file);
				break;
			default:
				// do nothing
			}
		}
		return false;
	}

	private boolean handleProjectChange(IProject project, int kind) throws CoreException {

		if (project.isOpen()) { // Return false on closing project
			switch (kind) {
			case IResourceDelta.CHANGED:
				return true;
			case IResourceDelta.ADDED:
				// Collection<IFile> allFiles = ExtendedPlatform.getAllFiles(project, false);
				// System.out.println("project added :" + allFiles.toString());
				// workspaceModelIndexer.indexProject(project, Collections.<Integer> emptyList());
				return false;
			case IResourceDelta.REMOVED:
				// Collection<IFile> files = ExtendedPlatform.getAllFiles(project, false);
				// System.out.println("project removed :" + files.toString());
				// workspaceModelIndexer.removeProject(project);
				return false;
			default:
				return true;
			}
		}
		return false;
	}

}
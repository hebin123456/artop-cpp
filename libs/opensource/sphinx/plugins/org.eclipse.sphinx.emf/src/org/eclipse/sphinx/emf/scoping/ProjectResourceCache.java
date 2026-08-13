/**
 * <copyright>
 *
 * Copyright (c) 2008-2019 BWM Car IT, See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     BMW Car IT - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.scoping;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.platform.resources.DefaultResourceChangeHandler;
import org.eclipse.sphinx.platform.resources.ResourceDeltaVisitor;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * @deprecated Was used by {@link ProjectResourceScope#belongsToRootOrReferencedProjects(URI, boolean)} before whose
 *             implementation has been greatly simplified by converting provided {@link URI} to corresponding
 *             {@link IFile} and delegating to {@link ProjectResourceScope#belongsTo(IFile, boolean)} rather than
 *             relying on {@link IFile} to {@link URI} mappings provided by this cache.
 */
@Deprecated
public class ProjectResourceCache {

	class InvalidationListener implements IResourceChangeListener {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			try {
				IResourceDelta delta = event.getDelta();
				if (delta != null) {
					IResourceDeltaVisitor visitor = new ResourceDeltaVisitor(event.getType(), new DefaultResourceChangeHandler() {
						@Override
						public void handleProjectCreated(int eventType, IProject project) {
							invalidate();
						}

						@Override
						public void handleProjectOpened(int eventType, IProject project) {
							invalidate();
						}

						@Override
						public void handleProjectRenamed(int eventType, IProject oldProject, IProject newProject) {
							invalidate();
						}

						@Override
						public void handleProjectDescriptionChanged(int eventType, IProject project) {
							invalidate();
						}

						@Override
						public void handleProjectClosed(int eventType, IProject project) {
							invalidate();
						}

						@Override
						public void handleProjectRemoved(int eventType, IProject project) {
							invalidate();
						}
					});

					delta.accept(visitor);
				}
			} catch (CoreException ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			}
		}
	}

	private final Map<IProject, Set<URI>> cache = new HashMap<IProject, Set<URI>>();
	private InvalidationListener invalidationListener = null;

	private final void buildCache(IProject project) {
		try {
			if (!cache.containsKey(project)) {
				final Set<URI> resources = new HashSet<URI>();
				if (project.exists() && project.isOpen()) {
					project.accept(new IResourceVisitor() {

						@Override
						public boolean visit(IResource resource) throws CoreException {
							if (resource instanceof IContainer) {
								return true;
							}
							resources.add(EcorePlatformUtil.createURI(resource.getFullPath()));
							return false;
						}
					});
				}
				cache.put(project, resources);

			}
			if (invalidationListener == null) {
				invalidationListener = new InvalidationListener();
				ResourcesPlugin.getWorkspace().addResourceChangeListener(invalidationListener);
			}
		} catch (CoreException ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}

	}

	private final void invalidate() {
		if (invalidationListener != null) {
			cache.clear();
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(invalidationListener);
			invalidationListener = null;
		}
	}

	public Set<URI> getResources(IProject project) {
		buildCache(project);
		return cache.containsKey(project) ? cache.get(project) : Collections.emptySet();
	}

}

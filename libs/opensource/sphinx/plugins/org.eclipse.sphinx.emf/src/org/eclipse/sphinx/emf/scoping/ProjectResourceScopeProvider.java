/**
 * <copyright>
 * 
 * Copyright (c) 2008-2012 See4sys, BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     BMW Car IT - [373481] Performance optimizations for model loading. Added referenced projects cache.
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.scoping;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.internal.model.ModelDescriptorSynchronizer;
import org.eclipse.sphinx.emf.internal.model.ProjectScopeModelDescriptorSynchronizerDelegate;
import org.eclipse.sphinx.platform.resources.DefaultResourceChangeHandler;
import org.eclipse.sphinx.platform.resources.ResourceDeltaVisitor;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public class ProjectResourceScopeProvider extends AbstractResourceScopeProvider {

	interface IReferencedProjectsProvider {
		Collection<IProject> get(IProject p);
	}

	static class ReferencedProjectsProvider implements IReferencedProjectsProvider {
		@Override
		public Collection<IProject> get(IProject p) {
			return ExtendedPlatform.getAllReferencedProjects(p);
		}
	}

	static class ReferencedProjectsCache implements IReferencedProjectsProvider {
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

		IReferencedProjectsProvider provider = new ReferencedProjectsProvider();

		Map<IProject, Collection<IProject>> cache = new WeakHashMap<IProject, Collection<IProject>>();

		ReferencedProjectsCache() {
			ResourcesPlugin.getWorkspace().addResourceChangeListener(new InvalidationListener());
		}

		@Override
		public synchronized Collection<IProject> get(IProject p) {
			Collection<IProject> referencedProjects = cache.get(p);

			if (referencedProjects == null) {
				referencedProjects = provider.get(p);

				cache.put(p, referencedProjects);
			}

			return referencedProjects;
		}

		synchronized void invalidate() {
			cache.clear();
		}
	}

	protected ReferencedProjectsCache referencedProjectsCache = new ReferencedProjectsCache();

	public ProjectResourceScopeProvider() {
		ModelDescriptorSynchronizer.INSTANCE.addDelegate(ProjectScopeModelDescriptorSynchronizerDelegate.INSTANCE);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.scoping.AbstractResourceScopeProvider#createScope(org.eclipse.core.resources.IResource)
	 */
	@Override
	protected ProjectResourceScope createScope(IResource resource) {
		ProjectResourceScope scope = new ProjectResourceScope(resource);
		configureScope(scope);
		return scope;
	}

	protected void configureScope(ProjectResourceScope scope) {
		scope.setReferencedProjectsProvider(referencedProjectsCache);
	}
}

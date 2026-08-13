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
 *     itemis - [414125] Enhance ResourceDeltaVisitor to enable the analysis of IFolder added/moved/removed
 * 
 * </copyright>
 */
package org.eclipse.sphinx.platform.resources;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.sphinx.platform.internal.Activator;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * An {@link IResourceDeltaVisitor} implementation which simplifies the analysis of {@link IResourceDelta}s and skips
 * platform private resources (i.e., team private resources, project description files, and project properties folders
 * and files). Rather than digging through the low-level {@link IResourceDelta}s themselves, clients can implement more
 * use case oriented high-level handler methods defined by {@link IResourceChangeHandler} and hook those up to
 * {@link IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)} implementations by
 * using this class.
 * 
 * @see IResourceDelta
 * @see IResourceDeltaVisitor
 * @see IResourceChangeHandler
 * @since 0.7.0
 */
public class ResourceDeltaVisitor implements IResourceDeltaVisitor {

	protected int eventType;

	protected Collection<? extends IResourceChangeHandler> resourceChangeHandlers;

	/**
	 * Constructor.
	 */
	public ResourceDeltaVisitor(int eventType, IResourceChangeHandler resourceChangeHandler) {
		this(eventType, resourceChangeHandler != null ? Collections.singleton(resourceChangeHandler) : Collections
				.<IResourceChangeHandler> emptySet());
	}

	/**
	 * Constructor.
	 */
	public ResourceDeltaVisitor(int eventType, Collection<? extends IResourceChangeHandler> resourceChangeHandlers) {
		Assert.isNotNull(resourceChangeHandlers);

		this.eventType = eventType;
		this.resourceChangeHandlers = resourceChangeHandlers;
	}

	/*
	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
	 */
	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		try {
			IResource resource = delta.getResource();
			ResourceDeltaFlagsAnalyzer flags = new ResourceDeltaFlagsAnalyzer(delta);
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				if (resource instanceof IProject) {
					IProject project = (IProject) resource;

					// Has a project been added?
					if (flags.OPEN && project.isOpen()) {
						// Has a new project been created?
						if (!flags.MOVED_FROM) {
							for (IResourceChangeHandler handler : resourceChangeHandlers) {
								try {
									handler.handleProjectCreated(eventType, project);
								} catch (Exception ex) {
									PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
								}
							}
						}
					}
				}

				else if (resource instanceof IFolder) {
					IFolder folder = (IFolder) resource;

					// Has a new folder been created?
					if (!flags.MOVED_FROM) {
						if (!ExtendedPlatform.isPlatformPrivateResource(folder)) {
							for (IResourceChangeHandler handler : resourceChangeHandlers) {
								try {
									handler.handleFolderAdded(eventType, folder);
								} catch (Exception ex) {
									PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
								}
							}
						}
					}
				}

				else if (resource instanceof IFile) {
					IFile file = (IFile) resource;

					// Has a new file been created?
					if (!flags.MOVED_FROM) {
						if (!ExtendedPlatform.isPlatformPrivateResource(file)) {
							for (IResourceChangeHandler handler : resourceChangeHandlers) {
								try {
									handler.handleFileAdded(eventType, file);
								} catch (Exception ex) {
									PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
								}
							}
						}
					}
				}
				break;
			case IResourceDelta.CHANGED:
				if (resource instanceof IProject) {
					IProject project = (IProject) resource;

					// Has a project been opened?
					if (flags.OPEN && project.isOpen()) {
						for (IResourceChangeHandler handler : resourceChangeHandlers) {
							try {
								handler.handleProjectOpened(eventType, project);
							} catch (Exception ex) {
								PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
							}
						}
					}

					// Has a project's description been changed (referenced projects, linked resources, nature, etc.)?
					else if (flags.DESCRIPTION && project.isOpen()) {
						for (IResourceDelta childDelta : delta.getAffectedChildren(IResourceDelta.CHANGED)) {
							ResourceDeltaFlagsAnalyzer childFlag = new ResourceDeltaFlagsAnalyzer(childDelta);
							if (childFlag.CONTENT && ExtendedPlatform.isProjectDescriptionFile(childDelta.getResource())) {
								for (IResourceChangeHandler handler : resourceChangeHandlers) {
									try {
										handler.handleProjectDescriptionChanged(eventType, project);
									} catch (Exception ex) {
										PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
									}
								}
							}
						}
					}

					// Has a settings folder for a project's properties but no other files been added or removed?
					else if (flags.ZERO && project.isOpen()) {
						IResourceDelta[] childDeltas = delta.getAffectedChildren(IResourceDelta.ADDED | IResourceDelta.REMOVED);
						if (childDeltas.length == 1) {
							IResourceDelta childDelta = childDeltas[0];
							if (childDelta.getResource() instanceof IFolder) {
								IFolder folder = (IFolder) childDelta.getResource();
								if (ExtendedPlatform.isProjectPropertiesFolder(folder)) {
									Set<String> preferenceFileNames = new HashSet<String>();
									for (IResourceDelta grandChildDelta : childDelta.getAffectedChildren(IResourceDelta.ADDED
											| IResourceDelta.REMOVED)) {
										ResourceDeltaFlagsAnalyzer grandChildFlag = new ResourceDeltaFlagsAnalyzer(grandChildDelta);
										if (grandChildFlag.ZERO) {
											preferenceFileNames.add(grandChildDelta.getResource().getName());
										}
									}
									for (IResourceChangeHandler handler : resourceChangeHandlers) {
										try {
											handler.handleProjectSettingsChanged(eventType, folder.getProject(), preferenceFileNames);
										} catch (Exception ex) {
											PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
										}
									}
								}
							}
						}
					}

					// Has a project been closed?
					else if (flags.OPEN && !project.isOpen()) {
						for (IResourceChangeHandler handler : resourceChangeHandlers) {
							try {
								handler.handleProjectClosed(eventType, project);
							} catch (Exception ex) {
								PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
							}
						}
					}
				}

				else if (resource instanceof IFolder) {
					IFolder folder = (IFolder) resource;

					// Has settings folder with a project's properties been changed?
					if (ExtendedPlatform.isProjectPropertiesFolder(folder)) {
						Set<String> preferenceFileNames = new HashSet<String>();
						for (IResourceDelta childDelta : delta.getAffectedChildren(IResourceDelta.ADDED | IResourceDelta.CHANGED
								| IResourceDelta.REMOVED)) {
							ResourceDeltaFlagsAnalyzer childFlag = new ResourceDeltaFlagsAnalyzer(childDelta);
							if (childFlag.ZERO || childFlag.CONTENT) {
								preferenceFileNames.add(childDelta.getResource().getName());
							}
						}
						for (IResourceChangeHandler handler : resourceChangeHandlers) {
							try {
								handler.handleProjectSettingsChanged(eventType, folder.getProject(), preferenceFileNames);
							} catch (Exception ex) {
								PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
							}
						}
					}

					else if (ExtendedPlatform.isTeamPrivateResource(resource)) {
						// Don't visit children of changed team private folders
						return false;
					}
				}

				else if (resource instanceof IFile) {
					IFile file = (IFile) resource;

					if (flags.CONTENT) {
						if (!ExtendedPlatform.isPlatformPrivateResource(file)) {
							for (IResourceChangeHandler handler : resourceChangeHandlers) {
								try {
									handler.handleFileChanged(eventType, file);
								} catch (Exception ex) {
									PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
								}
							}
						}
					}
				}
				break;
			case IResourceDelta.REMOVED:
				if (resource instanceof IProject) {
					IProject project = (IProject) resource;

					// Has a project been deleted?
					if (!flags.MOVED_TO) {
						for (IResourceChangeHandler handler : resourceChangeHandlers) {
							try {
								handler.handleProjectRemoved(eventType, project);
							} catch (Exception ex) {
								PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
							}
						}
					}

					// Has a project been renamed?
					else if (flags.MOVED_TO) {
						String newProjectName = delta.getMovedToPath().lastSegment();
						IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(newProjectName);
						for (IResourceChangeHandler handler : resourceChangeHandlers) {
							try {
								handler.handleProjectRenamed(eventType, project, newProject);
							} catch (Exception ex) {
								PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
							}
						}
					}
				}

				else if (resource instanceof IFolder) {
					IFolder folder = (IFolder) resource;

					// Has a folder been deleted?
					if (!flags.MOVED_TO) {
						if (!ExtendedPlatform.isPlatformPrivateResource(folder)) {
							for (IResourceChangeHandler handler : resourceChangeHandlers) {
								try {
									handler.handleFolderRemoved(eventType, folder);
								} catch (Exception ex) {
									PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
								}
							}
						}

					}

					// Has a folder been moved or renamed?
					else if (flags.MOVED_TO) {
						if (!ExtendedPlatform.isPlatformPrivateResource(folder)) {
							IFolder newFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(delta.getMovedToPath());
							for (IResourceChangeHandler handler : resourceChangeHandlers) {
								try {
									handler.handleFolderMoved(eventType, folder, newFolder);
								} catch (Exception ex) {
									PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
								}
							}
						}
					}
				}

				if (resource instanceof IFile) {
					IFile file = (IFile) resource;

					// Has a file been deleted?
					if (!flags.MOVED_TO) {
						if (!ExtendedPlatform.isPlatformPrivateResource(file)) {
							for (IResourceChangeHandler handler : resourceChangeHandlers) {
								try {
									handler.handleFileRemoved(eventType, file);
								} catch (Exception ex) {
									PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
								}
							}
						}
					}

					// Has a file been moved or renamed?
					else if (flags.MOVED_TO) {
						if (!ExtendedPlatform.isPlatformPrivateResource(file)) {
							IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(delta.getMovedToPath());
							for (IResourceChangeHandler handler : resourceChangeHandlers) {
								try {
									handler.handleFileMoved(eventType, file, newFile);
								} catch (Exception ex) {
									PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
								}
							}
						}
					}
				}
				break;
			default:
				break;
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
		}
		return true;
	}
}

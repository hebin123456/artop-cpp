/**
 * <copyright>
 *
 * Copyright (c) 2008-2019 See4sys, BMW Car IT, Continental Engineering Services, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Added/Updated javadoc
 *     Conti - [353487] - Performance of ExtendedPlatform.isPlatformPrivateResource
 *     itemis - [357965] Scheduling rules for saving new resources must be scoped to enclosing project
 *     BMW Car IT - [373481] Performance optimizations for model loading
 *     BMW Car IT - [374883] Improve handling of out-of-sync workspace files during descriptor initialization
 *
 * </copyright>
 */
package org.eclipse.sphinx.platform.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeSettings;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.sphinx.platform.internal.Activator;
import org.eclipse.sphinx.platform.internal.messages.Messages;
import org.eclipse.sphinx.platform.resources.AbstractResourceVisitor;
import org.eclipse.sphinx.platform.resources.IResourceSyncMarker;
import org.eclipse.sphinx.platform.resources.MarkerJob;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleRevision;

/**
 * A utility class which extends the functionality provided by the eclipse platform.
 */
@SuppressWarnings("restriction")
public final class ExtendedPlatform {

	public static final int LIMIT_INDIVIDUAL_RESOURCES_SCHEDULING_RULE = 500;

	// Prevent from instantiation
	private ExtendedPlatform() {
	}

	public static final boolean IS_PLATFORM_RUNNING;
	static {
		boolean result = false;
		try {
			result = Platform.isRunning();
		} catch (Throwable exception) {
			// Assume that we aren't running
		}
		IS_PLATFORM_RUNNING = result;
	}

	public static final boolean IS_WORKSPACE_AVAILABLE;
	static {
		IS_WORKSPACE_AVAILABLE = isBundleAvailable(ResourcesPlugin.PI_RESOURCES);
	}

	/**
	 * Returns the feature version of the running instance of the Eclipse platform (e.g., 3.4 for Eclipse 3.4, 3.5 for
	 * Eclipse 3.5).
	 *
	 * @return The feature version of the running Eclipse platform instance.
	 */
	public static String getFeatureVersion() {
		Bundle bundle = Platform.getBundle("org.eclipse.core.runtime"); //$NON-NLS-1$
		if (bundle != null) {
			Dictionary<?, ?> headers = bundle.getHeaders();
			String version = (String) headers.get("Bundle-Version"); //$NON-NLS-1$
			return version.substring(0, 3);
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns an ordinal for the feature version of the running instance of the Eclipse platform (e.g., 34 for Eclipse
	 * 3.4, 35 for Eclipse 3.5). Can be used for easy testing if the running version of Eclipse platform is newer or
	 * older than a given version.
	 *
	 * @return The feature version ordinal of the running Eclipse platform instance.
	 */
	public static int getFeatureVersionOrdinal() {
		String version = getFeatureVersion();
		return Integer.parseInt(version.replaceAll("\\.", "")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean isBundleAvailable(String pluginId) {
		if (IS_PLATFORM_RUNNING) {
			try {
				Bundle resourcesBundle = Platform.getBundle(pluginId);
				return resourcesBundle != null && (resourcesBundle.getState() & (Bundle.ACTIVE | Bundle.STARTING | Bundle.RESOLVED)) != 0;
			} catch (Throwable exception) {
				// Assume that it's not available
			}
		}
		return false;
	}

	/**
	 * Returns the active bundle matching specified symbolic name that has the highest version. Starts that bundle if
	 * not already in active state.
	 *
	 * @param symbolicName
	 *            Symbolic name of bundle to be loaded.
	 * @return The bundle that has the specified symbolic name with the highest version in active state, or
	 *         <tt>null</tt> if no bundle can be found or activated.
	 * @see Platform#getBundle(String)
	 */
	public static Bundle loadBundle(String symbolicName) {
		Bundle bundle = Platform.getBundle(symbolicName);
		if (bundle != null) {
			try {
				// Avoid attempts to start fragments individually (they get automatically started when their host
				// plug-in is started)
				BundleRevision revision = bundle.adapt(BundleRevision.class);
				if (revision != null && (revision.getTypes() & BundleRevision.TYPE_FRAGMENT) == 0) {
					if (bundle.getState() != Bundle.ACTIVE && bundle.getState() != Bundle.STARTING) {
						bundle.start(Bundle.START_TRANSIENT);
					}
				}
			} catch (BundleException ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
				return null;
			}
		}
		return bundle;
	}

	/**
	 * Returns the contributing bundle for a given <code>contribution</code>.
	 *
	 * @param contribution
	 *            The contribution whose contributor bundle must be returned<br>
	 *            (must not be <tt>null</tt>).
	 * @return The bundle that is responsible for the contribution of the given contribution (configuration element).
	 */
	public static final Bundle loadContributorBundle(IConfigurationElement contribution) {
		Assert.isNotNull(contribution);
		String symbolicName = contribution.getContributor().getName();
		return loadBundle(symbolicName);
	}

	/**
	 * Creates a {@link ISchedulingRule scheduling rule} required for modifying the given {@link IResource resource}.
	 *
	 * @param resource
	 *            The {@link IResource resource} to be modified.
	 * @return The resulting {@link ISchedulingRule scheduling rule} or <code>null</code> if no such could be created.
	 */
	public static ISchedulingRule createModifySchedulingRule(IResource resource) {
		if (resource != null) {
			IResourceRuleFactory ruleFactory = resource.getWorkspace().getRuleFactory();
			return ruleFactory.modifyRule(resource);
		}
		return null;
	}

	/**
	 * Creates a {@link ISchedulingRule scheduling rule} required for modifying the given collection of {@link IFile
	 * file}s.
	 *
	 * @param files
	 *            The collection of {@link IFile file}s to be modified.
	 * @return The resulting {@link ISchedulingRule scheduling rule} or <code>null</code> if provided collection of
	 *         {@link IFile file}s is empty or no such could be created for some other reason.
	 */
	public static ISchedulingRule createModifySchedulingRule(Collection<IFile> files) {
		if (files != null) {
			/*
			 * Performance optimization: Create a scheduling rule on a per file basis only if number of files is
			 * reasonably low.
			 */
			if (files.size() < LIMIT_INDIVIDUAL_RESOURCES_SCHEDULING_RULE) {
				Set<ISchedulingRule> rules = new HashSet<ISchedulingRule>();
				for (IFile file : files) {
					ISchedulingRule rule = createModifySchedulingRule(file);
					if (rule != null) {
						rules.add(rule);
					}
				}
				return MultiRule.combine(rules.toArray(new ISchedulingRule[rules.size()]));
			} else {
				// Return workspace root as scheduling rule otherwise
				return ResourcesPlugin.getWorkspace().getRoot();
			}
		}
		return null;
	}

	/**
	 * Creates a {@link ISchedulingRule scheduling rule} required for creating the given {@link IResource resource}.
	 *
	 * @param resource
	 *            The {@link IResource resource} to be created.
	 * @return The resulting {@link ISchedulingRule scheduling rule} or <code>null</code> if no such could be created.
	 */
	public static ISchedulingRule createCreateSchedulingRule(IResource resource) {
		if (resource != null) {
			IResourceRuleFactory ruleFactory = resource.getWorkspace().getRuleFactory();
			return ruleFactory.createRule(resource);
		}
		return null;
	}

	/**
	 * Creates a {@link ISchedulingRule scheduling rule} required for saving the given new {@link IResource resource}.
	 *
	 * @param resource
	 *            The new {@link IResource resource} to be saved.
	 * @return The resulting {@link ISchedulingRule scheduling rule} or <code>null</code> if no such could be created.
	 */
	public static ISchedulingRule createSaveNewSchedulingRule(IResource resource) {
		if (resource != null) {
			// Create modify rule for project behind given resource
			/*
			 * !! Important Note !! It might be that not all the folders on the resource's path already exist and are
			 * supposed to be created along with the new resource being saved. We therefore cannot go along with a
			 * create rule just for the file being created but must use a modify rule for the underlying project.
			 * Otherwise the creation of not yet existing containing folders would lead to rule conflicts.
			 */
			IProject project = resource.getProject();
			IResourceRuleFactory ruleFactory = resource.getWorkspace().getRuleFactory();
			return MultiRule.combine(new ISchedulingRule[] { ruleFactory.modifyRule(project), ruleFactory.refreshRule(project) });
		}
		return null;
	}

	/**
	 * Creates a {@link ISchedulingRule scheduling rule} required for saving the new {@link IFile file} with given
	 * {@link IPath path}.
	 *
	 * @param filePath
	 *            The {@link IPath path} of the new {@link IFile file} to be saved.
	 * @return The resulting {@link ISchedulingRule scheduling rule} or <code>null</code> if no such could be created.
	 */
	public static ISchedulingRule createSaveNewSchedulingRule(IPath filePath) {
		if (filePath != null) {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
			return createSaveNewSchedulingRule(file);
		}
		return null;
	}

	/**
	 * Creates a {@link ISchedulingRule scheduling rule} required for saving the new {@link IFile file}s with the given
	 * collection of {@link IPath path}s.
	 *
	 * @param filePaths
	 *            The collection of {@link IPath}s of the new {@link IFile file}s to be saved.
	 * @return The resulting {@link ISchedulingRule scheduling rule} or <code>null</code> if provided collection of
	 *         {@link IPath path}s is empty or no such could be created for some other reason.
	 */
	public static ISchedulingRule createSaveNewSchedulingRule(Collection<IPath> filePaths) {
		if (filePaths != null) {
			/*
			 * Performance optimization: Create a scheduling rule on a per file basis only if number of file paths is
			 * reasonably low.
			 */
			if (filePaths.size() < LIMIT_INDIVIDUAL_RESOURCES_SCHEDULING_RULE) {
				Set<ISchedulingRule> rules = new HashSet<ISchedulingRule>();
				for (IPath path : filePaths) {
					ISchedulingRule rule = createSaveNewSchedulingRule(path);
					if (rule != null) {
						rules.add(rule);
					}
				}
				return MultiRule.combine(rules.toArray(new ISchedulingRule[rules.size()]));
			} else {
				// Return workspace root as scheduling rule otherwise
				return ResourcesPlugin.getWorkspace().getRoot();
			}
		}
		return null;
	}

	/**
	 * Creates a {@link ISchedulingRule scheduling rule} required for saving the given {@link IResource resource}.
	 *
	 * @param resource
	 *            The {@link IResource resource} to be saved.
	 * @return The resulting {@link ISchedulingRule scheduling rule} or <code>null</code> if no such could be created.
	 */
	public static ISchedulingRule createSaveSchedulingRule(IResource resource) {
		if (resource != null) {
			IResourceRuleFactory ruleFactory = resource.getWorkspace().getRuleFactory();
			return MultiRule.combine(new ISchedulingRule[] { ruleFactory.modifyRule(resource), ruleFactory.refreshRule(resource) });
		}
		return null;
	}

	/**
	 * Indicates if given {@link IResource resource} is a project description file, i.e. a file named ".project" located
	 * directly under the root of a project.
	 *
	 * @param resource
	 *            The {@link IResource resource} to be investigated.
	 * @return <code>true</code> if given {@link IResource resource} is a project description file, or
	 *         <code>false</code> otherwise.
	 */
	public static boolean isProjectDescriptionFile(IResource resource) {
		if (resource instanceof IFile) {
			/*
			 * Performance optimization: test path segment number (which is fast) first and let path segment name
			 * comparison (which is slow) take place only if the path segment number condition is true
			 */
			IPath path = resource.getFullPath();
			return path.segmentCount() == 2 && IProjectDescription.DESCRIPTION_FILE_NAME.equals(path.lastSegment());
		}
		return false;
	}

	/**
	 * Indicates if given {@link IResource resource} is a project properties folder, i.e. a folder named ".settings"
	 * located directly under the root of a project.
	 *
	 * @param resource
	 *            The {@link IResource resource} to be investigated.
	 * @return <code>true</code> if given {@link IResource resource} is a project properties folder, or
	 *         <code>false</code> otherwise.
	 */
	public static boolean isProjectPropertiesFolder(IResource resource) {
		if (resource instanceof IFolder) {
			/*
			 * Performance optimization: test path segment number (which is fast) first and let path segment name
			 * comparison (which is slow) take place only if the path segment number condition is true
			 */
			IPath path = resource.getFullPath();
			return path.segmentCount() == 2 && ".settings".equals(path.lastSegment()); //$NON-NLS-1$
		}
		return false;
	}

	/**
	 * Indicates if given {@link IResource resource} is a project properties file, i.e. a *.prefs file in a folder named
	 * ".settings" and located directly under the root of a project.
	 *
	 * @param resource
	 *            The {@link IResource resource} to be investigated.
	 * @return <code>true</code> if given {@link IResource resource} is a project properties file, or <code>false</code>
	 *         otherwise.
	 */
	public static boolean isProjectPropertiesFile(IResource resource) {
		if (resource instanceof IFile) {
			return isProjectPropertiesFolder(resource.getParent()) && "prefs".equals(resource.getFileExtension()); //$NON-NLS-1$
		}
		return false;
	}

	/**
	 * Returns whether given {@link IResource resource} is team private (i.e., a resource marked for internal usage by
	 * CVS or SVN only).
	 * <p>
	 * This is a convenience method, fully equivalent to <code>isTeamPrivateResource(resource, IResource.NONE)</code>.
	 * </p>
	 *
	 * @return <code>true</code> if given {@link IResource resource} is team private, or <code>false</code> otherwise.
	 * @see IResource#isTeamPrivateMember(boolean)
	 * @since 0.7.0
	 */
	public static boolean isTeamPrivateResource(IResource resource) {
		return isTeamPrivateResource(resource, IResource.NONE);
	}

	/**
	 * Returns whether given {@link IResource resource} is team private (i.e., a resource marked for internal usage by
	 * CVS or SVN only).
	 * <p>
	 * The {@link #CHECK_ANCESTORS} option flag indicates whether this method should consider {@link IResource ancestor
	 * resource}s in its calculation. If the {@link IResource#CHECK_ANCESTORS} flag is present, this method will return
	 * <code>true</code> if given {@link IResource resource}, or any {@link IResource parent resource}, is team private.
	 * If the {@link IResource#CHECK_ANCESTORS} option flag is not specified, this method returns false for children of
	 * team private resources.
	 * </p>
	 *
	 * @param options
	 *            Bit-wise OR of option flag constants (only {@link IResource#CHECK_ANCESTORS} is applicable).
	 * @return <code>true</code> if given {@link IResource resource} is team private, or <code>false</code> otherwise.
	 * @see IResource#isTeamPrivateMember(boolean)
	 * @see IResource#CHECK_ANCESTORS
	 * @since 0.7.0
	 */
	public static boolean isTeamPrivateResource(IResource resource, int options) {
		if (resource != null) {
			// CVS private resources can be detected using native Eclipse API because CVS client is always shipped
			// with Eclipse
			if (resource.isTeamPrivateMember()) {
				return true;
			}

			// Do extra name-based check for detecting SVN private resources because SVN client is not necessarily
			// present in every client's Eclipse target platform
			/*
			 * Performance optimization: test if ".svn" is contained in resource name; this enables direct detection of
			 * ".svn" folders and "*.svn-base" files and avoids recursive checking of ancestor resources in these cases.
			 */
			if (resource.getName().contains(".svn")) { //$NON-NLS-1$
				return true;
			}

			// Check ancestors if the appropriate option is set
			if ((options & IResource.CHECK_ANCESTORS) != 0) {
				return isTeamPrivateResource(resource.getParent(), options);
			}
		}
		return false;
	}

	/**
	 * Returns whether given {@link IResource resource} is platform private (i.e., a team private resource, project
	 * description file, or project properties folder or file).
	 *
	 * @return <code>true</code> if given {@link IResource resource} is platform private, or <code>false</code>
	 *         otherwise.
	 * @see IResource#isTeamPrivateMember(boolean)
	 * @since 0.7.0
	 */
	public static boolean isPlatformPrivateResource(IResource resource) {
		if (isTeamPrivateResource(resource)) {
			return true;
		} else if (isProjectDescriptionFile(resource)) {
			return true;
		} else if (isProjectPropertiesFolder(resource) || isProjectPropertiesFile(resource)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns all {@link IFile file}s contained in the {@link IFolder folder}. The {@link IFolder folder} is searched
	 * recursively. Thus, {@link IFile file}s contained in descending {@link IFolder folder}s are also returned.
	 *
	 * @param folder
	 *            The {@link IFolder folder} owning the {@link IFile file}s to return.
	 * @return The list of {@link IFile file}s owned by the given {@link IFolder folder} (and its descendant
	 *         {@link IFolder folder}s).
	 * @since 0.7.0
	 */
	public static final Collection<IFile> getAllFiles(IFolder folder) {
		final List<IFile> files = new ArrayList<IFile>();
		try {
			folder.accept(new AbstractResourceVisitor() {
				@Override
				public boolean doVisit(IResource resource) throws CoreException {
					if (resource instanceof IFile) {
						IFile file = (IFile) resource;
						if (file.isAccessible()) {
							files.add(file);
						}
					}
					return true;
				}
			});
		} catch (CoreException ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
		return files;
	}

	/**
	 * @param project
	 * @param members
	 * @param files
	 * @param visitedProjects
	 *            The already visited projects.
	 * @param deeply
	 *            If <b><code>true</code></b>, goes through referenced projects.
	 * @since 0.7.0
	 */
	private static void collectAllFiles(IProject project, IResource[] members, final List<IFile> files, Collection<IProject> visitedProjects,
			boolean deeply) {
		Assert.isNotNull(project);
		Assert.isNotNull(members);
		Assert.isNotNull(files);
		Assert.isNotNull(visitedProjects);

		// Avoid infinite visiting of projects cyclically referencing each other by tracking project which already have
		// been visited
		visitedProjects.add(project);

		// Retrieve all files from the given project
		for (IResource resource : members) {
			try {
				resource.accept(new AbstractResourceVisitor() {
					@Override
					public boolean doVisit(IResource resource) throws CoreException {
						if (resource instanceof IFile) {
							IFile file = (IFile) resource;
							if (file.isAccessible()) {
								files.add(file);
							}
						}
						return true;
					}
				});
			} catch (CoreException ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			}
		}

		if (deeply) {
			// Go through referenced projects if any
			for (IProject referencedProject : getReferencedProjectsSafely(project)) {
				if (project.isAccessible() && referencedProject.isAccessible() && !visitedProjects.contains(referencedProject)) {
					collectAllFiles(referencedProject, getMembersSafely(referencedProject), files, visitedProjects, deeply);
				}
			}
		}
	}

	/**
	 * Returns all files of a project.
	 *
	 * @param project
	 *            The project whose files must be returned.
	 * @param deeply
	 *            If set to <b><code>true</code></b> also the files from referenced projects are returned.
	 * @return The list of files owned by the given project.<br>
	 *         Goes through referenced projects according to <code>deeply</code> flag.
	 * @since 0.7.0
	 */
	public static final Collection<IFile> getAllFiles(IProject project, boolean deeply) {
		List<IFile> files = new ArrayList<IFile>();
		collectAllFiles(project, getMembersSafely(project), files, new HashSet<IProject>(), deeply);
		return files;
	}

	/**
	 * Encapsulated method {@link IContainer#members()} in order to keep as much robustness as possible.
	 *
	 * @param container
	 *            The container whose members must be returned.
	 * @return The resources that are members of the given container.
	 * @since 0.7.0
	 */
	public final static IResource[] getMembersSafely(IContainer container) {
		Assert.isNotNull(container);
		try {
			if (container.isAccessible()) {
				List<IResource> members = new ArrayList<IResource>();
				for (IResource member : container.members()) {
					if (!isPlatformPrivateResource(member)) {
						members.add(member);
					}
				}
				return members.toArray(new IResource[members.size()]);
			}
		} catch (CoreException ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
		return new IResource[0];
	}

	/**
	 * Encapsulated method {@link IProject#getReferencedProjects()} in order to keep as much robustness as possible.
	 *
	 * @see IProject#getReferencedProjects()
	 * @param project
	 *            The project whose referenced projects must be returned.
	 * @return The list of projects that are referenced by the given project.
	 * @since 0.7.0
	 */
	public static IProject[] getReferencedProjectsSafely(IProject project) {
		Assert.isNotNull(project);
		IProject[] referencedProjects = new IProject[0];
		try {
			if (project.isAccessible()) {
				referencedProjects = project.getReferencedProjects();
			}
		} catch (CoreException ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
		return referencedProjects;
	}

	/**
	 * Encapsulated method {@link IProject#getReferencingProjects()} in order to keep as much robustness as possible.
	 *
	 * @see IProject#getReferencingProjects()
	 * @param project
	 *            The project whose referencing projects must be returned.
	 * @return The list of projects that are referencing the given project.
	 * @since 0.7.0
	 */
	public static IProject[] getReferencingProjectsSafely(IProject project) {
		Assert.isNotNull(project);
		return project.getReferencingProjects();
	}

	private static void collectProjectsInGroup(IProject project, boolean includeReferencingProjects, Set<IProject> projectGroup) {
		if (projectGroup.add(project)) {
			for (IProject p : getReferencedProjectsSafely(project)) {
				collectProjectsInGroup(p, includeReferencingProjects, projectGroup);
			}
			if (includeReferencingProjects) {
				for (IProject p : getReferencingProjectsSafely(project)) {
					collectProjectsInGroup(p, includeReferencingProjects, projectGroup);
				}
			}
		}
	}

	/**
	 * Computes a group of projects. This method will search recursively all referenced projects of the given project
	 * and if wanted also the projects that reference the given project.
	 *
	 * @param project
	 *            A project whose scope will be computed.
	 * @param includeReferencingProjects
	 *            If <code>true</code> also includes projects that reference the given project.
	 */
	public static Set<IProject> getProjectGroup(IProject project, boolean includeReferencingProjects) {
		Assert.isNotNull(project);
		// A set to which all projects in scope will be added
		Set<IProject> projectGroup = new HashSet<IProject>();
		// Collect projects in group safely
		collectProjectsInGroup(project, includeReferencingProjects, projectGroup);
		return projectGroup;
	}

	/**
	 * @param project
	 * @param referencedProjects
	 * @since 0.7.0
	 */
	private static void collectReferencedProjects(IProject project, Set<IProject> allReferencedProjects) {
		if (project.isAccessible() && allReferencedProjects.add(project)) {
			try {
				for (IProject p : project.getReferencedProjects()) {
					collectReferencedProjects(p, allReferencedProjects);
				}
			} catch (CoreException ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			}
		}
	}

	/**
	 * Returns a {@linkplain Collection collection} containing all the {@linkplain IProject project}s that are
	 * referenced by the specified project and recursively all the projects that are referenced by the projects that are
	 * referenced by the given {@link IProject project}.
	 *
	 * @param project
	 *            The project whose all referenced projects must be returned.
	 * @return All the projects that are referenced by the specified {@link IProject project}.
	 * @since 0.7.0
	 */
	public static Collection<IProject> getAllReferencedProjects(IProject project) {
		Assert.isNotNull(project);
		// A set to which all referenced projects will be added
		Set<IProject> referencedProjects = new HashSet<IProject>();
		// Collect referenced projects safely
		collectReferencedProjects(project, referencedProjects);
		// Remove the specified project from the returned list
		referencedProjects.remove(project);
		return referencedProjects;
	}

	/**
	 * @param project
	 * @param referencingProjects
	 * @since 0.7.0
	 */
	private static void collectReferencingProjects(IProject project, Set<IProject> allReferencingProjects) {
		if (project.isAccessible() && allReferencingProjects.add(project)) {
			for (IProject p : project.getReferencingProjects()) {
				collectReferencingProjects(p, allReferencingProjects);
			}
		}
	}

	/**
	 * Returns a {@linkplain Collection collection} containing all the {@linkplain IProject project}s that are
	 * referencing the specified project and recursively all the projects that are referencing the projects that are
	 * referencing by the given {@link IProject project}.
	 *
	 * @param project
	 *            The project whose all referencing projects must be returned.
	 * @return All the projects that are referencing the specified {@link IProject project}.
	 * @since 0.7.0
	 */
	public static Collection<IProject> getAllReferencingProjects(IProject project) {
		Assert.isNotNull(project);
		// A set to which all referencing projects will be added
		Set<IProject> referencingProjects = new HashSet<IProject>();
		// Collect referencing projects safely
		collectReferencingProjects(project, referencingProjects);
		// Remove the specified project from the returned list
		referencingProjects.remove(project);
		return referencingProjects;
	}

	/**
	 * Returns wheather the given {@link IProject} is a root project. Root projects are projects which are not
	 * referenced from another project. From a root project an entire model can be loaded.
	 *
	 * @param project
	 *            The project to be evaluated.
	 * @return <tt>true</tt> if given project is a root project, <tt>false</tt> otherwise.
	 */
	public static boolean isRootProject(IProject project) {
		return project.getReferencingProjects().length == 0;
	}

	/**
	 * Returns the root project to which the given <code>project</code> contributes. Root projects are projects which
	 * are not referenced from another project. From a root project an entire model can be loaded.
	 * <p>
	 * If the given <code>project</code> has more than one root project the first one found will be returned. If the
	 * provided <code>project</code> is a root project itself it is returned.
	 *
	 * @param project
	 *            The project for which the root project is to be determined.
	 * @return The root project of the given <code>project</code>.
	 */
	public static IProject getFirstRootProject(IProject project) {
		Assert.isNotNull(project);
		if (project.isAccessible()) {
			List<IProject> rootProjects = new ArrayList<IProject>();
			collectRootProjects(new HashSet<IProject>(), rootProjects, project);
			if (!rootProjects.isEmpty()) {
				return rootProjects.get(0);
			}
		}
		return null;
	}

	private static void collectRootProjects(Collection<IProject> visitedProjects, Collection<IProject> rootProjects, IProject project) {
		if (!project.isOpen()) {
			return;
		}
		if (visitedProjects.contains(project)) {
			// A cycle in references has been detected, ends the recursion
			rootProjects.add(project);
		} else {
			// Mark the given project as visited (detection of references cycle)
			visitedProjects.add(project);

			// Retrieves projects referencing the given project
			IProject[] referencingProjects = project.getReferencingProjects();
			if (referencingProjects.length == 0) {
				// Given project is the end of the references chain; keep it as a model project
				rootProjects.add(project);
			} else {
				for (IProject referencingProject : referencingProjects) {
					// Recursive call...
					collectRootProjects(visitedProjects, rootProjects, referencingProject);
				}
			}
		}
	}

	/**
	 * Returns all root projects in the workspace.
	 *
	 * @return All root projects in the workspace.
	 * @see #getFirstRootProject(IProject)
	 */
	public static Collection<IProject> getRootProjects() {
		Set<IProject> rootProjects = new HashSet<IProject>();
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (!rootProjects.contains(project)) {
				collectRootProjects(new HashSet<IProject>(), rootProjects, project);
			}
		}
		return rootProjects;
	}

	/**
	 * Returns the projects in the workspace which have the given nature associated with them.
	 *
	 * @param natureId
	 *            An identifier specifying the nature of the projects to be returned.
	 * @return A collection of projects having the specified nature.
	 */
	public static Collection<IProject> getProjects(String natureId) {
		Collection<IProject> projects = new ArrayList<IProject>();
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (project.isAccessible()) {
				try {
					if (project.hasNature(natureId)) {
						projects.add(project);
					}
				} catch (CoreException ex) {
					// fail silent
				}
			}
		}
		return projects;
	}

	/**
	 * Creates a file name that is unique among the names of the files present in {@link IContainer container} with
	 * given <code>containerFullPath</code>.
	 *
	 * @param containerFullPath
	 * @param candidateFileName
	 * @return
	 */

	public static String createUniqueFileName(IPath containerFullPath, String candidateFileName) {
		Assert.isNotNull(containerFullPath);
		if (candidateFileName == null || candidateFileName.trim().length() == 0) {
			candidateFileName = "default"; //$NON-NLS-1$
		}

		IPath candidateFilePath = containerFullPath.append(candidateFileName);
		String candidateFileBaseName = candidateFilePath.removeFileExtension().lastSegment();
		String extension = candidateFilePath.getFileExtension();
		for (int i = 1; ResourcesPlugin.getWorkspace().getRoot().exists(candidateFilePath); i++) {
			candidateFilePath = containerFullPath.append(candidateFileBaseName + i);
			if (extension != null) {
				candidateFilePath = candidateFilePath.addFileExtension(extension);
			}
		}
		return candidateFilePath.lastSegment();
	}

	/**
	 * Creates a unique {@link IPath path} starting from given <code>candidatePath</code> making sure that the result is
	 * different from any of the provided <code>allocatedPaths</code>. In case that given <code>candidatePath</code>
	 * already is unique wrt provided <code>allocatedPaths</code> the <code>candidatePath</code> is returned as is.
	 * Otherwise the <code>candidatePath</code> is made unique by appending an appropriate number to its last segment.
	 * Any file extension present on the <code>candiatePath</code> is be preserved.
	 *
	 * @param candidatePath
	 *            The candidate {@link IPath path} from which a unique path is to be created.
	 * @param allocatedPaths
	 *            The {@link IPath path}s from which the created path should be different.
	 * @return A {@link IPath path} that is unique wrt provided <code>allocatedPaths</code>.
	 */
	public static IPath createUniquePath(IPath candidatePath, Collection<IPath> allocatedPaths) {
		Assert.isNotNull(candidatePath);
		Assert.isNotNull(allocatedPaths);

		IPath parentPath = candidatePath.removeLastSegments(1);
		String candidateLastSegmentName = candidatePath.removeFileExtension().lastSegment();
		String extension = candidatePath.getFileExtension();
		for (int i = 1; allocatedPaths.contains(candidatePath); i++) {
			candidatePath = parentPath.append(candidateLastSegmentName + i);
			if (extension != null) {
				candidatePath = candidatePath.addFileExtension(extension);
			}
		}
		return candidatePath;
	}

	private static final String NO_CONTENT_TYPE_ID = "org.eclipse.sphinx.platform.noContentTypeId"; //$NON-NLS-1$

	/**
	 * Returns the content-type identifier of the specified {@linkplain IFile file}.
	 * <p>
	 * In order to prevent performance losses, the content type id is cached as a <em>session property</em> on the file.
	 * It is then possible to retrieve that content type id later by calling
	 * {@linkplain IFile#getSessionProperty(QualifiedName)} where {@link QualifiedName qualifiedName} can be computed by
	 * using {@linkplain ExtendedPlatform#toQualifedName(String)} and specifying
	 * {@linkplain IExtendedPlatformConstants#RESOURCE_PROPERTY_CONTENT_TYPE_ID} as <code>name</code>.
	 *
	 * @param file
	 *            The file whose content-type identifier must be returned.
	 * @return The content-type identifier of the given {@link IFile file} or <code>null</code> if no such could be
	 *         determined.
	 * @throws CoreException
	 * @since 0.7.0
	 */
	public static String getContentTypeId(IFile file) throws CoreException {
		// Try to retrieve cached content type id
		if (hasCachedContentTypeId(file)) {
			return getCachedContentTypeId(file);
		}

		// No cached content type id available, so retrieve it natively from the file's content description
		String contentTypeId = nativeGetContentTypeId(file);

		// Cache resulting content type id in order to avoid that it needs to be retrieved natively for a second time
		setCachedContentTypeId(file, contentTypeId);

		return contentTypeId;
	}

	/**
	 * Returns the content-type identifier of the specified {@linkplain File file}.
	 *
	 * @param file
	 *            The file whose content-type identifier must be returned.
	 * @return The content-type identifier of the given {@link File file} or <code>null</code> if no such could be
	 *         determined.
	 * @throws IOException
	 * @since 0.7.0
	 */
	public static String getContentTypeId(File file) throws IOException {
		Assert.isNotNull(file);

		FileInputStream inputStream = null;
		try {
			/*
			 * !! Important Note !! Don't attempt to determine content type id of non-existing files. Otherwise
			 * Platform.getContentTypeManager().getDescriptionFor() would try to deduce it from the file extension on
			 * the URI which is very likely to produce inadequate results (e.g., in case of a model file with an .xml
			 * extension and a metamodel-specifc content type which existed before but got deleted prior to calling this
			 * method).
			 */
			if (file.exists()) {
				inputStream = new FileInputStream(file);
				// FIXME File bug to Eclipse Platform: Platform.getContentTypeManager().getDescriptionFor() apparently
				// unable to return content type description for .project contents
				IContentDescription description = Platform.getContentTypeManager().getDescriptionFor(inputStream, file.getPath(), null);
				if (description != null) {
					IContentType contentType = description.getContentType();
					if (contentType != null) {
						return contentType.getId();
					}
				}
			}
		} catch (FileNotFoundException ex) {
			// Ignore exception
		} finally {
			safeClose(inputStream);
		}
		return null;
	}

	private static String nativeGetContentTypeId(IFile file) throws CoreException {
		if (file != null && file.isAccessible()) {
			// Get the file's content description
			IContentDescription contentDescription = null;
			try {
				contentDescription = file.getContentDescription();
			} catch (ResourceException rex) {
				// File out of sync?
				if (!file.isSynchronized(IResource.DEPTH_ZERO)) {
					// Refresh file and try again
					try {
						file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
					} catch (Exception ex) {
						// Ignore exception
					}
					contentDescription = file.getContentDescription();
				} else {
					throw rex;
				}
			}

			// Extract content type id
			if (contentDescription != null) {
				IContentType contentType = contentDescription.getContentType();
				if (contentType != null) {
					return contentType.getId();
				}
			}
		}
		return null;
	}

	public static boolean hasCachedContentTypeId(IFile file) {
		return internalGetCachedContentTypeId(file) != null;
	}

	private static String getCachedContentTypeId(IFile file) {
		// Retrieve encoded content type id
		String rawContentTypeId = internalGetCachedContentTypeId(file);

		if (rawContentTypeId == null) {
			return null;
		}

		// Decode cached content type id and return it
		if (rawContentTypeId.intern() != NO_CONTENT_TYPE_ID) {
			// Ensure backward compatibility
			if (rawContentTypeId.intern() != "org.eclipse.sphinx.platform.unspecifiedContentType") { //$NON-NLS-1$
				return rawContentTypeId;
			}
		}
		return null;
	}

	private static String internalGetCachedContentTypeId(IFile file) {
		if (file != null && file.isAccessible()) {
			// The resource property key for the file's content type id
			QualifiedName key = ExtendedPlatform.toQualifedName(IExtendedPlatformConstants.RESOURCE_PROPERTY_CONTENT_TYPE_ID);

			// Try to retrieve content type id from the file's session property
			Object sessionProperty = null;
			try {
				sessionProperty = file.getSessionProperty(key);
			} catch (Exception ex) {
				// Ignore exception
			}
			if (sessionProperty instanceof String) {
				return (String) sessionProperty;
			}

			// Try to retrieve content type id from the file's persistent property
			String persistentProperty = null;
			try {
				persistentProperty = file.getPersistentProperty(key);
			} catch (Exception ex) {
				// Ignore exception
			}
			if (persistentProperty != null) {
				// Re-cache content type id as session property on underlying file in order to avoid that it needs
				// to be retrieved from the file's persistent property for a second time
				try {
					file.setSessionProperty(key, persistentProperty);
				} catch (Exception ex) {
					// Ignore exception
				}
				return persistentProperty;
			}
		}

		return null;
	}

	public static void setCachedContentTypeId(IFile file, String contentTypeId) {
		// Encode content type id and cache it
		internalSetCachedContentTypeId(file, contentTypeId != null ? contentTypeId : NO_CONTENT_TYPE_ID);
	}

	private static void internalSetCachedContentTypeId(IFile file, String rawContentTypeId) {
		if (file != null && file.isAccessible()) {
			// The resource property key for the file's content type id
			QualifiedName key = ExtendedPlatform.toQualifedName(IExtendedPlatformConstants.RESOURCE_PROPERTY_CONTENT_TYPE_ID);

			// Cache content type id as session property on underlying file
			/*
			 * !! Important Note !! Exclude project description (.project) files from content type caching with session
			 * properties because this would entail resource change notifications for those files. However,
			 * IResourceChangeListeners might interpret such notifications as a change of a project's nature, its linked
			 * resources, or other and trigger unintended and inappropriate operations behind.
			 */
			if (!isProjectDescriptionFile(file)) {
				try {
					file.setSessionProperty(key, rawContentTypeId);
				} catch (Exception ex) {
					// Ignore exception
				}
			}
		}
	}

	public static void persistContentTypeIdProperties(final IProject project, final boolean includeReferencedProjects, boolean async,
			IProgressMonitor monitor) {
		if (async) {
			Job job = new Job(Messages.job_persistingContentTypeIdProperties) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						runPersistContentTypeIdProperties(ExtendedPlatform.getAllFiles(project, includeReferencedProjects), monitor);
						return Status.OK_STATUS;
					} catch (OperationCanceledException ex) {
						return Status.CANCEL_STATUS;
					}
				}

				@Override
				public boolean belongsTo(Object family) {
					return IExtendedPlatformConstants.FAMILY_LONG_RUNNING.equals(family);
				}
			};
			job.setRule(createModifySchedulingRule(project));
			job.setPriority(Job.BUILD);
			job.setSystem(true);
			job.schedule();
		} else {
			try {
				runPersistContentTypeIdProperties(ExtendedPlatform.getAllFiles(project, includeReferencedProjects), monitor);
			} catch (OperationCanceledException ex) {
				// Ignore exception
			}
		}
	}

	public static void persistContentTypeIdProperties(final Collection<IFile> files, boolean async, IProgressMonitor monitor) {
		if (async) {
			Job job = new Job(Messages.job_persistingContentTypeIdProperties) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						runPersistContentTypeIdProperties(files, monitor);
						return Status.OK_STATUS;
					} catch (OperationCanceledException ex) {
						return Status.CANCEL_STATUS;
					} catch (Exception ex) {
						return StatusUtil.createErrorStatus(Activator.getDefault(), ex);
					}
				}

				@Override
				public boolean belongsTo(Object family) {
					return IExtendedPlatformConstants.FAMILY_LONG_RUNNING.equals(family);
				}
			};
			job.setRule(createModifySchedulingRule(files));
			job.setPriority(Job.BUILD);
			job.setSystem(true);
			job.schedule();
		} else {
			try {
				runPersistContentTypeIdProperties(files, monitor);
			} catch (OperationCanceledException ex) {
				// Ignore exception
			}
		}
	}

	private static void runPersistContentTypeIdProperties(Collection<IFile> files, IProgressMonitor monitor) throws OperationCanceledException {
		SubMonitor progress = SubMonitor.convert(monitor, files.size());
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		QualifiedName key = ExtendedPlatform.toQualifedName(IExtendedPlatformConstants.RESOURCE_PROPERTY_CONTENT_TYPE_ID);
		for (IFile file : files) {
			progress.setTaskName(NLS.bind(Messages.task_persistingContentTypeIdPropertiesFor, file.getFullPath().toString()));

			try {
				if (file != null && file.isAccessible()) {
					String persistentProperty = null;
					try {
						persistentProperty = file.getPersistentProperty(key);
					} catch (CoreException ex) {
						// Ignore exception
					}
					if (persistentProperty == null) {
						Object sessionProperty = file.getSessionProperty(key);
						if (sessionProperty instanceof String) {
							String contentTypeId = (String) sessionProperty;
							file.setPersistentProperty(key, contentTypeId);
						}
					}
				}
			} catch (ResourceException ex) {
				PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
			} catch (CoreException ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			}

			progress.worked(1);
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
	}

	public static void removeCachedContentTypeId(IFile file) {
		try {
			if (file != null && file.isAccessible()) {
				QualifiedName key = ExtendedPlatform.toQualifedName(IExtendedPlatformConstants.RESOURCE_PROPERTY_CONTENT_TYPE_ID);
				if (file.getSessionProperty(key) != null) {
					file.setSessionProperty(key, null);
				}
				if (file.getPersistentProperty(key) != null) {
					file.setPersistentProperty(key, null);
				}
			}
		} catch (CoreException ex) {
			// Fail silently
		}
	}

	/**
	 * Returns a set of file extensions supported by content-type with given id.
	 *
	 * @param contentTypeId
	 *            The content-type id for which the supported file extensions are to be retrieved.
	 * @return The set of file extensions supported by content-type with given id or an empty collection if no such
	 *         could be determined.
	 */
	public static Collection<String> getContentTypeFileExtensions(String contentTypeId) {
		IContentType contentType = Platform.getContentTypeManager().getContentType(contentTypeId);
		if (contentType != null) {
			return Arrays.asList(contentType.getFileSpecs(IContentTypeSettings.FILE_EXTENSION_SPEC));
		}
		return Collections.emptySet();
	}

	/**
	 * Returns wether the given content type is applicable to the passed in file. Applicability will be checked based on
	 * the filename/extension. The contents of the file will not be considered which would induce a massive performance
	 * overhead.
	 *
	 * @param contentTypeId
	 *            The identifier of the content type to check for applicability
	 * @param file
	 *            The file to check for applicability
	 * @return true if the content is applicable false if file is null or a content with the specified ID cannot be
	 *         found by the platform content type manager
	 * @see Platform#getContentTypeManager()
	 * @see IContentType#isAssociatedWith(String)
	 */
	public static boolean isContentTypeApplicable(String contentTypeId, IFile file) {
		if (file == null) {
			return false;
		}
		IContentType contentType = Platform.getContentTypeManager().getContentType(contentTypeId);
		if (contentType == null) {
			return false;
		}
		return contentType.isAssociatedWith(file.getName());
	}

	/**
	 * Converts the specified {@linkplain String name} into a {@linkplain QualifiedName qualified name}.
	 *
	 * @param name
	 *            The name to convert.
	 * @return The qualified name resulting from the conversion of the given {@link String name}.
	 * @since 0.7.0
	 */
	public static QualifiedName toQualifedName(String name) {
		QualifiedName key;
		int dot = name.lastIndexOf('.');
		if (dot != -1) {
			key = new QualifiedName(name.substring(0, dot), name.substring(dot + 1));
		} else {
			key = new QualifiedName(null, name);
		}
		return key;
	}

	/**
	 * Adds specified {@link IProjectNature nature} to given {@link IProject project}.
	 *
	 * @param project
	 *            The {@link IProject project} to be handled.
	 * @param natureId
	 *            The id of the {@link IProjectNature project nature} to be added.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 * @throws CoreException
	 *             If the {@link IProject project} does not exist or is not open.
	 */
	public static void addNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(project);
		if (natureId == null || natureId.length() == 0) {
			return;
		}
		IProjectDescription description = project.getDescription();
		String[] previousNatures = description.getNatureIds();
		for (String element : previousNatures) {
			if (natureId.equals(element)) {
				return;
			}
		}
		String[] newNatures = new String[previousNatures.length + 1];
		System.arraycopy(previousNatures, 0, newNatures, 0, previousNatures.length);
		newNatures[previousNatures.length] = natureId;
		description.setNatureIds(newNatures);
		project.setDescription(description, monitor);
	}

	/**
	 * Removes specified {@link IProjectNature nature} from given {@link IProject project}.
	 *
	 * @param project
	 *            The {@link IProject project} to be handled.
	 * @param natureId
	 *            The id of the {@link IProjectNature project nature} to be removed.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 * @throws CoreException
	 *             If the {@link IProject project} does not exist or is not open.
	 */
	public static void removeNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(project);

		IProjectDescription description = project.getDescription();
		String[] previousNatures = description.getNatureIds();
		List<String> newNatures = new ArrayList<String>(Arrays.asList(previousNatures));
		newNatures.remove(natureId);
		description.setNatureIds(newNatures.toArray(new String[newNatures.size()]));
		project.setDescription(description, monitor);
	}

	/**
	 * Closes a stream and ignores any resulting exception. This is useful when doing stream cleanup in a finally block
	 * where secondary exceptions are not worth logging.
	 */
	public static void safeClose(InputStream in) {
		try {
			if (in != null) {
				in.close();
			}
		} catch (IOException e) {
			// ignore
		}
	}

	/**
	 * Closes a stream and ignores any resulting exception. This is useful when doing stream cleanup in a finally block
	 * where secondary exceptions are not worth logging.
	 */
	public static void safeClose(OutputStream out) {
		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			// ignore
		}
	}

	/**
	 * Performs a full garbage collection to free heap memory space.
	 * <p>
	 * Runs garbage collector in a separate thread to be sure that it can reclaim heap space of objects that have been
	 * disposed in the current thread.
	 */
	public static final void performGarbageCollection() {
		Job job = new Job(Messages.job_performingGarbageCollection) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				SubMonitor progress = SubMonitor.convert(monitor, 3);

				// Asynchronous garbage collector might already run
				System.gc();
				progress.worked(1);

				// To make sure it does a full GC call it twice
				System.gc();
				progress.worked(1);

				// Let the finalizer finish its work and remove objects from its queue
				System.runFinalization();
				progress.worked(1);

				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.setSystem(true);
		job.schedule();
	}

	/**
	 * Checks wether the resource is in-sync with the file system and updates its out-of-sync marker accordingly. If the
	 * resource is out-of-sync a problem marker is created otherwise any previously existing markers are removed. This
	 * method will not synchronize the resource.
	 *
	 * @param markerJob
	 *            the marker job that will be used to asynchronously create or delete markers
	 * @param resource
	 *            the resource to check
	 * @return true if the resource is in-sync false otherwise.
	 * @see IResourceSyncMarker#RESOURCE_SYNC_PROBLEM
	 */
	public static boolean isSynchronized(IResource resource) {
		if (resource.isSynchronized(IResource.DEPTH_ZERO)) {
			// remove out-of-sync markers that may have been created previously
			MarkerJob.INSTANCE.addDeleteMarkerTask(resource, IResourceSyncMarker.RESOURCE_SYNC_PROBLEM);
			return true;
		} else {
			// create an out-of-sync marker warning the user
			MarkerJob.INSTANCE.addCreateMarkerTask(resource, IResourceSyncMarker.RESOURCE_SYNC_PROBLEM, IMarker.SEVERITY_WARNING,
					NLS.bind(Messages.warning_resourceIsOutOfSync, resource.getFullPath()));
			return false;
		}
	}
}

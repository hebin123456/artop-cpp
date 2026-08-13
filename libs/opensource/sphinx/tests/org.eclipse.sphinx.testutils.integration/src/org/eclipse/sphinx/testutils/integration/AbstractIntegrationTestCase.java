/**
 * <copyright>
 *
 * Copyright (c) 2008-2019 See4sys, itemis, BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - Improved API for importing external resources to workspace
 *     itemis - Improved handling of reference workspace path names so as to make integration tests more stable when running on different platforms
 *     BMW Car IT - Introduced waitForFamily method, refactored scheduleAndWait method
 *     itemis - Added option to run integration tests without reusing test reference workspace from previous test to avoid side effects across individual tests
 *     itemis - [414125] Enhance ResourceDeltaVisitor to enable the analysis of IFolder added/moved/removed
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.testutils.integration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.saving.SaveIndicatorUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.workspace.domain.WorkspaceEditingDomainManager;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.StatusUtil;
import org.eclipse.sphinx.testutils.TestFileAccessor;
import org.eclipse.sphinx.testutils.integration.internal.Activator;
import org.eclipse.sphinx.testutils.integration.internal.IInternalReferenceWorkspace;
import org.eclipse.sphinx.testutils.integration.internal.ReferenceEditingDomainDescriptor;
import org.eclipse.sphinx.testutils.integration.internal.ReferenceModelDescriptor;
import org.eclipse.sphinx.testutils.integration.internal.ReferenceProjectDescriptor;
import org.eclipse.sphinx.testutils.integration.internal.ReferenceWorkspaceExtractor;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public abstract class AbstractIntegrationTestCase<T extends IReferenceWorkspace> extends TestCase {

	private static final String REFERENCE_WORKSPACE_PROPERTIES_FILE_NAME = "referenceWorkspaceSourceRootDirectory.properties";

	private static final String REFERENCE_WORKSPACE_SOURCE_ROOT_DIRECTORY_PROPERTIES_KEY = "referenceWorksaceSourceDirectory";

	private static final String JOB_FAMILY_TIMEOUT = System.getProperty(AbstractIntegrationTestCase.class.getName() + ".JOB_FAMILY_TIMEOUT",
			new Integer(1 * 60 * 1000).toString());

	private Plugin testPlugin = null;

	private TestFileAccessor testFileAccessor = null;

	private IInternalReferenceWorkspace internalRefWks;

	protected T refWks;

	private Set<String> projectSubsetToLoad = null;

	private Map<String, Set<String>> projectReferences = null;

	private boolean projectsClosedOnStartup = false;

	private ModelLoadingJobTracer modelLoadJobTracer = new ModelLoadingJobTracer();

	private ResourceProblemListener resourceProblemListener = new ResourceProblemListener();

	private ReferenceWorkspaceChangeListener referenceWorkspaceChangeListener = new ReferenceWorkspaceChangeListener();

	private File referenceWorkspaceTempDir;

	private File referenceWorkspaceSourceDir;

	private boolean recycleReferenceWorkspaceOfPreviousTestRun = true;

	public AbstractIntegrationTestCase(String referenceWorkspaceTempDirBaseName) {
		String tempDirPath = System.getProperty("java.io.tmpdir");
		File tempDir = new File(tempDirPath);

		// Complement name of temporary reference workspace directory with version suffix to avoid that reference
		// workspaces originating from different development streams (i.e., platform versions) get mixed up in same
		// temporary directory
		String referenceWorkspaceTempDirName = referenceWorkspaceTempDirBaseName + "-" + Activator.getPlugin().getBundle().getVersion();

		referenceWorkspaceTempDir = new File(tempDir, referenceWorkspaceTempDirName);
		if (!referenceWorkspaceTempDir.exists()) {
			referenceWorkspaceTempDir.mkdir();
		}
	}

	protected void setRecycleReferenceWorkspaceOfPreviousTestRun(boolean recycleReferenceWorkspaceOfPreviousTestRun) {
		this.recycleReferenceWorkspaceOfPreviousTestRun = recycleReferenceWorkspaceOfPreviousTestRun;
	}

	protected Set<String> getProjectSubsetToLoad() {
		if (projectSubsetToLoad == null) {
			projectSubsetToLoad = new HashSet<String>();
		}
		return projectSubsetToLoad;
	}

	protected Map<String, Set<String>> getProjectReferences() {
		if (projectReferences == null) {
			projectReferences = new HashMap<String, Set<String>>();
		}
		return projectReferences;
	}

	protected boolean isProjectsClosedOnStartup() {
		return projectsClosedOnStartup;
	}

	protected void setProjectsClosedOnStartup(boolean projectsClosedOnStartup) {
		this.projectsClosedOnStartup = projectsClosedOnStartup;
	}

	protected Plugin getTestPlugin() {
		if (testPlugin == null) {
			testPlugin = Activator.getPlugin();
		}
		return testPlugin;
	}

	protected void setTestPlugin(Plugin testPlugin) {
		this.testPlugin = testPlugin;
	}

	protected final TestFileAccessor getTestFileAccessor() {
		if (testFileAccessor == null) {
			testFileAccessor = new TestFileAccessor(getTestPlugin(), new File("working-dir"));
		}
		return testFileAccessor;
	}

	/**
	 * Sets up the test workspace by importing the reference workspace given in parameter.
	 */
	@Override
	protected void setUp() throws Exception {
		// TODO Surround with appropriate tracing option
		// System.out.println(">>> Entering setUp()");

		Job.getJobManager().addJobChangeListener(modelLoadJobTracer);

		// HACK: Enable workspace preference Window > Preferences > General > Always run in background so as to avoid
		// excessive creation of progress dialogs by
		// org.eclipse.sphinx.emf.workspace.ui.internal.ModelLoadingProgressIndicator#aboutToRun(IJobChangeEvent) during
		// testing.
		/*
		 * !! Important Note !! The ModelLoadingProgressIndicator is there for opening a dialog which shows the progress
		 * of model loading jobs unless this has been deactivated by enabling above named workspace preference. The
		 * problem is that org.eclipse.ui.progress.IProgressService#showInDialog(Shell, Job) used for that purpose
		 * attempts to recreate a new progress dialog each time being invoked. This causes the platform to run out of
		 * SWT handles when too many of such invocations come across within too short intervals (see
		 * org.eclipse.ui.internal.progress.ProgressMonitorFocusJobDialog#show(Job, Shell) and
		 * org.eclipse.jface.dialogs.ProgressMonitorDialog#aboutToRun() for details)
		 */
		IEclipsePreferences workbenchPrefs = InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench");
		workbenchPrefs.put("RUN_IN_BACKGROUND", Boolean.TRUE.toString());

		// Delete complete or unused projects depending on whether the reference workspace is intended to be reused or
		// not
		if (!recycleReferenceWorkspaceOfPreviousTestRun) {
			WorkspaceEditingDomainManager.INSTANCE.resetEditingDomainMapping();
			assertTrue(WorkspaceEditingDomainManager.INSTANCE.getEditingDomainMapping().getEditingDomains().isEmpty());
			assertAllModelResourcesUnloaded();

			synchronizedDeleteWorkspace();

			ModelDescriptorRegistry.INSTANCE.removeModels(ResourcesPlugin.getWorkspace().getRoot());
			assertTrue(ModelDescriptorRegistry.INSTANCE.getAllModels().size() == 0);
			assertAllModelDescriptorsRemoved();
		} else {
			// Delete un-used reference projects which imported for the previous test
			synchronizedDeleteProjects(getUnusedReferenceProjects());
		}

		// Create reference workspace descriptor
		refWks = createReferenceWorkspace(getProjectSubsetToLoad());
		Assert.isTrue(refWks instanceof IInternalReferenceWorkspace);
		internalRefWks = (IInternalReferenceWorkspace) refWks;

		// Unzip reference workspace from archive if needed
		if (needToExtractReferenceWorkspaceArchive()) {
			synchronizedDeleteWorkspace();
			deleteExternalResource(referenceWorkspaceTempDir);
			extractReferenceWorkspaceArchive();
		} else {
			loadReferenceWorkspaceSourceDir();
		}

		org.eclipse.sphinx.emf.workspace.Activator.getPlugin().stopWorkspaceSynchronizing();

		synchronizedImportMissingReferenceProjectsIntoWorkspace();

		// In case previous test failed and tearDown() was not called, some projects may still be closed - so open all
		// of them to be on the safe side
		synchronizedOpenAllProjects();

		synchronizedImportMissingReferenceFilesToWorkspace();

		synchronizedCreateProjectReferences();

		// Add ResourceProblemListener on all editingDomains to listening in problems when loading/unloading resources
		internalRefWks.addResourceSetProblemListener(resourceProblemListener);

		if (isProjectsClosedOnStartup()) {
			synchronizedUnloadAllProjects();
			synchronizedCloseAllProjects();
			assertReferenceWorkspaceClosed();
		} else {
			synchronizedLoadAllProjects();
			assertReferenceWorkspaceInitialized();
		}
		assertReferenceWorkspaceConsistent();

		// Add ReferenceWorkspaceChangeListener to listening in workspace changes during the test
		internalRefWks.addReferenceWorkspaceChangeListener(referenceWorkspaceChangeListener);

		org.eclipse.sphinx.emf.workspace.Activator.getPlugin().startWorkspaceSynchronizing();
	}

	/**
	 * Tears down the test workspace by deleting all projects.
	 */
	@Override
	protected void tearDown() throws Exception {
		// TODO Surround with appropriate tracing option
		// System.out.println(">>> Entering tearDown()");

		// Remove ResourceProblemListener from all editingDomains
		internalRefWks.removeResourceSetProblemListener(resourceProblemListener);
		internalRefWks.removeReferenceWorkspaceChangeListener(referenceWorkspaceChangeListener);

		// Unload resources which have been changed but not saved yet
		synchronizedUnloadDirtyResources();

		// Projects which have been renamed will be deleted
		synchronizedDeleteProjects(referenceWorkspaceChangeListener.getRenamedProjects());

		// Open projects which has been changed during the test
		synchronizedOpenProjects(detectProjectsToOpen());

		// Delete all folders which have been added during the test, including renamed folders
		synchronizedDeleteAddedFolders();

		// Delete all files which have been added during the test, including renamed files
		synchronizedDeleteAddedFiles();

		// Delete all files which have been changed during the test
		synchronizedDeleteChangedFiles();

		// Unload resources are on memory only, these resources are not marked as dirty
		synchronizedUnloadAddedResources();

		// If existing Projects' description were changed, reset its by copying contents from reference file
		synchronizedResetProjectDescriptions();

		// If existing Projects' setting were changed, reset them by copying contents from reference file
		synchronizedResetProjectSettings();

		// Clear list of resources changed
		resourceProblemListener.clearHistory();
		referenceWorkspaceChangeListener.clearHistory();

		Job.getJobManager().removeJobChangeListener(modelLoadJobTracer);

		assertReferenceWorkspaceConsistent();
	}

	/**
	 * Detects projects that have resources changed during the test. If projects were closed in the test, we need to
	 * open it to delete changed files or reset project description and settings
	 *
	 * @return
	 */
	private Collection<IProject> detectProjectsToOpen() {
		Set<IProject> projectsToOpen = new HashSet<IProject>();
		if (!(referenceWorkspaceChangeListener.getAddedFiles().size() > 0 || referenceWorkspaceChangeListener.getChangedFiles().size() > 0
				|| referenceWorkspaceChangeListener.getProjectsWithChangedDescription().size() > 0
				|| referenceWorkspaceChangeListener.getProjectsWithChangedSettings().size() > 0)) {
			return Collections.emptySet();
		}
		for (IFile changedFile : referenceWorkspaceChangeListener.getChangedFiles()) {
			assertNotNull(changedFile);
			IProject project = changedFile.getProject();
			assertNotNull(project);
			if (project.exists() && !project.isOpen()) {
				projectsToOpen.add(project);
			}
		}
		for (IFile addedFile : referenceWorkspaceChangeListener.getAddedFiles()) {
			assertNotNull(addedFile);
			IProject project = addedFile.getProject();
			if (project.exists() && !project.isOpen()) {
				projectsToOpen.add(project);
			}
		}
		for (IProject project : referenceWorkspaceChangeListener.getProjectsWithChangedDescription()) {
			if (!(project == null || project.exists() || project.isOpen())) {
				projectsToOpen.add(project);
			}
		}
		for (IProject project : referenceWorkspaceChangeListener.getProjectsWithChangedSettings().keySet()) {
			if (!(project == null || project.exists() || project.isOpen())) {
				projectsToOpen.add(project);
			}
		}
		return projectsToOpen;

	}

	/**
	 * Unloads all resources created in the previous test. These resources were added to ResourceSets but were not saved
	 * and not marked as dirty. So we need to detect and unload them
	 */
	private void synchronizedUnloadAddedResources() {
		List<Resource> resourcesToUnload = new ArrayList<Resource>();
		for (IMetaModelDescriptor metaModelDescriptor : internalRefWks.getReferenceEditingDomainDescritpors().keySet()) {
			TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(ResourcesPlugin.getWorkspace().getRoot(),
					metaModelDescriptor);
			if (editingDomain != null) {
				Set<String> referenceFileNames = internalRefWks.getReferenceFileNames(metaModelDescriptor);
				for (Resource resource : editingDomain.getResourceSet().getResources()) {
					if (!referenceFileNames.contains(resource.getURI().lastSegment())) {
						resourcesToUnload.add(resource);
					}
				}
			}
			EcorePlatformUtil.unloadResources(editingDomain, resourcesToUnload, true, null);
			waitForModelLoading();
		}
	}

	/**
	 * Deletes files which has been changed in the test. These files will be imported again in setUp() of next test if
	 * they are required for the test.
	 *
	 * @throws Exception
	 */
	private void synchronizedDeleteChangedFiles() throws Exception {
		for (IFile changedFile : referenceWorkspaceChangeListener.getChangedFiles()) {
			if (changedFile != null && changedFile.isAccessible()) {
				synchronizedDeleteFile(changedFile);
			}
		}
	}

	/**
	 * Deletes all new files which have been added during the test.
	 *
	 * @throws Exception
	 */
	private void synchronizedDeleteAddedFiles() throws Exception {
		for (IFile addedFile : referenceWorkspaceChangeListener.getAddedFiles()) {
			if (refWks.getAllReferenceFiles().contains(addedFile)) {
				continue;
			} else if (addedFile != null && addedFile.isAccessible()) {
				synchronizedDeleteFile(addedFile);
			}
		}
	}

	/**
	 * Deletes all new folders which have been added during the test.
	 *
	 * @throws Exception
	 */
	private void synchronizedDeleteAddedFolders() throws Exception {
		for (IFolder addedFolder : referenceWorkspaceChangeListener.getAddedFolders()) {
			if (addedFolder != null && addedFolder.isAccessible()) {
				synchronizedDeleteFolder(addedFolder);
			}
		}
	}

	/**
	 * Unloads all dirty resources in editingDomains
	 */
	private void synchronizedUnloadDirtyResources() {
		for (IMetaModelDescriptor metaModelDescriptor : internalRefWks.getReferenceEditingDomainDescritpors().keySet()) {
			TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(ResourcesPlugin.getWorkspace().getRoot(),
					metaModelDescriptor);
			if (editingDomain != null) {
				Collection<Resource> dirtyResources = SaveIndicatorUtil.getDirtyResources(editingDomain);
				EcorePlatformUtil.unloadResources(editingDomain, dirtyResources, true, null);
			}
		}
		waitForModelLoading();
	}

	/**
	 * Resets description of projects which have been changed during the test by copying .project file from source
	 * reference file
	 *
	 * @throws Exception
	 */
	private void synchronizedResetProjectDescriptions() throws Exception {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				MultiStatus errorStatus = new MultiStatus(Activator.getPlugin().getSymbolicName(), IStatus.ERROR,
						"Problems encountered while resetting project descriptions.", new RuntimeException());
				for (IProject project : referenceWorkspaceChangeListener.getProjectsWithChangedDescription()) {
					try {
						if (project.exists()) {
							File projectSourceDir = new File(referenceWorkspaceSourceDir, project.getName());
							importExternalResourceToWorkspace(new File(projectSourceDir, ".project"), project);
						}
					} catch (Exception ex) {
						errorStatus.add(StatusUtil.createErrorStatus(Activator.getDefault(), ex));
					}
				}
				if (errorStatus.getChildren().length > 0) {
					throw new CoreException(errorStatus);
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	/**
	 * Resets settings of projects which have been changed during the test by copying .settings folder from source
	 * reference directory
	 *
	 * @throws Exception
	 */
	private void synchronizedResetProjectSettings() throws Exception {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				MultiStatus errorStatus = new MultiStatus(Activator.getPlugin().getSymbolicName(), IStatus.ERROR,
						"Problems encountered while resetting project settings.", new RuntimeException());
				for (IProject project : referenceWorkspaceChangeListener.getProjectsWithChangedSettings().keySet()) {
					try {
						if (project.exists()) {
							File projectSourceDir = new File(referenceWorkspaceSourceDir, project.getName());
							importExternalResourceToWorkspace(new File(projectSourceDir, ".settings"), project);
						}
					} catch (Exception ex) {
						errorStatus.add(StatusUtil.createErrorStatus(Activator.getDefault(), ex));
					}
				}
				if (errorStatus.getChildren().length > 0) {
					throw new CoreException(errorStatus);
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	/**
	 * Checks if it is necessary to unzip reference workspace from archive.
	 *
	 * @return <code>true</code> if the reference workspace archive was not already unzipped and reference workspace
	 *         source root directory path was not already saved to the referenceWorkspaceSourceRootDirectory.properties
	 *         file or if reference workspace archive is newer than existing unzipped reference workspace in temporary
	 *         directory; <code>false</code> otherwise.
	 */
	private boolean needToExtractReferenceWorkspaceArchive() throws Exception {
		java.net.URI referenceWorkspaceInputFileURI = getReferenceWorkspaceFileAccessor()
				.getInputFileURI(internalRefWks.getReferenceWorkspaceArchiveFileName(), true);
		File referenceWorkspaceArchive = null;
		if (referenceWorkspaceInputFileURI != null) {
			referenceWorkspaceArchive = new File(referenceWorkspaceInputFileURI);
		}
		File propertiesFile = new File(referenceWorkspaceTempDir, REFERENCE_WORKSPACE_PROPERTIES_FILE_NAME);

		return !referenceWorkspaceArchive.isDirectory() && (!propertiesFile.exists()
				|| referenceWorkspaceArchive != null && referenceWorkspaceArchive.lastModified() > referenceWorkspaceTempDir.lastModified());
	}

	private TestFileAccessor getReferenceWorkspaceFileAccessor() {
		return new TestFileAccessor(internalRefWks.getReferenceWorkspacePlugin());
	}

	/**
	 * Saves the reference workspace source directory to read
	 *
	 * @throws IOException
	 */
	private void saveReferenceWorkspaceSourceDir() throws IOException {
		Properties properties = new Properties();
		properties.put(REFERENCE_WORKSPACE_SOURCE_ROOT_DIRECTORY_PROPERTIES_KEY, referenceWorkspaceSourceDir.getAbsolutePath());

		File propertiesFile = new File(referenceWorkspaceTempDir, REFERENCE_WORKSPACE_PROPERTIES_FILE_NAME);
		if (!propertiesFile.exists()) {
			propertiesFile.createNewFile();
		}

		OutputStream out = null;
		try {
			out = new FileOutputStream(propertiesFile);
			properties.store(out, "Reference workspace source directory");
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	private void loadReferenceWorkspaceSourceDir() throws IOException, URISyntaxException {
		java.net.URI referenceWorkspaceInputFileURI = getReferenceWorkspaceFileAccessor()
				.getInputFileURI(internalRefWks.getReferenceWorkspaceArchiveFileName(), true);
		if (referenceWorkspaceInputFileURI != null) {
			File referenceWorkspaceFile = new File(referenceWorkspaceInputFileURI);
			if (referenceWorkspaceFile.isDirectory()) {
				referenceWorkspaceSourceDir = referenceWorkspaceFile;
			}
		} else {
			Properties properties = new Properties();
			File propertiesFile = new File(referenceWorkspaceTempDir, REFERENCE_WORKSPACE_PROPERTIES_FILE_NAME);

			InputStream in = new FileInputStream(propertiesFile);
			properties.load(in);

			String path = properties.getProperty(REFERENCE_WORKSPACE_SOURCE_ROOT_DIRECTORY_PROPERTIES_KEY);
			if (path == null) {
				throw new RuntimeException("No value for key '" + REFERENCE_WORKSPACE_SOURCE_ROOT_DIRECTORY_PROPERTIES_KEY
						+ "' found in properties file '" + propertiesFile.getAbsolutePath() + "'");
			}
			referenceWorkspaceSourceDir = new File(path);
		}
	}

	private void synchronizedImportMissingReferenceProjectsIntoWorkspace() throws Exception {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				MultiStatus errorStatus = new MultiStatus(Activator.getPlugin().getSymbolicName(), IStatus.ERROR,
						"Problems encountered while importing missing reference projects into workspace.", new RuntimeException());
				Set<IProject> missingProjects = getMissingReferenceProjects();
				for (IProject project : missingProjects) {
					try {
						importExternalResourceToWorkspace(new File(referenceWorkspaceSourceDir, project.getName()), project.getParent());
					} catch (Exception ex) {
						errorStatus.add(StatusUtil.createErrorStatus(Activator.getDefault(), ex));
					}
				}
				if (errorStatus.getChildren().length > 0) {
					throw new CoreException(errorStatus);
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	private Set<IProject> getMissingReferenceProjects() {
		Set<IProject> missingReferenceProjects = new HashSet<IProject>();
		for (ReferenceProjectDescriptor descriptor : internalRefWks.getReferenceProjectDescriptors()) {
			if (!descriptor.getProject().exists()) {
				missingReferenceProjects.add(descriptor.getProject());
			}
		}
		return missingReferenceProjects;
	}

	private Set<IProject> getUnusedReferenceProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		Set<IProject> unusedReferenceProjects = new HashSet<IProject>();
		for (IProject project : projects) {
			boolean used = false;
			for (String prjName : getProjectSubsetToLoad()) {
				if (prjName.equals(project.getName())) {
					used = true;
				}
			}
			if (!used) {
				unusedReferenceProjects.add(project);
			}
		}
		return unusedReferenceProjects;
	}

	private void synchronizedImportMissingReferenceFilesToWorkspace() throws Exception {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				MultiStatus errorStatus = new MultiStatus(Activator.getPlugin().getSymbolicName(), IStatus.ERROR,
						"Problems encountered while importing missing reference projects into workspace.", new RuntimeException());
				for (IFile missingFile : getMissingReferenceFiles()) {
					try {
						File sourceFile = referenceWorkspaceSourceDir;
						for (String segment : missingFile.getFullPath().segments()) {
							sourceFile = new File(sourceFile, segment);
						}
						importExternalResourceToWorkspace(sourceFile, missingFile.getParent());
					} catch (Exception ex) {
						errorStatus.add(StatusUtil.createErrorStatus(Activator.getDefault(), ex));
					}
				}
				if (errorStatus.getChildren().length > 0) {
					throw new CoreException(errorStatus);
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	private Set<IFile> getMissingReferenceFiles() {
		Set<IFile> missingReferenceFiles = new HashSet<IFile>();
		for (IFile file : refWks.getAllReferenceFiles()) {
			if (!file.exists()) {
				missingReferenceFiles.add(file);
			}
		}
		return missingReferenceFiles;
	}

	private void extractReferenceWorkspaceArchive() throws CoreException {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				ReferenceWorkspaceExtractor extractor = new ReferenceWorkspaceExtractor();
				extractor.extract(getReferenceWorkspaceFileAccessor(), internalRefWks.getReferenceWorkspaceArchiveFileName(),
						referenceWorkspaceTempDir);
				referenceWorkspaceSourceDir = extractor.getExtractedWorkspaceRootDirectory();

				try {
					saveReferenceWorkspaceSourceDir();
				} catch (Exception ex) {
					IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
					throw new CoreException(status);
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, null);
	}

	protected abstract T createReferenceWorkspace(Set<String> referenceProjectNames);

	private void synchronizedCreateProjectReferences() throws CoreException {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				MultiStatus errorStatus = new MultiStatus(Activator.getPlugin().getSymbolicName(), IStatus.ERROR,
						"Problems encountered while creating project references.", new RuntimeException());
				SubMonitor progress = SubMonitor.convert(monitor, ResourcesPlugin.getWorkspace().getRoot().getProjects().length);
				for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
					try {
						if (project.isAccessible()) {
							IProjectDescription projectDescription = project.getDescription();

							// Any project references configured for current project?
							if (getProjectReferences().containsKey(project.getName())) {
								// Set configured project references
								projectDescription.setReferencedProjects(toReferenceProjectArray(getProjectReferences().get(project.getName())));
							} else {
								// Remove all potentially existing project references
								projectDescription.setReferencedProjects(new IProject[0]);
							}

							project.setDescription(projectDescription, progress.newChild(1));
						}

					} catch (Exception ex) {
						errorStatus.add(StatusUtil.createErrorStatus(Activator.getDefault(), ex));
					}
				}
				if (errorStatus.getChildren().length > 0) {
					throw new CoreException(errorStatus);
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	private IProject[] toReferenceProjectArray(Set<String> referenceProjectNames) {
		Set<IProject> projects = new HashSet<IProject>(referenceProjectNames.size());
		for (String referenceProjectName : referenceProjectNames) {
			IProject referenceProject = refWks.getReferenceProject(referenceProjectName);
			if (referenceProject != null) {
				projects.add(referenceProject);
			}
		}
		return projects.toArray(new IProject[projects.size()]);
	}

	private void assertReferenceWorkspaceInitialized() throws Exception {
		assertExpectedReferenceProjectsExist();
		assertExpectedReferenceFilesExist();
		assertExpectedReferenceModelResourcesLoaded();
		assertExpectedReferenceModelDescriptorsExist();
	}

	private void assertReferenceWorkspaceConsistent() {
		assertNoEmptyModelDescriptorsExist();
		assertNoDuplicatedModelDescriptorsExist();
	}

	private void assertExpectedReferenceProjectsExist() {
		Set<IProject> missingReferenceProjects = getMissingReferenceProjects();

		if (missingReferenceProjects.size() > 0) {
			System.err.println("Missing reference project(s):");
			for (IProject project : missingReferenceProjects) {
				System.err.println("  " + project.getFullPath());
			}
		}
		assertEquals("Missing reference project(s).", 0, missingReferenceProjects.size());

	}

	private void assertExpectedReferenceFilesExist() {
		Set<IFile> missingReferenceFiles = getMissingReferenceFiles();
		if (missingReferenceFiles.size() > 0) {
			System.err.println("Missing reference file(s):");
			for (IFile file : missingReferenceFiles) {
				System.err.println("  " + file.getFullPath());
			}
		}
		assertEquals("Missing reference file(s).", 0, missingReferenceFiles.size());
	}

	private void assertExpectedReferenceModelResourcesLoaded() {
		Map<TransactionalEditingDomain, Set<URI>> missingReferenceModelResources = new HashMap<TransactionalEditingDomain, Set<URI>>();

		for (IMetaModelDescriptor metaModelDescriptor : internalRefWks.getReferenceEditingDomainDescritpors().keySet()) {
			ReferenceEditingDomainDescriptor editingDomainDescriptor = internalRefWks.getReferenceEditingDomainDescritpors().get(metaModelDescriptor);

			TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(ResourcesPlugin.getWorkspace().getRoot(),
					metaModelDescriptor);
			assertNotNull(editingDomain);
			Set<URI> actualResourceURIs = new HashSet<URI>();
			for (Resource resource : editingDomain.getResourceSet().getResources()) {
				actualResourceURIs.add(resource.getURI());
			}

			Set<URI> missingResourceURIs = new HashSet<URI>(editingDomainDescriptor.getResourceURIs());
			missingResourceURIs.removeAll(actualResourceURIs);
			if (missingResourceURIs.size() > 0) {
				missingReferenceModelResources.put(editingDomain, missingResourceURIs);
			}
		}

		if (missingReferenceModelResources.size() > 0) {
			for (TransactionalEditingDomain editingDomain : missingReferenceModelResources.keySet()) {
				System.err.println("Missing model resource(s) in editing domain '" + editingDomain.getID() + "':");
				Set<URI> missingResourceURIs = missingReferenceModelResources.get(editingDomain);

				for (URI uri : missingResourceURIs) {
					System.err.println("  " + uri.toString());
				}
				for (Resource errorResource : resourceProblemListener.getErrorResources()) {
					if (missingResourceURIs.contains(errorResource.getURI())) {
						if (errorResource.getErrors().get(0) instanceof Exception) {
							throw new WrappedException((Exception) errorResource.getErrors().get(0));
						} else {
							throw new RuntimeException(errorResource.getErrors().get(0).getMessage());
						}
					}

				}
				assertEquals("Missing model resource(s) in editing domain '" + editingDomain.getID() + "'. " + missingResourceURIs.toString(), 0,
						missingResourceURIs.size());
			}
		}
	}

	private void assertExpectedReferenceModelDescriptorsExist() {
		Map<IProject, Set<ReferenceModelDescriptor>> missingModelDescriptors = new HashMap<IProject, Set<ReferenceModelDescriptor>>();

		for (ReferenceProjectDescriptor referenceProjectDescriptor : internalRefWks.getReferenceProjectDescriptors()) {
			Set<ReferenceModelDescriptor> missingModelDescriptorsInProject = new HashSet<ReferenceModelDescriptor>();
			missingModelDescriptorsInProject.addAll(referenceProjectDescriptor.getReferenceModelDescriptors());

			for (ReferenceModelDescriptor referenceModelDescriptor : referenceProjectDescriptor.getReferenceModelDescriptors()) {
				for (IModelDescriptor modelDescriptor : ModelDescriptorRegistry.INSTANCE.getModels(referenceProjectDescriptor.getProject())) {
					if (referenceModelDescriptor.getMetaModelDescriptor().equals(modelDescriptor.getMetaModelDescriptor())
							&& referenceModelDescriptor.getEditingDomainName().equals(modelDescriptor.getEditingDomain().getID())
							&& referenceModelDescriptor.getRootProject().equals(modelDescriptor.getScope().getRoot())) {
						missingModelDescriptorsInProject.remove(referenceModelDescriptor);
						break;
					}
				}
			}

			if (missingModelDescriptorsInProject.size() > 0) {
				missingModelDescriptors.put(referenceProjectDescriptor.getProject(), missingModelDescriptorsInProject);
			}
		}

		if (missingModelDescriptors.size() > 0) {
			for (IProject project : missingModelDescriptors.keySet()) {
				System.err.println("Missing model(s) in project '" + project.getName() + "':");
				Set<ReferenceModelDescriptor> missingModelDescriptorsInProject = missingModelDescriptors.get(project);
				for (ReferenceModelDescriptor referenceModelDescriptor : missingModelDescriptorsInProject) {
					System.err.println("  " + referenceModelDescriptor);
				}
				assertEquals("Missing model(s) in project '" + project.getName() + "'.", 0, missingModelDescriptorsInProject.size());
			}
		}
	}

	private void assertNoEmptyModelDescriptorsExist() {
		Set<IModelDescriptor> emptyModelDescriptors = new HashSet<IModelDescriptor>();

		for (IModelDescriptor modelDescriptor : ModelDescriptorRegistry.INSTANCE.getAllModels()) {
			if (modelDescriptor.getPersistedFiles(true).isEmpty() && modelDescriptor.getLoadedResources(true).isEmpty()) {
				emptyModelDescriptors.add(modelDescriptor);
			}
		}

		if (!emptyModelDescriptors.isEmpty()) {
			System.err.println("Empty model(s) encountered:");
			for (IModelDescriptor emptyModelDescriptor : emptyModelDescriptors) {
				System.err.println("  " + emptyModelDescriptor);
			}
			assertEquals("Empty model(s) encountered.", 0, emptyModelDescriptors.size());
		}
	}

	private void assertNoDuplicatedModelDescriptorsExist() {
		Set<IModelDescriptor> duplicatedModelDescriptors = new HashSet<IModelDescriptor>();

		Collection<IModelDescriptor> allModels = ModelDescriptorRegistry.INSTANCE.getAllModels();
		for (IModelDescriptor modelDescriptor : allModels) {
			if (!duplicatedModelDescriptors.contains(modelDescriptor)) {
				if (Collections.frequency(allModels, modelDescriptor) > 1) {
					duplicatedModelDescriptors.add(modelDescriptor);
				}
			}
		}

		if (!duplicatedModelDescriptors.isEmpty()) {
			System.err.println("Duplicated model(s) encountered:");
			for (IModelDescriptor duplicatedModelDescriptor : duplicatedModelDescriptors) {
				System.err.println("  " + duplicatedModelDescriptor);
			}
			assertEquals("Duplicated model(s) encountered.", 0, duplicatedModelDescriptors.size());
		}
	}

	private void assertReferenceWorkspaceClosed() {
		assertAllModelDescriptorsRemoved();
		assertAllProjectsClosed();
		assertAllModelResourcesUnloaded();
	}

	private void assertAllModelDescriptorsRemoved() {
		assertWorkspaceModelsSizeEquals(0);
	}

	private void assertAllModelResourcesUnloaded() {
		for (TransactionalEditingDomain editingDomain : WorkspaceEditingDomainUtil.getAllEditingDomains()) {
			assertEditingDomainResourcesSizeEquals(editingDomain, 0);
		}
	}

	private void assertAllProjectsClosed() {
		Set<IProject> openProjects = new HashSet<IProject>();
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (project.isOpen()) {
				openProjects.add(project);
			}
		}
		if (openProjects.size() > 0) {
			System.err.println("Unexpected number of open project(s):");
			for (IProject project : openProjects) {
				System.err.println("  " + project.getFullPath());
			}
		}
		assertTrue("Unexpected number of open project(s).", openProjects.size() == 0);
	}

	protected void assertEditingDomainResourcesSizeEquals(TransactionalEditingDomain editingDomain, int expected) {
		assertNotNull(editingDomain);

		EList<Resource> resources = editingDomain.getResourceSet().getResources();
		if (expected == 0 && resources.size() > 0) {
			System.err.println("Unexpected number of resources in editingDomain " + editingDomain.getID() + ":");
			for (Resource resource : resources) {
				System.err.println("  " + resource.getURI());
			}
		}
		assertEquals("Unexpected number of resources in editingDomain " + editingDomain.getID(), expected, resources.size());
	}

	protected void assertEditingDomainDoesNotContainResource(TransactionalEditingDomain editingDomain, Resource resource) {
		assertNotNull(editingDomain);
		assertFalse(editingDomain.getResourceSet().getResources().contains(resource));
	}

	protected void assertEditingDomainContainsResource(TransactionalEditingDomain editingDomain, Resource resource) {
		assertNotNull(editingDomain);
		assertTrue(editingDomain.getResourceSet().getResources().contains(resource));
	}

	protected void assertEditingDomainContainsResources(TransactionalEditingDomain editingDomain, Collection<Resource> resources) {
		assertNotNull(resources);
		for (Resource resource : resources) {
			assertEditingDomainContainsResource(editingDomain, resource);
		}
	}

	protected void assertEditingDomainDoesNotContainResources(TransactionalEditingDomain editingDomain, Collection<Resource> resources) {
		if (resources != null && resources.size() > 0) {
			for (Resource resource : resources) {
				assertEditingDomainDoesNotContainResource(editingDomain, resource);
			}
		}
	}

	// FIXME Consider to rely on URI rather than just resource name
	protected void assertEditingDomainContainsResource(TransactionalEditingDomain editingDomain, String resourceName) {
		assertNotNull(editingDomain);
		assertNotNull(resourceName);
		for (Resource res : editingDomain.getResourceSet().getResources()) {
			if (resourceName.equals(res.getURI().lastSegment())) {
				assertTrue(true);
				return;
			}

		}
		assertTrue("Editing domain " + editingDomain.getID() + " does not contain resource named '" + resourceName + "'.", false);
	}

	// FIXME Consider to rely on URI rather than just resource name
	protected void assertEditingDomainDoesNotContainResource(TransactionalEditingDomain editingDomain, String resourceName) {
		if (resourceName == null || editingDomain == null) {
			assertTrue(true);
			return;
		}
		for (Resource res : editingDomain.getResourceSet().getResources()) {
			if (resourceName.equals(res.getURI().lastSegment())) {
				assertTrue(false);
			}

		}
		assertTrue(true);
	}

	// FIXME Consider to rely on URI rather than just resource name
	protected void assertEditingDomainContainsNamedResources(TransactionalEditingDomain editingDomain, Collection<String> resourceNames) {
		assertNotNull(resourceNames);
		if (resourceNames.size() == 0) {
			assertTrue(false);
			return;
		}
		for (String resourceName : resourceNames) {
			assertEditingDomainContainsResource(editingDomain, resourceName);
		}

	}

	// FIXME Consider to rely on URI rather than just resource name
	protected void assertEditingDomainDoesNotContainNamedResources(TransactionalEditingDomain editingDomain, Collection<String> resourceNames) {
		if (resourceNames == null) {
			assertTrue(true);
			return;
		}
		if (resourceNames.size() == 0) {
			assertTrue(true);
			return;
		}
		for (String resourceName : resourceNames) {
			assertEditingDomainDoesNotContainResource(editingDomain, resourceName);
		}
	}

	// FIXME Consider to rely on IProject rather than just project name
	protected void assertReferenceProjectAllResourcesLoaded(String projectName) {
		ReferenceProjectDescriptor referenceProjectDescriptor = internalRefWks.getReferenceProjectDescriptor(projectName);
		assertNotNull("Project " + projectName + " does not exist in test reference workspace", referenceProjectDescriptor);
		Map<IMetaModelDescriptor, Set<IFile>> files = referenceProjectDescriptor.getFiles();
		if (files != null) {
			for (IMetaModelDescriptor metaModelDescriptor : files.keySet()) {
				if (metaModelDescriptor != MetaModelDescriptorRegistry.NO_MM) {
					assertReferenceProjectResourcesLoaded(metaModelDescriptor, projectName);
				}
			}
		}

	}

	// FIXME Consider to rely on IProject rather than just project name
	protected void assertReferenceProjectAllResourcesNotLoaded(String projectName) {
		ReferenceProjectDescriptor referenceProjectDescriptor = internalRefWks.getReferenceProjectDescriptor(projectName);
		assertNotNull("Project " + projectName + " does not exist in test reference workspace", referenceProjectDescriptor);
		Map<IMetaModelDescriptor, Set<IFile>> files = referenceProjectDescriptor.getFiles();
		if (files != null) {
			for (IMetaModelDescriptor metaModelDescriptor : files.keySet()) {
				if (metaModelDescriptor != MetaModelDescriptorRegistry.NO_MM) {
					assertReferenceProjectResourcesNotLoaded(metaModelDescriptor, projectName);
				}
			}
		}
	}

	// FIXME Consider to rely on IProject rather than just project name
	protected void assertReferenceProjectResourcesLoaded(IMetaModelDescriptor metaModelDescriptor, String projectName) {
		ReferenceProjectDescriptor referenceProjectDescriptor = internalRefWks.getReferenceProjectDescriptor(projectName);
		assertNotNull("Project " + projectName + " does not exist in test reference workspace", referenceProjectDescriptor);
		assertTrue(referenceProjectDescriptor.getProject().exists());
		List<String> filesNames = referenceProjectDescriptor.getFileNames(metaModelDescriptor);
		if (internalRefWks.getReferenceEditingDomainDescriptor(metaModelDescriptor) == null) {
			assertTrue("No such resources in editing domain descriptor", false);
		}
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(ResourcesPlugin.getWorkspace().getRoot(),
				metaModelDescriptor);
		assertNotNull(editingDomain);
		assertEditingDomainContainsNamedResources(editingDomain, filesNames);
	}

	// FIXME Consider to rely on IProject rather than just project name
	protected void assertReferenceProjectResourcesNotLoaded(IMetaModelDescriptor metaModelDescriptor, String projectName) {
		ReferenceProjectDescriptor referenceProjectDescriptor = internalRefWks.getReferenceProjectDescriptor(projectName);
		assertNotNull(referenceProjectDescriptor);
		List<String> filesNames = referenceProjectDescriptor.getFileNames(metaModelDescriptor);
		if (internalRefWks.getReferenceEditingDomainDescriptor(metaModelDescriptor) == null) {
			assertTrue(true);
			return;
		}
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(ResourcesPlugin.getWorkspace().getRoot(),
				metaModelDescriptor);
		assertNotNull(editingDomain);
		assertEditingDomainDoesNotContainNamedResources(editingDomain, filesNames);
	}

	protected void assertWorkspaceModelsSizeEquals(int expected) {
		Collection<IModelDescriptor> modelDescriptors = ModelDescriptorRegistry.INSTANCE.getModels(ResourcesPlugin.getWorkspace().getRoot());
		if (expected == 0 && modelDescriptors.size() > 0) {
			System.err.println("Unexpected number of model descriptor(s) in workspace:");
			for (IModelDescriptor modelDescriptor : modelDescriptors) {
				System.err.println("  " + modelDescriptor);
			}
		}
		assertEquals("Unexpected number of model descriptors in workspace.", expected, modelDescriptors.size());
	}

	protected void assertProjectModelsSizeEquals(IProject project, int expected) {
		Collection<IModelDescriptor> modelDescriptors = ModelDescriptorRegistry.INSTANCE.getModels(project);
		if (expected == 0 && modelDescriptors.size() > 0) {
			System.err.println("Unexpected number of model descriptor(s) in project '" + project.getName() + "':");
			for (IModelDescriptor modelDescriptor : modelDescriptors) {
				System.err.println("  " + modelDescriptor);
			}
		}
		assertEquals("Unexpected number of model descriptor(s) in project '" + project.getName() + "'.", expected, modelDescriptors.size());
	}

	protected void assertProjectHasModels(IProject project, IMetaModelDescriptor metaModelDescriptor) {
		Collection<IModelDescriptor> models = ModelDescriptorRegistry.INSTANCE.getModels(project, metaModelDescriptor);
		assertTrue("Missing '" + metaModelDescriptor + "' model descriptor in project '" + project.getName() + "'.", !models.isEmpty());
	}

	protected void assertProjectHasNoModels(IProject project, IMetaModelDescriptor metaModelDescriptor) {
		Collection<IModelDescriptor> models = ModelDescriptorRegistry.INSTANCE.getModels(project, metaModelDescriptor);
		assertTrue("Unexpected '" + metaModelDescriptor + "' model descriptor in project '" + project.getName() + "'.", models.isEmpty());
	}

	protected void synchronizedLoadFile(IFile file) {
		ModelLoadManager.INSTANCE.loadFile(file, true, null);
		waitForModelLoading();
	}

	protected void synchronizedUnloadFile(IFile file) {
		ModelLoadManager.INSTANCE.unloadFile(file, true, null);
		waitForModelLoading();
	}

	protected void synchronizedDeleteFile(final IFile file) throws CoreException {
		assertNotNull(file);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				file.delete(true, true, monitor);
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	protected void synchronizedMoveFile(final IFile file, final IPath target) throws CoreException {
		assertNotNull(file);
		assertNotNull(target);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				file.move(target, true, new NullProgressMonitor());
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	protected void synchronizedRenameFile(final IFile file, final String newname) throws CoreException {
		assertNotNull(file);
		assertNotNull(newname);
		assertFalse(newname.length() == 0);

		final IPath newPath = file.getFullPath().removeLastSegments(1).append(newname);
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				file.move(newPath, true, new NullProgressMonitor());
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	protected void synchronizedSetFileContents(final IFile file, final InputStream source) throws CoreException {
		assertNotNull(source);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				file.setContents(source, true, true, monitor);
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
		waitForModelLoading();
	}

	/**
	 * Deletes external file or directory with specified <code>path</code>. If external resource is a directory then all
	 * files and subdirectories under this directory are deleted. If it is a file then only the file is deleted.
	 *
	 * @param path
	 *            The path of the file or directory to be deleted.
	 * @throws IOException
	 *             If a deletion of some file or directory fails; no attempts to delete remaining files or directories
	 *             are made in this case.
	 */
	protected void deleteExternalResource(String path) throws IOException {
		URI uri = URI.createURI(path, true);
		URI fileURI = EcoreResourceUtil.convertToAbsoluteFileURI(uri);
		File file = new File(fileURI.toFileString());
		deleteExternalResource(file);
	}

	/**
	 * Deletes specified external <code>file</code>. If external file is a directory then all files and subdirectories
	 * under this directory are deleted. If it is a file then only the file is deleted.
	 *
	 * @param path
	 *            The file or directory to be deleted.
	 * @throws IOException
	 *             If a deletion of some file or directory fails; no attempts to delete remaining files or directories
	 *             are made in this case.
	 */
	protected void deleteExternalResource(File file) throws IOException {
		Assert.isNotNull(file);

		if (file.isDirectory()) {
			String[] children = file.list();
			// Delete the files and directories in directory to be deleted
			for (String child : children) {
				deleteExternalResource(new File(file, child));
			}

			// The directory is now empty so delete it
			if (!file.delete()) {
				throw new IOException("Unable to delete external directory: '" + file.getPath() + "'");
			}
		} else {
			if (file.exists()) {
				if (!file.delete()) {
					throw new IOException("Unable to delete external file: '" + file.getPath() + "'");
				}
			}
		}
	}

	protected void synchronizedDeleteFolder(final IFolder folder) throws Exception {
		assertNotNull(folder);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				folder.delete(true, true, monitor);
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	protected void synchronizedMoveFolder(final IFolder folder, final IPath target) throws CoreException {
		assertNotNull(folder);
		assertNotNull(target);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				folder.move(target, true, new NullProgressMonitor());
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	protected void synchronizedLoadProject(IProject project, boolean includeReferencedProjects) {
		ModelLoadManager.INSTANCE.loadProject(project, includeReferencedProjects, true, null);
		waitForModelLoading();
	}

	protected void synchronizedLoadAllProjects() {
		ModelLoadManager.INSTANCE.loadProjects(Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects()), true, true, null);
		waitForModelLoading();
	}

	protected void synchronizedUnloadProject(IProject project, boolean includeReferencedProjects) {
		ModelLoadManager.INSTANCE.unloadProject(project, includeReferencedProjects, true, null);
		waitForModelLoading();
	}

	protected void synchronizedUnloadProjects(Collection<IProject> projectsToUnload, boolean includeReferenceProjects) {
		ModelLoadManager.INSTANCE.unloadProjects(projectsToUnload, includeReferenceProjects, true, null);
		waitForModelLoading();
	}

	protected void synchronizedUnloadAllProjects() {
		ModelLoadManager.INSTANCE.unloadProjects(Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects()), false, true, null);
		waitForModelLoading();
	}

	protected void synchronizedOpenProject(final IProject project) throws Exception {
		assertNotNull(project);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				project.open(monitor);
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	protected void synchronizedOpenProjects(final Collection<IProject> projects) throws Exception {
		assertNotNull(projects);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				for (IProject project : projects) {
					project.open(monitor);
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	protected void synchronizedOpenAllProjects() throws Exception {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				MultiStatus errorStatus = new MultiStatus(Activator.getPlugin().getSymbolicName(), IStatus.ERROR,
						"Problems encountered while opening projects.", new RuntimeException());
				SubMonitor progress = SubMonitor.convert(monitor, ResourcesPlugin.getWorkspace().getRoot().getProjects().length);
				for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
					try {
						project.open(progress.newChild(1));
					} catch (CoreException ex) {
						errorStatus.add(ex.getStatus());
					}
				}
				if (errorStatus.getChildren().length > 0) {
					throw new CoreException(errorStatus);
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	protected void synchronizedRenameProject(final IProject project, final String newName) throws Exception {
		assertNotNull(project);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				project.move(project.getFullPath().removeLastSegments(1).append(newName), true, monitor);
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	protected void synchronizedCloseProject(final IProject project) throws Exception {
		assertNotNull(project);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				project.close(monitor);
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	protected void synchronizedCloseAllProjects() throws Exception {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				MultiStatus errorStatus = new MultiStatus(Activator.getPlugin().getSymbolicName(), IStatus.ERROR,
						"Problems encountered while closing projects.", new RuntimeException());
				Collection<IProject> safeProjects = new ArrayList<IProject>(Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects()));
				SubMonitor progress = SubMonitor.convert(monitor, safeProjects.size());
				for (IProject project : safeProjects) {
					try {
						project.close(progress.newChild(1));
					} catch (CoreException ex) {
						errorStatus.add(ex.getStatus());
					}
				}
				if (errorStatus.getChildren().length > 0) {
					throw new CoreException(errorStatus);
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	protected void synchronizedDeleteProject(final IProject project) throws Exception {
		assertNotNull(project);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				safeDeleteProject(project, monitor);
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	protected void synchronizedDeleteProjects(final Collection<IProject> projects) throws Exception {
		assertNotNull(projects);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				MultiStatus errorStatus = new MultiStatus(Activator.getPlugin().getSymbolicName(), IStatus.ERROR,
						"Problems encountered while closing projects.", new RuntimeException());
				SubMonitor progress = SubMonitor.convert(monitor, projects.size());
				for (IProject project : projects) {
					try {
						safeDeleteProject(project, progress.newChild(1));
					} catch (CoreException ex) {
						errorStatus.add(ex.getStatus());
					}
				}
				if (errorStatus.getChildren().length > 0) {
					throw new CoreException(errorStatus);
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	private void safeDeleteProject(IProject project, IProgressMonitor monitor) throws CoreException {
		/*
		 * !! Important Note !! There may be processes in different threads that periodically lock files in the project
		 * under deletion (e.g. Java Indexing). This lets project deletions sporadically end up in CoreExceptions with
		 * the message "Problems encountered while deleting files.". Since it's just a test, we simply try to repeat the
		 * project deletion a couple times with a random wait of a second or two in between attempts. This should
		 * normally reduce the rate of failures down to "good enough".
		 */
		for (int i = 0; i < 5; i++) {
			try {
				project.delete(true, true, monitor);
				return;
			} catch (CoreException ex) {
				if (i == 4) {
					throw ex;
				} else {
					try {
						Thread.sleep(500 + Math.round(1500 * Math.random()));
					} catch (InterruptedException iex) {
						// Ignore exception
					}
				}
			}
		}
	}

	protected void synchronizedDeleteWorkspace() throws CoreException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				workspace.getRoot().delete(true, true, null);
			}
		};
		workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
		workspace.checkpoint(false);
		waitForModelLoading();
	}

	protected Resource getProjectResource(IProject project, String fileName) {
		assertNotNull(project);
		assertNotNull(fileName);
		IFile file = project.getFile(fileName);
		return EcorePlatformUtil.getResource(file);
	}

	protected Collection<IFile> getAllModelFiles() {
		Collection<IFile> allFiles = new HashSet<IFile>();
		for (IProject project : ExtendedPlatform.getRootProjects()) {
			for (IFile file : ExtendedPlatform.getAllFiles(project, true)) {
				final String fileName = file.getName();
				final String fileExtension = file.getFileExtension();
				if (!fileName.startsWith(".") && !hasDotParent(file) && !fileExtension.equals("java") && !fileExtension.equals("xtend")
						&& !fileExtension.equals("_trace") && !fileExtension.equals("class") && !fileExtension.equals("MF")
						&& !fileName.equals("pom.xml") && !fileName.equals("plugin.xml") && !fileName.equals("plugin.properties")
						&& !fileName.equals("build.properties")) {
					allFiles.add(file);
				}
			}
		}
		return allFiles;
	}

	private boolean hasDotParent(IResource resource) {
		assertNotNull(resource);
		IContainer parent = resource.getParent();
		if (parent != null) {
			if (parent.getName().startsWith(".")) {
				return true;
			}
			return hasDotParent(parent);
		}
		return false;
	}

	protected void assertProxiesResolved(Resource inputResource) {
		assertNotNull(inputResource);
		for (TreeIterator<EObject> allContents = inputResource.getAllContents(); allContents.hasNext();) {
			EObject object = allContents.next();
			assertFalse(object.eIsProxy());
		}
	}

	/**
	 * Blocks the calling thread until model loading completes.
	 */
	protected void waitForModelLoading() {
		waitForFamily(IExtendedPlatformConstants.FAMILY_MODEL_LOADING);
	}

	/**
	 * Blocks the calling thread until model loading completes.
	 */
	protected void waitForModelSaving() {
		waitForFamily(IExtendedPlatformConstants.FAMILY_MODEL_SAVING);
	}

	/**
	 * Blocks the calling thread until automatic build completes.
	 */
	protected void waitForAutoBuild() {
		waitForFamily(ResourcesPlugin.FAMILY_AUTO_BUILD);
	}

	/**
	 * Blocks the calling thread until the specified job family completes.
	 */
	private void waitForFamily(final Object family) {
		// wait in a separate thread to realize a timeout to avoid potentially waiting forever
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					Job.getJobManager().join(family, null);
				} catch (Exception ex) {
					// Ignore exception
				}
			}
		};

		t.setDaemon(true);
		t.start();

		try {
			t.join(Integer.parseInt(JOB_FAMILY_TIMEOUT));
		} catch (InterruptedException ex) {
			// Ignore exception
		}

		if (t.isAlive()) {
			throw new RuntimeException("Timeout while waiting for job family '" + family + "'.");
		}
	}

	private class ModelLoadingJobTracer extends JobChangeAdapter {

		private boolean isModelLoadingJob(IJobChangeEvent event) {
			if (event != null) {
				Job job = event.getJob();
				if (job != null) {
					return job.belongsTo(IExtendedPlatformConstants.FAMILY_MODEL_LOADING);
				}
			}
			return false;
		}

		@Override
		public void scheduled(IJobChangeEvent event) {
			if (isModelLoadingJob(event)) {
				// TODO Surround with appropriate tracing option
				// System.out.println("Model loading job '" + event.getJob().getName() + "' -> SCHEDULED");
			}
		}

		@Override
		public void running(IJobChangeEvent event) {
			if (isModelLoadingJob(event)) {
				// TODO Surround with appropriate tracing option
				// System.out.println("Model loading job '" + event.getJob().getName() + "' -> RUNNING");
			}
		}

		@Override
		public void done(IJobChangeEvent event) {
			if (isModelLoadingJob(event)) {
				// TODO Surround with appropriate tracing option
				// System.out.println("Model loading job '" + event.getJob().getName() + "' -> DONE");
			}
		}
	}

	protected void importExternalResourceToWorkspace(File externalResource, IContainer targetContainer) throws Exception {
		assertNotNull(externalResource);
		assertNotNull(targetContainer);

		if (externalResource.exists()) {
			if (externalResource.isDirectory()) {
				// Copy files and folders inside external directory to target container
				IContainer importedContainer;
				if (targetContainer instanceof IWorkspaceRoot) {
					importedContainer = ((IWorkspaceRoot) targetContainer).getProject(externalResource.getName());
				} else {
					importedContainer = targetContainer.getFolder(new Path(externalResource.getName()));
				}
				for (File file : externalResource.listFiles()) {
					importExternalResourceToWorkspace(file, importedContainer);
				}
			} else {
				// Make sure that target container exists
				createContainerTree(targetContainer);

				// Copy external file to target container
				IFile importedFile = targetContainer.getFile(new Path(externalResource.getName()));
				InputStream externalFileContents = new FileInputStream(externalResource);
				if (importedFile.exists()) {
					// Overwrite old file's contents
					importedFile.setCharset(null, null);
					importedFile.setContents(externalFileContents, true, false, null);
				} else {
					// Create new file in target container
					try {
						importedFile.create(externalFileContents, true, null);
						importedFile.setCharset(null, null);
					} catch (Exception ex) {
						// Exception complaining that imported file already exists?
						if (ex.getMessage().contains("already exists")) {
							// Overwrite existing file's contents
							externalFileContents = new FileInputStream(externalResource);
							importedFile.setCharset(null, null);
							importedFile.setContents(externalFileContents, true, false, null);
						} else {
							throw ex;
						}
					}
				}

				importedFile.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
			}
		}
	}

	/**
	 * Creates project and/or any missing folders on path of given container.
	 *
	 * @param container
	 *            The container to be processed.
	 * @throws Exception
	 */
	private void createContainerTree(IContainer container) throws Exception {
		Assert.isNotNull(container);

		IProject project = container.getProject();
		if (!project.exists()) {
			project.create(null);
			project.open(null);
		} else if (!project.isAccessible()) {
			project.open(null);
		}

		if (container.getFullPath().segmentCount() > 1) {
			IContainer parentContainer = project;
			for (int i = 1; i < container.getFullPath().segmentCount(); i++) {
				IFolder folder = parentContainer.getFolder(new Path(container.getFullPath().segment(i)));
				if (!folder.exists()) {
					try {
						folder.create(true, true, null);
					} catch (Exception ex) {
						if (ex.getMessage().contains("already exists")) {
							// Do nothing
						} else {
							throw ex;
						}
					}
				}
				parentContainer = folder;
			}
		}
	}

	protected void synchronizedImportExternalResourceToWorkspace(final File externalResource, final IContainer targetContainer) throws Exception {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					importExternalResourceToWorkspace(externalResource, targetContainer);
				} catch (Exception ex) {
					IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
					throw new CoreException(status);
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().checkpoint(false);
		waitForModelLoading();
	}

	protected void synchronizedImportInputFileToWorkspace(String inputFileName, IContainer targetContainer) throws Exception {
		java.net.URI inputFileURI = getTestFileAccessor().getInputFileURI(inputFileName, true);
		synchronizedImportExternalResourceToWorkspace(new File(inputFileURI), targetContainer);
	}

	protected ReferenceWorkspaceChangeListener getReferenceWorkspaceChangeListener() {
		return referenceWorkspaceChangeListener;
	}
}

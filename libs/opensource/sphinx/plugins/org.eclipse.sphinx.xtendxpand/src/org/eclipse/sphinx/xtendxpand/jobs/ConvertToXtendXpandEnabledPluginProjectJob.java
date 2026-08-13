/**
 * <copyright>
 *
 * Copyright (c) 2011 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [358082] Precedence of Xtend MetaModels gets lost in Xtend/Xpand runtime enhancements implemented in Sphinx
 *     itemis - Revised implementation (redesigned overriding points and getters/setters, improved of naming, fixed progress monitor issues)
 *     See4sys - Replaced ConvertProjectToPluginOperation with ConvertProjectToPluginProjectJob and
 *               moved from org.eclipse.sphinx.xtendxpand.ui.jobs to org.eclipse.sphinx.xtendxpand.jobs
 *     itemis - [405696] Create separate plug-in for PDE dependencies
 *     itemis - [445101] Add the org.eclipse.sphinx.jdt plug-in
 *     itemis - [445125] Rework the org.eclipse.sphinx.xtendxpand.jobs.ConvertToXtendXpandEnabledPluginProjectJob job
 *
 * </copyright>
 */
package org.eclipse.sphinx.xtendxpand.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.sphinx.jdt.jobs.ConvertProjectToJavaProjectJob;
import org.eclipse.sphinx.jdt.util.JavaExtensions;
import org.eclipse.sphinx.pde.jobs.ConvertProjectToPluginProjectJob;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.StatusUtil;
import org.eclipse.sphinx.xtendxpand.internal.Activator;
import org.eclipse.sphinx.xtendxpand.internal.messages.Messages;
import org.eclipse.sphinx.xtendxpand.util.XtendXpandUtil;

/**
 * A {@link WorkspaceJob workspace job} that supports conversion of {@link IProject project}s to an Xtend/Xpand-enabled
 * plug-in project.
 */
public class ConvertToXtendXpandEnabledPluginProjectJob extends WorkspaceJob {

	private static final String PROJECT_RELATIVE_JAVA_SOURCE_DEFAULT_PATH = "src"; //$NON-NLS-1$

	private static final String JAVA_EXTENSIONS_PACKAGE_DEFAULT_NAME = "extensions"; //$NON-NLS-1$

	private static final List<String> PDE_DEFAULT_REQUIRED_BUNDLES_IDS = Arrays.asList(new String[] { "org.eclipse.xtend.util.stdlib" }); //$NON-NLS-1$

	private static final String PDE_EXECUTION_ENVIRONMENT_J2SE_15 = "J2SE-1.5"; //$NON-NLS-1$
	private static final String PDE_EXECUTION_ENVIRONMENT_JavaSE_16 = "JavaSE-1.6"; //$NON-NLS-1$
	private static final String PDE_EXECUTION_ENVIRONMENT_JavaSE_17 = "JavaSE-1.8"; //$NON-NLS-1$
	private static final String PDE_EXECUTION_ENVIRONMENT_JavaSE_18 = "JavaSE-1.8"; //$NON-NLS-1$

	private IProject project;
	private String projectRelativeJavaSourcePath = PROJECT_RELATIVE_JAVA_SOURCE_DEFAULT_PATH;
	private String compilerCompliance;
	private String javaExtensionsPackageName;
	private List<String> enabledMetaModelContributorTypeNames = null;

	public ConvertToXtendXpandEnabledPluginProjectJob(String name, IProject project) {
		super(name);

		this.project = project;
		compilerCompliance = JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE);
		javaExtensionsPackageName = project.getName().toLowerCase() + "." + JAVA_EXTENSIONS_PACKAGE_DEFAULT_NAME; //$NON-NLS-1$

		setPriority(Job.BUILD);
		setRule(ResourcesPlugin.getWorkspace().getRoot());
	}

	public String getProjectRelativeJavaSourcePath() {
		return projectRelativeJavaSourcePath;
	}

	public void setProjectRelativeJavaSourcePath(String projectRelativeJavaSourcePath) {
		this.projectRelativeJavaSourcePath = projectRelativeJavaSourcePath;
	}

	public String getCompilerCompliance() {
		return compilerCompliance;
	}

	public void setCompilerCompliance(String compilerCompliance) {
		JavaExtensions.validateCompilerCompliance(compilerCompliance);
		this.compilerCompliance = compilerCompliance;
	}

	public String getJavaExtensionsPackageName() {
		return javaExtensionsPackageName;
	}

	public void setJavaExtensionsPackageName(String javaExtensionsPackageName) {
		this.javaExtensionsPackageName = javaExtensionsPackageName;
	}

	public List<String> getEnabledMetamodelContributorTypeNames() {
		if (enabledMetaModelContributorTypeNames == null) {
			enabledMetaModelContributorTypeNames = new ArrayList<String>();
		}
		return enabledMetaModelContributorTypeNames;
	}

	/*
	 * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, Messages.task_ConvertingToXtendXpandEnabledPluginProject, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		try {
			// Convert project to Java project
			convertToJavaProject(progress.newChild(30));

			// Convert project to plug-in project
			convertToPluginProject(progress.newChild(35));

			// Convert to Xtend/Xpand project
			convertToXtendXpandProject(progress.newChild(35));
		} catch (OperationCanceledException ex) {
			return Status.CANCEL_STATUS;
		} catch (Exception ex) {
			return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
		}
		return Status.OK_STATUS;
	}

	protected void convertToJavaProject(IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, Messages.task_ConvertingToJavaProject, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		try {
			ConvertProjectToJavaProjectJob convertProjectToJavaProjectJob = new ConvertProjectToJavaProjectJob(project);
			convertProjectToJavaProjectJob.runInWorkspace(progress);
		} catch (Exception ex) {
			IStatus status = StatusUtil.createErrorStatus(Activator.getDefault(), ex);
			throw new CoreException(status);
		}
	}

	protected void convertToPluginProject(IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, Messages.task_ConvertingToPluginProject, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		// Convert to plug-in project
		try {
			ConvertProjectToPluginProjectJob convertProjectToPluginOperation = new ConvertProjectToPluginProjectJob(
					Collections.singletonList(project), getRequiredBundleIds(), getRequiredExecutionEnvironment());
			convertProjectToPluginOperation.runInWorkspace(progress);
		} catch (Exception ex) {
			IStatus status = StatusUtil.createErrorStatus(Activator.getDefault(), ex);
			throw new CoreException(status);
		}
	}

	protected void convertToXtendXpandProject(IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, Messages.task_ConvertingToXtendXpandProject, 3);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		// Add Xtend/Xpand nature
		if (!project.hasNature(XtendXpandUtil.XTEND_XPAND_NATURE_ID)) {
			ExtendedPlatform.addNature(project, XtendXpandUtil.XTEND_XPAND_NATURE_ID, progress.newChild(1));
		}

		// Set enabled Xtend metamodel contributors
		if (!getEnabledMetamodelContributorTypeNames().isEmpty()) {
			StringBuilder enabledMetaModelContributorTypeNamesStr = new StringBuilder();
			for (Iterator<String> iter = getEnabledMetamodelContributorTypeNames().iterator(); iter.hasNext();) {
				String contributorTypeName = iter.next();
				enabledMetaModelContributorTypeNamesStr.append(contributorTypeName);
				if (iter.hasNext()) {
					enabledMetaModelContributorTypeNamesStr.append(","); //$NON-NLS-1$
				}
			}

			IEclipsePreferences prefs = new ProjectScope(project).getNode(XtendXpandUtil.XTEND_SHARED_UI_PLUGIN_ID);
			prefs.put(XtendXpandUtil.PREFERENCE_KEY_PROJECT_SPECIFIC_METAMODEL, Boolean.TRUE.toString());
			prefs.put(XtendXpandUtil.PREFERENCE_KEY_METAMODEL_CONTRIBUTOR, enabledMetaModelContributorTypeNamesStr.toString());
			try {
				prefs.flush();
			} catch (Exception ex) {
				IStatus status = StatusUtil.createErrorStatus(Activator.getDefault(), ex);
				throw new CoreException(status);
			}
		}
		progress.worked(1);

		// Add Java package for Java-based Xtend extensions
		IJavaProject javaProject = JavaCore.create(project);
		IPackageFragmentRoot rootPackageFragmentRoot = javaProject.getPackageFragmentRoot(project.getFolder(getProjectRelativeJavaSourcePath()));
		rootPackageFragmentRoot.createPackageFragment(getJavaExtensionsPackageName(), true, progress.newChild(1));
	}

	protected List<String> getRequiredBundleIds() {
		return new ArrayList<String>(PDE_DEFAULT_REQUIRED_BUNDLES_IDS);
	}

	protected String getRequiredExecutionEnvironment() {
		String requiredExecutionEnvironment = null;
		String compilerCompliance = getCompilerCompliance();
		if (JavaCore.VERSION_1_5.equals(compilerCompliance)) {
			requiredExecutionEnvironment = PDE_EXECUTION_ENVIRONMENT_J2SE_15;
		} else if (JavaCore.VERSION_1_6.equals(compilerCompliance)) {
			requiredExecutionEnvironment = PDE_EXECUTION_ENVIRONMENT_JavaSE_16;
		} else if (JavaCore.VERSION_1_7.equals(compilerCompliance)) {
			requiredExecutionEnvironment = PDE_EXECUTION_ENVIRONMENT_JavaSE_17;
		} else if (JavaExtensions.VERSION_1_8.equals(compilerCompliance)) {
			requiredExecutionEnvironment = PDE_EXECUTION_ENVIRONMENT_JavaSE_18;
		}
		return requiredExecutionEnvironment;
	}
}

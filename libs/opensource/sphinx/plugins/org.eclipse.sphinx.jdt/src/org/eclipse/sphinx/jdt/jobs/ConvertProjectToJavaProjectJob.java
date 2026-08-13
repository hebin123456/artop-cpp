/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.jdt.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.sphinx.jdt.internal.Activator;
import org.eclipse.sphinx.jdt.internal.messages.Messages;
import org.eclipse.sphinx.jdt.util.JavaExtensions;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.StatusUtil;

public class ConvertProjectToJavaProjectJob extends WorkspaceJob {

	private static final String PROJECT_RELATIVE_JAVA_SOURCE_DEFAULT_PATH = "src"; //$NON-NLS-1$

	private static final String JAVA_CLASSPATH_JRE_CONTAINER_ENTRY_SUFFIX_J2SE_1_5 = "/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/J2SE-1.5"; //$NON-NLS-1$
	private static final String JAVA_CLASSPATH_JRE_CONTAINER_ENTRY_SUFFIX_JAVA_SE_1_6 = "/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6"; //$NON-NLS-1$
	private static final String JAVA_CLASSPATH_JRE_CONTAINER_ENTRY_SUFFIX_JAVA_SE_1_7 = "/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8"; //$NON-NLS-1$
	private static final String JAVA_CLASSPATH_JRE_CONTAINER_ENTRY_SUFFIX_JAVA_SE_1_8 = "/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8"; //$NON-NLS-1$

	private Collection<IProject> projectsToConvert;
	private String projectRelativeJavaSourcePath = PROJECT_RELATIVE_JAVA_SOURCE_DEFAULT_PATH;
	private String compilerCompliance;

	/**
	 * Workspace operation to convert the specified project into a Java project.
	 *
	 * @param projectToConvert
	 *            The project to be converted.
	 */
	public ConvertProjectToJavaProjectJob(IProject projectToConvert) {
		this(Collections.singletonList(projectToConvert));
	}

	/**
	 * Workspace operation to convert the specified projects into a Java project.
	 *
	 * @param projectsToConvert
	 *            The set of projects to be converted.
	 */
	public ConvertProjectToJavaProjectJob(Collection<IProject> projectsToConvert) {
		super(Messages.job_convertProjectToJavaProject);
		this.projectsToConvert = projectsToConvert;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, Messages.job_convertProjectToJavaProject, projectsToConvert.size());
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		try {
			for (IProject projectToConvert : projectsToConvert) {
				convertToJavaProject(projectToConvert, progress.newChild(1));
			}
		} catch (OperationCanceledException ex) {
			return Status.CANCEL_STATUS;
		} catch (Exception ex) {
			return StatusUtil.createErrorStatus(Activator.getDefault(), ex);
		}

		return Status.OK_STATUS;
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

	protected void convertToJavaProject(IProject projectToConvert, IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, Messages.job_convertProjectToJavaProject, 3);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		if (projectToConvert == null || !projectToConvert.exists()) {
			return;
		}

		// Add Java nature
		if (!projectToConvert.hasNature(JavaCore.NATURE_ID)) {
			ExtendedPlatform.addNature(projectToConvert, JavaCore.NATURE_ID, progress.newChild(1));
		}

		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		// Compute Java source path and make sure that all folders on it exist
		IPath javaSourcePath;
		String projectRelativeJavaSourcePath = getProjectRelativeJavaSourcePath();
		if (projectRelativeJavaSourcePath != null && projectRelativeJavaSourcePath.length() > 0) {
			javaSourcePath = projectToConvert.getFullPath().append(projectRelativeJavaSourcePath);
			IPath projectRelativeJavaSourcePathObj = javaSourcePath.removeFirstSegments(1);
			IFolder javaSourceFolder = projectToConvert.getFolder(projectRelativeJavaSourcePathObj);
			if (!javaSourceFolder.exists()) {
				SubMonitor createSourceFolderProgress = progress.newChild(1).setWorkRemaining(javaSourcePath.segmentCount());
				for (int i = projectRelativeJavaSourcePathObj.segmentCount() - 1; i >= 0; i--) {
					IFolder folder = projectToConvert.getFolder(projectRelativeJavaSourcePathObj.removeLastSegments(i));
					if (!folder.exists()) {
						folder.create(false, true, createSourceFolderProgress.newChild(1));
					} else {
						createSourceFolderProgress.worked(1);
					}
				}
			}
		} else {
			javaSourcePath = projectToConvert.getFullPath();
		}

		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		// Set compiler compliance
		IJavaProject javaProject = JavaCore.create(projectToConvert);
		String compilerCompliance = getCompilerCompliance();
		if (compilerCompliance != null && !JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE).equals(compilerCompliance)) {
			javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, compilerCompliance);
			javaProject.setOption(JavaCore.COMPILER_SOURCE, compilerCompliance);
			javaProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, compilerCompliance);
		} else if (compilerCompliance == null) {
			compilerCompliance = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		}

		// Setup Java classpath
		List<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
		classpathEntries.addAll(Arrays.asList(javaProject.getRawClasspath()));

		// Classpath entry for Java source folder required?
		if (javaSourcePath.segmentCount() > 1) {
			// Remove existing classpath entries pointing at direct or indirect parents of Java source folder
			IClasspathEntry sourceClasspathEntry = JavaCore.newSourceEntry(javaSourcePath);
			for (Iterator<IClasspathEntry> iter = classpathEntries.iterator(); iter.hasNext();) {
				IClasspathEntry classpathEntry = iter.next();
				if (classpathEntry.getPath().isPrefixOf(javaSourcePath)) {
					iter.remove();
				}
			}

			// Add new classpath entry for actual Java source folder
			classpathEntries.add(0, sourceClasspathEntry);
		}

		// Remove existing classpath entries pointing at JRE system library
		IClasspathEntry jreClasspathEntry = JavaCore.newVariableEntry(new Path(JavaRuntime.JRELIB_VARIABLE), new Path(JavaRuntime.JRESRC_VARIABLE),
				new Path(JavaRuntime.JRESRCROOT_VARIABLE));
		for (Iterator<IClasspathEntry> i = classpathEntries.iterator(); i.hasNext();) {
			IClasspathEntry classpathEntry = i.next();
			if (classpathEntry.getPath().isPrefixOf(jreClasspathEntry.getPath())) {
				i.remove();
			}
		}

		// Add new classpath entry for actual JRE system library
		String jreContainer = JavaRuntime.JRE_CONTAINER;
		if (JavaCore.VERSION_1_5.equals(compilerCompliance)) {
			jreContainer += JAVA_CLASSPATH_JRE_CONTAINER_ENTRY_SUFFIX_J2SE_1_5;
		} else if (JavaCore.VERSION_1_6.equals(compilerCompliance)) {
			jreContainer += JAVA_CLASSPATH_JRE_CONTAINER_ENTRY_SUFFIX_JAVA_SE_1_6;
		} else if (JavaCore.VERSION_1_7.equals(compilerCompliance)) {
			jreContainer += JAVA_CLASSPATH_JRE_CONTAINER_ENTRY_SUFFIX_JAVA_SE_1_7;
		} else if (JavaExtensions.VERSION_1_8.equals(compilerCompliance)) {
			jreContainer += JAVA_CLASSPATH_JRE_CONTAINER_ENTRY_SUFFIX_JAVA_SE_1_8;
		}
		classpathEntries.add(JavaCore.newContainerEntry(new Path(jreContainer)));

		// Apply Java classpath
		IClasspathEntry[] entries = new IClasspathEntry[classpathEntries.size()];
		int i = 0;
		for (IClasspathEntry entry : classpathEntries) {
			entries[i] = entry;
			i++;
		}
		javaProject.setRawClasspath(entries, progress.newChild(1));
	}
}

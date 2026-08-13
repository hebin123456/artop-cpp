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
package org.eclipse.sphinx.emf.mwe.dynamic.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.mwe2.runtime.workflow.Workflow;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.JavaWorkspaceScope;
import org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog;
import org.eclipse.jdt.ui.dialogs.ITypeInfoFilterExtension;
import org.eclipse.jdt.ui.dialogs.ITypeInfoRequestor;
import org.eclipse.jdt.ui.dialogs.TypeSelectionExtension;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.SearchablePluginsManager;
import org.eclipse.sphinx.emf.mwe.dynamic.WorkflowContributorRegistry;
import org.eclipse.sphinx.emf.mwe.dynamic.ui.internal.Activator;
import org.eclipse.sphinx.emf.mwe.dynamic.ui.internal.messages.Messages;
import org.eclipse.sphinx.jdt.util.JavaExtensions;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.ReflectUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Workflow {@link IType type} {@link SelectionDialog selection dialog} capable of finding static workflows in
 * contributed binary plug-ins as well as dynamic workflows in plug-in projects in the workspace.
 * <p>
 * Extends {@link OpenTypeSelectionDialog} allowing to search and select any Java type and customizes it to match and
 * show only subtypes of {@link Workflow} for that purpose.
 * <p>
 */
@SuppressWarnings("restriction")
public class WorkflowTypeSelectionDialog extends OpenTypeSelectionDialog {

	/**
	 * Customized {@link JavaWorkspaceScope} implementation that narrows Java search paths down to plug-in projects in
	 * the workspace and registered workflow contributor plug-ins.
	 */
	protected static class WorkflowSearchScope extends JavaWorkspaceScope {

		@Override
		public IPath[] enclosingProjectsAndJars() {
			// Check if previously computed and cached workflow search paths are available and just return them if
			// so
			IPath[] result = null;
			try {
				result = (IPath[]) ReflectUtil.getInvisibleFieldValue(this, "enclosingPaths"); //$NON-NLS-1$
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
			if (result != null) {
				return result;
			}

			// Let (re-)compute all workspace search paths and narrow them down to the subset or search paths
			// relevant to dynamic workflows in the workspace and static workflows from registered contributor
			// plug-ins
			Set<IPath> workflowSearchPaths = new LinkedHashSet<IPath>();
			Set<IPath> javaProjectPaths = getJavaProjectPaths();
			for (IPath path : super.enclosingProjectsAndJars()) {
				if (javaProjectPaths.contains(path) || WorkflowContributorRegistry.INSTANCE.isContributorClasspathLocation(path)) {
					workflowSearchPaths.add(path);
				}
			}

			// Update cached workflow search paths
			result = workflowSearchPaths.toArray(new IPath[workflowSearchPaths.size()]);
			try {
				ReflectUtil.setInvisibleFieldValue(this, "enclosingPaths", result); //$NON-NLS-1$
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
			return result;
		}

		protected Set<IPath> getJavaProjectPaths() {
			Set<IPath> javaProjectPaths = new HashSet<IPath>();
			try {
				for (IJavaProject javaProject : JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects()) {
					if (!javaProject.getProject().getName().equals(SearchablePluginsManager.PROXY_PROJECT_NAME)) {
						javaProjectPaths.add(javaProject.getProject().getFullPath());
					}
				}
			} catch (JavaModelException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
			return javaProjectPaths;
		}
	};

	/**
	 * Custom {@link TypeSelectionExtension} implementation that filters matches resulting from Java type search to keep
	 * only subclasses of {@link Workflow}.
	 */
	protected static class WorkflowTypeSelectionExtension extends TypeSelectionExtension {
		@Override
		public ITypeInfoFilterExtension getFilterExtension() {
			return new ITypeInfoFilterExtension() {
				@Override
				public boolean select(ITypeInfoRequestor typeInfoRequestor) {
					try {
						// Retrieve Java type behind given match
						TypeNameMatch match = (TypeNameMatch) ReflectUtil.getInvisibleFieldValue(typeInfoRequestor, "fMatch"); //$NON-NLS-1$
						IType matchType = match.getType();

						// Find out where matched type originates from
						if (!matchType.isBinary()) {
							// Matched type refers to an on-the-fly compiled Java class in the runtime workspace

							// Try to find type for MWE2 Workflow within underlying Java project
							IType workflowType = matchType.getJavaProject().findType(Workflow.class.getName());
							if (workflowType != null) {
								// Retrieve super class hierarchy of matched type
								ITypeHierarchy supertypeHierarchy = matchType.newSupertypeHierarchy(new NullProgressMonitor());
								List<IType> matchSuperclasses = Arrays.asList(supertypeHierarchy.getAllSuperclasses(matchType));

								// Check if matched type is a subclass of MWE2 Workflow
								return matchSuperclasses.contains(workflowType);
							}
						} else {
							// Matched type refers to a binary Java class from the running Eclipse instance

							// Test if Java class behind matched type is a known contributed workflow
							/*
							 * Performance optimization: Ignore class names with "bin" prefix. Such class names are
							 * duplicates of the same class name without "bin" prefix. They occur when running in a
							 * runtime workbench and the underlying classes are actually classes from plug-in projects
							 * in the development workbench.
							 */
							if (!matchType.getFullyQualifiedName().startsWith(JavaExtensions.DEFAULT_OUTPUT_FOLDER_NAME)) {
								return WorkflowContributorRegistry.INSTANCE.matchesContributedWorkflowClass(matchType);
							}
						}
					} catch (Exception ex) {
						// Ignore exception
					}
					return false;
				}
			};
		}
	};

	public WorkflowTypeSelectionDialog(Shell parent) {
		super(parent, false, PlatformUI.getWorkbench().getProgressService(), new WorkflowSearchScope(), IJavaSearchConstants.CLASS,
				new WorkflowTypeSelectionExtension());
		setTitle(Messages.dialog_workflowTypeSelection_title);
		setMessage(Messages.dialog_workflowTypeSelection_message);
	}

	@Override
	public void create() {
		addWorkflowContributorsToJavaSearch();
		super.create();
	}

	protected void addWorkflowContributorsToJavaSearch() {
		final SearchablePluginsManager manager = PDECore.getDefault().getSearchablePluginsManager();

		// Determine workflow contributor plug-ins that still need to be added to Java search
		final List<IPluginModelBase> modelsToBeAdded = new ArrayList<IPluginModelBase>();
		for (String id : WorkflowContributorRegistry.INSTANCE.getContributorPluginIds()) {
			// Retrieve PDE plug-in model for current workflow contributor plug-in
			IPluginModelBase model = PluginRegistry.findModel(id);

			// Check if current workflow contributor plug-in has not already been added to Java search
			if (model != null && model.getUnderlyingResource() == null && !manager.isInJavaSearch(model.getPluginBase().getId())) {
				modelsToBeAdded.add(model);
			}
		}
		if (modelsToBeAdded.isEmpty()) {
			return;
		}

		// Perform addition of previously determined workflow contributor plug-ins to Java search
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						manager.addToJavaSearch(modelsToBeAdded.toArray(new IPluginModelBase[modelsToBeAdded.size()]));
					} finally {
						monitor.done();
					}
				}
			});
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
	}
}

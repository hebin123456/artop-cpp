/**
 * <copyright>
 *
 * Copyright (c) 2014-2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [458921] Newly introduced registries for metamodel serives, check validators and workflow contributors are not standalone-safe
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.mwe.dynamic;

import static org.eclipse.sphinx.jdt.util.JavaExtensions.isDevModePluginClasspathLocationOf;
import static org.eclipse.sphinx.jdt.util.JavaExtensions.isInstalledPluginClasspathRootLocationOf;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.mwe2.runtime.workflow.Workflow;
import org.eclipse.jdt.core.IType;
import org.eclipse.sphinx.emf.mwe.dynamic.internal.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.StatusUtil;

public class WorkflowContributorRegistry {

	private static final String EXTP_WORKFLOW_CONTRIBUTORS = Activator.INSTANCE.getSymbolicName() + ".workflowContributors"; //$NON-NLS-1$
	private static final String ELEM_CONTRIBUTOR = "contributor"; //$NON-NLS-1$
	private static final String ATTR_PLUGIN_ID = "pluginId"; //$NON-NLS-1$

	public static final WorkflowContributorRegistry INSTANCE = new WorkflowContributorRegistry(Platform.getExtensionRegistry(),
			PlatformLogUtil.getLog(Activator.getPlugin()));

	private Set<String> contributorPluginIds = null;

	private IExtensionRegistry extensionRegistry;

	private ILog log;

	protected WorkflowContributorRegistry(IExtensionRegistry extensionRegistry, ILog log) {
		this.extensionRegistry = extensionRegistry;
		this.log = log;
	}

	public Set<String> getContributorPluginIds() {
		initialize();
		return contributorPluginIds != null ? contributorPluginIds : Collections.<String> emptySet();
	}

	/**
	 * Initialize internal data by reading from platform registry
	 */
	private void initialize() {
		if (extensionRegistry == null) {
			return;
		}

		if (contributorPluginIds == null) {
			synchronized (this) {
				contributorPluginIds = new HashSet<String>();

				IConfigurationElement[] elements = extensionRegistry.getConfigurationElementsFor(EXTP_WORKFLOW_CONTRIBUTORS);
				for (IConfigurationElement element : elements) {
					if (!element.getName().equals(ELEM_CONTRIBUTOR)) {
						continue;
					}

					// Retrieve contributor bundle id
					String id = element.getAttribute(ATTR_PLUGIN_ID);
					if (id == null || id.isEmpty()) {
						String msg = "Missing contributor id in " + EXTP_WORKFLOW_CONTRIBUTORS + " extension from " //$NON-NLS-1$ //$NON-NLS-2$
								+ element.getContributor().getName();
						IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), new RuntimeException(msg));
						log.log(status);
						continue;
					}

					contributorPluginIds.add(id);
				}
			}
		}
	}

	/**
	 * Checks if the provided classpath location is a contributed via an installed plug-in or in a development mode
	 * plug-in. Examples:
	 * <p>pluginId = org.eclipse.sphinx.examples.workflows</p>
	 * <p>Expected to match:</p>
	 * <ul>
	 * <li>W:/eclipse-sphinx/org.eclipse.sphinx/plugins/org.eclipse.sphinx.examples.workflows/bin/</li>
	 * <li>W:/eclipse-sphinx/org.eclipse.sphinx/examples/org.eclipse.sphinx.examples.workflows/bin/org/eclipse/sphinx/examples/workflows/model/PrintModelContentWorkflow.class</li>
	 * <li>W:/eclipse-sphinx/.metadata/.plugins/org.eclipse.pde.core/.bundle_pool/plugins/org.eclipse.sphinx.examples.workflows_0.9.0.v20141209-1519.jar</li>
	 * <ul>
	 * <p>Expected to ignore:</p>
	 * <ul>
	 * <li>/External Plug-in Libraries</li>
	 * <li>/MyProject</li>
	 * <li>C:/Program%20Files/Java/jdk1.7.0_55/jre/lib/resources.jar</li>
	 * <li>W:/eclipse-sphinx/.metadata/.plugins/org.eclipse.pde.core/.bundle_pool/plugins/org.eclipse.sphinx.examples.workflows.lib_0.9.0.v20141209-1519.jar</li>
	 * </ul>
	 */
	public boolean isContributorClasspathLocation(IPath classpathLocation) {
		return getContributorPluginId(classpathLocation) != null;
	}

	private String getContributorPluginId(IPath classpathLocation) {
		if (classpathLocation != null && classpathLocation.segmentCount() > 1) {
			for (String id : getContributorPluginIds()) {
				if (isInstalledPluginClasspathRootLocationOf(id, classpathLocation) || isDevModePluginClasspathLocationOf(id, classpathLocation)) {
					return id;
				}
			}
		}
		return null;
	}

	public boolean matchesContributedWorkflowClass(IType workflowType) {
		try {
			return loadContributedWorkflowClass(workflowType) != null;
		} catch (Exception ex) {
			// Ignore exception, just return false
			return false;
		}
	}

	public Class<Workflow> loadContributedWorkflowClass(IType workflowType) throws CoreException {
		Assert.isNotNull(workflowType);

		try {
			String contributorPluginId = getContributorPluginId(workflowType.getPath());
			if (contributorPluginId == null) {
				throw new IllegalStateException(
						"Workflow '" + workflowType.getFullyQualifiedName() + "' is not contained in any registered workflow contributor plug-in"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			Class<?> clazz = CommonPlugin.loadClass(contributorPluginId, workflowType.getFullyQualifiedName());
			if (!Workflow.class.isAssignableFrom(clazz)) {
				throw new IllegalStateException("Workflow class '" + clazz.getName() + "' is not a subclass of " + Workflow.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			@SuppressWarnings("unchecked")
			Class<Workflow> workflowClass = (Class<Workflow>) clazz;
			return workflowClass;
		} catch (Exception ex) {
			IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
			throw new CoreException(status);
		}
	}
}

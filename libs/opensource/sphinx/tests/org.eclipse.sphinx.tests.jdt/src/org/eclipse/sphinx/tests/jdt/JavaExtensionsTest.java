/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.tests.jdt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.sphinx.jdt.util.JavaExtensions;
import org.junit.Test;

@SuppressWarnings("nls")
public class JavaExtensionsTest {

	private static final IPath EXAMPLE_WORKFLOW_TEST_PROJECT_PATH = new Path(
			"W:/eclipse-sphinx/org.eclipse.sphinx/plugins/org.eclipse.sphinx.examples.workflows");

	private static final IPath WORKFLOW_DEVMODE_CLASSPATH_LOCATION = EXAMPLE_WORKFLOW_TEST_PROJECT_PATH.append("bin/");

	private static final IPath PRINTMODELCONTENT_WORKFLOW_DEVMODE_CLASSPATH_LOCATION = EXAMPLE_WORKFLOW_TEST_PROJECT_PATH
			.append("bin/org/eclipse/sphinx/examples/workflows/model/PrintModelContentWorkflow.class");

	private static final IPath PLUGIN_INSTALLATION_LOCATION = new Path(Platform.getInstallLocation().getURL().getPath()).append("plugins");

	private static final IPath WORKFLOW_INSTALLED_PLUGIN_CLASSPATH_LOCATION = PLUGIN_INSTALLATION_LOCATION
			.append("org.eclipse.sphinx.examples.workflows_0.9.0.v20141209-1519.jar");

	private static final IPath MY_PROJECT_PATH = new Path("/MyProject");
	private static final IPath JRE_RESOURCES_JAR_PATH = new Path("C:/Program%20Files/Java/jdk1.7.0_55/jre/lib/resources.jar");

	private static final IPath INSTALLED_EXAMPLE_WORKFLOW_LIB_PLUGIN_JAR_PATH = PLUGIN_INSTALLATION_LOCATION
			.append("org.eclipse.sphinx.examples.workflows.lib_0.9.0.v20141209-1519.jar");

	@Test
	public void testIsInstalledPluginClasspathRootLocationOf() {
		String pluginId = "org.eclipse.sphinx.examples.workflows";
		boolean result = JavaExtensions.isInstalledPluginClasspathRootLocationOf(pluginId, WORKFLOW_INSTALLED_PLUGIN_CLASSPATH_LOCATION);
		assertTrue(result);

		result = JavaExtensions.isInstalledPluginClasspathRootLocationOf(pluginId, WORKFLOW_DEVMODE_CLASSPATH_LOCATION);
		assertFalse(result);

		result = JavaExtensions.isInstalledPluginClasspathRootLocationOf(pluginId, PRINTMODELCONTENT_WORKFLOW_DEVMODE_CLASSPATH_LOCATION);
		assertFalse(result);

		result = JavaExtensions.isInstalledPluginClasspathRootLocationOf(pluginId, MY_PROJECT_PATH);
		assertFalse(result);

		result = JavaExtensions.isInstalledPluginClasspathRootLocationOf(pluginId, JRE_RESOURCES_JAR_PATH);
		assertFalse(result);

		result = JavaExtensions.isInstalledPluginClasspathRootLocationOf(pluginId, INSTALLED_EXAMPLE_WORKFLOW_LIB_PLUGIN_JAR_PATH);
		assertFalse(result);

		pluginId = "org.eclipse.sphinx.examples.workflows.exended";
		result = JavaExtensions.isInstalledPluginClasspathRootLocationOf(pluginId, WORKFLOW_INSTALLED_PLUGIN_CLASSPATH_LOCATION);
		assertFalse(result);
	}

	@Test
	public void testIsDevModePluginClasspathLocationOf() {
		String pluginId = "org.eclipse.sphinx.examples.workflows";
		boolean result = JavaExtensions.isDevModePluginClasspathLocationOf(pluginId, WORKFLOW_INSTALLED_PLUGIN_CLASSPATH_LOCATION);
		assertFalse(result);

		result = JavaExtensions.isDevModePluginClasspathLocationOf(pluginId, WORKFLOW_DEVMODE_CLASSPATH_LOCATION);
		assertTrue(result);

		result = JavaExtensions.isDevModePluginClasspathLocationOf(pluginId, PRINTMODELCONTENT_WORKFLOW_DEVMODE_CLASSPATH_LOCATION);
		assertTrue(result);

		result = JavaExtensions.isDevModePluginClasspathLocationOf(pluginId, MY_PROJECT_PATH);
		assertFalse(result);

		result = JavaExtensions.isDevModePluginClasspathLocationOf(pluginId, JRE_RESOURCES_JAR_PATH);
		assertFalse(result);

		result = JavaExtensions.isDevModePluginClasspathLocationOf(pluginId, INSTALLED_EXAMPLE_WORKFLOW_LIB_PLUGIN_JAR_PATH);
		assertFalse(result);

		pluginId = "org.eclipse.sphinx.examples.workflows.exended";
		result = JavaExtensions.isDevModePluginClasspathLocationOf(pluginId, WORKFLOW_INSTALLED_PLUGIN_CLASSPATH_LOCATION);
		assertFalse(result);

		result = JavaExtensions.isDevModePluginClasspathLocationOf(pluginId, WORKFLOW_DEVMODE_CLASSPATH_LOCATION);
		assertFalse(result);

		result = JavaExtensions.isDevModePluginClasspathLocationOf(pluginId, PRINTMODELCONTENT_WORKFLOW_DEVMODE_CLASSPATH_LOCATION);
		assertFalse(result);
	}
}

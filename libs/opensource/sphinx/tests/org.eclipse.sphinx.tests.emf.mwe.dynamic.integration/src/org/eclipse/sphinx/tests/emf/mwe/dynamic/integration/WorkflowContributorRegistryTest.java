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
package org.eclipse.sphinx.tests.emf.mwe.dynamic.integration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.sphinx.tests.emf.mwe.dynamic.integration.internal.Activator;
import org.eclipse.sphinx.tests.emf.mwe.dynamic.integration.internal.mocks.WorkflowContributorRegistryMockFactory;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("nls")
public class WorkflowContributorRegistryTest {

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

	private static WorkflowContributorRegistryMockFactory mockFactory = new WorkflowContributorRegistryMockFactory();

	private static TestableWorkflowContributorRegistry workflowContributorRegistry;

	@BeforeClass
	public static void initWorkflowContributorRegistry() {
		IExtensionRegistry extensionRegistry = mockFactory.createExtensionRegistryMock(Activator.getPlugin(),
				"org.eclipse.sphinx.examples.workflows", "org.eclipse.sphinx.examples.workflows.extended");
		workflowContributorRegistry = new TestableWorkflowContributorRegistry(extensionRegistry);
	}

	@Test
	public void testGetContributorPluginIds() {
		assertNotNull(workflowContributorRegistry);

		Set<String> contributorPluginIds = workflowContributorRegistry.getContributorPluginIds();
		assertTrue(contributorPluginIds.size() == 2);
	}

	@Test
	public void testIsContributorClasspathLocation() {
		assertNotNull(workflowContributorRegistry);

		boolean contributorClasspathLocation = workflowContributorRegistry.isContributorClasspathLocation(WORKFLOW_DEVMODE_CLASSPATH_LOCATION);
		assertTrue(contributorClasspathLocation);

		contributorClasspathLocation = workflowContributorRegistry
				.isContributorClasspathLocation(PRINTMODELCONTENT_WORKFLOW_DEVMODE_CLASSPATH_LOCATION);
		assertTrue(contributorClasspathLocation);

		contributorClasspathLocation = workflowContributorRegistry.isContributorClasspathLocation(WORKFLOW_INSTALLED_PLUGIN_CLASSPATH_LOCATION);
		assertTrue(contributorClasspathLocation);

		contributorClasspathLocation = workflowContributorRegistry.isContributorClasspathLocation(MY_PROJECT_PATH);
		assertFalse(contributorClasspathLocation);

		contributorClasspathLocation = workflowContributorRegistry.isContributorClasspathLocation(JRE_RESOURCES_JAR_PATH);
		assertFalse(contributorClasspathLocation);

		contributorClasspathLocation = workflowContributorRegistry.isContributorClasspathLocation(INSTALLED_EXAMPLE_WORKFLOW_LIB_PLUGIN_JAR_PATH);
		assertFalse(contributorClasspathLocation);
	}
}

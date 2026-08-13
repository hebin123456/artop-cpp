/**
 * <copyright>
 *
 * Copyright (c) 2020 IncQuery Labs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     IncQuery Labs - [566004] Test case reproducing regression
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.viatra.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.DeleteCommand;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.emf.workspace.viatra.query.WorkspaceViatraQueryEngineHelper;
import org.eclipse.sphinx.examples.hummingbird.ide.natures.HummingbirdNature;
import org.eclipse.sphinx.examples.hummingbird.ide.preferences.IHummingbirdPreferences;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.tests.emf.viatra.query.internal.Activator;
import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class WorkspaceScopingViatraQueryTest {

	private IProject project1;
	private IProject project2;

	@Before
	public void setUp() throws Exception {
		project1 = ResourcesPlugin.getWorkspace().getRoot().getProject("project1");
		project2 = ResourcesPlugin.getWorkspace().getRoot().getProject("project2");

		if (project1.exists()) {
			project1.delete(true, new NullProgressMonitor());
		}
		if (project2.exists()) {
			project2.delete(true, new NullProgressMonitor());
		}

		project1.create(new NullProgressMonitor());
		project2.create(new NullProgressMonitor());
		project1.open(new NullProgressMonitor());
		project2.open(new NullProgressMonitor());

		configureProject(project1);
		configureProject(project2);

		copyToProject("resources/input/hb20.typemodel", project1);
		copyToProject("resources/input/hb20.instancemodel", project2);
	}

	private void configureProject(IProject project) throws CoreException {
		HummingbirdNature.addTo(project, new NullProgressMonitor());
		IHummingbirdPreferences.METAMODEL_VERSION.setToDefaultInProject(project);
		IHummingbirdPreferences.RESOURCE_VERSION.setToDefault(project);
	}

	private void copyToProject(String path, IProject project) throws CoreException, IOException {
		IPath p = new Path(path);
		URL url = FileLocator.find(Activator.getPlugin().getBundle(), p, null);
		IFile f = project.getFile(p.lastSegment());
		f.create(url.openStream(), true, new NullProgressMonitor());
	}

	@Test
	public void test() throws Exception {
		ModelLoadManager.INSTANCE.loadAllProjects(Hummingbird20MMDescriptor.INSTANCE, false, new NullProgressMonitor());

		Collection<IModelDescriptor> desc1 = ModelDescriptorRegistry.INSTANCE.getModels(project1);
		assertEquals(1, desc1.size());
		IModelDescriptor model1 = desc1.iterator().next();
		Collection<Resource> ress1 = model1.getLoadedResources(true);
		assertEquals(1, ress1.size());
		Resource res1 = ress1.iterator().next();
		EObject root1 = res1.getContents().get(0);
		assertTrue(root1 instanceof Platform);

		Collection<IModelDescriptor> desc2 = ModelDescriptorRegistry.INSTANCE.getModels(project2);
		assertEquals(1, desc2.size());
		IModelDescriptor model2 = desc2.iterator().next();
		Collection<Resource> ress2 = model2.getLoadedResources(true);
		assertEquals(1, ress2.size());
		Resource res2 = ress2.iterator().next();
		EObject root2 = res2.getContents().get(0);
		assertTrue(root2 instanceof Application);
		Application app = (Application) root2;

		EcoreUtil.resolveAll(res2);

		// without setting project dependencies, sphinx shouldn't be able to resolve
		// cross-references
		assertTrue(app.getComponents().get(0).getType().eIsProxy());

		// Regression https://bugs.eclipse.org/bugs/show_bug.cgi?id=566004: deletion event is also received
		// by the other VQ engine causing it to become tainted
		model1.getEditingDomain().getCommandStack()
				.execute(new DeleteCommand(model1.getEditingDomain(), Collections.singleton(((Platform) root1).getComponentTypes().get(1))));

		ViatraQueryEngine v1 = new WorkspaceViatraQueryEngineHelper().getEngine(model1);
		assertFalse(((AdvancedViatraQueryEngine) v1).isTainted());
		ViatraQueryEngine v2 = new WorkspaceViatraQueryEngineHelper().getEngine(model2);
		assertFalse(((AdvancedViatraQueryEngine) v2).isTainted());
	}
}

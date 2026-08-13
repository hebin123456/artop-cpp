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
package org.eclipse.sphinx.tests.emf.workspace.integration.splitting;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.splitting.IModelSplitOperation;
import org.eclipse.sphinx.emf.splitting.IModelSplitPolicy;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.workspace.operations.BasicModelSplitOperation;
import org.eclipse.sphinx.examples.hummingbird20.splitting.Hummingbird20TypeModelSplitPolicy;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.tests.emf.workspace.integration.internal.Activator;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

public class ModelSplitOperationTest extends DefaultIntegrationTestCase {

	private static final String COMPONENT_TYPES_TARGET_FILE_NAME = "ComponentTypes.typemodel"; //$NON-NLS-1$
	private static final String INTERFACES_TARGET_FILE_NAME = "Interfaces.typemodel"; //$NON-NLS-1$

	public ModelSplitOperationTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);

		// Remove all project references except:
		// HB_PROJECT_NAME_20_E -> HB_PROJECT_NAME_20_D
		Map<String, Set<String>> projectReferences = getProjectReferences();
		projectReferences.remove(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E);
		projectReferences.remove(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);

		// Set test plug-in for retrieving test input resources
		setTestPlugin(Activator.getPlugin());
	}

	public void testModelSplitOperation() throws Exception {
		IFile modelFile = refWks.hbProject20_B.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertNotNull(modelFile);

		Resource resource = getResource(modelFile);
		assertNotNull(resource);

		IModelSplitPolicy modelSplitPolicy = new Hummingbird20TypeModelSplitPolicy();
		IModelSplitOperation operation = new BasicModelSplitOperation(resource, modelSplitPolicy);
		operation.run(new NullProgressMonitor());

		// Inferfaces
		IFile interfacesFile = refWks.hbProject20_B.getFile(INTERFACES_TARGET_FILE_NAME);
		assertNotNull(interfacesFile);
		Resource interfaceResource = getResource(interfacesFile);
		assertNotNull(interfaceResource);
		assertTrue(interfaceResource.getContents().size() == 1);
		EObject rootEObject = interfaceResource.getContents().get(0);
		assertTrue(rootEObject instanceof Platform);
		EList<Interface> interfaces = ((Platform) rootEObject).getInterfaces();
		assertEquals(2, interfaces.size());
		EList<ComponentType> componentTypes = ((Platform) rootEObject).getComponentTypes();
		assertEquals(0, componentTypes.size());

		// Component Types
		IFile componentTypesFile = refWks.hbProject20_B.getFile(COMPONENT_TYPES_TARGET_FILE_NAME);
		assertNotNull(componentTypesFile);
		Resource componentTypesResource = getResource(componentTypesFile);
		assertNotNull(componentTypesResource);
		rootEObject = componentTypesResource.getContents().get(0);
		assertTrue(rootEObject instanceof Platform);
		componentTypes = ((Platform) rootEObject).getComponentTypes();
		assertEquals(2, componentTypes.size());
		interfaces = ((Platform) rootEObject).getInterfaces();
		assertEquals(0, interfaces.size());

		for (ComponentType componentType : componentTypes) {
			if ("ComponentType1".equals(componentType.getName())) { //$NON-NLS-1$
				assertEquals(2, componentType.getPorts().size());
				break;
			}
		}
	}

	private Resource getResource(IFile modelFile) {
		if (modelFile != null) {
			if (ModelDescriptorRegistry.INSTANCE.isModelFile(modelFile)) {
				return EcorePlatformUtil.loadResource(modelFile, EcoreResourceUtil.getDefaultLoadOptions());
			} else {
				return EcoreResourceUtil.loadResource(null, EcorePlatformUtil.createURI(modelFile.getFullPath()),
						EcoreResourceUtil.getDefaultLoadOptions());
			}
		}
		return null;
	}
}

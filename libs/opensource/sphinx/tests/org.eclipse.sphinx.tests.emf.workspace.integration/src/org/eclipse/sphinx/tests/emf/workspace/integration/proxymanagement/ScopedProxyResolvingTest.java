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
 *     See4sys - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.workspace.integration.proxymanagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

// TODO Create a new test plug-in named org.eclipse.sphinx.tests.emf.workspace.viatra.query
// with a dependency on org.eclipse.sphinx.examples.hummingbird20.viatra.query and this test inside
// to cover the case of scoped proxy resolution with Viatra/Query support in Maven/Tycho builds
public class ScopedProxyResolvingTest extends DefaultIntegrationTestCase {

	private IFile typeModel_20A_2_file;
	private IFile instanceModel_20A_3_file;
	private IFile instanceModel_20A_4_file;
	private IFile typeModel_20D_2_file;
	private IFile instanceModel_20D_3_file;
	private IFile instanceModel_20E_1_file;

	public ScopedProxyResolvingTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Retrieve Hummingbird 2.0 files that are relevant to this test
		List<IFile> allHummingbird20Files = new ArrayList<IFile>();
		typeModel_20A_2_file = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		allHummingbird20Files.add(typeModel_20A_2_file);
		instanceModel_20A_3_file = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);
		allHummingbird20Files.add(instanceModel_20A_3_file);
		instanceModel_20A_4_file = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_21_20A_4);
		allHummingbird20Files.add(instanceModel_20A_4_file);
		typeModel_20D_2_file = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		allHummingbird20Files.add(typeModel_20D_2_file);
		instanceModel_20D_3_file = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		allHummingbird20Files.add(instanceModel_20D_3_file);
		instanceModel_20E_1_file = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		allHummingbird20Files.add(instanceModel_20E_1_file);

		// Simulate simultaneous import of multiple projects by unloading them and then loading the files they contain
		// (or more precisely the subset of files that are relevant to this test) in a single step
		synchronizedUnloadAllProjects();
		ModelLoadManager.INSTANCE.loadFiles(allHummingbird20Files, false, null);
	}

	// Given that proxies might be resolved correctly by accident even if underlying logic is insufficient or not aware
	// of Sphinx resource scopes, we need to repeat this test several times to get a meaningful result
	public void testScopedProxyResovling_firstRun() throws Exception {
		// FIXME Perform the following actions to re-enable this test: remove convertedInLoading_ prefix from component
		// names in *.instancemodel files in default reference workspace, adapt URI fragments used in this test
		// accordingly, investigate why these prefixes stay in reference workspace after having run
		// ModelConverterTest,double check and fix AbstractIntegrationTestCase#synchronizedDeleteChangedFiles()
		// validateResolvedCrossReferencesInHB20A();
		// validateResolvedCrossReferencesInHB20D();
		// validateResolvedCrossReferencesInHB20E();
	}

	// Given that proxies might be resolved correctly by accident even if underlying logic is insufficient or not aware
	// of Sphinx resource scopes, we need to repeat this test several times to get a meaningful result
	public void testScopedProxyResovling_secondRun() throws Exception {
		// FIXME Perform the following actions to re-enable this test: remove convertedInLoading_ prefix from component
		// names in *.instancemodel files in default reference workspace, adapt URI fragments used in this test
		// accordingly, investigate why these prefixes stay in reference workspace after having run
		// ModelConverterTest,double check and fix AbstractIntegrationTestCase#synchronizedDeleteChangedFiles()
		// validateResolvedCrossReferencesInHB20A();
		// validateResolvedCrossReferencesInHB20D();
		// validateResolvedCrossReferencesInHB20E();
	}

	// Given that proxies might be resolved correctly by accident even if underlying logic is insufficient or not aware
	// of Sphinx resource scopes, we need to repeat this test several times to get a meaningful result
	public void testScopedProxyResovling_thirdRun() throws Exception {
		// FIXME Perform the following actions to re-enable this test: remove convertedInLoading_ prefix from component
		// names in *.instancemodel files in default reference workspace, adapt URI fragments used in this test
		// accordingly, investigate why these prefixes stay in reference workspace after having run
		// ModelConverterTest,double check and fix AbstractIntegrationTestCase#synchronizedDeleteChangedFiles()
		// validateResolvedCrossReferencesInHB20A();
		// validateResolvedCrossReferencesInHB20D();
		// validateResolvedCrossReferencesInHB20E();
	}

	@SuppressWarnings("nls")
	protected void validateResolvedCrossReferencesInHB20A() throws Exception {
		Resource typeModel_20A_2_resource = EcorePlatformUtil.getResource(typeModel_20A_2_file);
		Resource instanceModel_20A_3_resource = EcorePlatformUtil.getResource(instanceModel_20A_3_file);
		Resource instanceModel_20A_4_resource = EcorePlatformUtil.getResource(instanceModel_20A_4_file);

		// Test consistency of hbFile20_20A_3.instancemodel -> hbFile20_20A_2.typemodel cross references
		Component convertedComponent1 = (Component) instanceModel_20A_3_resource.getEObject("//convertedInLoading_Component1");
		assertNotNull(convertedComponent1);

		ComponentType componentType1 = convertedComponent1.getType();
		assertNotNull(componentType1);
		assertEquals("ComponentType1", componentType1.getName());
		assertSame(componentType1.eResource(), typeModel_20A_2_resource);

		Component convertedComponent2 = (Component) instanceModel_20A_3_resource.getEObject("//convertedInLoading_Component2");
		assertNotNull(convertedComponent2);

		ComponentType componentType2 = convertedComponent2.getType();
		assertNotNull(componentType2);
		assertEquals("ComponentType2", componentType2.getName());
		assertSame(componentType2.eResource(), typeModel_20A_2_resource);

		// Test consistency of hbFile21_20A_4.instancemodel -> hbFile20_20A_2.typemodel cross references
		Component component1 = (Component) instanceModel_20A_4_resource.getEObject("//Component1");
		assertNotNull(component1);

		componentType1 = component1.getType();
		assertNotNull(componentType1);
		assertEquals("ComponentType1", componentType1.getName());
		assertSame(componentType1.eResource(), typeModel_20A_2_resource);

		Component component2 = (Component) instanceModel_20A_4_resource.getEObject("//Component2");
		assertNotNull(component2);

		componentType2 = component2.getType();
		assertNotNull(componentType2);
		assertEquals("ComponentType2", componentType2.getName());
		assertSame(componentType2.eResource(), typeModel_20A_2_resource);
	}

	@SuppressWarnings("nls")
	protected void validateResolvedCrossReferencesInHB20D() throws Exception {
		Resource typeModel_20D_2_resource = EcorePlatformUtil.getResource(typeModel_20D_2_file);
		Resource instanceModel_20D_3_resource = EcorePlatformUtil.getResource(instanceModel_20D_3_file);

		// Test consistency of hbFile20_20D_3.instancemodel -> hbFile20_20D_2.typemodel cross references
		Component convertedComponent1 = (Component) instanceModel_20D_3_resource.getEObject("//convertedInLoading_Component1");
		assertNotNull(convertedComponent1);

		ComponentType componentType1 = convertedComponent1.getType();
		assertNotNull(componentType1);
		assertEquals("ComponentType1", componentType1.getName());
		assertSame(componentType1.eResource(), typeModel_20D_2_resource);

		Component convertedComponent2 = (Component) instanceModel_20D_3_resource.getEObject("//convertedInLoading_Component2");
		assertNotNull(convertedComponent2);

		ComponentType componentType2 = convertedComponent2.getType();
		assertNotNull(componentType2);
		assertEquals("ComponentType2", componentType2.getName());
		assertSame(componentType2.eResource(), typeModel_20D_2_resource);
	}

	@SuppressWarnings("nls")
	protected void validateResolvedCrossReferencesInHB20E() throws Exception {
		Resource typeModel_20D_2_resource = EcorePlatformUtil.getResource(typeModel_20D_2_file);
		Resource instanceModel_20E_1_resource = EcorePlatformUtil.getResource(instanceModel_20E_1_file);

		// Test consistency of hbFile20_20E_1.instancemodel -> hbFile20_20D_2.typemodel cross references
		Component convertedComponent1 = (Component) instanceModel_20E_1_resource.getEObject("//convertedInLoading_Component1");
		assertNotNull(convertedComponent1);

		ComponentType componentType1 = convertedComponent1.getType();
		assertNotNull(componentType1);
		assertEquals("ComponentType1", componentType1.getName());
		assertSame(componentType1.eResource(), typeModel_20D_2_resource);

		Component convertedComponent2 = (Component) instanceModel_20E_1_resource.getEObject("//convertedInLoading_Component2");
		assertNotNull(convertedComponent2);

		ComponentType componentType2 = convertedComponent2.getType();
		assertNotNull(componentType2);
		assertEquals("ComponentType2", componentType2.getName());
		assertSame(componentType2.eResource(), typeModel_20D_2_resource);
	}
}

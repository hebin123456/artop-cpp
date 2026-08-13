/**
 * <copyright>
 *
 * Copyright (c) 2008-2019 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [400897] ExtendedResourceAdapter's approach of reflectively clearing all EObject fields when performing memory-optimized unloads bears the risk of leaving some EObjects leaked
 *     itemis - [409510] Enable resource scope-sensitive proxy resolutions without forcing metamodel implementations to subclass EObjectImpl
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *     itemis - [425379] ExtendedResourceSet may contain a resource multiple times (with normalized and non-normalized URI)
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.integration.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.FeatureMap.Entry;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.WrapperItemProvider;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapter;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapterFactory;
import org.eclipse.sphinx.emf.resource.ModelResourceDescriptor;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.examples.hummingbird10.Application;
import org.eclipse.sphinx.examples.hummingbird10.Component;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Factory;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.examples.hummingbird10.Interface;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.tests.emf.integration.internal.Activator;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.xml.sax.SAXParseException;

/**
 * JUnit Test for class {@link EcorePlatformUtil}
 */
@SuppressWarnings("nls")
public class EcorePlatformUtilTest extends DefaultIntegrationTestCase {

	public EcorePlatformUtilTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
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

	private Platform createPlatform() {
		Platform platform = TypeModel20Factory.eINSTANCE.createPlatform();
		platform.setName("Platform");

		org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface interface1 = TypeModel20Factory.eINSTANCE.createInterface();
		interface1.setName("interface");
		platform.getInterfaces().add(interface1);

		ComponentType componentType = TypeModel20Factory.eINSTANCE.createComponentType();
		componentType.setName("ComponentType");
		platform.getComponentTypes().add(componentType);

		org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter param = TypeModel20Factory.eINSTANCE.createParameter();
		param.setName("param");
		param.setDataType("String");
		param.setOptional(true);
		componentType.getParameters().add(param);

		Port port = TypeModel20Factory.eINSTANCE.createPort();
		port.setName("port");
		port.setOwner(componentType);
		port.setMaxProviderCount(100);
		port.setMinProviderCount(10);
		port.setRequiredInterface(interface1);

		componentType.getPorts().add(port);
		return platform;

	}

	private Application createApplicationHB10() {
		Application application = Hummingbird10Factory.eINSTANCE.createApplication();
		application.setName("Application");

		Component component = Hummingbird10Factory.eINSTANCE.createComponent();
		component.setName("Component");

		Interface interface1 = Hummingbird10Factory.eINSTANCE.createInterface();
		interface1.setName("Interface");

		application.getComponents().add(component);
		application.getInterfaces().add(interface1);

		return application;
	}

	/**
	 * Test method for {@link EcorePlatformUtil#validate(IFile, URL)} .
	 */
	public void testValidate() throws Exception {

		IFile validModelFile = refWks.hbProject20_A.getFile("valid_exampleModel.xml");
		assertNotNull(validModelFile);
		validModelFile.create(getTestFileAccessor().openInputFileInputStream("valid_exampleModel.xml"), true, null);

		IFile invalidModelFile = refWks.hbProject20_A.getFile("invalid_exampleModel.xml");
		assertNotNull(invalidModelFile);
		invalidModelFile.create(getTestFileAccessor().openInputFileInputStream("invalid_exampleModel.xml"), true, null);

		URL schemaURL = getTestFileAccessor().getInputFileURI("exampleSchema.xsd", true).toURL();

		try {
			EcorePlatformUtil.validate(validModelFile, schemaURL);

		} catch (Exception ex) {
			fail(ex.getClass().getName() + " " + ex.getMessage());
		}
		try {
			EcorePlatformUtil.validate(invalidModelFile, schemaURL);
		} catch (Exception ex) {
			if (!(ex instanceof SAXParseException)) {
				fail(ex.getMessage());
			}
		}

	}

	/**
	 * Test method for {@link EcorePlatformUtil#unloadFile(ResourceSet resourceSet, IPath modelPath)} .
	 */
	public void testUnloadResource() throws Exception {
		int resourceCountInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		int resourceCountInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		int resourceCountInEditingDomainUml2 = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);

		// ==================================================
		// HUMINGBIRD 20
		// Unload HB_FILE_NAME_20_20_A_1
		IFile file = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertNotNull(file);

		EcorePlatformUtil.unloadFile(refWks.editingDomain20, file);
		waitForModelLoading();

		resourceCountInEditingDomain20 = resourceCountInEditingDomain20 - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceCountInEditingDomain20);

		// Unload HB_FILE_NAME_20_20_A_2
		file = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		assertNotNull(file);

		EcorePlatformUtil.unloadFile(refWks.editingDomain20, file);
		waitForModelLoading();

		resourceCountInEditingDomain20 = resourceCountInEditingDomain20 - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceCountInEditingDomain20);

		// Unload HB_FILE_NAME_20_20_A_3
		file = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);
		assertNotNull(file);

		EcorePlatformUtil.unloadFile(refWks.editingDomain20, file);
		waitForModelLoading();

		resourceCountInEditingDomain20 = resourceCountInEditingDomain20 - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceCountInEditingDomain20);

		// Unload HB_FILE_NAME_20_20_D_1
		file = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertNotNull(file);

		EcorePlatformUtil.unloadFile(refWks.editingDomain20, file);
		waitForModelLoading();

		resourceCountInEditingDomain20 = resourceCountInEditingDomain20 - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceCountInEditingDomain20);

		// Unload HB_FILE_NAME_20_20_D_2
		file = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertNotNull(file);

		EcorePlatformUtil.unloadFile(refWks.editingDomain20, file);
		waitForModelLoading();

		resourceCountInEditingDomain20 = resourceCountInEditingDomain20 - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceCountInEditingDomain20);

		// Unload HB_FILE_NAME_20_20_D_3
		file = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertNotNull(file);

		EcorePlatformUtil.unloadFile(refWks.editingDomain20, file);
		waitForModelLoading();

		resourceCountInEditingDomain20 = resourceCountInEditingDomain20 - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceCountInEditingDomain20);
		// ====================================================
		// HUMMINGBIRD 10
		// Unload HB_FILE_NAME_10_10_A_1
		file = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertNotNull(file);

		EcorePlatformUtil.unloadFile(refWks.editingDomain10, file);
		waitForModelLoading();

		resourceCountInEditingDomain10 = resourceCountInEditingDomain10 - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceCountInEditingDomain10);

		// Unload HB_FILE_NAME_10_10_A_2
		file = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertNotNull(file);

		EcorePlatformUtil.unloadFile(refWks.editingDomain10, file);
		waitForModelLoading();

		resourceCountInEditingDomain10 = resourceCountInEditingDomain10 - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceCountInEditingDomain10);

		// Unload HB_FILE_NAME_10_10_A_3
		file = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertNotNull(file);

		EcorePlatformUtil.unloadFile(refWks.editingDomain10, file);
		waitForModelLoading();

		resourceCountInEditingDomain10 = resourceCountInEditingDomain10 - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceCountInEditingDomain10);
		// ===============================================
		// UML2 MODEL
		// Unload file UML2_FILE_NAME_20D_1
		file = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertNotNull(file);

		EcorePlatformUtil.unloadFile(refWks.editingDomainUml2, file);
		waitForModelLoading();

		resourceCountInEditingDomainUml2 = resourceCountInEditingDomainUml2 - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceCountInEditingDomainUml2);

		// Unload file UML2_FILE_NAME_20D_2
		file = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertNotNull(file);

		EcorePlatformUtil.unloadFile(refWks.editingDomainUml2, file);
		waitForModelLoading();

		resourceCountInEditingDomainUml2 = resourceCountInEditingDomainUml2 - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceCountInEditingDomainUml2);

		// Unload file UML2_FILE_NAME_20D_3
		file = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		assertNotNull(file);

		EcorePlatformUtil.unloadFile(refWks.editingDomainUml2, file);
		waitForModelLoading();

		resourceCountInEditingDomainUml2 = resourceCountInEditingDomainUml2 - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceCountInEditingDomainUml2);

		// ==============================================
		// Unload null file
		try {
			EcorePlatformUtil.unloadFile(refWks.editingDomain10, null);
		} catch (Exception ex) {
			fail("Exception while input file is NULL: " + ex.getClass() + " " + ex.getLocalizedMessage());
		}
		// ---------------
		// Unload with null EditingDomain
		try {
			file = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4);
			EcorePlatformUtil.unloadFile(null, file);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceCountInEditingDomain10);

		} catch (Exception ex) {
			fail("Exception while input file is NULL: " + ex.getClass() + " " + ex.getLocalizedMessage());
		}
		// -----------------
		// Unload file with un correlative editing domain
		file = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4);
		EcorePlatformUtil.unloadFile(refWks.editingDomain20, file);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceCountInEditingDomain10);

		file = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		EcorePlatformUtil.unloadFile(refWks.editingDomain20, file);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceCountInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceCountInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceCountInEditingDomainUml2);

	}

	/**
	 * Test method for {@link EcorePlatformUtil#createURI(IPath)}
	 */
	public void testCreateURI() throws Exception {
		IFile hbFile10_10A_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertNotNull(hbFile10_10A_1);

		// Paths pointing to existing location inside workspace
		IPath existingFullPathInsideWorkspace = hbFile10_10A_1.getFullPath();
		URI expectedURI = URI.createPlatformResourceURI(existingFullPathInsideWorkspace.toString(), true);
		assertEquals(expectedURI, EcorePlatformUtil.createURI(existingFullPathInsideWorkspace));

		IPath existingLocationPathInsideWorkspace = hbFile10_10A_1.getLocation();
		assertEquals(expectedURI, EcorePlatformUtil.createURI(existingLocationPathInsideWorkspace));

		// Paths pointing to non-existing locations inside workspace
		IPath nonExistingFullPathInsideWorkspace = existingFullPathInsideWorkspace.removeLastSegments(1).append("dummy.xml");
		expectedURI = URI.createPlatformResourceURI(nonExistingFullPathInsideWorkspace.toString(), true);
		assertEquals(expectedURI, EcorePlatformUtil.createURI(nonExistingFullPathInsideWorkspace));

		IPath nonExistingLocationPathInsideWorkspace = existingLocationPathInsideWorkspace.removeLastSegments(1).append("dummy.xml");
		assertEquals(expectedURI, EcorePlatformUtil.createURI(nonExistingLocationPathInsideWorkspace));

		nonExistingFullPathInsideWorkspace = new Path("/opt/a/b/c/dummy.xml");
		expectedURI = URI.createPlatformResourceURI(nonExistingFullPathInsideWorkspace.toString(), true);
		assertEquals(expectedURI, EcorePlatformUtil.createURI(nonExistingFullPathInsideWorkspace));

		// Path pointing to location outside workspace
		IPath nonExistingLocationPathOutsideWorkspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().removeLastSegments(1)
				.append("dummy.xml");
		String device = nonExistingLocationPathOutsideWorkspace.getDevice();
		if (device != null) {
			// Under Windows, location path will include a drive letter
			// -> it will be interpreted as (non existing) location path outside workspace

			// Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=423284: convert drive letter to lower case
			String expectedPath = nonExistingLocationPathOutsideWorkspace.toString();
			expectedPath = expectedPath.substring(0, 1).toLowerCase() + expectedPath.substring(1);
			expectedURI = URI.createFileURI(expectedPath);

			assertEquals(expectedURI, EcorePlatformUtil.createURI(nonExistingLocationPathOutsideWorkspace));
		} else {
			// Under Linux, location path will not include any drive letter but start with a leading separator
			// -> it will be interpreted as (non existing) full path inside workspace
			expectedURI = URI.createPlatformResourceURI(nonExistingLocationPathOutsideWorkspace.toString(), true);

			assertEquals(expectedURI, EcorePlatformUtil.createURI(nonExistingLocationPathOutsideWorkspace));
		}
	}

	/**
	 * Test method for{@link EcorePlatformUtil#createPath(URI)}
	 */
	public void testCreatePath() throws Exception {
		IFile hbFile10_10A_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertNotNull(hbFile10_10A_1);

		String hbFile10_10A_1StringUri = DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1;
		// Given URI is platform URI
		String platformResourceURIPrefix = "platform:/resource";
		URI platformResouceURI = URI.createURI(platformResourceURIPrefix + "/" + hbFile10_10A_1StringUri, true);
		IPath hbFile10_10A_1Path = hbFile10_10A_1.getFullPath();

		assertEquals(hbFile10_10A_1Path, EcorePlatformUtil.createPath(platformResouceURI));

		// Given URI is FileURI
		URI fileURI = URI.createFileURI(hbFile10_10A_1.getLocation().toString());

		assertEquals(hbFile10_10A_1.getLocation(), EcorePlatformUtil.createPath(fileURI));
		// Given URI is PluginURI
		String pluginURIPrefix = "platform:/plugin";
		String pluginID = "org.eclipse.sphinx.emf.test";
		URI platformPluginURI = URI.createURI(pluginURIPrefix + "/" + pluginID + "/" + hbFile10_10A_1StringUri, true);

		assertEquals("/" + pluginID + "/" + hbFile10_10A_1StringUri, EcorePlatformUtil.createPath(platformPluginURI).toString());

		try {
			EcorePlatformUtil.createPath(null);
		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				fail("Exception whil input path is null: " + ex.getClass() + " " + ex.getLocalizedMessage());
			}
		}
	}

	/**
	 * Test method for {@link EcorePlatformUtil#createAbsoluteFileURI(IPath)}
	 */
	public void testCreateAbsoluteFileURI() throws Exception {
		IFile hbFile10_10A_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertNotNull(hbFile10_10A_1);

		IPath hbFile10_10A_1Path = hbFile10_10A_1.getFullPath();
		URI expectedURI = URI.createFileURI(hbFile10_10A_1.getLocation().toString());

		assertEquals(expectedURI, EcorePlatformUtil.createAbsoluteFileURI(hbFile10_10A_1Path));
		// TODO NPE: Null input
		// try {
		// IPath nullPath = null;
		// EcorePlatformUtil.createAbsoluteFileURI(nullPath);
		//
		// } catch (Exception ex) {
		// fail("Exception while input path is null: " + ex.getClass() + " " + ex.getLocalizedMessage());
		// }
	}

	/**
	 * Test method for {@link EcorePlatformUtil#createAbsoluteFileLocation(URI)}
	 */
	public void testCreateAbsoluteFileLocation() throws Exception {
		IFile hbFile10_10A_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertNotNull(hbFile10_10A_1);

		// File URI
		URI fileURI = URI.createFileURI(hbFile10_10A_1.getLocation().toString());
		assertEquals(hbFile10_10A_1.getLocation().toString().replace("//", "/"),
				EcorePlatformUtil.createAbsoluteFileLocation(fileURI).toString().replace("//", "/"));

		// Platform URI
		URI platformResouceURI = URI.createPlatformResourceURI(hbFile10_10A_1.getFullPath().toString(), true);
		assertEquals(hbFile10_10A_1.getLocation(), EcorePlatformUtil.createAbsoluteFileLocation(platformResouceURI));

		// TODO NPE: Null input
		// try {
		// URI nullUri = null;
		// EcorePlatformUtil.createAbsoluteFileLocation(nullUri);
		//
		// } catch (Exception ex) {
		// fail("Exception whil input path is null: " + ex.getClass() + " " + ex.getLocalizedMessage());
		// }
	}

	/**
	 * Test method for {@link EcorePlatformUtil#convertToAbsoluteFileLocation(IPath)}
	 */
	public void testConvertToAbsoluteFileLocation() throws Exception {

		IFile hbFile10_10A_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertNotNull(hbFile10_10A_1);
		// Given Path is full Path
		IPath convertToAbsoluteFileLocation = EcorePlatformUtil.convertToAbsoluteFileLocation(hbFile10_10A_1.getFullPath());
		assertEquals(hbFile10_10A_1.getLocation(), convertToAbsoluteFileLocation);
		// Given path is file location
		assertEquals(hbFile10_10A_1.getLocation(), EcorePlatformUtil.convertToAbsoluteFileLocation(hbFile10_10A_1.getLocation()));
		// Null input
		try {
			IPath nullPath = null;
			EcorePlatformUtil.convertToAbsoluteFileLocation(nullPath);

		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				fail("Exception whil input path is null: " + ex.getClass() + " " + ex.getLocalizedMessage());
			}
		}

	}

	/**
	 * Test method for {@link EcorePlatformUtil#isFileLoaded(IFile)}
	 *
	 * @throws Exception
	 */
	public void testIsFileLoaded() throws Exception {
		IFile loadedHBFile10_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile loadedHBFile10_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile loadedHBFile10_3 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertNotNull(loadedHBFile10_1);
		assertNotNull(loadedHBFile10_2);
		assertNotNull(loadedHBFile10_3);

		assertTrue(loadedHBFile10_1.isAccessible());
		assertTrue(loadedHBFile10_2.isAccessible());
		assertTrue(loadedHBFile10_3.isAccessible());

		assertTrue(EcorePlatformUtil.isFileLoaded(loadedHBFile10_1));
		assertTrue(EcorePlatformUtil.isFileLoaded(loadedHBFile10_2));
		assertTrue(EcorePlatformUtil.isFileLoaded(loadedHBFile10_3));
		// --------------------------------------------------------------------
		// Files Hummingbird20
		int initialResourcesInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		assertEquals(initialResourcesInEditingDomain20, refWks.editingDomain20.getResourceSet().getResources().size());
		IFile loadedHBFile20_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		IFile loadedHBFile20_2 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		IFile loadedHBFile20_3 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);

		assertNotNull(loadedHBFile20_1);
		assertNotNull(loadedHBFile20_2);
		assertNotNull(loadedHBFile20_3);

		assertTrue(loadedHBFile20_1.isAccessible());
		assertTrue(loadedHBFile20_2.isAccessible());
		assertTrue(loadedHBFile20_3.isAccessible());

		assertTrue(EcorePlatformUtil.isFileLoaded(loadedHBFile20_1));
		assertTrue(EcorePlatformUtil.isFileLoaded(loadedHBFile20_2));
		assertTrue(EcorePlatformUtil.isFileLoaded(loadedHBFile20_3));

		// --------------------------------------------------------------------
		// Files Uml2
		IFile loadedUml2File_1 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		IFile loadedUml2File_2 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		IFile loadedUml2File_3 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertNotNull(loadedUml2File_1);
		assertNotNull(loadedUml2File_2);
		assertNotNull(loadedUml2File_3);

		assertTrue(loadedUml2File_1.isAccessible());
		assertTrue(loadedUml2File_2.isAccessible());
		assertTrue(loadedUml2File_3.isAccessible());

		assertTrue(EcorePlatformUtil.isFileLoaded(loadedUml2File_1));
		assertTrue(EcorePlatformUtil.isFileLoaded(loadedUml2File_2));
		assertTrue(EcorePlatformUtil.isFileLoaded(loadedUml2File_2));

		// --------------------------------------------------------------------
		// Get file while resources is memory only
		String onlyInMemoryResourceName = "newResource.instancemodel";
		String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + onlyInMemoryResourceName;
		URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

		// We retrieve model root from file HB_FILE_NAME_20_20A_1
		IFile referenceFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		Resource resource = EcorePlatformUtil.getResource(referenceFile);
		assertNotNull(resource);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot = resource.getContents().get(0);
		assertNotNull(modelRoot);

		// we add the new resource.
		IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, onlyInMemoryResourceIPath,
				Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), modelRoot, true, null);
		waitForModelLoading();

		// We ensure that no underlying file exist on file system for our newly created resource.
		assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));

		// we retrieve the newly created resource
		Resource onlyInMemoryResource20 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
		assertNotNull(onlyInMemoryResource20);
		Path path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
		IFile fileOfResourceInMemoryOnly = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		assertTrue(EcorePlatformUtil.isFileLoaded(fileOfResourceInMemoryOnly));
		// --------------------------------------------------------------------
		// Given file is Unloaded

		synchronizedUnloadFile(loadedHBFile10_1);
		waitForModelLoading();
		assertFalse(EcorePlatformUtil.isFileLoaded(loadedHBFile10_1));
		assertTrue(EcorePlatformUtil.isFileLoaded(loadedHBFile10_2));
		assertTrue(EcorePlatformUtil.isFileLoaded(loadedHBFile10_3));

		synchronizedUnloadFile(loadedHBFile20_1);
		waitForModelLoading();
		assertFalse(EcorePlatformUtil.isFileLoaded(loadedHBFile20_1));
		assertTrue(EcorePlatformUtil.isFileLoaded(loadedHBFile20_2));
		assertTrue(EcorePlatformUtil.isFileLoaded(loadedHBFile20_3));

		synchronizedUnloadFile(loadedUml2File_1);
		waitForModelLoading();
		assertFalse(EcorePlatformUtil.isFileLoaded(loadedUml2File_1));
		assertTrue(EcorePlatformUtil.isFileLoaded(loadedUml2File_2));
		assertTrue(EcorePlatformUtil.isFileLoaded(loadedUml2File_2));

		// ------------------------------------------------------------------
		// Given file is deleted
		synchronizedDeleteFile(loadedHBFile10_2);
		assertFalse(loadedHBFile10_1.getName(), EcorePlatformUtil.isFileLoaded(loadedHBFile10_1));
		assertFalse(loadedHBFile10_2.getName(), EcorePlatformUtil.isFileLoaded(loadedHBFile10_2));
		assertTrue(loadedHBFile10_3.getName(), EcorePlatformUtil.isFileLoaded(loadedHBFile10_3));

		synchronizedDeleteFile(loadedHBFile10_3);
		assertFalse(loadedHBFile10_1.getName(), EcorePlatformUtil.isFileLoaded(loadedHBFile10_1));
		assertFalse(loadedHBFile10_2.getName(), EcorePlatformUtil.isFileLoaded(loadedHBFile10_2));
		assertFalse(loadedHBFile10_3.getName(), EcorePlatformUtil.isFileLoaded(loadedHBFile10_3));

		// --------------------------------------------------------------------
		// Given file is NULL
		IFile nullFile = null;
		assertFalse(EcorePlatformUtil.isFileLoaded(nullFile));

	}

	/**
	 * Test method for {@link EcorePlatformUtil#isFileLoaded(TransactionalEditingDomain, IFile)}
	 */
	public void testIsFileLoadedWithEditingDomain() throws Exception {
		IFile loadedHBFile10_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile loadedHBFile10_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile loadedHBFile10_3 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertNotNull(loadedHBFile10_1);
		assertNotNull(loadedHBFile10_2);
		assertNotNull(loadedHBFile10_3);

		assertTrue(loadedHBFile10_1.isAccessible());
		assertTrue(loadedHBFile10_2.isAccessible());
		assertTrue(loadedHBFile10_3.isAccessible());

		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedHBFile10_1));
		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedHBFile10_2));
		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedHBFile10_3));

		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain20, loadedHBFile10_1));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain20, loadedHBFile10_2));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain20, loadedHBFile10_3));
		// --------------------------------------------------------------------
		// Files AR3x
		IFile loadedHBFile20_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		IFile loadedHBFile20_2 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		IFile loadedHBFile20_3 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);

		assertNotNull(loadedHBFile20_1);
		assertNotNull(loadedHBFile20_2);
		assertNotNull(loadedHBFile20_3);

		assertTrue(loadedHBFile20_1.isAccessible());
		assertTrue(loadedHBFile20_2.isAccessible());
		assertTrue(loadedHBFile20_3.isAccessible());

		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomain20, loadedHBFile20_1));
		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomain20, loadedHBFile20_2));
		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomain20, loadedHBFile20_3));

		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedHBFile20_1));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedHBFile20_2));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedHBFile20_3));
		// --------------------------------------------------------------------
		// Files Uml
		IFile loadedUml2File_1 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		IFile loadedUml2File_2 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		IFile loadedUml2File_3 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertNotNull(loadedUml2File_1);
		assertNotNull(loadedUml2File_2);
		assertNotNull(loadedUml2File_3);

		assertTrue(loadedUml2File_1.isAccessible());
		assertTrue(loadedUml2File_2.isAccessible());
		assertTrue(loadedUml2File_3.isAccessible());

		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomainUml2, loadedUml2File_1));
		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomainUml2, loadedUml2File_2));
		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomainUml2, loadedUml2File_2));

		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain20, loadedUml2File_1));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain20, loadedUml2File_2));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain20, loadedUml2File_3));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedUml2File_1));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedUml2File_2));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedUml2File_3));

		// --------------------------------------------------------------------
		// Get file while resources is memory only
		String onlyInMemoryResourceName = "newResource.instancemodel";
		String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + onlyInMemoryResourceName;
		URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

		// We retrieve model root from file HB_FILE_NAME_20_20A_1
		IFile referenceFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		Resource resource = EcorePlatformUtil.getResource(referenceFile);
		assertNotNull(resource);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot = resource.getContents().get(0);
		assertNotNull(modelRoot);

		// we add the new resource.
		IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, onlyInMemoryResourceIPath,
				Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), modelRoot, true, null);
		waitForModelLoading();

		// We ensure that no underlying file exist on file system for our newly created resource.
		assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));

		// we retrieve the newly created resource
		Resource onlyInMemoryResource20 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
		assertNotNull(onlyInMemoryResource20);
		Path path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
		IFile fileOfResourceInMemoryOnly = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomain20, fileOfResourceInMemoryOnly));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, fileOfResourceInMemoryOnly));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomainUml2, fileOfResourceInMemoryOnly));
		// --------------------------------------------------------------------
		// Given file is Unloaded

		synchronizedUnloadFile(loadedHBFile10_1);
		waitForModelLoading();
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedHBFile10_1));
		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedHBFile10_2));
		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedHBFile10_3));

		synchronizedUnloadFile(loadedHBFile20_1);
		waitForModelLoading();
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain20, loadedHBFile20_1));
		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomain20, loadedHBFile20_2));
		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomain20, loadedHBFile20_3));

		synchronizedUnloadFile(loadedUml2File_1);
		waitForModelLoading();
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomainUml2, loadedUml2File_1));
		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomainUml2, loadedUml2File_2));
		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomainUml2, loadedUml2File_2));
		// ------------------------------------------------------------------
		// Given file is deleted
		synchronizedDeleteFile(loadedHBFile10_2);
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedHBFile10_1));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedHBFile10_2));
		assertTrue(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedHBFile10_3));

		synchronizedDeleteFile(loadedHBFile10_3);
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedHBFile10_1));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedHBFile10_2));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, loadedHBFile10_3));
		// ------------------------------------------------------------------
		// Given EditingDomain is NULL
		TransactionalEditingDomain nullEditingDomain = null;
		assertFalse(EcorePlatformUtil.isFileLoaded(nullEditingDomain, loadedHBFile10_1));
		assertFalse(EcorePlatformUtil.isFileLoaded(nullEditingDomain, loadedHBFile10_2));
		assertFalse(EcorePlatformUtil.isFileLoaded(nullEditingDomain, loadedHBFile10_3));
		assertFalse(EcorePlatformUtil.isFileLoaded(nullEditingDomain, loadedHBFile20_1));
		assertFalse(EcorePlatformUtil.isFileLoaded(nullEditingDomain, loadedHBFile20_2));
		assertFalse(EcorePlatformUtil.isFileLoaded(nullEditingDomain, loadedHBFile20_3));
		assertFalse(EcorePlatformUtil.isFileLoaded(nullEditingDomain, loadedUml2File_1));
		assertFalse(EcorePlatformUtil.isFileLoaded(nullEditingDomain, loadedUml2File_2));
		assertFalse(EcorePlatformUtil.isFileLoaded(nullEditingDomain, loadedUml2File_3));

		// --------------------------------------------------------------------
		// Given file is NULL
		IFile nullFile = null;
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain20, nullFile));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomain10, nullFile));
		assertFalse(EcorePlatformUtil.isFileLoaded(refWks.editingDomainUml2, nullFile));

	}

	/**
	 * Test method for {@link EcorePlatformUtil#isResourceLoaded(TransactionalEditingDomain, Resource)}
	 */
	public void testIsResourceLoaded() throws Exception {
		// Hummingbird10 Resources
		Resource loadedHBResource10_1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource loadedHBResource10_2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		Resource loadedHBResource10_3 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, true), false);

		assertNotNull(loadedHBResource10_1);
		assertNotNull(loadedHBResource10_2);
		assertNotNull(loadedHBResource10_3);

		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedHBResource10_1));
		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedHBResource10_2));
		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedHBResource10_3));

		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain20, loadedHBResource10_1));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain20, loadedHBResource10_2));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain20, loadedHBResource10_3));
		// --------------------------------------------------------------------
		// //Hummingbird20 Resources
		Resource loadedHBResource20_1 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		Resource loadedHBResource20_2 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);
		Resource loadedHBResource20_3 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, true), false);

		assertNotNull(loadedHBResource20_1);
		assertNotNull(loadedHBResource20_2);
		assertNotNull(loadedHBResource20_3);

		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain20, loadedHBResource20_1));
		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain20, loadedHBResource20_2));
		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain20, loadedHBResource20_3));

		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedHBResource20_1));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedHBResource20_2));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedHBResource20_3));
		// --------------------------------------------------------------------
		// Files Uml2

		Resource loadedResourceUml2_1 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		Resource loadedResourceUml2_2 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2, true), false);
		Resource loadedResourceUml2_3 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3, true), false);

		assertNotNull(loadedResourceUml2_1);
		assertNotNull(loadedResourceUml2_2);
		assertNotNull(loadedResourceUml2_3);

		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomainUml2, loadedResourceUml2_1));
		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomainUml2, loadedResourceUml2_2));
		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomainUml2, loadedResourceUml2_3));

		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain20, loadedResourceUml2_1));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain20, loadedResourceUml2_2));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain20, loadedResourceUml2_3));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedResourceUml2_1));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedResourceUml2_2));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedResourceUml2_3));

		// --------------------------------------------------------------------
		// Get file while resources is memory only
		String onlyInMemoryResourceName = "newResource.instancemodel";
		String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + onlyInMemoryResourceName;
		URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

		// We retrieve model root from file HB_FILE_NAME_20_20A_1
		IFile referenceFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		Resource resource = EcorePlatformUtil.getResource(referenceFile);
		assertNotNull(resource);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot = resource.getContents().get(0);
		assertNotNull(modelRoot);

		// we add the new resource.
		IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, onlyInMemoryResourceIPath,
				Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), modelRoot, true, null);
		waitForModelLoading();

		// We ensure that no underlying file exist on file system for our newly created resource.
		assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));

		// we retrieve the newly created resource
		Resource onlyInMemoryResource20 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
		assertNotNull(onlyInMemoryResource20);
		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain20, onlyInMemoryResource20));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, onlyInMemoryResource20));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomainUml2, onlyInMemoryResource20));
		// --------------------------------------------------------------------
		// Given file is Unloaded
		IFile hbFile10_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hbFile10_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile hbFile10_3 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertNotNull(hbFile10_1);
		assertNotNull(hbFile10_2);
		assertNotNull(hbFile10_3);

		ModelLoadManager.INSTANCE.unloadFile(hbFile10_1, true, new NullProgressMonitor());
		waitForModelLoading();
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedHBResource10_1));
		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedHBResource10_2));
		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedHBResource10_3));

		IFile hbFile20_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		IFile hbFile20_2 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		IFile hbFile20_3 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);

		assertNotNull(hbFile20_1);
		assertNotNull(hbFile20_2);
		assertNotNull(hbFile20_3);

		ModelLoadManager.INSTANCE.unloadFile(hbFile20_1, true, new NullProgressMonitor());
		waitForModelLoading();
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain20, loadedHBResource20_1));
		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain20, loadedHBResource20_2));
		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain20, loadedHBResource20_3));

		IFile Uml2File_1 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		IFile Uml2File_2 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		IFile Uml2File_3 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertNotNull(Uml2File_1);
		assertNotNull(Uml2File_2);
		assertNotNull(Uml2File_3);

		ModelLoadManager.INSTANCE.unloadFile(Uml2File_1, true, new NullProgressMonitor());
		waitForModelLoading();
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomainUml2, loadedResourceUml2_1));
		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomainUml2, loadedResourceUml2_2));
		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomainUml2, loadedResourceUml2_3));
		// ------------------------------------------------------------------
		// Given file is deleted
		synchronizedDeleteFile(hbFile10_2);
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedHBResource10_1));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedHBResource10_2));
		assertTrue(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedHBResource10_3));

		synchronizedDeleteFile(hbFile10_3);
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedHBResource10_1));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedHBResource10_2));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, loadedHBResource10_3));
		// ------------------------------------------------------------------
		// Given EditingDomain is NULL
		TransactionalEditingDomain nullEditingDomain = null;
		assertFalse(EcorePlatformUtil.isResourceLoaded(nullEditingDomain, loadedHBResource10_1));
		assertFalse(EcorePlatformUtil.isResourceLoaded(nullEditingDomain, loadedHBResource10_2));
		assertFalse(EcorePlatformUtil.isResourceLoaded(nullEditingDomain, loadedHBResource10_3));
		assertFalse(EcorePlatformUtil.isResourceLoaded(nullEditingDomain, loadedHBResource20_1));
		assertFalse(EcorePlatformUtil.isResourceLoaded(nullEditingDomain, loadedHBResource20_2));
		assertFalse(EcorePlatformUtil.isResourceLoaded(nullEditingDomain, loadedHBResource20_3));
		assertFalse(EcorePlatformUtil.isResourceLoaded(nullEditingDomain, loadedResourceUml2_1));
		assertFalse(EcorePlatformUtil.isResourceLoaded(nullEditingDomain, loadedResourceUml2_2));
		assertFalse(EcorePlatformUtil.isResourceLoaded(nullEditingDomain, loadedResourceUml2_3));

		// --------------------------------------------------------------------
		// Given file is NULL
		Resource nullResource = null;
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain20, nullResource));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomain10, nullResource));
		assertFalse(EcorePlatformUtil.isResourceLoaded(refWks.editingDomainUml2, nullResource));

	}

	/**
	 * Test method for {@link EcorePlatformUtil#getFile(EObject)}
	 */
	public void testGetFileFromEObject() throws Exception {
		// Hummingbird20 EObject
		IFile hbFile20_20A_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertNotNull(hbFile20_20A_1);
		assertTrue(hbFile20_20A_1.isAccessible());

		Resource hbResource20 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		assertNotNull(hbResource20);
		assertFalse(hbResource20.getContents().isEmpty());
		EObject rootObject20 = hbResource20.getContents().get(0);

		assertEquals(hbFile20_20A_1, EcorePlatformUtil.getFile(rootObject20));
		// --------------------------------------------------------------------
		// Hummingbird10 EObject
		IFile hbFile10_10A_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertNotNull(hbFile10_10A_1);
		assertTrue(hbFile10_10A_1.isAccessible());
		Resource hbResource10 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);

		assertNotNull(hbResource10);
		assertFalse(hbResource10.getContents().isEmpty());
		EObject rootObject10 = hbResource10.getContents().get(0);

		assertEquals(hbFile10_10A_1, EcorePlatformUtil.getFile(rootObject10));
		// --------------------------------------------------------------------
		// Uml2 EObject
		IFile shinxFile_3D_1 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertNotNull(shinxFile_3D_1);
		assertTrue(shinxFile_3D_1.isAccessible());
		Resource Uml2Resource = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);

		assertNotNull(Uml2Resource);
		assertFalse(Uml2Resource.getContents().isEmpty());
		EObject uml2Object = Uml2Resource.getContents().get(0);

		assertEquals(shinxFile_3D_1, EcorePlatformUtil.getFile(uml2Object));
		// --------------------------------------------------------------------
		// Get file while resources is memory only
		String onlyInMemoryResourceName = "newResource.instanceModel";
		String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + onlyInMemoryResourceName;
		URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

		// We retrieve model root from file HB_FILE_NAME_20_20A_1
		IFile referenceFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		Resource resource = EcorePlatformUtil.getResource(referenceFile);
		assertNotNull(resource);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot = resource.getContents().get(0);
		assertNotNull(modelRoot);

		// we add the new resource.
		IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, onlyInMemoryResourceIPath,
				Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), modelRoot, true, null);
		waitForModelLoading();

		// We ensure that no underlying file exist on file system for our newly created resource.
		assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));

		// we retrieve the newly created resource
		Resource onlyInMemoryResource20 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
		assertNotNull(onlyInMemoryResource20);
		Path path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
		IFile expectedFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

		IFile fileOfResourceInMemoryOnly = EcorePlatformUtil.getFile(modelRoot);
		assertNotNull(fileOfResourceInMemoryOnly);
		assertEquals(expectedFile, fileOfResourceInMemoryOnly);
		assertFalse(fileOfResourceInMemoryOnly.isAccessible());
		// --------------------------------------------------------------------
		// Get file of object in unloaded resource
		synchronizedUnloadProject(refWks.hbProject10_A, false);
		assertNotNull(rootObject10);
		assertNull(EcorePlatformUtil.getFile(rootObject10));
		// --------------------------------------------------------------------
		// Get deleted file from its object
		hbFile20_20A_1.delete(true, new NullProgressMonitor());
		assertNotNull(rootObject20);
		assertNotNull(EcorePlatformUtil.getFile(rootObject20));
		assertFalse(EcorePlatformUtil.getFile(rootObject20).isAccessible());
		// --------------------------------------------------------------------
		// Given Object is null
		EObject nullObject = null;
		assertNull(EcorePlatformUtil.getFile(nullObject));

	}

	/**
	 * Test method for {@link EcorePlatformUtil#getFile(URI)}
	 *
	 * @throws Exception
	 * @throws OperationCanceledException
	 */
	public void testGetFileFromUri() throws Exception {
		// Test with a resource with an underlying file

		// Context: URI of Hummingbird 20 resource
		IFile hbFile20_20A_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertNotNull(hbFile20_20A_1);
		assertTrue(hbFile20_20A_1.isAccessible());
		String hbFile20_20A_1Path = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1;

		Resource hbResource20 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		assertNotNull(hbResource20);
		assertEquals(hbFile20_20A_1, EcorePlatformUtil.getFile(hbResource20.getURI()));
		assertEquals(hbFile20_20A_1, EcorePlatformUtil.getFile(EcorePlatformUtil.createAbsoluteFileURI(hbFile20_20A_1.getFullPath())));
		assertEquals(hbFile20_20A_1, EcorePlatformUtil.getFile(URI.createPlatformResourceURI(hbFile20_20A_1Path, true)));
		assertEquals(hbFile20_20A_1, EcorePlatformUtil.getFile(URI.createURI(hbFile20_20A_1Path, true)));
		// --------------------------------------------------------------------
		// Context: URI of Hummingbird 10 resource
		IFile hbFile10_10A_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertNotNull(hbFile10_10A_1);
		assertTrue(hbFile10_10A_1.isAccessible());
		String hbFile10_10A_1Path = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1;

		Resource hbResource10 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		assertNotNull(hbResource10);
		assertEquals(hbFile10_10A_1, EcorePlatformUtil.getFile(hbResource10.getURI()));
		assertEquals(hbFile10_10A_1, EcorePlatformUtil.getFile(EcorePlatformUtil.createAbsoluteFileURI(hbFile10_10A_1.getFullPath())));
		assertEquals(hbFile10_10A_1, EcorePlatformUtil.getFile(URI.createPlatformResourceURI(hbFile10_10A_1Path, true)));
		assertEquals(hbFile10_10A_1, EcorePlatformUtil.getFile(URI.createURI(hbFile10_10A_1Path, true)));
		// --------------------------------------------------------------------
		// Context: URI of UML resource

		IFile fileUml2_20D_1 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertNotNull(fileUml2_20D_1);
		assertTrue(fileUml2_20D_1.isAccessible());
		String fileUml2_20D_1Path = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
				+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1;

		Resource resourceUml2 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		assertNotNull(resourceUml2);
		assertEquals(fileUml2_20D_1, EcorePlatformUtil.getFile(resourceUml2.getURI()));
		assertEquals(fileUml2_20D_1, EcorePlatformUtil.getFile(EcorePlatformUtil.createAbsoluteFileURI(fileUml2_20D_1.getFullPath())));
		assertEquals(fileUml2_20D_1, EcorePlatformUtil.getFile(URI.createPlatformResourceURI(fileUml2_20D_1Path, true)));
		assertEquals(fileUml2_20D_1, EcorePlatformUtil.getFile(URI.createURI(fileUml2_20D_1Path, true)));

		// --------------------------------------------------------------------
		// Test with a resource existing only in memory
		String onlyInMemoryResourceName = "newResource.instancemodel";
		String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + onlyInMemoryResourceName;
		URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

		EObject modelRoot = createHummingbird20InstanceModelRoot();
		assertNotNull(modelRoot);
		// we add the new resource.
		IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, onlyInMemoryResourceIPath,
				Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), modelRoot, true, null);
		waitForModelLoading();

		// We ensure that no underlying file exist on file system for our newly created resource.
		assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));

		// we retrieve the newly created resource
		Resource onlyInMemoryResource20 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
		assertNotNull(onlyInMemoryResource20);
		Path path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
		IFile expectedFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

		assertEquals(expectedFile, EcorePlatformUtil.getFile(onlyInMemoryResource20.getURI()));
		assertEquals(expectedFile, EcorePlatformUtil.getFile(EcorePlatformUtil.createAbsoluteFileURI(onlyInMemoryResourceIPath)));
		assertEquals(expectedFile, EcorePlatformUtil.getFile(URI.createPlatformResourceURI(onlyInMemoryResourcePath, true)));
		assertEquals(expectedFile, EcorePlatformUtil.getFile(URI.createURI(onlyInMemoryResourcePath, true)));
		// --------------------------------------------------------------------
		// Given URI is NULL
		URI nullUri = null;
		assertNull(EcorePlatformUtil.getFile(nullUri));
		// --------------------------------------------------------------------
		// Given URI is of deleted files
		synchronizedDeleteFile(hbFile10_10A_1);
		assertEquals(hbFile10_10A_1, EcorePlatformUtil.getFile(hbResource10.getURI()));
		assertFalse(EcorePlatformUtil.getFile(hbResource10.getURI()).isAccessible());

		assertEquals(hbFile10_10A_1, EcorePlatformUtil.getFile(EcorePlatformUtil.createAbsoluteFileURI(hbFile10_10A_1.getFullPath())));
		assertFalse(EcorePlatformUtil.getFile(EcorePlatformUtil.createAbsoluteFileURI(hbFile10_10A_1.getFullPath())).isAccessible());

		assertEquals(hbFile10_10A_1, EcorePlatformUtil.getFile(URI.createPlatformResourceURI(hbFile10_10A_1Path, true)));
		assertFalse(EcorePlatformUtil.getFile(URI.createPlatformResourceURI(hbFile10_10A_1Path, true)).isAccessible());

		assertEquals(hbFile10_10A_1, EcorePlatformUtil.getFile(URI.createURI(hbFile10_10A_1Path, true)));
		assertFalse(EcorePlatformUtil.getFile(URI.createURI(hbFile10_10A_1Path, true)).isAccessible());
		// --------------------------------------------------------------------
		// Given URI is belong to resource outside workspace
		java.net.URI outsideWrkUri = getTestFileAccessor().getInputFileURI("hbFile20.instancemodel");
		URI outsideWrksUri = getTestFileAccessor().convertToEMFURI(outsideWrkUri);
		// Cannot get file outside workspace
		IFile fileOutSideWrks = EcorePlatformUtil.getFile(outsideWrksUri);
		assertNull(fileOutSideWrks);
	}

	/**
	 * Test method for {@link EcorePlatformUtil#getFile(Resource)}
	 *
	 * @throws InterruptedException
	 * @throws OperationCanceledException
	 */
	public void testGetFileFromResouce() throws Exception {

		// --------------------------------------------------------------------
		// File Hummingbird10
		IFile hbFile10_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hbFile10_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile hbFile10_3 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertNotNull(hbFile10_1);
		assertNotNull(hbFile10_2);
		assertNotNull(hbFile10_3);
		assertTrue(hbFile10_1.exists());
		assertTrue(hbFile10_2.exists());
		assertTrue(hbFile10_3.exists());

		URI hbResource10_1URI = EcorePlatformUtil.createURI(hbFile10_1.getFullPath());
		Resource hbResource10_1 = refWks.editingDomain10.getResourceSet().getResource(hbResource10_1URI, false);
		URI hbResource10_2URI = EcorePlatformUtil.createURI(hbFile10_2.getFullPath());
		Resource hbResource10_2 = refWks.editingDomain10.getResourceSet().getResource(hbResource10_2URI, false);
		URI hbResource10_3URI = EcorePlatformUtil.createURI(hbFile10_3.getFullPath());
		Resource hbResource10_3 = refWks.editingDomain10.getResourceSet().getResource(hbResource10_3URI, false);

		assertNotNull(hbResource10_1);
		assertNotNull(hbResource10_2);
		assertNotNull(hbResource10_3);

		assertEquals(hbFile10_1, EcorePlatformUtil.getFile(hbResource10_1));
		assertEquals(hbFile10_2, EcorePlatformUtil.getFile(hbResource10_2));
		assertEquals(hbFile10_3, EcorePlatformUtil.getFile(hbResource10_3));
		// --------------------------------------------------------------------
		// File Hummingbird20
		int initialResourcesInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);

		assertEquals(initialResourcesInEditingDomain20, refWks.editingDomain20.getResourceSet().getResources().size());
		IFile hbFile20_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		IFile hbFile20_2 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		IFile hbFile20_3 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);
		IFile hbFile20_4 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile hbFile20_5 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile hbFile20_6 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);

		assertNotNull(hbFile20_1);
		assertNotNull(hbFile20_2);
		assertNotNull(hbFile20_3);
		assertNotNull(hbFile20_4);
		assertNotNull(hbFile20_5);
		assertNotNull(hbFile20_6);
		assertTrue(hbFile20_1.exists());
		assertTrue(hbFile20_2.exists());
		assertTrue(hbFile20_3.exists());
		assertTrue(hbFile20_4.exists());
		assertTrue(hbFile20_5.exists());
		assertTrue(hbFile20_6.exists());

		Resource hbResource20_1 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		Resource hbResource20_2 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);
		Resource hbResource20_3 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, true), false);
		Resource hbResource20_4 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		Resource hbResource20_5 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, true), false);
		Resource hbResource20_6 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, true), false);
		assertNotNull(hbResource20_1);
		assertNotNull(hbResource20_2);
		assertNotNull(hbResource20_3);
		assertNotNull(hbResource20_4);
		assertNotNull(hbResource20_5);
		assertNotNull(hbResource20_6);

		assertEquals(hbFile20_1, EcorePlatformUtil.getFile(hbResource20_1));
		assertEquals(hbFile20_2, EcorePlatformUtil.getFile(hbResource20_2));
		assertEquals(hbFile20_3, EcorePlatformUtil.getFile(hbResource20_3));
		assertEquals(hbFile20_4, EcorePlatformUtil.getFile(hbResource20_4));
		assertEquals(hbFile20_5, EcorePlatformUtil.getFile(hbResource20_5));
		assertEquals(hbFile20_6, EcorePlatformUtil.getFile(hbResource20_6));
		// --------------------------------------------------------------------
		// Uml2 File
		int initialResourcesIneditingDomainUml2 = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, initialResourcesIneditingDomainUml2);
		IFile uml2File_1 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		IFile uml2File_2 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		IFile uml2File_3 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertNotNull(uml2File_1);
		assertNotNull(uml2File_2);
		assertNotNull(uml2File_3);
		assertTrue(uml2File_1.exists());
		assertTrue(uml2File_2.exists());
		assertTrue(uml2File_3.exists());

		Resource uml2Resource_1 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		Resource uml2Resource_2 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2, true), false);
		Resource uml2Resource_3 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3, true), false);

		assertNotNull(uml2Resource_1);
		assertNotNull(uml2Resource_2);
		assertNotNull(uml2Resource_3);

		assertEquals(uml2File_1, EcorePlatformUtil.getFile(uml2Resource_1));
		assertEquals(uml2File_2, EcorePlatformUtil.getFile(uml2Resource_2));
		assertEquals(uml2File_3, EcorePlatformUtil.getFile(uml2Resource_3));
		// --------------------------------------------------------------------
		// Test with a resource existing only in memory
		String onlyInMemoryResourceName = "newResource.instancemodel";
		String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + onlyInMemoryResourceName;
		URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

		// We retrieve model root from file HB_FILE_NAME_20_20A_1
		EObject modelRoot = createHummingbird20InstanceModelRoot();
		// we add the new resource.
		IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, onlyInMemoryResourceIPath,
				Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), modelRoot, true, null);
		waitForModelLoading();

		// We ensure that no underlying file exist on file system for our newly created resource.
		assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));

		// we retrieve the newly created resource
		Resource onlyInMemoryResource20 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
		assertNotNull(onlyInMemoryResource20);
		Path path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
		IFile expectedFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		assertEquals(expectedFile, EcorePlatformUtil.getFile(onlyInMemoryResource20));
		assertFalse(EcorePlatformUtil.getFile(onlyInMemoryResource20).isAccessible());
		// --------------------------------------------------------------------
		// Given resource is NULL
		Resource nullResource = null;
		assertNull(EcorePlatformUtil.getFile(nullResource));

	}

	/**
	 * Test method for {@link EcorePlatformUtil#getFile(org.eclipse.emf.ecore.util.FeatureMap.Entry)}
	 */
	public void testGetFileFromFeatureMapEntry() throws Exception {
		Resource hbResource20A_1 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);

		assertNotNull(hbResource20A_1);
		assertFalse(hbResource20A_1.getContents().isEmpty());
		assertTrue(hbResource20A_1.getContents().get(0) instanceof org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application);
		org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application modelRoot20A_1 = (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application) hbResource20A_1
				.getContents().get(0);
		assertNotNull(modelRoot20A_1);

		assertFalse(modelRoot20A_1.getComponents().isEmpty());
		org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component component = modelRoot20A_1.getComponents().get(0);
		assertNotNull(component);

		assertFalse(component.getParameterExpressions().isEmpty());
		ParameterExpression parameterExpression = component.getParameterExpressions().get(0);
		assertNotNull(parameterExpression);
		assertFalse(parameterExpression.getMixed().isEmpty());
		Entry testEntry = parameterExpression.getMixed().get(0);

		assertEquals(InstanceModel20Package.Literals.PARAMETER_EXPRESSION__EXPRESSIONS, testEntry.getEStructuralFeature());

		IFile file = EcorePlatformUtil.getFile(testEntry);
		assertNotNull(file);

		// Given Entry is NULL
		Entry nullEntry = null;
		assertNull(EcorePlatformUtil.getFile(nullEntry));

		// --------------------------------------------------------------------
		// Given Entry belong to unloaded resource
		IFile hbFile20_20A_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		ModelLoadManager.INSTANCE.unloadFile(hbFile20_20A_1, true, new NullProgressMonitor());
		waitForModelLoading();
		assertNull(EcorePlatformUtil.getFile(testEntry));
	}

	/**
	 * Test method for {@link EcorePlatformUtil#getFile(org.eclipse.emf.edit.provider.IWrapperItemProvider)}
	 */
	public void testGetFileFromIWrapperItemProvider() throws Exception {
		IFile hbFile20_20A_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertNotNull(hbFile20_20A_1);
		assertTrue(hbFile20_20A_1.isAccessible());

		Resource hb20Resource = EcorePlatformUtil.getResource(hbFile20_20A_1);
		assertFalse(hb20Resource.getContents().isEmpty());
		org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application hb20Application = (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application) hb20Resource
				.getContents().get(0);
		assertNotNull(hb20Application);

		assertFalse(hb20Application.getComponents().isEmpty());
		org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component component = hb20Application.getComponents().get(0);

		WrapperItemProvider wrapperItemProvider = new WrapperItemProvider(component, hb20Application,
				InstanceModel20Package.eINSTANCE.getApplication_Components(), 1,
				((AdapterFactoryEditingDomain) WorkspaceEditingDomainUtil.getEditingDomain(hbFile20_20A_1)).getAdapterFactory());

		assertEquals(hbFile20_20A_1, EcorePlatformUtil.getFile(wrapperItemProvider));

		// --------------------------------------------------------------------
		// Input is null
		WrapperItemProvider nullItemProvider = null;
		assertNull(EcorePlatformUtil.getFile(nullItemProvider));
		// --------------------------------------------------------------------
		// WrapperItemProvider of object in unloaded resource
		ModelLoadManager.INSTANCE.unloadFile(hbFile20_20A_1, true, new NullProgressMonitor());
		waitForModelLoading();
		assertNull(EcorePlatformUtil.getFile(wrapperItemProvider));
	}

	/**
	 * Test method for {@link EcorePlatformUtil#getResource(EObject)}
	 */
	public void testGetResourceFromEObject() throws Exception {
		// Hummingbird20 EObject
		Resource hbResource20 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);

		assertNotNull(hbResource20);
		assertFalse(hbResource20.getContents().isEmpty());
		EObject object3x = hbResource20.getContents().get(0);

		assertSame(hbResource20, EcorePlatformUtil.getResource(object3x));
		// --------------------------------------------------------------------
		// Hummingbird10 EObject
		IFile hbFile10_10A_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertNotNull(hbFile10_10A_1);
		assertTrue(hbFile10_10A_1.isAccessible());
		Resource hbResource10 = null;
		for (Resource res : refWks.editingDomain10.getResourceSet().getResources()) {
			if (DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1.equals(res.getURI().lastSegment())) {
				hbResource10 = res;
				break;
			}
		}
		assertNotNull(hbResource10);
		assertFalse(hbResource10.getContents().isEmpty());
		EObject object21 = hbResource10.getContents().get(0);

		assertSame(hbResource10, EcorePlatformUtil.getResource(object21));
		// --------------------------------------------------------------------
		// Uml2 EObject
		IFile uml2File_20D_1 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertNotNull(uml2File_20D_1);
		assertTrue(uml2File_20D_1.isAccessible());
		Resource uml2Resource = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		assertNotNull(uml2Resource);
		assertFalse(uml2Resource.getContents().isEmpty());
		EObject uml2Object = uml2Resource.getContents().get(0);

		assertSame(uml2Resource, EcorePlatformUtil.getResource(uml2Object));
		// --------------------------------------------------------------------
		// Given object is NULL
		EObject nullEObject = null;
		assertNull(EcorePlatformUtil.getResource(nullEObject));
		// --------------------------------------------------------------------
		// Given object belong to unloaded resource
		ModelLoadManager.INSTANCE.unloadFile(hbFile10_10A_1, true, new NullProgressMonitor());
		waitForModelLoading();
		assertNotNull(object21);
		assertNull(EcorePlatformUtil.getResource(object21));
	}

	/**
	 * Test method for {@link EcorePlatformUtil#getResource(URI)}
	 */
	public void testGetResourceFromURI() throws Exception {

		String hbFile20_20A_1StringUri = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1;
		String hbFile20_20A_2StringUri = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2;
		String hbFile20_20A_3StringUri = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3;
		String hbFile20_20D_1StringUri = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1;
		String hbFile20_20D_2StringUri = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2;
		String hbFile20_20D_3StringUri = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3;

		Resource hbResource20_1 = null;
		Resource hbResource20_2 = null;
		Resource hbResource20_3 = null;
		Resource hbResource20_4 = null;
		Resource hbResource20_5 = null;
		Resource hbResource20_6 = null;
		for (Resource res : refWks.editingDomain20.getResourceSet().getResources()) {
			if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1.equals(res.getURI().lastSegment())) {
				hbResource20_1 = res;
			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2.equals(res.getURI().lastSegment())) {
				hbResource20_2 = res;
			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3.equals(res.getURI().lastSegment())) {
				hbResource20_3 = res;
			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1.equals(res.getURI().lastSegment())) {
				hbResource20_4 = res;
			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2.equals(res.getURI().lastSegment())) {
				hbResource20_5 = res;
			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3.equals(res.getURI().lastSegment())) {
				hbResource20_6 = res;
			}
		}
		assertNotNull(hbResource20_1);
		assertNotNull(hbResource20_2);
		assertNotNull(hbResource20_3);
		assertNotNull(hbResource20_4);
		assertNotNull(hbResource20_5);
		assertNotNull(hbResource20_6);

		// PlatformResource URI
		assertEquals(hbResource20_1, EcorePlatformUtil.getResource(hbResource20_1.getURI()));
		assertEquals(hbResource20_2, EcorePlatformUtil.getResource(hbResource20_2.getURI()));
		assertEquals(hbResource20_3, EcorePlatformUtil.getResource(hbResource20_3.getURI()));
		assertEquals(hbResource20_4, EcorePlatformUtil.getResource(hbResource20_4.getURI()));
		assertEquals(hbResource20_5, EcorePlatformUtil.getResource(hbResource20_5.getURI()));
		assertEquals(hbResource20_6, EcorePlatformUtil.getResource(hbResource20_6.getURI()));

		assertEquals(hbResource20_1, EcorePlatformUtil.getResource(URI.createPlatformResourceURI(hbFile20_20A_1StringUri, true)));
		assertEquals(hbResource20_2, EcorePlatformUtil.getResource(URI.createPlatformResourceURI(hbFile20_20A_2StringUri, true)));
		assertEquals(hbResource20_3, EcorePlatformUtil.getResource(URI.createPlatformResourceURI(hbFile20_20A_3StringUri, true)));
		assertEquals(hbResource20_4, EcorePlatformUtil.getResource(URI.createPlatformResourceURI(hbFile20_20D_1StringUri, true)));
		assertEquals(hbResource20_5, EcorePlatformUtil.getResource(URI.createPlatformResourceURI(hbFile20_20D_2StringUri, true)));
		assertEquals(hbResource20_6, EcorePlatformUtil.getResource(URI.createPlatformResourceURI(hbFile20_20D_3StringUri, true)));

		// Workspace-relative absolute URIs
		assertEquals(hbResource20_1, EcorePlatformUtil.getResource(URI.createURI(hbFile20_20A_1StringUri, true)));
		assertEquals(hbResource20_2, EcorePlatformUtil.getResource(URI.createURI(hbFile20_20A_2StringUri, true)));
		assertEquals(hbResource20_3, EcorePlatformUtil.getResource(URI.createURI(hbFile20_20A_3StringUri, true)));
		assertEquals(hbResource20_4, EcorePlatformUtil.getResource(URI.createURI(hbFile20_20D_1StringUri, true)));
		assertEquals(hbResource20_5, EcorePlatformUtil.getResource(URI.createURI(hbFile20_20D_2StringUri, true)));
		assertEquals(hbResource20_6, EcorePlatformUtil.getResource(URI.createURI(hbFile20_20D_3StringUri, true)));

		// Absolute file URIs
		URI hbFile20_20A_1FileUri = URI.createFileURI(hbFile20_20A_1StringUri);
		// Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=425631: create file URI manually when running
		// under Windows
		if (org.eclipse.core.runtime.Platform.OS_WIN32.equals(org.eclipse.core.runtime.Platform.getOS())) {
			hbFile20_20A_1FileUri = URI.createURI("file:" + hbFile20_20A_1StringUri);
		}
		assertTrue("Unexpected non-file URI: " + hbFile20_20A_1FileUri, hbFile20_20A_1FileUri.isFile());
		assertNull(EcorePlatformUtil.getResource(hbFile20_20A_1FileUri));
		URI hbFile20_20A_2FileUri = URI.createFileURI(hbFile20_20A_2StringUri);
		// Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=425631: create file URI manually when running
		// under Windows
		if (org.eclipse.core.runtime.Platform.OS_WIN32.equals(org.eclipse.core.runtime.Platform.getOS())) {
			hbFile20_20A_2FileUri = URI.createURI("file:" + hbFile20_20A_2StringUri);
		}
		assertTrue("Unexpected non-file URI: " + hbFile20_20A_2FileUri, hbFile20_20A_2FileUri.isFile());
		assertNull(EcorePlatformUtil.getResource(hbFile20_20A_2FileUri));
		URI hbFile20_20A_3FileUri = URI.createFileURI(hbFile20_20A_3StringUri);
		// Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=425631: create file URI manually when running
		// under Windows
		if (org.eclipse.core.runtime.Platform.OS_WIN32.equals(org.eclipse.core.runtime.Platform.getOS())) {
			hbFile20_20A_3FileUri = URI.createURI("file:" + hbFile20_20A_3StringUri);
		}
		assertTrue("Unexpected non-file URI: " + hbFile20_20A_3FileUri, hbFile20_20A_3FileUri.isFile());
		assertNull(EcorePlatformUtil.getResource(hbFile20_20A_3FileUri));
		URI hbFile20_20D_1FileUri = URI.createFileURI(hbFile20_20D_1StringUri);
		// Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=425631: create file URI manually when running
		// under Windows
		if (org.eclipse.core.runtime.Platform.OS_WIN32.equals(org.eclipse.core.runtime.Platform.getOS())) {
			hbFile20_20D_1FileUri = URI.createURI("file:" + hbFile20_20D_1StringUri);
		}
		assertTrue("Unexpected non-file URI: " + hbFile20_20D_1FileUri, hbFile20_20D_1FileUri.isFile());
		assertNull(EcorePlatformUtil.getResource(hbFile20_20D_1FileUri));
		URI hbFile20_20D_2FileUri = URI.createFileURI(hbFile20_20D_2StringUri);
		// Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=425631: create file URI manually when running
		// under Windows
		if (org.eclipse.core.runtime.Platform.OS_WIN32.equals(org.eclipse.core.runtime.Platform.getOS())) {
			hbFile20_20D_2FileUri = URI.createURI("file:" + hbFile20_20D_2StringUri);
		}
		assertTrue("Unexpected non-file URI: " + hbFile20_20D_2FileUri, hbFile20_20D_2FileUri.isFile());
		assertNull(EcorePlatformUtil.getResource(hbFile20_20D_2FileUri));
		URI hbFile20_20D_3FileUri = URI.createFileURI(hbFile20_20D_3StringUri);
		// Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=425631: create file URI manually when running
		// under Windows
		if (org.eclipse.core.runtime.Platform.OS_WIN32.equals(org.eclipse.core.runtime.Platform.getOS())) {
			hbFile20_20D_3FileUri = URI.createURI("file:" + hbFile20_20D_3StringUri);
		}
		assertTrue("Unexpected non-file URI: " + hbFile20_20D_3FileUri, hbFile20_20D_3FileUri.isFile());
		assertNull(EcorePlatformUtil.getResource(hbFile20_20D_3FileUri));

		// --------------------------------------------------------------------
		// Given URI is unloaded resource's uri
		IFile fileToUnload = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertNotNull(fileToUnload);
		ModelLoadManager.INSTANCE.unloadFile(fileToUnload, true, new NullProgressMonitor());
		waitForModelLoading();
		assertNotNull(hbResource20_1);
		assertNull(EcorePlatformUtil.getResource(hbResource20_1.getURI()));
		assertNull(EcorePlatformUtil.getResource(hbFile20_20A_1FileUri));
		assertNull(EcorePlatformUtil.getResource(URI.createURI(hbFile20_20A_1StringUri, true)));
		assertNull(EcorePlatformUtil.getResource(URI.createPlatformResourceURI(hbFile20_20A_1StringUri, true)));
		// --------------------------------------------------------------------
		// Given URI is outside of workspace
		String hbFile20_20B_1StringUri = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1;
		assertNull(EcorePlatformUtil.getResource(URI.createFileURI(hbFile20_20B_1StringUri)));
		assertNull(EcorePlatformUtil.getResource(URI.createURI(hbFile20_20B_1StringUri, true)));
		assertNull(EcorePlatformUtil.getResource(URI.createPlatformResourceURI(hbFile20_20B_1StringUri, true)));

		// --------------------------------------------------------------------
		// Given URI is NULL
		URI nullUri = null;
		assertNull(EcorePlatformUtil.getResource(nullUri));

	}

	/**
	 * Test method for {@link EcorePlatformUtil#getResource(IFile)}
	 */
	public void testGetResourceFromFile() throws Exception {
		// File Hummingbird10
		IFile hbFile10_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hbFile10_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile hbFile10_3 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertNotNull(hbFile10_1);
		assertNotNull(hbFile10_2);
		assertNotNull(hbFile10_3);
		assertTrue(hbFile10_1.exists());
		assertTrue(hbFile10_2.exists());
		assertTrue(hbFile10_3.exists());

		Resource hbResource10_1 = null;
		Resource hbResource10_2 = null;
		Resource hbResource10_3 = null;
		for (Resource res : refWks.editingDomain10.getResourceSet().getResources()) {
			if (DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1.equals(res.getURI().lastSegment())) {
				hbResource10_1 = res;
			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2.equals(res.getURI().lastSegment())) {
				hbResource10_2 = res;
			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3.equals(res.getURI().lastSegment())) {
				hbResource10_3 = res;
			}
		}
		assertNotNull(hbResource10_1);
		assertNotNull(hbResource10_2);
		assertNotNull(hbResource10_3);

		assertEquals(hbResource10_1, EcorePlatformUtil.getResource(hbFile10_1));
		assertEquals(hbResource10_2, EcorePlatformUtil.getResource(hbFile10_2));
		assertEquals(hbResource10_3, EcorePlatformUtil.getResource(hbFile10_3));
		// --------------------------------------------------------------------
		// File Hummingbird20
		int initialResourcesInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		assertEquals(initialResourcesInEditingDomain20, refWks.editingDomain20.getResourceSet().getResources().size());

		IFile hbFile20_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		IFile hbFile20_2 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		IFile hbFile20_3 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);
		IFile hbFile20_4 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile hbFile20_5 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile hbFile20_6 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);

		assertNotNull(hbFile20_1);
		assertNotNull(hbFile20_2);
		assertNotNull(hbFile20_3);
		assertNotNull(hbFile20_4);
		assertNotNull(hbFile20_5);
		assertNotNull(hbFile20_6);
		assertTrue(hbFile20_1.exists());
		assertTrue(hbFile20_2.exists());
		assertTrue(hbFile20_3.exists());
		assertTrue(hbFile20_4.exists());
		assertTrue(hbFile20_5.exists());
		assertTrue(hbFile20_6.exists());

		Resource hbResource20_1 = null;
		Resource hbResource20_2 = null;
		Resource hbResource20_3 = null;
		Resource hbResource20_4 = null;
		Resource hbResource20_5 = null;
		Resource hbResource20_6 = null;
		for (Resource res : refWks.editingDomain20.getResourceSet().getResources()) {
			if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1.equals(res.getURI().lastSegment())) {
				hbResource20_1 = res;
			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2.equals(res.getURI().lastSegment())) {
				hbResource20_2 = res;
			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3.equals(res.getURI().lastSegment())) {
				hbResource20_3 = res;
			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1.equals(res.getURI().lastSegment())) {
				hbResource20_4 = res;
			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2.equals(res.getURI().lastSegment())) {
				hbResource20_5 = res;
			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3.equals(res.getURI().lastSegment())) {
				hbResource20_6 = res;
			}
		}
		assertNotNull(hbResource20_1);
		assertNotNull(hbResource20_2);
		assertNotNull(hbResource20_3);
		assertNotNull(hbResource20_4);
		assertNotNull(hbResource20_5);
		assertNotNull(hbResource20_6);

		assertEquals(hbResource20_1, EcorePlatformUtil.getResource(hbFile20_1));
		assertEquals(hbResource20_2, EcorePlatformUtil.getResource(hbFile20_2));
		assertEquals(hbResource20_3, EcorePlatformUtil.getResource(hbFile20_3));
		assertEquals(hbResource20_4, EcorePlatformUtil.getResource(hbFile20_4));
		assertEquals(hbResource20_5, EcorePlatformUtil.getResource(hbFile20_5));
		assertEquals(hbResource20_6, EcorePlatformUtil.getResource(hbFile20_6));
		// --------------------------------------------------------------------
		// Uml2 File
		int initialResourcesIneditingDomainUml2 = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, initialResourcesIneditingDomainUml2);

		IFile uml2File_1 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		IFile uml2File_2 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		IFile uml2File_3 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertNotNull(uml2File_1);
		assertNotNull(uml2File_2);
		assertNotNull(uml2File_3);
		assertTrue(uml2File_1.exists());
		assertTrue(uml2File_2.exists());
		assertTrue(uml2File_3.exists());

		Resource uml2Resource_1 = null;
		Resource uml2Resource_2 = null;
		Resource uml2Resource_3 = null;
		for (Resource res : refWks.editingDomainUml2.getResourceSet().getResources()) {
			if (DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1.equals(res.getURI().lastSegment())) {
				uml2Resource_1 = res;
			} else if (DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2.equals(res.getURI().lastSegment())) {
				uml2Resource_2 = res;
			} else if (DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3.equals(res.getURI().lastSegment())) {
				uml2Resource_3 = res;
			}
		}
		assertNotNull(uml2Resource_1);
		assertNotNull(uml2Resource_2);
		assertNotNull(uml2Resource_3);

		assertEquals(uml2Resource_1, EcorePlatformUtil.getResource(uml2File_1));
		assertEquals(uml2Resource_2, EcorePlatformUtil.getResource(uml2File_2));
		assertEquals(uml2Resource_3, EcorePlatformUtil.getResource(uml2File_3));
		// --------------------------------------------------------------------
		// Given File is unloaded
		ModelLoadManager.INSTANCE.unloadFile(hbFile10_1, true, new NullProgressMonitor());
		waitForModelLoading();
		assertNull(EcorePlatformUtil.getResource(hbFile10_1));
		ModelLoadManager.INSTANCE.unloadFile(hbFile20_1, true, new NullProgressMonitor());
		waitForModelLoading();
		assertNull(EcorePlatformUtil.getResource(hbFile20_1));
		ModelLoadManager.INSTANCE.unloadFile(uml2File_1, true, new NullProgressMonitor());
		waitForModelLoading();
		assertNull(EcorePlatformUtil.getResource(uml2File_1));
		// --------------------------------------------------------------------
		// Given File is NULL
		IFile nullFile = null;
		assertNull(EcorePlatformUtil.getResource(nullFile));
	}

	/**
	 * Test method for {@link EcorePlatformUtil#getResource(org.eclipse.emf.ecore.util.FeatureMap.Entry)}
	 */
	public void testGetResourceFromFeatureMapEntry() throws Exception {
		Resource hbResource20A_1 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);

		assertNotNull(hbResource20A_1);
		assertFalse(hbResource20A_1.getContents().isEmpty());
		assertTrue(hbResource20A_1.getContents().get(0) instanceof org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application);
		org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application modelRoot20A_1 = (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application) hbResource20A_1
				.getContents().get(0);
		assertNotNull(modelRoot20A_1);

		assertFalse(modelRoot20A_1.getComponents().isEmpty());
		org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component component = modelRoot20A_1.getComponents().get(0);
		assertNotNull(component);

		assertFalse(component.getParameterExpressions().isEmpty());
		ParameterExpression parameterExpression = component.getParameterExpressions().get(0);
		assertNotNull(parameterExpression);
		assertFalse(parameterExpression.getMixed().isEmpty());
		Entry testEntry = parameterExpression.getMixed().get(0);

		assertEquals(InstanceModel20Package.Literals.PARAMETER_EXPRESSION__EXPRESSIONS, testEntry.getEStructuralFeature());

		Resource resource = EcorePlatformUtil.getResource(testEntry);
		assertEquals(hbResource20A_1, resource);

		// --------------------------------------------------------------------
		// Input is NULL
		Entry nullEntry = null;
		assertNull(EcorePlatformUtil.getResource(nullEntry));
	}

	/**
	 * Test method for {@link EcorePlatformUtil#getResource(org.eclipse.emf.edit.provider.IWrapperItemProvider)}
	 */
	public void testGetResourceFromIWrapperItemProvider() throws Exception {
		// IFile hbFile20_20A_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		// assertNotNull(hbFile20_20A_1);
		// assertTrue(hbFile20_20A_1.isAccessible());
		//
		// Resource hbResource20_A_1 = refWks.editingDomain20.getResourceSet().getResource(
		// URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
		// + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		// assertNotNull(hbResource20_A_1);
		//
		// WrapperItemProvider wrapperItemProvider = new WrapperItemProvider(element, modelRoot,
		// Package.eINSTANCE
		// .get, 1, ((AdapterFactoryEditingDomain)
		// WorkspaceEditingDomainUtil.getEditingDomain(hbFile20_20A_1))
		// .getAdapterFactory());
		//
		// Resource resource = EcorePlatformUtil.getResource(wrapperItemProvider);
		// assertNotNull(resource);
		// assertEquals(hbResource20_A_1, resource);
		//
		// // --------------------------------------------------------------------
		// // Input is NULL
		// WrapperItemProvider nullWrapperItemProvider = null;
		// assertNull(EcorePlatformUtil.getResource(nullWrapperItemProvider));
		// // --------------------------------------------------------------------
		// // WrapperItemProvider of object in unloaded resource
		// ModelLoadManager.INSTANCE.unloadFile(hbFile20_20A_1, true, new NullProgressMonitor());
		// waitForModelLoading();
		// assertNull(EcorePlatformUtil.getResource(wrapperItemProvider));

	}

	/**
	 * Test method for
	 * {@link EcorePlatformUtil#saveNewModelResource(org.eclipse.emf.transaction.TransactionalEditingDomain, IPath, String, EObject, boolean, org.eclipse.core.runtime.IProgressMonitor)}
	 *
	 * @throws InterruptedException
	 */
	public void testSaveNewModelResource_WithoutSaveOptions() throws Exception {
		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		int resourcesInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		int resourcesIneditingDomainUml2 = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);

		String newResourceName10 = "NewModel10.hummingbird";
		String newResourceName10_ = "NewModel10_.hummingbird";
		String newResourceName20 = "NewModel20.instancemodel";
		String newUml2ResourceName = "NewUml2.uml";
		String newUml2ResourceName_ = "NewUml2_.uml";
		String testResourcename10 = "test10.hummingbird";
		String testResourcename20 = "test20.instancemodel";
		IPath newResourcePath10_1 = refWks.hbProject10_A.getFullPath().append(newResourceName10);
		IPath newResourcePath10_2 = refWks.hbProject10_A.getFullPath().append(newResourceName10_);
		IPath newResourcePath10_3 = refWks.hbProject10_A.getFullPath().append(testResourcename10);
		IPath newResourcePath20_1 = refWks.hbProject10_A.getFullPath().append(newResourceName20);
		IPath newResourcePath20_2 = refWks.hbProject20_A.getFullPath().append(newResourceName20);
		IPath newResourcePath20_3 = refWks.hbProject20_A.getFullPath().append(testResourcename20);
		IPath newUml2ResourcePath_1 = refWks.hbProject10_A.getFullPath().append(newUml2ResourceName);
		IPath newUml2ResourcePath_2 = refWks.hbProject20_A.getFullPath().append(newUml2ResourceName);
		IPath newUml2ResourcePath_3 = refWks.hbProject20_A.getFullPath().append(newUml2ResourceName_);

		// Save resource
		{
			EcorePlatformUtil.saveNewModelResource(refWks.editingDomain10, newResourcePath10_1, Hummingbird10Package.eCONTENT_TYPE,
					createApplicationHB10(), true, new NullProgressMonitor());
			waitForModelSaving();
			resourcesInEditingDomain10++;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);

			// Reload project
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
			waitForModelLoading();

			// Verify the saved resource
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);

			URI newResourceURI = EcorePlatformUtil.createURI(newResourcePath10_1);
			Resource savedResource = refWks.editingDomain10.getResourceSet().getResource(newResourceURI, false);
			assertNotNull(savedResource);
			assertEquals(1, savedResource.getContents().size());
			assertTrue(savedResource.getContents().get(0) instanceof Application);
			Application retrievedSavedApp = (Application) savedResource.getContents().get(0);
			assertEquals(1, retrievedSavedApp.getComponents().size());
			assertEquals(1, retrievedSavedApp.getInterfaces().size());
		}
		// *************************************************************************
		// Context: use ContentTypeId of Hummingbird20 to save hbResource10 to EditingDomain10
		{
			EcorePlatformUtil.saveNewModelResource(refWks.editingDomain10, newResourcePath10_2,
					Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), createApplicationHB10(), true, new NullProgressMonitor());
			waitForModelSaving();
			resourcesInEditingDomain10++;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);

			// Reload project
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
			waitForModelLoading();

			// Verify the saved resource
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);

			URI newResourceURI1 = EcorePlatformUtil.createURI(newResourcePath10_2);
			Resource savedResource1 = refWks.editingDomain10.getResourceSet().getResource(newResourceURI1, false);
			assertNotNull(savedResource1);
			assertEquals(1, savedResource1.getContents().size());
			assertTrue(savedResource1.getContents().get(0) instanceof Application);
			Application retrievedSavedApp1 = (Application) savedResource1.getContents().get(0);
			assertEquals(1, retrievedSavedApp1.getComponents().size());
			assertEquals(1, retrievedSavedApp1.getInterfaces().size());
		}
		// *************************************************************************
		// Context: Overwrite existing resource by Resource10 saved with ContentTypeId of Hummingbird20 in
		// EditingDomain10
		{
			EcorePlatformUtil.saveNewModelResource(refWks.editingDomain10, newResourcePath10_1,
					Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), createApplicationHB10(), true, new NullProgressMonitor());
			waitForModelSaving();
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
			// Reload project
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
			waitForModelLoading();
			// Verify the saved resource
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);

			URI newResourceURI = EcorePlatformUtil.createURI(newResourcePath10_1);
			Resource savedResource = refWks.editingDomain10.getResourceSet().getResource(newResourceURI, false);
			assertNotNull(savedResource);
			assertEquals(1, savedResource.getContents().size());
			assertTrue(savedResource.getContents().get(0) instanceof Application);

			Application retrievedSavedApp2 = (Application) savedResource.getContents().get(0);
			assertEquals(1, retrievedSavedApp2.getComponents().size());
			assertEquals(1, retrievedSavedApp2.getInterfaces().size());
		}
		// *************************************************************************
		// Context: save HB20 Resource to EditingDomain10 in hbProject10
		{
			// Save resource
			EcorePlatformUtil.saveNewModelResource(refWks.editingDomain10, newResourcePath20_1,
					Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), createPlatform(), true, new NullProgressMonitor());
			waitForModelSaving();
			resourcesInEditingDomain10++;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
			// Reload project
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject20_A, false, true, new NullProgressMonitor());
			waitForModelLoading();

			// Verify the saved resource
			resourcesInEditingDomain10--;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
			//
			URI newResourceURI = EcorePlatformUtil.createURI(newResourcePath20_1);
			Resource savedResource = refWks.editingDomain10.getResourceSet().getResource(newResourceURI, false);
			assertNull(savedResource);
			savedResource = refWks.editingDomain20.getResourceSet().getResource(newResourceURI, false);
			assertNull(savedResource);
			savedResource = refWks.editingDomain20.getResourceSet().getResource(newResourceURI, true);
			assertNotNull(savedResource);
			resourcesInEditingDomain20++;
			//
			assertEquals(1, savedResource.getContents().size());
			assertTrue(savedResource.getContents().get(0) instanceof Platform);
			Platform retrievedSavedPlatform1 = (Platform) savedResource.getContents().get(0);
			assertEquals(1, retrievedSavedPlatform1.getComponentTypes().size());
			assertEquals(1, retrievedSavedPlatform1.getInterfaces().size());
		}

		// *************************************************************************
		// Context: save HB20 Resource to EditingDomain10 and in hbProject20
		{
			EcorePlatformUtil.saveNewModelResource(refWks.editingDomain10, newResourcePath20_2,
					Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), createPlatform(), true, new NullProgressMonitor());
			waitForModelSaving();
			resourcesInEditingDomain10++;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);

			// Reload project
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject20_A, false, true, new NullProgressMonitor());
			waitForModelLoading();

			// Verify the saved resource
			resourcesInEditingDomain10--;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
			resourcesInEditingDomain20++;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);

			URI newResourceURI = EcorePlatformUtil.createURI(newResourcePath20_2);
			Resource savedResource = refWks.editingDomain20.getResourceSet().getResource(newResourceURI, false);
			assertNotNull(savedResource);

			assertEquals(1, savedResource.getContents().size());
			assertTrue(savedResource.getContents().get(0) instanceof Platform);
			Platform retrievedSavedPlatform1 = (Platform) savedResource.getContents().get(0);
			assertEquals(1, retrievedSavedPlatform1.getComponentTypes().size());
			assertEquals(1, retrievedSavedPlatform1.getInterfaces().size());
		}
		// -----------------------------------------------------------------------
		// Context: Save uml2 resource in EditingDomain10

		// Save new model resource
		Model newModel = UMLFactory.eINSTANCE.createModel();
		newModel.setName("UML2Model");

		Package newPack1 = UMLFactory.eINSTANCE.createPackage();
		newPack1.setName("Package1");
		newModel.getPackagedElements().add(newPack1);

		Package newPack2 = UMLFactory.eINSTANCE.createPackage();
		newPack2.setName("Package2");
		newModel.getPackagedElements().add(newPack2);

		{
			// Save uml2Resource in EditingDomain21
			EcorePlatformUtil.saveNewModelResource(refWks.editingDomain10, newUml2ResourcePath_1, UMLPackage.eCONTENT_TYPE, newModel, true,
					new NullProgressMonitor());
			waitForModelSaving();
			resourcesInEditingDomain10++;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesIneditingDomainUml2);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);

			// Reload project
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
			waitForModelLoading();

			// Verify the saved resource
			resourcesInEditingDomain10--;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
			resourcesIneditingDomainUml2++;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesIneditingDomainUml2);
			// Resource HB20 in hbProject10_A is unloaded
			resourcesInEditingDomain20--;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);

			// Verified saved resource
			Resource uml2Resource = refWks.editingDomainUml2.getResourceSet()
					.getResource(URI.createPlatformResourceURI(newUml2ResourcePath_1.toString(), true), true);
			assertNotNull(uml2Resource);
			assertNotNull(uml2Resource.getContents());
			assertEquals(1, uml2Resource.getContents().size());
			Model savedModel = (Model) uml2Resource.getContents().get(0);
			assertNotNull(savedModel);

			assertEquals(2, savedModel.getPackagedElements().size());
			assertEquals("Package1", savedModel.getPackagedElements().get(0).getName());
			assertEquals("Package2", savedModel.getPackagedElements().get(1).getName());
		}
		// *************************************************************************
		// Context: Save uml2 resource in editingDomainUml
		{
			// FIXME create a new model resource
			Model newModel_2 = UMLFactory.eINSTANCE.createModel();
			newModel_2.setName("UML2Model");

			Package newPack1_2 = UMLFactory.eINSTANCE.createPackage();
			newPack1_2.setName("Package1");
			newModel_2.getPackagedElements().add(newPack1_2);

			Package newPack2_2 = UMLFactory.eINSTANCE.createPackage();
			newPack2_2.setName("Package2");
			newModel_2.getPackagedElements().add(newPack2_2);

			EcorePlatformUtil.saveNewModelResource(refWks.editingDomainUml2, newUml2ResourcePath_2, UMLPackage.eCONTENT_TYPE, newModel_2, true,
					new NullProgressMonitor());
			waitForModelSaving();
			resourcesIneditingDomainUml2++;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesIneditingDomainUml2);

			// Reload project
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject20_A, false, true, new NullProgressMonitor());
			waitForModelLoading();

			// Verify the saved resource
			assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesIneditingDomainUml2);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);

			// Verify saved resources
			Resource uml2Resource = refWks.editingDomainUml2.getResourceSet()
					.getResource(URI.createPlatformResourceURI(newUml2ResourcePath_2.toString(), true), false);
			assertNotNull(uml2Resource);

			// Verify second resource
			assertNotNull(uml2Resource.getContents());
			assertEquals(1, uml2Resource.getContents().size());
			Model savedModel = (Model) uml2Resource.getContents().get(0);
			assertNotNull(savedModel);
			assertEquals(2, savedModel.getPackagedElements().size());
			assertEquals("Package1", savedModel.getPackagedElements().get(0).getName());
			assertEquals("Package2", savedModel.getPackagedElements().get(1).getName());
		}
		// *************************************************************************
		// Context:ad hoc case
		{
			// Given EditingDomain is NULL
			TransactionalEditingDomain nullEditingDomain = null;
			EcorePlatformUtil.saveNewModelResource(nullEditingDomain, newResourcePath10_3, Hummingbird10Package.eCONTENT_TYPE,
					createApplicationHB10(), true, new NullProgressMonitor());
			waitForModelSaving();
			EcorePlatformUtil.saveNewModelResource(nullEditingDomain, newResourcePath20_3,
					Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), createPlatform(), true, new NullProgressMonitor());
			waitForModelSaving();
			EcorePlatformUtil.saveNewModelResource(nullEditingDomain, newUml2ResourcePath_3, UMLPackage.eCONTENT_TYPE, newModel, true,
					new NullProgressMonitor());
			waitForModelSaving();

			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesIneditingDomainUml2);
		}
		{
			// Given Path is NULL
			IPath nullPath = null;
			try {
				EcorePlatformUtil.saveNewModelResource(refWks.editingDomain10, nullPath, Hummingbird10Package.eCONTENT_TYPE, createApplicationHB10(),
						true, new NullProgressMonitor());
				waitForModelSaving();
			} catch (Exception ex) {
				if (!(ex instanceof AssertionFailedException)) {
					fail("Exception when the given Path is Null: " + ex.getMessage());
				}
			}
		}
		{
			// Given contentTypeID is NULL or incorrect
			EcorePlatformUtil.saveNewModelResource(refWks.editingDomain10, newResourcePath10_3, null, createApplicationHB10(), true,
					new NullProgressMonitor());
			EcorePlatformUtil.saveNewModelResource(refWks.editingDomain20, newResourcePath20_3, null, createPlatform(), true,
					new NullProgressMonitor());
			EcorePlatformUtil.saveNewModelResource(refWks.editingDomainUml2, newUml2ResourcePath_3, null, newModel, true, new NullProgressMonitor());
			waitForModelSaving();
			resourcesInEditingDomain10++;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
			resourcesInEditingDomain20++;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
			resourcesIneditingDomainUml2++;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesIneditingDomainUml2);
		}
		{
			// Given RootObject is NULL
			EObject rootObject = null;
			try {
				EcorePlatformUtil.saveNewModelResource(refWks.editingDomain10, newResourcePath10_3, Hummingbird10Package.eCONTENT_TYPE, rootObject,
						true, new NullProgressMonitor());
				waitForModelSaving();
			} catch (Exception ex) {
				if (!(ex instanceof AssertionFailedException)) {
					fail("Exception when the given Path is Null: " + ex.getMessage());
				}
			}
		}
	}

	/**
	 * Test method for
	 * {@link EcorePlatformUtil#saveNewModelResource(org.eclipse.emf.transaction.TransactionalEditingDomain, IPath, String, EObject, java.util.Map, boolean, org.eclipse.core.runtime.IProgressMonitor)}
	 *
	 * @see {@link this#testSaveNewModelResource_WithoutSaveOptions()}
	 * @throws InterruptedException
	 */
	public void testSaveNewModelResource_WithSaveOptions() throws Exception {
		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);

		String newResourceName = "NewModel.hummingbird";
		IPath newResourcePath = refWks.hbProject10_A.getFullPath().append(newResourceName);
		// Save options
		Map<Object, Object> options = new HashMap<Object, Object>();
		options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
		options.put(XMLResource.OPTION_SAVE_DOCTYPE, Boolean.TRUE);

		// Save new model resource
		EcorePlatformUtil.saveNewModelResource(refWks.editingDomain10, newResourcePath, Hummingbird10Package.eCONTENT_TYPE, createApplicationHB10(),
				options, true, new NullProgressMonitor());
		waitForModelSaving();

		// Reload project
		ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
		waitForModelLoading();

		// Verify the saved resource
		assertEquals(resourcesInEditingDomain10 + 1, refWks.editingDomain10.getResourceSet().getResources().size());
		URI savedResourceURI = EcorePlatformUtil.createURI(newResourcePath);
		Resource savedResource = refWks.editingDomain10.getResourceSet().getResource(savedResourceURI, false);
		assertNotNull(savedResource);
		assertEquals(1, savedResource.getContents().size());
		assertTrue(savedResource.getContents().get(0) instanceof Application);
		Application retrievedSavedApp = (Application) savedResource.getContents().get(0);
		assertEquals(1, retrievedSavedApp.getComponents().size());
		assertEquals(1, retrievedSavedApp.getInterfaces().size());
	}

	/**
	 * Test method for
	 * {@link EcorePlatformUtil#saveNewModelResources(org.eclipse.emf.transaction.TransactionalEditingDomain, java.util.Collection, java.util.Map, boolean, org.eclipse.core.runtime.IProgressMonitor)}
	 *
	 * @see {@link this#testSaveNewModelResource_WithoutSaveOptions()}
	 *      {@link this#testSaveNewModelResource_WithoutSaveOptions()}
	 * @throws InterruptedException
	 */

	public void testSaveNewModelResources_WithSaveOptions() throws Exception {
		assertNotNull(refWks.hbProject10_A);
		assertNotNull(refWks.hbProject20_A);
		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		int resourcesInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);

		String newResourceName1 = "NewModel1.hummingbird";
		String newResourceName2 = "NewModel2.hummingbird";
		String newResourceName3 = "NewModel3.hummingbird";
		String newResourceName4 = "NewModel4.hummingbird";
		String newResourceName5 = "NewModel5.hummingbird";
		IPath newResourcePath10_1 = refWks.hbProject10_A.getFullPath().append(newResourceName1);
		IPath newResourcePath10_2 = refWks.hbProject10_A.getFullPath().append(newResourceName2);
		IPath newResourcePath10_3 = refWks.hbProject10_A.getFullPath().append(newResourceName3);
		IPath newResourcePath10_4 = refWks.hbProject10_A.getFullPath().append(newResourceName4);
		IPath newResourcePath10_5 = refWks.hbProject10_A.getFullPath().append(newResourceName5);
		IPath newResourcePath20_1 = refWks.hbProject20_A.getFullPath().append(newResourceName3);

		Collection<ModelResourceDescriptor> modelResourceDescriptors10 = new ArrayList<ModelResourceDescriptor>();
		Collection<ModelResourceDescriptor> modelResourceDescriptors_mix = new ArrayList<ModelResourceDescriptor>();
		Map<Object, Object> options = new HashMap<Object, Object>();
		options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
		options.put(XMLResource.OPTION_SAVE_DOCTYPE, Boolean.TRUE);

		ModelResourceDescriptor modelDesc1 = new ModelResourceDescriptor(newResourcePath10_1, Hummingbird10Package.eCONTENT_TYPE,
				createApplicationHB10());
		ModelResourceDescriptor modelDesc2 = new ModelResourceDescriptor(newResourcePath10_2, Hummingbird10Package.eCONTENT_TYPE,
				createApplicationHB10());
		ModelResourceDescriptor modelDesc3 = new ModelResourceDescriptor(newResourcePath10_3, Hummingbird10Package.eCONTENT_TYPE,
				createApplicationHB10());
		ModelResourceDescriptor modelDesc4 = new ModelResourceDescriptor(newResourcePath10_4, Hummingbird10Package.eCONTENT_TYPE,
				createApplicationHB10());
		ModelResourceDescriptor modelDesc5 = new ModelResourceDescriptor(newResourcePath10_5, Hummingbird10Package.eCONTENT_TYPE,
				createApplicationHB10());
		ModelResourceDescriptor modelDesc6 = new ModelResourceDescriptor(newResourcePath20_1,
				Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), createPlatform());

		{
			// Save resources with its corresponding editingDomain
			modelResourceDescriptors10.add(modelDesc1);
			modelResourceDescriptors10.add(modelDesc2);

			EcorePlatformUtil.saveNewModelResources(refWks.editingDomain10, modelResourceDescriptors10, options, true, new NullProgressMonitor());
			waitForModelSaving();
			resourcesInEditingDomain10 += 2;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);

			// Reload project
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
			waitForModelLoading();

			// Verify the saved resource
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);

			Resource savedResource1 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(newResourcePath10_1.toString(), true), false);
			Resource savedResource2 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(newResourcePath10_2.toString(), true), false);
			assertNotNull(savedResource1);
			assertNotNull(savedResource2);

			assertEquals(1, savedResource1.getContents().size());
			assertTrue(savedResource1.getContents().get(0) instanceof Application);
			Application retrievedSavedApp1 = (Application) savedResource1.getContents().get(0);
			assertEquals(1, retrievedSavedApp1.getComponents().size());
			assertEquals(1, retrievedSavedApp1.getInterfaces().size());

			assertEquals(1, savedResource2.getContents().size());
			assertTrue(savedResource2.getContents().get(0) instanceof Application);
			Application retrievedSavedApp2 = (Application) savedResource2.getContents().get(0);
			assertEquals(1, retrievedSavedApp2.getComponents().size());
			assertEquals(1, retrievedSavedApp2.getInterfaces().size());

		}
		// *********************************************************************
		{
			// Save resource with inappropriate EditingDomain
			modelResourceDescriptors10.clear();
			modelResourceDescriptors10.add(modelDesc3);
			modelResourceDescriptors10.add(modelDesc4);

			EcorePlatformUtil.saveNewModelResources(refWks.editingDomain20, modelResourceDescriptors10, options, true, new NullProgressMonitor());
			waitForModelSaving();
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20 + 2);

			// Reload project
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
			waitForModelLoading();

			// Verify the saved resource
			resourcesInEditingDomain10 += 2;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);

			Resource savedResource1 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(newResourcePath10_3.toString(), true), false);
			Resource savedResource2 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(newResourcePath10_4.toString(), true), false);
			assertNotNull(savedResource1);
			assertNotNull(savedResource2);

			assertEquals(1, savedResource1.getContents().size());
			assertTrue(savedResource1.getContents().get(0) instanceof Application);
			Application retrievedSavedApp1 = (Application) savedResource1.getContents().get(0);
			assertEquals(1, retrievedSavedApp1.getComponents().size());
			assertEquals(1, retrievedSavedApp1.getInterfaces().size());

			assertEquals(1, savedResource2.getContents().size());
			assertTrue(savedResource2.getContents().get(0) instanceof Application);
			Application retrievedSavedApp2 = (Application) savedResource1.getContents().get(0);
			assertEquals(1, retrievedSavedApp2.getComponents().size());
			assertEquals(1, retrievedSavedApp2.getInterfaces().size());

		}
		// *******************************************************************************

		{
			// Context save ModelDescriptor Hb10 and HB20 at once
			modelResourceDescriptors_mix.add(modelDesc5);
			modelResourceDescriptors_mix.add(modelDesc6);

			EcorePlatformUtil.saveNewModelResources(refWks.editingDomain10, modelResourceDescriptors_mix, options, true, new NullProgressMonitor());
			waitForModelSaving();
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10 + 2);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);

			// Reload project
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject20_A, false, true, new NullProgressMonitor());

			waitForModelLoading();

			// Verify the saved resource
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10 + 1);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20 + 1);

			Resource savedResource1 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(newResourcePath10_5.toString(), true), false);
			Resource savedResource2 = refWks.editingDomain20.getResourceSet()
					.getResource(URI.createPlatformResourceURI(newResourcePath20_1.toString(), true), false);
			assertNotNull(savedResource1);
			assertNotNull(savedResource2);

			assertEquals(1, savedResource1.getContents().size());
			assertTrue(savedResource1.getContents().get(0) instanceof Application);
			Application retrievedSavedApp = (Application) savedResource1.getContents().get(0);
			assertEquals(1, retrievedSavedApp.getComponents().size());
			assertEquals(1, retrievedSavedApp.getInterfaces().size());

			assertTrue(savedResource2.getContents().get(0) instanceof Platform);
			Platform retrievedSavedPlatform = (Platform) savedResource2.getContents().get(0);

			assertEquals(1, retrievedSavedPlatform.getComponentTypes().size());
			assertEquals(1, retrievedSavedPlatform.getInterfaces().size());

		}

	}

	/**
	 * Test method for
	 * {@link EcorePlatformUtil#saveNewModelResources(org.eclipse.emf.transaction.TransactionalEditingDomain, java.util.Collection, boolean, org.eclipse.core.runtime.IProgressMonitor)}
	 *
	 * @see {@link this#testSaveNewModelResource_WithoutSaveOptions()}
	 * @throws InterruptedException
	 */
	public void testSaveNewModelResources_WithoutSaveOptions() throws Exception {
		assertNotNull(refWks.hbProject10_A);
		assertNotNull(refWks.hbProject20_A);
		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		int resourcesInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);

		String newResourceName1 = "NewModel1.hummingbird";
		String newResourceName2 = "NewModel2.hummingbird";
		String newResourceName3 = "NewModel3.hummingbird";
		String newResourceName4 = "NewModel4.hummingbird";
		String newResourceName5 = "NewModel5.hummingbird";
		IPath newResourcePath10_1 = refWks.hbProject10_A.getFullPath().append(newResourceName1);
		IPath newResourcePath10_2 = refWks.hbProject10_A.getFullPath().append(newResourceName2);
		IPath newResourcePath10_3 = refWks.hbProject10_A.getFullPath().append(newResourceName3);
		IPath newResourcePath10_4 = refWks.hbProject10_A.getFullPath().append(newResourceName4);
		IPath newResourcePath10_5 = refWks.hbProject10_A.getFullPath().append(newResourceName5);
		IPath newResourcePath20_1 = refWks.hbProject20_A.getFullPath().append(newResourceName3);

		Collection<ModelResourceDescriptor> modelResourceDescriptors10 = new ArrayList<ModelResourceDescriptor>();
		Collection<ModelResourceDescriptor> modelResourceDescriptors_mix = new ArrayList<ModelResourceDescriptor>();

		ModelResourceDescriptor modelDesc1 = new ModelResourceDescriptor(newResourcePath10_1, Hummingbird10Package.eCONTENT_TYPE,
				createApplicationHB10());
		ModelResourceDescriptor modelDesc2 = new ModelResourceDescriptor(newResourcePath10_2, Hummingbird10Package.eCONTENT_TYPE,
				createApplicationHB10());
		ModelResourceDescriptor modelDesc3 = new ModelResourceDescriptor(newResourcePath10_3, Hummingbird10Package.eCONTENT_TYPE,
				createApplicationHB10());
		ModelResourceDescriptor modelDesc4 = new ModelResourceDescriptor(newResourcePath10_4, Hummingbird10Package.eCONTENT_TYPE,
				createApplicationHB10());
		ModelResourceDescriptor modelDesc5 = new ModelResourceDescriptor(newResourcePath10_5, Hummingbird10Package.eCONTENT_TYPE,
				createApplicationHB10());
		ModelResourceDescriptor modelDesc6 = new ModelResourceDescriptor(newResourcePath20_1,
				Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), createPlatform());

		{
			// Save resources with its corresponding editingDomain
			modelResourceDescriptors10.add(modelDesc1);
			modelResourceDescriptors10.add(modelDesc2);

			EcorePlatformUtil.saveNewModelResources(refWks.editingDomain10, modelResourceDescriptors10, true, new NullProgressMonitor());
			waitForModelSaving();
			resourcesInEditingDomain10 += 2;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);

			// Reload project
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
			waitForModelLoading();

			// Verify the saved resource
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);

			Resource savedResource1 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(newResourcePath10_1.toString(), true), false);
			Resource savedResource2 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(newResourcePath10_2.toString(), true), false);
			assertNotNull(savedResource1);
			assertNotNull(savedResource2);

			assertEquals(1, savedResource1.getContents().size());
			assertTrue(savedResource1.getContents().get(0) instanceof Application);
			Application retrievedSavedApp1 = (Application) savedResource1.getContents().get(0);
			assertEquals(1, retrievedSavedApp1.getComponents().size());
			assertEquals(1, retrievedSavedApp1.getInterfaces().size());

			assertEquals(1, savedResource2.getContents().size());
			assertTrue(savedResource2.getContents().get(0) instanceof Application);
			Application retrievedSavedApp2 = (Application) savedResource2.getContents().get(0);
			assertEquals(1, retrievedSavedApp2.getComponents().size());
			assertEquals(1, retrievedSavedApp2.getInterfaces().size());

		}
		// ====================================================================
		{
			// Save resource with inappropriate EditingDomain
			modelResourceDescriptors10.clear();
			modelResourceDescriptors10.add(modelDesc3);
			modelResourceDescriptors10.add(modelDesc4);

			EcorePlatformUtil.saveNewModelResources(refWks.editingDomain20, modelResourceDescriptors10, true, new NullProgressMonitor());
			waitForModelSaving();
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20 + 2);

			// Reload project
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
			waitForModelLoading();

			// Verify the saved resource
			resourcesInEditingDomain10 += 2;
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);

			Resource savedResource1 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(newResourcePath10_3.toString(), true), false);
			Resource savedResource2 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(newResourcePath10_4.toString(), true), false);
			assertNotNull(savedResource1);
			assertNotNull(savedResource2);

			assertEquals(1, savedResource1.getContents().size());
			assertTrue(savedResource1.getContents().get(0) instanceof Application);
			Application retrievedSavedApp1 = (Application) savedResource1.getContents().get(0);
			assertEquals(1, retrievedSavedApp1.getComponents().size());
			assertEquals(1, retrievedSavedApp1.getInterfaces().size());

			assertEquals(1, savedResource2.getContents().size());
			assertTrue(savedResource2.getContents().get(0) instanceof Application);
			Application retrievedSavedApp2 = (Application) savedResource2.getContents().get(0);
			assertEquals(1, retrievedSavedApp2.getComponents().size());
			assertEquals(1, retrievedSavedApp2.getInterfaces().size());
		}
		// ====================================================================
		{
			// Context save ModelDescriptor HB10 and Hb20 at once
			modelResourceDescriptors_mix.add(modelDesc5);
			modelResourceDescriptors_mix.add(modelDesc6);

			EcorePlatformUtil.saveNewModelResources(refWks.editingDomain10, modelResourceDescriptors_mix, true, new NullProgressMonitor());
			waitForModelSaving();
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10 + 2);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);

			// Reload project
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject20_A, false, true, new NullProgressMonitor());

			waitForModelLoading();

			// Verify the saved resource
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10 + 1);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20 + 1);

			Resource savedResource1 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(newResourcePath10_5.toString(), true), false);
			Resource savedResource2 = refWks.editingDomain20.getResourceSet()
					.getResource(URI.createPlatformResourceURI(newResourcePath20_1.toString(), true), false);
			assertNotNull(savedResource1);
			assertNotNull(savedResource2);

			assertEquals(1, savedResource1.getContents().size());
			assertTrue(savedResource1.getContents().get(0) instanceof Application);
			Application retrievedSavedApp2 = (Application) savedResource1.getContents().get(0);
			assertEquals(1, retrievedSavedApp2.getComponents().size());
			assertEquals(1, retrievedSavedApp2.getInterfaces().size());

			assertEquals(1, savedResource2.getContents().size());
			assertTrue(savedResource2.getContents().get(0) instanceof Platform);
			Platform retrievedSavedPlatform = (Platform) savedResource2.getContents().get(0);
			assertEquals(1, retrievedSavedPlatform.getComponentTypes().size());
			assertEquals(1, retrievedSavedPlatform.getInterfaces().size());
		}
	}

	/**
	 * Test method for
	 * {@link EcorePlatformUtil#saveModel(Object, Map, boolean, org.eclipse.core.runtime.IProgressMonitor)} with
	 * contextObject is EObject
	 *
	 * @throws InterruptedException
	 */
	public void testSaveModel_Object_EObject_WithSaveOptions() throws Exception {
		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);

		Resource hbResource10_1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource hbResource10_2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		assertNotNull(hbResource10_1);
		assertNotNull(hbResource10_2);

		// First resource
		assertFalse(hbResource10_1.getContents().isEmpty());
		assertTrue(hbResource10_1.getContents().get(0) instanceof Application);
		final Application testApplicationHB10_1 = (Application) hbResource10_1.getContents().get(0);
		assertEquals(1, testApplicationHB10_1.getComponents().size());
		assertEquals(1, testApplicationHB10_1.getInterfaces().size());
		final Component testComponent1 = testApplicationHB10_1.getComponents().get(0);
		final Interface testInterface1 = testApplicationHB10_1.getInterfaces().get(0);

		assertFalse(hbResource10_2.getContents().isEmpty());
		assertTrue(hbResource10_2.getContents().get(0) instanceof Application);
		Application testApplicationHB10_2 = (Application) hbResource10_2.getContents().get(0);
		assertEquals(1, testApplicationHB10_2.getComponents().size());
		assertEquals(1, testApplicationHB10_2.getInterfaces().size());
		final Component testComponent2 = testApplicationHB10_2.getComponents().get(0);
		final Interface testInterface2 = testApplicationHB10_2.getInterfaces().get(0);

		final String newComponentName = "newComponent";
		final String newComponentName1 = "newComponentName1";
		final String newComponentName2 = "newComponentName2";

		final String newInterfaceName1 = "newInterface1";
		final String newInterfaceName2 = "newInterface2";

		int numberOfComponentInFirstResource = testApplicationHB10_1.getComponents().size();

		// Modify resource
		try {
			WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain10, new Runnable() {
				@Override
				public void run() {
					// Rename objects in first resource
					testComponent1.setName(newComponentName1);
					testInterface1.setName(newInterfaceName1);
					// Add one more element to first resource
					Component newComponent = Hummingbird10Factory.eINSTANCE.createComponent();
					newComponent.setName(newComponentName);
					testApplicationHB10_1.getComponents().add(newComponent);
					// Modify second resource
					testComponent2.setName(newComponentName2);
					testInterface2.setName(newInterfaceName2);

				}
			}, "Modify model");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}

		assertEquals(newComponentName1, testComponent1.getName());
		assertEquals(newComponentName2, testComponent2.getName());
		assertEquals(newInterfaceName1, testInterface1.getName());
		assertEquals(newInterfaceName2, testInterface2.getName());
		assertEquals(numberOfComponentInFirstResource + 1, testApplicationHB10_1.getComponents().size());

		// Save Model with SaveOptions
		Map<Object, Object> options = new HashMap<Object, Object>();
		options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
		options.put(XMLResource.OPTION_SAVE_DOCTYPE, Boolean.TRUE);

		EcorePlatformUtil.saveModel(testApplicationHB10_1, options, true, new NullProgressMonitor());
		waitForModelSaving();

		// Reload project
		ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
		waitForModelLoading();

		// Verify the saved resource
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		Resource savedResource1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource savedResource2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		assertNotNull(savedResource1);
		assertNotNull(savedResource2);

		// Verify the modification was saved in the first resource
		assertEquals(1, savedResource1.getContents().size());
		assertTrue(savedResource1.getContents().get(0) instanceof Application);
		Application retrievedSavedApp1 = (Application) savedResource1.getContents().get(0);
		assertEquals(2, retrievedSavedApp1.getComponents().size());
		assertEquals(1, retrievedSavedApp1.getInterfaces().size());
		assertEquals("First resource to save was not saved", newComponentName1, retrievedSavedApp1.getComponents().get(0).getName());
		assertEquals("First resource to save was not saved", newComponentName, retrievedSavedApp1.getComponents().get(1).getName());
		assertEquals("First resource to save was not saved", newInterfaceName1, retrievedSavedApp1.getInterfaces().get(0).getName());

		// Verify the modification was not saved in the second resource
		assertEquals(1, savedResource2.getContents().size());
		assertTrue(savedResource2.getContents().get(0) instanceof Application);
		Application retrievedSavedApp2 = (Application) savedResource2.getContents().get(0);
		assertEquals(1, retrievedSavedApp2.getComponents().size());
		assertEquals(1, retrievedSavedApp2.getInterfaces().size());
		assertEquals("Second resource in the same model was not saved", newComponentName2, retrievedSavedApp2.getComponents().get(0).getName());
		assertEquals("Second resource in the same model was not saved", newInterfaceName2, retrievedSavedApp2.getInterfaces().get(0).getName());
	}

	public void testSaveModel_Object_EObject_WithoutSaveOptions() throws Exception {

		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);

		Resource hbResource10_1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource hbResource10_2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		assertNotNull(hbResource10_1);
		assertNotNull(hbResource10_2);

		// First resource
		assertFalse(hbResource10_1.getContents().isEmpty());
		assertTrue(hbResource10_1.getContents().get(0) instanceof Application);
		final Application testApplicationHB10_1 = (Application) hbResource10_1.getContents().get(0);
		assertEquals(1, testApplicationHB10_1.getComponents().size());
		assertEquals(1, testApplicationHB10_1.getInterfaces().size());
		final Component testComponent1 = testApplicationHB10_1.getComponents().get(0);
		final Interface testInterface1 = testApplicationHB10_1.getInterfaces().get(0);

		assertFalse(hbResource10_2.getContents().isEmpty());
		assertTrue(hbResource10_2.getContents().get(0) instanceof Application);
		Application testApplicationHB10_2 = (Application) hbResource10_2.getContents().get(0);
		assertEquals(1, testApplicationHB10_2.getComponents().size());
		assertEquals(1, testApplicationHB10_2.getInterfaces().size());
		final Component testComponent2 = testApplicationHB10_2.getComponents().get(0);
		final Interface testInterface2 = testApplicationHB10_2.getInterfaces().get(0);

		final String newComponentName = "newComponent";
		final String newComponentName1 = "newComponentName1";
		final String newComponentName2 = "newComponentName2";

		final String newInterfaceName1 = "newInterface1";
		final String newInterfaceName2 = "newInterface2";

		int numberOfComponentInFirstResource = testApplicationHB10_1.getComponents().size();

		// Modify resource
		try {
			WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain10, new Runnable() {
				@Override
				public void run() {
					// Rename objects in first resource
					testComponent1.setName(newComponentName1);
					testInterface1.setName(newInterfaceName1);
					// Add one more element to first resource
					Component newComponent = Hummingbird10Factory.eINSTANCE.createComponent();
					newComponent.setName(newComponentName);
					testApplicationHB10_1.getComponents().add(newComponent);
					// Modify second resource
					testComponent2.setName(newComponentName2);
					testInterface2.setName(newInterfaceName2);

				}
			}, "Modify model");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}

		assertEquals(newComponentName1, testComponent1.getName());
		assertEquals(newComponentName2, testComponent2.getName());
		assertEquals(newInterfaceName1, testInterface1.getName());
		assertEquals(newInterfaceName2, testInterface2.getName());
		assertEquals(numberOfComponentInFirstResource + 1, testApplicationHB10_1.getComponents().size());

		EcorePlatformUtil.saveModel(testApplicationHB10_1, true, new NullProgressMonitor());
		waitForModelSaving();

		// Verify the saved resource
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		Resource savedResource1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource savedResource2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		assertNotNull(savedResource1);
		assertNotNull(savedResource2);

		// Verify the modification was saved in the first resource
		assertEquals(1, savedResource1.getContents().size());
		assertTrue(savedResource1.getContents().get(0) instanceof Application);
		Application retrievedSavedApp1 = (Application) savedResource1.getContents().get(0);
		assertEquals(2, retrievedSavedApp1.getComponents().size());
		assertEquals(1, retrievedSavedApp1.getInterfaces().size());
		assertEquals("First resource to save was not saved", newComponentName1, retrievedSavedApp1.getComponents().get(0).getName());
		assertEquals("First resource to save was not saved", newComponentName, retrievedSavedApp1.getComponents().get(1).getName());
		assertEquals("First resource to save was not saved", newInterfaceName1, retrievedSavedApp1.getInterfaces().get(0).getName());

		// Verify the modification was not saved in the second resource
		assertEquals(1, savedResource2.getContents().size());
		assertTrue(savedResource2.getContents().get(0) instanceof Application);
		Application retrievedSavedApp2 = (Application) savedResource2.getContents().get(0);
		assertEquals(1, retrievedSavedApp2.getComponents().size());
		assertEquals(1, retrievedSavedApp2.getInterfaces().size());
		assertEquals("Second resource in the same model was not saved", newComponentName2, retrievedSavedApp2.getComponents().get(0).getName());
		assertEquals("Second resource in the same model was not saved", newInterfaceName2, retrievedSavedApp2.getInterfaces().get(0).getName());
	}

	/**
	 * Test method for
	 * {@link EcorePlatformUtil#saveModel(Object, java.util.Map, boolean, org.eclipse.core.runtime.IProgressMonitor)}
	 * with contextObject is IProject
	 *
	 * @throws InterruptedException
	 * @throws OperationCanceledException
	 */
	public void testSaveModel_Object_IProject_WithSaveOptions() throws Exception {
		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);

		Resource hbResource10_1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource hbResource10_2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		assertNotNull(hbResource10_1);
		assertNotNull(hbResource10_2);

		// First resource
		assertFalse(hbResource10_1.getContents().isEmpty());
		assertTrue(hbResource10_1.getContents().get(0) instanceof Application);
		final Application testApplicationHB10_1 = (Application) hbResource10_1.getContents().get(0);
		assertEquals(1, testApplicationHB10_1.getComponents().size());
		assertEquals(1, testApplicationHB10_1.getInterfaces().size());
		final Component testComponent1 = testApplicationHB10_1.getComponents().get(0);
		final Interface testInterface1 = testApplicationHB10_1.getInterfaces().get(0);

		assertFalse(hbResource10_2.getContents().isEmpty());
		assertTrue(hbResource10_2.getContents().get(0) instanceof Application);
		Application testApplicationHB10_2 = (Application) hbResource10_2.getContents().get(0);
		assertEquals(1, testApplicationHB10_2.getComponents().size());
		assertEquals(1, testApplicationHB10_2.getInterfaces().size());
		final Component testComponent2 = testApplicationHB10_2.getComponents().get(0);
		final Interface testInterface2 = testApplicationHB10_2.getInterfaces().get(0);

		final String newComponentName1 = "newComponentName1";
		final String newComponentName2 = "newComponentName2";

		final String newInterfaceName1 = "newInterface1";
		final String newInterfaceName2 = "newInterface2";

		// Modify resource
		try {
			WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain10, new Runnable() {
				@Override
				public void run() {
					// Rename objects in first resource
					testComponent1.setName(newComponentName1);
					testInterface1.setName(newInterfaceName1);
					// Modify second resource
					testComponent2.setName(newComponentName2);
					testInterface2.setName(newInterfaceName2);

				}
			}, "Modify model");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}

		assertEquals(newComponentName1, testComponent1.getName());
		assertEquals(newComponentName2, testComponent2.getName());
		assertEquals(newInterfaceName1, testInterface1.getName());
		assertEquals(newInterfaceName2, testInterface2.getName());
		// Save project with SaveOptions

		Map<Object, Object> options = new HashMap<Object, Object>();
		options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
		options.put(XMLResource.OPTION_SAVE_DOCTYPE, Boolean.TRUE);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);

		EcorePlatformUtil.saveModel(refWks.hbProject10_A, options, true, new NullProgressMonitor());
		waitForModelSaving();

		// Reload project
		ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
		waitForModelLoading();

		// Verify the saved resource
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		Resource savedResource1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource savedResource2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		assertNotNull(savedResource1);
		assertNotNull(savedResource2);

		// Verify the modification was saved in the first resource
		assertEquals(1, savedResource1.getContents().size());
		assertTrue(savedResource1.getContents().get(0) instanceof Application);
		Application retrievedSavedApp1 = (Application) savedResource1.getContents().get(0);
		assertEquals(1, retrievedSavedApp1.getComponents().size());
		assertEquals(1, retrievedSavedApp1.getInterfaces().size());
		assertEquals("First resource was not saved", newComponentName1, retrievedSavedApp1.getComponents().get(0).getName());
		assertEquals("First resource was not saved", newInterfaceName1, retrievedSavedApp1.getInterfaces().get(0).getName());

		// Verify the modification was not saved in the second resource
		assertEquals(1, savedResource2.getContents().size());
		assertTrue(savedResource2.getContents().get(0) instanceof Application);
		Application retrievedSavedApp2 = (Application) savedResource2.getContents().get(0);
		assertEquals(1, retrievedSavedApp2.getComponents().size());
		assertEquals(1, retrievedSavedApp2.getInterfaces().size());
		assertEquals("Second resource was not saved", newComponentName2, retrievedSavedApp2.getComponents().get(0).getName());
		assertEquals("Second resource was not saved", newInterfaceName2, retrievedSavedApp2.getInterfaces().get(0).getName());
	}

	/**
	 * Test method for {@link EcorePlatformUtil#saveModel(Object, boolean, org.eclipse.core.runtime.IProgressMonitor)}
	 *
	 * @throws InterruptedException
	 */
	public void testSaveModel_Object_IProject_WithoutSaveOptions() throws Exception {
		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);

		Resource hbResource10_1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource hbResource10_2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		Resource hbResource10_3 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, true), false);
		assertNotNull(hbResource10_1);
		assertNotNull(hbResource10_2);
		assertNotNull(hbResource10_3);

		// First resource
		assertFalse(hbResource10_1.getContents().isEmpty());
		assertTrue(hbResource10_1.getContents().get(0) instanceof Application);
		final Application testApplicationHB10_1 = (Application) hbResource10_1.getContents().get(0);
		assertEquals(1, testApplicationHB10_1.getComponents().size());
		assertEquals(1, testApplicationHB10_1.getInterfaces().size());
		final Component testComponent1 = testApplicationHB10_1.getComponents().get(0);
		final Interface testInterface1 = testApplicationHB10_1.getInterfaces().get(0);

		assertFalse(hbResource10_2.getContents().isEmpty());
		assertTrue(hbResource10_2.getContents().get(0) instanceof Application);
		Application testApplicationHB10_2 = (Application) hbResource10_2.getContents().get(0);
		assertEquals(1, testApplicationHB10_2.getComponents().size());
		assertEquals(1, testApplicationHB10_2.getInterfaces().size());
		final Component testComponent2 = testApplicationHB10_2.getComponents().get(0);
		final Interface testInterface2 = testApplicationHB10_2.getInterfaces().get(0);

		final String newComponentName1 = "newComponentName1";
		final String newComponentName2 = "newComponentName2";

		final String newInterfaceName1 = "newInterface1";
		final String newInterfaceName2 = "newInterface2";

		// Modify resource
		try {
			WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain10, new Runnable() {
				@Override
				public void run() {
					// Rename objects in first resource
					testComponent1.setName(newComponentName1);
					testInterface1.setName(newInterfaceName1);
					// Modify second resource
					testComponent2.setName(newComponentName2);
					testInterface2.setName(newInterfaceName2);

				}
			}, "Modify model");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}

		assertEquals(newComponentName1, testComponent1.getName());
		assertEquals(newComponentName2, testComponent2.getName());
		assertEquals(newInterfaceName1, testInterface1.getName());
		assertEquals(newInterfaceName2, testInterface2.getName());

		EcorePlatformUtil.saveModel(refWks.hbProject10_A, true, new NullProgressMonitor());
		waitForModelSaving();

		// Reload project
		ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
		waitForModelLoading();

		// Verify the saved resource
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		Resource savedResource1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource savedResource2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		assertNotNull(savedResource1);
		assertNotNull(savedResource2);

		// Verify the modification was saved in the first resource
		assertEquals(1, savedResource1.getContents().size());
		assertTrue(savedResource1.getContents().get(0) instanceof Application);
		Application retrievedSavedApp1 = (Application) savedResource1.getContents().get(0);
		assertEquals(1, retrievedSavedApp1.getComponents().size());
		assertEquals(1, retrievedSavedApp1.getInterfaces().size());
		assertEquals("First resource was not saved", newComponentName1, retrievedSavedApp1.getComponents().get(0).getName());
		assertEquals("First resource was not saved", newInterfaceName1, retrievedSavedApp1.getInterfaces().get(0).getName());

		// Verify the modification was not saved in the second resource
		assertEquals(1, savedResource2.getContents().size());
		assertTrue(savedResource2.getContents().get(0) instanceof Application);
		Application retrievedSavedApp2 = (Application) savedResource2.getContents().get(0);
		assertEquals(1, retrievedSavedApp2.getComponents().size());
		assertEquals(1, retrievedSavedApp2.getInterfaces().size());
		assertEquals("Second resource was not saved", newComponentName2, retrievedSavedApp2.getComponents().get(0).getName());
		assertEquals("Second resource was not saved", newInterfaceName2, retrievedSavedApp2.getInterfaces().get(0).getName());
	}

	/**
	 * Test method for
	 * {@link EcorePlatformUtil#saveProject(org.eclipse.core.resources.IProject, boolean, org.eclipse.core.runtime.IProgressMonitor)}
	 *
	 * @throws InterruptedException
	 */
	public void testSaveProject_WithoutSaveOptions() throws Exception {
		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);

		Resource hbResource10_1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource hbResource10_2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		assertNotNull(hbResource10_1);
		assertNotNull(hbResource10_2);

		// First resource
		assertFalse(hbResource10_1.getContents().isEmpty());
		assertTrue(hbResource10_1.getContents().get(0) instanceof Application);
		final Application testApplicationHB10_1 = (Application) hbResource10_1.getContents().get(0);
		assertEquals(1, testApplicationHB10_1.getComponents().size());
		assertEquals(1, testApplicationHB10_1.getInterfaces().size());
		final Component testComponent1 = testApplicationHB10_1.getComponents().get(0);
		final Interface testInterface1 = testApplicationHB10_1.getInterfaces().get(0);

		assertFalse(hbResource10_2.getContents().isEmpty());
		assertTrue(hbResource10_2.getContents().get(0) instanceof Application);
		Application testApplicationHB10_2 = (Application) hbResource10_2.getContents().get(0);
		assertEquals(1, testApplicationHB10_2.getComponents().size());
		assertEquals(1, testApplicationHB10_2.getInterfaces().size());
		final Component testComponent2 = testApplicationHB10_2.getComponents().get(0);
		final Interface testInterface2 = testApplicationHB10_2.getInterfaces().get(0);

		final String newComponentName1 = "newComponentName1";
		final String newComponentName2 = "newComponentName2";

		final String newInterfaceName1 = "newInterface1";
		final String newInterfaceName2 = "newInterface2";

		// Modify resource
		try {
			WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain10, new Runnable() {
				@Override
				public void run() {
					// Rename objects in first resource
					testComponent1.setName(newComponentName1);
					testInterface1.setName(newInterfaceName1);
					// Modify second resource
					testComponent2.setName(newComponentName2);
					testInterface2.setName(newInterfaceName2);

				}
			}, "Modify model");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}

		assertEquals(newComponentName1, testComponent1.getName());
		assertEquals(newComponentName2, testComponent2.getName());
		assertEquals(newInterfaceName1, testInterface1.getName());
		assertEquals(newInterfaceName2, testInterface2.getName());

		EcorePlatformUtil.saveProject(refWks.hbProject10_A, true, new NullProgressMonitor());
		waitForModelSaving();

		// Reload project
		ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
		waitForModelLoading();

		// Verify the saved resource
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);

		Resource savedResource1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource savedResource2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		assertNotNull(savedResource1);
		assertNotNull(savedResource2);

		// Verify the modification was saved in the first resource
		assertEquals(1, savedResource1.getContents().size());
		assertTrue(savedResource1.getContents().get(0) instanceof Application);
		Application retrievedSavedApp1 = (Application) savedResource1.getContents().get(0);
		assertEquals(1, retrievedSavedApp1.getComponents().size());
		assertEquals(1, retrievedSavedApp1.getInterfaces().size());
		assertEquals("First resource was not saved", newComponentName1, retrievedSavedApp1.getComponents().get(0).getName());
		assertEquals("First resource was not saved", newInterfaceName1, retrievedSavedApp1.getInterfaces().get(0).getName());

		// Verify the modification was not saved in the second resource
		assertEquals(1, savedResource2.getContents().size());
		assertTrue(savedResource2.getContents().get(0) instanceof Application);
		Application retrievedSavedApp2 = (Application) savedResource2.getContents().get(0);
		assertEquals(1, retrievedSavedApp2.getComponents().size());
		assertEquals(1, retrievedSavedApp2.getInterfaces().size());
		assertEquals("Second resource was not saved", newComponentName2, retrievedSavedApp2.getComponents().get(0).getName());
		assertEquals("Second resource was not saved", newInterfaceName2, retrievedSavedApp2.getInterfaces().get(0).getName());
	}

	/**
	 * Test method for
	 * {@link EcorePlatformUtil#saveProject(org.eclipse.core.resources.IProject, java.util.Map, boolean, org.eclipse.core.runtime.IProgressMonitor)}
	 */
	public void testSaveProject_WithSaveOptions() throws Exception {
		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);

		Resource hbResource10_1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource hbResource10_2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		assertNotNull(hbResource10_1);
		assertNotNull(hbResource10_2);
		// First resource
		assertFalse(hbResource10_1.getContents().isEmpty());
		assertTrue(hbResource10_1.getContents().get(0) instanceof Application);
		final Application testApplicationHB10_1 = (Application) hbResource10_1.getContents().get(0);
		assertEquals(1, testApplicationHB10_1.getComponents().size());
		assertEquals(1, testApplicationHB10_1.getInterfaces().size());
		final Component testComponent1 = testApplicationHB10_1.getComponents().get(0);
		final Interface testInterface1 = testApplicationHB10_1.getInterfaces().get(0);

		assertFalse(hbResource10_2.getContents().isEmpty());
		assertTrue(hbResource10_2.getContents().get(0) instanceof Application);
		Application testApplicationHB10_2 = (Application) hbResource10_2.getContents().get(0);
		assertEquals(1, testApplicationHB10_2.getComponents().size());
		assertEquals(1, testApplicationHB10_2.getInterfaces().size());
		final Component testComponent2 = testApplicationHB10_2.getComponents().get(0);
		final Interface testInterface2 = testApplicationHB10_2.getInterfaces().get(0);

		final String newComponentName1 = "newComponentName1";
		final String newComponentName2 = "newComponentName2";

		final String newInterfaceName1 = "newInterface1";
		final String newInterfaceName2 = "newInterface2";

		// Modify resource
		try {
			WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain10, new Runnable() {
				@Override
				public void run() {
					// Rename objects in first resource
					testComponent1.setName(newComponentName1);
					testInterface1.setName(newInterfaceName1);
					// Modify second resource
					testComponent2.setName(newComponentName2);
					testInterface2.setName(newInterfaceName2);

				}
			}, "Modify model");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}

		assertEquals(newComponentName1, testComponent1.getName());
		assertEquals(newComponentName2, testComponent2.getName());
		assertEquals(newInterfaceName1, testInterface1.getName());
		assertEquals(newInterfaceName2, testInterface2.getName());
		// Save project with SaveOptions

		Map<Object, Object> options = new HashMap<Object, Object>();
		options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
		options.put(XMLResource.OPTION_SAVE_DOCTYPE, Boolean.TRUE);
		// TODO if save with DocTypeInfo, these resource will be removed from the EditingDomain. Have not found the
		// reason yet!
		// ((XMLResource) hbResource10_1).setDoctypeInfo("SPHINX GROUP", "SPHINX");
		// ((XMLResource) hbResource10_2).setDoctypeInfo("SPHINX GROUP", "SPHINX");
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);

		EcorePlatformUtil.saveProject(refWks.hbProject10_A, options, true, new NullProgressMonitor());
		waitForModelSaving();

		// Reload project
		ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, true, new NullProgressMonitor());
		waitForModelLoading();

		// Verify the saved resource
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);

		Resource savedResource1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource savedResource2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		assertNotNull(savedResource1);
		assertNotNull(savedResource2);

		// Verify the modification was saved in the first resource
		assertEquals(1, savedResource1.getContents().size());
		assertTrue(savedResource1.getContents().get(0) instanceof Application);
		Application retrievedSavedApp1 = (Application) savedResource1.getContents().get(0);
		assertEquals(1, retrievedSavedApp1.getComponents().size());
		assertEquals(1, retrievedSavedApp1.getInterfaces().size());
		assertEquals("First resource was not saved", newComponentName1, retrievedSavedApp1.getComponents().get(0).getName());
		assertEquals("First resource was not saved", newInterfaceName1, retrievedSavedApp1.getInterfaces().get(0).getName());

		// Verify the modification was not saved in the second resource
		assertEquals(1, savedResource2.getContents().size());
		assertTrue(savedResource2.getContents().get(0) instanceof Application);
		Application retrievedSavedApp2 = (Application) savedResource2.getContents().get(0);
		assertEquals(1, retrievedSavedApp2.getComponents().size());
		assertEquals(1, retrievedSavedApp2.getInterfaces().size());
		assertEquals("Second resource was not saved", newComponentName2, retrievedSavedApp2.getComponents().get(0).getName());
		assertEquals("Second resource was not saved", newInterfaceName2, retrievedSavedApp2.getInterfaces().get(0).getName());
	}

	/**
	 * Test method for {@link EcorePlatformUtil#unloadFile(IFile)}
	 *
	 * @throws Exception
	 * @throws OperationCanceledException
	 */
	public void testUnloadResource_File() throws Exception {
		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		int resourcesInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		int resourcesInEditingDomainUml2 = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);

		// Hummingbird10 files
		IFile hbFile10_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hbFile10_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile hbFile10_3 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertNotNull(hbFile10_1);
		assertNotNull(hbFile10_2);
		assertNotNull(hbFile10_3);
		assertTrue(hbFile10_1.exists());
		assertTrue(hbFile10_2.exists());
		assertTrue(hbFile10_3.exists());

		// Hummingbird20 files
		IFile hbFile20_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		IFile hbFile20_2 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		IFile hbFile20_3 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);
		IFile hbFile20_4 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile hbFile20_5 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile hbFile20_6 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);

		assertNotNull(hbFile20_1);
		assertNotNull(hbFile20_2);
		assertNotNull(hbFile20_3);
		assertNotNull(hbFile20_4);
		assertNotNull(hbFile20_5);
		assertNotNull(hbFile20_6);
		assertTrue(hbFile20_1.exists());
		assertTrue(hbFile20_2.exists());
		assertTrue(hbFile20_3.exists());
		assertTrue(hbFile20_4.exists());
		assertTrue(hbFile20_5.exists());
		assertTrue(hbFile20_6.exists());

		Collection<IFile> unloadedResources3x = new ArrayList<IFile>();
		unloadedResources3x.add(hbFile20_1);
		unloadedResources3x.add(hbFile20_2);
		unloadedResources3x.add(hbFile20_4);
		unloadedResources3x.add(hbFile20_5);

		// Uml File
		IFile uml2File_1 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		IFile uml2File_2 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		IFile uml2File_3 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertNotNull(uml2File_1);
		assertNotNull(uml2File_2);
		assertNotNull(uml2File_3);
		assertTrue(uml2File_1.exists());
		assertTrue(uml2File_2.exists());
		assertTrue(uml2File_3.exists());

		// Unload Null File
		IFile nullFile = null;
		EcorePlatformUtil.unloadFile(nullFile);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);
		// Unload Hummingbird20 File
		EcorePlatformUtil.unloadFile(hbFile20_1);
		waitForModelLoading();

		resourcesInEditingDomain20--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(hbFile20_2);
		waitForModelLoading();

		resourcesInEditingDomain20--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(hbFile20_3);
		waitForModelLoading();

		resourcesInEditingDomain20--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(hbFile20_4);
		waitForModelLoading();

		resourcesInEditingDomain20--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(hbFile20_5);
		waitForModelLoading();

		resourcesInEditingDomain20--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(hbFile20_6);
		waitForModelLoading();

		resourcesInEditingDomain20--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Unload Hummingbird10 File
		EcorePlatformUtil.unloadFile(hbFile10_1);
		waitForModelLoading();

		resourcesInEditingDomain10--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(hbFile10_2);
		waitForModelLoading();

		resourcesInEditingDomain10--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(hbFile10_3);
		waitForModelLoading();

		resourcesInEditingDomain10--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Unload Uml2File
		EcorePlatformUtil.unloadFile(uml2File_1);
		waitForModelLoading();

		resourcesInEditingDomainUml2--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(uml2File_2);
		waitForModelLoading();

		resourcesInEditingDomainUml2--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(uml2File_3);
		waitForModelLoading();

		resourcesInEditingDomainUml2--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

	}

	/**
	 * Test method for
	 * {@link EcorePlatformUtil#unloadFile(org.eclipse.emf.transaction.TransactionalEditingDomain, IFile)}
	 */
	public void testUnloadResource_File_WithEditingDomain() throws Exception {
		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		int resourcesInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		int resourcesInEditingDomainUml2 = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);

		IFile hbFile10_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hbFile10_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile hbFile10_3 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertNotNull(hbFile10_1);
		assertNotNull(hbFile10_2);
		assertNotNull(hbFile10_3);
		assertTrue(hbFile10_1.exists());
		assertTrue(hbFile10_2.exists());
		assertTrue(hbFile10_3.exists());

		TransactionalEditingDomain nullEditingDomain = null;
		IFile nullFile = null;
		// Unload with NullEditing Domain
		EcorePlatformUtil.unloadFile(nullEditingDomain, hbFile10_1);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Unload with Null File
		EcorePlatformUtil.unloadFile(refWks.editingDomain10, nullFile);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Unload with NullEditingDomain and NUlL file

		EcorePlatformUtil.unloadFile(nullEditingDomain, nullFile);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Unload Files HB10
		EcorePlatformUtil.unloadFile(refWks.editingDomain10, hbFile10_1);
		waitForModelLoading();

		resourcesInEditingDomain10--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(refWks.editingDomain10, hbFile10_2);
		waitForModelLoading();

		resourcesInEditingDomain10--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(refWks.editingDomain10, hbFile10_3);
		waitForModelLoading();

		resourcesInEditingDomain10--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);
		// =================================================
		// File HB20
		IFile hbFile20_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		IFile hbFile20_2 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		IFile hbFile20_3 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);
		IFile hbFile20_4 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile hbFile20_5 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile hbFile20_6 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);

		assertNotNull(hbFile20_1);
		assertNotNull(hbFile20_2);
		assertNotNull(hbFile20_3);
		assertNotNull(hbFile20_4);
		assertNotNull(hbFile20_5);
		assertNotNull(hbFile20_6);
		assertTrue(hbFile20_1.exists());
		assertTrue(hbFile20_2.exists());
		assertTrue(hbFile20_3.exists());
		assertTrue(hbFile20_4.exists());
		assertTrue(hbFile20_5.exists());
		assertTrue(hbFile20_6.exists());
		// Unload ResourceHB20 with EditingDomain10
		EcorePlatformUtil.unloadFile(refWks.editingDomain10, hbFile20_1);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Unload ResourceHB20 with EditingDomain20
		EcorePlatformUtil.unloadFile(refWks.editingDomain20, hbFile20_1);
		waitForModelLoading();

		resourcesInEditingDomain20--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(refWks.editingDomain20, hbFile20_2);
		waitForModelLoading();

		resourcesInEditingDomain20--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(refWks.editingDomain20, hbFile20_3);
		waitForModelLoading();

		resourcesInEditingDomain20--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(refWks.editingDomain20, hbFile20_4);
		waitForModelLoading();

		resourcesInEditingDomain20--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(refWks.editingDomain20, hbFile20_5);
		waitForModelLoading();

		resourcesInEditingDomain20--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(refWks.editingDomain20, hbFile20_6);
		waitForModelLoading();

		resourcesInEditingDomain20--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);
		// =============================================
		// Uml File
		IFile uml2File_1 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		IFile uml2File_2 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		IFile uml2File_3 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertNotNull(uml2File_1);
		assertNotNull(uml2File_2);
		assertNotNull(uml2File_3);
		assertTrue(uml2File_1.exists());
		assertTrue(uml2File_2.exists());
		assertTrue(uml2File_3.exists());

		// Unload uml2Resource with EditingDomain20
		EcorePlatformUtil.unloadFile(refWks.editingDomain20, uml2File_1);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Unload Uml2Resource with EditingDomainUml2
		EcorePlatformUtil.unloadFile(refWks.editingDomainUml2, uml2File_1);
		waitForModelLoading();

		resourcesInEditingDomainUml2--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(refWks.editingDomainUml2, uml2File_2);
		waitForModelLoading();

		resourcesInEditingDomainUml2--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		EcorePlatformUtil.unloadFile(refWks.editingDomainUml2, uml2File_3);
		waitForModelLoading();

		resourcesInEditingDomainUml2--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

	}

	/**
	 * Test method for
	 * {@link EcorePlatformUtil#unloadFiles(org.eclipse.emf.transaction.TransactionalEditingDomain, java.util.Collection, boolean, org.eclipse.core.runtime.IProgressMonitor)}
	 */
	public void testUnloadFiles_with_memoryOptimized() throws Exception {
		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		int resourcesInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		int resourcesInEditingDomainUml2 = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);

		// File HB10
		IFile hbFile10_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hbFile10_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile hbFile10_3 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertNotNull(hbFile10_1);
		assertNotNull(hbFile10_2);
		assertNotNull(hbFile10_3);
		assertTrue(hbFile10_1.exists());
		assertTrue(hbFile10_2.exists());
		assertTrue(hbFile10_3.exists());

		Resource hbResource10_1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource hbResource10_2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		Resource hbResource10_3 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, true), false);
		assertNotNull(hbResource10_1);
		assertNotNull(hbResource10_2);
		assertNotNull(hbResource10_3);
		Collection<IFile> unloadedResources10 = new ArrayList<IFile>();
		unloadedResources10.add(hbFile10_1);
		unloadedResources10.add(hbFile10_2);
		// Verify tested resource are loaded
		Resource resource = refWks.editingDomain10.getResourceSet().getResource(hbResource10_1.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot10_1 = resource.getContents().get(0);

		assertNotNull(modelRoot10_1);
		assertFalse(modelRoot10_1.eIsProxy());
		assertTrue(modelRoot10_1.eContents().size() > 0);

		resource = refWks.editingDomain10.getResourceSet().getResource(hbResource10_2.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot10_2 = resource.getContents().get(0);

		assertNotNull(modelRoot10_2);
		assertFalse(modelRoot10_2.eIsProxy());
		assertTrue(modelRoot10_2.eContents().size() > 0);
		// Unload Files HB10
		EcorePlatformUtil.unloadFiles(refWks.editingDomain10, unloadedResources10, true, new NullProgressMonitor());
		waitForModelLoading();

		resourcesInEditingDomain10 = resourcesInEditingDomain10 - unloadedResources10.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Verify that unloaded resources were removed from ResourceSet and proxified
		assertFalse(refWks.editingDomain10.getResourceSet().getResources().contains(hbResource10_1));
		assertFalse(refWks.editingDomain10.getResourceSet().getResources().contains(hbResource10_2));
		assertTrue(refWks.editingDomain10.getResourceSet().getResources().contains(hbResource10_3));

		assertNotNull(modelRoot10_1);
		assertTrue(modelRoot10_1.eIsProxy());

		assertNotNull(modelRoot10_2);
		assertTrue(modelRoot10_2.eIsProxy());

		// ===================================================
		// File HB20
		IFile hbFile20_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		IFile hbFile20_2 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		IFile hbFile20_3 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);
		IFile hbFile20_4 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile hbFile20_5 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile hbFile20_6 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);

		assertNotNull(hbFile20_1);
		assertNotNull(hbFile20_2);
		assertNotNull(hbFile20_3);
		assertNotNull(hbFile20_4);
		assertNotNull(hbFile20_5);
		assertNotNull(hbFile20_6);
		assertTrue(hbFile20_1.exists());
		assertTrue(hbFile20_2.exists());
		assertTrue(hbFile20_3.exists());
		assertTrue(hbFile20_4.exists());
		assertTrue(hbFile20_5.exists());
		assertTrue(hbFile20_6.exists());

		Resource hbResource20_1 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		Resource hbResource20_2 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);
		Resource hbResource20_3 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, true), false);
		Resource hbResource20_4 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		Resource hbResource20_5 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, true), false);
		Resource hbResource20_6 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, true), false);
		assertNotNull(hbResource20_1);
		assertNotNull(hbResource20_2);
		assertNotNull(hbResource20_3);
		assertNotNull(hbResource20_4);
		assertNotNull(hbResource20_5);
		assertNotNull(hbResource20_6);

		Collection<IFile> unloadedResources20 = new ArrayList<IFile>();
		unloadedResources20.add(hbFile20_1);
		unloadedResources20.add(hbFile20_2);
		unloadedResources20.add(hbFile20_4);
		unloadedResources20.add(hbFile20_5);
		// Verify tested resources are loaded
		resource = refWks.editingDomain20.getResourceSet().getResource(hbResource20_1.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot20_1 = resource.getContents().get(0);

		assertNotNull(modelRoot20_1);
		assertFalse(modelRoot20_1.eIsProxy());
		assertTrue(modelRoot20_1.eContents().size() > 0);

		resource = refWks.editingDomain20.getResourceSet().getResource(hbResource20_2.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot20_2 = resource.getContents().get(0);

		assertNotNull(modelRoot20_2);
		assertFalse(modelRoot20_2.eIsProxy());
		assertTrue(modelRoot20_2.eContents().size() > 0);

		resource = refWks.editingDomain20.getResourceSet().getResource(hbResource20_4.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot20_4 = resource.getContents().get(0);

		assertNotNull(modelRoot20_4);
		assertFalse(modelRoot20_4.eIsProxy());
		assertTrue(modelRoot20_4.eContents().size() > 0);

		resource = refWks.editingDomain20.getResourceSet().getResource(hbResource20_5.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot20_5 = resource.getContents().get(0);

		assertNotNull(modelRoot20_5);
		assertFalse(modelRoot20_5.eIsProxy());
		assertTrue(modelRoot20_5.eContents().size() > 0);
		// Unload Resource20 with EditingDomain10
		EcorePlatformUtil.unloadFiles(refWks.editingDomain10, unloadedResources20, true, new NullProgressMonitor());
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Unload Resource20 with EditingDomain20
		EcorePlatformUtil.unloadFiles(refWks.editingDomain20, unloadedResources20, true, new NullProgressMonitor());
		waitForModelLoading();

		resourcesInEditingDomain20 = resourcesInEditingDomain20 - unloadedResources20.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Verify unloaded resources were removed from ResourceSet and proxified
		assertFalse(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_1));
		assertFalse(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_2));
		assertFalse(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_4));
		assertFalse(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_5));

		assertTrue(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_3));
		assertTrue(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_6));

		assertNotNull(modelRoot20_1);
		assertTrue(modelRoot20_1.eIsProxy());
		assertTrue(((InternalEObject) modelRoot20_1).eProxyURI().isEmpty());
		assertEquals(3, modelRoot20_1.eContents().size());

		assertNotNull(modelRoot20_2);
		assertTrue(modelRoot20_2.eIsProxy());
		assertTrue(((InternalEObject) modelRoot20_2).eProxyURI().isEmpty());
		assertEquals(5, modelRoot20_2.eContents().size());

		assertNotNull(modelRoot20_4);
		assertTrue(modelRoot20_4.eIsProxy());
		assertTrue(((InternalEObject) modelRoot20_4).eProxyURI().isEmpty());
		assertEquals(2, modelRoot20_4.eContents().size());

		assertNotNull(modelRoot20_5);
		assertTrue(modelRoot20_5.eIsProxy());
		assertTrue(((InternalEObject) modelRoot20_5).eProxyURI().isEmpty());
		assertEquals(5, modelRoot20_5.eContents().size());

		// ===============================================
		// Uml File
		IFile uml2File_1 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		IFile uml2File_2 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		IFile uml2File_3 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertNotNull(uml2File_1);
		assertNotNull(uml2File_2);
		assertNotNull(uml2File_3);
		assertTrue(uml2File_1.exists());
		assertTrue(uml2File_2.exists());
		assertTrue(uml2File_3.exists());

		Resource uml2Resource_1 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		Resource uml2Resource_2 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2, true), false);
		Resource uml2Resource_3 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3, true), false);
		assertNotNull(uml2Resource_1);
		assertNotNull(uml2Resource_2);
		assertNotNull(uml2Resource_3);
		ExtendedResource extendedUml2Resource_1 = ExtendedResourceAdapterFactory.INSTANCE.adapt(uml2Resource_1);
		ExtendedResource extendedUml2Resource_2 = ExtendedResourceAdapterFactory.INSTANCE.adapt(uml2Resource_2);
		ExtendedResource extendedUml2Resource_3 = ExtendedResourceAdapterFactory.INSTANCE.adapt(uml2Resource_3);
		assertTrue(extendedUml2Resource_1 instanceof ExtendedResourceAdapter);
		assertTrue(extendedUml2Resource_2 instanceof ExtendedResourceAdapter);
		assertTrue(extendedUml2Resource_3 instanceof ExtendedResourceAdapter);
		Collection<IFile> unloadedUml2Resources = new ArrayList<IFile>();
		// Unload empty resource
		EcorePlatformUtil.unloadFiles(refWks.editingDomain20, unloadedUml2Resources, true, new NullProgressMonitor());
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		unloadedUml2Resources.add(uml2File_1);
		// Verify tested resource were loaded
		resource = refWks.editingDomainUml2.getResourceSet().getResource(uml2Resource_1.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRootUml2_1 = resource.getContents().get(0);

		assertNotNull(modelRootUml2_1);
		assertFalse(modelRootUml2_1.eIsProxy());
		assertTrue(modelRootUml2_1.eContents().size() > 0);
		// Unload uml2Resource with EditingDomain20
		EcorePlatformUtil.unloadFiles(refWks.editingDomain20, unloadedUml2Resources, true, new NullProgressMonitor());
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Unload ResourceUml2 with EditingDomainUml2
		EcorePlatformUtil.unloadFiles(refWks.editingDomainUml2, unloadedUml2Resources, true, new NullProgressMonitor());
		waitForModelLoading();

		resourcesInEditingDomainUml2 = resourcesInEditingDomainUml2 - unloadedUml2Resources.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Verify unloaded resources were removed from ResourceSet and not proxified
		assertFalse(refWks.editingDomainUml2.getResourceSet().getResources().contains(uml2Resource_1));
		assertTrue(refWks.editingDomainUml2.getResourceSet().getResources().contains(uml2Resource_2));
		assertTrue(refWks.editingDomainUml2.getResourceSet().getResources().contains(uml2Resource_3));
		// Uml resource should be proxifed
		assertNotNull(modelRootUml2_1);
		assertTrue(modelRootUml2_1.eIsProxy());
		assertTrue(modelRootUml2_1.eContents().size() > 0);
	}

	public void testUnloadFiles_without_memoryOptimized() throws Exception {
		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		int resourcesInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		int resourcesInEditingDomainUml2 = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		// File HB10
		IFile hbFile10_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hbFile10_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile hbFile10_3 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertNotNull(hbFile10_1);
		assertNotNull(hbFile10_2);
		assertNotNull(hbFile10_3);
		assertTrue(hbFile10_1.exists());
		assertTrue(hbFile10_2.exists());
		assertTrue(hbFile10_3.exists());

		Resource hbResource10_1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource hbResource10_2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		Resource hbResource10_3 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, true), false);
		assertNotNull(hbResource10_1);
		assertNotNull(hbResource10_2);
		assertNotNull(hbResource10_3);

		Collection<IFile> unloadedResources10 = new ArrayList<IFile>();
		unloadedResources10.add(hbFile10_1);
		unloadedResources10.add(hbFile10_2);

		// Verify tested resources are loaded
		Resource resource = refWks.editingDomain10.getResourceSet().getResource(hbResource10_1.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot10_1 = resource.getContents().get(0);

		assertNotNull(modelRoot10_1);
		assertFalse(modelRoot10_1.eIsProxy());
		assertTrue(modelRoot10_1.eContents().size() > 0);

		resource = refWks.editingDomain10.getResourceSet().getResource(hbResource10_2.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot10_2 = resource.getContents().get(0);

		assertNotNull(modelRoot10_2);
		assertFalse(modelRoot10_2.eIsProxy());
		assertTrue(modelRoot10_2.eContents().size() > 0);

		// Unload Files HB10
		EcorePlatformUtil.unloadFiles(refWks.editingDomain10, unloadedResources10, true, new NullProgressMonitor());
		waitForModelLoading();

		resourcesInEditingDomain10 = resourcesInEditingDomain10 - unloadedResources10.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Verify that unloaded resources were removed from ResourceSet and proxified
		assertFalse(refWks.editingDomain10.getResourceSet().getResources().contains(hbResource10_1));
		assertFalse(refWks.editingDomain10.getResourceSet().getResources().contains(hbResource10_2));
		assertTrue(refWks.editingDomain10.getResourceSet().getResources().contains(hbResource10_3));

		assertNotNull(modelRoot10_1);
		assertTrue(modelRoot10_1.eIsProxy());
		assertTrue(modelRoot10_1.eContents().size() > 0);

		assertNotNull(modelRoot10_2);
		assertTrue(modelRoot10_2.eIsProxy());
		assertTrue(modelRoot10_2.eContents().size() > 0);
		// ================================================
		// File HB20
		IFile hbFile20_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		IFile hbFile20_2 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		IFile hbFile20_3 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);
		IFile hbFile20_4 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile hbFile20_5 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile hbFile20_6 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);

		assertNotNull(hbFile20_1);
		assertNotNull(hbFile20_2);
		assertNotNull(hbFile20_3);
		assertNotNull(hbFile20_4);
		assertNotNull(hbFile20_5);
		assertNotNull(hbFile20_6);
		assertTrue(hbFile20_1.exists());
		assertTrue(hbFile20_2.exists());
		assertTrue(hbFile20_3.exists());
		assertTrue(hbFile20_4.exists());
		assertTrue(hbFile20_5.exists());
		assertTrue(hbFile20_6.exists());

		Resource hbResource20_1 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		Resource hbResource20_2 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);
		Resource hbResource20_3 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, true), false);
		Resource hbResource20_4 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		Resource hbResource20_5 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, true), false);
		Resource hbResource20_6 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, true), false);
		assertNotNull(hbResource20_1);
		assertNotNull(hbResource20_2);
		assertNotNull(hbResource20_3);
		assertNotNull(hbResource20_4);
		assertNotNull(hbResource20_5);
		assertNotNull(hbResource20_6);

		Collection<IFile> unloadedResources20 = new ArrayList<IFile>();
		unloadedResources20.add(hbFile20_1);
		unloadedResources20.add(hbFile20_2);
		unloadedResources20.add(hbFile20_4);
		unloadedResources20.add(hbFile20_5);

		// Verify tested resources are loaded
		resource = refWks.editingDomain20.getResourceSet().getResource(hbResource20_1.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot20_1 = resource.getContents().get(0);

		assertNotNull(modelRoot20_1);
		assertFalse(modelRoot20_1.eIsProxy());
		assertTrue(modelRoot20_1.eContents().size() > 0);

		resource = refWks.editingDomain20.getResourceSet().getResource(hbResource20_2.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot20_2 = resource.getContents().get(0);

		assertNotNull(modelRoot20_2);
		assertFalse(modelRoot20_2.eIsProxy());
		assertTrue(modelRoot20_2.eContents().size() > 0);

		resource = refWks.editingDomain20.getResourceSet().getResource(hbResource20_4.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot20_4 = resource.getContents().get(0);

		assertNotNull(modelRoot20_4);
		assertFalse(modelRoot20_4.eIsProxy());
		assertTrue(modelRoot20_4.eContents().size() > 0);

		resource = refWks.editingDomain20.getResourceSet().getResource(hbResource20_5.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot20_5 = resource.getContents().get(0);

		assertNotNull(modelRoot20_5);
		assertFalse(modelRoot20_5.eIsProxy());
		assertTrue(modelRoot20_5.eContents().size() > 0);

		// Unload Resource20 with EditingDomain10
		EcorePlatformUtil.unloadFiles(refWks.editingDomain10, unloadedResources20, true, new NullProgressMonitor());
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Unload Resource20 with EditingDomain20
		EcorePlatformUtil.unloadFiles(refWks.editingDomain20, unloadedResources20, true, new NullProgressMonitor());
		waitForModelLoading();

		resourcesInEditingDomain20 = resourcesInEditingDomain20 - unloadedResources20.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Verify unloaded resources were removed from ResourceSet and proxified
		assertFalse(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_1));
		assertFalse(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_2));
		assertFalse(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_4));
		assertFalse(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_5));

		assertTrue(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_3));
		assertTrue(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_6));

		assertNotNull(modelRoot20_1);
		assertTrue(modelRoot20_1.eIsProxy());
		assertTrue(modelRoot20_1.eContents().size() > 0);

		assertNotNull(modelRoot20_2);
		assertTrue(modelRoot20_2.eIsProxy());
		assertTrue(modelRoot20_2.eContents().size() > 0);

		assertNotNull(modelRoot20_4);
		assertTrue(modelRoot20_4.eIsProxy());
		assertTrue(modelRoot20_4.eContents().size() > 0);

		assertNotNull(modelRoot20_5);
		assertTrue(modelRoot20_5.eIsProxy());
		assertTrue(modelRoot20_5.eContents().size() > 0);
		// =====================================================
		// Uml File
		IFile uml2File_1 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		IFile uml2File_2 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		IFile uml2File_3 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertNotNull(uml2File_1);
		assertNotNull(uml2File_2);
		assertNotNull(uml2File_3);
		assertTrue(uml2File_1.exists());
		assertTrue(uml2File_2.exists());
		assertTrue(uml2File_3.exists());

		Resource uml2Resource_1 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		Resource uml2Resource_2 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2, true), false);
		Resource uml2Resource_3 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3, true), false);
		assertNotNull(uml2Resource_1);
		assertNotNull(uml2Resource_2);
		assertNotNull(uml2Resource_3);
		ExtendedResource extendedUml2Resource_1 = ExtendedResourceAdapterFactory.INSTANCE.adapt(uml2Resource_1);
		ExtendedResource extendedUml2Resource_2 = ExtendedResourceAdapterFactory.INSTANCE.adapt(uml2Resource_2);
		ExtendedResource extendedUml2Resource_3 = ExtendedResourceAdapterFactory.INSTANCE.adapt(uml2Resource_3);
		assertTrue(extendedUml2Resource_1 instanceof ExtendedResourceAdapter);
		assertTrue(extendedUml2Resource_2 instanceof ExtendedResourceAdapter);
		assertTrue(extendedUml2Resource_3 instanceof ExtendedResourceAdapter);

		Collection<IFile> unloadedUml2Resources = new ArrayList<IFile>();
		// Unload empty resource
		EcorePlatformUtil.unloadFiles(refWks.editingDomain20, unloadedUml2Resources, true, new NullProgressMonitor());
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		unloadedUml2Resources.add(uml2File_1);
		// Verify tested resource were loaded
		resource = refWks.editingDomainUml2.getResourceSet().getResource(uml2Resource_1.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRootUml2_1 = resource.getContents().get(0);

		assertNotNull(modelRootUml2_1);
		assertFalse(modelRootUml2_1.eIsProxy());
		assertTrue(modelRootUml2_1.eContents().size() > 0);
		// Unload uml2Resource with EditingDomain20
		EcorePlatformUtil.unloadFiles(refWks.editingDomain20, unloadedUml2Resources, true, new NullProgressMonitor());
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Unload ResourceUml2 with EditingDomainUml2
		EcorePlatformUtil.unloadFiles(refWks.editingDomainUml2, unloadedUml2Resources, true, new NullProgressMonitor());
		waitForModelLoading();

		resourcesInEditingDomainUml2 = resourcesInEditingDomainUml2 - unloadedUml2Resources.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Verify unloaded resources were removed from ResourceSet and proxified
		assertFalse(refWks.editingDomainUml2.getResourceSet().getResources().contains(uml2Resource_1));
		assertTrue(refWks.editingDomainUml2.getResourceSet().getResources().contains(uml2Resource_2));
		assertTrue(refWks.editingDomainUml2.getResourceSet().getResources().contains(uml2Resource_3));

		assertNotNull(modelRootUml2_1);
		assertTrue(modelRootUml2_1.eIsProxy());
		assertTrue(modelRootUml2_1.eContents().size() > 0);
	}

	/**
	 * Test method for
	 * {@link EcorePlatformUtil#unloadResources(org.eclipse.emf.transaction.TransactionalEditingDomain, java.util.Collection)}
	 */
	public void testUnloadResources_without_memoryOptimized() throws Exception {

		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		int resourcesInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		int resourcesInEditingDomainUml2 = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);

		// Unload AllResource of EditingDomain20
		Collection<Resource> unloadedResources20 = new ArrayList<Resource>();
		Resource hbResource20_1 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		Resource hbResource20_2 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);
		Resource hbResource20_3 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, true), false);
		Resource hbResource20_4 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		Resource hbResource20_5 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, true), false);
		Resource hbResource20_6 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, true), false);
		assertNotNull(hbResource20_1);
		assertNotNull(hbResource20_2);
		assertNotNull(hbResource20_3);
		assertNotNull(hbResource20_4);
		assertNotNull(hbResource20_5);
		assertNotNull(hbResource20_6);

		unloadedResources20.add(hbResource20_1);
		unloadedResources20.add(hbResource20_4);
		unloadedResources20.add(hbResource20_5);
		// Verify tested resources are loaded
		Resource resource = refWks.editingDomain20.getResourceSet().getResource(hbResource20_1.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot20_1 = resource.getContents().get(0);

		assertNotNull(modelRoot20_1);
		assertFalse(modelRoot20_1.eIsProxy());

		resource = refWks.editingDomain20.getResourceSet().getResource(hbResource20_4.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot20_4 = resource.getContents().get(0);
		assertNotNull(modelRoot20_4);
		assertFalse(modelRoot20_4.eIsProxy());

		resource = refWks.editingDomain20.getResourceSet().getResource(hbResource20_5.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot20_5 = resource.getContents().get(0);
		assertNotNull(modelRoot20_5);
		assertFalse(modelRoot20_5.eIsProxy());

		EcorePlatformUtil.unloadResources(refWks.editingDomain20, unloadedResources20, true, new NullProgressMonitor());
		waitForModelLoading();

		resourcesInEditingDomain20 = resourcesInEditingDomain20 - unloadedResources20.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);
		// Verify unloaded resource are removed from the resource set and proxified
		assertFalse(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_1));
		assertFalse(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_4));
		assertFalse(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_5));

		assertTrue(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_2));
		assertTrue(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_3));
		assertTrue(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_6));

		assertNotNull(modelRoot20_1);
		assertTrue(modelRoot20_1.eIsProxy());
		assertTrue(modelRoot20_1.eContents().size() > 0);

		assertNotNull(modelRoot20_4);
		assertTrue(modelRoot20_4.eIsProxy());
		assertTrue(modelRoot20_4.eContents().size() > 0);

		assertNotNull(modelRoot20_5);
		assertTrue(modelRoot20_5.eIsProxy());
		assertTrue(modelRoot20_5.eContents().size() > 0);

		// Unload AllResource of EditingDomain10
		Collection<Resource> unloadedResources10 = new ArrayList<Resource>();
		Resource hbResource10_1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource hbResource10_2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		Resource hbResource10_3 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, true), false);
		assertNotNull(hbResource10_1);
		assertNotNull(hbResource10_2);
		assertNotNull(hbResource10_3);

		unloadedResources10.add(hbResource10_1);
		unloadedResources10.add(hbResource10_2);
		// Verify tested resource were loaded
		resource = refWks.editingDomain10.getResourceSet().getResource(hbResource10_1.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot10_1 = resource.getContents().get(0);
		assertNotNull(modelRoot10_1);
		assertFalse(modelRoot10_1.eIsProxy());

		resource = refWks.editingDomain10.getResourceSet().getResource(hbResource10_2.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot10_2 = resource.getContents().get(0);
		assertNotNull(modelRoot10_2);
		assertFalse(modelRoot10_2.eIsProxy());

		// UnloadResources10 with EditingDomain20
		EcorePlatformUtil.unloadResources(refWks.editingDomain20, unloadedResources10, true, new NullProgressMonitor());
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// UnloadResourc10 with EditingDomain10
		EcorePlatformUtil.unloadResources(refWks.editingDomain10, unloadedResources10, true, new NullProgressMonitor());
		waitForModelLoading();

		resourcesInEditingDomain10 = resourcesInEditingDomain10 - unloadedResources10.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);
		// Verify unloaded resource were removed from ResourceSet and proxified
		assertFalse(refWks.editingDomain10.getResourceSet().getResources().contains(hbResource10_1));
		assertFalse(refWks.editingDomain10.getResourceSet().getResources().contains(hbResource10_1));
		assertTrue(refWks.editingDomain10.getResourceSet().getResources().contains(hbResource10_3));

		assertNotNull(modelRoot10_1);
		assertTrue(modelRoot10_1.eIsProxy());
		assertTrue(modelRoot10_1.eContents().size() > 0);

		assertNotNull(modelRoot10_2);
		assertTrue(modelRoot10_2.eIsProxy());
		assertTrue(modelRoot10_2.eContents().size() > 0);

		// Unload AllResource of uml2EditingDomain
		Collection<Resource> unloadedUml2Resources = new ArrayList<Resource>();
		Resource uml2Resource_1 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		Resource uml2Resource_2 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2, true), false);
		Resource uml2Resource_3 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3, true), false);
		assertNotNull(uml2Resource_1);
		assertNotNull(uml2Resource_2);
		assertNotNull(uml2Resource_3);
		ExtendedResource extendedUml2Resource_1 = ExtendedResourceAdapterFactory.INSTANCE.adapt(uml2Resource_1);
		ExtendedResource extendedUml2Resource_2 = ExtendedResourceAdapterFactory.INSTANCE.adapt(uml2Resource_2);
		ExtendedResource extendedUml2Resource_3 = ExtendedResourceAdapterFactory.INSTANCE.adapt(uml2Resource_3);
		assertTrue(extendedUml2Resource_1 instanceof ExtendedResourceAdapter);
		assertTrue(extendedUml2Resource_2 instanceof ExtendedResourceAdapter);
		assertTrue(extendedUml2Resource_3 instanceof ExtendedResourceAdapter);

		unloadedUml2Resources.add(uml2Resource_1);
		unloadedUml2Resources.add(uml2Resource_2);

		// Verify tested resource were loaded
		resource = refWks.editingDomainUml2.getResourceSet().getResource(uml2Resource_1.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRootUml2_1 = resource.getContents().get(0);
		assertNotNull(modelRootUml2_1);
		assertFalse(modelRootUml2_1.eIsProxy());

		resource = refWks.editingDomainUml2.getResourceSet().getResource(uml2Resource_2.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRootUml2_2 = resource.getContents().get(0);
		assertNotNull(modelRootUml2_2);
		assertFalse(modelRootUml2_2.eIsProxy());

		// Unload uml2Resource with EditingDomain20
		EcorePlatformUtil.unloadResources(refWks.editingDomain20, unloadedUml2Resources, true, new NullProgressMonitor());
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Unload uml2Resource with uml2EditingDomain
		EcorePlatformUtil.unloadResources(refWks.editingDomainUml2, unloadedUml2Resources, true, new NullProgressMonitor());
		waitForModelLoading();

		resourcesInEditingDomainUml2 = resourcesInEditingDomainUml2 - unloadedUml2Resources.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Verify unloaded resource were removed from the ResourceSet and proxified
		assertFalse(refWks.editingDomainUml2.getResourceSet().getResources().contains(uml2Resource_1));
		assertFalse(refWks.editingDomainUml2.getResourceSet().getResources().contains(uml2Resource_2));
		assertTrue(refWks.editingDomainUml2.getResourceSet().getResources().contains(uml2Resource_3));

		assertNotNull(modelRootUml2_1);
		assertTrue(modelRootUml2_1.eIsProxy());
		assertTrue(modelRootUml2_1.eContents().size() > 0);

		assertNotNull(modelRootUml2_2);
		assertTrue(modelRootUml2_2.eIsProxy());
		assertTrue(modelRootUml2_2.eContents().size() > 0);
	}

	public void testUnloadResources_with_memoryOptimized() throws Exception {
		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		int resourcesInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		int resourcesInEditingDomainUml2 = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);

		// Unload AllResource of EditingDomain20
		Collection<Resource> unloadedResources20 = new ArrayList<Resource>();
		Resource hbResource20_1 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		Resource hbResource20_2 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);
		Resource hbResource20_3 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, true), false);
		Resource hbResource20_4 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		Resource hbResource20_5 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, true), false);
		Resource hbResource20_6 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, true), false);
		assertNotNull(hbResource20_1);
		assertNotNull(hbResource20_2);
		assertNotNull(hbResource20_3);
		assertNotNull(hbResource20_4);
		assertNotNull(hbResource20_5);
		assertNotNull(hbResource20_6);

		unloadedResources20.add(hbResource20_1);
		unloadedResources20.add(hbResource20_4);
		unloadedResources20.add(hbResource20_5);

		// Verify tested resources are loaded
		Resource resource = refWks.editingDomain20.getResourceSet().getResource(hbResource20_1.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot20_1 = resource.getContents().get(0);

		assertNotNull(modelRoot20_1);
		assertFalse(modelRoot20_1.eIsProxy());

		resource = refWks.editingDomain20.getResourceSet().getResource(hbResource20_4.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot20_4 = resource.getContents().get(0);

		assertNotNull(modelRoot20_4);
		assertFalse(modelRoot20_4.eIsProxy());

		resource = refWks.editingDomain20.getResourceSet().getResource(hbResource20_5.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot20_5 = resource.getContents().get(0);

		assertNotNull(modelRoot20_5);
		assertFalse(modelRoot20_5.eIsProxy());

		EcorePlatformUtil.unloadResources(refWks.editingDomain20, unloadedResources20, true, new NullProgressMonitor());
		waitForModelLoading();

		resourcesInEditingDomain20 = resourcesInEditingDomain20 - unloadedResources20.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Verify unloaded Resource were removed from the ResourceSet and memory also
		assertFalse(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_1));
		assertFalse(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_4));
		assertFalse(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_5));

		assertTrue(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_2));
		assertTrue(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_3));
		assertTrue(refWks.editingDomain20.getResourceSet().getResources().contains(hbResource20_6));

		assertNotNull(modelRoot20_1);
		assertTrue(modelRoot20_1.eIsProxy());
		assertTrue(((InternalEObject) modelRoot20_1).eProxyURI().isEmpty());
		assertEquals(3, modelRoot20_1.eContents().size());

		assertNotNull(modelRoot20_4);
		assertTrue(modelRoot20_4.eIsProxy());
		assertTrue(((InternalEObject) modelRoot20_4).eProxyURI().isEmpty());
		assertEquals(2, modelRoot20_4.eContents().size());

		assertNotNull(modelRoot20_5);
		assertTrue(modelRoot20_5.eIsProxy());
		assertTrue(((InternalEObject) modelRoot20_5).eProxyURI().isEmpty());
		assertEquals(5, modelRoot20_5.eContents().size());

		// Unload AllResource of EditingDomain21
		Collection<Resource> unloadedResources10 = new ArrayList<Resource>();
		Resource hbResource10_1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource hbResource10_2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		Resource hbResource10_3 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, true), false);
		assertNotNull(hbResource10_1);
		assertNotNull(hbResource10_2);
		assertNotNull(hbResource10_3);

		unloadedResources10.add(hbResource10_1);
		unloadedResources10.add(hbResource10_2);

		// Verify tested resource are loaded
		resource = refWks.editingDomain10.getResourceSet().getResource(hbResource10_1.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot10_1 = resource.getContents().get(0);

		assertNotNull(modelRoot10_1);
		assertFalse(modelRoot10_1.eIsProxy());
		assertTrue(modelRoot10_1.eContents().size() > 0);

		resource = refWks.editingDomain10.getResourceSet().getResource(hbResource10_2.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot10_2 = resource.getContents().get(0);
		assertNotNull(modelRoot10_2);
		assertFalse(modelRoot10_2.eIsProxy());
		assertTrue(modelRoot10_2.eContents().size() > 0);

		// UnloadResources10 with EditingDomain20
		EcorePlatformUtil.unloadResources(refWks.editingDomain20, unloadedResources10, true, new NullProgressMonitor());
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// UnloadResource10 with EditingDomain10
		EcorePlatformUtil.unloadResources(refWks.editingDomain10, unloadedResources10, true, new NullProgressMonitor());
		waitForModelLoading();

		resourcesInEditingDomain10 = resourcesInEditingDomain10 - unloadedResources10.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Verify that unloaded resources were removed from ResourceSet and proxified
		assertFalse(refWks.editingDomain10.getResourceSet().getResources().contains(hbResource10_1));
		assertFalse(refWks.editingDomain10.getResourceSet().getResources().contains(hbResource10_2));
		assertTrue(refWks.editingDomain10.getResourceSet().getResources().contains(hbResource10_3));

		assertNotNull(modelRoot10_1);
		assertTrue(modelRoot10_1.eIsProxy());

		assertNotNull(modelRoot10_2);
		assertTrue(modelRoot10_2.eIsProxy());

		// Unload AllResource of uml2EditingDomain
		Collection<Resource> unloadedUml2Resources = new ArrayList<Resource>();
		Resource uml2Resource_1 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		Resource uml2Resource_2 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2, true), false);
		Resource uml2Resource_3 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3, true), false);
		assertNotNull(uml2Resource_1);
		assertNotNull(uml2Resource_2);
		assertNotNull(uml2Resource_3);
		ExtendedResource extendedUml2Resource_1 = ExtendedResourceAdapterFactory.INSTANCE.adapt(uml2Resource_1);
		ExtendedResource extendedUml2Resource_2 = ExtendedResourceAdapterFactory.INSTANCE.adapt(uml2Resource_2);
		ExtendedResource extendedUml2Resource_3 = ExtendedResourceAdapterFactory.INSTANCE.adapt(uml2Resource_3);
		assertTrue(extendedUml2Resource_1 instanceof ExtendedResourceAdapter);
		assertTrue(extendedUml2Resource_2 instanceof ExtendedResourceAdapter);
		assertTrue(extendedUml2Resource_3 instanceof ExtendedResourceAdapter);

		unloadedUml2Resources.add(uml2Resource_1);
		unloadedUml2Resources.add(uml2Resource_2);
		// Verify tested resource were loaded
		resource = refWks.editingDomainUml2.getResourceSet().getResource(uml2Resource_1.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRootUml2_1 = resource.getContents().get(0);
		assertNotNull(modelRootUml2_1);
		assertFalse(modelRootUml2_1.eIsProxy());

		resource = refWks.editingDomainUml2.getResourceSet().getResource(uml2Resource_2.getURI(), false);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRootUml2_2 = resource.getContents().get(0);
		assertNotNull(modelRootUml2_2);
		assertFalse(modelRootUml2_2.eIsProxy());

		// Unload uml2Resource with EditingDomain20
		EcorePlatformUtil.unloadResources(refWks.editingDomain20, unloadedUml2Resources, true, new NullProgressMonitor());
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Unload uml2Resource with uml2EditingDomain
		EcorePlatformUtil.unloadResources(refWks.editingDomainUml2, unloadedUml2Resources, true, new NullProgressMonitor());
		waitForModelLoading();

		resourcesInEditingDomainUml2 = resourcesInEditingDomainUml2 - unloadedUml2Resources.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Verify unloaded resource were removed from the ResourceSet and not proxified
		assertFalse(refWks.editingDomainUml2.getResourceSet().getResources().contains(uml2Resource_1));
		assertFalse(refWks.editingDomainUml2.getResourceSet().getResources().contains(uml2Resource_2));
		assertTrue(refWks.editingDomainUml2.getResourceSet().getResources().contains(uml2Resource_3));
		// the Uml resource should be proxifed
		assertNotNull(modelRootUml2_1);
		assertTrue(modelRootUml2_1.eIsProxy());
		assertTrue(modelRootUml2_1.eContents().size() > 0);

		assertNotNull(modelRootUml2_2);
		assertTrue(modelRootUml2_2.eIsProxy());
		assertTrue(modelRootUml2_2.eContents().size() > 0);
	}

	/**
	 * Test method for
	 * {@link EcorePlatformUtil#unloadAllResources(org.eclipse.emf.transaction.TransactionalEditingDomain)}
	 */
	public void testUnloadAllResources() throws Exception {
		int resourcesInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		int resourcesInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		int resourcesInEditingDomainUml2 = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);

		// NULL editing domain
		EcorePlatformUtil.unloadAllResources(null, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);
		// Unload AllResource of EditingDomain20
		EcorePlatformUtil.unloadAllResources(refWks.editingDomain20, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Unload AllResource of EditingDomain10
		EcorePlatformUtil.unloadAllResources(refWks.editingDomain10, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Unload AllResource of uml2EditingDomain
		EcorePlatformUtil.unloadAllResources(refWks.editingDomainUml2, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);
	}

	/**
	 * Test method for {@link EcorePlatformUtil#createSaveNewSchedulingRule(Collection)}
	 */
	public void testCreateSaveNewSchedulingRule_Collection_ModelResourceDescriptors() throws Exception {
		// Since Project is automatically loaded during import we need first to unload it.
		ModelLoadManager.INSTANCE.unloadProject(refWks.hbProject20_A, false, true, new NullProgressMonitor());
		waitForModelLoading();

		ModelLoadManager.INSTANCE.unloadProject(refWks.hbProject20_D, false, true, new NullProgressMonitor());
		waitForModelLoading();

		ModelLoadManager.INSTANCE.unloadProject(refWks.hbProject20_E, false, true, new NullProgressMonitor());
		waitForModelLoading();

		assertTrue(refWks.editingDomain20.getResourceSet().getResources().isEmpty());

		String testFileName1 = "testsave3x_1.instancemodel";
		String testFileName2 = "testsave3x_2.instancemodel";

		IPath path_1 = refWks.hbProject20_A.getFullPath().append(testFileName1);
		IPath path_2 = refWks.hbProject20_A.getFullPath().append(testFileName2);

		ModelResourceDescriptor modelResDes1 = new ModelResourceDescriptor(path_1, Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(),
				createPlatform());
		ModelResourceDescriptor modelResDes2 = new ModelResourceDescriptor(path_2, Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(),
				createPlatform());

		IFile file1 = refWks.hbProject20_A.getFile(path_1);
		IFile file2 = refWks.hbProject20_A.getFile(path_2);
		IResourceRuleFactory ruleFactory = ((IResource) file1).getWorkspace().getRuleFactory();

		ISchedulingRule schedulingRule1 = ruleFactory.createRule(file1);
		ISchedulingRule schedulingRule2 = ruleFactory.createRule(file2);

		// Create new scheduling rules for collection of model resource descriptors
		Collection<ModelResourceDescriptor> modelDescriptors = new ArrayList<ModelResourceDescriptor>();
		modelDescriptors.add(modelResDes1);
		modelDescriptors.add(modelResDes2);

		ISchedulingRule schedulingRules = EcorePlatformUtil.createSaveNewSchedulingRule(modelDescriptors);
		assertNotNull(schedulingRules);
		assertTrue(schedulingRules.contains(schedulingRule1));
		assertTrue(schedulingRules.contains(schedulingRule2));
	}

	/**
	 * Test method for {@link EcorePlatformUtil#createSaveSchedulingRule(Collection)}
	 */
	public void testCreateSaveSchedulingRule_CollectionOfResource() throws Exception {
		Resource hbResource20_1 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		Resource hbResource20_2 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);
		Resource hbResource20_3 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, true), false);
		Resource hbResource20_4 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		Resource hbResource20_5 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, true), false);
		Resource hbResource20_6 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, true), false);
		assertNotNull(hbResource20_1);
		assertNotNull(hbResource20_2);
		assertNotNull(hbResource20_3);
		assertNotNull(hbResource20_4);
		assertNotNull(hbResource20_5);
		assertNotNull(hbResource20_6);

		IFile file1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		IFile file2 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		IFile file3 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);

		IFile file4 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile file5 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile file6 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);

		assertNotNull(file1);
		assertNotNull(file2);
		assertNotNull(file3);

		assertNotNull(file4);
		assertNotNull(file5);
		assertNotNull(file6);

		IResourceRuleFactory ruleFactory = ((IResource) file1).getWorkspace().getRuleFactory();

		ISchedulingRule schedulingRule1 = ruleFactory.createRule(file1);
		ISchedulingRule schedulingRule2 = ruleFactory.createRule(file2);
		ISchedulingRule schedulingRule3 = ruleFactory.createRule(file3);

		ISchedulingRule schedulingRule4 = ruleFactory.createRule(file4);
		ISchedulingRule schedulingRule5 = ruleFactory.createRule(file5);
		ISchedulingRule schedulingRule6 = ruleFactory.createRule(file6);

		// Create new scheduling rules for collection of model resource descriptors
		Collection<Resource> resources = new ArrayList<Resource>();
		resources.add(hbResource20_1);
		resources.add(hbResource20_2);
		resources.add(hbResource20_3);
		resources.add(hbResource20_4);
		resources.add(hbResource20_5);
		resources.add(hbResource20_6);

		ISchedulingRule schedulingRules = EcorePlatformUtil.createSaveSchedulingRule(resources);
		assertNotNull(schedulingRules);
		assertTrue(schedulingRules.contains(schedulingRule1));
		assertTrue(schedulingRules.contains(schedulingRule2));
		assertTrue(schedulingRules.contains(schedulingRule3));
		assertTrue(schedulingRules.contains(schedulingRule4));
		assertTrue(schedulingRules.contains(schedulingRule5));
		assertTrue(schedulingRules.contains(schedulingRule6));
	}

	/**
	 * Test method for {@link EcorePlatformUtil#createSaveSchedulingRule(Resource))}
	 */
	public void testCreateSaveSchedulingRule_Resource() throws Exception {
		Resource hbResource20_1 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		assertNotNull(hbResource20_1);

		IFile file1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertNotNull(file1);
		IResourceRuleFactory ruleFactory = ((IResource) file1).getWorkspace().getRuleFactory();
		ISchedulingRule schedulingRule1 = ruleFactory.createRule(file1);

		// Create new scheduling rules for collection of model resource descriptors
		ISchedulingRule schedulingRules = EcorePlatformUtil.createSaveSchedulingRule(hbResource20_1);
		assertNotNull(schedulingRules);
		assertTrue(schedulingRules.contains(schedulingRule1));
	}

	public void testReadComments() {
		IFile referenceFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		Collection<String> comments = EcorePlatformUtil.readRootElementComments(referenceFile);
		assertNotNull(comments);
		assertEquals(3, comments.size());
		for (String comment : comments) {
			assertTrue(comment.length() > 0);
		}
	}

}

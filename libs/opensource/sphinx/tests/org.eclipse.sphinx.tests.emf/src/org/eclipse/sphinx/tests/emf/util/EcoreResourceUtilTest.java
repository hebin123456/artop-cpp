/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [418005] Add support for model files with multiple root elements
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.util;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.sphinx.emf.resource.ScopingResourceSetImpl;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.util.Hummingbird20ResourceFactoryImpl;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.tests.emf.internal.Activator;
import org.eclipse.sphinx.testutils.AbstractTestCase;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;
import org.xml.sax.SAXParseException;

@SuppressWarnings({ "nls", "restriction" })
public class EcoreResourceUtilTest extends AbstractTestCase {

	@Override
	protected Plugin getTestPlugin() {
		return Activator.getPlugin();
	}

	/**
	 * Test method for {@link EcoreResourceUtil#getURIConverter(ResourceSet resourceSet)} .
	 */
	public void testGetURIConverter() {
		ResourceSet newResourceSet = new ScopingResourceSetImpl();
		URIConverter resourceSetUriConverterExpected = newResourceSet.getURIConverter();
		assertNotNull(resourceSetUriConverterExpected);

		URIConverter resourceSetUriConverterRetrieved = EcoreResourceUtil.getURIConverter(newResourceSet);
		assertNotNull(resourceSetUriConverterRetrieved);
		assertSame(resourceSetUriConverterExpected, resourceSetUriConverterRetrieved);

		assertNotNull(EcoreResourceUtil.getURIConverter(null));
	}

	/**
	 * Test method for {@link EcoreResourceUtil#readModelNamespace(ResourceSet resourceSet,URI modelURI)} .
	 * {@link EcoreResourceUtil#readModelNamespace(Resource)}
	 *
	 * @throws Exception
	 */
	public void testReadModelNamespace() throws Exception {
		String namespace20_1 = "http://www.eclipse.org/sphinx/examples/hummingbird/2.0.0/instancemodel";
		String namespace21_1 = "http://www.eclipse.org/sphinx/examples/hummingbird/2.0.1/instancemodel";
		String namespace20_2 = "http://www.eclipse.org/sphinx/examples/hummingbird/2.0.0/typemodel";
		String namespace21_2 = "http://www.eclipse.org/sphinx/examples/hummingbird/2.0.1/typemodel";
		String namespace10 = "http://www.eclipse.org/sphinx/examples/hummingbird/1.0.0";
		String namespaceUml2 = UML2MMDescriptor.BASE_NAMESPACE + "/2.1.0/UML";

		final String hbFile20_1 = "ModelNamespace/hbFile20_1.instancemodel";
		final String hbFile20_2 = "ModelNamespace/hbFile20_2.typemodel";
		final String hbFile20_3 = "ModelNamespace/hbFile20_3.instancemodel";
		final String hbFile20_4 = "ModelNamespace/hbFile20_4.typemodel";

		final String hbFile10 = "hbFile10.hummingbird";

		final String uml2File = "uml2File.uml";

		Hummingbird20ResourceFactoryImpl hb20ResourceFactory = new Hummingbird20ResourceFactoryImpl();
		XMIResourceFactoryImpl xmiResourceFactoryImpl = new XMIResourceFactoryImpl();

		// Read Model Namespace from Resource
		// HB Resource
		EObject modelRoot = loadInputFile(hbFile20_1, hb20ResourceFactory, null);
		assertNotNull(modelRoot);
		Resource resource21_instancemodel = modelRoot.eResource();
		assertNotNull(resource21_instancemodel);
		assertEquals(namespace21_1, EcoreResourceUtil.readModelNamespace(resource21_instancemodel));

		modelRoot = loadInputFile(hbFile20_2, hb20ResourceFactory, null);
		assertNotNull(modelRoot);
		Resource resource21_typemodel = modelRoot.eResource();
		assertEquals(namespace21_2, EcoreResourceUtil.readModelNamespace(resource21_typemodel));

		modelRoot = loadInputFile(hbFile20_3, hb20ResourceFactory, null);
		assertNotNull(modelRoot);
		Resource resource20_instancemodel = modelRoot.eResource();
		assertNotNull(resource20_instancemodel);
		assertEquals(namespace20_1, EcoreResourceUtil.readModelNamespace(resource20_instancemodel));

		modelRoot = loadInputFile(hbFile20_4, hb20ResourceFactory, null);
		assertNotNull(modelRoot);
		Resource resource20_typemodel = modelRoot.eResource();
		assertNotNull(resource20_typemodel);
		assertEquals(namespace20_2, EcoreResourceUtil.readModelNamespace(resource20_typemodel));

		modelRoot = loadInputFile(hbFile10, xmiResourceFactoryImpl, null);
		assertNotNull(modelRoot);
		Resource resource10 = modelRoot.eResource();
		assertNotNull(resource10);
		String readModelNamespace10 = EcoreResourceUtil.readModelNamespace(resource10);
		assertEquals(namespace10, readModelNamespace10);

		// UML resource
		modelRoot = loadInputFile(uml2File, new UMLResourceFactoryImpl(), null);
		assertNotNull(modelRoot);
		Resource resourceUml2 = modelRoot.eResource();
		assertNotNull(resourceUml2);
		String readModelNamespaceUml2 = EcoreResourceUtil.readModelNamespace(resourceUml2);
		assertEquals(namespaceUml2, readModelNamespaceUml2);

	}

	/**
	 * Test method for {@link EcoreResourceUtil#getModelName(Notifier modelRoot)} .
	 *
	 * @throws Exception
	 */
	public void testGetModelName() throws Exception {

		final String hbFile20_1 = "ModelNamespace/hbFile20_1.instancemodel";
		final String hbFile20_2 = "ModelNamespace/hbFile20_2.typemodel";
		final String uml2File = "uml2File.uml";
		final String hbFile10 = "hbFile10.hummingbird";

		UMLResourceFactoryImpl umlResourceFactory = new UMLResourceFactoryImpl();

		Hummingbird20ResourceFactoryImpl hb20ResourceFactory = new Hummingbird20ResourceFactoryImpl();
		XMIResourceFactoryImpl xmiResourceFactoryImpl = new XMIResourceFactoryImpl();
		// HB20 Model
		{
			// Read Model Namespace from Resource
			// HB Resource
			EObject modelRoot = loadInputFile(hbFile20_1, hb20ResourceFactory, null);
			assertNotNull(modelRoot);
			assertTrue(EcoreResourceUtil.getModelName(modelRoot), EcoreResourceUtil.getModelName(modelRoot).equalsIgnoreCase("InstanceModel"));

			modelRoot = loadInputFile(hbFile20_2, hb20ResourceFactory, null);
			assertNotNull(modelRoot);
			assertTrue(EcoreResourceUtil.getModelName(modelRoot), EcoreResourceUtil.getModelName(modelRoot).equalsIgnoreCase("TypeModel"));

		}
		// HB10 Model
		{
			EObject modelRoot10 = loadInputFile(hbFile10, xmiResourceFactoryImpl, null);
			assertNotNull(modelRoot10);
			assertNotNull(modelRoot10.eResource());
			assertTrue(EcoreResourceUtil.getModelName(modelRoot10), EcoreResourceUtil.getModelName(modelRoot10).equalsIgnoreCase("Hummingbird10"));

			assertFalse(modelRoot10.eResource().getContents().isEmpty());
			EObject contextObject = modelRoot10.eResource().getContents().get(0);
			assertNotNull(contextObject);

			assertTrue(EcoreResourceUtil.getModelName(contextObject),
					EcoreResourceUtil.getModelName(contextObject).equalsIgnoreCase("Hummingbird10"));
		}

		// Uml2 Model
		{
			EObject modelRootUml2 = loadInputFile(uml2File, umlResourceFactory, null);
			assertNotNull(modelRootUml2);
			assertNotNull(modelRootUml2.eResource());
			assertTrue(EcoreResourceUtil.getModelName(modelRootUml2).equalsIgnoreCase("Uml"));
			assertTrue(EcoreResourceUtil.getModelName(modelRootUml2.eResource()).equalsIgnoreCase("UML"));
		}
		// NULL input
		try {
			Application nullApp = null;
			assertEquals("", EcoreResourceUtil.getModelName(nullApp));

			Resource nullResource = null;
			assertEquals("", EcoreResourceUtil.getModelName(nullResource));
		} catch (Exception ex) {
			fail("Exception while input resource is NULL: " + ex.getCause() + " " + ex.getLocalizedMessage());
		}
	}

	/**
	 * Test method for {@link EcoreResourceUtil#validate(URI, URL)} .
	 */
	public void testValidate() throws Exception {
		java.net.URI validXMLURI = getTestFileAccessor().getInputFileURI("valid_exampleModel.xml");
		URI xmlEMFURI = getTestFileAccessor().convertToEMFURI(validXMLURI);

		java.net.URI invalidXMLURI = getTestFileAccessor().getInputFileURI("invalid_exampleModel.xml");
		URI invalidXmlEMFURI = getTestFileAccessor().convertToEMFURI(invalidXMLURI);

		URL schemaURL = getTestFileAccessor().getInputFileURI("exampleSchema.xsd", true).toURL();

		try {
			EcoreResourceUtil.validate(xmlEMFURI, schemaURL);

		} catch (Exception ex) {
			fail(ex.getClass().getName() + " " + ex.getMessage());
		}
		try {
			EcoreResourceUtil.validate(invalidXmlEMFURI, schemaURL);
		} catch (Exception ex) {
			if (!(ex instanceof SAXParseException)) {
				ex.printStackTrace();
				fail(ex.getMessage());
			}
		}
	}

	/**
	 * Test method for {@link EcoreResourceUtil#readTargetNamespace(Resource)}
	 *
	 * @throws Exception
	 */
	public void testReadTargetNamespaceFromResource() throws Exception {
		String expectedTargetNamespace_hb20InstanceModel = "http://www.eclipse.org/sphinx/examples/hummingbird/2.0.1/instancemodel";
		String expectedTargetNamespace_hb20TypeModel = "http://www.eclipse.org/sphinx/examples/hummingbird/2.0.1/typemodel";

		final String hbFile20_1 = "ModelNamespace/hbFile20_1.instancemodel";
		final String hbFile20_2 = "ModelNamespace/hbFile20_2.typemodel";
		final String hbFile10 = "hbFile10.hummingbird";

		Hummingbird20ResourceFactoryImpl hb20ResourceFactory = new Hummingbird20ResourceFactoryImpl();
		XMIResourceFactoryImpl xmiResourceFactoryImpl = new XMIResourceFactoryImpl();
		UMLResourceFactoryImpl umlResourceFactory = new UMLResourceFactoryImpl();

		// String expectedTargetHBNameSpace = "http://www.omg.org/XMI";

		// Resource is NULL
		assertEquals(null, EcoreResourceUtil.readTargetNamespace(null));
		// =========================================
		// Uml2 Resource
		EObject modelRootUml2 = loadInputFile("uml2File.uml", umlResourceFactory, null);
		assertNotNull(modelRootUml2);
		Resource resourceUml2 = modelRootUml2.eResource();
		assertNotNull(resourceUml2);

		assertFalse(resourceUml2.getContents().isEmpty());
		EObject retrievedModelRootUml2 = resourceUml2.getContents().get(0);
		assertNotNull(retrievedModelRootUml2);
		assertSame(modelRootUml2, retrievedModelRootUml2);
		// XML Resource
		// -----Read Target Namespace from HB10 Resource

		EObject modelRoot10 = loadInputFile(hbFile10, xmiResourceFactoryImpl, null);
		assertNotNull(modelRoot10);
		Resource resource10 = modelRoot10.eResource();
		assertNotNull(resource10);
		assertNull(EcoreResourceUtil.readTargetNamespace(resource10));

		// -----Read Target Namespace from HB20 Resource
		EObject modelRoot = loadInputFile(hbFile20_1, hb20ResourceFactory, null);
		assertNotNull(modelRoot);
		Resource resource20_1 = modelRoot.eResource();
		assertNotNull(resource20_1);

		modelRoot = loadInputFile(hbFile20_2, hb20ResourceFactory, null);
		assertNotNull(modelRoot);
		Resource resource20_2 = modelRoot.eResource();
		assertNotNull(resource20_2);

		assertNull(EcoreResourceUtil.readTargetNamespace(resource20_1));
		assertNull(EcoreResourceUtil.readTargetNamespace(resource20_2));

		// =====================================================
		// ----Read TargetNamspace from HB20 xsd file
		URL schemaURLHB20_InstanceModel = FileLocator.find(org.eclipse.sphinx.examples.hummingbird20.Activator.getPlugin().getBundle(),
				new Path("model/InstanceModel20XMI.xsd"), null);
		URL schemaURLHB20_TypeModel = FileLocator.find(org.eclipse.sphinx.examples.hummingbird20.Activator.getPlugin().getBundle(),
				new Path("model/TypeModel20XMI.xsd"), null);

		ResourceSet testResourceSet = new ResourceSetImpl();

		Resource testResource_1 = testResourceSet.createResource(URI.createURI(schemaURLHB20_InstanceModel.toString(), true));
		Resource testResource_2 = testResourceSet.createResource(URI.createURI(schemaURLHB20_TypeModel.toString(), true));

		assertEquals(expectedTargetNamespace_hb20InstanceModel, EcoreResourceUtil.readTargetNamespace(testResource_1));
		assertEquals(expectedTargetNamespace_hb20TypeModel, EcoreResourceUtil.readTargetNamespace(testResource_2));
	}

	/**
	 * Test method for {@link EcoreResourceUtil#convertToPlatformResourceURI(URI)}
	 */
	public void testConvertToPlatformResourceURI() throws Exception {

		String testURI = "/hbProject20_1/hbFile20_20a_1.modelinstance";
		URI platformResourceURI = URI.createPlatformResourceURI(testURI, true);
		// =====================================================
		// The given URI is already a Platform resource URI ;
		assertEquals(platformResourceURI, EcoreResourceUtil.convertToPlatformResourceURI(platformResourceURI));

		// =====================================================
		File file = File.createTempFile("test", null);
		String outsideWorkspaceLocation = file.getAbsolutePath();
		URI outsideWorkspaceURI = URI.createFileURI(outsideWorkspaceLocation);
		assertEquals(outsideWorkspaceURI, EcoreResourceUtil.convertToPlatformResourceURI(outsideWorkspaceURI));
		// =====================================================
		// The given URI is already a Platform plugin URI
		URI platformPluginURI = URI
				.createPlatformPluginURI(Activator.getPlugin().getBundle().getSymbolicName() + "resources/input/hbFile20.instancemodel", true);
		assertEquals(platformPluginURI, EcoreResourceUtil.convertToPlatformResourceURI(platformPluginURI));
		// =====================================================
		// The given URI is not a Platform Resource URI
		URI randomURI = URI.createURI(testURI, true);
		assertEquals(platformResourceURI, EcoreResourceUtil.convertToPlatformResourceURI(randomURI));
	}

	/**
	 * Test method for {@link EcoreResourceUtil#readSchemaLocationEntries(Resource)}
	 */
	public void testReadSchemaLocation() throws Exception {
		// Resource is NULL
		assertNotNull(EcoreResourceUtil.readSchemaLocationEntries(null));
		assertTrue(EcoreResourceUtil.readSchemaLocationEntries(null).isEmpty());

		// Read Model Name space from Resource
		// HB Resource
		Hummingbird20ResourceFactoryImpl hb20ResourceFactory = new Hummingbird20ResourceFactoryImpl();
		EObject modelRoot = loadInputFile("hbFile20.instancemodel", hb20ResourceFactory, null);
		assertNotNull(modelRoot);
		Resource resource20_withSchema = modelRoot.eResource();

		XMIResourceFactoryImpl hb10ResourceFactory = new XMIResourceFactoryImpl();
		modelRoot = loadInputFile("hbFile10.hummingbird", hb10ResourceFactory, null);
		assertNotNull(modelRoot);
		Resource resource10_withoutSchema = modelRoot.eResource();

		assertNotNull(resource10_withoutSchema);
		assertNotNull(resource20_withSchema);

		// =====================================================
		// Resource with SchemaLocation - Result: NOT NULL
		assertEquals(1, EcoreResourceUtil.readSchemaLocationEntries(resource20_withSchema).size());
		assertEquals("InstanceModel20XMI.xsd",
				EcoreResourceUtil.readSchemaLocationEntries(resource20_withSchema).get(InstanceModel20Package.eNS_URI));
		// =====================================================
		// Resource without SchemaLocation- Result: NULL
		assertEquals(0, EcoreResourceUtil.readSchemaLocationEntries(resource10_withoutSchema).size());
	}

	/**
	 * Test method for {@link EcoreResourceUtil#getResourceContents(Resource)}
	 *
	 * @throws Exception
	 */
	public void testGetResourceContents() throws Exception {
		Hummingbird20ResourceFactoryImpl hb20ResourceFactory = new Hummingbird20ResourceFactoryImpl();
		XMIResourceFactoryImpl xmiResourceFactoryImpl = new XMIResourceFactoryImpl();
		UMLResourceFactoryImpl umlResourceFactory = new UMLResourceFactoryImpl();

		// Hummingbird 20 Resource
		EObject modelRoot20 = loadInputFile("hbFile20.instancemodel", hb20ResourceFactory, null);
		assertNotNull(modelRoot20);
		Resource resource20 = modelRoot20.eResource();
		assertNotNull(resource20);
		assertSame(resource20.getContents(), EcoreResourceUtil.getResourceContents(resource20));
		// Hummingbird 10 Resource
		EObject modelRoot10 = loadInputFile("hbFile10.hummingbird", xmiResourceFactoryImpl, null);
		assertNotNull(modelRoot10);
		Resource resource10 = modelRoot10.eResource();
		assertNotNull(resource10);
		assertSame(resource10.getContents(), EcoreResourceUtil.getResourceContents(resource10));
		// =========================================
		// Uml2 Resource
		EObject modelRootUml2 = loadInputFile("uml2File.uml", umlResourceFactory, null);
		assertNotNull(modelRootUml2);
		Resource resourceUml2 = modelRootUml2.eResource();
		assertNotNull(resourceUml2);
		assertSame(resourceUml2.getContents(), EcoreResourceUtil.getResourceContents(resourceUml2));
		// =========================================
		// Input is NULL
		assertNotNull(EcoreResourceUtil.getResourceContents(null));
		assertEquals(0, EcoreResourceUtil.getResourceContents(null).size());
	}

	public void testGetURI() throws Exception {
		Hummingbird20ResourceFactoryImpl hb20ResourceFactory = new Hummingbird20ResourceFactoryImpl();

		// Hummingbird 20 Resource
		EObject modelRoot20 = loadInputFile("hbFile20.instancemodel", hb20ResourceFactory, null);
		assertNotNull(modelRoot20);
		Resource resource20 = modelRoot20.eResource();
		assertNotNull(resource20);

		URI uri = EcoreResourceUtil.getURI(modelRoot20);
		assertNotNull(uri);
		assertEquals("hb:/#/", uri.toString());

		uri = EcoreResourceUtil.getURI(modelRoot20, true);
		assertNotNull(uri);
		assertEquals("platform:/plugin/org.eclipse.sphinx.tests.emf/resources/input/hbFile20.instancemodel#/", uri.toString());
	}
}

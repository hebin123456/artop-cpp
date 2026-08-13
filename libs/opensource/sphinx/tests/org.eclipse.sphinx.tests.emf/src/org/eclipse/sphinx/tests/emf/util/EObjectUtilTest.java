/**
 * <copyright>
 *
 * Copyright (c) 2008-2010 See4sys and others.
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
package org.eclipse.sphinx.tests.emf.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Factory;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Factory;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Package;
import org.eclipse.sphinx.examples.hummingbird20.common.Description;
import org.eclipse.sphinx.examples.hummingbird20.common.Identifiable;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.util.Hummingbird20ResourceFactoryImpl;
import org.eclipse.sphinx.tests.emf.internal.Activator;
import org.eclipse.sphinx.testutils.AbstractTestCase;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.UMLFactory;

@SuppressWarnings("nls")
public class EObjectUtilTest extends AbstractTestCase {

	/**
	 * Test method for {@link EObjectUtil# isAssignableFrom(EClass eClass, String typeName) } .
	 */
	public void testIsAssignableFrom() {
		/* Test data */
		Application hb20ModelRoot = InstanceModel20Factory.eINSTANCE.createApplication();
		Component hb20Component = InstanceModel20Factory.eINSTANCE.createComponent();

		org.eclipse.sphinx.examples.hummingbird10.Application hb10ModelRoot = Hummingbird10Factory.eINSTANCE.createApplication();
		org.eclipse.sphinx.examples.hummingbird10.Component hb10Component = Hummingbird10Factory.eINSTANCE.createComponent();

		Model uml2ModelRoot = UMLFactory.eINSTANCE.createModel();
		org.eclipse.uml2.uml.Package uml2Package = UMLFactory.eINSTANCE.createPackage();
		/* Tests cases execution */
		assertTrue("test " + hb20ModelRoot.eClass().getName() + " is not assignable from " + Application.class.getName(),
				EObjectUtil.isAssignableFrom(hb20ModelRoot.eClass(), Application.class.getName()));

		assertTrue("test " + hb20ModelRoot.eClass().getName() + " is not assignable from " + Identifiable.class.getName(),
				EObjectUtil.isAssignableFrom(hb20ModelRoot.eClass(), Identifiable.class.getName()));

		assertTrue("test " + hb20ModelRoot.eClass().getName() + " is not assignable from " + Identifiable.class.getName(),
				EObjectUtil.isAssignableFrom(hb20ModelRoot.eClass(), EObject.class.getName()));
		// --------
		assertTrue("test " + hb20Component.eClass().getName() + " is not assignable from " + Identifiable.class.getName(),
				EObjectUtil.isAssignableFrom(hb20Component.eClass(), Identifiable.class.getName()));

		assertTrue("test " + hb20Component.eClass().getName() + " is not assignable from " + Component.class.getName(),
				EObjectUtil.isAssignableFrom(hb20Component.eClass(), Component.class.getName()));

		assertTrue("test " + hb20Component.eClass().getName() + " is not assignable from " + EObject.class.getName(),
				EObjectUtil.isAssignableFrom(hb20Component.eClass(), EObject.class.getName()));

		assertTrue("test " + hb20ModelRoot.eClass().getName() + " is not ssignable from " + hb20Component.eClass().getESuperTypes().get(0).getName(),
				EObjectUtil.isAssignableFrom(hb20ModelRoot.eClass(), hb20Component.eClass().getESuperTypes().get(0).getName()));
		// --------
		assertFalse(
				"test " + hb20Component.eClass().getName() + " is assignable from "
						+ org.eclipse.sphinx.examples.hummingbird10.Component.class.getName(),
				EObjectUtil.isAssignableFrom(hb20Component.eClass(), org.eclipse.sphinx.examples.hummingbird10.Component.class.getName()));

		assertFalse("test " + hb20ModelRoot.eClass().getName() + " is assignable from " + hb10Component.eClass().getName(),
				EObjectUtil.isAssignableFrom(hb20ModelRoot.eClass(), hb10Component.eClass().getName()));
		// Cannot retrieved super class more than 1 level, except the case the 2 level higher class is EObject
		assertFalse("test " + hb20Component.eClass().getName() + " is assignable from " + Notifier.class.getName(),
				EObjectUtil.isAssignableFrom(hb20Component.eClass(), Notifier.class.getName()));

		// ==================================

		assertTrue(
				"test " + hb10ModelRoot.eClass().getName() + " is not assignable from "
						+ org.eclipse.sphinx.examples.hummingbird10.Application.class.getName(),
				EObjectUtil.isAssignableFrom(hb10ModelRoot.eClass(), org.eclipse.sphinx.examples.hummingbird10.Application.class.getName()));

		assertTrue("test " + hb10ModelRoot.eClass().getName() + " is not assignable from " + EObject.class.getName(),
				EObjectUtil.isAssignableFrom(hb10ModelRoot.eClass(), EObject.class.getName()));

		// --------------
		assertTrue(
				"test " + hb10Component.eClass().getName() + " is not assignable from "
						+ org.eclipse.sphinx.examples.hummingbird10.Component.class.getName(),
				EObjectUtil.isAssignableFrom(hb10Component.eClass(), org.eclipse.sphinx.examples.hummingbird10.Component.class.getName()));

		assertTrue("test " + hb10Component.eClass().getName() + " is not assignable from " + EObject.class.getName(),
				EObjectUtil.isAssignableFrom(hb10Component.eClass(), EObject.class.getName()));

		// ------------
		assertFalse("test " + hb10ModelRoot.eClass().getName() + " is assignable from " + Notifier.class.getName(),
				EObjectUtil.isAssignableFrom(hb10ModelRoot.eClass(), Notifier.class.getName()));

		assertFalse("test " + hb10Component.eClass().getName() + " is assignable from " + EObject.class.getName(),
				EObjectUtil.isAssignableFrom(hb10Component.eClass(), Notifier.class.getName()));

		assertFalse("test " + hb10ModelRoot.eClass().getName() + " is assignable from " + Identifiable.class.getName(),
				EObjectUtil.isAssignableFrom(hb10ModelRoot.eClass(), Identifiable.class.getName()));

		assertFalse("test " + hb10Component.eClass().getName() + " is assignable from " + Component.class.getName(),
				EObjectUtil.isAssignableFrom(hb10Component.eClass(), Component.class.getName()));

		assertFalse("test " + hb10ModelRoot.eClass().eClass().getName() + " is assignable from " + Application.class.getName(),
				EObjectUtil.isAssignableFrom(hb10ModelRoot.eClass(), Application.class.getName()));
		// ==================================

		assertTrue("test " + uml2ModelRoot.eClass().getName() + " is not assignable from " + Model.class.getName(),
				EObjectUtil.isAssignableFrom(uml2ModelRoot.eClass(), Model.class.getName()));

		assertTrue("test " + uml2ModelRoot.eClass().getName() + " is not assignable from " + NamedElement.class.getName(),
				EObjectUtil.isAssignableFrom(uml2ModelRoot.eClass(), NamedElement.class.getName()));
		assertTrue("test " + uml2ModelRoot.eClass().getName() + " is not assignable from " + EObject.class.getName(),
				EObjectUtil.isAssignableFrom(uml2ModelRoot.eClass(), EObject.class.getName()));

		assertTrue("test " + uml2Package.eClass().getName() + " is not assignable from " + org.eclipse.uml2.uml.Package.class.getName(),
				EObjectUtil.isAssignableFrom(uml2Package.eClass(), org.eclipse.uml2.uml.Package.class.getName()));
		assertTrue("test " + uml2Package.eClass().getName() + " is not assignable from " + PackageableElement.class.getName(),
				EObjectUtil.isAssignableFrom(uml2Package.eClass(), PackageableElement.class.getName()));
		assertTrue("test " + uml2Package.eClass().getName() + " is not assignable from " + EObject.class.getName(),
				EObjectUtil.isAssignableFrom(uml2Package.eClass(), EObject.class.getName()));
		assertTrue("test " + uml2Package.eClass().getName() + " is not assignable from " + EObject.class.getName(),
				EObjectUtil.isAssignableFrom(uml2Package.eClass(), EObject.class.getName()));

		assertTrue("test " + uml2Package.eClass().getName() + " is not assignable from " + Model.class.getName(),
				EObjectUtil.isAssignableFrom(uml2ModelRoot.eClass(), Model.class.getName()));
		// -----------------
		assertFalse("test " + uml2Package.eClass().getName() + " is assignable from " + Model.class.getName(),
				EObjectUtil.isAssignableFrom(uml2Package.eClass(), Model.class.getName()));
		assertFalse("test " + uml2Package.eClass().getName() + " is assignable from " + Application.class.getName(),
				EObjectUtil.isAssignableFrom(uml2ModelRoot.eClass(), Application.class.getName()));
		assertFalse("test " + uml2ModelRoot.eClass().getName() + " is assignable from " + Application.class.getName(),
				EObjectUtil.isAssignableFrom(uml2ModelRoot.eClass(), Application.class.getName()));
	}

	/**
	 * Test method for {@link EObjectUtil# getEStructuralFeature(Object object, String featureName)} .
	 */
	public void testGetEStructuralFeature() {
		/* Test variable settings */

		Platform hb20ModelRoot = TypeModel20Factory.eINSTANCE.createPlatform();
		ComponentType componentType = TypeModel20Factory.eINSTANCE.createComponentType();
		Application hb20Application = InstanceModel20Factory.eINSTANCE.createApplication();

		org.eclipse.sphinx.examples.hummingbird10.Application hb10ModelRoot = Hummingbird10Factory.eINSTANCE.createApplication();

		EStructuralFeature expectedStructuralFeature10_1 = hb10ModelRoot.eClass().getEStructuralFeature("components");
		EStructuralFeature expectedStructuralFeature10_2 = hb10ModelRoot.eClass().getEStructuralFeature("interfaces");

		EStructuralFeature expectedStructuralFeature20_1 = hb20ModelRoot.eClass().getEStructuralFeature("componentTypes");
		EStructuralFeature expectedStructuralFeature20_2 = hb20ModelRoot.eClass().getEStructuralFeature("interfaces");
		EStructuralFeature expectedStructuralFeature20_3 = componentType.eClass().getEStructuralFeature("ports");
		EStructuralFeature expectedStructuralFeature20_4 = hb20Application.eClass().getEStructuralFeature("components");

		/* Tests cases execution */
		assertSame(expectedStructuralFeature10_1, EObjectUtil.getEStructuralFeature(hb10ModelRoot, "components"));
		assertSame(expectedStructuralFeature10_2, EObjectUtil.getEStructuralFeature(hb10ModelRoot, "interfaces"));

		assertSame(expectedStructuralFeature20_1, EObjectUtil.getEStructuralFeature(hb20ModelRoot, "componentTypes"));
		assertSame(expectedStructuralFeature20_2, EObjectUtil.getEStructuralFeature(hb20ModelRoot, "interfaces"));
		assertSame(expectedStructuralFeature20_3, EObjectUtil.getEStructuralFeature(componentType, "ports"));

		assertNotSame(expectedStructuralFeature20_4, EObjectUtil.getEStructuralFeature(hb10ModelRoot, "components"));

		assertNotSame(expectedStructuralFeature10_2, EObjectUtil.getEStructuralFeature(hb20ModelRoot, "interfaces"));
	}

	/**
	 * Test method for {@link EObjectUtil#findEClassifier(org.eclipse.emf.ecore.EPackage, Class)} and
	 * {@link EObjectUtil#findEClassifier(org.eclipse.emf.ecore.EPackage, String)}
	 */
	public void testFindEClassifier() {

		// Test for EObjectUtil#findEClassifier(org.eclipse.emf.ecore.EPackage, Class)
		assertNotNull(EObjectUtil.findEClassifier(Hummingbird10Package.eINSTANCE, org.eclipse.sphinx.examples.hummingbird10.Application.class));
		assertNotNull(EObjectUtil.findEClassifier(Hummingbird10Package.eINSTANCE, org.eclipse.sphinx.examples.hummingbird10.Interface.class));
		assertNotNull(EObjectUtil.findEClassifier(Hummingbird10Package.eINSTANCE, org.eclipse.sphinx.examples.hummingbird10.Component.class));

		assertNotNull(EObjectUtil.findEClassifier(TypeModel20Package.eINSTANCE, ComponentType.class));
		assertNotNull(EObjectUtil.findEClassifier(TypeModel20Package.eINSTANCE, Interface.class));
		assertNotNull(EObjectUtil.findEClassifier(TypeModel20Package.eINSTANCE, Platform.class));

		assertNotNull(EObjectUtil.findEClassifier(InstanceModel20Package.eINSTANCE, Application.class));
		assertNotNull(EObjectUtil.findEClassifier(InstanceModel20Package.eINSTANCE, Component.class));
		assertNotNull(EObjectUtil.findEClassifier(InstanceModel20Package.eINSTANCE, ParameterValue.class));

		assertNull(EObjectUtil.findEClassifier(Hummingbird10Package.eINSTANCE, ComponentType.class));
		assertNotNull(EObjectUtil.findEClassifier(TypeModel20Package.eINSTANCE, org.eclipse.sphinx.examples.hummingbird10.Interface.class));

		// Test for EObjectUtil#findEClassifier(org.eclipse.emf.ecore.EPackage, String)
		assertNotNull(EObjectUtil.findEClassifier(Hummingbird10Package.eINSTANCE,
				org.eclipse.sphinx.examples.hummingbird10.Application.class.getSimpleName()));
		assertNotNull(EObjectUtil.findEClassifier(Hummingbird10Package.eINSTANCE,
				org.eclipse.sphinx.examples.hummingbird10.Interface.class.getSimpleName()));
		assertNotNull(EObjectUtil.findEClassifier(Hummingbird10Package.eINSTANCE,
				org.eclipse.sphinx.examples.hummingbird10.Component.class.getSimpleName()));

		assertNotNull(EObjectUtil.findEClassifier(TypeModel20Package.eINSTANCE, ComponentType.class.getSimpleName()));
		assertNotNull(EObjectUtil.findEClassifier(TypeModel20Package.eINSTANCE, Interface.class.getSimpleName()));
		assertNotNull(EObjectUtil.findEClassifier(TypeModel20Package.eINSTANCE, Platform.class.getSimpleName()));

		assertNotNull(EObjectUtil.findEClassifier(InstanceModel20Package.eINSTANCE, Application.class.getSimpleName()));
		assertNotNull(EObjectUtil.findEClassifier(InstanceModel20Package.eINSTANCE, Component.class.getSimpleName()));
		assertNotNull(EObjectUtil.findEClassifier(InstanceModel20Package.eINSTANCE,
				org.eclipse.sphinx.examples.hummingbird10.Component.class.getSimpleName()));
		assertNotNull(EObjectUtil.findEClassifier(InstanceModel20Package.eINSTANCE, ParameterValue.class.getSimpleName()));

		assertNotNull(EObjectUtil.findEClassifier(InstanceModel20Package.eINSTANCE, Component.class.getSimpleName()));
		assertNotNull(EObjectUtil.findEClassifier(Hummingbird10Package.eINSTANCE, Component.class.getSimpleName()));
		assertNotNull(EObjectUtil.findEClassifier(Hummingbird10Package.eINSTANCE, Application.class.getSimpleName()));
		assertNotNull(
				EObjectUtil.findEClassifier(TypeModel20Package.eINSTANCE, org.eclipse.sphinx.examples.hummingbird10.Interface.class.getSimpleName()));

		assertNull(EObjectUtil.findEClassifier(Hummingbird10Package.eINSTANCE, Platform.class.getSimpleName()));
		assertNull(EObjectUtil.findEClassifier(Hummingbird10Package.eINSTANCE, ComponentType.class.getSimpleName()));
		// Context: Given Package is NULL
		boolean flag = false;
		try {
			EObjectUtil.findEClassifier(null, Platform.class);
			EObjectUtil.findEClassifier(null, Platform.class.getSimpleName());
			Class<?> testClass = null;
			assertNull(EObjectUtil.findEClassifier(TypeModel20Package.eINSTANCE, testClass));
		} catch (AssertionFailedException e) {
			flag = true;
		}
		assertTrue(flag);

	}

	/**
	 * Test method for {@link EObjectUtil#findESubTypesOf(EClass, boolean)}
	 *
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void testFindESubTypeOf() throws InstantiationException, IllegalAccessException {
		Application app = InstanceModel20Factory.eINSTANCE.createApplication();
		Component component = InstanceModel20Factory.eINSTANCE.createComponent();
		ComponentType componentType = TypeModel20Factory.eINSTANCE.createComponentType();
		Interface interface1 = TypeModel20Factory.eINSTANCE.createInterface();
		Platform platform = TypeModel20Factory.eINSTANCE.createPlatform();
		Port port = TypeModel20Factory.eINSTANCE.createPort();
		Connection connection = InstanceModel20Factory.eINSTANCE.createConnection();
		Parameter parameter = TypeModel20Factory.eINSTANCE.createParameter();
		ParameterValue parameterValue = InstanceModel20Factory.eINSTANCE.createParameterValue();

		// Context concreteTypesOnly is FALSE
		EClass indefiableClass = Common20Package.eINSTANCE.getIdentifiable();
		assertNotNull(indefiableClass);
		try {
			List<EClass> subTypes = EObjectUtil.findESubTypesOf(indefiableClass, false);

			assertEquals(10, subTypes.size());
			assertTrue(subTypes.contains(app.eClass()));
			assertTrue(subTypes.contains(component.eClass()));
			assertTrue(subTypes.contains(componentType.eClass()));
			assertTrue(subTypes.contains(interface1.eClass()));
			assertTrue(subTypes.contains(platform.eClass()));
			assertTrue(subTypes.contains(port.eClass()));
			assertTrue(subTypes.contains(connection.eClass()));
			assertTrue(subTypes.contains(parameter.eClass()));
			assertTrue(subTypes.contains(parameterValue.eClass()));
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception:" + ex.getLocalizedMessage() + " ");
		}
	}

	/**
	 * Test method for {@link EObjectUtil#getEContainerClassifiers(EClass)}
	 */
	public void testGetEContainerClassifiers() {
		Application app = InstanceModel20Factory.eINSTANCE.createApplication();
		ComponentType componentType = TypeModel20Factory.eINSTANCE.createComponentType();
		Port port = TypeModel20Factory.eINSTANCE.createPort();

		List<EClassifier> testList = EObjectUtil.getEContainerClassifiers(app.eClass());
		assertEquals(0, testList.size());

		testList = EObjectUtil.getEContainerClassifiers(componentType.eClass());
		assertEquals(0, testList.size());

		testList = EObjectUtil.getEContainerClassifiers(port.eClass());
		assertEquals(1, testList.size());
		assertEquals(TypeModel20Package.eINSTANCE.getEClassifier("ComponentType"), testList.get(0));
		assertNotSame(Hummingbird10Package.eINSTANCE.getEClassifier("Component"), testList.get(0));

		// Input is NULL
		EClass nullEClass = null;
		try {
			testList = EObjectUtil.getEContainerClassifiers(nullEClass);

		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				fail("Exception when input is NULL" + ex.getLocalizedMessage());
			}
		}

	}

	/**
	 * Test method for
	 * {@link EObjectUtil#getAnnotatedEClassifiers(org.eclipse.emf.ecore.EPackage, String, String, String)}
	 */
	public void testGetAnnotatedEClassifiers() {
		Application hb20ModelRoot1 = InstanceModel20Factory.eINSTANCE.createApplication();

		List<EClassifier> testList = new ArrayList<EClassifier>();
		testList = EObjectUtil.getAnnotatedEClassifiers(hb20ModelRoot1.eClass().getEPackage(), ExtendedMetaData.ANNOTATION_URI, "kind", "mixed");
		assertEquals(2, testList.size());

		testList = EObjectUtil.getAnnotatedEClassifiers(hb20ModelRoot1.eClass().getEPackage(), ExtendedMetaData.ANNOTATION_URI, "Name", "mixed");
		assertTrue(testList.isEmpty());

		testList = EObjectUtil.getAnnotatedEClassifiers(hb20ModelRoot1.eClass().getEPackage(), ExtendedMetaData.ANNOTATION_URI, "kind", "mix");
		assertTrue(testList.isEmpty());

		testList = EObjectUtil.getAnnotatedEClassifiers(hb20ModelRoot1.eClass().getEPackage(), "ExtendedMetaData", "kind", "mix");
		assertTrue(testList.isEmpty());
		// Context: EPackage is NULL
		try {
			EObjectUtil.getAnnotatedEClassifiers(null, Hummingbird10Package.eNS_URI, "qualified", "true");
		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				fail("Exception when the given EPackage is NULL :" + ex.getMessage());
			}
		}
		// Given source is NULL
		try {
			testList = EObjectUtil.getAnnotatedEClassifiers(hb20ModelRoot1.eClass().getEPackage(), null, "kind", "mixed");
			assertTrue(testList.isEmpty());
		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				fail("Exception when the given source is NULL :" + ex.getMessage());
			}
		}

		// Context: detailKey is NULL
		try {
			testList = EObjectUtil.getAnnotatedEClassifiers(hb20ModelRoot1.eClass().getEPackage(), "ExtendedMetaData", null, "mixed");
			assertTrue(testList.isEmpty());
		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				fail("Exception when the given detail key is NULL :" + ex.getMessage());
			}
		}

		// Context: detailValue is NULL
		try {
			testList = EObjectUtil.getAnnotatedEClassifiers(hb20ModelRoot1.eClass().getEPackage(), "ExtendedMetaData", "kind", null);
			assertTrue(testList.isEmpty());
		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				fail("Exception when the given detailValue is NULL :" + ex.getMessage());
			}
		}

	}

	/**
	 * Test method for {@link EObjectUtil#getOrphans(EObject, EReference)}
	 */
	public void testGetOrphans() {
		/* Create test data */
		Platform platform = TypeModel20Factory.eINSTANCE.createPlatform();
		platform.setName("platform");

		Interface interface1 = TypeModel20Factory.eINSTANCE.createInterface();
		interface1.setName("interface1");

		Interface interface2 = TypeModel20Factory.eINSTANCE.createInterface();
		interface2.setName("interface2");

		Interface interface3 = TypeModel20Factory.eINSTANCE.createInterface();
		interface3.setName("interface3");

		ComponentType compType1 = TypeModel20Factory.eINSTANCE.createComponentType();
		compType1.setName("compType1");

		Port port1 = TypeModel20Factory.eINSTANCE.createPort();
		port1.setName("port1");
		port1.setMaxProviderCount(10);
		port1.setMinProviderCount(0);
		port1.setOwner(compType1);
		port1.setRequiredInterface(interface1);

		Port port2 = TypeModel20Factory.eINSTANCE.createPort();
		port2.setOwner(compType1);
		port2.setRequiredInterface(interface2);

		Port port3 = TypeModel20Factory.eINSTANCE.createPort();
		port3.setOwner(compType1);
		port3.setRequiredInterface(interface2);

		Port port4 = TypeModel20Factory.eINSTANCE.createPort();
		port4.setOwner(compType1);

		ComponentType compType2 = TypeModel20Factory.eINSTANCE.createComponentType();
		compType2.setName("compType2");

		ComponentType compType3 = TypeModel20Factory.eINSTANCE.createComponentType();
		compType3.setName("compType3");

		platform.getComponentTypes().add(compType1);
		platform.getComponentTypes().add(compType2);
		platform.getComponentTypes().add(compType3);
		platform.getInterfaces().add(interface1);
		platform.getInterfaces().add(interface3);
		interface2.getProvidingComponentTypes().add(compType2);

		Component component1 = InstanceModel20Factory.eINSTANCE.createComponent();
		component1.setName("Component1");
		component1.setType(compType1);

		Component component2 = InstanceModel20Factory.eINSTANCE.createComponent();
		component2.setName("Component2");
		component2.setType(compType2);

		Component component3 = InstanceModel20Factory.eINSTANCE.createComponent();
		component3.setName("Component3");
		component3.setType(compType3);
		/* Test cases */
		EReference ref_Platform_Components = TypeModel20Package.eINSTANCE.getPlatform_ComponentTypes();
		EReference ref_Platform_Interfaces = TypeModel20Package.eINSTANCE.getPlatform_Interfaces();
		EReference ref_ComponentType_Ports = TypeModel20Package.eINSTANCE.getComponentType_Ports();

		EReference ref_Component_ComponentType = InstanceModel20Package.eINSTANCE.getComponent_Type();
		EReference ref_Port_Interface = TypeModel20Package.eINSTANCE.getPort_RequiredInterface();

		assertNotNull(ref_ComponentType_Ports);
		assertNotNull(ref_Platform_Interfaces);
		assertNotNull(ref_ComponentType_Ports);
		assertNotNull(ref_Port_Interface);
		// ============================================
		// Target objects don't contain references which are containment and container

		List<EObject> testList = null;
		testList = EObjectUtil.getOrphans(compType1, ref_ComponentType_Ports);
		assertEquals(1, testList.size());
		testList.contains(port4);
		// =====================================
		// --------------
		// Target objects contain references which are containment or container
		// reference to container is set and containing containment
		testList = EObjectUtil.getOrphans(component1, ref_Component_ComponentType);
		// assertEquals(0, testList.size());
		// reference to container is set
		testList = EObjectUtil.getOrphans(component2, ref_Component_ComponentType);
		assertEquals(0, testList.size());
		// reference to container is not set, doesn't contain containments
		testList = EObjectUtil.getOrphans(component3, ref_Component_ComponentType);
		assertEquals(1, testList.size());

		// -------------
		testList = EObjectUtil.getOrphans(platform, ref_Platform_Components);
		assertEquals(2, testList.size());
		assertTrue(testList.contains(compType1));
		assertTrue(testList.contains(compType3));

		// add interface2 to platfrom
		platform.getInterfaces().add(interface2);
		testList = EObjectUtil.getOrphans(platform, ref_Platform_Interfaces);
		assertEquals(1, testList.size());
		assertTrue(testList.contains(interface3));
		// Context: owner is NULL
		boolean flag = true;
		String message = "";
		try {
			testList = EObjectUtil.getOrphans(null, ref_Component_ComponentType);
		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				message = ex.getMessage();
				flag = false;
			}
		}
		assertTrue("Exception when the given owner is NULL" + message, flag);
		// Context: reference is NULL
		flag = true;
		try {
			testList = EObjectUtil.getOrphans(platform, null);
		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				message = ex.getMessage();
				flag = false;
			}
		}
		assertTrue("Exception when the given reference is NULL" + message, flag);
	}

	/**
	 * Test method for {@link EObjectUtil#getMixedText(org.eclipse.emf.ecore.util.FeatureMap)}
	 *
	 * @throws Exception
	 */
	public void testGetMixedText() throws Exception {
		Application hb20Application = (Application) loadInputFile("hbFile20.instancemodel", new Hummingbird20ResourceFactoryImpl(), null);
		assertNotNull(hb20Application);
		Description description = hb20Application.getDescription();
		assertNotNull(description);

		String mixedText = EObjectUtil.getMixedText(description.getMixed());
		assertEquals("DescriptionText", mixedText);
	}

	/**
	 * Test method for {@link EObjectUtil#setMixedText(FeatureMap, String)}
	 *
	 * @throws Exception
	 */
	public void testSetMixedText() throws Exception {
		String workingFileName = "hbFile20.instancemodel";
		String hb20ApplicationName = "MyApp";
		String newText = "NewDescriptionText";

		Application hb20Application = InstanceModel20Factory.eINSTANCE.createApplication();
		hb20Application.setName(hb20ApplicationName);
		Description description = Common20Factory.eINSTANCE.createDescription();
		EObjectUtil.setMixedText(description.getMixed(), newText);
		assertEquals(newText, EObjectUtil.getMixedText(description.getMixed()));
		hb20Application.setDescription(description);

		saveWorkingFile(workingFileName, hb20Application, new Hummingbird20ResourceFactoryImpl(), null);

		Application savedHb20Application = (Application) loadWorkingFile(workingFileName, new Hummingbird20ResourceFactoryImpl(), null);
		assertNotNull(savedHb20Application);
		assertEquals(hb20ApplicationName, savedHb20Application.getName());
		Description savedDescription = savedHb20Application.getDescription();
		assertNotNull(savedDescription);

		assertEquals(newText, EObjectUtil.getMixedText(savedDescription.getMixed()));
	}

	@Override
	protected Plugin getTestPlugin() {
		return Activator.getPlugin();
	}
}
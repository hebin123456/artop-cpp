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
 *     itemis - [409510] Enable resource scope-sensitive proxy resolutions without forcing metamodel implementations to subclass EObjectImpl
 *     Siemens - [574930] Model load manager extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.platform.util;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.validation.internal.util.DisabledConstraintStatus;
import org.eclipse.emf.validation.model.ConstraintStatus;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.resource.ScopingResourceSetImpl;
import org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PlatformImpl;
import org.eclipse.sphinx.platform.stats.PerformanceStatsLog;
import org.eclipse.sphinx.platform.util.ReflectUtil;

import junit.framework.TestCase;

@SuppressWarnings({ "nls", "restriction" })
public class ReflectUtilTest extends TestCase {

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

	/**
	 * Test method for (@link {@link ReflectUtil#getSimplePackageName(String)}
	 */
	public void testGetSimplePackageName() {

		String testQualifiedPackageName = "org.eclipse.sphinx.tests.platform.util";
		String simplePackageName = "util";

		assertEquals(simplePackageName, ReflectUtil.getSimplePackageName(testQualifiedPackageName));

	}

	/**
	 * Test method for {@link ReflectUtil#getSuperPackageName(String)}
	 */
	public void testGetSuperPackageName() {
		String testQualifiedPackageName = "org.eclipse.sphinx.tests.platform.util";
		String superPackageName = "org.eclipse.sphinx.tests.platform";

		assertEquals(superPackageName, ReflectUtil.getSuperPackageName(testQualifiedPackageName));
	}

	/**
	 * Test method for{@link ReflectUtil#isAssignableFrom(Class, String)}
	 */
	public void testIsAssignableFrom() {
		assertTrue(ReflectUtil.isAssignableFrom(int.class, "int"));
		assertFalse(ReflectUtil.isAssignableFrom(int.class, "Integer"));
		assertFalse(ReflectUtil.isAssignableFrom(Integer.class, "int"));
		assertTrue(ReflectUtil.isAssignableFrom(Status.class, "IStatus"));
		assertFalse(ReflectUtil.isAssignableFrom(IStatus.class, "IConstraintStatus"));
		assertTrue(ReflectUtil.isAssignableFrom(ConstraintStatus.class, "IStatus"));
		assertTrue(ReflectUtil.isAssignableFrom(ConstraintStatus.class, "IConstraintStatus"));
		assertTrue(ReflectUtil.isAssignableFrom(DisabledConstraintStatus.class, "ConstraintStatus"));
		assertTrue(ReflectUtil.isAssignableFrom(DisabledConstraintStatus.class, "IConstraintStatus"));
		assertFalse(ReflectUtil.isAssignableFrom(DisabledConstraintStatus.class, "IResourceStatus"));
	}

	/**
	 * Test method for {@link ReflectUtil#clearAllFields(Object)}
	 */
	public void testClearAllFields_1() throws IllegalAccessException {
		Platform platform = createPlatform();
		assertFalse(platform.getComponentTypes().isEmpty());

		ComponentType componentType = platform.getComponentTypes().get(0);
		assertFalse(componentType.getPorts().isEmpty());

		Port port = componentType.getPorts().get(0);
		ReflectUtil.clearAllFields(port);

		assertNotNull(port);
		// Fields of super class
		assertNull(port.getName());

		// Fields of object class
		// Static/final field is not cleared
		assertNotNull(port.getMaxProviderCount());
		// Primitive field is not cleared
		assertNotNull(port.getMinProviderCount());

		assertNull(port.getRequiredInterface());

	}

	/**
	 * Test method for {@link ReflectUtil#clearAllFields(Object, String[])}
	 *
	 * @throws IllegalAccessException
	 */
	public void testClearAllFields_2() throws IllegalAccessException {
		Platform platform = createPlatform();
		assertFalse(platform.getComponentTypes().isEmpty());

		ComponentType componentType = platform.getComponentTypes().get(0);
		assertFalse(componentType.getPorts().isEmpty());

		Port port = componentType.getPorts().get(0);
		ReflectUtil.clearAllFields(port, new String[] { "name" });

		assertNotNull(port);
		// Fields of super class in excluded list
		assertNotNull(port.getName());

		// Fields of object class
		// Static/final field is not cleared
		assertNotNull(port.getMaxProviderCount());
		// Primitive field is not cleared
		assertNotNull(port.getMinProviderCount());

		assertNull(port.getRequiredInterface());

	}

	/**
	 * Test method for {@link ReflectUtil#getFieldValue(Object, String)}
	 */
	public void testGetFieldValue() {
		String publicValue = "publicValue";
		String protectedValue = "protectedValue";
		String privateValue = "privateValue";

		Data testData = new Data(publicValue, privateValue, protectedValue);

		// Non- static
		// Public field
		try {
			assertEquals("Got value is not the same with expected", publicValue, ReflectUtil.getFieldValue(testData, "publicField"));
		} catch (Exception e) {
			if (e instanceof IllegalAccessException) {
				fail("Cannot access to public field");
			} else if (e instanceof NoSuchFieldException) {
				fail("Cannot find to public field");

			} else {
				fail(e.getLocalizedMessage());
			}
		}
		try {
			assertNull(ReflectUtil.getFieldValue(Data.class, "publicField"));
		} catch (Exception e) {
			if (e instanceof IllegalAccessException) {
				fail("Cannot access to public field");
			} else if (e instanceof NoSuchFieldException) {
				fail("Cannot find to public field");
			} else if (!(e instanceof IllegalArgumentException)) {
				fail(e.getClass() + "  " + e.getLocalizedMessage());
			}
		}
		// Protected Field
		try {
			assertEquals("Got value is not the same with expected", protectedValue, ReflectUtil.getFieldValue(testData, "protectedField"));
		} catch (Exception e) {
			if (!(e instanceof IllegalAccessException)) {
				fail("Did not throw IllegalAccessException when the given field is private or protected:\n " + "Threw: " + e.getClass() + " "
						+ e.getMessage());
			}
		}
		try {
			assertNull(ReflectUtil.getFieldValue(Data.class, "protectedField"));
		} catch (Exception e) {

			if (!(e instanceof IllegalAccessException || e instanceof IllegalArgumentException)) {
				fail("Did not throw IllegalAccessException/IllegalArgumentException when get the given field is private or protected from class instead of object:\n "
						+ "Threw: " + e.getClass() + " " + e.getMessage());
			}
		}
		// Private field
		try {
			assertEquals(privateValue, ReflectUtil.getFieldValue(testData, "privateField"));
		} catch (Exception e) {
			if (!(e instanceof IllegalAccessException)) {
				fail("Did not throw IllegalAccessException when the given field is private or protected:\n " + "Threw: " + e.getClass() + " "
						+ e.getMessage());
			}
		}
		try {
			assertNull(ReflectUtil.getFieldValue(Data.class, "privateField"));
		} catch (Exception e) {
			if (!(e instanceof IllegalAccessException || e instanceof IllegalArgumentException)) {
				fail("Did not throw IllegalAccessException/IllegalArgumentException when get the given field is private or protected from class instead of object:\n "
						+ "Threw: " + e.getClass() + " " + e.getMessage());
			}
		}
		// Static Field
		// -- visible field
		try {
			assertEquals(Data.pubSField, ReflectUtil.getFieldValue(Data.class, "pubSField"));
		} catch (Exception e) {
			fail("Cannot get public static field: " + e.getLocalizedMessage());
		}
		// --invisible field
		try {
			assertEquals(Data.proSField, ReflectUtil.getFieldValue(Data.class, "proSField"));
		} catch (Exception e) {
			if (!(e instanceof IllegalAccessException)) {

				fail("Throw " + e.getClass().toString() + "-Did not throw IllegalAccessException when the given field is private or protectedt");
			}
		}

		try {
			assertEquals(Data.getPrisfield(), ReflectUtil.getFieldValue(Data.class, "priSField"));
		} catch (Exception e) {
			if (!(e instanceof IllegalAccessException)) {
				fail("Throw " + e.getClass().toString() + "-Did not throw IllegalAccessException when the given field is private or protectedt");
			}
		}

		// Un_existing Field
		try {
			ReflectUtil.getFieldValue(testData, "unexistingField");
		} catch (Exception e) {
			if (!(e instanceof NoSuchFieldException)) {
				fail("Throw " + e.getClass().toString() + "-Did not throw NoSuchFieldException when the given field is not exit");
			}
		}

	}

	/**
	 * Test method for {@link ReflectUtil#getInvisibleFieldValue(Object, String)}
	 *
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 */
	public void testGetInvisibleFieldValue() throws Exception {
		String publicValue = "publicValue";
		String protectedValue = "protectedValue";
		String privateValue = "privateValue";

		Data testData = new Data(publicValue, privateValue, protectedValue);

		// Non static field
		// --Non-static private field
		try {
			assertEquals("Return values are not equal", testData.getPrivateField(), ReflectUtil.getInvisibleFieldValue(testData, "privateField"));
		} catch (Exception e) {
			if (e instanceof IllegalAccessException) {
				fail("Cannot access non -static protected field");
			}
		}
		// -- Non-static protected field
		// -in different package
		try {
			assertEquals(true, ReflectUtil.getInvisibleFieldValue(PerformanceStatsLog.class, "consoleLog"));
		} catch (Exception e) {
			if (e instanceof IllegalAccessException) {
				fail("Cannot access non -static protected field");
			}
		}
		// in the same package
		try {
			assertEquals(testData.protectedField, ReflectUtil.getInvisibleFieldValue(testData, "protected field"));
		} catch (Exception e) {
			if (e instanceof IllegalAccessException) {
				fail("Cannot access non -static protected field");
			}
		}
		// Static field
		// --Static protected field
		// in the same package
		try {
			assertEquals(Data.proSField, ReflectUtil.getInvisibleFieldValue(Data.class, "proSField"));
		} catch (Exception e) {
			if (e instanceof IllegalAccessException) {
				fail("Cannot access static protected field");
			}
		}
		// in different package
		try {
			assertEquals("!STACK", ReflectUtil.getInvisibleFieldValue(PerformanceStatsLog.class, "STACK"));
		} catch (Exception e) {
			if (e instanceof IllegalAccessException) {
				fail("Cannot access static protected field");
			}
		}
		// --Static private field
		try {
			assertEquals(Data.getPrisfield(), ReflectUtil.getInvisibleFieldValue(Data.class, "priSField"));
		} catch (Exception e) {
			if (e instanceof IllegalAccessException) {
				fail("Cannot access static private field");
			}
		}

		// Given field is not exist
		try {
			ReflectUtil.getInvisibleFieldValue(Data.class, "priField");
		} catch (Exception e) {
			if (!(e instanceof NoSuchFieldException)) {
				fail("Don't throw NoSuchFiledException when getting unexsitingField\nThrew " + e.getClass() + " " + e.getLocalizedMessage());
			}
		}
	}

	/**
	 * Test method for {@link ReflectUtil#invokeMethod(Object, String, Object...)}
	 */
	public void testInvokeMethod() {
		Platform platform = createPlatform();
		String expectedPlatformName = platform.getName();

		ResourceSet testResourceSet = new ScopingResourceSetImpl();
		Resource testResource = testResourceSet.createResource(URI.createURI("newResource.xml"));
		testResource.getContents().add(platform);

		// Non static public method
		boolean flag = false;
		String message = "";
		try {
			assertSame(expectedPlatformName, ReflectUtil.invokeMethod(platform, "getName"));

		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
		// public static method
		try {
			Object copiedObject = ReflectUtil.invokeMethod(EcoreUtil.class, "copy", platform);
			assertNotNull(copiedObject);
		} catch (Exception e) {
			fail("Cannot invoke public static method");

		}

		// Protected method
		try {
			assertEquals(testResource, ReflectUtil.invokeMethod(testResourceSet, "getContextResourceScopes", platform));
		} catch (Exception e) {
			if (e instanceof IllegalAccessException) {
				flag = true;
			} else {
				message = "Thrown exception is: " + e.getClass() + ":" + e.getMessage() + " instead of IllegalAccessException";
			}
		}
		assertTrue(message, flag);

		// Un_existing method
		flag = false;
		try {
			ReflectUtil.invokeMethod(testResourceSet, "getContextResourceSet", platform);
		} catch (Exception e) {
			if (e instanceof NoSuchMethodException) {
				flag = true;
			} else {
				message = "Thrown exception is: " + e.getClass() + ":" + e.getMessage() + " instead of NoSuchMethodException";
			}
		}
		assertTrue(message, flag);
		flag = false;
		try {
			ReflectUtil.invokeMethod(testResourceSet, "getEObject", platform, true);
		} catch (Exception e) {
			if (e instanceof NoSuchMethodException) {
				flag = true;
			} else {
				message = "Thrown exception is: " + e.getClass() + ":" + e.getMessage() + " instead of NoSuchMethodException";
			}
		}

		assertTrue(message, flag);
		flag = false;
		try {
			ReflectUtil.invokeMethod(testResourceSet, "getEObject", platform);
		} catch (Exception e) {
			if (e instanceof NoSuchMethodException) {
				flag = true;
			} else {
				message = "Thrown exception is: " + e.getClass() + ":" + e.getMessage() + " instead of NoSuchMethodException";
			}
		}
		assertTrue(message, flag);
	}

	/**
	 * Test method for {@link ReflectUtil#invokeInvisibleMethod(Object, String, Object...)}
	 */
	public void testInvokeInvisibleMethod() throws Exception {
		Platform platform = createPlatform();

		ResourceSet testResourceSet = new ResourceSetImpl();
		Resource testResource = testResourceSet.createResource(URI.createURI("newResource.xml"));
		testResource.getContents().add(platform);

		// Non static method
		// --protected method
		FileNotFoundException testIOException = new FileNotFoundException("testIOException");
		try {
			ReflectUtil.invokeInvisibleMethod(testResourceSet, "handleDemandLoadException", testResource, testIOException);
		} catch (InvocationTargetException ite) {
			assertSame(ite.getTargetException().getCause(), testIOException);
		} catch (Exception e) {
			fail("Cannot invoke non-static private method " + e.getLocalizedMessage());
		}
		assertEquals(1, testResource.getErrors().size());
		Diagnostic diagnostic = testResource.getErrors().get(0);
		assertTrue(diagnostic instanceof WrappedException);
		assertSame(testIOException, ((WrappedException) diagnostic).getCause());

		// -- private method
		try {
			assertTrue(ReflectUtil.invokeInvisibleMethod(this, "createPlatform") instanceof Platform);
		} catch (Exception e) {

			fail("Cannot invoke non-static private method " + e.getLocalizedMessage());
		}

		// Static method
		// -- protected static
		try {

			assertEquals(Data.proSField, ReflectUtil.invokeInvisibleMethod(Data.class, "proctectedStaticMethod"));
		} catch (Exception e) {

			fail("Cannot invoke proctected static method " + e.getLocalizedMessage());
		}
		// --private static
		try {

			assertEquals(Data.getPrisfield(), ReflectUtil.invokeInvisibleMethod(Data.class, "privateStaticMethod"));
		} catch (Exception e) {

			fail("Cannot invoke private static  method " + e.getLocalizedMessage());
		}
		// Un_existing method
		boolean flag = false;
		try {
			ReflectUtil.invokeInvisibleMethod(Resource.class, "getContextResourceSet", platform);
		} catch (NoSuchMethodException e) {
			flag = true;
		}
		String message = "Did not throw NoSuchMethodException when the given method is not exist";
		assertTrue(message, flag);

		// Visible method
		flag = false;
		message = "";
		try {
			assertSame(platform, ReflectUtil.invokeInvisibleMethod(testResourceSet, "getEObject", EcoreUtil.getURI(platform), true));

		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
	}

	/**
	 * Test method for {@link ReflectUtil#findDeclaredField(Class, String)}
	 */
	public void testFindDeclaredField() {
		assertNull(ReflectUtil.findDeclaredField(Platform.class, "name"));
		assertNull(ReflectUtil.findDeclaredField(PlatformImpl.class, "name"));

		assertNull(ReflectUtil.findDeclaredField(Platform.class, "interfaces"));
		assertNotNull(ReflectUtil.findDeclaredField(PlatformImpl.class, "interfaces"));
		assertNotNull(ReflectUtil.findDeclaredField(PlatformImpl.class, "componentTypes"));
		assertNull(ReflectUtil.findDeclaredField(PlatformImpl.class, "componentType"));
	}

	/**
	 * Test method for {@link ReflectUtil#findField(Class, String)}
	 */
	public void testFindField() {
		assertNull(ReflectUtil.findField(Platform.class, "name"));
		assertNotNull(ReflectUtil.findField(PlatformImpl.class, "name"));

		assertNotNull(ReflectUtil.findField(PlatformImpl.class, "interfaces"));
		assertNotNull(ReflectUtil.findField(PlatformImpl.class, "componentTypes"));
		assertNull(ReflectUtil.findField(PlatformImpl.class, "ShortName"));

	}

	/**
	 * Test method for {@link ReflectUtil#findDeclaredMethod(Class, String, Class...)}
	 *
	 * @throws Exception
	 */
	public void testFindDeclaredMethod() throws Exception {
		assertNull(ReflectUtil.findDeclaredMethod(Platform.class, "getName"));
		assertNull(ReflectUtil.findDeclaredMethod(PlatformImpl.class, "getName"));

		assertNotNull(ReflectUtil.findDeclaredMethod(Platform.class, "getComponentTypes"));
		assertNotNull(ReflectUtil.findDeclaredMethod(PlatformImpl.class, "getComponentTypes"));
		assertNotNull(ReflectUtil.findDeclaredMethod(Platform.class, "getInterfaces"));
		assertNull(ReflectUtil.findDeclaredMethod(Platform.class, "getInter"));
	}

	/**
	 * Test method for {@link ReflectUtil#findMethod(Class, String, Class...)}
	 *
	 * @throws Exception
	 */
	public void testFindMethod() throws Exception {
		// Overloading
		// loadProject(IProject project, boolean includeReferencedProjects, boolean async, IProgressMonitor monitor)

		assertNull(
				ReflectUtil.findMethod(IModelLoadManager.class, "LoadProject", IProject.class, boolean.class, boolean.class, IProgressMonitor.class));
		assertNull(ReflectUtil.findMethod(IModelLoadManager.class, "loadProject", IProject.class));
		assertNull(ReflectUtil.findMethod(IModelLoadManager.class, "loadProject", IProject.class, boolean.class, boolean.class, String.class));
		assertNotNull(
				ReflectUtil.findMethod(IModelLoadManager.class, "loadProject", IProject.class, boolean.class, boolean.class, IProgressMonitor.class));
		assertEquals(4,
				ReflectUtil.findMethod(IModelLoadManager.class, "loadProject", IProject.class, boolean.class, boolean.class, IProgressMonitor.class)
						.getParameterTypes().length);

		assertNotNull(ReflectUtil.findMethod(IModelLoadManager.class, "loadProject", IProject.class, boolean.class, IMetaModelDescriptor.class,
				boolean.class, IProgressMonitor.class));
		assertEquals(5, ReflectUtil.findMethod(IModelLoadManager.class, "loadProject", IProject.class, boolean.class, IMetaModelDescriptor.class,
				boolean.class, IProgressMonitor.class).getParameterTypes().length);

		// Interface
		assertNotNull(ReflectUtil.findMethod(Platform.class, "getName"));
		assertNotNull(ReflectUtil.findMethod(Platform.class, "getComponentTypes"));

		assertNull(ReflectUtil.findMethod(Platform.class, "getShortName"));
		// SuperClass
		assertNotNull(ReflectUtil.findMethod(PlatformImpl.class, "getName"));

		assertNull(ReflectUtil.findMethod(PlatformImpl.class, "eUnset"));
		assertNotNull(ReflectUtil.findMethod(PlatformImpl.class, "eUnset", int.class));
		assertNull(ReflectUtil.findMethod(PlatformImpl.class, "eUnset", int.class, String.class));
	}

}

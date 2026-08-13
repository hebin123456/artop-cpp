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
package org.eclipse.sphinx.tests.emf.internal.expressions;

import junit.framework.TestCase;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.impl.EcoreFactoryImpl;
import org.eclipse.sphinx.emf.internal.expressions.EMFObjectPropertyTester;

@SuppressWarnings({ "nls", "restriction" })
public class EMFObjectPropertyTesterTest extends TestCase {

	private static final String INSTANCE_OF = "instanceOf";
	private static final String CLASS_NAME_MATCHES = "classNameMatches";

	public EMFObjectPropertyTester propertyTester;

	@Override
	public void setUp() throws Exception {
		propertyTester = new EMFObjectPropertyTester();
	}

	@Override
	public void tearDown() throws Exception {

	}

	public void testInstanceOfTest() {
		EObject eObjA = new EObjectA();

		boolean result = propertyTester.test(eObjA, INSTANCE_OF, null, EObjectA.class.getName());
		assertTrue("Tested if an instance of EObjectA was instanceof EObjectA.", result);
		result = propertyTester.test(eObjA, INSTANCE_OF, null, EObjectB.class.getName());
		assertFalse("Tested if an instance of EObjectA was instanceof EObjectB.", result);
		result = propertyTester.test(eObjA, INSTANCE_OF, null, EObject.class.getName());
		assertTrue("Tested if an instance of EObjectA was instanceof EObject.", result);
	}

	public void testClassNameMatchesTest() {
		EObject eObjA = new EObjectA();
		boolean result = propertyTester.test(eObjA, CLASS_NAME_MATCHES, null, ".*EObjectA");
		assertTrue("Tested if the class name of EObjectA ends with 'EObjectA'", result);
		result = propertyTester.test(eObjA, CLASS_NAME_MATCHES, null, "EObjectA");
		assertFalse("Tested if the class name of EObjectA is equal to 'EObjectA'", result);
	}

	private class EObjectA extends EObjectImpl {

		public EObjectA() {
			EcoreFactory factory = new EcoreFactoryImpl();
			EClass eClass = factory.createEClass();
			eClass.setName(EObjectA.class.getSimpleName());
			eClass.setInstanceClass(EObjectA.class);
			eSetClass(eClass);
		}

	}

	private class EObjectB extends EObjectImpl {

		@SuppressWarnings("unused")
		public EObjectB() {
			EcoreFactory factory = new EcoreFactoryImpl();
			EClass eClass = factory.createEClass();
			eClass.setName(EObjectB.class.getSimpleName());
			eClass.setInstanceClass(EObjectB.class);
			eSetClass(eClass);
		}

	}

}

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
package org.eclipse.sphinx.tests.emf.comparators;

import junit.framework.TestCase;

import org.eclipse.sphinx.emf.ecore.EcoreComparator;
import org.eclipse.sphinx.examples.hummingbird10.Application;
import org.eclipse.sphinx.examples.hummingbird10.Component;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Factory;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;

@SuppressWarnings("nls")
public class EcoreComparatorTest extends TestCase {

	/**
	 * Test method for {@link EcoreComparator#compareObjects(Object o1, Object o2)} .
	 */
	public void testCompareObjects() {

		Application appHummingbird10 = Hummingbird10Factory.eINSTANCE.createApplication();
		appHummingbird10.setName("Model");

		Component component = Hummingbird10Factory.eINSTANCE.createComponent();
		component.setName("Component");
		appHummingbird10.getComponents().add(component);

		Application appHummingbird10_ = Hummingbird10Factory.eINSTANCE.createApplication();
		appHummingbird10.setName("Model");

		org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application appHummingbird20 = InstanceModel20Factory.eINSTANCE.createApplication();
		appHummingbird20.setName("Application");

		ComponentType componentType = TypeModel20Factory.eINSTANCE.createComponentType();
		componentType.setName("componentType");

		EcoreComparator comparator = new EcoreComparator();

		assertTrue(comparator.compare(true, true) == 0);
		assertFalse(comparator.compare(true, false) == 0);
		assertTrue(comparator.compare("String", "String") == 0);
		assertTrue(comparator.compare("String", "NoTheSameString") > 0);
		assertTrue(comparator.compare(appHummingbird10, appHummingbird10) == 0);
		assertTrue(comparator.compare(appHummingbird10, appHummingbird10_) == 0);
		// assertTrue("res: " + comparator.compare(appHummingbird10, appHummingbird20),
		// comparator.compare(appHummingbird10, appHummingbird20) > 0);
		// assertTrue(comparator.compare(appHummingbird10_, appHummingbird20) > 0);
		// assertTrue(comparator.compare(component, componentType) > 0);
		// assertTrue("res: " + comparator.compare(appHummingbird10, component),
		// comparator.compare(appHummingbird10, component) > 0);
	}
}

/**
 * <copyright>
 *
 * Copyright (c) 2012 BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     BMW Car IT - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.ecore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.sphinx.emf.ecore.OrderedFeatureMap.Order;
import org.eclipse.sphinx.emf.ecore.OrderedFeatureMap.OrderedListBehavior;
import org.junit.Test;

@SuppressWarnings("nls")
public class OrderedFeatureMapTest {
	final String s1 = "1";
	final String s2 = "2";
	final String s3 = "3";
	final String s4 = "4";

	Order<String> o = new Order<String>() {
		@Override
		public int order(String s) {
			return Integer.valueOf(s);
		}
	};

	List<String> l = new ArrayList<String>();

	OrderedListBehavior<String> olb = new OrderedListBehavior<String>(l, o);

	@Test
	public void testOrderedListBehaviorAdd1() {
		l.add(s1);
		l.add(s4);

		olb.add(l.size(), s2);

		assertEquals(Arrays.asList(s1, s2, s4), l);
	}

	@Test
	public void testOrderedListBehaviorAdd2() {
		l.add(s1);
		l.add(s4);

		olb.add(0, s2);

		assertEquals(Arrays.asList(s1, s2, s4), l);
	}

	@Test
	public void testOrderedListBehaviorAdd3() {
		l.add(s2);
		l.add(s3);

		olb.add(0, s1);

		assertEquals(Arrays.asList(s1, s2, s3), l);
	}

	@Test
	public void testOrderedListBehaviorAddAll1() {
		l.add(s1);
		l.add(s3);

		olb.addAll(l.size(), Arrays.asList(s2, s4));

		assertEquals(Arrays.asList(s1, s2, s3, s4), l);
	}

	@Test
	public void testOrderedListBehaviorAddAll2() {
		l.add(s1);
		l.add(s3);

		olb.addAll(0, Arrays.asList(s2, s4));

		assertEquals(Arrays.asList(s1, s2, s3, s4), l);
	}

	@Test
	public void testOrderedListBehaviorAddAll3() {
		l.add(s1);
		l.add(s4);

		olb.addAll(l.size(), Arrays.asList(s2, s3));

		assertEquals(Arrays.asList(s1, s2, s3, s4), l);
	}

	@Test
	public void testOrderedListBehaviorSet() {
		l.add(s1);
		l.add(s3);

		olb.set(1, s3);

		assertEquals(Arrays.asList(s1, s3), l);

		try {
			olb.set(0, s3);
		} catch (IllegalStateException e) {
			return;
		}

		fail("Setting an index with a different order shouldn't be allowed.");
	}
}

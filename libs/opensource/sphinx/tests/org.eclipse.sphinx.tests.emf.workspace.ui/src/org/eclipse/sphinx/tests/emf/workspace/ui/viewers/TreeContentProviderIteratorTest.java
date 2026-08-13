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
package org.eclipse.sphinx.tests.emf.workspace.ui.viewers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.sphinx.emf.workspace.ui.viewers.TreeContentProviderIterator;
import org.eclipse.sphinx.tests.emf.workspace.ui.scenarios.Hummingbird20ScenarioTreeContentProvider;
import org.junit.Test;

public class TreeContentProviderIteratorTest {

	@Test
	public void testTreeContentProviderIteratorOnProject1() {
		Hummingbird20ScenarioTreeContentProvider provider = new Hummingbird20ScenarioTreeContentProvider();
		TreeContentProviderIterator iter = new TreeContentProviderIterator(provider, provider.project1);

		assertTrue(iter.hasNext());
		assertSame(provider.project1, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.file1, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.application1, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.description1, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.components1, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component11, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.outgoingConnections11, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component11ToComponent22Connection, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component22Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.outgoingConnections22Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component22ToComponent11ConnectionRef, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component11RefRef, iter.next());
		assertTrue(iter.isRecurrent());

		// As component11RefRef is recurrent from the project1's perspective, its outgoingConnections11RefRef and
		// parameterValues11RefRef children are skipped

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValues22Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValue221Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValues11, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValue111, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValue112, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component12, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.file2, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.application2, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.description2, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.components2, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component21, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component22, iter.next());
		assertTrue(iter.isRecurrent());

		// As component22 is recurrent from the project1's perspective, its outgoingConnections22 and parameterValue221
		// children are skipped and the tree iteration terminates right here
		assertFalse(iter.hasNext());
	}

	@Test
	public void testTreeContentProviderIteratorOnFile1() {
		Hummingbird20ScenarioTreeContentProvider provider = new Hummingbird20ScenarioTreeContentProvider();
		TreeContentProviderIterator iter = new TreeContentProviderIterator(provider, provider.file1);

		assertTrue(iter.hasNext());
		assertSame(provider.file1, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.application1, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.description1, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.components1, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component11, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.outgoingConnections11, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component11ToComponent22Connection, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component22Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.outgoingConnections22Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component22ToComponent11ConnectionRef, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component11RefRef, iter.next());
		assertTrue(iter.isRecurrent());

		// As component11RefRef is recurrent from the file1's perspective, its outgoingConnections11RefRef and
		// parameterValues11RefRef children are skipped

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValues22Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValue221Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValues11, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValue111, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValue112, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component12, iter.next());
		assertFalse(iter.isRecurrent());

		assertFalse(iter.hasNext());
	}

	@Test
	public void testTreeContentProviderIteratorOnFile2() {
		Hummingbird20ScenarioTreeContentProvider provider = new Hummingbird20ScenarioTreeContentProvider();
		TreeContentProviderIterator iter = new TreeContentProviderIterator(provider, provider.file2);

		assertTrue(iter.hasNext());
		assertSame(provider.file2, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.application2, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.description2, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.components2, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component21, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component22, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.outgoingConnections22, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component22ToComponent11Connection, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component11Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.outgoingConnections11Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component11ToComponent22ConnectionRef, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component22RefRef, iter.next());
		assertTrue(iter.isRecurrent());

		// As component22RefRef is recurrent from the file2's perspective, its outgoingConnections22RefRef and
		// parameterValues22RefRef children are skipped

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValues11Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValue111Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValue112Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValues22, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValue221, iter.next());
		assertFalse(iter.isRecurrent());

		assertFalse(iter.hasNext());
	}

	@Test
	public void testTreeContentProviderIteratorOnComponent11ToComponent22Connection() {
		Hummingbird20ScenarioTreeContentProvider provider = new Hummingbird20ScenarioTreeContentProvider();
		TreeContentProviderIterator iter = new TreeContentProviderIterator(provider,
				provider.component11ToComponent22Connection);

		assertTrue(iter.hasNext());
		assertSame(provider.component11ToComponent22Connection, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component22Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.outgoingConnections22Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component22ToComponent11ConnectionRef, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component11RefRef, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.outgoingConnections11RefRef, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.component11ToComponent22ConnectionRefRef, iter.next());
		assertTrue(iter.isRecurrent());

		// As component11ToComponent22ConnectionRefRef is recurrent from the component11ToComponent22Connection's
		// perspective, its component22RefRefRef child is skipped

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValues11RefRef, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValue111RefRef, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValue112RefRef, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValues22Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertTrue(iter.hasNext());
		assertSame(provider.parameterValue221Ref, iter.next());
		assertFalse(iter.isRecurrent());

		assertFalse(iter.hasNext());
	}
}

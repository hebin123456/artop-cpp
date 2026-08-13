/**
 * <copyright>
 *
 * Copyright (c) 2016-2021 itemis, Siemens and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     Siemens - [577073] URI change detector delegate extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.workspace.referentialintegrity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.sphinx.emf.workspace.internal.referentialintegrity.IntermittentRemoveTracker;
import org.eclipse.sphinx.emf.workspace.referentialintegrity.IURIChangeDetectorDelegate;
import org.eclipse.sphinx.emf.workspace.referentialintegrity.URIChangeNotification;
import org.eclipse.sphinx.examples.hummingbird20.ide.internal.referentialintegrity.Hummingbird20URIChangeDetectorDelegate;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue;
import org.eclipse.sphinx.tests.emf.workspace.referentialintegrity.scenarios.Hummingbird20TestModel;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings({ "nls", "restriction" })
public class AbstractHierarchicalFragmentURIChangeDetectorDelegateTest {

	protected static final int MAX_INTERMITTENT_REMOVE_TEST_INTERVAL = 100;

	private static class TestURIChangeDetectorDelegate extends Hummingbird20URIChangeDetectorDelegate {
		@Override
		protected IntermittentRemoveTracker createIntermittentRemoveTracker() {
			IntermittentRemoveTracker intermittentRemoveTracker = super.createIntermittentRemoveTracker();
			intermittentRemoveTracker.setMaxIntermittentRemoveInterval(100);
			return intermittentRemoveTracker;
		}
	}

	private static class TestURIChangeDetectorAdapter extends EContentAdapter {

		private IURIChangeDetectorDelegate delegate;
		private Map<Resource, List<URIChangeNotification>> changedURIs = new HashMap<>();

		public TestURIChangeDetectorAdapter(IURIChangeDetectorDelegate delegate) {
			this.delegate = delegate;
		}

		@Override
		public void notifyChanged(Notification notification) {
			Map<Resource, List<URIChangeNotification>> changedURIsResult = delegate.detectChangedURIs(Collections.singletonList(notification));

			for (Resource resource : changedURIsResult.keySet()) {
				if (changedURIs.containsKey(resource)) {
					changedURIs.get(resource).addAll(changedURIsResult.get(resource));
				} else {
					changedURIs.put(resource, changedURIsResult.get(resource));
				}
			}

		}

		public Map<Resource, List<URIChangeNotification>> getChangedURIs() {
			return changedURIs;
		}

		public long getChangedURISize() {
			return getChangedURIs().values().stream().map(Collection::size).reduce(0, Integer::sum);
		}
	}

	private Hummingbird20TestModel model;
	private TestURIChangeDetectorAdapter uriChangeDectector;

	@Before
	public void setup() {
		model = new Hummingbird20TestModel();
		uriChangeDectector = new TestURIChangeDetectorAdapter(new TestURIChangeDetectorDelegate());
		model.resourceSet.eAdapters().add(uriChangeDectector);
	}

	@Test
	public void testDetectChangedURIsNotification_singleLeafObjectChanged() {
		model.parameterValue111.setName("parameterValue111_changed");

		Map<Resource, List<URIChangeNotification>> changedURIs = uriChangeDectector.getChangedURIs();
		assertEquals(1, uriChangeDectector.getChangedURISize());

		assertTrue(changedURIs.containsKey(model.resource1));
		assertEquals(1, changedURIs.keySet().size());
		URIChangeNotification uriChangeNotification = changedURIs.get(model.resource1).get(0);
		assertSame(model.parameterValue111, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource1#//component11/parameterValue111"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource1#//component11/parameterValue111_changed"), uriChangeNotification.getNewURI());
	}

	@Test
	public void testDetectChangedURIsNotification_multipleLeafObjectsChanged() {
		model.parameterValue111.setName("parameterValue111_changed");
		model.parameterValue112.setName("parameterValue112_changed");

		Map<Resource, List<URIChangeNotification>> changedURIs = uriChangeDectector.getChangedURIs();
		assertEquals(2, uriChangeDectector.getChangedURISize());

		assertTrue(changedURIs.containsKey(model.resource1));
		assertEquals(1, changedURIs.keySet().size());
		URIChangeNotification uriChangeNotification = changedURIs.get(model.resource1).get(0);
		assertSame(model.parameterValue111, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource1#//component11/parameterValue111"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource1#//component11/parameterValue111_changed"), uriChangeNotification.getNewURI());

		uriChangeNotification = changedURIs.get(model.resource1).get(1);
		assertSame(model.parameterValue112, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource1#//component11/parameterValue112"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource1#//component11/parameterValue112_changed"), uriChangeNotification.getNewURI());
	}

	@Test
	public void testDetectChangedURIsNotification_containerObjectChanged() {
		model.component11.setName("component11_changed");

		Map<Resource, List<URIChangeNotification>> changedURIs = uriChangeDectector.getChangedURIs();
		assertEquals(4, uriChangeDectector.getChangedURISize());

		assertTrue(changedURIs.containsKey(model.resource1));
		assertEquals(1, changedURIs.keySet().size());
		URIChangeNotification uriChangeNotification = changedURIs.get(model.resource1).get(0);
		assertSame(model.component11, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource1#//component11"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource1#//component11_changed"), uriChangeNotification.getNewURI());

		uriChangeNotification = changedURIs.get(model.resource1).get(1);
		assertSame(model.component11ToComponent22Connection, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource1#//component11/component11ToComponent22Connection"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource1#//component11_changed/component11ToComponent22Connection"), uriChangeNotification.getNewURI());

		uriChangeNotification = changedURIs.get(model.resource1).get(2);
		assertSame(model.parameterValue111, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource1#//component11/parameterValue111"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource1#//component11_changed/parameterValue111"), uriChangeNotification.getNewURI());

		uriChangeNotification = changedURIs.get(model.resource1).get(3);
		assertSame(model.parameterValue112, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource1#//component11/parameterValue112"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource1#//component11_changed/parameterValue112"), uriChangeNotification.getNewURI());
	}

	@Test
	public void testDetectChangedURIsNotification_singleLeafObjectRemovedAndAddedElsewhere() {
		model.component22.getParameterValues().remove(model.parameterValue221);
		model.component11.getParameterValues().add(model.parameterValue221);

		Map<Resource, List<URIChangeNotification>> changedURIs = uriChangeDectector.getChangedURIs();
		assertEquals(1, uriChangeDectector.getChangedURISize());

		assertTrue(changedURIs.containsKey(model.resource1));
		assertEquals(1, changedURIs.keySet().size());
		URIChangeNotification uriChangeNotification = changedURIs.get(model.resource1).get(0);
		assertSame(model.parameterValue221, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource2#//component22/parameterValue221"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource1#//component11/parameterValue221"), uriChangeNotification.getNewURI());
	}

	@Test
	public void testDetectChangedURIsNotification_multipleLeafObjectsRemovedAndAddedElsewhere() {
		model.component11.getParameterValues().remove(model.parameterValue111);
		model.component11.getParameterValues().remove(model.parameterValue112);
		model.component22.getParameterValues().add(model.parameterValue111);
		model.component22.getParameterValues().add(model.parameterValue112);

		Map<Resource, List<URIChangeNotification>> changedURIs = uriChangeDectector.getChangedURIs();
		assertEquals(2, uriChangeDectector.getChangedURISize());

		assertTrue(changedURIs.containsKey(model.resource2));
		assertEquals(1, changedURIs.keySet().size());
		URIChangeNotification uriChangeNotification = changedURIs.get(model.resource2).get(0);
		assertSame(model.parameterValue111, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource1#//component11/parameterValue111"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource2#//component22/parameterValue111"), uriChangeNotification.getNewURI());

		uriChangeNotification = changedURIs.get(model.resource2).get(1);
		assertSame(model.parameterValue112, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource1#//component11/parameterValue112"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource2#//component22/parameterValue112"), uriChangeNotification.getNewURI());
	}

	@Test
	public void testDetectChangedURIsNotification_containerObjectRemovedAndAddedElsewhere() {
		model.application2.getComponents().remove(model.component22);
		model.application1.getComponents().add(model.component22);

		Map<Resource, List<URIChangeNotification>> changedURIs = uriChangeDectector.getChangedURIs();
		assertEquals(3, uriChangeDectector.getChangedURISize());

		assertTrue(changedURIs.containsKey(model.resource1));
		assertEquals(1, changedURIs.keySet().size());
		URIChangeNotification uriChangeNotification = changedURIs.get(model.resource1).get(0);
		assertSame(model.component22, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource2#//component22"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource1#//component22"), uriChangeNotification.getNewURI());

		uriChangeNotification = changedURIs.get(model.resource1).get(1);
		assertSame(model.component22ToComponent11Connection, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource2#//component22/component22ToComponent11Connection"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource1#//component22/component22ToComponent11Connection"), uriChangeNotification.getNewURI());

		uriChangeNotification = changedURIs.get(model.resource1).get(2);
		assertSame(model.parameterValue221, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource2#//component22/parameterValue221"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource1#//component22/parameterValue221"), uriChangeNotification.getNewURI());
	}

	@Test
	public void testDetectChangedURIsNotification_singleLeafObjectMoved() {
		model.component11.getParameterValues().add(model.parameterValue221);

		Map<Resource, List<URIChangeNotification>> changedURIs = uriChangeDectector.getChangedURIs();
		assertEquals(1, uriChangeDectector.getChangedURISize());

		assertTrue(changedURIs.containsKey(model.resource1));
		assertEquals(1, changedURIs.keySet().size());
		URIChangeNotification uriChangeNotification = changedURIs.get(model.resource1).get(0);
		assertSame(model.parameterValue221, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource2#//component22/parameterValue221"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource1#//component11/parameterValue221"), uriChangeNotification.getNewURI());
	}

	@Test
	public void testDetectChangedURIsNotification_multipleObjectsMoved() {
		model.component22.getParameterValues().add(model.parameterValue111);
		model.component22.getParameterValues().add(model.parameterValue112);

		Map<Resource, List<URIChangeNotification>> changedURIs = uriChangeDectector.getChangedURIs();
		assertEquals(2, uriChangeDectector.getChangedURISize());

		assertTrue(changedURIs.containsKey(model.resource2));
		assertEquals(1, changedURIs.keySet().size());
		URIChangeNotification uriChangeNotification = changedURIs.get(model.resource2).get(0);
		assertSame(model.parameterValue111, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource1#//component11/parameterValue111"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource2#//component22/parameterValue111"), uriChangeNotification.getNewURI());

		uriChangeNotification = changedURIs.get(model.resource2).get(1);
		assertSame(model.parameterValue112, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource1#//component11/parameterValue112"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource2#//component22/parameterValue112"), uriChangeNotification.getNewURI());
	}

	@Test
	public void testDetectChangedURIsNotification_containerObjectMoved() {
		model.application1.getComponents().add(model.component22);

		Map<Resource, List<URIChangeNotification>> changedURIs = uriChangeDectector.getChangedURIs();
		assertEquals(3, uriChangeDectector.getChangedURISize());

		assertTrue(changedURIs.containsKey(model.resource1));
		assertEquals(1, changedURIs.keySet().size());
		URIChangeNotification uriChangeNotification = changedURIs.get(model.resource1).get(0);
		assertSame(model.component22, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource2#//component22"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource1#//component22"), uriChangeNotification.getNewURI());

		uriChangeNotification = changedURIs.get(model.resource1).get(1);
		assertSame(model.component22ToComponent11Connection, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource2#//component22/component22ToComponent11Connection"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource1#//component22/component22ToComponent11Connection"), uriChangeNotification.getNewURI());

		uriChangeNotification = changedURIs.get(model.resource1).get(2);
		assertSame(model.parameterValue221, uriChangeNotification.getNewEObject());
		assertNotNull(uriChangeNotification.getOldURI());
		assertEquals(URI.createURI("resource2#//component22/parameterValue221"), uriChangeNotification.getOldURI());
		assertNotNull(uriChangeNotification.getNewURI());
		assertEquals(URI.createURI("resource1#//component22/parameterValue221"), uriChangeNotification.getNewURI());
	}

	@Test
	public void testDetectChangedURIsNotification_unrelatedRemovalsAndAdditions() throws InterruptedException {
		assertEquals(2, model.component11.getParameterValues().size());
		assertEquals(1, model.component22.getParameterValues().size());

		// Isolated addition
		ParameterValue parameterValue113 = InstanceModel20Factory.eINSTANCE.createParameterValue();
		model.component11.getParameterValues().add(parameterValue113);

		assertEquals(3, model.component11.getParameterValues().size());
		assertEquals(1, model.component22.getParameterValues().size());
		assertEquals(0, uriChangeDectector.getChangedURIs().size());

		// Isolated removal
		model.component11.getParameterValues().remove(model.parameterValue111);

		assertEquals(2, model.component11.getParameterValues().size());
		assertEquals(1, model.component22.getParameterValues().size());
		assertEquals(0, uriChangeDectector.getChangedURIs().size());

		// Addition and removal
		ParameterValue parameterValue114 = InstanceModel20Factory.eINSTANCE.createParameterValue();
		model.component11.getParameterValues().add(parameterValue114);
		model.component11.getParameterValues().remove(parameterValue114);

		assertEquals(2, model.component11.getParameterValues().size());
		assertEquals(1, model.component22.getParameterValues().size());
		assertEquals(0, uriChangeDectector.getChangedURIs().size());

		// Unrelated removal and addition
		model.component11.getParameterValues().remove(model.parameterValue112);
		Thread.sleep(MAX_INTERMITTENT_REMOVE_TEST_INTERVAL * 2);
		model.component22.getParameterValues().add(model.parameterValue112);

		assertEquals(1, model.component11.getParameterValues().size());
		assertEquals(2, model.component22.getParameterValues().size());
		assertEquals(0, uriChangeDectector.getChangedURIs().size());
	}
}

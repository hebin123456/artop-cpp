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
package org.eclipse.sphinx.tests.emf.workspace.resources;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.emf.edit.provider.WrapperItemProvider;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.tests.emf.workspace.resources.scenarios.BasicModelProblemMarkerFinderScenario;
import org.junit.Test;

@SuppressWarnings("nls")
public class BasicModelProblemMarkerFinderTest {

	public BasicModelProblemMarkerFinderTest() throws Exception {
		super();
	}

	protected int count(Collection<IMarker> problemMarkers, int severity) throws CoreException {
		int count = 0;
		for (IMarker problemMarker : problemMarkers) {
			if (problemMarker.getAttribute(IMarker.SEVERITY, -1) == severity) {
				count++;
			}
		}
		return count;
	}

	private static class EObjectProblemMarkerFinderScenario extends BasicModelProblemMarkerFinderScenario {

		public EObject eObject0;
		public EObject eObject1;
		public EObject eObject2;
		public EObject eObject3;
		public EObject eObject6;

		public EObjectProblemMarkerFinderScenario() throws CoreException {
			eObject0 = BasicModelProblemMarkerFinderScenario.createTestEObject("platform:/resource/myProject/myFile#/eObject0");
			addExpectedProblemMarkers(eObject0, 0, 0, 0);

			eObject1 = BasicModelProblemMarkerFinderScenario.createTestEObject("platform:/resource/myProject/myFile#/eObject1");
			addExpectedProblemMarkers(eObject1, 1, 0, 0);

			eObject2 = BasicModelProblemMarkerFinderScenario.createTestEObject("platform:/resource/myProject/myFile#/eObject2");
			addExpectedProblemMarkers(eObject2, 0, 2, 0);

			eObject3 = BasicModelProblemMarkerFinderScenario.createTestEObject("platform:/resource/myProject/myFile#/eObject3");
			addExpectedProblemMarkers(eObject3, 0, 0, 3);

			eObject6 = BasicModelProblemMarkerFinderScenario.createTestEObject("platform:/resource/myProject/myFile#/eObject6");
			addExpectedProblemMarkers(eObject6, 1, 2, 3);
		}
	};

	@Test
	public void testGetProblemMarkersForEObject() throws Exception {
		EObjectProblemMarkerFinderScenario scenario = new EObjectProblemMarkerFinderScenario();
		TestableModelProblemMarkerFinder modelProblemMarkerFinder = new TestableModelProblemMarkerFinder(scenario);

		Collection<IMarker> problemMarkers = modelProblemMarkerFinder.getProblemMarkers(scenario.eObject0);
		assertThat(problemMarkers, notNullValue());
		assertThat(problemMarkers.size(), equalTo(0));

		problemMarkers = modelProblemMarkerFinder.getProblemMarkers(scenario.eObject1);
		assertThat(problemMarkers, notNullValue());
		assertThat(problemMarkers.size(), equalTo(1));
		assertThat(count(problemMarkers, IMarker.SEVERITY_ERROR), equalTo(1));

		problemMarkers = modelProblemMarkerFinder.getProblemMarkers(scenario.eObject2);
		assertThat(problemMarkers, notNullValue());
		assertThat(problemMarkers.size(), equalTo(2));
		assertThat(count(problemMarkers, IMarker.SEVERITY_WARNING), equalTo(2));

		problemMarkers = modelProblemMarkerFinder.getProblemMarkers(scenario.eObject3);
		assertThat(problemMarkers, notNullValue());
		assertThat(problemMarkers.size(), equalTo(3));
		assertThat(count(problemMarkers, IMarker.SEVERITY_INFO), equalTo(3));

		problemMarkers = modelProblemMarkerFinder.getProblemMarkers(scenario.eObject6);
		assertThat(problemMarkers, notNullValue());
		assertThat(problemMarkers.size(), equalTo(6));
		assertThat(count(problemMarkers, IMarker.SEVERITY_ERROR), equalTo(1));
		assertThat(count(problemMarkers, IMarker.SEVERITY_WARNING), equalTo(2));
		assertThat(count(problemMarkers, IMarker.SEVERITY_INFO), equalTo(3));
	}

	@Test
	public void testGetSeverityOfEObject() throws Exception {
		EObjectProblemMarkerFinderScenario scenario = new EObjectProblemMarkerFinderScenario();
		TestableModelProblemMarkerFinder modelProblemMarkerFinder = new TestableModelProblemMarkerFinder(scenario);

		assertThat(modelProblemMarkerFinder.getSeverity(scenario.eObject0), equalTo(-1));
		assertThat(modelProblemMarkerFinder.getSeverity(scenario.eObject1), equalTo(IMarker.SEVERITY_ERROR));
		assertThat(modelProblemMarkerFinder.getSeverity(scenario.eObject2), equalTo(IMarker.SEVERITY_WARNING));
		assertThat(modelProblemMarkerFinder.getSeverity(scenario.eObject3), equalTo(IMarker.SEVERITY_INFO));
		assertThat(modelProblemMarkerFinder.getSeverity(scenario.eObject6), equalTo(IMarker.SEVERITY_ERROR));
	}

	private class TransientItemProviderProblemMarkerFinderScenario extends EObjectProblemMarkerFinderScenario {

		public TransientItemProvider transientItemProvider0;
		public TransientItemProvider transientItemProvider1;
		public TransientItemProvider transientItemProvider2;
		public TransientItemProvider transientItemProvider3;
		public TransientItemProvider transientItemProvider6;

		public TransientItemProviderProblemMarkerFinderScenario() throws CoreException {
			super();

			transientItemProvider0 = new TransientItemProvider(null);
			eObject0.eAdapters().add(transientItemProvider0);
			transientItemProvider1 = new TransientItemProvider(null);
			eObject1.eAdapters().add(transientItemProvider1);
			transientItemProvider2 = new TransientItemProvider(null);
			eObject2.eAdapters().add(transientItemProvider2);
			transientItemProvider3 = new TransientItemProvider(null);
			eObject3.eAdapters().add(transientItemProvider3);
			transientItemProvider6 = new TransientItemProvider(null);
			eObject6.eAdapters().add(transientItemProvider6);
		}
	}

	@Test
	public void testGetProblemMarkersForTransientItemProvider() throws Exception {
		TransientItemProviderProblemMarkerFinderScenario scenario = new TransientItemProviderProblemMarkerFinderScenario();
		TestableModelProblemMarkerFinder modelProblemMarkerFinder = new TestableModelProblemMarkerFinder(scenario);

		Collection<IMarker> problemMarkers = modelProblemMarkerFinder.getProblemMarkers(scenario.transientItemProvider0);
		assertThat(problemMarkers, notNullValue());
		assertThat(problemMarkers.size(), equalTo(0));

		problemMarkers = modelProblemMarkerFinder.getProblemMarkers(scenario.transientItemProvider1);
		assertThat(problemMarkers, notNullValue());
		assertThat(problemMarkers.size(), equalTo(0));

		problemMarkers = modelProblemMarkerFinder.getProblemMarkers(scenario.transientItemProvider2);
		assertThat(problemMarkers, notNullValue());
		assertThat(problemMarkers.size(), equalTo(0));

		problemMarkers = modelProblemMarkerFinder.getProblemMarkers(scenario.transientItemProvider3);
		assertThat(problemMarkers, notNullValue());
		assertThat(problemMarkers.size(), equalTo(0));

		problemMarkers = modelProblemMarkerFinder.getProblemMarkers(scenario.transientItemProvider6);
		assertThat(problemMarkers, notNullValue());
		assertThat(problemMarkers.size(), equalTo(0));
	}

	@Test
	public void testGetSeverityOfTransientItemProvider() throws Exception {
		TransientItemProviderProblemMarkerFinderScenario scenario = new TransientItemProviderProblemMarkerFinderScenario();
		TestableModelProblemMarkerFinder modelProblemMarkerFinder = new TestableModelProblemMarkerFinder(scenario);

		assertThat(modelProblemMarkerFinder.getSeverity(scenario.transientItemProvider0), equalTo(-1));
		assertThat(modelProblemMarkerFinder.getSeverity(scenario.transientItemProvider1), equalTo(-1));
		assertThat(modelProblemMarkerFinder.getSeverity(scenario.transientItemProvider2), equalTo(-1));
		assertThat(modelProblemMarkerFinder.getSeverity(scenario.transientItemProvider3), equalTo(-1));
		assertThat(modelProblemMarkerFinder.getSeverity(scenario.transientItemProvider6), equalTo(-1));
	}

	private class WrapperItemProviderProblemMarkerFinderScenario extends EObjectProblemMarkerFinderScenario {

		public IWrapperItemProvider wrapperItemProvider0;
		public IWrapperItemProvider wrapperItemProvider1;
		public IWrapperItemProvider wrapperItemProvider2;
		public IWrapperItemProvider wrapperItemProvider3;
		public IWrapperItemProvider wrapperItemProvider6;

		public WrapperItemProviderProblemMarkerFinderScenario() throws CoreException {
			super();

			wrapperItemProvider0 = new WrapperItemProvider(eObject0, null, null, -1, null);
			wrapperItemProvider1 = new WrapperItemProvider(eObject1, null, null, -1, null);
			wrapperItemProvider2 = new WrapperItemProvider(eObject2, null, null, -1, null);
			wrapperItemProvider3 = new WrapperItemProvider(eObject3, null, null, -1, null);
			wrapperItemProvider6 = new WrapperItemProvider(eObject6, null, null, -1, null);
		}
	}

	@Test
	public void testGetProblemMarkersForWrapperItemProvider() throws Exception {
		WrapperItemProviderProblemMarkerFinderScenario scenario = new WrapperItemProviderProblemMarkerFinderScenario();
		TestableModelProblemMarkerFinder modelProblemMarkerFinder = new TestableModelProblemMarkerFinder(scenario);

		Collection<IMarker> problemMarkers = modelProblemMarkerFinder.getProblemMarkers(scenario.wrapperItemProvider0);
		assertThat(problemMarkers, notNullValue());
		assertThat(problemMarkers.size(), equalTo(0));

		problemMarkers = modelProblemMarkerFinder.getProblemMarkers(scenario.wrapperItemProvider1);
		assertThat(problemMarkers, notNullValue());
		assertThat(problemMarkers.size(), equalTo(1));
		assertThat(count(problemMarkers, IMarker.SEVERITY_ERROR), equalTo(1));

		problemMarkers = modelProblemMarkerFinder.getProblemMarkers(scenario.wrapperItemProvider2);
		assertThat(problemMarkers, notNullValue());
		assertThat(problemMarkers.size(), equalTo(2));
		assertThat(count(problemMarkers, IMarker.SEVERITY_WARNING), equalTo(2));

		problemMarkers = modelProblemMarkerFinder.getProblemMarkers(scenario.wrapperItemProvider3);
		assertThat(problemMarkers, notNullValue());
		assertThat(problemMarkers.size(), equalTo(3));
		assertThat(count(problemMarkers, IMarker.SEVERITY_INFO), equalTo(3));

		problemMarkers = modelProblemMarkerFinder.getProblemMarkers(scenario.wrapperItemProvider6);
		assertThat(problemMarkers, notNullValue());
		assertThat(problemMarkers.size(), equalTo(6));
		assertThat(count(problemMarkers, IMarker.SEVERITY_ERROR), equalTo(1));
		assertThat(count(problemMarkers, IMarker.SEVERITY_WARNING), equalTo(2));
		assertThat(count(problemMarkers, IMarker.SEVERITY_INFO), equalTo(3));
	}

	@Test
	public void testGetSeverityOfWrapperItemProvider() throws Exception {
		WrapperItemProviderProblemMarkerFinderScenario scenario = new WrapperItemProviderProblemMarkerFinderScenario();
		TestableModelProblemMarkerFinder modelProblemMarkerFinder = new TestableModelProblemMarkerFinder(scenario);

		assertThat(modelProblemMarkerFinder.getSeverity(scenario.wrapperItemProvider0), equalTo(-1));
		assertThat(modelProblemMarkerFinder.getSeverity(scenario.wrapperItemProvider1), equalTo(IMarker.SEVERITY_ERROR));
		assertThat(modelProblemMarkerFinder.getSeverity(scenario.wrapperItemProvider2), equalTo(IMarker.SEVERITY_WARNING));
		assertThat(modelProblemMarkerFinder.getSeverity(scenario.wrapperItemProvider3), equalTo(IMarker.SEVERITY_INFO));
		assertThat(modelProblemMarkerFinder.getSeverity(scenario.wrapperItemProvider6), equalTo(IMarker.SEVERITY_ERROR));
	}
}

/**
 * <copyright>
 *
 * Copyright (c) 2013 BMW Car IT, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     BMW Car IT - Initial API and implementation
 *     itemis - [409510] Enable resource scope-sensitive proxy resolutions without forcing metamodel implementations to subclass EObjectImpl
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.ecore.proxymanagement;

import static org.eclipse.sphinx.emf.util.EObjectUtil.proxify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sphinx.emf.ecore.proxymanagement.ProxyResolutionBehavior;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.resource.ScopingResourceSetImpl;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.junit.Before;
import org.junit.Test;

public class ProxyResolutionBehaviorTest {

	private ProxyResolutionBehavior fBehaviorUT = ProxyResolutionBehavior.INSTANCE;

	private MockResourceSetImpl fResourceSet;

	private MockScopingResourceSetImpl fScopingResourceSet;

	private EObject fProxyToResolve;

	@Before
	public void clearResourceSets() {
		fResourceSet = new MockResourceSetImpl();
		fScopingResourceSet = new MockScopingResourceSetImpl();
		fProxyToResolve = proxify(TypeModel20Factory.eINSTANCE.createInterface());
	}

	@Test
	public void shouldReturnProxyIfResolutionIsNotPossibleInResourceSet() throws Exception {
		EObject resolvedEObject = fBehaviorUT.eResolveProxy(fResourceSet.getRoot(), fProxyToResolve);
		assertThat(resolvedEObject, is(fProxyToResolve));
	}

	@Test
	public void shouldReturnProxyIfResolutionIsNotPossibleInScopingResourceSet() throws Exception {
		EObject resolvedEObject = fBehaviorUT.eResolveProxy(fScopingResourceSet.getRoot(), fProxyToResolve);
		assertThat(resolvedEObject, is(fProxyToResolve));
	}

	@Test
	public void shouldReturnResolvedEObjectForResourceSet() throws Exception {
		EObject expectedResolvedEObject = TypeModel20Factory.eINSTANCE.createInterface();
		fResourceSet.setExpectedResolvedEObject(expectedResolvedEObject);
		EObject resolvedEObject = fBehaviorUT.eResolveProxy(fResourceSet.getRoot(), fProxyToResolve);
		assertThat(resolvedEObject, is(expectedResolvedEObject));
	}

	@Test
	public void shouldReturnResolvedEObjectForScopingResourceSet() throws Exception {
		EObject expectedResolvedEObject = TypeModel20Factory.eINSTANCE.createInterface();
		fScopingResourceSet.setExpectedResolvedEObject(expectedResolvedEObject);
		EObject resolvedEObject = fBehaviorUT.eResolveProxy(fScopingResourceSet.getRoot(), fProxyToResolve);
		assertThat(resolvedEObject, is(expectedResolvedEObject));
	}

	@Test
	public void shouldReturnProxyIfCalledOnResouceNotContainedInAResourceSet() {
		EObject resolvedEObject = fBehaviorUT.eResolveProxy(TypeModel20Factory.eINSTANCE.createInterface(), fProxyToResolve);
		assertThat(resolvedEObject, is(fProxyToResolve));
	}

	/**
	 * A ResourceSet which only contains one Resource containing only one root element.
	 */
	private static class MockResourceSetImpl extends ResourceSetImpl {

		private EObject fExpectedResolvedEObject;

		public MockResourceSetImpl() {
			getResources().add(new MockHummingbird20Resource());
		}

		public EObject getRoot() {
			return getResource().getRoot();
		}

		private MockHummingbird20Resource getResource() {
			return (MockHummingbird20Resource) getResources().get(0);
		}

		public void setExpectedResolvedEObject(EObject expectedResolvedEObject) {
			fExpectedResolvedEObject = expectedResolvedEObject;
		}

		@Override
		public EObject getEObject(URI uri, boolean loadOnDemand) {
			return fExpectedResolvedEObject;
		}

	}

	/**
	 * A ScopingResourceSet which only contains one Resource containing only one root element.
	 */
	private static class MockScopingResourceSetImpl extends ScopingResourceSetImpl {

		private EObject fExpectedResolvedEObject;

		public MockScopingResourceSetImpl() {
			getResources().add(new MockHummingbird20Resource());
		}

		public EObject getRoot() {
			return getResource().getRoot();
		}

		private MockHummingbird20Resource getResource() {
			return (MockHummingbird20Resource) getResources().get(0);
		}

		public void setExpectedResolvedEObject(EObject expectedResolvedEObject) {
			fExpectedResolvedEObject = expectedResolvedEObject;
		}

		@Override
		protected EObject getEObject(URI uri, IMetaModelDescriptor metaModelDescriptor, Object contextObject, boolean loadOnDemand) {
			return fExpectedResolvedEObject;
		}

		@Override
		protected List<Resource> getResourcesInScope(Object contextObject, boolean includeReferencedScopes, boolean ignoreMetaModel) {
			return getResources();
		}

	}

	/**
	 * A simple Resource for testing which only contains one Interface as its root element.
	 */
	private static class MockHummingbird20Resource extends ResourceImpl {

		public EObject getRoot() {
			if (getContents().isEmpty()) {
				getContents().add(TypeModel20Factory.eINSTANCE.createInterface());
			}
			return getContents().get(0);
		}

		@Override
		public URI getURI() {
			return URI.createFileURI(""); //$NON-NLS-1$
		}

	}

}

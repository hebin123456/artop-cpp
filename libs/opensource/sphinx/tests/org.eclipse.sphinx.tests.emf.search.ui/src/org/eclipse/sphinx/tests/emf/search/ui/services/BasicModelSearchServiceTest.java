/**
 * <copyright>
 *
 * Copyright (c) 2021 Elektrobit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     Elektrobit - [575391] BasicModelSearchService now supports the use of wildcards and regular expressions
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.search.ui.services;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.search.ui.ModelSearchMatch;
import org.eclipse.sphinx.emf.search.ui.QuerySpecification;
import org.eclipse.sphinx.emf.search.ui.services.BasicModelSearchService;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.tests.emf.search.ui.internal.mock.BasicModelSearchServiceMock;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests {@link BasicModelSearchService}
 */
public final class BasicModelSearchServiceTest {

	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz"; //$NON-NLS-1$
	BasicModelSearchService searchService;

	@Before
	public void setUp() throws Exception {
		IMetaModelDescriptor descriptor = Hummingbird20MMDescriptor.INSTANCE;
		List<IMetaModelDescriptor> descriptors = new ArrayList<>();
		descriptors.add(descriptor);
		searchService = new BasicModelSearchServiceMock(descriptors);
	}

	@Test
	public void testGetMatchesMatchCaseSensitiveFail1() {
		List<ModelSearchMatch> matches = getMatches("Abc", ALPHABET, true); //$NON-NLS-1$
		assertTrue(matches.isEmpty());
	}

	@Test
	public void testGetMatchesMatchCaseSensitiveFail2() {
		List<ModelSearchMatch> matches = getMatches("A*c", ALPHABET, true); //$NON-NLS-1$
		assertTrue(matches.isEmpty());
	}

	@Test
	public void testGetMatchesMatchCaseSensitiveSuccess1() {
		List<ModelSearchMatch> matches = getMatches("abc", ALPHABET, true); //$NON-NLS-1$
		assertFalse(matches.isEmpty());
	}

	@Test
	public void testGetMatchesMatchCaseSensitiveSuccess2() {
		List<ModelSearchMatch> matches = getMatches("a*defgh", ALPHABET, true); //$NON-NLS-1$
		assertFalse(matches.isEmpty());
	}

	@Test
	public void testGetMatchesMatchCaseInsensitiveFail1() {
		List<ModelSearchMatch> matches = getMatches("y*a", ALPHABET.toUpperCase(), false); //$NON-NLS-1$
		assertTrue(matches.isEmpty());
	}

	@Test
	public void testGetMatchesMatchCaseInsensitiveFail2() {
		List<ModelSearchMatch> matches = getMatches("A?bc", ALPHABET.toUpperCase(), false); //$NON-NLS-1$
		assertTrue(matches.isEmpty());
	}

	@Test
	public void testGetMatchesMatchCaseInsensitiveSuccess1() {
		List<ModelSearchMatch> matches = getMatches("efg*xyz", ALPHABET.toUpperCase(), false); //$NON-NLS-1$
		assertFalse(matches.isEmpty());
	}

	@Test
	public void testGetMatchesMatchCaseInsensitiveSuccess2() {
		List<ModelSearchMatch> matches = getMatches("a?c", ALPHABET.toUpperCase(), false); //$NON-NLS-1$
		assertFalse(matches.isEmpty());
	}

	private List<ModelSearchMatch> getMatches(String searchExpression, String stringToCheck, boolean isCaseSensitive) {
		// create pattern
		Set<IProject> projects = new HashSet<>();
		QuerySpecification pattern = new QuerySpecification(searchExpression, isCaseSensitive, projects);

		// get mocked resource
		List<Resource> resources = new ArrayList<>();
		Resource resource = getResourceMock(stringToCheck);
		resources.add(resource);

		// run and return getMatches method
		return searchService.getMatches(resources, pattern);
	}

	private Resource getResourceMock(Object stringToCheck) {
		// define object to be returned
		EObject eObjMock = createNiceMock(EObject.class);
		expect(eObjMock.eGet(EasyMock.anyObject())).andReturn(stringToCheck);
		replay(eObjMock);

		// define treeIterator behavior
		@SuppressWarnings("unchecked")
		TreeIterator<EObject> treeIterator = createNiceMock(TreeIterator.class);
		expect(treeIterator.hasNext()).andReturn(true);
		expect(treeIterator.next()).andReturn(eObjMock);
		replay(treeIterator);

		// set resource behavior
		Resource resourceMock = createNiceMock(Resource.class);
		expect(resourceMock.getAllContents()).andReturn(treeIterator);
		replay(resourceMock);
		return resourceMock;
	}
}

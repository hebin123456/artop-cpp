/**
 * <copyright>
 *
 * Copyright (c) 2016 itemis and others.
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
package org.eclipse.sphinx.tests.emf.util;

import static org.eclipse.sphinx.emf.util.URIExtensions.asPrefix;
import static org.eclipse.sphinx.emf.util.URIExtensions.getFragment;
import static org.eclipse.sphinx.emf.util.URIExtensions.replaceBaseURI;
import static org.eclipse.sphinx.emf.util.URIExtensions.replaceLastFragmentSegment;
import static org.eclipse.sphinx.emf.util.URIExtensions.replaceLastSegment;
import static org.eclipse.sphinx.emf.util.URIExtensions.substituteFragment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.emf.common.util.URI;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SuppressWarnings("nls")
public class URIExtensionsTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	public void testGetFragment_exception() {
		expectedException.expect(AssertionFailedException.class);
		getFragment(null);
	}

	@Test
	public void testGetFragment_nullResult() {
		URI uri = URI.createURI("");
		assertNull(getFragment(uri));

		uri = URI.createURI("platform:/resource/myProject/myResource");
		assertNull(getFragment(uri));

		uri = URI.createURI("platform:/resource/myProject/myResource?myQueryString");
		assertNull(getFragment(uri));
	}

	@Test
	public void testGetFragment_meaningfulResult() {
		// Normal fragment root segment
		URI uri = URI.createURI("platform:/resource/myProject/myResource#/myRootContainerObject/myContentObject");
		URI actualFragment = getFragment(uri);
		assertEquals("/myRootContainerObject/myContentObject", actualFragment.path());
		assertNull(actualFragment.query());
		assertNull(actualFragment.fragment());

		uri = URI.createURI("platform:/resource/myProject/myResource?myQuery#/myRootContainerObject/myContentObject");
		actualFragment = getFragment(uri);
		assertEquals("/myRootContainerObject/myContentObject", actualFragment.path());
		assertNull(actualFragment.query());
		assertNull(actualFragment.fragment());

		uri = URI.createURI("platform:/resource/myProject/myResource#/myRootContainerObject/myContentObject?myFragmentQuery");
		actualFragment = getFragment(uri);
		assertEquals("/myRootContainerObject/myContentObject", actualFragment.path());
		assertEquals("myFragmentQuery", actualFragment.query());
		assertNull(actualFragment.fragment());

		uri = URI.createURI("platform:/resource/myProject/myResource?myQuery#/myRootContainerObject/myContentObject?myFragmentQuery");
		actualFragment = getFragment(uri);
		assertEquals("/myRootContainerObject/myContentObject", actualFragment.path());
		assertEquals("myFragmentQuery", actualFragment.query());
		assertNull(actualFragment.fragment());

		// Empty fragment root segment
		uri = URI.createURI("platform:/resource/myProject/myResource#//myContentObject");
		actualFragment = getFragment(uri);
		assertEquals("//myContentObject", actualFragment.path());
		assertNull(actualFragment.query());
		assertNull(actualFragment.fragment());

		uri = URI.createURI("platform:/resource/myProject/myResource?myQuery#//myContentObject");
		actualFragment = getFragment(uri);
		assertEquals("//myContentObject", actualFragment.path());
		assertNull(actualFragment.query());
		assertNull(actualFragment.fragment());

		uri = URI.createURI("platform:/resource/myProject/myResource#//myContentObject?myFragmentQuery");
		actualFragment = getFragment(uri);
		assertEquals("//myContentObject", actualFragment.path());
		assertEquals("myFragmentQuery", actualFragment.query());
		assertNull(actualFragment.fragment());

		uri = URI.createURI("platform:/resource/myProject/myResource?myQuery#//myContentObject?myFragmentQuery");
		actualFragment = getFragment(uri);
		assertEquals("//myContentObject", actualFragment.path());
		assertEquals("myFragmentQuery", actualFragment.query());
		assertNull(actualFragment.fragment());
	}

	@Test
	public void testSubstituteFragment_exception() {
		expectedException.expect(AssertionFailedException.class);
		substituteFragment(null, null);

		expectedException.expect(AssertionFailedException.class);
		substituteFragment(null, URI.createURI(""));
	}

	@Test
	public void testSubstituteFragment_unchangedResult() {
		URI uri = URI.createURI("");
		URI fragment = null;
		assertEquals(uri, substituteFragment(uri, fragment));

		uri = URI.createURI("platform:/resource/myProject/myResource");
		fragment = null;
		assertEquals(uri, substituteFragment(uri, fragment));

		uri = URI.createURI("platform:/resource/myProject/myResource?myQuery");
		fragment = null;
		assertEquals(uri, substituteFragment(uri, fragment));

		uri = URI.createURI("#myFragment");
		fragment = URI.createURI("myFragment");
		assertEquals(uri, substituteFragment(uri, fragment));

		uri = URI.createURI("platform:/resource/myProject/myResource#myFragment");
		fragment = URI.createURI("myFragment");
		assertEquals(uri, substituteFragment(uri, fragment));

		uri = URI.createURI("platform:/resource/myProject/myResource?myQuery#myFragment");
		fragment = URI.createURI("myFragment");
		assertEquals(uri, substituteFragment(uri, fragment));
	}

	@Test
	public void testSubstituteFragment_meaningfulResult() {
		// Without path, without query
		URI uri = URI.createURI("");
		URI fragment = URI.createURI("");
		URI expectedURI = URI.createURI("#");
		assertEquals(expectedURI, substituteFragment(uri, fragment));

		uri = URI.createURI("#");
		fragment = null;
		expectedURI = URI.createURI("");
		assertEquals(expectedURI, substituteFragment(uri, fragment));

		uri = URI.createURI("");
		fragment = URI.createURI("myFragment");
		expectedURI = URI.createURI("#myFragment");
		assertEquals(expectedURI, substituteFragment(uri, fragment));

		uri = URI.createURI("#myFragment");
		fragment = null;
		expectedURI = URI.createURI("");
		assertEquals(expectedURI, substituteFragment(uri, fragment));

		// With path, without query
		uri = URI.createURI("platform:/resource/myProject/myResource");
		fragment = URI.createURI("");
		expectedURI = URI.createURI("platform:/resource/myProject/myResource#");
		assertEquals(expectedURI, substituteFragment(uri, fragment));

		uri = URI.createURI("platform:/resource/myProject/myResource#");
		fragment = null;
		expectedURI = URI.createURI("platform:/resource/myProject/myResource");
		assertEquals(expectedURI, substituteFragment(uri, fragment));

		uri = URI.createURI("platform:/resource/myProject/myResource");
		fragment = URI.createURI("myFragment");
		expectedURI = URI.createURI("platform:/resource/myProject/myResource#myFragment");
		assertEquals(expectedURI, substituteFragment(uri, fragment));

		uri = URI.createURI("platform:/resource/myProject/myResource#myFragment");
		fragment = null;
		expectedURI = URI.createURI("platform:/resource/myProject/myResource");
		assertEquals(expectedURI, substituteFragment(uri, fragment));

		// With path and query
		uri = URI.createURI("platform:/resource/myProject/myResource?myQuery");
		fragment = URI.createURI("");
		expectedURI = URI.createURI("platform:/resource/myProject/myResource?myQuery#");
		assertEquals(expectedURI, substituteFragment(uri, fragment));

		uri = URI.createURI("platform:/resource/myProject/myResource?myQuery#");
		fragment = null;
		expectedURI = URI.createURI("platform:/resource/myProject/myResource?myQuery");
		assertEquals(expectedURI, substituteFragment(uri, fragment));

		uri = URI.createURI("platform:/resource/myProject/myResource?myQuery");
		fragment = URI.createURI("myFragment");
		expectedURI = URI.createURI("platform:/resource/myProject/myResource?myQuery#myFragment");
		assertEquals(expectedURI, substituteFragment(uri, fragment));

		uri = URI.createURI("platform:/resource/myProject/myResource?myQuery#myFragment");
		fragment = null;
		expectedURI = URI.createURI("platform:/resource/myProject/myResource?myQuery");
		assertEquals(expectedURI, substituteFragment(uri, fragment));
	}

	public void testAsPrefix_exception() {
		expectedException.expect(AssertionFailedException.class);
		asPrefix(null);
	}

	@Test
	public void testAsPrefix_unchangedResult() {
		URI uri = URI.createURI("/");
		assertEquals(uri, asPrefix(uri));
	
		uri = URI.createURI("platform:/resource/myProject/");
		assertEquals(uri, asPrefix(uri));
	}

	@Test
	public void testAsPrefix_meaningfulResult() {
		URI uri = URI.createURI("platform:/resource/myProject");
		URI expectedPrefix = URI.createURI("platform:/resource/myProject/");
		assertEquals(expectedPrefix, asPrefix(uri));
	
		uri = URI.createURI("platform:/resource/myProject?myQuery");
		expectedPrefix = URI.createURI("platform:/resource/myProject/");
		assertEquals(expectedPrefix, asPrefix(uri));
	
		uri = URI.createURI("platform:/resource/myProject/?myQuery");
		expectedPrefix = URI.createURI("platform:/resource/myProject/");
		assertEquals(expectedPrefix, asPrefix(uri));
	
		uri = URI.createURI("platform:/resource/myProject#myFragment");
		expectedPrefix = URI.createURI("platform:/resource/myProject/");
		assertEquals(expectedPrefix, asPrefix(uri));
	
		uri = URI.createURI("platform:/resource/myProject/#myFragment");
		expectedPrefix = URI.createURI("platform:/resource/myProject/");
		assertEquals(expectedPrefix, asPrefix(uri));
	
		uri = URI.createURI("platform:/resource/myProject?myQuery#myFragment");
		expectedPrefix = URI.createURI("platform:/resource/myProject/");
		assertEquals(expectedPrefix, asPrefix(uri));
	
		uri = URI.createURI("platform:/resource/myProject/?myQuery#myFragment");
		expectedPrefix = URI.createURI("platform:/resource/myProject/");
		assertEquals(expectedPrefix, asPrefix(uri));
	}

	@Test
	public void testReplaceLastSegment_exception() {
		// Null URI
		expectedException.expect(AssertionFailedException.class);
		replaceLastSegment(null, null, null);

		expectedException.expect(AssertionFailedException.class);
		replaceLastSegment(null, "", "");

		expectedException.expect(AssertionFailedException.class);
		replaceLastSegment(null, "old", "new");

		// Empty URI
		expectedException.expect(IllegalArgumentException.class);
		replaceLastSegment(URI.createURI(""), null, null);

		expectedException.expect(IllegalArgumentException.class);
		replaceLastSegment(URI.createURI(""), "", "");

		expectedException.expect(IllegalArgumentException.class);
		replaceLastSegment(URI.createURI(""), "old", "new");

		// Authority-only URI
		expectedException.expect(IllegalArgumentException.class);
		replaceLastSegment(URI.createURI("//authorityButNotPath"), null, null);

		expectedException.expect(IllegalArgumentException.class);
		replaceLastSegment(URI.createURI("//authorityButNotPath"), "", "");

		expectedException.expect(IllegalArgumentException.class);
		replaceLastSegment(URI.createURI("//authorityButNotPath"), "old", "new");
	}

	@Test
	public void testReplaceLastSegment_nullResult() {
		URI uri = URI.createURI("platform:/resource/myProject/myOldResource");
		assertNull(replaceLastSegment(uri, null, null));

		uri = URI.createURI("platform:/resource/myProject/myOldResource");
		assertNull(replaceLastSegment(uri, "", ""));

		uri = URI.createURI("platform:/resource/myProject/myOldResource");
		assertNull(replaceLastSegment(uri, "oldResource", "newResource"));
	}

	@Test
	public void testReplaceLastSegment_meaningfulResult() {
		URI uri = URI.createURI("platform:/resource/myProject/myOldResource");
		URI expectedURI = URI.createURI("platform:/resource/myProject/myNewResource");
		assertEquals(expectedURI, replaceLastSegment(uri, "myOldResource", "myNewResource"));

		uri = URI.createURI("platform:/resource/myProject/myOldResource#myFragment");
		expectedURI = URI.createURI("platform:/resource/myProject/myNewResource#myFragment");
		assertEquals(expectedURI, replaceLastSegment(uri, "myOldResource", "myNewResource"));

		uri = URI.createURI("platform:/resource/myProject/myOldResource?myQuery");
		expectedURI = URI.createURI("platform:/resource/myProject/myNewResource?myQuery");
		assertEquals(expectedURI, replaceLastSegment(uri, "myOldResource", "myNewResource"));

		uri = URI.createURI("platform:/resource/myProject/myOldResource?myQuery#myFragment");
		expectedURI = URI.createURI("platform:/resource/myProject/myNewResource?myQuery#myFragment");
		assertEquals(expectedURI, replaceLastSegment(uri, "myOldResource", "myNewResource"));
	}

	@Test
	public void testReplaceBaseURI_exception() {
		expectedException.expect(AssertionFailedException.class);
		replaceBaseURI(null, null, null);

		expectedException.expect(AssertionFailedException.class);
		replaceBaseURI(URI.createURI(""), null, null);

		expectedException.expect(AssertionFailedException.class);
		replaceBaseURI(null, URI.createURI(""), null);

		expectedException.expect(AssertionFailedException.class);
		replaceBaseURI(null, null, URI.createURI(""));

		expectedException.expect(AssertionFailedException.class);
		replaceBaseURI(URI.createURI(""), URI.createURI(""), null);

		expectedException.expect(AssertionFailedException.class);
		replaceBaseURI(URI.createURI(""), null, URI.createURI(""));

		expectedException.expect(AssertionFailedException.class);
		replaceBaseURI(null, URI.createURI(""), URI.createURI(""));
	}

	@Test
	public void testReplaceBaseURI_nullResult() {
		// Old base URI' path not matching
		URI uri = URI.createURI("platform:/resource/myProject/myOldResource#/myRootContainerObject/myOldContainerObject/myContentObject");
		URI oldBaseURI = URI.createURI("#/myRootContainerObject/myOldContainerObject/myContentObject");
		URI newBaseURI = URI.createURI("#/myRootContainerObject/myNewContainerObject");
		assertNull(replaceBaseURI(uri, oldBaseURI, newBaseURI));

		uri = URI.createURI("platform:/resource/myProject/myOldResource#/myRootContainerObject/myOldContainerObject/myContentObject");
		oldBaseURI = URI.createURI("platform:/resource/myProject/oldResource#/myRootContainerObject/myOldContainerObject");
		newBaseURI = URI.createURI("platform:/resource/myProject/newResource#/myRootContainerObject/myNewContainerObject");
		assertNull(replaceBaseURI(uri, oldBaseURI, newBaseURI));

		// Old base URI' query not matching
		uri = URI.createURI("platform:/resource/myProject/myOldResource?myQuery#/myRootContainerObject/myOldContainerObject/myContentObject");
		oldBaseURI = URI.createURI("platform:/resource/myProject/myOldResource#/myRootContainerObject/myOldContainerObject");
		newBaseURI = URI.createURI("platform:/resource/myProject/myNewResource#/myRootContainerObject/myNewContainerObject");
		assertNull(replaceBaseURI(uri, oldBaseURI, newBaseURI));

		uri = URI.createURI("platform:/resource/myProject/myOldResource?myQuery#/myRootContainerObject/myOldContainerObject/myContentObject");
		oldBaseURI = URI.createURI("platform:/resource/myProject/myOldResource?query#/myRootContainerObject/myOldContainerObject");
		newBaseURI = URI.createURI("platform:/resource/myProject/myNewResource?query#/myRootContainerObject/myNewContainerObject");
		assertNull(replaceBaseURI(uri, oldBaseURI, newBaseURI));

		// Old base URI' fragment not matching
		uri = URI.createURI("platform:/resource/myProject/myOldResource#/myRootContainerObject/myOldContainerObject/myContentObject");
		oldBaseURI = URI.createURI("platform:/resource/myProject/myOldResource#/rootContainerObject/myOldContainerObject");
		newBaseURI = URI.createURI("platform:/resource/myProject/myNewResource#/rootContainerObject/myNewContainerObject");
		assertNull(replaceBaseURI(uri, oldBaseURI, newBaseURI));

		uri = URI.createURI("platform:/resource/myProject/myOldResource#/myRootContainerObject/myOldContainerObject/myContentObject");
		oldBaseURI = URI.createURI("platform:/resource/myProject/myOldResource#/myRootContainerObject/oldContainerObject");
		newBaseURI = URI.createURI("platform:/resource/myProject/myNewResource#/myRootContainerObject/newContainerObject");
		assertNull(replaceBaseURI(uri, oldBaseURI, newBaseURI));
	}

	@Test
	public void testReplaceBaseURI_meaningfulResult() {
		// Normal fragment root segment
		URI uri = URI.createURI("platform:/resource/myProject/myOldResource#/myRootContainerObject/myOldContainerObject/myContentObject");
		URI oldBaseURI = URI.createURI("platform:/resource/myProject/myOldResource#/myRootContainerObject/myOldContainerObject");
		URI newBaseURI = URI.createURI("platform:/resource/myProject/myNewResource#/myRootContainerObject/myNewContainerObject");
		URI expectedURI = URI.createURI("platform:/resource/myProject/myNewResource#/myRootContainerObject/myNewContainerObject/myContentObject");
		assertEquals(expectedURI, replaceBaseURI(uri, oldBaseURI, newBaseURI));

		uri = URI.createURI("platform:/resource/myProject/myOldResource#/myRootContainerObject/myOldContainerObject/myContentObject?myFragmentQuery");
		oldBaseURI = URI.createURI("platform:/resource/myProject/myOldResource#/myRootContainerObject/myOldContainerObject");
		newBaseURI = URI.createURI("platform:/resource/myProject/myNewResource#/myRootContainerObject/myNewContainerObject");
		expectedURI = URI
				.createURI("platform:/resource/myProject/myNewResource#/myRootContainerObject/myNewContainerObject/myContentObject?myFragmentQuery");
		assertEquals(expectedURI, replaceBaseURI(uri, oldBaseURI, newBaseURI));

		uri = URI.createURI("platform:/resource/myProject/myOldResource?myQuery#/myRootContainerObject/myOldContainerObject/myContentObject");
		oldBaseURI = URI.createURI("platform:/resource/myProject/myOldResource?myQuery#/myRootContainerObject/myOldContainerObject");
		newBaseURI = URI.createURI("platform:/resource/myProject/myNewResource?myQuery#/myRootContainerObject/myNewContainerObject");
		expectedURI = URI.createURI("platform:/resource/myProject/myNewResource?myQuery#/myRootContainerObject/myNewContainerObject/myContentObject");
		assertEquals(expectedURI, replaceBaseURI(uri, oldBaseURI, newBaseURI));

		uri = URI.createURI(
				"platform:/resource/myProject/myOldResource?myQuery#/myRootContainerObject/myOldContainerObject/myContentObject?myFragmentQuery");
		oldBaseURI = URI.createURI("platform:/resource/myProject/myOldResource?myQuery#/myRootContainerObject/myOldContainerObject");
		newBaseURI = URI.createURI("platform:/resource/myProject/myNewResource?myQuery#/myRootContainerObject/myNewContainerObject");
		expectedURI = URI.createURI(
				"platform:/resource/myProject/myNewResource?myQuery#/myRootContainerObject/myNewContainerObject/myContentObject?myFragmentQuery");
		assertEquals(expectedURI, replaceBaseURI(uri, oldBaseURI, newBaseURI));

		// Empty fragment root segment
		uri = URI.createURI("platform:/resource/myProject/myOldResource#//myOldContainerObject/myContentObject");
		oldBaseURI = URI.createURI("platform:/resource/myProject/myOldResource#//myOldContainerObject");
		newBaseURI = URI.createURI("platform:/resource/myProject/myNewResource#//myNewContainerObject");
		expectedURI = URI.createURI("platform:/resource/myProject/myNewResource#//myNewContainerObject/myContentObject");
		assertEquals(expectedURI, replaceBaseURI(uri, oldBaseURI, newBaseURI));

		uri = URI.createURI("platform:/resource/myProject/myOldResource#//myOldContainerObject/myContentObject?myFragmentQuery");
		oldBaseURI = URI.createURI("platform:/resource/myProject/myOldResource#//myOldContainerObject");
		newBaseURI = URI.createURI("platform:/resource/myProject/myNewResource#//myNewContainerObject");
		expectedURI = URI.createURI("platform:/resource/myProject/myNewResource#//myNewContainerObject/myContentObject?myFragmentQuery");
		assertEquals(expectedURI, replaceBaseURI(uri, oldBaseURI, newBaseURI));

		uri = URI.createURI("platform:/resource/myProject/myOldResource?myQuery#//myOldContainerObject/myContentObject");
		oldBaseURI = URI.createURI("platform:/resource/myProject/myOldResource?myQuery#//myOldContainerObject");
		newBaseURI = URI.createURI("platform:/resource/myProject/myNewResource?myQuery#//myNewContainerObject");
		expectedURI = URI.createURI("platform:/resource/myProject/myNewResource?myQuery#//myNewContainerObject/myContentObject");
		assertEquals(expectedURI, replaceBaseURI(uri, oldBaseURI, newBaseURI));

		uri = URI.createURI("platform:/resource/myProject/myOldResource?myQuery#//myOldContainerObject/myContentObject?myFragmentQuery");
		oldBaseURI = URI.createURI("platform:/resource/myProject/myOldResource?myQuery#//myOldContainerObject");
		newBaseURI = URI.createURI("platform:/resource/myProject/myNewResource?myQuery#//myNewContainerObject");
		expectedURI = URI.createURI("platform:/resource/myProject/myNewResource?myQuery#//myNewContainerObject/myContentObject?myFragmentQuery");
		assertEquals(expectedURI, replaceBaseURI(uri, oldBaseURI, newBaseURI));
	}

	@Test
	public void testReplaceLastFragmentSegment_exception() {
		expectedException.expect(AssertionFailedException.class);
		replaceLastFragmentSegment(null, null, null);

		expectedException.expect(AssertionFailedException.class);
		replaceLastFragmentSegment(null, "", "");

		expectedException.expect(AssertionFailedException.class);
		replaceLastFragmentSegment(null, "old", "new");
	}

	@Test
	public void testReplaceLastFragmentSegment_nullResult() {
		URI uri = URI.createURI("");
		assertNull(replaceLastFragmentSegment(uri, null, null));

		uri = URI.createURI("platform:/resource/myProject/myResource");
		assertNull(replaceLastFragmentSegment(uri, "", ""));

		uri = URI.createURI("platform:/resource/myProject/myResource?myQuery");
		assertNull(replaceLastFragmentSegment(uri, "old", "new"));
	}

	@Test
	public void testReplaceLastFragmentSegment_meaningfulResult() {
		// Normal fragment root segment
		URI uri = URI.createURI("platform:/resource/myProject/myResource#/myRootContainerObject/myOldContentObject");
		URI expectedURI = URI.createURI("platform:/resource/myProject/myResource#/myRootContainerObject/myNewContentObject");
		assertEquals(expectedURI, replaceLastFragmentSegment(uri, "myOldContentObject", "myNewContentObject"));

		uri = URI.createURI("platform:/resource/myProject/myResource#/myRootContainerObject/myOldContentObject?myFragmentQuery");
		expectedURI = URI.createURI("platform:/resource/myProject/myResource#/myRootContainerObject/myNewContentObject?myFragmentQuery");
		assertEquals(expectedURI, replaceLastFragmentSegment(uri, "myOldContentObject", "myNewContentObject"));

		uri = URI.createURI("platform:/resource/myProject/myResource?myQuery#/myRootContainerObject/myOldContentObject");
		expectedURI = URI.createURI("platform:/resource/myProject/myResource?myQuery#/myRootContainerObject/myNewContentObject");
		assertEquals(expectedURI, replaceLastFragmentSegment(uri, "myOldContentObject", "myNewContentObject"));

		uri = URI.createURI("platform:/resource/myProject/myResource?myQuery#/myRootContainerObject/myOldContentObject?myFragmentQuery");
		expectedURI = URI.createURI("platform:/resource/myProject/myResource?myQuery#/myRootContainerObject/myNewContentObject?myFragmentQuery");
		assertEquals(expectedURI, replaceLastFragmentSegment(uri, "myOldContentObject", "myNewContentObject"));

		// Empty fragment root segment
		uri = URI.createURI("platform:/resource/myProject/myResource#//myOldContentObject");
		expectedURI = URI.createURI("platform:/resource/myProject/myResource#//myNewContentObject");
		assertEquals(expectedURI, replaceLastFragmentSegment(uri, "myOldContentObject", "myNewContentObject"));

		uri = URI.createURI("platform:/resource/myProject/myResource#//myOldContentObject?myFragmentQuery");
		expectedURI = URI.createURI("platform:/resource/myProject/myResource#//myNewContentObject?myFragmentQuery");
		assertEquals(expectedURI, replaceLastFragmentSegment(uri, "myOldContentObject", "myNewContentObject"));

		uri = URI.createURI("platform:/resource/myProject/myResource?myQuery#//myOldContentObject");
		expectedURI = URI.createURI("platform:/resource/myProject/myResource?myQuery#//myNewContentObject");
		assertEquals(expectedURI, replaceLastFragmentSegment(uri, "myOldContentObject", "myNewContentObject"));

		uri = URI.createURI("platform:/resource/myProject/myResource?myQuery#//myOldContentObject?myFragmentQuery");
		expectedURI = URI.createURI("platform:/resource/myProject/myResource?myQuery#//myNewContentObject?myFragmentQuery");
		assertEquals(expectedURI, replaceLastFragmentSegment(uri, "myOldContentObject", "myNewContentObject"));
	}
}

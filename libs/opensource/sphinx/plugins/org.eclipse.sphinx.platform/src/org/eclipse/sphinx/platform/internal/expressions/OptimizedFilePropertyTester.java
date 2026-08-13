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
package org.eclipse.sphinx.platform.internal.expressions;

import org.eclipse.core.internal.propertytester.ResourcePropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

/**
 * A replacement for {@link org.eclipse.core.internal.propertytester.FilePropertyTester} providing an optimized
 * implementation for {@link #CONTENT_TYPE_ID} property tests which significantly increases runtime performance when
 * this kind of property test is applied to big numbers of files.
 * 
 * @since 0.7.0
 */
@SuppressWarnings("restriction")
public class OptimizedFilePropertyTester extends ResourcePropertyTester {

	/**
	 * A property indicating that we are looking to verify that the file matches the content type matching the given
	 * identifier. The identifier is provided as the expected value.
	 */
	protected static final String CONTENT_TYPE_ID = "contentTypeId"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.internal.resources.ResourcePropertyTester#test(java.lang.Object, java.lang.String,
	 * java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String method, Object[] args, Object expectedValue) {
		if (receiver instanceof IFile && CONTENT_TYPE_ID.equals(method)) {
			return testContentType((IFile) receiver, toString(expectedValue));
		}
		return false;
	}

	/**
	 * Tests whether the content type for <code>file</code> matches the <code>contentTypeId</code>. It is possible that
	 * this method call could cause the file to be read. It is also possible (through poor plug-in design) for this
	 * method to load plug-ins.
	 * 
	 * @param file
	 *            The file for which the content type should be determined; must not be <code>null</code>.
	 * @param contentTypeId
	 *            The expected content type; must not be <code>null</code>.
	 * @return <code>true</code> iff the best matching content type has an identifier that matches
	 *         <code>contentTypeId</code>; <code>false</code> otherwise.
	 */
	protected boolean testContentType(final IFile file, String contentTypeId) {
		String expectedValue = contentTypeId.trim();
		try {
			String actualValue = ExtendedPlatform.getContentTypeId(file);
			return expectedValue.equals(actualValue);
		} catch (Exception ex) {
			return false;
		}
	}
}

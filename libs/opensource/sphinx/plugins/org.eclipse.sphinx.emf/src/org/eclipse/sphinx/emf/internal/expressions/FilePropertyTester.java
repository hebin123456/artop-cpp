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
package org.eclipse.sphinx.emf.internal.expressions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.scoping.IResourceScopeProvider;
import org.eclipse.sphinx.emf.scoping.ResourceScopeProviderRegistry;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

/**
 * A property tester for files.
 * 
 * @since 0.7.0
 */
public class FilePropertyTester extends PropertyTester {

	/**
	 * A property indicating that a file is a model file inside a valid resource scope.
	 */
	private static final String IS_IN_SCOPE = "isInScope"; //$NON-NLS-1$

	/**
	 * A property indicating that a file contains an instance of the metamodel matching specified id pattern.
	 */
	private static final String METAMODEL_ID_MATCHES = "metaModelIdMatches"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IFile) {
			if (IS_IN_SCOPE.equals(property)) {
				return testInScope((IFile) receiver, toBoolean(expectedValue));
			}
			if (METAMODEL_ID_MATCHES.equals(property)) {
				if (!ExtendedPlatform.isTeamPrivateResource((IFile) receiver, IResource.CHECK_ANCESTORS)) {
					return testMetamodelId((IFile) receiver, toString(expectedValue));
				}
			}
		}
		return false;
	}

	protected boolean testInScope(IFile receiver, boolean inScope) {
		boolean resourceScope = false;
		/*
		 * Performance optimization: Check if given file is a potential model file inside an existing scope. This helps
		 * excluding obvious non-model files and model files that are out of scope right away and avoids potentially
		 * lengthy but useless processing of the same.
		 */
		if (!ResourceScopeProviderRegistry.INSTANCE.isNotInAnyScope(receiver)) {
			IMetaModelDescriptor effectiveMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getEffectiveDescriptor(receiver);
			IResourceScopeProvider resourceScopeProvider = ResourceScopeProviderRegistry.INSTANCE.getResourceScopeProvider(effectiveMMDescriptor);
			if (resourceScopeProvider != null) {
				resourceScope = resourceScopeProvider.getScope(receiver) != null;
			}
		}
		return resourceScope == inScope;
	}

	protected boolean testMetamodelId(IFile receiver, String metaModelIdPattern) {
		Assert.isNotNull(metaModelIdPattern);
		metaModelIdPattern = metaModelIdPattern.trim();
		IMetaModelDescriptor descriptorFromFile = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(receiver);
		if (descriptorFromFile != null) {
			return descriptorFromFile.getIdentifier().matches(metaModelIdPattern);
		}
		return false;
	}

	/**
	 * Converts the given expected value to a boolean.
	 * 
	 * @param expectedValue
	 *            the expected value (may be <code>null</code>).
	 * @return <code>false</code> if the expected value equals Boolean.FALSE, <code>true</code> otherwise
	 */
	protected boolean toBoolean(Object expectedValue) {
		if (expectedValue instanceof Boolean) {
			return ((Boolean) expectedValue).booleanValue();
		}
		return true;
	}

	/**
	 * Converts the given expected value to a <code>String</code>.
	 * 
	 * @param expectedValue
	 *            the expected value (may be <code>null</code>).
	 * @return the empty string if the expected value is <code>null</code>, otherwise the <code>toString()</code>
	 *         representation of the expected value
	 */
	protected String toString(Object expectedValue) {
		return expectedValue == null ? "" : expectedValue.toString(); //$NON-NLS-1$
	}
}

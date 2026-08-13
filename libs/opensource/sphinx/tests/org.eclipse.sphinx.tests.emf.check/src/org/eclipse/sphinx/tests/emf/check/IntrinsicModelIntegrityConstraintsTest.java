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
package org.eclipse.sphinx.tests.emf.check;

import static org.eclipse.sphinx.tests.emf.check.internal.mocks.ValidatorContribution.testableHummingbird20ConnectionsCheckValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.sphinx.emf.check.ICheckValidator;
import org.eclipse.sphinx.emf.check.catalog.Catalog;
import org.eclipse.sphinx.tests.emf.check.internal.Activator;
import org.eclipse.sphinx.tests.emf.check.internal.TestableCheckValidatorRegistry;
import org.eclipse.sphinx.tests.emf.check.internal.mocks.CheckValidatorRegistryMockFactory;
import org.eclipse.sphinx.tests.emf.check.util.CheckValidationTestUtil;
import org.junit.Assert;
import org.junit.Test;

public class IntrinsicModelIntegrityConstraintsTest {

	private static CheckValidatorRegistryMockFactory mockFactory = new CheckValidatorRegistryMockFactory();

	@Test
	public void testIntrinsicModelIntegrityConstraintsEnabled() {
		EValidator.Registry eValidatorRegistry = new org.eclipse.emf.ecore.impl.EValidatorRegistryImpl();
		Diagnostician diagnostician = new Diagnostician(eValidatorRegistry);

		// Create Mock extension registry
		IExtensionRegistry extensionRegistry = mockFactory.createExtensionRegistryMock(Activator.getPlugin(),
				testableHummingbird20ConnectionsCheckValidator);
		TestableCheckValidatorRegistry checkValidatorRegistry = TestableCheckValidatorRegistry.INSTANCE;

		// Clear the registry
		checkValidatorRegistry.clear();
		// Set the created mock extension registry on the TestableCheckValidatorRegistry
		checkValidatorRegistry.setExtensionRegistry(extensionRegistry);
		// Set the locally create EValidator.Registry
		checkValidatorRegistry.setEValidatorRegistry(eValidatorRegistry);

		Collection<Catalog> checkCatalogs = checkValidatorRegistry.getCheckCatalogs();
		Assert.assertEquals(1, checkCatalogs.size());

		Map<Object, Object> contextEntries = new HashMap<Object, Object>();
		contextEntries.put(ICheckValidator.OPTION_ENABLE_INTRINSIC_MODEL_INTEGRITY_CONSTRAINTS, true);

		Diagnostic diagnostic = diagnostician.validate(CheckValidationTestUtil.createApplication("_myApp"), contextEntries); //$NON-NLS-1$
		Assert.assertEquals(1, diagnostic.getChildren().size());

		String errorMsg = "The feature 'components' of"; //$NON-NLS-1$
		Assert.assertEquals(1, CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(), errorMsg).size());
	}

	@Test
	public void testIntrinsicModelIntegrityConstraintsDisabled() {
		EValidator.Registry eValidatorRegistry = new org.eclipse.emf.ecore.impl.EValidatorRegistryImpl();
		Diagnostician diagnostician = new Diagnostician(eValidatorRegistry);

		// Create Mock extension registry
		IExtensionRegistry extensionRegistry = mockFactory.createExtensionRegistryMock(Activator.getPlugin(),
				testableHummingbird20ConnectionsCheckValidator);
		TestableCheckValidatorRegistry checkValidatorRegistry = TestableCheckValidatorRegistry.INSTANCE;

		// Clear the registry
		checkValidatorRegistry.clear();
		// Set the created mock extension registry on the TestableCheckValidatorRegistry
		checkValidatorRegistry.setExtensionRegistry(extensionRegistry);
		// Set the locally create EValidator.Registry
		checkValidatorRegistry.setEValidatorRegistry(eValidatorRegistry);

		Collection<Catalog> checkCatalogs = checkValidatorRegistry.getCheckCatalogs();
		Assert.assertEquals(1, checkCatalogs.size());

		Map<Object, Object> contextEntries = new HashMap<Object, Object>();
		contextEntries.put(ICheckValidator.OPTION_ENABLE_INTRINSIC_MODEL_INTEGRITY_CONSTRAINTS, false);

		Diagnostic diagnostic = diagnostician.validate(CheckValidationTestUtil.createApplication("_myApp"), contextEntries); //$NON-NLS-1$
		Assert.assertEquals(0, diagnostic.getChildren().size());
	}
}

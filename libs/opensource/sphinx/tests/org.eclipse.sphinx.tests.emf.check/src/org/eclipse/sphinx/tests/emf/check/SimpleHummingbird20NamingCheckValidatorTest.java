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
 *     itemis - [473260] Progress indication of check framework
 *     itemis - [473261] Check Validation: Cancel button unresponsive
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.check;

import static org.eclipse.sphinx.examples.hummingbird20.check.simple.SimpleHummingbird20NamingCheckValidator.ISSUE_MSG;
import static org.eclipse.sphinx.tests.emf.check.internal.mocks.ValidatorContribution.testableSimpleHummingbird20NamingCheckValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.sphinx.emf.check.ICheckValidator;
import org.eclipse.sphinx.emf.check.catalog.Catalog;
import org.eclipse.sphinx.emf.check.internal.CheckMethodWrapper;
import org.eclipse.sphinx.examples.hummingbird20.check.simple.SimpleHummingbird20NamingCheckValidator;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.tests.emf.check.internal.Activator;
import org.eclipse.sphinx.tests.emf.check.internal.TestableCheckValidatorRegistry;
import org.eclipse.sphinx.tests.emf.check.internal.TestableSimpleHummingbird20NamingCheckValidator;
import org.eclipse.sphinx.tests.emf.check.internal.mocks.CheckValidatorRegistryMockFactory;
import org.eclipse.sphinx.tests.emf.check.util.CheckValidationTestUtil;
import org.junit.Assert;
import org.junit.Test;

public class SimpleHummingbird20NamingCheckValidatorTest {

	private static CheckValidatorRegistryMockFactory mockFactory = new CheckValidatorRegistryMockFactory();

	@Test
	public void testInitCheckMethods() {
		TestableSimpleHummingbird20NamingCheckValidator validator = new TestableSimpleHummingbird20NamingCheckValidator();
		validator.initCheckMethods();

		List<CheckMethodWrapper> checkMethodsForApplication = validator.getCheckMethodsForModelObjectType(Application.class);
		Assert.assertEquals(1, checkMethodsForApplication.size());

		Catalog catalog = validator.getCheckCatalog();
		Assert.assertNull(catalog);
	}

	@Test
	public void testOtherCategorySelected() {
		EValidator.Registry eValidatorRegistry = new org.eclipse.emf.ecore.impl.EValidatorRegistryImpl();
		Diagnostician diagnostician = new Diagnostician(eValidatorRegistry);

		// Create Mock extension registry
		IExtensionRegistry extensionRegistry = mockFactory.createExtensionRegistryMock(Activator.getPlugin(),
				testableSimpleHummingbird20NamingCheckValidator);
		TestableCheckValidatorRegistry checkValidatorRegistry = TestableCheckValidatorRegistry.INSTANCE;

		// Clear the registry
		checkValidatorRegistry.clear();
		// Set the created mock extension registry on the TestableCheckValidatorRegistry
		checkValidatorRegistry.setExtensionRegistry(extensionRegistry);
		// Set the locally create EValidator.Registry
		checkValidatorRegistry.setEValidatorRegistry(eValidatorRegistry);

		Collection<Catalog> checkCatalogs = checkValidatorRegistry.getCheckCatalogs();
		Assert.assertEquals(0, checkCatalogs.size());

		Set<String> categories = new HashSet<String>();
		categories.add(ICheckValidator.OPTION_CATEGORIES_OTHER_ID);

		Map<Object, Object> contextEntries = new HashMap<Object, Object>();
		contextEntries.put(ICheckValidator.OPTION_CATEGORIES, categories);

		Diagnostic diagnostic = diagnostician.validate(CheckValidationTestUtil.createApplication("_myApp"), contextEntries); //$NON-NLS-1$
		Assert.assertEquals(1, diagnostic.getChildren().size());

		// Expected messages
		Assert.assertEquals(1, CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(), ISSUE_MSG).size());
	}

	@Test
	public void testCheckIntrinsicConstraintsFalse() {
		EValidator.Registry eValidatorRegistry = new org.eclipse.emf.ecore.impl.EValidatorRegistryImpl();
		Diagnostician diagnostician = new Diagnostician(eValidatorRegistry);

		// Create Mock extension registry
		IExtensionRegistry extensionRegistry = mockFactory.createExtensionRegistryMock(Activator.getPlugin(),
				testableSimpleHummingbird20NamingCheckValidator);
		TestableCheckValidatorRegistry checkValidatorRegistry = TestableCheckValidatorRegistry.INSTANCE;

		// Clear the registry
		checkValidatorRegistry.clear();
		// Set the created mock extension registry on the TestableCheckValidatorRegistry
		checkValidatorRegistry.setExtensionRegistry(extensionRegistry);
		// Set the locally create EValidator.Registry
		checkValidatorRegistry.setEValidatorRegistry(eValidatorRegistry);

		Collection<Catalog> checkCatalogs = checkValidatorRegistry.getCheckCatalogs();
		Assert.assertEquals(0, checkCatalogs.size());

		Set<String> categories = new HashSet<String>();

		Map<Object, Object> contextEntries = new HashMap<Object, Object>();
		contextEntries.put(ICheckValidator.OPTION_CATEGORIES, categories);

		contextEntries.put(ICheckValidator.OPTION_ENABLE_INTRINSIC_MODEL_INTEGRITY_CONSTRAINTS, false);

		Diagnostic diagnostic = diagnostician.validate(CheckValidationTestUtil.createApplication("_myApp"), contextEntries); //$NON-NLS-1$
		Assert.assertEquals(1, diagnostic.getChildren().size());

		Assert.assertEquals(1, CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(), SimpleHummingbird20NamingCheckValidator.ISSUE_MSG)
				.size());
	}

	@Test
	public void testCheckIntrinsicConstraintsTrue() {
		EValidator.Registry eValidatorRegistry = new org.eclipse.emf.ecore.impl.EValidatorRegistryImpl();
		Diagnostician diagnostician = new Diagnostician(eValidatorRegistry);

		// Create Mock extension registry
		IExtensionRegistry extensionRegistry = mockFactory.createExtensionRegistryMock(Activator.getPlugin(),
				testableSimpleHummingbird20NamingCheckValidator);
		TestableCheckValidatorRegistry checkValidatorRegistry = TestableCheckValidatorRegistry.INSTANCE;

		// Clear the registry
		checkValidatorRegistry.clear();
		// Set the created mock extension registry on the TestableCheckValidatorRegistry
		checkValidatorRegistry.setExtensionRegistry(extensionRegistry);
		// Set the locally create EValidator.Registry
		checkValidatorRegistry.setEValidatorRegistry(eValidatorRegistry);

		Collection<Catalog> checkCatalogs = checkValidatorRegistry.getCheckCatalogs();
		Assert.assertEquals(0, checkCatalogs.size());

		Map<Object, Object> contextEntries = new HashMap<Object, Object>();
		contextEntries.put(ICheckValidator.OPTION_ENABLE_INTRINSIC_MODEL_INTEGRITY_CONSTRAINTS, true);

		Diagnostic diagnostic = diagnostician.validate(CheckValidationTestUtil.createApplication("_myApp"), contextEntries); //$NON-NLS-1$
		Assert.assertEquals(2, diagnostic.getChildren().size());

		// Expected messages
		String errorMsg = "The feature 'components' of"; //$NON-NLS-1$
		Assert.assertEquals(1, CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(), errorMsg).size());

		Assert.assertEquals(1, CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(), SimpleHummingbird20NamingCheckValidator.ISSUE_MSG)
				.size());
	}

	@Test
	public void testWithoutContextEntries() {
		EValidator.Registry eValidatorRegistry = new org.eclipse.emf.ecore.impl.EValidatorRegistryImpl();
		Diagnostician diagnostician = new Diagnostician(eValidatorRegistry);

		// Create Mock extension registry
		IExtensionRegistry extensionRegistry = mockFactory.createExtensionRegistryMock(Activator.getPlugin(),
				testableSimpleHummingbird20NamingCheckValidator);
		TestableCheckValidatorRegistry checkValidatorRegistry = TestableCheckValidatorRegistry.INSTANCE;

		// Clear the registry
		checkValidatorRegistry.clear();
		// Set the created mock extension registry on the TestableCheckValidatorRegistry
		checkValidatorRegistry.setExtensionRegistry(extensionRegistry);
		// Set the locally create EValidator.Registry
		checkValidatorRegistry.setEValidatorRegistry(eValidatorRegistry);

		Collection<Catalog> checkCatalogs = checkValidatorRegistry.getCheckCatalogs();
		Assert.assertEquals(0, checkCatalogs.size());

		Diagnostic diagnostic = diagnostician.validate(CheckValidationTestUtil.createApplication("_myApp")); //$NON-NLS-1$
		Assert.assertEquals(1, diagnostic.getChildren().size());
	}
}

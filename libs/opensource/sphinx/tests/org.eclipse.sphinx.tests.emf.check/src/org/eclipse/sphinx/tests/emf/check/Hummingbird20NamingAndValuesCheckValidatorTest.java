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

import static org.eclipse.sphinx.examples.hummingbird20.check.withcatalog.Hummingbird20NamingAndValuesCheckValidator.ISSUE_MSG_ARGUMENT_CATEGORIES_CASE1;
import static org.eclipse.sphinx.examples.hummingbird20.check.withcatalog.Hummingbird20NamingAndValuesCheckValidator.ISSUE_MSG_ARGUMENT_CATEGORIES_CASE2;
import static org.eclipse.sphinx.examples.hummingbird20.check.withcatalog.Hummingbird20NamingAndValuesCheckValidator.ISSUE_MSG_ARGUMENT_CATEGORIES_CASE3;
import static org.eclipse.sphinx.examples.hummingbird20.check.withcatalog.Hummingbird20NamingAndValuesCheckValidator.ISSUE_MSG_ARGUMENT_CATEGORIES_CASE4;
import static org.eclipse.sphinx.examples.hummingbird20.check.withcatalog.Hummingbird20NamingAndValuesCheckValidator.ISSUE_MSG_ARGUMENT_FORMAT_SUPERTYPE;
import static org.eclipse.sphinx.tests.emf.check.internal.mocks.ValidatorContribution.testableHummingbird20NamingAndValuesCheckValidator;

import java.text.MessageFormat;
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
import org.eclipse.sphinx.examples.hummingbird20.common.Identifiable;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.tests.emf.check.internal.Activator;
import org.eclipse.sphinx.tests.emf.check.internal.TestableCheckValidatorRegistry;
import org.eclipse.sphinx.tests.emf.check.internal.TestableHummingbird20NamingAndValuesCheckValidator;
import org.eclipse.sphinx.tests.emf.check.internal.mocks.CheckValidatorRegistryMockFactory;
import org.eclipse.sphinx.tests.emf.check.util.CheckValidationTestUtil;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class Hummingbird20NamingAndValuesCheckValidatorTest {

	private static CheckValidatorRegistryMockFactory mockFactory = new CheckValidatorRegistryMockFactory();

	@Test
	public void testInitCheckMethods() {
		TestableHummingbird20NamingAndValuesCheckValidator validator = new TestableHummingbird20NamingAndValuesCheckValidator();
		validator.initCheckMethods();

		List<CheckMethodWrapper> checkMethodsForApplication = validator.getCheckMethodsForModelObjectType(Application.class);
		Assert.assertEquals(6, checkMethodsForApplication.size());

		List<CheckMethodWrapper> checkMethodsForComponent = validator.getCheckMethodsForModelObjectType(Component.class);
		Assert.assertEquals(3, checkMethodsForComponent.size());

		List<CheckMethodWrapper> checkMethodsForIdentifiable = validator.getCheckMethodsForModelObjectType(Identifiable.class);
		Assert.assertEquals(1, checkMethodsForIdentifiable.size());
	}

	@Test
	public void testCategory1() {
		EValidator.Registry eValidatorRegistry = new org.eclipse.emf.ecore.impl.EValidatorRegistryImpl();
		Diagnostician diagnostician = new Diagnostician(eValidatorRegistry);

		IExtensionRegistry extensionRegistry = mockFactory.createExtensionRegistryMock(Activator.getPlugin(),
				testableHummingbird20NamingAndValuesCheckValidator);
		TestableCheckValidatorRegistry checkValidatorRegistry = TestableCheckValidatorRegistry.INSTANCE;
		checkValidatorRegistry.clear();
		checkValidatorRegistry.setExtensionRegistry(extensionRegistry);
		checkValidatorRegistry.setEValidatorRegistry(eValidatorRegistry);

		Collection<Catalog> checkCatalogs = checkValidatorRegistry.getCheckCatalogs();
		Assert.assertNotNull(checkCatalogs);
		Assert.assertEquals(1, checkCatalogs.size());

		// Category 1 is in the Check catalog
		Set<String> categories = new HashSet<String>();
		categories.add("Category1");
		Map<Object, Object> contextEntries = new HashMap<Object, Object>();
		contextEntries.put(ICheckValidator.OPTION_CATEGORIES, categories);

		Diagnostic diagnostic = diagnostician.validate(CheckValidationTestUtil.createApplication("_myApp"), contextEntries);
		Assert.assertEquals(4, diagnostic.getChildren().size());

		// Expected messages
		Assert.assertEquals(1, CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(), ISSUE_MSG_ARGUMENT_CATEGORIES_CASE1).size());
		Assert.assertEquals(1, CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(), ISSUE_MSG_ARGUMENT_CATEGORIES_CASE2).size());
		Assert.assertEquals(1, CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(), ISSUE_MSG_ARGUMENT_CATEGORIES_CASE3).size());
		Assert.assertEquals(
				1,
				CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(),
						MessageFormat.format(ISSUE_MSG_ARGUMENT_FORMAT_SUPERTYPE, Application.class.getSimpleName())).size());
	}

	@Test
	public void testCategory2() {
		EValidator.Registry eValidatorRegistry = new org.eclipse.emf.ecore.impl.EValidatorRegistryImpl();
		Diagnostician diagnostician = new Diagnostician(eValidatorRegistry);

		IExtensionRegistry extensionRegistry = mockFactory.createExtensionRegistryMock(Activator.getPlugin(),
				testableHummingbird20NamingAndValuesCheckValidator);
		TestableCheckValidatorRegistry checkValidatorRegistry = TestableCheckValidatorRegistry.INSTANCE;
		checkValidatorRegistry.clear();
		checkValidatorRegistry.setExtensionRegistry(extensionRegistry);
		checkValidatorRegistry.setEValidatorRegistry(eValidatorRegistry);

		Collection<Catalog> checkCatalogs = checkValidatorRegistry.getCheckCatalogs();
		Assert.assertNotNull(checkCatalogs);
		Assert.assertEquals(1, checkCatalogs.size());

		// Category 2 is in the Check catalog
		Set<String> categories = new HashSet<String>();
		categories.add("Category2");
		Map<Object, Object> contextEntries = new HashMap<Object, Object>();
		contextEntries.put(ICheckValidator.OPTION_CATEGORIES, categories);

		// Expected messages
		Diagnostic diagnostic = diagnostician.validate(CheckValidationTestUtil.createApplication("_myApp"), contextEntries);
		Assert.assertEquals(4, diagnostic.getChildren().size());

		Assert.assertEquals(1, CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(), ISSUE_MSG_ARGUMENT_CATEGORIES_CASE1).size());
		Assert.assertEquals(1, CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(), ISSUE_MSG_ARGUMENT_CATEGORIES_CASE2).size());
		Assert.assertEquals(1, CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(), ISSUE_MSG_ARGUMENT_CATEGORIES_CASE4).size());
		Assert.assertEquals(
				1,
				CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(),
						MessageFormat.format(ISSUE_MSG_ARGUMENT_FORMAT_SUPERTYPE, Application.class.getSimpleName())).size());
	}

	@Test
	public void testCategory1And2() {
		EValidator.Registry eValidatorRegistry = new org.eclipse.emf.ecore.impl.EValidatorRegistryImpl();
		Diagnostician diagnostician = new Diagnostician(eValidatorRegistry);

		IExtensionRegistry extensionRegistry = mockFactory.createExtensionRegistryMock(Activator.getPlugin(),
				testableHummingbird20NamingAndValuesCheckValidator);
		TestableCheckValidatorRegistry checkValidatorRegistry = TestableCheckValidatorRegistry.INSTANCE;
		checkValidatorRegistry.clear();
		checkValidatorRegistry.setExtensionRegistry(extensionRegistry);
		checkValidatorRegistry.setEValidatorRegistry(eValidatorRegistry);

		Collection<Catalog> checkCatalogs = checkValidatorRegistry.getCheckCatalogs();
		Assert.assertNotNull(checkCatalogs);
		Assert.assertEquals(1, checkCatalogs.size());

		// Category 1 and Category 2 are in the Check catalog
		Set<String> categories = new HashSet<String>();
		categories.add("Category1");
		categories.add("Category2");
		Map<Object, Object> contextEntries = new HashMap<Object, Object>();
		contextEntries.put(ICheckValidator.OPTION_CATEGORIES, categories);

		Diagnostic diagnostic = diagnostician.validate(CheckValidationTestUtil.createApplication("_myApp"), contextEntries);
		Assert.assertEquals(5, diagnostic.getChildren().size());

		// Expected messages
		Assert.assertEquals(1, CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(), ISSUE_MSG_ARGUMENT_CATEGORIES_CASE1).size());
		Assert.assertEquals(1, CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(), ISSUE_MSG_ARGUMENT_CATEGORIES_CASE2).size());
		Assert.assertEquals(1, CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(), ISSUE_MSG_ARGUMENT_CATEGORIES_CASE3).size());
		Assert.assertEquals(1, CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(), ISSUE_MSG_ARGUMENT_CATEGORIES_CASE4).size());
		Assert.assertEquals(
				1,
				CheckValidationTestUtil.findDiagnositcsWithMsg(diagnostic.getChildren(),
						MessageFormat.format(ISSUE_MSG_ARGUMENT_FORMAT_SUPERTYPE, Application.class.getSimpleName())).size());
	}
}

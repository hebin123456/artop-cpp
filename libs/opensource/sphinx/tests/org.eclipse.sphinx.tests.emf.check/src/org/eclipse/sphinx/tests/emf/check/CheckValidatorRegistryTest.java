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
 *     itemis - [478811] Check validation may compromise EMF Validation-based validation
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.check;

import static org.eclipse.sphinx.tests.emf.check.internal.mocks.ValidatorContribution.testableHummingbird20ConnectionsCheckValidator;
import static org.eclipse.sphinx.tests.emf.check.internal.mocks.ValidatorContribution.testableHummingbird20NamingAndValuesCheckValidator;
import static org.eclipse.sphinx.tests.emf.check.internal.mocks.ValidatorContribution.testableSimpleHummingbird20NamingCheckValidator;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.sphinx.emf.check.ICheckValidator;
import org.eclipse.sphinx.emf.check.catalog.Catalog;
import org.eclipse.sphinx.emf.validation.ICompositeValidator;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.tests.emf.check.internal.Activator;
import org.eclipse.sphinx.tests.emf.check.internal.TestableCheckValidatorRegistry;
import org.eclipse.sphinx.tests.emf.check.internal.mocks.CheckValidatorRegistryMockFactory;
import org.junit.Assert;
import org.junit.Test;

public class CheckValidatorRegistryTest {

	private static CheckValidatorRegistryMockFactory mockFactory = new CheckValidatorRegistryMockFactory();

	@Test
	public void testContributedValidators() {
		IExtensionRegistry extensionRegistry = mockFactory.createExtensionRegistryMock(Activator.getPlugin(),
				testableSimpleHummingbird20NamingCheckValidator, testableHummingbird20NamingAndValuesCheckValidator,
				testableHummingbird20ConnectionsCheckValidator);
		EValidator.Registry eValidatorRegistry = new org.eclipse.emf.ecore.impl.EValidatorRegistryImpl();

		TestableCheckValidatorRegistry checkValidatorRegistry = TestableCheckValidatorRegistry.INSTANCE;
		checkValidatorRegistry.clear();
		checkValidatorRegistry.setExtensionRegistry(extensionRegistry);
		checkValidatorRegistry.setEValidatorRegistry(eValidatorRegistry);

		EValidator validator = checkValidatorRegistry.getValidator(InstanceModel20Package.eINSTANCE);
		Assert.assertNotNull(validator);
		Assert.assertTrue(validator instanceof ICompositeValidator);

		List<EValidator> children = ((ICompositeValidator) validator).getValidators();
		Assert.assertEquals(3, children.size());

	}

	@Test
	public void test_getCheckCatalogs() {
		IExtensionRegistry extensionRegistry = mockFactory.createExtensionRegistryMock(Activator.getPlugin(),
				testableSimpleHummingbird20NamingCheckValidator, testableHummingbird20NamingAndValuesCheckValidator,
				testableHummingbird20ConnectionsCheckValidator);
		EValidator.Registry eValidatorRegistry = new org.eclipse.emf.ecore.impl.EValidatorRegistryImpl();

		TestableCheckValidatorRegistry checkValidatorRegistry = TestableCheckValidatorRegistry.INSTANCE;
		checkValidatorRegistry.clear();
		checkValidatorRegistry.setExtensionRegistry(extensionRegistry);
		checkValidatorRegistry.setEValidatorRegistry(eValidatorRegistry);

		Collection<Catalog> checkCatalogs = checkValidatorRegistry.getCheckCatalogs();
		Assert.assertEquals(2, checkCatalogs.size());
	}

	@Test
	public void test_getCheckValidators() {
		IExtensionRegistry extensionRegistry = mockFactory.createExtensionRegistryMock(Activator.getPlugin(),
				testableSimpleHummingbird20NamingCheckValidator, testableHummingbird20NamingAndValuesCheckValidator,
				testableHummingbird20ConnectionsCheckValidator);
		EValidator.Registry eValidatorRegistry = new org.eclipse.emf.ecore.impl.EValidatorRegistryImpl();

		TestableCheckValidatorRegistry checkValidatorRegistry = TestableCheckValidatorRegistry.INSTANCE;
		checkValidatorRegistry.clear();
		checkValidatorRegistry.setExtensionRegistry(extensionRegistry);
		checkValidatorRegistry.setEValidatorRegistry(eValidatorRegistry);

		Collection<ICheckValidator> validators = checkValidatorRegistry.getCheckValidators();
		Assert.assertEquals(3, validators.size());
	}
}

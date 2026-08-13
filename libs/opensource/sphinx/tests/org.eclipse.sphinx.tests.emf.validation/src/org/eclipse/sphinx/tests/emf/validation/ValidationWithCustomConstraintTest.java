/**
 * <copyright>
 *
 * Copyright (c) 2021 Elektrobit and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *    Elektrobit - [567814] The EMF validation framework reports multiple identical validation problems
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.validation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.validation.service.IConstraintFilter;
import org.eclipse.sphinx.emf.validation.diagnostic.ExtendedDiagnostician;
import org.eclipse.sphinx.emf.validation.diagnostic.filters.RuleIdFilter;
import org.eclipse.sphinx.emf.validation.preferences.IValidationPreferences;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.tests.emf.validation.internal.Activator;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;
import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Tests for validation with Custom Constraint
 */
public final class ValidationWithCustomConstraintTest extends DefaultIntegrationTestCase {

	@SuppressWarnings("nls")
	@Test
	public void testNoMultipleIdenticalValidationErrors() {

		try {

			int countIdenticalErrors = 0;
			List<String> diagnosticMessages = new ArrayList<String>();

			setEMFIntrinsicConstraints(true);

			IFile hbFile20_20A_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_5);
			assertNotNull(hbFile20_20A_1);
			assertTrue(hbFile20_20A_1.isAccessible());

			Resource hbResource20 = refWks.editingDomain20.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_5, true),
							false);
			assertNotNull(hbResource20);
			assertFalse(hbResource20.getContents().isEmpty());

			EObject rootObject20 = hbResource20.getContents().get(0);

			ExtendedDiagnostician diagnostician = new ExtendedDiagnostician();

			IConstraintFilter filter = new RuleIdFilter("org.eclipse.sphinx.tests.emf.validation.constraints.CustomApplicationErrorInNameConstraint");

			Diagnostic diagnostic = diagnostician.validate(rootObject20, filter, IResource.DEPTH_INFINITE);

			for (Diagnostic child : diagnostic.getChildren()) {

				String message = child.getMessage();
				diagnosticMessages.add(child.getMessage());

				if (message.equals("The name of \"CustomApplication\" must not contain \"Error\" in name.")) {

					countIdenticalErrors++;
				}
			}

			String emfMultiplicitySuperTypeError = "The feature 'components' of 'Custom Application CustomApplicationError' with 0 values must have at least 1 values";
			assertEquals("Wrong number of identical errors", 1, countIdenticalErrors);
			assertTrue("The Emf intrinsic constraint did not report error on 'components' feature for Application super-type! Expected: " + "'"
					+ emfMultiplicitySuperTypeError + "'", diagnosticMessages.toString().contains(emfMultiplicitySuperTypeError));

		}

		catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
	}

	private static void setEMFIntrinsicConstraints(boolean value) {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(org.eclipse.sphinx.emf.validation.Activator.PLUGIN_ID);

		if (prefs == null) {
			return;
		}

		prefs.putBoolean(IValidationPreferences.PREF_ENABLE_EMF_DEFAULT_RULES, value);
		try {
			prefs.flush();
		} catch (BackingStoreException ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
	}
}

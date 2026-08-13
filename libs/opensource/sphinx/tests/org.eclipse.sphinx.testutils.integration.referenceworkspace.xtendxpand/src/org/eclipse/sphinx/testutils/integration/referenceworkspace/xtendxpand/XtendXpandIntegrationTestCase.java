/**
 * <copyright>
 *
 * Copyright (c) 2011-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.testutils.integration.referenceworkspace.xtendxpand;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.sphinx.testutils.integration.AbstractIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.xtendxpand.internal.Activator;

public class XtendXpandIntegrationTestCase extends AbstractIntegrationTestCase<XtendXpandTestReferenceWorkspace> {

	public XtendXpandIntegrationTestCase() {
		super(XtendXpandTestReferenceWorkspace.class.getName());

		// Set default project references as follows:
		// HB_TRANSFORM_XTEND_PROJECT_NAME -> HB_CODEGEN_XPAND_PROJECT_NAME
		Map<String, Set<String>> projectReferences = getProjectReferences();
		projectReferences.put(XtendXpandTestReferenceWorkspace.HB_TRANSFORM_XTEND_PROJECT_NAME,
				Collections.singleton(XtendXpandTestReferenceWorkspace.HB_CODEGEN_XPAND_PROJECT_NAME));

		// Set test plug-in for retrieving test input resources
		setTestPlugin(Activator.getPlugin());
	}

	@Override
	protected XtendXpandTestReferenceWorkspace createReferenceWorkspace(Set<String> referenceProjectNames) {
		return new XtendXpandTestReferenceWorkspace(referenceProjectNames);
	}
}
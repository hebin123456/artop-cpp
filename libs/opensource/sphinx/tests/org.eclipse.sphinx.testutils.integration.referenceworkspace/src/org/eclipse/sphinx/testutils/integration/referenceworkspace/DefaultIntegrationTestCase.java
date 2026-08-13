/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - Added option to run integration tests without reusing test reference workspace from previous test to avoid side effects across individual tests
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.testutils.integration.referenceworkspace;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.eclipse.sphinx.testutils.integration.AbstractIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.internal.Activator;
import org.eclipse.uml2.uml.UMLFactory;

/**
 *
 */
public class DefaultIntegrationTestCase extends AbstractIntegrationTestCase<DefaultTestReferenceWorkspace> {

	public DefaultIntegrationTestCase() {
		super(DefaultTestReferenceWorkspace.class.getName());

		// Set default project references as follows:
		// HB_PROJECT_NAME_20_E -> HB_PROJECT_NAME_20_D -> HB_PROJECT_NAME_10_E -> HB_PROJECT_NAME_10_D
		Map<String, Set<String>> projectReferences = getProjectReferences();
		projectReferences.put(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E,
				Collections.singleton(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D));
		projectReferences.put(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
				Collections.singleton(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E));
		projectReferences.put(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E,
				Collections.singleton(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D));

		// Set default test plug-in for retrieving test input resources
		setTestPlugin(Activator.getPlugin());
	}

	@Override
	protected DefaultTestReferenceWorkspace createReferenceWorkspace(Set<String> referenceProjectSubset) {
		return new DefaultTestReferenceWorkspace(referenceProjectSubset);
	}

	protected EObject createHummingbird20InstanceModelRoot() {
		return InstanceModel20Factory.eINSTANCE.createApplication();
	}

	protected EObject createHummingbird20TypeModelRoot() {
		return TypeModel20Factory.eINSTANCE.createPlatform();

	}

	protected EObject createUML2ModelRoot() {
		return UMLFactory.eINSTANCE.createModel();
	}
}

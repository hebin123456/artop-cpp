/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
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
package org.eclipse.sphinx.tests.emf.serialization.generators.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

@SuppressWarnings("nls")
public class EReferenceContainedWithoutSubtypesWithClassifierNameSuffixModelBuilder extends EReferenceContainedWithoutSubtypesModelBuilder {

	public EReferenceContainedWithoutSubtypesWithClassifierNameSuffixModelBuilder(String name, int persistenceMappingStrategy) {
		super(name, persistenceMappingStrategy);
	}

	@Override
	protected List<EPackage> createMetaModel(String name, boolean isMany, int persistenceMappingStrategy) {
		List<EPackage> ePackages = new ArrayList<EPackage>();
		EPackage ePackage = createEPackage(name, "http://www.eclipse.org/sphinx/tests/emf/serialization/generators/" + name, "nodes");
		ePackages.add(ePackage);

		EClass nodeEClass = createEClass(ePackage, "Node", "NODE", "NODES", true);
		EReference eReference = createEReference(nodeEClass, "child", true, isMany, nodeEClass, "CHILD", "CHILDREN", persistenceMappingStrategy);
		metadata.setXMLClassifierNameSuffix(eReference, "-TYPE");

		return ePackages;
	}
}

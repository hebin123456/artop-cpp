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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

@SuppressWarnings("nls")
public class EReferenceContainedWithoutSubtypesModelBuilder extends AbstractNodeModelBuilder {

	public EReferenceContainedWithoutSubtypesModelBuilder(String name, int persistenceMappingStrategy) {
		super(name, persistenceMappingStrategy);
	}

	@Override
	protected List<EPackage> createMetaModel(String name, boolean isMany, int persistenceMappingStrategy) {
		List<EPackage> ePackages = new ArrayList<EPackage>();
		EPackage ePackage = createEPackage(name, "http://www.eclipse.org/sphinx/tests/emf/serialization/generators/" + name, "nodes");
		ePackages.add(ePackage);

		EClass nodeEClass = createEClass(ePackage, "Node", "NODE", "NODES", true);
		createEReference(nodeEClass, "child", true, isMany, nodeEClass, "CHILD", "CHILDREN", persistenceMappingStrategy);

		return ePackages;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected EObject createModel(EPackage rootEPackage, EPackage childEPackage, int numberOfChildren) {
		EObject eObject = createNodeEObject(rootEPackage, "Node");

		EReference childrenEReference = (EReference) ((EClass) rootEPackage.getEClassifier("Node")).getEStructuralFeature("child");

		if (null != childrenEReference) {
			if (childrenEReference.isMany()) {
				for (int i = 0; i < numberOfChildren; i++) {
					EObject childNode = createNodeEObject(childEPackage, "Node");
					((EList<EObject>) eObject.eGet(childrenEReference)).add(childNode);
				}
			} else if (0 < numberOfChildren) {
				EObject childNode = createNodeEObject(childEPackage, "Node");
				eObject.eSet(childrenEReference, childNode);
			} else {
				// intentionally no child references
			}
		} else {
			// no children reference in meta model
		}

		return eObject;
	}

}

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
public class MultiPackageNodeModelBuilder extends AbstractNodeModelBuilder {

	public MultiPackageNodeModelBuilder(String name, int persistenceMappingStrategy) {
		super(name, persistenceMappingStrategy);
	}

	@Override
	public EObject getManyModel(int numberOfChildren) {
		return createModel(manyPackages.get(0), manyPackages.get(1), numberOfChildren);
	}

	@Override
	public EObject getSingleModel(int numberOfChildren) {
		return createModel(singlePackages.get(0), singlePackages.get(1), 1);
	}

	@Override
	protected List<EPackage> createMetaModel(String name, boolean isMany, int persistenceMappingStrategy) {
		List<EPackage> ePackages = new ArrayList<EPackage>();
		EPackage ePackage1 = createEPackage(name + "1", "http://www.eclipse.org/sphinx/tests/emf/serialization/generators/1", "nodes1");
		ePackages.add(ePackage1);
		EPackage ePackage2 = createEPackage(name + "2", "http://www.eclipse.org/sphinx/tests/emf/serialization/generators/2", "nodes2");
		ePackages.add(ePackage2);

		EClass nodesEClass1 = createEClass(ePackage1, "Node", "NODE", "NODES", true);
		EClass nodesEClass2 = createEClass(ePackage2, "Node", "NODE", "NODES", false);

		createEReference(nodesEClass1, "children", true, isMany, nodesEClass2, "CHILD", "CHILDREN", persistenceMappingStrategy);
		return ePackages;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected EObject createModel(EPackage rootEPackage, EPackage childEPackage, int numberOfChildren) {
		EObject eObject = createNodeEObject(rootEPackage, "Node");

		EReference childrenEReference = (EReference) ((EClass) rootEPackage.getEClassifier("Node")).getEStructuralFeature("children");

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

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
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;

@SuppressWarnings("nls")
public class EAttributeContainedCustomDataTypesModelBuilder extends AbstractNodeModelBuilder {

	public EAttributeContainedCustomDataTypesModelBuilder(String name, int persistenceMappingStrategy) {
		super(name, persistenceMappingStrategy);
	}

	@Override
	protected List<EPackage> createMetaModel(String name, boolean isMany, int persistenceMappingStrategy) {
		List<EPackage> ePackages = new ArrayList<EPackage>();
		EPackage ePackage = createEPackage(name, "http://www.eclipse.org/sphinx/tests/emf/serialization/generators/" + name, "nodes");
		ePackages.add(ePackage);

		EClass nodeEClass = createEClass(ePackage, "Node", "NODE", "NODES", true);
		EDataType stringEDataType = createEDataType(ePackage, "String", "STRING", "STRING-VALUES", false);
		createEAttribute(nodeEClass, "property", isMany, stringEDataType, "PROPERTY", "PROPERTIES", persistenceMappingStrategy);

		return ePackages;
	}

	@Override
	@SuppressWarnings({ "unchecked", "unused" })
	protected EObject createModel(EPackage rootEPackage, EPackage childEPackage, int numberOfChildren) {
		EObject eObject = createNodeEObject(rootEPackage, "Node");

		EAttribute propertyEAttribute = (EAttribute) ((EClass) rootEPackage.getEClassifier("Node")).getEStructuralFeature("property");
		EDataType stringEDataType = propertyEAttribute.getEAttributeType();

		if (null != propertyEAttribute) {
			if (propertyEAttribute.isMany()) {
				for (int i = 0; i < numberOfChildren; i++) {
					((EList<Object>) eObject.eGet(propertyEAttribute)).add(EcoreUtil.createFromString(stringEDataType, "property" + i));
				}
			} else if (0 < numberOfChildren) {
				eObject.eSet(propertyEAttribute, EcoreUtil.createFromString(stringEDataType, "property"));
			} else {
				// intentionally no child references
			}
		} else {
			// no children reference in meta model
		}

		return eObject;
	}

}

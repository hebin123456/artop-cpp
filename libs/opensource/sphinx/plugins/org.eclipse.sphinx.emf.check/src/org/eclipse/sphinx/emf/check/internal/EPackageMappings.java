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
 *     itemis - [458976] Validators are not singleton when they implement checks for different EPackages
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EPackage;

public class EPackageMappings {

	private Map<String, Object> ePackageMappings = new HashMap<String, Object>();

	public void put(String javaPackageName, Object ePackageObject) {
		ePackageMappings.put(javaPackageName, ePackageObject);
	}

	public EPackage getEPackageFor(Class<?> clazz) {
		Assert.isNotNull(clazz);

		// Retrieve mapped EPackage object
		Object ePackageObject = ePackageMappings.get(clazz.getName());
		if (ePackageObject == null) {
			ePackageObject = ePackageMappings.get(clazz.getPackage().getName());
		}

		// Unpack and contained EPackage
		if (ePackageObject instanceof EPackage) {
			return (EPackage) ePackageObject;
		} else if (ePackageObject instanceof EPackage.Descriptor) {
			return ((EPackage.Descriptor) ePackageObject).getEPackage();
		}
		return null;
	}

	public void clear() {
		ePackageMappings.clear();
	}
}
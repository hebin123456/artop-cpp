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
 *     itemis - [477076] Access to the "ordered" attributes in metamodels that are optimized for deterministic code generation using QVTO or OCL
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;

/**
 * Provides access to an alternative storage of the "ordered" information in case the ETypedElement#ordered attribute is
 * always set to true in order to support deterministic code generation using frameworks that map not ordered
 * EStructuralFeatures to sets.
 */
public class ExtendedBasicExtendedMetaData extends BasicExtendedMetaData {
	public static ExtendedBasicExtendedMetaData INSTANCE = new ExtendedBasicExtendedMetaData();
	protected Map<EStructuralFeature, Boolean> eStructuralFeature2OrderedMap = new HashMap<EStructuralFeature, Boolean>();

	public ExtendedBasicExtendedMetaData() {
		super();
	}

	public ExtendedBasicExtendedMetaData(Registry registry) {
		super(registry);
	}

	public ExtendedBasicExtendedMetaData(String annotationURI, Registry registry, Map<EModelElement, EAnnotation> annotationMap) {
		super(annotationURI, registry, annotationMap);
	}

	public ExtendedBasicExtendedMetaData(String annotationURI, Registry registry) {
		super(annotationURI, registry);
	}

	public boolean isOrdered(EStructuralFeature eStructuralFeature) {
		Boolean isOrdered = eStructuralFeature2OrderedMap.get(eStructuralFeature);
		if (isOrdered == null) {
			setOrdered(eStructuralFeature, isOrdered = basicIsOrdered(eStructuralFeature));
		}
		return isOrdered;
	}

	protected boolean basicIsOrdered(EStructuralFeature eStructuralFeature) {
		String orderedString = null;
		EAnnotation eAnnotation = getAnnotation(eStructuralFeature, false);
		if (eAnnotation != null) {
			orderedString = eAnnotation.getDetails().get("ordered"); //$NON-NLS-1$
		}
		if (orderedString != null) {
			// annotation with key ordered is defined
			return Boolean.parseBoolean(orderedString);
		} else {
			// read from native Ecore if annotation is not defined
			return eStructuralFeature.isOrdered();
		}
	}

	public void setOrdered(EStructuralFeature eStructuralFeature, boolean ordered) {
		EAnnotation eAnnotation = getAnnotation(eStructuralFeature, true);
		eAnnotation.getDetails().put("ordered", Boolean.toString(ordered)); //$NON-NLS-1$

		eStructuralFeature2OrderedMap.put(eStructuralFeature, ordered);
	}

}

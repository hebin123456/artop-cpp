/**
 * <copyright>
 *
 * Copyright (c) 2008-2015 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [477076] Access to the "ordered" attributes in metamodels that are optimized for deterministic code generation using QVTO or OCL
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;

/**
 * Enables {@link XMLResource XML resource}s which have been created with older but still compatible versions of a
 * metamodel to be loaded with the most recent version of that metamodel. Relies on the {@link IMetaModelDescriptor
 * descriptor} of the metamodel in question to find out with XML resources can be deemed compatible and which not.
 */
public class BasicMigrationExtendedMetaData extends ExtendedBasicExtendedMetaData {

	public BasicMigrationExtendedMetaData() {
		super();
	}

	public BasicMigrationExtendedMetaData(Registry registry) {
		super(registry);
	}

	public BasicMigrationExtendedMetaData(String annotationURI, Registry registry, Map<EModelElement, EAnnotation> annotationMap) {
		super(annotationURI, registry, annotationMap);
	}

	public BasicMigrationExtendedMetaData(String annotationURI, Registry registry) {
		super(annotationURI, registry);
	}

	/**
	 * Does the same as the super implementation but maps given XML element to corresponding {@link EStructuralFeature
	 * feature} of given {@link EClass} if underlying namespaces are compatible instead of doing so only when they are
	 * equal.
	 *
	 * @see org.eclipse.emf.ecore.util.BasicExtendedMetaData#getLocalElement(org.eclipse.emf.ecore.EClass,
	 *      java.lang.String, java.lang.String)
	 */

	@Override
	protected EStructuralFeature getLocalElement(EClass eClass, String namespace, String name) {
		EStructuralFeature result = null;
		if (isFeatureKindSpecific()) {
			List<EStructuralFeature> allElements = getAllElements(eClass);
			for (int i = 0, size = allElements.size(); i < size; ++i) {
				EStructuralFeature eStructuralFeature = allElements.get(i);
				if (name.equals(getName(eStructuralFeature))) {
					String featureNamespace = getNamespace(eStructuralFeature);
					if (namespace == null) {
						if (featureNamespace == null) {
							return eStructuralFeature;
						} else if (result == null) {
							result = eStructuralFeature;
						}
					} else if (isSameOrCompatibleNamespace(namespace, featureNamespace)) {
						return eStructuralFeature;
					} else if (featureNamespace == null && result == null) {
						result = eStructuralFeature;
					}
				}
			}
		} else {
			for (int i = 0, size = eClass.getFeatureCount(); i < size; ++i) {
				EStructuralFeature eStructuralFeature = eClass.getEStructuralFeature(i);
				switch (getFeatureKind(eStructuralFeature)) {
				case UNSPECIFIED_FEATURE:
				case ELEMENT_FEATURE: {
					if (name.equals(getName(eStructuralFeature))) {
						String featureNamespace = getNamespace(eStructuralFeature);
						if (namespace == null) {
							if (featureNamespace == null) {
								return eStructuralFeature;
							} else if (result == null) {
								result = eStructuralFeature;
							}
						} else if (namespace.equals(featureNamespace)) {
							return eStructuralFeature;
						} else if (featureNamespace == null && result == null) {
							result = eStructuralFeature;
						}
					}
					break;
				}
				}
			}
		}
		return isFeatureNamespaceMatchingLax() ? result : null;
	}

	/**
	 * Tests if given XML namespace is equal to or compatible with the given {@link EStructuralFeature feature}
	 * namespace.
	 *
	 * @param xmlNamespace
	 *            The XML element namespace to be investigated.
	 * @param featureNamespace
	 *            The {@link EStructuralFeature feature} namespace to be used as reference.
	 * @return <code>true</code> if given XML namespace and {@link EStructuralFeature feature} namespace are equal or
	 *         compatible, or <code>false</code> otherwise.
	 */
	protected boolean isSameOrCompatibleNamespace(String xmlNamespace, String featureNamespace) {
		Assert.isNotNull(xmlNamespace);

		try {
			URI xmlNamespaceURI = new URI(xmlNamespace);
			IMetaModelDescriptor xmlMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(xmlNamespaceURI);
			if (xmlMMDescriptor != null) {
				return xmlMMDescriptor.getNamespace().equals(featureNamespace);
			}
		} catch (URISyntaxException ex) {
			// Ignore exception
		}

		// Fall back to behavior provided by super implementation
		return xmlNamespace.equals(featureNamespace);
	}
}

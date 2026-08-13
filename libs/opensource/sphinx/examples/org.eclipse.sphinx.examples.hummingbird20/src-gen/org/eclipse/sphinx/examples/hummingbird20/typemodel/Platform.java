/**
 * <copyright>
 *
 * Copyright (c) 2008-2014 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - Enhancements and maintenance
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.typemodel;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.sphinx.examples.hummingbird20.common.Identifiable;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Platform</b></em>'. <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents a Platform model object
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform#getComponentTypes <em>Component Types</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform#getInterfaces <em>Interfaces</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform#getMixed <em>Mixed</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform#getXSISchemaLocation <em>XSI Schema Location</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getPlatform()
 * @model extendedMetaData="kind='mixed'"
 * @generated
 */
public interface Platform extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Component Types</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Component Types</em>' containment reference list isn't clear, there really should be
	 * more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Aggregates all Component Types with this Platform...
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Component Types</em>' containment reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getPlatform_ComponentTypes()
	 * @model containment="true" required="true"
	 * @generated
	 */
	EList<ComponentType> getComponentTypes();

	/**
	 * Returns the value of the '<em><b>Interfaces</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Interfaces</em>' containment reference list isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Aggregates all Interfaces with this Platform...
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Interfaces</em>' containment reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getPlatform_Interfaces()
	 * @model containment="true"
	 * @generated
	 */
	EList<Interface> getInterfaces();

	/**
	 * Returns the value of the '<em><b>Mixed</b></em>' attribute list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.util.FeatureMap.Entry}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mixed</em>' attribute list isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mixed</em>' attribute list.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getPlatform_Mixed()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.EFeatureMapEntry" many="true"
	 *        extendedMetaData="name=':mixed' kind='elementWildcard'"
	 * @generated
	 */
	FeatureMap getMixed();

	/**
	 * Returns the value of the '<em><b>XSI Schema Location</b></em>' map.
	 * The key is of type {@link java.lang.String},
	 * and the value is of type {@link java.lang.String},
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>XSI Schema Location</em>' map isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>XSI Schema Location</em>' map.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getPlatform_XSISchemaLocation()
	 * @model mapType="org.eclipse.emf.ecore.EStringToStringMapEntry<org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EString>" transient="true"
	 *        extendedMetaData="name='xsi:schemaLocation' kind='element'"
	 * @generated
	 */
	EMap<String, String> getXSISchemaLocation();

} // Platform

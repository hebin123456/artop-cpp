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
package org.eclipse.sphinx.examples.hummingbird20.common;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Identifiable</b></em>'. <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents an Identifiable model object
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.common.Identifiable#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.common.Identifiable#getDescription <em>Description</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sphinx.examples.hummingbird20.common.Common20Package#getIdentifiable()
 * @model abstract="true"
 * @generated
 */
public interface Identifiable extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Specifies the name of this Identifiable model object
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Common20Package#getIdentifiable_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.common.Identifiable#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Description</em>' containment reference isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Specifies the description of this Identifiable model object
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Description</em>' containment reference.
	 * @see #setDescription(Description)
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Common20Package#getIdentifiable_Description()
	 * @model containment="true"
	 * @generated
	 */
	Description getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.common.Identifiable#getDescription <em>Description</em>}' containment reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' containment reference.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(Description value);

} // Identifiable

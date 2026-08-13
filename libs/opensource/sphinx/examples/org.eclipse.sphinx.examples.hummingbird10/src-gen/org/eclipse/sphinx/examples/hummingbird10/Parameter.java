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
package org.eclipse.sphinx.examples.hummingbird10;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Parameter</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Parameter#getName <em>Name</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Parameter#getValue <em>Value</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getParameter()
 * @model
 * @generated
 */
public interface Parameter extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getParameter_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird10.Parameter#getName <em>Name</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @param value
	 *            the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' attribute isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(String)
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getParameter_Value()
	 * @model
	 * @generated
	 */
	String getValue();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird10.Parameter#getValue <em>Value</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @param value
	 *            the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(String value);

} // Parameter

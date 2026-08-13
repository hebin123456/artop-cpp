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

import org.eclipse.sphinx.examples.hummingbird20.common.Identifiable;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Parameter</b></em>'. <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents a Parameter model object
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter#getDataType <em>Data Type</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter#isOptional <em>Optional</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getParameter()
 * @model
 * @generated
 */
public interface Parameter extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Data Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data Type</em>' attribute isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data Type</em>' attribute.
	 * @see #setDataType(String)
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getParameter_DataType()
	 * @model
	 * @generated
	 */
	String getDataType();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter#getDataType <em>Data Type</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data Type</em>' attribute.
	 * @see #getDataType()
	 * @generated
	 */
	void setDataType(String value);

	/**
	 * Returns the value of the '<em><b>Optional</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Optional</em>' attribute isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Optional</em>' attribute.
	 * @see #setOptional(boolean)
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getParameter_Optional()
	 * @model
	 * @generated
	 */
	boolean isOptional();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter#isOptional <em>Optional</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Optional</em>' attribute.
	 * @see #isOptional()
	 * @generated
	 */
	void setOptional(boolean value);

} // Parameter

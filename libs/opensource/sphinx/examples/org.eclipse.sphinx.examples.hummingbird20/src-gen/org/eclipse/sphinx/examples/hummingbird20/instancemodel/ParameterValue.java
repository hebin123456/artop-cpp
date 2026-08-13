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
package org.eclipse.sphinx.examples.hummingbird20.instancemodel;

import org.eclipse.sphinx.examples.hummingbird20.common.Identifiable;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Parameter Value</b></em>'. <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents a Parameter Value model object
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getParameterValue()
 * @model
 * @generated
 */
public interface ParameterValue extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' reference isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reference to the type of this Parameter
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Type</em>' reference.
	 * @see #setType(Parameter)
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getParameterValue_Type()
	 * @model required="true"
	 * @generated
	 */
	Parameter getType();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue#getType <em>Type</em>}' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' reference.
	 * @see #getType()
	 * @generated
	 */
	void setType(Parameter value);

	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' attribute isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Specifies the value of this ParameterValue
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(String)
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getParameterValue_Value()
	 * @model
	 * @generated
	 */
	String getValue();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue#getValue <em>Value</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(String value);

} // ParameterValue

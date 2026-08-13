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
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Port</b></em>'. <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents a Port model object
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getOwner <em>Owner</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getRequiredInterface <em>Required Interface</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getMinProviderCount <em>Min Provider Count</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getMaxProviderCount <em>Max Provider Count</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getPort()
 * @model
 * @generated
 */
public interface Port extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Owner</b></em>' container reference. It is bidirectional and its opposite is '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType#getPorts <em>Ports</em>}'. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Owner</em>' container reference isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Owner</em>' container reference.
	 * @see #setOwner(ComponentType)
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getPort_Owner()
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType#getPorts
	 * @model opposite="ports" required="true" transient="false"
	 * @generated
	 */
	ComponentType getOwner();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getOwner <em>Owner</em>}' container reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Owner</em>' container reference.
	 * @see #getOwner()
	 * @generated
	 */
	void setOwner(ComponentType value);

	/**
	 * Returns the value of the '<em><b>Required Interface</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface#getRequiringPorts <em>Requiring Ports</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Required Interface</em>' reference isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Specifies the required interface
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Required Interface</em>' reference.
	 * @see #setRequiredInterface(Interface)
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getPort_RequiredInterface()
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface#getRequiringPorts
	 * @model opposite="requiringPorts" required="true"
	 * @generated
	 */
	Interface getRequiredInterface();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getRequiredInterface <em>Required Interface</em>}' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Required Interface</em>' reference.
	 * @see #getRequiredInterface()
	 * @generated
	 */
	void setRequiredInterface(Interface value);

	/**
	 * Returns the value of the '<em><b>Min Provider Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Min Provider Count</em>' attribute isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Min Provider Count</em>' attribute.
	 * @see #setMinProviderCount(int)
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getPort_MinProviderCount()
	 * @model
	 * @generated
	 */
	int getMinProviderCount();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getMinProviderCount <em>Min Provider Count</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Min Provider Count</em>' attribute.
	 * @see #getMinProviderCount()
	 * @generated
	 */
	void setMinProviderCount(int value);

	/**
	 * Returns the value of the '<em><b>Max Provider Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Max Provider Count</em>' attribute isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Max Provider Count</em>' attribute.
	 * @see #setMaxProviderCount(int)
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getPort_MaxProviderCount()
	 * @model
	 * @generated
	 */
	int getMaxProviderCount();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getMaxProviderCount <em>Max Provider Count</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max Provider Count</em>' attribute.
	 * @see #getMaxProviderCount()
	 * @generated
	 */
	void setMaxProviderCount(int value);

} // Port

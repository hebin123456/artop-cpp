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
import org.eclipse.sphinx.examples.hummingbird20.common.Identifiable;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Component Type</b></em>'. <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents a ComponentType model object
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType#getProvidedInterfaces <em>Provided Interfaces</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType#getPorts <em>Ports</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType#getParameters <em>Parameters</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getComponentType()
 * @model
 * @generated
 */
public interface ComponentType extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Provided Interfaces</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface#getProvidingComponentTypes <em>Providing Component Types</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Provided Interfaces</em>' reference list isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Provided Interfaces</em>' reference list.
	 * @see #isSetProvidedInterfaces()
	 * @see #unsetProvidedInterfaces()
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getComponentType_ProvidedInterfaces()
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface#getProvidingComponentTypes
	 * @model opposite="providingComponentTypes" unsettable="true"
	 * @generated
	 */
	EList<Interface> getProvidedInterfaces();

	/**
	 * Unsets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType#getProvidedInterfaces <em>Provided Interfaces</em>}' reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #isSetProvidedInterfaces()
	 * @see #getProvidedInterfaces()
	 * @generated
	 */
	void unsetProvidedInterfaces();

	/**
	 * Returns whether the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType#getProvidedInterfaces <em>Provided Interfaces</em>}' reference list is set.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return whether the value of the '<em>Provided Interfaces</em>' reference list is set.
	 * @see #unsetProvidedInterfaces()
	 * @see #getProvidedInterfaces()
	 * @generated
	 */
	boolean isSetProvidedInterfaces();

	/**
	 * Returns the value of the '<em><b>Ports</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getOwner <em>Owner</em>}'.
	 * <!-- begin-user-doc
	 * -->
	 * <p>
	 * If the meaning of the '<em>Ports</em>' containment reference list isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Aggregates all Ports with this ComponentType...
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Ports</em>' containment reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getComponentType_Ports()
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getOwner
	 * @model opposite="owner" containment="true"
	 * @generated
	 */
	EList<Port> getPorts();

	/**
	 * Returns the value of the '<em><b>Parameters</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parameters</em>' containment reference list isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Aggregates all Parameters with this ComponentType...
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Parameters</em>' containment reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getComponentType_Parameters()
	 * @model containment="true"
	 *        extendedMetaData="ordered='false'"
	 * @generated
	 */
	EList<Parameter> getParameters();

} // ComponentType

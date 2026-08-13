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
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Interface</b></em>'. <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents an Interface model object
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface#getProvidingComponentTypes <em>Providing Component Types</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface#getRequiringPorts <em>Requiring Ports</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getInterface()
 * @model
 * @generated
 */
public interface Interface extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Providing Component Types</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType#getProvidedInterfaces <em>Provided Interfaces</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Providing Component Types</em>' reference list isn't clear, there really should be
	 * more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Specifies the providing component types
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Providing Component Types</em>' reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getInterface_ProvidingComponentTypes()
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType#getProvidedInterfaces
	 * @model opposite="providedInterfaces"
	 * @generated
	 */
	EList<ComponentType> getProvidingComponentTypes();

	/**
	 * Returns the value of the '<em><b>Requiring Ports</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getRequiredInterface <em>Required Interface</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Requiring Ports</em>' reference list isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Specifies the requiring ports
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Requiring Ports</em>' reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#getInterface_RequiringPorts()
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getRequiredInterface
	 * @model opposite="requiredInterface"
	 * @generated
	 */
	EList<Port> getRequiringPorts();

} // Interface

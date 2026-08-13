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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Interface</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Interface#getName <em>Name</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Interface#getProvidingComponents <em>Providing Components</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getInterface()
 * @model
 * @generated
 */
public interface Interface extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getInterface_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird10.Interface#getName <em>Name</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @param value
	 *            the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Providing Components</b></em>' reference list. The list contents are of type
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Component}. It is bidirectional and its opposite is '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Component#getProvidedInterfaces <em>Provided Interfaces</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Providing Components</em>' reference list isn't clear, there really should be more of
	 * a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Providing Components</em>' reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getInterface_ProvidingComponents()
	 * @see org.eclipse.sphinx.examples.hummingbird10.Component#getProvidedInterfaces
	 * @model opposite="providedInterfaces"
	 * @generated
	 */
	EList<Component> getProvidingComponents();

} // Interface

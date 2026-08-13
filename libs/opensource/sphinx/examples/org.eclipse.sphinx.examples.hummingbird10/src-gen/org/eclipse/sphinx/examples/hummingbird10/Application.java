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
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Application</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Application#getName <em>Name</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Application#getComponents <em>Components</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Application#getInterfaces <em>Interfaces</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getApplication()
 * @model
 * @generated
 */
public interface Application extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getApplication_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird10.Application#getName <em>Name</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @param value
	 *            the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Components</b></em>' containment reference list. The list contents are of type
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Component}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Components</em>' containment reference list isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Components</em>' containment reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getApplication_Components()
	 * @model containment="true" required="true"
	 * @generated
	 */
	EList<Component> getComponents();

	/**
	 * Returns the value of the '<em><b>Interfaces</b></em>' containment reference list. The list contents are of type
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Interface}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Interfaces</em>' containment reference list isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Interfaces</em>' containment reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getApplication_Interfaces()
	 * @model containment="true"
	 * @generated
	 */
	EList<Interface> getInterfaces();

} // Application

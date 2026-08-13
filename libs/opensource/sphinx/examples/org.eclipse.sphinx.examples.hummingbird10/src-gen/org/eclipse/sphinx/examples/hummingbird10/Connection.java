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
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Connection</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Connection#getRequiredInterface <em>Required Interface</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Connection#getTargetComponent <em>Target Component</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Connection#getSourceComponent <em>Source Component</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Connection#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getConnection()
 * @model
 *        annotation="http://www.eclipse.org/emf/2002/Ecore constraints='validateInterfacesImplementedByTargetComponent'"
 * @generated
 */
public interface Connection extends EObject {
	/**
	 * Returns the value of the '<em><b>Required Interface</b></em>' reference. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Required Interface</em>' reference isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Required Interface</em>' reference.
	 * @see #setRequiredInterface(Interface)
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getConnection_RequiredInterface()
	 * @model required="true"
	 * @generated
	 */
	Interface getRequiredInterface();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird10.Connection#getRequiredInterface
	 * <em>Required Interface</em>}' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @param value
	 *            the new value of the '<em>Required Interface</em>' reference.
	 * @see #getRequiredInterface()
	 * @generated
	 */
	void setRequiredInterface(Interface value);

	/**
	 * Returns the value of the '<em><b>Target Component</b></em>' reference. It is bidirectional and its opposite is '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Component#getIncomingConnections <em>Incoming Connections</em>}
	 * '. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Target Component</em>' reference isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Target Component</em>' reference.
	 * @see #setTargetComponent(Component)
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getConnection_TargetComponent()
	 * @see org.eclipse.sphinx.examples.hummingbird10.Component#getIncomingConnections
	 * @model opposite="incomingConnections" required="true"
	 * @generated
	 */
	Component getTargetComponent();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird10.Connection#getTargetComponent
	 * <em>Target Component</em>}' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @param value
	 *            the new value of the '<em>Target Component</em>' reference.
	 * @see #getTargetComponent()
	 * @generated
	 */
	void setTargetComponent(Component value);

	/**
	 * Returns the value of the '<em><b>Source Component</b></em>' container reference. It is bidirectional and its
	 * opposite is '{@link org.eclipse.sphinx.examples.hummingbird10.Component#getOutgoingConnections
	 * <em>Outgoing Connections</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Source Component</em>' container reference isn't clear, there really should be more of
	 * a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Source Component</em>' container reference.
	 * @see #setSourceComponent(Component)
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getConnection_SourceComponent()
	 * @see org.eclipse.sphinx.examples.hummingbird10.Component#getOutgoingConnections
	 * @model opposite="outgoingConnections" required="true"
	 * @generated
	 */
	Component getSourceComponent();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird10.Connection#getSourceComponent
	 * <em>Source Component</em>}' container reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @param value
	 *            the new value of the '<em>Source Component</em>' container reference.
	 * @see #getSourceComponent()
	 * @generated
	 */
	void setSourceComponent(Component value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getConnection_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird10.Connection#getName <em>Name</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @param value
	 *            the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

} // Connection

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
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Component</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Component#getName <em>Name</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Component#getOutgoingConnections <em>Outgoing Connections</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Component#getProvidedInterfaces <em>Provided Interfaces</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Component#getParameters <em>Parameters</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.Component#getIncomingConnections <em>Incoming Connections</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getComponent()
 * @model
 * @generated
 */
public interface Component extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute. The default value is <code>""</code>. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getComponent_Name()
	 * @model default="" required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird10.Component#getName <em>Name</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @param value
	 *            the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Outgoing Connections</b></em>' containment reference list. The list contents are
	 * of type {@link org.eclipse.sphinx.examples.hummingbird10.Connection}. It is bidirectional and its opposite is '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Connection#getSourceComponent <em>Source Component</em>}'. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Outgoing Connections</em>' containment reference list isn't clear, there really should
	 * be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Outgoing Connections</em>' containment reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getComponent_OutgoingConnections()
	 * @see org.eclipse.sphinx.examples.hummingbird10.Connection#getSourceComponent
	 * @model opposite="sourceComponent" containment="true"
	 * @generated
	 */
	EList<Connection> getOutgoingConnections();

	/**
	 * Returns the value of the '<em><b>Provided Interfaces</b></em>' reference list. The list contents are of type
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Interface}. It is bidirectional and its opposite is '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Interface#getProvidingComponents <em>Providing Components</em>}
	 * '. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Provided Interfaces</em>' reference list isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Provided Interfaces</em>' reference list.
	 * @see #isSetProvidedInterfaces()
	 * @see #unsetProvidedInterfaces()
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getComponent_ProvidedInterfaces()
	 * @see org.eclipse.sphinx.examples.hummingbird10.Interface#getProvidingComponents
	 * @model opposite="providingComponents" unsettable="true"
	 * @generated
	 */
	EList<Interface> getProvidedInterfaces();

	/**
	 * Unsets the value of the '{@link org.eclipse.sphinx.examples.hummingbird10.Component#getProvidedInterfaces
	 * <em>Provided Interfaces</em>}' reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @see #isSetProvidedInterfaces()
	 * @see #getProvidedInterfaces()
	 * @generated
	 */
	void unsetProvidedInterfaces();

	/**
	 * Returns whether the value of the '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Component#getProvidedInterfaces <em>Provided Interfaces</em>}'
	 * reference list is set. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return whether the value of the '<em>Provided Interfaces</em>' reference list is set.
	 * @see #unsetProvidedInterfaces()
	 * @see #getProvidedInterfaces()
	 * @generated
	 */
	boolean isSetProvidedInterfaces();

	/**
	 * Returns the value of the '<em><b>Parameters</b></em>' containment reference list. The list contents are of type
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Parameter}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parameters</em>' containment reference list isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Parameters</em>' containment reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getComponent_Parameters()
	 * @model containment="true"
	 * @generated
	 */
	EList<Parameter> getParameters();

	/**
	 * Returns the value of the '<em><b>Incoming Connections</b></em>' reference list. The list contents are of type
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Connection}. It is bidirectional and its opposite is '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Connection#getTargetComponent <em>Target Component</em>}'. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Incoming Connections</em>' reference list isn't clear, there really should be more of
	 * a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 *
	 * @return the value of the '<em>Incoming Connections</em>' reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#getComponent_IncomingConnections()
	 * @see org.eclipse.sphinx.examples.hummingbird10.Connection#getTargetComponent
	 * @model opposite="targetComponent"
	 * @generated
	 */
	EList<Connection> getIncomingConnections();

} // Component

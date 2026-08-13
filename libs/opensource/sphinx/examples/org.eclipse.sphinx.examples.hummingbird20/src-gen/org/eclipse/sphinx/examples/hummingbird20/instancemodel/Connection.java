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
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Connection</b></em>'. <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents a Connection model object
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getSourceComponent <em>Source Component</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getSourcePort <em>Source Port</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getTargetComponent <em>Target Component</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getConnection()
 * @model
 * @generated
 */
public interface Connection extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Source Component</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getOutgoingConnections <em>Outgoing Connections</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Source Component</em>' container reference isn't clear, there really should be more of
	 * a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Specifies the source component
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Source Component</em>' container reference.
	 * @see #setSourceComponent(Component)
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getConnection_SourceComponent()
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getOutgoingConnections
	 * @model opposite="outgoingConnections" required="true"
	 * @generated
	 */
	Component getSourceComponent();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getSourceComponent <em>Source Component</em>}' container reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source Component</em>' container reference.
	 * @see #getSourceComponent()
	 * @generated
	 */
	void setSourceComponent(Component value);

	/**
	 * Returns the value of the '<em><b>Source Port</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Source Port</em>' reference isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Specifies the source port
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Source Port</em>' reference.
	 * @see #setSourcePort(Port)
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getConnection_SourcePort()
	 * @model required="true"
	 * @generated
	 */
	Port getSourcePort();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getSourcePort <em>Source Port</em>}' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source Port</em>' reference.
	 * @see #getSourcePort()
	 * @generated
	 */
	void setSourcePort(Port value);

	/**
	 * Returns the value of the '<em><b>Target Component</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getIncomingConnections <em>Incoming Connections</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Target Component</em>' reference isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Specifies the target component
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Target Component</em>' reference.
	 * @see #setTargetComponent(Component)
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getConnection_TargetComponent()
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getIncomingConnections
	 * @model opposite="incomingConnections" required="true"
	 * @generated
	 */
	Component getTargetComponent();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getTargetComponent <em>Target Component</em>}' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Target Component</em>' reference.
	 * @see #getTargetComponent()
	 * @generated
	 */
	void setTargetComponent(Component value);

} // Connection

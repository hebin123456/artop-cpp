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

import org.eclipse.emf.common.util.EList;
import org.eclipse.sphinx.examples.hummingbird20.common.Identifiable;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Component</b></em>'. <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents a Component model object
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getOutgoingConnections <em>Outgoing Connections</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getIncomingConnections <em>Incoming Connections</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getParameterValues <em>Parameter Values</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getParameterExpressions <em>Parameter Expressions</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getComponent()
 * @model
 * @generated
 */
public interface Component extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' reference isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reference to the type of this Component
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Type</em>' reference.
	 * @see #setType(ComponentType)
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getComponent_Type()
	 * @model
	 * @generated
	 */
	ComponentType getType();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getType <em>Type</em>}' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' reference.
	 * @see #getType()
	 * @generated
	 */
	void setType(ComponentType value);

	/**
	 * Returns the value of the '<em><b>Outgoing Connections</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getSourceComponent <em>Source Component</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Outgoing Connections</em>' containment reference list isn't clear, there really should
	 * be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Outgoing Connections</em>' containment reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getComponent_OutgoingConnections()
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getSourceComponent
	 * @model opposite="sourceComponent" containment="true"
	 * @generated
	 */
	EList<Connection> getOutgoingConnections();

	/**
	 * Returns the value of the '<em><b>Incoming Connections</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getTargetComponent <em>Target Component</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Incoming Connections</em>' reference list isn't clear, there really should be more of
	 * a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Incoming Connections</em>' reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getComponent_IncomingConnections()
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getTargetComponent
	 * @model opposite="targetComponent"
	 * @generated
	 */
	EList<Connection> getIncomingConnections();

	/**
	 * Returns the value of the '<em><b>Parameter Values</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parameter Values</em>' containment reference list isn't clear, there really should be
	 * more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Aggregates all ParameterValues with this Component...
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Parameter Values</em>' containment reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getComponent_ParameterValues()
	 * @model containment="true"
	 * @generated
	 */
	EList<ParameterValue> getParameterValues();

	/**
	 * Returns the value of the '<em><b>Parameter Expressions</b></em>' containment reference list. The list contents
	 * are of type {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression}. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parameter Expressions</em>' containment reference list isn't clear, there really
	 * should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Parameter Expressions</em>' containment reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getComponent_ParameterExpressions()
	 * @model containment="true"
	 * @generated
	 */
	EList<ParameterExpression> getParameterExpressions();

} // Component

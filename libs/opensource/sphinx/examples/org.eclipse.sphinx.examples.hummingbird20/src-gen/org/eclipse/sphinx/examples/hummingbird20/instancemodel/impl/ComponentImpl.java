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
package org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sphinx.examples.hummingbird20.common.impl.IdentifiableImpl;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Component</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ComponentImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ComponentImpl#getOutgoingConnections <em>Outgoing Connections</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ComponentImpl#getIncomingConnections <em>Incoming Connections</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ComponentImpl#getParameterValues <em>Parameter Values</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ComponentImpl#getParameterExpressions <em>Parameter Expressions</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ComponentImpl extends IdentifiableImpl implements Component {
	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' reference. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected ComponentType type;

	/**
	 * The cached value of the '{@link #getOutgoingConnections() <em>Outgoing Connections</em>}' containment reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getOutgoingConnections()
	 * @generated
	 * @ordered
	 */
	protected EList<Connection> outgoingConnections;

	/**
	 * The cached value of the '{@link #getIncomingConnections() <em>Incoming Connections</em>}' reference list. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getIncomingConnections()
	 * @generated
	 * @ordered
	 */
	protected EList<Connection> incomingConnections;

	/**
	 * The cached value of the '{@link #getParameterValues() <em>Parameter Values</em>}' containment reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getParameterValues()
	 * @generated
	 * @ordered
	 */
	protected EList<ParameterValue> parameterValues;

	/**
	 * The cached value of the '{@link #getParameterExpressions() <em>Parameter Expressions</em>}' containment reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getParameterExpressions()
	 * @generated
	 * @ordered
	 */
	protected EList<ParameterExpression> parameterExpressions;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected ComponentImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return InstanceModel20Package.Literals.COMPONENT;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ComponentType getType() {
		if (type != null && type.eIsProxy()) {
			InternalEObject oldType = (InternalEObject)type;
			type = (ComponentType)eResolveProxy(oldType);
			if (type != oldType) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, InstanceModel20Package.COMPONENT__TYPE, oldType, type));
			}
		}
		return type;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ComponentType basicGetType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setType(ComponentType newType) {
		ComponentType oldType = type;
		type = newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InstanceModel20Package.COMPONENT__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Connection> getOutgoingConnections() {
		if (outgoingConnections == null) {
			outgoingConnections = new EObjectContainmentWithInverseEList<Connection>(Connection.class, this, InstanceModel20Package.COMPONENT__OUTGOING_CONNECTIONS, InstanceModel20Package.CONNECTION__SOURCE_COMPONENT);
		}
		return outgoingConnections;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Connection> getIncomingConnections() {
		if (incomingConnections == null) {
			incomingConnections = new EObjectWithInverseResolvingEList<Connection>(Connection.class, this, InstanceModel20Package.COMPONENT__INCOMING_CONNECTIONS, InstanceModel20Package.CONNECTION__TARGET_COMPONENT);
		}
		return incomingConnections;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ParameterValue> getParameterValues() {
		if (parameterValues == null) {
			parameterValues = new EObjectContainmentEList<ParameterValue>(ParameterValue.class, this, InstanceModel20Package.COMPONENT__PARAMETER_VALUES);
		}
		return parameterValues;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ParameterExpression> getParameterExpressions() {
		if (parameterExpressions == null) {
			parameterExpressions = new EObjectContainmentEList<ParameterExpression>(ParameterExpression.class, this, InstanceModel20Package.COMPONENT__PARAMETER_EXPRESSIONS);
		}
		return parameterExpressions;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case InstanceModel20Package.COMPONENT__OUTGOING_CONNECTIONS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getOutgoingConnections()).basicAdd(otherEnd, msgs);
			case InstanceModel20Package.COMPONENT__INCOMING_CONNECTIONS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getIncomingConnections()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case InstanceModel20Package.COMPONENT__OUTGOING_CONNECTIONS:
				return ((InternalEList<?>)getOutgoingConnections()).basicRemove(otherEnd, msgs);
			case InstanceModel20Package.COMPONENT__INCOMING_CONNECTIONS:
				return ((InternalEList<?>)getIncomingConnections()).basicRemove(otherEnd, msgs);
			case InstanceModel20Package.COMPONENT__PARAMETER_VALUES:
				return ((InternalEList<?>)getParameterValues()).basicRemove(otherEnd, msgs);
			case InstanceModel20Package.COMPONENT__PARAMETER_EXPRESSIONS:
				return ((InternalEList<?>)getParameterExpressions()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case InstanceModel20Package.COMPONENT__TYPE:
				if (resolve) return getType();
				return basicGetType();
			case InstanceModel20Package.COMPONENT__OUTGOING_CONNECTIONS:
				return getOutgoingConnections();
			case InstanceModel20Package.COMPONENT__INCOMING_CONNECTIONS:
				return getIncomingConnections();
			case InstanceModel20Package.COMPONENT__PARAMETER_VALUES:
				return getParameterValues();
			case InstanceModel20Package.COMPONENT__PARAMETER_EXPRESSIONS:
				return getParameterExpressions();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case InstanceModel20Package.COMPONENT__TYPE:
				setType((ComponentType)newValue);
				return;
			case InstanceModel20Package.COMPONENT__OUTGOING_CONNECTIONS:
				getOutgoingConnections().clear();
				getOutgoingConnections().addAll((Collection<? extends Connection>)newValue);
				return;
			case InstanceModel20Package.COMPONENT__INCOMING_CONNECTIONS:
				getIncomingConnections().clear();
				getIncomingConnections().addAll((Collection<? extends Connection>)newValue);
				return;
			case InstanceModel20Package.COMPONENT__PARAMETER_VALUES:
				getParameterValues().clear();
				getParameterValues().addAll((Collection<? extends ParameterValue>)newValue);
				return;
			case InstanceModel20Package.COMPONENT__PARAMETER_EXPRESSIONS:
				getParameterExpressions().clear();
				getParameterExpressions().addAll((Collection<? extends ParameterExpression>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case InstanceModel20Package.COMPONENT__TYPE:
				setType((ComponentType)null);
				return;
			case InstanceModel20Package.COMPONENT__OUTGOING_CONNECTIONS:
				getOutgoingConnections().clear();
				return;
			case InstanceModel20Package.COMPONENT__INCOMING_CONNECTIONS:
				getIncomingConnections().clear();
				return;
			case InstanceModel20Package.COMPONENT__PARAMETER_VALUES:
				getParameterValues().clear();
				return;
			case InstanceModel20Package.COMPONENT__PARAMETER_EXPRESSIONS:
				getParameterExpressions().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case InstanceModel20Package.COMPONENT__TYPE:
				return type != null;
			case InstanceModel20Package.COMPONENT__OUTGOING_CONNECTIONS:
				return outgoingConnections != null && !outgoingConnections.isEmpty();
			case InstanceModel20Package.COMPONENT__INCOMING_CONNECTIONS:
				return incomingConnections != null && !incomingConnections.isEmpty();
			case InstanceModel20Package.COMPONENT__PARAMETER_VALUES:
				return parameterValues != null && !parameterValues.isEmpty();
			case InstanceModel20Package.COMPONENT__PARAMETER_EXPRESSIONS:
				return parameterExpressions != null && !parameterExpressions.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} // ComponentImpl

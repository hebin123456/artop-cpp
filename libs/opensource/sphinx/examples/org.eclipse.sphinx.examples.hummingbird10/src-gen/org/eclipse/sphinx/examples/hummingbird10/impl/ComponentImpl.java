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
package org.eclipse.sphinx.examples.hummingbird10.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sphinx.examples.hummingbird10.Component;
import org.eclipse.sphinx.examples.hummingbird10.Connection;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.examples.hummingbird10.Interface;
import org.eclipse.sphinx.examples.hummingbird10.Parameter;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Component</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.impl.ComponentImpl#getName <em>Name</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.impl.ComponentImpl#getOutgoingConnections <em>Outgoing
 * Connections</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.impl.ComponentImpl#getProvidedInterfaces <em>Provided Interfaces
 * </em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.impl.ComponentImpl#getParameters <em>Parameters</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.impl.ComponentImpl#getIncomingConnections <em>Incoming
 * Connections</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ComponentImpl extends EObjectImpl implements Component {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = ""; //$NON-NLS-1$

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The cached value of the '{@link #getOutgoingConnections() <em>Outgoing Connections</em>}' containment reference
	 * list. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getOutgoingConnections()
	 * @generated
	 * @ordered
	 */
	protected EList<Connection> outgoingConnections;

	/**
	 * The cached value of the '{@link #getProvidedInterfaces() <em>Provided Interfaces</em>}' reference list. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getProvidedInterfaces()
	 * @generated
	 * @ordered
	 */
	protected EList<Interface> providedInterfaces;

	/**
	 * The cached value of the '{@link #getParameters() <em>Parameters</em>}' containment reference list. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getParameters()
	 * @generated
	 * @ordered
	 */
	protected EList<Parameter> parameters;

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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ComponentImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return Hummingbird10Package.Literals.COMPONENT;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, Hummingbird10Package.COMPONENT__NAME, oldName, name));
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EList<Connection> getOutgoingConnections() {
		if (outgoingConnections == null) {
			outgoingConnections = new EObjectContainmentWithInverseEList<Connection>(Connection.class, this,
					Hummingbird10Package.COMPONENT__OUTGOING_CONNECTIONS, Hummingbird10Package.CONNECTION__SOURCE_COMPONENT);
		}
		return outgoingConnections;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EList<Interface> getProvidedInterfaces() {
		if (providedInterfaces == null) {
			providedInterfaces = new EObjectWithInverseResolvingEList.Unsettable.ManyInverse<Interface>(Interface.class, this,
					Hummingbird10Package.COMPONENT__PROVIDED_INTERFACES, Hummingbird10Package.INTERFACE__PROVIDING_COMPONENTS);
		}
		return providedInterfaces;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void unsetProvidedInterfaces() {
		if (providedInterfaces != null) {
			((InternalEList.Unsettable<?>) providedInterfaces).unset();
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean isSetProvidedInterfaces() {
		return providedInterfaces != null && ((InternalEList.Unsettable<?>) providedInterfaces).isSet();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EList<Parameter> getParameters() {
		if (parameters == null) {
			parameters = new EObjectContainmentEList<Parameter>(Parameter.class, this, Hummingbird10Package.COMPONENT__PARAMETERS);
		}
		return parameters;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EList<Connection> getIncomingConnections() {
		if (incomingConnections == null) {
			incomingConnections = new EObjectWithInverseResolvingEList<Connection>(Connection.class, this,
					Hummingbird10Package.COMPONENT__INCOMING_CONNECTIONS, Hummingbird10Package.CONNECTION__TARGET_COMPONENT);
		}
		return incomingConnections;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case Hummingbird10Package.COMPONENT__OUTGOING_CONNECTIONS:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getOutgoingConnections()).basicAdd(otherEnd, msgs);
		case Hummingbird10Package.COMPONENT__PROVIDED_INTERFACES:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getProvidedInterfaces()).basicAdd(otherEnd, msgs);
		case Hummingbird10Package.COMPONENT__INCOMING_CONNECTIONS:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getIncomingConnections()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case Hummingbird10Package.COMPONENT__OUTGOING_CONNECTIONS:
			return ((InternalEList<?>) getOutgoingConnections()).basicRemove(otherEnd, msgs);
		case Hummingbird10Package.COMPONENT__PROVIDED_INTERFACES:
			return ((InternalEList<?>) getProvidedInterfaces()).basicRemove(otherEnd, msgs);
		case Hummingbird10Package.COMPONENT__PARAMETERS:
			return ((InternalEList<?>) getParameters()).basicRemove(otherEnd, msgs);
		case Hummingbird10Package.COMPONENT__INCOMING_CONNECTIONS:
			return ((InternalEList<?>) getIncomingConnections()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case Hummingbird10Package.COMPONENT__NAME:
			return getName();
		case Hummingbird10Package.COMPONENT__OUTGOING_CONNECTIONS:
			return getOutgoingConnections();
		case Hummingbird10Package.COMPONENT__PROVIDED_INTERFACES:
			return getProvidedInterfaces();
		case Hummingbird10Package.COMPONENT__PARAMETERS:
			return getParameters();
		case Hummingbird10Package.COMPONENT__INCOMING_CONNECTIONS:
			return getIncomingConnections();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case Hummingbird10Package.COMPONENT__NAME:
			setName((String) newValue);
			return;
		case Hummingbird10Package.COMPONENT__OUTGOING_CONNECTIONS:
			getOutgoingConnections().clear();
			getOutgoingConnections().addAll((Collection<? extends Connection>) newValue);
			return;
		case Hummingbird10Package.COMPONENT__PROVIDED_INTERFACES:
			getProvidedInterfaces().clear();
			getProvidedInterfaces().addAll((Collection<? extends Interface>) newValue);
			return;
		case Hummingbird10Package.COMPONENT__PARAMETERS:
			getParameters().clear();
			getParameters().addAll((Collection<? extends Parameter>) newValue);
			return;
		case Hummingbird10Package.COMPONENT__INCOMING_CONNECTIONS:
			getIncomingConnections().clear();
			getIncomingConnections().addAll((Collection<? extends Connection>) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case Hummingbird10Package.COMPONENT__NAME:
			setName(NAME_EDEFAULT);
			return;
		case Hummingbird10Package.COMPONENT__OUTGOING_CONNECTIONS:
			getOutgoingConnections().clear();
			return;
		case Hummingbird10Package.COMPONENT__PROVIDED_INTERFACES:
			unsetProvidedInterfaces();
			return;
		case Hummingbird10Package.COMPONENT__PARAMETERS:
			getParameters().clear();
			return;
		case Hummingbird10Package.COMPONENT__INCOMING_CONNECTIONS:
			getIncomingConnections().clear();
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case Hummingbird10Package.COMPONENT__NAME:
			return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
		case Hummingbird10Package.COMPONENT__OUTGOING_CONNECTIONS:
			return outgoingConnections != null && !outgoingConnections.isEmpty();
		case Hummingbird10Package.COMPONENT__PROVIDED_INTERFACES:
			return isSetProvidedInterfaces();
		case Hummingbird10Package.COMPONENT__PARAMETERS:
			return parameters != null && !parameters.isEmpty();
		case Hummingbird10Package.COMPONENT__INCOMING_CONNECTIONS:
			return incomingConnections != null && !incomingConnections.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) {
			return super.toString();
		}

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: "); //$NON-NLS-1$
		result.append(name);
		result.append(')');
		return result.toString();
	}

} // ComponentImpl

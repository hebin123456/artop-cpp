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
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sphinx.examples.hummingbird10.Application;
import org.eclipse.sphinx.examples.hummingbird10.Component;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.examples.hummingbird10.Interface;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Application</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.impl.ApplicationImpl#getName <em>Name</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.impl.ApplicationImpl#getComponents <em>Components</em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.impl.ApplicationImpl#getInterfaces <em>Interfaces</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ApplicationImpl extends EObjectImpl implements Application {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

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
	 * The cached value of the '{@link #getComponents() <em>Components</em>}' containment reference list. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getComponents()
	 * @generated
	 * @ordered
	 */
	protected EList<Component> components;

	/**
	 * The cached value of the '{@link #getInterfaces() <em>Interfaces</em>}' containment reference list. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getInterfaces()
	 * @generated
	 * @ordered
	 */
	protected EList<Interface> interfaces;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ApplicationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return Hummingbird10Package.Literals.APPLICATION;
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
			eNotify(new ENotificationImpl(this, Notification.SET, Hummingbird10Package.APPLICATION__NAME, oldName, name));
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EList<Component> getComponents() {
		if (components == null) {
			components = new EObjectContainmentEList<Component>(Component.class, this, Hummingbird10Package.APPLICATION__COMPONENTS);
		}
		return components;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EList<Interface> getInterfaces() {
		if (interfaces == null) {
			interfaces = new EObjectContainmentEList<Interface>(Interface.class, this, Hummingbird10Package.APPLICATION__INTERFACES);
		}
		return interfaces;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case Hummingbird10Package.APPLICATION__COMPONENTS:
			return ((InternalEList<?>) getComponents()).basicRemove(otherEnd, msgs);
		case Hummingbird10Package.APPLICATION__INTERFACES:
			return ((InternalEList<?>) getInterfaces()).basicRemove(otherEnd, msgs);
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
		case Hummingbird10Package.APPLICATION__NAME:
			return getName();
		case Hummingbird10Package.APPLICATION__COMPONENTS:
			return getComponents();
		case Hummingbird10Package.APPLICATION__INTERFACES:
			return getInterfaces();
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
		case Hummingbird10Package.APPLICATION__NAME:
			setName((String) newValue);
			return;
		case Hummingbird10Package.APPLICATION__COMPONENTS:
			getComponents().clear();
			getComponents().addAll((Collection<? extends Component>) newValue);
			return;
		case Hummingbird10Package.APPLICATION__INTERFACES:
			getInterfaces().clear();
			getInterfaces().addAll((Collection<? extends Interface>) newValue);
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
		case Hummingbird10Package.APPLICATION__NAME:
			setName(NAME_EDEFAULT);
			return;
		case Hummingbird10Package.APPLICATION__COMPONENTS:
			getComponents().clear();
			return;
		case Hummingbird10Package.APPLICATION__INTERFACES:
			getInterfaces().clear();
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
		case Hummingbird10Package.APPLICATION__NAME:
			return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
		case Hummingbird10Package.APPLICATION__COMPONENTS:
			return components != null && !components.isEmpty();
		case Hummingbird10Package.APPLICATION__INTERFACES:
			return interfaces != null && !interfaces.isEmpty();
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

} // ApplicationImpl

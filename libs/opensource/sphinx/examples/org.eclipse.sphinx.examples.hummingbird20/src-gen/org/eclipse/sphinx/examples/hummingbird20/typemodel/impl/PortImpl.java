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
package org.eclipse.sphinx.examples.hummingbird20.typemodel.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sphinx.examples.hummingbird20.common.impl.IdentifiableImpl;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Port</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PortImpl#getOwner <em>Owner</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PortImpl#getRequiredInterface <em>Required Interface</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PortImpl#getMinProviderCount <em>Min Provider Count</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PortImpl#getMaxProviderCount <em>Max Provider Count</em>}</li>
 * </ul>
 *
 * @generated
 */
public class PortImpl extends IdentifiableImpl implements Port {
	/**
	 * The cached value of the '{@link #getRequiredInterface() <em>Required Interface</em>}' reference. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getRequiredInterface()
	 * @generated
	 * @ordered
	 */
	protected Interface requiredInterface;

	/**
	 * The default value of the '{@link #getMinProviderCount() <em>Min Provider Count</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getMinProviderCount()
	 * @generated
	 * @ordered
	 */
	protected static final int MIN_PROVIDER_COUNT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getMinProviderCount() <em>Min Provider Count</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getMinProviderCount()
	 * @generated
	 * @ordered
	 */
	protected int minProviderCount = MIN_PROVIDER_COUNT_EDEFAULT;

	/**
	 * The default value of the '{@link #getMaxProviderCount() <em>Max Provider Count</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getMaxProviderCount()
	 * @generated
	 * @ordered
	 */
	protected static final int MAX_PROVIDER_COUNT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getMaxProviderCount() <em>Max Provider Count</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getMaxProviderCount()
	 * @generated
	 * @ordered
	 */
	protected int maxProviderCount = MAX_PROVIDER_COUNT_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected PortImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TypeModel20Package.Literals.PORT;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ComponentType getOwner() {
		if (eContainerFeatureID() != TypeModel20Package.PORT__OWNER) return null;
		return (ComponentType)eInternalContainer();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetOwner(ComponentType newOwner, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newOwner, TypeModel20Package.PORT__OWNER, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOwner(ComponentType newOwner) {
		if (newOwner != eInternalContainer() || (eContainerFeatureID() != TypeModel20Package.PORT__OWNER && newOwner != null)) {
			if (EcoreUtil.isAncestor(this, newOwner))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString()); //$NON-NLS-1$
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newOwner != null)
				msgs = ((InternalEObject)newOwner).eInverseAdd(this, TypeModel20Package.COMPONENT_TYPE__PORTS, ComponentType.class, msgs);
			msgs = basicSetOwner(newOwner, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TypeModel20Package.PORT__OWNER, newOwner, newOwner));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Interface getRequiredInterface() {
		if (requiredInterface != null && requiredInterface.eIsProxy()) {
			InternalEObject oldRequiredInterface = (InternalEObject)requiredInterface;
			requiredInterface = (Interface)eResolveProxy(oldRequiredInterface);
			if (requiredInterface != oldRequiredInterface) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TypeModel20Package.PORT__REQUIRED_INTERFACE, oldRequiredInterface, requiredInterface));
			}
		}
		return requiredInterface;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Interface basicGetRequiredInterface() {
		return requiredInterface;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRequiredInterface(Interface newRequiredInterface, NotificationChain msgs) {
		Interface oldRequiredInterface = requiredInterface;
		requiredInterface = newRequiredInterface;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TypeModel20Package.PORT__REQUIRED_INTERFACE, oldRequiredInterface, newRequiredInterface);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRequiredInterface(Interface newRequiredInterface) {
		if (newRequiredInterface != requiredInterface) {
			NotificationChain msgs = null;
			if (requiredInterface != null)
				msgs = ((InternalEObject)requiredInterface).eInverseRemove(this, TypeModel20Package.INTERFACE__REQUIRING_PORTS, Interface.class, msgs);
			if (newRequiredInterface != null)
				msgs = ((InternalEObject)newRequiredInterface).eInverseAdd(this, TypeModel20Package.INTERFACE__REQUIRING_PORTS, Interface.class, msgs);
			msgs = basicSetRequiredInterface(newRequiredInterface, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TypeModel20Package.PORT__REQUIRED_INTERFACE, newRequiredInterface, newRequiredInterface));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getMinProviderCount() {
		return minProviderCount;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMinProviderCount(int newMinProviderCount) {
		int oldMinProviderCount = minProviderCount;
		minProviderCount = newMinProviderCount;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TypeModel20Package.PORT__MIN_PROVIDER_COUNT, oldMinProviderCount, minProviderCount));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getMaxProviderCount() {
		return maxProviderCount;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMaxProviderCount(int newMaxProviderCount) {
		int oldMaxProviderCount = maxProviderCount;
		maxProviderCount = newMaxProviderCount;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TypeModel20Package.PORT__MAX_PROVIDER_COUNT, oldMaxProviderCount, maxProviderCount));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case TypeModel20Package.PORT__OWNER:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetOwner((ComponentType)otherEnd, msgs);
			case TypeModel20Package.PORT__REQUIRED_INTERFACE:
				if (requiredInterface != null)
					msgs = ((InternalEObject)requiredInterface).eInverseRemove(this, TypeModel20Package.INTERFACE__REQUIRING_PORTS, Interface.class, msgs);
				return basicSetRequiredInterface((Interface)otherEnd, msgs);
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
			case TypeModel20Package.PORT__OWNER:
				return basicSetOwner(null, msgs);
			case TypeModel20Package.PORT__REQUIRED_INTERFACE:
				return basicSetRequiredInterface(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
			case TypeModel20Package.PORT__OWNER:
				return eInternalContainer().eInverseRemove(this, TypeModel20Package.COMPONENT_TYPE__PORTS, ComponentType.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TypeModel20Package.PORT__OWNER:
				return getOwner();
			case TypeModel20Package.PORT__REQUIRED_INTERFACE:
				if (resolve) return getRequiredInterface();
				return basicGetRequiredInterface();
			case TypeModel20Package.PORT__MIN_PROVIDER_COUNT:
				return getMinProviderCount();
			case TypeModel20Package.PORT__MAX_PROVIDER_COUNT:
				return getMaxProviderCount();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case TypeModel20Package.PORT__OWNER:
				setOwner((ComponentType)newValue);
				return;
			case TypeModel20Package.PORT__REQUIRED_INTERFACE:
				setRequiredInterface((Interface)newValue);
				return;
			case TypeModel20Package.PORT__MIN_PROVIDER_COUNT:
				setMinProviderCount((Integer)newValue);
				return;
			case TypeModel20Package.PORT__MAX_PROVIDER_COUNT:
				setMaxProviderCount((Integer)newValue);
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
			case TypeModel20Package.PORT__OWNER:
				setOwner((ComponentType)null);
				return;
			case TypeModel20Package.PORT__REQUIRED_INTERFACE:
				setRequiredInterface((Interface)null);
				return;
			case TypeModel20Package.PORT__MIN_PROVIDER_COUNT:
				setMinProviderCount(MIN_PROVIDER_COUNT_EDEFAULT);
				return;
			case TypeModel20Package.PORT__MAX_PROVIDER_COUNT:
				setMaxProviderCount(MAX_PROVIDER_COUNT_EDEFAULT);
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
			case TypeModel20Package.PORT__OWNER:
				return getOwner() != null;
			case TypeModel20Package.PORT__REQUIRED_INTERFACE:
				return requiredInterface != null;
			case TypeModel20Package.PORT__MIN_PROVIDER_COUNT:
				return minProviderCount != MIN_PROVIDER_COUNT_EDEFAULT;
			case TypeModel20Package.PORT__MAX_PROVIDER_COUNT:
				return maxProviderCount != MAX_PROVIDER_COUNT_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (minProviderCount: "); //$NON-NLS-1$
		result.append(minProviderCount);
		result.append(", maxProviderCount: "); //$NON-NLS-1$
		result.append(maxProviderCount);
		result.append(')');
		return result.toString();
	}

} // PortImpl

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

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sphinx.examples.hummingbird20.common.impl.IdentifiableImpl;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Interface</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.InterfaceImpl#getProvidingComponentTypes <em>Providing Component Types</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.InterfaceImpl#getRequiringPorts <em>Requiring Ports</em>}</li>
 * </ul>
 *
 * @generated
 */
public class InterfaceImpl extends IdentifiableImpl implements Interface {
	/**
	 * The cached value of the '{@link #getProvidingComponentTypes() <em>Providing Component Types</em>}' reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getProvidingComponentTypes()
	 * @generated
	 * @ordered
	 */
	protected EList<ComponentType> providingComponentTypes;

	/**
	 * The cached value of the '{@link #getRequiringPorts() <em>Requiring Ports</em>}' reference list. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getRequiringPorts()
	 * @generated
	 * @ordered
	 */
	protected EList<Port> requiringPorts;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected InterfaceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TypeModel20Package.Literals.INTERFACE;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ComponentType> getProvidingComponentTypes() {
		if (providingComponentTypes == null) {
			providingComponentTypes = new EObjectWithInverseResolvingEList.ManyInverse<ComponentType>(ComponentType.class, this, TypeModel20Package.INTERFACE__PROVIDING_COMPONENT_TYPES, TypeModel20Package.COMPONENT_TYPE__PROVIDED_INTERFACES);
		}
		return providingComponentTypes;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Port> getRequiringPorts() {
		if (requiringPorts == null) {
			requiringPorts = new EObjectWithInverseResolvingEList<Port>(Port.class, this, TypeModel20Package.INTERFACE__REQUIRING_PORTS, TypeModel20Package.PORT__REQUIRED_INTERFACE);
		}
		return requiringPorts;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case TypeModel20Package.INTERFACE__PROVIDING_COMPONENT_TYPES:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getProvidingComponentTypes()).basicAdd(otherEnd, msgs);
			case TypeModel20Package.INTERFACE__REQUIRING_PORTS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getRequiringPorts()).basicAdd(otherEnd, msgs);
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
			case TypeModel20Package.INTERFACE__PROVIDING_COMPONENT_TYPES:
				return ((InternalEList<?>)getProvidingComponentTypes()).basicRemove(otherEnd, msgs);
			case TypeModel20Package.INTERFACE__REQUIRING_PORTS:
				return ((InternalEList<?>)getRequiringPorts()).basicRemove(otherEnd, msgs);
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
			case TypeModel20Package.INTERFACE__PROVIDING_COMPONENT_TYPES:
				return getProvidingComponentTypes();
			case TypeModel20Package.INTERFACE__REQUIRING_PORTS:
				return getRequiringPorts();
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
			case TypeModel20Package.INTERFACE__PROVIDING_COMPONENT_TYPES:
				getProvidingComponentTypes().clear();
				getProvidingComponentTypes().addAll((Collection<? extends ComponentType>)newValue);
				return;
			case TypeModel20Package.INTERFACE__REQUIRING_PORTS:
				getRequiringPorts().clear();
				getRequiringPorts().addAll((Collection<? extends Port>)newValue);
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
			case TypeModel20Package.INTERFACE__PROVIDING_COMPONENT_TYPES:
				getProvidingComponentTypes().clear();
				return;
			case TypeModel20Package.INTERFACE__REQUIRING_PORTS:
				getRequiringPorts().clear();
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
			case TypeModel20Package.INTERFACE__PROVIDING_COMPONENT_TYPES:
				return providingComponentTypes != null && !providingComponentTypes.isEmpty();
			case TypeModel20Package.INTERFACE__REQUIRING_PORTS:
				return requiringPorts != null && !requiringPorts.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} // InterfaceImpl

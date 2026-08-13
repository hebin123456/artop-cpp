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
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sphinx.examples.hummingbird20.common.impl.IdentifiableImpl;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Component Type</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.ComponentTypeImpl#getProvidedInterfaces <em>Provided Interfaces</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.ComponentTypeImpl#getPorts <em>Ports</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.ComponentTypeImpl#getParameters <em>Parameters</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ComponentTypeImpl extends IdentifiableImpl implements ComponentType {
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
	 * The cached value of the '{@link #getPorts() <em>Ports</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPorts()
	 * @generated
	 * @ordered
	 */
	protected EList<Port> ports;

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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected ComponentTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TypeModel20Package.Literals.COMPONENT_TYPE;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Interface> getProvidedInterfaces() {
		if (providedInterfaces == null) {
			providedInterfaces = new EObjectWithInverseResolvingEList.Unsettable.ManyInverse<Interface>(Interface.class, this, TypeModel20Package.COMPONENT_TYPE__PROVIDED_INTERFACES, TypeModel20Package.INTERFACE__PROVIDING_COMPONENT_TYPES);
		}
		return providedInterfaces;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetProvidedInterfaces() {
		if (providedInterfaces != null) ((InternalEList.Unsettable<?>)providedInterfaces).unset();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetProvidedInterfaces() {
		return providedInterfaces != null && ((InternalEList.Unsettable<?>)providedInterfaces).isSet();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Port> getPorts() {
		if (ports == null) {
			ports = new EObjectContainmentWithInverseEList<Port>(Port.class, this, TypeModel20Package.COMPONENT_TYPE__PORTS, TypeModel20Package.PORT__OWNER);
		}
		return ports;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Parameter> getParameters() {
		if (parameters == null) {
			parameters = new EObjectContainmentEList<Parameter>(Parameter.class, this, TypeModel20Package.COMPONENT_TYPE__PARAMETERS);
		}
		return parameters;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case TypeModel20Package.COMPONENT_TYPE__PROVIDED_INTERFACES:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getProvidedInterfaces()).basicAdd(otherEnd, msgs);
			case TypeModel20Package.COMPONENT_TYPE__PORTS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getPorts()).basicAdd(otherEnd, msgs);
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
			case TypeModel20Package.COMPONENT_TYPE__PROVIDED_INTERFACES:
				return ((InternalEList<?>)getProvidedInterfaces()).basicRemove(otherEnd, msgs);
			case TypeModel20Package.COMPONENT_TYPE__PORTS:
				return ((InternalEList<?>)getPorts()).basicRemove(otherEnd, msgs);
			case TypeModel20Package.COMPONENT_TYPE__PARAMETERS:
				return ((InternalEList<?>)getParameters()).basicRemove(otherEnd, msgs);
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
			case TypeModel20Package.COMPONENT_TYPE__PROVIDED_INTERFACES:
				return getProvidedInterfaces();
			case TypeModel20Package.COMPONENT_TYPE__PORTS:
				return getPorts();
			case TypeModel20Package.COMPONENT_TYPE__PARAMETERS:
				return getParameters();
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
			case TypeModel20Package.COMPONENT_TYPE__PROVIDED_INTERFACES:
				getProvidedInterfaces().clear();
				getProvidedInterfaces().addAll((Collection<? extends Interface>)newValue);
				return;
			case TypeModel20Package.COMPONENT_TYPE__PORTS:
				getPorts().clear();
				getPorts().addAll((Collection<? extends Port>)newValue);
				return;
			case TypeModel20Package.COMPONENT_TYPE__PARAMETERS:
				getParameters().clear();
				getParameters().addAll((Collection<? extends Parameter>)newValue);
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
			case TypeModel20Package.COMPONENT_TYPE__PROVIDED_INTERFACES:
				unsetProvidedInterfaces();
				return;
			case TypeModel20Package.COMPONENT_TYPE__PORTS:
				getPorts().clear();
				return;
			case TypeModel20Package.COMPONENT_TYPE__PARAMETERS:
				getParameters().clear();
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
			case TypeModel20Package.COMPONENT_TYPE__PROVIDED_INTERFACES:
				return isSetProvidedInterfaces();
			case TypeModel20Package.COMPONENT_TYPE__PORTS:
				return ports != null && !ports.isEmpty();
			case TypeModel20Package.COMPONENT_TYPE__PARAMETERS:
				return parameters != null && !parameters.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} // ComponentTypeImpl

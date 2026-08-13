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
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EStringToStringMapEntryImpl;
import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sphinx.examples.hummingbird20.common.impl.IdentifiableImpl;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Platform</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PlatformImpl#getComponentTypes <em>Component Types</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PlatformImpl#getInterfaces <em>Interfaces</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PlatformImpl#getMixed <em>Mixed</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PlatformImpl#getXSISchemaLocation <em>XSI Schema Location</em>}</li>
 * </ul>
 *
 * @generated
 */
public class PlatformImpl extends IdentifiableImpl implements Platform {
	/**
	 * The cached value of the '{@link #getComponentTypes() <em>Component Types</em>}' containment reference list. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getComponentTypes()
	 * @generated
	 * @ordered
	 */
	protected EList<ComponentType> componentTypes;

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
	 * The cached value of the '{@link #getMixed() <em>Mixed</em>}' attribute list.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getMixed()
	 * @generated
	 * @ordered
	 */
	protected FeatureMap mixed;

	/**
	 * The cached value of the '{@link #getXSISchemaLocation() <em>XSI Schema Location</em>}' map.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @see #getXSISchemaLocation()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, String> xSISchemaLocation;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected PlatformImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TypeModel20Package.Literals.PLATFORM;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ComponentType> getComponentTypes() {
		if (componentTypes == null) {
			componentTypes = new EObjectContainmentEList<ComponentType>(ComponentType.class, this, TypeModel20Package.PLATFORM__COMPONENT_TYPES);
		}
		return componentTypes;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Interface> getInterfaces() {
		if (interfaces == null) {
			interfaces = new EObjectContainmentEList<Interface>(Interface.class, this, TypeModel20Package.PLATFORM__INTERFACES);
		}
		return interfaces;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FeatureMap getMixed() {
		if (mixed == null) {
			mixed = new BasicFeatureMap(this, TypeModel20Package.PLATFORM__MIXED);
		}
		return mixed;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EMap<String, String> getXSISchemaLocation() {
		if (xSISchemaLocation == null) {
			xSISchemaLocation = new EcoreEMap<String,String>(EcorePackage.Literals.ESTRING_TO_STRING_MAP_ENTRY, EStringToStringMapEntryImpl.class, this, TypeModel20Package.PLATFORM__XSI_SCHEMA_LOCATION);
		}
		return xSISchemaLocation;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case TypeModel20Package.PLATFORM__COMPONENT_TYPES:
				return ((InternalEList<?>)getComponentTypes()).basicRemove(otherEnd, msgs);
			case TypeModel20Package.PLATFORM__INTERFACES:
				return ((InternalEList<?>)getInterfaces()).basicRemove(otherEnd, msgs);
			case TypeModel20Package.PLATFORM__MIXED:
				return ((InternalEList<?>)getMixed()).basicRemove(otherEnd, msgs);
			case TypeModel20Package.PLATFORM__XSI_SCHEMA_LOCATION:
				return ((InternalEList<?>)getXSISchemaLocation()).basicRemove(otherEnd, msgs);
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
			case TypeModel20Package.PLATFORM__COMPONENT_TYPES:
				return getComponentTypes();
			case TypeModel20Package.PLATFORM__INTERFACES:
				return getInterfaces();
			case TypeModel20Package.PLATFORM__MIXED:
				if (coreType) return getMixed();
				return ((FeatureMap.Internal)getMixed()).getWrapper();
			case TypeModel20Package.PLATFORM__XSI_SCHEMA_LOCATION:
				if (coreType) return getXSISchemaLocation();
				else return getXSISchemaLocation().map();
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
			case TypeModel20Package.PLATFORM__COMPONENT_TYPES:
				getComponentTypes().clear();
				getComponentTypes().addAll((Collection<? extends ComponentType>)newValue);
				return;
			case TypeModel20Package.PLATFORM__INTERFACES:
				getInterfaces().clear();
				getInterfaces().addAll((Collection<? extends Interface>)newValue);
				return;
			case TypeModel20Package.PLATFORM__MIXED:
				((FeatureMap.Internal)getMixed()).set(newValue);
				return;
			case TypeModel20Package.PLATFORM__XSI_SCHEMA_LOCATION:
				((EStructuralFeature.Setting)getXSISchemaLocation()).set(newValue);
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
			case TypeModel20Package.PLATFORM__COMPONENT_TYPES:
				getComponentTypes().clear();
				return;
			case TypeModel20Package.PLATFORM__INTERFACES:
				getInterfaces().clear();
				return;
			case TypeModel20Package.PLATFORM__MIXED:
				getMixed().clear();
				return;
			case TypeModel20Package.PLATFORM__XSI_SCHEMA_LOCATION:
				getXSISchemaLocation().clear();
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
			case TypeModel20Package.PLATFORM__COMPONENT_TYPES:
				return componentTypes != null && !componentTypes.isEmpty();
			case TypeModel20Package.PLATFORM__INTERFACES:
				return interfaces != null && !interfaces.isEmpty();
			case TypeModel20Package.PLATFORM__MIXED:
				return mixed != null && !mixed.isEmpty();
			case TypeModel20Package.PLATFORM__XSI_SCHEMA_LOCATION:
				return xSISchemaLocation != null && !xSISchemaLocation.isEmpty();
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
		result.append(" (mixed: "); //$NON-NLS-1$
		result.append(mixed);
		result.append(')');
		return result.toString();
	}

} // PlatformImpl

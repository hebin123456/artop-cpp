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
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Application</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ApplicationImpl#getComponents <em>Components</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ApplicationImpl#getMixed <em>Mixed</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ApplicationImpl#getXSISchemaLocation <em>XSI Schema Location</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ApplicationImpl#getMixedOuterContent <em>Mixed Outer Content</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ApplicationImpl extends IdentifiableImpl implements Application {
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
	 * The cached value of the '{@link #getMixedOuterContent() <em>Mixed Outer Content</em>}' attribute list. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getMixedOuterContent()
	 * @generated
	 * @ordered
	 */
	protected FeatureMap mixedOuterContent;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected ApplicationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return InstanceModel20Package.Literals.APPLICATION;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Component> getComponents() {
		if (components == null) {
			components = new EObjectContainmentEList<Component>(Component.class, this, InstanceModel20Package.APPLICATION__COMPONENTS);
		}
		return components;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FeatureMap getMixed() {
		if (mixed == null) {
			mixed = new BasicFeatureMap(this, InstanceModel20Package.APPLICATION__MIXED);
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
			xSISchemaLocation = new EcoreEMap<String,String>(EcorePackage.Literals.ESTRING_TO_STRING_MAP_ENTRY, EStringToStringMapEntryImpl.class, this, InstanceModel20Package.APPLICATION__XSI_SCHEMA_LOCATION);
		}
		return xSISchemaLocation;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public FeatureMap getMixedOuterContent() {
		if (mixedOuterContent == null) {
			mixedOuterContent = new BasicFeatureMap(this, InstanceModel20Package.APPLICATION__MIXED_OUTER_CONTENT);
		}
		return mixedOuterContent;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case InstanceModel20Package.APPLICATION__COMPONENTS:
				return ((InternalEList<?>)getComponents()).basicRemove(otherEnd, msgs);
			case InstanceModel20Package.APPLICATION__MIXED:
				return ((InternalEList<?>)getMixed()).basicRemove(otherEnd, msgs);
			case InstanceModel20Package.APPLICATION__XSI_SCHEMA_LOCATION:
				return ((InternalEList<?>)getXSISchemaLocation()).basicRemove(otherEnd, msgs);
			case InstanceModel20Package.APPLICATION__MIXED_OUTER_CONTENT:
				return ((InternalEList<?>)getMixedOuterContent()).basicRemove(otherEnd, msgs);
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
			case InstanceModel20Package.APPLICATION__COMPONENTS:
				return getComponents();
			case InstanceModel20Package.APPLICATION__MIXED:
				if (coreType) return getMixed();
				return ((FeatureMap.Internal)getMixed()).getWrapper();
			case InstanceModel20Package.APPLICATION__XSI_SCHEMA_LOCATION:
				if (coreType) return getXSISchemaLocation();
				else return getXSISchemaLocation().map();
			case InstanceModel20Package.APPLICATION__MIXED_OUTER_CONTENT:
				if (coreType) return getMixedOuterContent();
				return ((FeatureMap.Internal)getMixedOuterContent()).getWrapper();
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
			case InstanceModel20Package.APPLICATION__COMPONENTS:
				getComponents().clear();
				getComponents().addAll((Collection<? extends Component>)newValue);
				return;
			case InstanceModel20Package.APPLICATION__MIXED:
				((FeatureMap.Internal)getMixed()).set(newValue);
				return;
			case InstanceModel20Package.APPLICATION__XSI_SCHEMA_LOCATION:
				((EStructuralFeature.Setting)getXSISchemaLocation()).set(newValue);
				return;
			case InstanceModel20Package.APPLICATION__MIXED_OUTER_CONTENT:
				((FeatureMap.Internal)getMixedOuterContent()).set(newValue);
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
			case InstanceModel20Package.APPLICATION__COMPONENTS:
				getComponents().clear();
				return;
			case InstanceModel20Package.APPLICATION__MIXED:
				getMixed().clear();
				return;
			case InstanceModel20Package.APPLICATION__XSI_SCHEMA_LOCATION:
				getXSISchemaLocation().clear();
				return;
			case InstanceModel20Package.APPLICATION__MIXED_OUTER_CONTENT:
				getMixedOuterContent().clear();
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
			case InstanceModel20Package.APPLICATION__COMPONENTS:
				return components != null && !components.isEmpty();
			case InstanceModel20Package.APPLICATION__MIXED:
				return mixed != null && !mixed.isEmpty();
			case InstanceModel20Package.APPLICATION__XSI_SCHEMA_LOCATION:
				return xSISchemaLocation != null && !xSISchemaLocation.isEmpty();
			case InstanceModel20Package.APPLICATION__MIXED_OUTER_CONTENT:
				return mixedOuterContent != null && !mixedOuterContent.isEmpty();
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
		result.append(", mixedOuterContent: "); //$NON-NLS-1$
		result.append(mixedOuterContent);
		result.append(')');
		return result.toString();
	}

} // ApplicationImpl

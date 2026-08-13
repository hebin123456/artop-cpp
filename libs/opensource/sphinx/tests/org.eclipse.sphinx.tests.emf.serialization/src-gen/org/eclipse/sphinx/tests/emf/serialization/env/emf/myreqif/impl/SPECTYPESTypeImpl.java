/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage;
import org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.RELATIONGROUPTYPE;
import org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.SPECIFICATIONTYPE;
import org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.SPECOBJECTTYPE;
import org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.SPECRELATIONTYPE;
import org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.SPECTYPESType;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>SPECTYPES Type</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.impl.SPECTYPESTypeImpl#getGroup <em>Group</em>}</li>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.impl.SPECTYPESTypeImpl#getRELATIONGROUPTYPE <em>
 * RELATIONGROUPTYPE</em>}</li>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.impl.SPECTYPESTypeImpl#getSPECOBJECTTYPE <em>
 * SPECOBJECTTYPE</em>}</li>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.impl.SPECTYPESTypeImpl#getSPECRELATIONTYPE <em>
 * SPECRELATIONTYPE</em>}</li>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.impl.SPECTYPESTypeImpl#getSPECIFICATIONTYPE <em>
 * SPECIFICATIONTYPE</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SPECTYPESTypeImpl extends EObjectImpl implements SPECTYPESType {
	/**
	 * The cached value of the '{@link #getGroup() <em>Group</em>}' attribute list. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @see #getGroup()
	 * @generated
	 * @ordered
	 */
	protected FeatureMap group;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected SPECTYPESTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MyreqifPackage.Literals.SPECTYPES_TYPE;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public FeatureMap getGroup() {
		if (group == null) {
			group = new BasicFeatureMap(this, MyreqifPackage.SPECTYPES_TYPE__GROUP);
		}
		return group;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EList<RELATIONGROUPTYPE> getRELATIONGROUPTYPE() {
		return getGroup().list(MyreqifPackage.Literals.SPECTYPES_TYPE__RELATIONGROUPTYPE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EList<SPECOBJECTTYPE> getSPECOBJECTTYPE() {
		return getGroup().list(MyreqifPackage.Literals.SPECTYPES_TYPE__SPECOBJECTTYPE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EList<SPECRELATIONTYPE> getSPECRELATIONTYPE() {
		return getGroup().list(MyreqifPackage.Literals.SPECTYPES_TYPE__SPECRELATIONTYPE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EList<SPECIFICATIONTYPE> getSPECIFICATIONTYPE() {
		return getGroup().list(MyreqifPackage.Literals.SPECTYPES_TYPE__SPECIFICATIONTYPE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case MyreqifPackage.SPECTYPES_TYPE__GROUP:
			return ((InternalEList<?>) getGroup()).basicRemove(otherEnd, msgs);
		case MyreqifPackage.SPECTYPES_TYPE__RELATIONGROUPTYPE:
			return ((InternalEList<?>) getRELATIONGROUPTYPE()).basicRemove(otherEnd, msgs);
		case MyreqifPackage.SPECTYPES_TYPE__SPECOBJECTTYPE:
			return ((InternalEList<?>) getSPECOBJECTTYPE()).basicRemove(otherEnd, msgs);
		case MyreqifPackage.SPECTYPES_TYPE__SPECRELATIONTYPE:
			return ((InternalEList<?>) getSPECRELATIONTYPE()).basicRemove(otherEnd, msgs);
		case MyreqifPackage.SPECTYPES_TYPE__SPECIFICATIONTYPE:
			return ((InternalEList<?>) getSPECIFICATIONTYPE()).basicRemove(otherEnd, msgs);
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
		case MyreqifPackage.SPECTYPES_TYPE__GROUP:
			if (coreType) {
				return getGroup();
			}
			return ((FeatureMap.Internal) getGroup()).getWrapper();
		case MyreqifPackage.SPECTYPES_TYPE__RELATIONGROUPTYPE:
			return getRELATIONGROUPTYPE();
		case MyreqifPackage.SPECTYPES_TYPE__SPECOBJECTTYPE:
			return getSPECOBJECTTYPE();
		case MyreqifPackage.SPECTYPES_TYPE__SPECRELATIONTYPE:
			return getSPECRELATIONTYPE();
		case MyreqifPackage.SPECTYPES_TYPE__SPECIFICATIONTYPE:
			return getSPECIFICATIONTYPE();
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
		case MyreqifPackage.SPECTYPES_TYPE__GROUP:
			((FeatureMap.Internal) getGroup()).set(newValue);
			return;
		case MyreqifPackage.SPECTYPES_TYPE__RELATIONGROUPTYPE:
			getRELATIONGROUPTYPE().clear();
			getRELATIONGROUPTYPE().addAll((Collection<? extends RELATIONGROUPTYPE>) newValue);
			return;
		case MyreqifPackage.SPECTYPES_TYPE__SPECOBJECTTYPE:
			getSPECOBJECTTYPE().clear();
			getSPECOBJECTTYPE().addAll((Collection<? extends SPECOBJECTTYPE>) newValue);
			return;
		case MyreqifPackage.SPECTYPES_TYPE__SPECRELATIONTYPE:
			getSPECRELATIONTYPE().clear();
			getSPECRELATIONTYPE().addAll((Collection<? extends SPECRELATIONTYPE>) newValue);
			return;
		case MyreqifPackage.SPECTYPES_TYPE__SPECIFICATIONTYPE:
			getSPECIFICATIONTYPE().clear();
			getSPECIFICATIONTYPE().addAll((Collection<? extends SPECIFICATIONTYPE>) newValue);
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
		case MyreqifPackage.SPECTYPES_TYPE__GROUP:
			getGroup().clear();
			return;
		case MyreqifPackage.SPECTYPES_TYPE__RELATIONGROUPTYPE:
			getRELATIONGROUPTYPE().clear();
			return;
		case MyreqifPackage.SPECTYPES_TYPE__SPECOBJECTTYPE:
			getSPECOBJECTTYPE().clear();
			return;
		case MyreqifPackage.SPECTYPES_TYPE__SPECRELATIONTYPE:
			getSPECRELATIONTYPE().clear();
			return;
		case MyreqifPackage.SPECTYPES_TYPE__SPECIFICATIONTYPE:
			getSPECIFICATIONTYPE().clear();
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
		case MyreqifPackage.SPECTYPES_TYPE__GROUP:
			return group != null && !group.isEmpty();
		case MyreqifPackage.SPECTYPES_TYPE__RELATIONGROUPTYPE:
			return !getRELATIONGROUPTYPE().isEmpty();
		case MyreqifPackage.SPECTYPES_TYPE__SPECOBJECTTYPE:
			return !getSPECOBJECTTYPE().isEmpty();
		case MyreqifPackage.SPECTYPES_TYPE__SPECRELATIONTYPE:
			return !getSPECRELATIONTYPE().isEmpty();
		case MyreqifPackage.SPECTYPES_TYPE__SPECIFICATIONTYPE:
			return !getSPECIFICATIONTYPE().isEmpty();
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
		result.append(" (group: ");
		result.append(group);
		result.append(')');
		return result.toString();
	}

} // SPECTYPESTypeImpl

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
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sphinx.emf.ecore.ExtendedEObjectImpl;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20EObjectImpl;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Formula;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Parameter Expression</b></em>'. <!--
 * end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ParameterExpressionImpl#getMixed <em>Mixed</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ParameterExpressionImpl#getExpressions <em>Expressions</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ParameterExpressionImpl extends Hummingbird20EObjectImpl implements ParameterExpression {
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected ParameterExpressionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return InstanceModel20Package.Literals.PARAMETER_EXPRESSION;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FeatureMap getMixed() {
		if (mixed == null) {
			mixed = new BasicFeatureMap(this, InstanceModel20Package.PARAMETER_EXPRESSION__MIXED);
		}
		return mixed;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Formula> getExpressions() {
		return getMixed().list(InstanceModel20Package.Literals.PARAMETER_EXPRESSION__EXPRESSIONS);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case InstanceModel20Package.PARAMETER_EXPRESSION__MIXED:
				return ((InternalEList<?>)getMixed()).basicRemove(otherEnd, msgs);
			case InstanceModel20Package.PARAMETER_EXPRESSION__EXPRESSIONS:
				return ((InternalEList<?>)getExpressions()).basicRemove(otherEnd, msgs);
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
			case InstanceModel20Package.PARAMETER_EXPRESSION__MIXED:
				if (coreType) return getMixed();
				return ((FeatureMap.Internal)getMixed()).getWrapper();
			case InstanceModel20Package.PARAMETER_EXPRESSION__EXPRESSIONS:
				return getExpressions();
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
			case InstanceModel20Package.PARAMETER_EXPRESSION__MIXED:
				((FeatureMap.Internal)getMixed()).set(newValue);
				return;
			case InstanceModel20Package.PARAMETER_EXPRESSION__EXPRESSIONS:
				getExpressions().clear();
				getExpressions().addAll((Collection<? extends Formula>)newValue);
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
			case InstanceModel20Package.PARAMETER_EXPRESSION__MIXED:
				getMixed().clear();
				return;
			case InstanceModel20Package.PARAMETER_EXPRESSION__EXPRESSIONS:
				getExpressions().clear();
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
			case InstanceModel20Package.PARAMETER_EXPRESSION__MIXED:
				return mixed != null && !mixed.isEmpty();
			case InstanceModel20Package.PARAMETER_EXPRESSION__EXPRESSIONS:
				return !getExpressions().isEmpty();
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

} // ParameterExpressionImpl

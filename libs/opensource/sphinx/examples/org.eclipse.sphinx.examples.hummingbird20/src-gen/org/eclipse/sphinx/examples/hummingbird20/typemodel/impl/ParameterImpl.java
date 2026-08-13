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
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.sphinx.examples.hummingbird20.common.impl.IdentifiableImpl;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Parameter</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.ParameterImpl#getDataType <em>Data Type</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.ParameterImpl#isOptional <em>Optional</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ParameterImpl extends IdentifiableImpl implements Parameter {
	/**
	 * The default value of the '{@link #getDataType() <em>Data Type</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getDataType()
	 * @generated
	 * @ordered
	 */
	protected static final String DATA_TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDataType() <em>Data Type</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getDataType()
	 * @generated
	 * @ordered
	 */
	protected String dataType = DATA_TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #isOptional() <em>Optional</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #isOptional()
	 * @generated
	 * @ordered
	 */
	protected static final boolean OPTIONAL_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isOptional() <em>Optional</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #isOptional()
	 * @generated
	 * @ordered
	 */
	protected boolean optional = OPTIONAL_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected ParameterImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TypeModel20Package.Literals.PARAMETER;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDataType() {
		return dataType;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDataType(String newDataType) {
		String oldDataType = dataType;
		dataType = newDataType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TypeModel20Package.PARAMETER__DATA_TYPE, oldDataType, dataType));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isOptional() {
		return optional;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOptional(boolean newOptional) {
		boolean oldOptional = optional;
		optional = newOptional;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TypeModel20Package.PARAMETER__OPTIONAL, oldOptional, optional));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TypeModel20Package.PARAMETER__DATA_TYPE:
				return getDataType();
			case TypeModel20Package.PARAMETER__OPTIONAL:
				return isOptional();
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
			case TypeModel20Package.PARAMETER__DATA_TYPE:
				setDataType((String)newValue);
				return;
			case TypeModel20Package.PARAMETER__OPTIONAL:
				setOptional((Boolean)newValue);
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
			case TypeModel20Package.PARAMETER__DATA_TYPE:
				setDataType(DATA_TYPE_EDEFAULT);
				return;
			case TypeModel20Package.PARAMETER__OPTIONAL:
				setOptional(OPTIONAL_EDEFAULT);
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
			case TypeModel20Package.PARAMETER__DATA_TYPE:
				return DATA_TYPE_EDEFAULT == null ? dataType != null : !DATA_TYPE_EDEFAULT.equals(dataType);
			case TypeModel20Package.PARAMETER__OPTIONAL:
				return optional != OPTIONAL_EDEFAULT;
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
		result.append(" (dataType: "); //$NON-NLS-1$
		result.append(dataType);
		result.append(", optional: "); //$NON-NLS-1$
		result.append(optional);
		result.append(')');
		return result.toString();
	}

} // ParameterImpl

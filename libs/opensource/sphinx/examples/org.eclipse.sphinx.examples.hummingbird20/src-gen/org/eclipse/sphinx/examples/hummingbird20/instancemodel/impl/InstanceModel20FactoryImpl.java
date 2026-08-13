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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.CustomApplication;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Formula;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!-- end-user-doc -->
 *
 * @generated
 */
public class InstanceModel20FactoryImpl extends EFactoryImpl implements InstanceModel20Factory {
	/**
	 * Creates the default factory implementation. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	public static InstanceModel20Factory init() {
		try {
			InstanceModel20Factory theInstanceModel20Factory = (InstanceModel20Factory) EPackage.Registry.INSTANCE
					.getEFactory(InstanceModel20Package.eNS_URI);
			if (theInstanceModel20Factory != null) {
				return theInstanceModel20Factory;
			}
		} catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new InstanceModel20FactoryImpl();
	}

	/**
	 * Creates an instance of the factory. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	public InstanceModel20FactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
		case InstanceModel20Package.APPLICATION:
			return createApplication();
		case InstanceModel20Package.COMPONENT:
			return createComponent();
		case InstanceModel20Package.CONNECTION:
			return createConnection();
		case InstanceModel20Package.PARAMETER_VALUE:
			return createParameterValue();
		case InstanceModel20Package.PARAMETER_EXPRESSION:
			return createParameterExpression();
		case InstanceModel20Package.FORMULA:
			return createFormula();
		case InstanceModel20Package.CUSTOM_APPLICATION:
			return createCustomApplication();
		default:
			throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public Application createApplication() {
		ApplicationImpl application = new ApplicationImpl();
		return application;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public Component createComponent() {
		ComponentImpl component = new ComponentImpl();
		return component;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public Connection createConnection() {
		ConnectionImpl connection = new ConnectionImpl();
		return connection;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public ParameterValue createParameterValue() {
		ParameterValueImpl parameterValue = new ParameterValueImpl();
		return parameterValue;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public ParameterExpression createParameterExpression() {
		ParameterExpressionImpl parameterExpression = new ParameterExpressionImpl();
		return parameterExpression;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public Formula createFormula() {
		FormulaImpl formula = new FormulaImpl();
		return formula;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public CustomApplication createCustomApplication() {
		CustomApplicationImpl customApplication = new CustomApplicationImpl();
		return customApplication;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	@Override
	public InstanceModel20Package getInstanceModel20Package() {
		return (InstanceModel20Package) getEPackage();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static InstanceModel20Package getPackage() {
		return InstanceModel20Package.eINSTANCE;
	}

} // InstanceModel20FactoryImpl

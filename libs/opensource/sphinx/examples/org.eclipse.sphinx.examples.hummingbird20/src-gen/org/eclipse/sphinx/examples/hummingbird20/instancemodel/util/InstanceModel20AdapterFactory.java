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
package org.eclipse.sphinx.examples.hummingbird20.instancemodel.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sphinx.examples.hummingbird20.common.Identifiable;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.*;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Formula;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue;

/**
 * <!-- begin-user-doc --> The <b>Adapter Factory</b> for the model. It provides an adapter <code>createXXX</code>
 * method for each class of the model. <!-- end-user-doc -->
 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package
 * @generated
 */
public class InstanceModel20AdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected static InstanceModel20Package modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public InstanceModel20AdapterFactory() {
		if (modelPackage == null) {
			modelPackage = InstanceModel20Package.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc --> This
	 * implementation returns <code>true</code> if the object is either the model's package or is an instance object of
	 * the model. <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected InstanceModel20Switch<Adapter> modelSwitch = new InstanceModel20Switch<Adapter>() {
			@Override
			public Adapter caseApplication(Application object) {
				return createApplicationAdapter();
			}
			@Override
			public Adapter caseComponent(Component object) {
				return createComponentAdapter();
			}
			@Override
			public Adapter caseConnection(Connection object) {
				return createConnectionAdapter();
			}
			@Override
			public Adapter caseParameterValue(ParameterValue object) {
				return createParameterValueAdapter();
			}
			@Override
			public Adapter caseParameterExpression(ParameterExpression object) {
				return createParameterExpressionAdapter();
			}
			@Override
			public Adapter caseFormula(Formula object) {
				return createFormulaAdapter();
			}
			@Override
			public Adapter caseCustomApplication(CustomApplication object) {
				return createCustomApplicationAdapter();
			}
			@Override
			public Adapter caseIdentifiable(Identifiable object) {
				return createIdentifiableAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}

	/**
	 * Creates a new adapter for an object of class '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application <em>Application</em>}'. <!--
	 * begin-user-doc --> This default implementation returns null so that we can easily ignore cases; it's useful to
	 * ignore a case when inheritance will catch all the cases anyway. <!-- end-user-doc -->
	 * 
	 * @return the new adapter.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application
	 * @generated
	 */
	public Adapter createApplicationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component <em>Component</em>}'. <!--
	 * begin-user-doc --> This default implementation returns null so that we can easily ignore cases; it's useful to
	 * ignore a case when inheritance will catch all the cases anyway. <!-- end-user-doc -->
	 * 
	 * @return the new adapter.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component
	 * @generated
	 */
	public Adapter createComponentAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection <em>Connection</em>}'. <!--
	 * begin-user-doc --> This default implementation returns null so that we can easily ignore cases; it's useful to
	 * ignore a case when inheritance will catch all the cases anyway. <!-- end-user-doc -->
	 * 
	 * @return the new adapter.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection
	 * @generated
	 */
	public Adapter createConnectionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue <em>Parameter Value</em>}'. <!--
	 * begin-user-doc --> This default implementation returns null so that we can easily ignore cases; it's useful to
	 * ignore a case when inheritance will catch all the cases anyway. <!-- end-user-doc -->
	 * 
	 * @return the new adapter.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue
	 * @generated
	 */
	public Adapter createParameterValueAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression <em>Parameter Expression</em>}'.
	 * <!-- begin-user-doc --> This default implementation returns null so that we can easily ignore cases; it's
	 * useful to ignore a case when inheritance will catch all the cases anyway. <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression
	 * @generated
	 */
	public Adapter createParameterExpressionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Formula <em>Formula</em>}'.
	 * <!-- begin-user-doc
	 * --> This default implementation returns null so that we can easily ignore cases; it's useful to ignore a case
	 * when inheritance will catch all the cases anyway. <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Formula
	 * @generated
	 */
	public Adapter createFormulaAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.CustomApplication <em>Custom Application</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.CustomApplication
	 * @generated
	 */
	public Adapter createCustomApplicationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.sphinx.examples.hummingbird20.common.Identifiable <em>Identifiable</em>}'.
	 * <!-- begin-user-doc
	 * --> This default implementation returns null so that we can easily ignore cases; it's useful to ignore a case
	 * when inheritance will catch all the cases anyway. <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Identifiable
	 * @generated
	 */
	public Adapter createIdentifiableAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc --> This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} // InstanceModel20AdapterFactory

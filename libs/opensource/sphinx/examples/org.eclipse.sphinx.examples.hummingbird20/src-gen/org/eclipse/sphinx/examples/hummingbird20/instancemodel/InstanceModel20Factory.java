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
package org.eclipse.sphinx.examples.hummingbird20.instancemodel;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc --> The <b>Factory</b> for the model. It provides a create method for each non-abstract class of
 * the model. <!-- end-user-doc -->
 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package
 * @generated
 */
public interface InstanceModel20Factory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	InstanceModel20Factory eINSTANCE = org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20FactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Application</em>'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return a new object of class '<em>Application</em>'.
	 * @generated
	 */
	Application createApplication();

	/**
	 * Returns a new object of class '<em>Component</em>'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return a new object of class '<em>Component</em>'.
	 * @generated
	 */
	Component createComponent();

	/**
	 * Returns a new object of class '<em>Connection</em>'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return a new object of class '<em>Connection</em>'.
	 * @generated
	 */
	Connection createConnection();

	/**
	 * Returns a new object of class '<em>Parameter Value</em>'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return a new object of class '<em>Parameter Value</em>'.
	 * @generated
	 */
	ParameterValue createParameterValue();

	/**
	 * Returns a new object of class '<em>Parameter Expression</em>'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return a new object of class '<em>Parameter Expression</em>'.
	 * @generated
	 */
	ParameterExpression createParameterExpression();

	/**
	 * Returns a new object of class '<em>Formula</em>'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return a new object of class '<em>Formula</em>'.
	 * @generated
	 */
	Formula createFormula();

	/**
	 * Returns a new object of class '<em>Custom Application</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Custom Application</em>'.
	 * @generated
	 */
	CustomApplication createCustomApplication();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	InstanceModel20Package getInstanceModel20Package();

} // InstanceModel20Factory

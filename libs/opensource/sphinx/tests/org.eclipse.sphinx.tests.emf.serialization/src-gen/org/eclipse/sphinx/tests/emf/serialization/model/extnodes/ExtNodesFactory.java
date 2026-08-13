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
package org.eclipse.sphinx.tests.emf.serialization.model.extnodes;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc --> The <b>Factory</b> for the model. It provides a create method for each non-abstract class of
 * the model. <!-- end-user-doc -->
 * 
 * @see org.eclipse.sphinx.tests.emf.serialization.model.extnodes.ExtNodesPackage
 * @generated
 */
public interface ExtNodesFactory extends EFactory {
	/**
	 * The singleton instance of the factory. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	ExtNodesFactory eINSTANCE = org.eclipse.sphinx.tests.emf.serialization.model.extnodes.impl.ExtNodesFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Extended Node</em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return a new object of class '<em>Extended Node</em>'.
	 * @generated
	 */
	ExtendedNode createExtendedNode();

	/**
	 * Returns a new object of class '<em>Extension</em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return a new object of class '<em>Extension</em>'.
	 * @generated
	 */
	Extension createExtension();

	/**
	 * Returns the package supported by this factory. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the package supported by this factory.
	 * @generated
	 */
	ExtNodesPackage getExtNodesPackage();

} // ExtNodesFactory

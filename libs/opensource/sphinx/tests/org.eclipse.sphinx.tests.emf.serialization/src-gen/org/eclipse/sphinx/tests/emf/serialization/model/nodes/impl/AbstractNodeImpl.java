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
package org.eclipse.sphinx.tests.emf.serialization.model.nodes.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.sphinx.tests.emf.serialization.model.nodes.AbstractNode;
import org.eclipse.sphinx.tests.emf.serialization.model.nodes.NodesPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Abstract Node</b></em>'. <!-- end-user-doc -->
 * <p>
 * </p>
 *
 * @generated
 */
public abstract class AbstractNodeImpl extends EObjectImpl implements AbstractNode {
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected AbstractNodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return NodesPackage.Literals.ABSTRACT_NODE;
	}

} // AbstractNodeImpl

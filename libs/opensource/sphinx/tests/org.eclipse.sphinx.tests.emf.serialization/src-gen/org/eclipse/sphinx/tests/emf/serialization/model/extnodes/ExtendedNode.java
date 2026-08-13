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

import org.eclipse.sphinx.tests.emf.serialization.model.nodes.SubNode;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Extended Node</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.model.extnodes.ExtendedNode#getAltenativeName <em>Altenative Name
 * </em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.tests.emf.serialization.model.extnodes.ExtNodesPackage#getExtendedNode()
 * @model
 * @generated
 */
public interface ExtendedNode extends SubNode {
	/**
	 * Returns the value of the '<em><b>Altenative Name</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Altenative Name</em>' attribute isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Altenative Name</em>' attribute.
	 * @see #setAltenativeName(String)
	 * @see org.eclipse.sphinx.tests.emf.serialization.model.extnodes.ExtNodesPackage#getExtendedNode_AltenativeName()
	 * @model
	 * @generated
	 */
	String getAltenativeName();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.tests.emf.serialization.model.extnodes.ExtendedNode#getAltenativeName
	 * <em>Altenative Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Altenative Name</em>' attribute.
	 * @see #getAltenativeName()
	 * @generated
	 */
	void setAltenativeName(String value);

} // ExtendedNode

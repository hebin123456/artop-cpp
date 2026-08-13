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
package org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>TARGET Type</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.TARGETType#getSPECOBJECTREF <em>SPECOBJECTREF</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getTARGETType()
 * @model extendedMetaData="name='TARGET_._type' kind='elementOnly'"
 * @generated
 */
public interface TARGETType extends EObject {
	/**
	 * Returns the value of the '<em><b>SPECOBJECTREF</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>SPECOBJECTREF</em>' attribute isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>SPECOBJECTREF</em>' attribute.
	 * @see #setSPECOBJECTREF(String)
	 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getTARGETType_SPECOBJECTREF()
	 * @model dataType="org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.GLOBALREF"
	 *        extendedMetaData="kind='element' name='SPEC-OBJECT-REF' namespace='##targetNamespace'"
	 * @generated
	 */
	String getSPECOBJECTREF();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.TARGETType#getSPECOBJECTREF
	 * <em>SPECOBJECTREF</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>SPECOBJECTREF</em>' attribute.
	 * @see #getSPECOBJECTREF()
	 * @generated
	 */
	void setSPECOBJECTREF(String value);

} // TARGETType

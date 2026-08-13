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

import java.math.BigInteger;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>EMBEDDEDVALUE</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.EMBEDDEDVALUE#getKEY <em>KEY</em>}</li>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.EMBEDDEDVALUE#getOTHERCONTENT <em>OTHERCONTENT</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getEMBEDDEDVALUE()
 * @model extendedMetaData="name='EMBEDDED-VALUE' kind='empty'"
 * @generated
 */
public interface EMBEDDEDVALUE extends EObject {
	/**
	 * Returns the value of the '<em><b>KEY</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>KEY</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>KEY</em>' attribute.
	 * @see #setKEY(BigInteger)
	 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getEMBEDDEDVALUE_KEY()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Integer" required="true"
	 *        extendedMetaData="kind='attribute' name='KEY'"
	 * @generated
	 */
	BigInteger getKEY();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.EMBEDDEDVALUE#getKEY
	 * <em>KEY</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>KEY</em>' attribute.
	 * @see #getKEY()
	 * @generated
	 */
	void setKEY(BigInteger value);

	/**
	 * Returns the value of the '<em><b>OTHERCONTENT</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>OTHERCONTENT</em>' attribute isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>OTHERCONTENT</em>' attribute.
	 * @see #setOTHERCONTENT(String)
	 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getEMBEDDEDVALUE_OTHERCONTENT()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
	 *        extendedMetaData="kind='attribute' name='OTHER-CONTENT'"
	 * @generated
	 */
	String getOTHERCONTENT();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.EMBEDDEDVALUE#getOTHERCONTENT
	 * <em>OTHERCONTENT</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>OTHERCONTENT</em>' attribute.
	 * @see #getOTHERCONTENT()
	 * @generated
	 */
	void setOTHERCONTENT(String value);

} // EMBEDDEDVALUE

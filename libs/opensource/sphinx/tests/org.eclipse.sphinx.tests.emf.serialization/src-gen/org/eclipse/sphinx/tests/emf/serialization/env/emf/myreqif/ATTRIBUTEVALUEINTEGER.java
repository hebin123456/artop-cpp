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
 * <!-- begin-user-doc --> A representation of the model object '<em><b>ATTRIBUTEVALUEINTEGER</b></em>'. <!--
 * end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.ATTRIBUTEVALUEINTEGER#getDEFINITION <em>DEFINITION
 * </em>}</li>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.ATTRIBUTEVALUEINTEGER#getTHEVALUE <em>THEVALUE</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getATTRIBUTEVALUEINTEGER()
 * @model extendedMetaData="name='ATTRIBUTE-VALUE-INTEGER' kind='elementOnly'"
 * @generated
 */
public interface ATTRIBUTEVALUEINTEGER extends EObject {
	/**
	 * Returns the value of the '<em><b>DEFINITION</b></em>' containment reference. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>DEFINITION</em>' containment reference isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>DEFINITION</em>' containment reference.
	 * @see #setDEFINITION(DEFINITIONType5)
	 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getATTRIBUTEVALUEINTEGER_DEFINITION()
	 * @model containment="true" required="true"
	 *        extendedMetaData="kind='element' name='DEFINITION' namespace='##targetNamespace'"
	 * @generated
	 */
	DEFINITIONType5 getDEFINITION();

	/**
	 * Sets the value of the '
	 * {@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.ATTRIBUTEVALUEINTEGER#getDEFINITION
	 * <em>DEFINITION</em>}' containment reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>DEFINITION</em>' containment reference.
	 * @see #getDEFINITION()
	 * @generated
	 */
	void setDEFINITION(DEFINITIONType5 value);

	/**
	 * Returns the value of the '<em><b>THEVALUE</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>THEVALUE</em>' attribute isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>THEVALUE</em>' attribute.
	 * @see #setTHEVALUE(BigInteger)
	 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getATTRIBUTEVALUEINTEGER_THEVALUE()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Integer" required="true"
	 *        extendedMetaData="kind='attribute' name='THE-VALUE'"
	 * @generated
	 */
	BigInteger getTHEVALUE();

	/**
	 * Sets the value of the '
	 * {@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.ATTRIBUTEVALUEINTEGER#getTHEVALUE <em>THEVALUE</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>THEVALUE</em>' attribute.
	 * @see #getTHEVALUE()
	 * @generated
	 */
	void setTHEVALUE(BigInteger value);

} // ATTRIBUTEVALUEINTEGER

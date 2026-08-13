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
 * <!-- begin-user-doc --> A representation of the model object '<em><b>DEFINITION Type1</b></em>'. <!-- end-user-doc
 * -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.DEFINITIONType1#getATTRIBUTEDEFINITIONDATEREF <em>
 * ATTRIBUTEDEFINITIONDATEREF</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getDEFINITIONType1()
 * @model extendedMetaData="name='DEFINITION_._1_._type' kind='elementOnly'"
 * @generated
 */
public interface DEFINITIONType1 extends EObject {
	/**
	 * Returns the value of the '<em><b>ATTRIBUTEDEFINITIONDATEREF</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>ATTRIBUTEDEFINITIONDATEREF</em>' attribute isn't clear, there really should be more of
	 * a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>ATTRIBUTEDEFINITIONDATEREF</em>' attribute.
	 * @see #setATTRIBUTEDEFINITIONDATEREF(String)
	 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getDEFINITIONType1_ATTRIBUTEDEFINITIONDATEREF()
	 * @model dataType="org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.LOCALREF"
	 *        extendedMetaData="kind='element' name='ATTRIBUTE-DEFINITION-DATE-REF' namespace='##targetNamespace'"
	 * @generated
	 */
	String getATTRIBUTEDEFINITIONDATEREF();

	/**
	 * Sets the value of the '
	 * {@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.DEFINITIONType1#getATTRIBUTEDEFINITIONDATEREF
	 * <em>ATTRIBUTEDEFINITIONDATEREF</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>ATTRIBUTEDEFINITIONDATEREF</em>' attribute.
	 * @see #getATTRIBUTEDEFINITIONDATEREF()
	 * @generated
	 */
	void setATTRIBUTEDEFINITIONDATEREF(String value);

} // DEFINITIONType1

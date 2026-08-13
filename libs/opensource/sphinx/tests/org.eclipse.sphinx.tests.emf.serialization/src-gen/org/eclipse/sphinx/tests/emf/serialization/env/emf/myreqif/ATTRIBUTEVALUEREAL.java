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
 * <!-- begin-user-doc --> A representation of the model object '<em><b>ATTRIBUTEVALUEREAL</b></em>'. <!-- end-user-doc
 * -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.ATTRIBUTEVALUEREAL#getDEFINITION <em>DEFINITION</em>}</li>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.ATTRIBUTEVALUEREAL#getTHEVALUE <em>THEVALUE</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getATTRIBUTEVALUEREAL()
 * @model extendedMetaData="name='ATTRIBUTE-VALUE-REAL' kind='elementOnly'"
 * @generated
 */
public interface ATTRIBUTEVALUEREAL extends EObject {
	/**
	 * Returns the value of the '<em><b>DEFINITION</b></em>' containment reference. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>DEFINITION</em>' containment reference isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>DEFINITION</em>' containment reference.
	 * @see #setDEFINITION(DEFINITIONType4)
	 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getATTRIBUTEVALUEREAL_DEFINITION()
	 * @model containment="true" required="true"
	 *        extendedMetaData="kind='element' name='DEFINITION' namespace='##targetNamespace'"
	 * @generated
	 */
	DEFINITIONType4 getDEFINITION();

	/**
	 * Sets the value of the '
	 * {@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.ATTRIBUTEVALUEREAL#getDEFINITION <em>DEFINITION</em>}'
	 * containment reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>DEFINITION</em>' containment reference.
	 * @see #getDEFINITION()
	 * @generated
	 */
	void setDEFINITION(DEFINITIONType4 value);

	/**
	 * Returns the value of the '<em><b>THEVALUE</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>THEVALUE</em>' attribute isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>THEVALUE</em>' attribute.
	 * @see #isSetTHEVALUE()
	 * @see #unsetTHEVALUE()
	 * @see #setTHEVALUE(double)
	 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getATTRIBUTEVALUEREAL_THEVALUE()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Double" required="true"
	 *        extendedMetaData="kind='attribute' name='THE-VALUE'"
	 * @generated
	 */
	double getTHEVALUE();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.ATTRIBUTEVALUEREAL#getTHEVALUE
	 * <em>THEVALUE</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>THEVALUE</em>' attribute.
	 * @see #isSetTHEVALUE()
	 * @see #unsetTHEVALUE()
	 * @see #getTHEVALUE()
	 * @generated
	 */
	void setTHEVALUE(double value);

	/**
	 * Unsets the value of the '
	 * {@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.ATTRIBUTEVALUEREAL#getTHEVALUE <em>THEVALUE</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #isSetTHEVALUE()
	 * @see #getTHEVALUE()
	 * @see #setTHEVALUE(double)
	 * @generated
	 */
	void unsetTHEVALUE();

	/**
	 * Returns whether the value of the '
	 * {@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.ATTRIBUTEVALUEREAL#getTHEVALUE <em>THEVALUE</em>}'
	 * attribute is set. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return whether the value of the '<em>THEVALUE</em>' attribute is set.
	 * @see #unsetTHEVALUE()
	 * @see #getTHEVALUE()
	 * @see #setTHEVALUE(double)
	 * @generated
	 */
	boolean isSetTHEVALUE();

} // ATTRIBUTEVALUEREAL

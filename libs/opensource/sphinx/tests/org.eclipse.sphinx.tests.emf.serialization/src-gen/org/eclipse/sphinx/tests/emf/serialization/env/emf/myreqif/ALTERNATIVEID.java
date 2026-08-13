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
 * <!-- begin-user-doc --> A representation of the model object '<em><b>ALTERNATIVEID</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.ALTERNATIVEID#getIDENTIFIER <em>IDENTIFIER</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getALTERNATIVEID()
 * @model extendedMetaData="name='ALTERNATIVE-ID' kind='empty'"
 * @generated
 */
public interface ALTERNATIVEID extends EObject {
	/**
	 * Returns the value of the '<em><b>IDENTIFIER</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>IDENTIFIER</em>' attribute isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>IDENTIFIER</em>' attribute.
	 * @see #setIDENTIFIER(String)
	 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getALTERNATIVEID_IDENTIFIER()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
	 *        extendedMetaData="kind='attribute' name='IDENTIFIER'"
	 * @generated
	 */
	String getIDENTIFIER();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.ALTERNATIVEID#getIDENTIFIER
	 * <em>IDENTIFIER</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>IDENTIFIER</em>' attribute.
	 * @see #getIDENTIFIER()
	 * @generated
	 */
	void setIDENTIFIER(String value);

} // ALTERNATIVEID

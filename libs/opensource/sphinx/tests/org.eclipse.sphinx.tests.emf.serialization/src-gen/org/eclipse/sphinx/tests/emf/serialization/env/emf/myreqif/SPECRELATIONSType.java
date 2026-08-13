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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>SPECRELATIONS Type</b></em>'. <!-- end-user-doc
 * -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.SPECRELATIONSType#getGroup <em>Group</em>}</li>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.SPECRELATIONSType#getSPECRELATIONREF <em>
 * SPECRELATIONREF</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getSPECRELATIONSType()
 * @model extendedMetaData="name='SPEC-RELATIONS_._type' kind='elementOnly'"
 * @generated
 */
public interface SPECRELATIONSType extends EObject {
	/**
	 * Returns the value of the '<em><b>Group</b></em>' attribute list. The list contents are of type
	 * {@link org.eclipse.emf.ecore.util.FeatureMap.Entry}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Group</em>' attribute list isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Group</em>' attribute list.
	 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getSPECRELATIONSType_Group()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.EFeatureMapEntry" many="true"
	 *        extendedMetaData="kind='group' name='group:0'"
	 * @generated
	 */
	FeatureMap getGroup();

	/**
	 * Returns the value of the '<em><b>SPECRELATIONREF</b></em>' attribute list. The list contents are of type
	 * {@link java.lang.String}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>SPECRELATIONREF</em>' attribute list isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>SPECRELATIONREF</em>' attribute list.
	 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getSPECRELATIONSType_SPECRELATIONREF()
	 * @model unique="false" dataType="org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.LOCALREF" transient="true"
	 *        volatile="true" derived="true"
	 *        extendedMetaData="kind='element' name='SPEC-RELATION-REF' namespace='##targetNamespace' group='#group:0'"
	 * @generated
	 */
	EList<String> getSPECRELATIONREF();

} // SPECRELATIONSType

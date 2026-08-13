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
 * <!-- begin-user-doc --> A representation of the model object '<em><b>SPECOBJECTS Type</b></em>'. <!-- end-user-doc
 * -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.SPECOBJECTSType#getGroup <em>Group</em>}</li>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.SPECOBJECTSType#getSPECOBJECT <em>SPECOBJECT</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getSPECOBJECTSType()
 * @model extendedMetaData="name='SPEC-OBJECTS_._type' kind='elementOnly'"
 * @generated
 */
public interface SPECOBJECTSType extends EObject {
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
	 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getSPECOBJECTSType_Group()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.EFeatureMapEntry" many="true"
	 *        extendedMetaData="kind='group' name='group:0'"
	 * @generated
	 */
	FeatureMap getGroup();

	/**
	 * Returns the value of the '<em><b>SPECOBJECT</b></em>' containment reference list. The list contents are of type
	 * {@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.SPECOBJECT}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>SPECOBJECT</em>' containment reference list isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>SPECOBJECT</em>' containment reference list.
	 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getSPECOBJECTSType_SPECOBJECT()
	 * @model containment="true" transient="true" volatile="true" derived="true"
	 *        extendedMetaData="kind='element' name='SPEC-OBJECT' namespace='##targetNamespace' group='#group:0'"
	 * @generated
	 */
	EList<SPECOBJECT> getSPECOBJECT();

} // SPECOBJECTSType

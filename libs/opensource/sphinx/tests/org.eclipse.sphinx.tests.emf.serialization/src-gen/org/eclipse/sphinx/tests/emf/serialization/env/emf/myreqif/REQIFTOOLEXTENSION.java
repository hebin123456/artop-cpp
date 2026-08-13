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
import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>REQIFTOOLEXTENSION</b></em>'. <!-- end-user-doc
 * -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.REQIFTOOLEXTENSION#getAny <em>Any</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getREQIFTOOLEXTENSION()
 * @model extendedMetaData="name='REQ-IF-TOOL-EXTENSION' kind='elementOnly'"
 * @generated
 */
public interface REQIFTOOLEXTENSION extends EObject {
	/**
	 * Returns the value of the '<em><b>Any</b></em>' attribute list. The list contents are of type
	 * {@link org.eclipse.emf.ecore.util.FeatureMap.Entry}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Any</em>' attribute list isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Any</em>' attribute list.
	 * @see org.eclipse.sphinx.tests.emf.serialization.env.emf.myreqif.MyreqifPackage#getREQIFTOOLEXTENSION_Any()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.EFeatureMapEntry" many="true"
	 *        extendedMetaData="kind='elementWildcard' wildcards='##other' name=':0' processing='lax'"
	 * @generated
	 */
	FeatureMap getAny();

} // REQIFTOOLEXTENSION

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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Extension</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.sphinx.tests.emf.serialization.model.extnodes.Extension#getExtensions <em>Extensions</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.tests.emf.serialization.model.extnodes.ExtNodesPackage#getExtension()
 * @model
 * @generated
 */
public interface Extension extends EObject {
	/**
	 * Returns the value of the '<em><b>Extensions</b></em>' containment reference list. The list contents are of type
	 * {@link org.eclipse.sphinx.tests.emf.serialization.model.extnodes.ExtendedNode}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Extensions</em>' containment reference list isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Extensions</em>' containment reference list.
	 * @see org.eclipse.sphinx.tests.emf.serialization.model.extnodes.ExtNodesPackage#getExtension_Extensions()
	 * @model containment="true" extendedMetaData="namespace='##targetNamespace'"
	 * @generated
	 */
	EList<ExtendedNode> getExtensions();

} // Extension

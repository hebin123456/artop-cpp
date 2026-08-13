/**
 * <copyright>
 * 
 * Copyright (c) 2008-2014 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - Enhancements and maintenance
 * 
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.common;

import org.eclipse.emf.common.util.URI;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Translation</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.common.Translation#getLanguage <em>Language</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.common.Translation#getResourceURI <em>Resource URI</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sphinx.examples.hummingbird20.common.Common20Package#getTranslation()
 * @model
 * @generated
 */
public interface Translation extends EObject {
	/**
	 * Returns the value of the '<em><b>Language</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.sphinx.examples.hummingbird20.common.LanguageCultureName}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Language</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Language</em>' attribute.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.LanguageCultureName
	 * @see #setLanguage(LanguageCultureName)
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Common20Package#getTranslation_Language()
	 * @model
	 * @generated
	 */
	LanguageCultureName getLanguage();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.common.Translation#getLanguage <em>Language</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Language</em>' attribute.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.LanguageCultureName
	 * @see #getLanguage()
	 * @generated
	 */
	void setLanguage(LanguageCultureName value);

	/**
	 * Returns the value of the '<em><b>Resource URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Resource URI</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Resource URI</em>' attribute.
	 * @see #setResourceURI(URI)
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Common20Package#getTranslation_ResourceURI()
	 * @model dataType="org.eclipse.sphinx.examples.hummingbird20.common.EURI"
	 * @generated
	 */
	URI getResourceURI();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.common.Translation#getResourceURI <em>Resource URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Resource URI</em>' attribute.
	 * @see #getResourceURI()
	 * @generated
	 */
	void setResourceURI(URI value);

} // Translation

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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Description</b></em>'. <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents a Description model object
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.common.Description#getMixed <em>Mixed</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.common.Description#getLanguage <em>Language</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.common.Description#getTranslations <em>Translations</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sphinx.examples.hummingbird20.common.Common20Package#getDescription()
 * @model extendedMetaData="kind='mixed'"
 * @generated
 */
public interface Description extends EObject {
	/**
	 * Returns the value of the '<em><b>Mixed</b></em>' attribute list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.util.FeatureMap.Entry}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mixed</em>' attribute list isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mixed</em>' attribute list.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Common20Package#getDescription_Mixed()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.EFeatureMapEntry" many="true"
	 *        extendedMetaData="name=':mixed' kind='elementWildcard'"
	 * @generated
	 */
	FeatureMap getMixed();

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
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Common20Package#getDescription_Language()
	 * @model
	 * @generated
	 */
	LanguageCultureName getLanguage();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.examples.hummingbird20.common.Description#getLanguage <em>Language</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Language</em>' attribute.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.LanguageCultureName
	 * @see #getLanguage()
	 * @generated
	 */
	void setLanguage(LanguageCultureName value);

	/**
	 * Returns the value of the '<em><b>Translations</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.sphinx.examples.hummingbird20.common.Translation}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Translations</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Translations</em>' containment reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Common20Package#getDescription_Translations()
	 * @model containment="true"
	 * @generated
	 */
	EList<Translation> getTranslations();

} // Description

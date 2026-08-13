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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc --> The <b>Package</b> for the model. It contains accessors for the meta objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.sphinx.examples.hummingbird20.common.Common20Factory
 * @model kind="package"
 * @generated
 */
public interface Common20Package extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "common"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/sphinx/examples/hummingbird/2.0.1/common"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "cn"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	Common20Package eINSTANCE = org.eclipse.sphinx.examples.hummingbird20.common.impl.Common20PackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.common.impl.IdentifiableImpl <em>Identifiable</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.IdentifiableImpl
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.Common20PackageImpl#getIdentifiable()
	 * @generated
	 */
	int IDENTIFIABLE = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIABLE__NAME = 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIABLE__DESCRIPTION = 1;

	/**
	 * The number of structural features of the '<em>Identifiable</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int IDENTIFIABLE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.common.impl.DescriptionImpl <em>Description</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.DescriptionImpl
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.Common20PackageImpl#getDescription()
	 * @generated
	 */
	int DESCRIPTION = 1;

	/**
	 * The feature id for the '<em><b>Mixed</b></em>' attribute list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DESCRIPTION__MIXED = 0;

	/**
	 * The feature id for the '<em><b>Language</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DESCRIPTION__LANGUAGE = 1;

	/**
	 * The feature id for the '<em><b>Translations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DESCRIPTION__TRANSLATIONS = 2;

	/**
	 * The number of structural features of the '<em>Description</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int DESCRIPTION_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.common.impl.TranslationImpl <em>Translation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.TranslationImpl
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.Common20PackageImpl#getTranslation()
	 * @generated
	 */
	int TRANSLATION = 2;

	/**
	 * The feature id for the '<em><b>Language</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSLATION__LANGUAGE = 0;

	/**
	 * The feature id for the '<em><b>Resource URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSLATION__RESOURCE_URI = 1;

	/**
	 * The number of structural features of the '<em>Translation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSLATION_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.common.LanguageCultureName <em>Language Culture Name</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.LanguageCultureName
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.Common20PackageImpl#getLanguageCultureName()
	 * @generated
	 */
	int LANGUAGE_CULTURE_NAME = 3;

	/**
	 * The meta object id for the '<em>EURI</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.common.util.URI
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.Common20PackageImpl#getEURI()
	 * @generated
	 */
	int EURI = 4;

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird20.common.Identifiable <em>Identifiable</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Identifiable</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Identifiable
	 * @generated
	 */
	EClass getIdentifiable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.examples.hummingbird20.common.Identifiable#getName <em>Name</em>}'.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Identifiable#getName()
	 * @see #getIdentifiable()
	 * @generated
	 */
	EAttribute getIdentifiable_Name();

	/**
	 * Returns the meta object for the containment reference '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.common.Identifiable#getDescription <em>Description</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the containment reference '<em>Description</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Identifiable#getDescription()
	 * @see #getIdentifiable()
	 * @generated
	 */
	EReference getIdentifiable_Description();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird20.common.Description <em>Description</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Description</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Description
	 * @generated
	 */
	EClass getDescription();

	/**
	 * Returns the meta object for the attribute list '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.common.Description#getMixed <em>Mixed</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute list '<em>Mixed</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Description#getMixed()
	 * @see #getDescription()
	 * @generated
	 */
	EAttribute getDescription_Mixed();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.examples.hummingbird20.common.Description#getLanguage <em>Language</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Language</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Description#getLanguage()
	 * @see #getDescription()
	 * @generated
	 */
	EAttribute getDescription_Language();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.sphinx.examples.hummingbird20.common.Description#getTranslations <em>Translations</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Translations</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Description#getTranslations()
	 * @see #getDescription()
	 * @generated
	 */
	EReference getDescription_Translations();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird20.common.Translation <em>Translation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Translation</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Translation
	 * @generated
	 */
	EClass getTranslation();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.examples.hummingbird20.common.Translation#getLanguage <em>Language</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Language</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Translation#getLanguage()
	 * @see #getTranslation()
	 * @generated
	 */
	EAttribute getTranslation_Language();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.examples.hummingbird20.common.Translation#getResourceURI <em>Resource URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Resource URI</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Translation#getResourceURI()
	 * @see #getTranslation()
	 * @generated
	 */
	EAttribute getTranslation_ResourceURI();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.sphinx.examples.hummingbird20.common.LanguageCultureName <em>Language Culture Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Language Culture Name</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.LanguageCultureName
	 * @generated
	 */
	EEnum getLanguageCultureName();

	/**
	 * Returns the meta object for data type '{@link org.eclipse.emf.common.util.URI <em>EURI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>EURI</em>'.
	 * @see org.eclipse.emf.common.util.URI
	 * @model instanceClass="org.eclipse.emf.common.util.URI"
	 * @generated
	 */
	EDataType getEURI();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	Common20Factory getCommon20Factory();

	/**
	 * <!-- begin-user-doc --> Defines literals for the meta objects that represent
	 * <ul>
	 * <li>each class,</li>
	 * <li>each feature of each class,</li>
	 * <li>each enum,</li>
	 * <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird20.common.impl.IdentifiableImpl <em>Identifiable</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.IdentifiableImpl
		 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.Common20PackageImpl#getIdentifiable()
		 * @generated
		 */
		EClass IDENTIFIABLE = eINSTANCE.getIdentifiable();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute IDENTIFIABLE__NAME = eINSTANCE.getIdentifiable_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' containment reference feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference IDENTIFIABLE__DESCRIPTION = eINSTANCE.getIdentifiable_Description();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird20.common.impl.DescriptionImpl <em>Description</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.DescriptionImpl
		 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.Common20PackageImpl#getDescription()
		 * @generated
		 */
		EClass DESCRIPTION = eINSTANCE.getDescription();

		/**
		 * The meta object literal for the '<em><b>Mixed</b></em>' attribute list feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute DESCRIPTION__MIXED = eINSTANCE.getDescription_Mixed();

		/**
		 * The meta object literal for the '<em><b>Language</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DESCRIPTION__LANGUAGE = eINSTANCE.getDescription_Language();

		/**
		 * The meta object literal for the '<em><b>Translations</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DESCRIPTION__TRANSLATIONS = eINSTANCE.getDescription_Translations();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird20.common.impl.TranslationImpl <em>Translation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.TranslationImpl
		 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.Common20PackageImpl#getTranslation()
		 * @generated
		 */
		EClass TRANSLATION = eINSTANCE.getTranslation();

		/**
		 * The meta object literal for the '<em><b>Language</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TRANSLATION__LANGUAGE = eINSTANCE.getTranslation_Language();

		/**
		 * The meta object literal for the '<em><b>Resource URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TRANSLATION__RESOURCE_URI = eINSTANCE.getTranslation_ResourceURI();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird20.common.LanguageCultureName <em>Language Culture Name</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.examples.hummingbird20.common.LanguageCultureName
		 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.Common20PackageImpl#getLanguageCultureName()
		 * @generated
		 */
		EEnum LANGUAGE_CULTURE_NAME = eINSTANCE.getLanguageCultureName();

		/**
		 * The meta object literal for the '<em>EURI</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.emf.common.util.URI
		 * @see org.eclipse.sphinx.examples.hummingbird20.common.impl.Common20PackageImpl#getEURI()
		 * @generated
		 */
		EDataType EURI = eINSTANCE.getEURI();

	}

} // Common20Package

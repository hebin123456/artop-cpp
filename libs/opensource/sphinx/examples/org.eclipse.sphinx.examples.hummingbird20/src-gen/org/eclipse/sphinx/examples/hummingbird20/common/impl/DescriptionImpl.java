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
package org.eclipse.sphinx.examples.hummingbird20.common.impl;

import java.util.Collection;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sphinx.emf.ecore.ExtendedEObjectImpl;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20EObjectImpl;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Package;
import org.eclipse.sphinx.examples.hummingbird20.common.Description;
import org.eclipse.sphinx.examples.hummingbird20.common.LanguageCultureName;
import org.eclipse.sphinx.examples.hummingbird20.common.Translation;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Description</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.common.impl.DescriptionImpl#getMixed <em>Mixed</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.common.impl.DescriptionImpl#getLanguage <em>Language</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.common.impl.DescriptionImpl#getTranslations <em>Translations</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DescriptionImpl extends Hummingbird20EObjectImpl implements Description {
	/**
	 * The cached value of the '{@link #getMixed() <em>Mixed</em>}' attribute list.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getMixed()
	 * @generated
	 * @ordered
	 */
	protected FeatureMap mixed;

	/**
	 * The default value of the '{@link #getLanguage() <em>Language</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLanguage()
	 * @generated
	 * @ordered
	 */
	protected static final LanguageCultureName LANGUAGE_EDEFAULT = LanguageCultureName.EN_US;
	/**
	 * The cached value of the '{@link #getLanguage() <em>Language</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLanguage()
	 * @generated
	 * @ordered
	 */
	protected LanguageCultureName language = LANGUAGE_EDEFAULT;
	/**
	 * The cached value of the '{@link #getTranslations() <em>Translations</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTranslations()
	 * @generated
	 * @ordered
	 */
	protected EList<Translation> translations;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected DescriptionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return Common20Package.Literals.DESCRIPTION;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FeatureMap getMixed() {
		if (mixed == null) {
			mixed = new BasicFeatureMap(this, Common20Package.DESCRIPTION__MIXED);
		}
		return mixed;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LanguageCultureName getLanguage() {
		return language;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLanguage(LanguageCultureName newLanguage) {
		LanguageCultureName oldLanguage = language;
		language = newLanguage == null ? LANGUAGE_EDEFAULT : newLanguage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, Common20Package.DESCRIPTION__LANGUAGE, oldLanguage, language));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Translation> getTranslations() {
		if (translations == null) {
			translations = new EObjectContainmentEList<Translation>(Translation.class, this, Common20Package.DESCRIPTION__TRANSLATIONS);
		}
		return translations;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case Common20Package.DESCRIPTION__MIXED:
				return ((InternalEList<?>)getMixed()).basicRemove(otherEnd, msgs);
			case Common20Package.DESCRIPTION__TRANSLATIONS:
				return ((InternalEList<?>)getTranslations()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case Common20Package.DESCRIPTION__MIXED:
				if (coreType) return getMixed();
				return ((FeatureMap.Internal)getMixed()).getWrapper();
			case Common20Package.DESCRIPTION__LANGUAGE:
				return getLanguage();
			case Common20Package.DESCRIPTION__TRANSLATIONS:
				return getTranslations();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case Common20Package.DESCRIPTION__MIXED:
				((FeatureMap.Internal)getMixed()).set(newValue);
				return;
			case Common20Package.DESCRIPTION__LANGUAGE:
				setLanguage((LanguageCultureName)newValue);
				return;
			case Common20Package.DESCRIPTION__TRANSLATIONS:
				getTranslations().clear();
				getTranslations().addAll((Collection<? extends Translation>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case Common20Package.DESCRIPTION__MIXED:
				getMixed().clear();
				return;
			case Common20Package.DESCRIPTION__LANGUAGE:
				setLanguage(LANGUAGE_EDEFAULT);
				return;
			case Common20Package.DESCRIPTION__TRANSLATIONS:
				getTranslations().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case Common20Package.DESCRIPTION__MIXED:
				return mixed != null && !mixed.isEmpty();
			case Common20Package.DESCRIPTION__LANGUAGE:
				return language != LANGUAGE_EDEFAULT;
			case Common20Package.DESCRIPTION__TRANSLATIONS:
				return translations != null && !translations.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (mixed: "); //$NON-NLS-1$
		result.append(mixed);
		result.append(", language: "); //$NON-NLS-1$
		result.append(language);
		result.append(')');
		return result.toString();
	}

} // DescriptionImpl

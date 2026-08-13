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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Language Culture Name</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.eclipse.sphinx.examples.hummingbird20.common.Common20Package#getLanguageCultureName()
 * @model
 * @generated
 */
public enum LanguageCultureName implements Enumerator {
	/**
	 * The '<em><b>En US</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #EN_US_VALUE
	 * @generated
	 * @ordered
	 */
	EN_US(0, "enUS", "en-US"), //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>De DE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #DE_DE_VALUE
	 * @generated
	 * @ordered
	 */
	DE_DE(1, "deDE", "de-DE"), //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>Fr FR</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #FR_FR_VALUE
	 * @generated
	 * @ordered
	 */
	FR_FR(2, "frFR", "fr-FR"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>En US</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>En US</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #EN_US
	 * @model name="enUS" literal="en-US"
	 * @generated
	 * @ordered
	 */
	public static final int EN_US_VALUE = 0;

	/**
	 * The '<em><b>De DE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>De DE</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #DE_DE
	 * @model name="deDE" literal="de-DE"
	 * @generated
	 * @ordered
	 */
	public static final int DE_DE_VALUE = 1;

	/**
	 * The '<em><b>Fr FR</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Fr FR</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #FR_FR
	 * @model name="frFR" literal="fr-FR"
	 * @generated
	 * @ordered
	 */
	public static final int FR_FR_VALUE = 2;

	/**
	 * An array of all the '<em><b>Language Culture Name</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final LanguageCultureName[] VALUES_ARRAY =
		new LanguageCultureName[] {
			EN_US,
			DE_DE,
			FR_FR,
		};

	/**
	 * A public read-only list of all the '<em><b>Language Culture Name</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<LanguageCultureName> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Language Culture Name</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static LanguageCultureName get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			LanguageCultureName result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Language Culture Name</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static LanguageCultureName getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			LanguageCultureName result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Language Culture Name</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static LanguageCultureName get(int value) {
		switch (value) {
			case EN_US_VALUE: return EN_US;
			case DE_DE_VALUE: return DE_DE;
			case FR_FR_VALUE: return FR_FR;
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private LanguageCultureName(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getValue() {
	  return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
	  return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLiteral() {
	  return literal;
	}

	/**
	 * Returns the literal value of the enumerator, which is its string representation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}
	
} //LanguageCultureName

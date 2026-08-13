/**
 * <copyright>
 * 
 * Copyright (c) 2014-2015 itemis and others.
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
package org.eclipse.sphinx.emf.check.catalog;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Constraint</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.sphinx.emf.check.catalog.Constraint#getSeverity <em>Severity</em>}</li>
 *   <li>{@link org.eclipse.sphinx.emf.check.catalog.Constraint#getMessage <em>Message</em>}</li>
 *   <li>{@link org.eclipse.sphinx.emf.check.catalog.Constraint#getCategories <em>Categories</em>}</li>
 *   <li>{@link org.eclipse.sphinx.emf.check.catalog.Constraint#getComment <em>Comment</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.emf.check.catalog.CheckCatalogPackage#getConstraint()
 * @model
 * @generated
 */
public interface Constraint extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Severity</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.sphinx.emf.check.catalog.Severity}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Severity</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Severity</em>' attribute.
	 * @see org.eclipse.sphinx.emf.check.catalog.Severity
	 * @see #setSeverity(Severity)
	 * @see org.eclipse.sphinx.emf.check.catalog.CheckCatalogPackage#getConstraint_Severity()
	 * @model
	 * @generated
	 */
	Severity getSeverity();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.emf.check.catalog.Constraint#getSeverity <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Severity</em>' attribute.
	 * @see org.eclipse.sphinx.emf.check.catalog.Severity
	 * @see #getSeverity()
	 * @generated
	 */
	void setSeverity(Severity value);

	/**
	 * Returns the value of the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Message</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Message</em>' attribute.
	 * @see #setMessage(String)
	 * @see org.eclipse.sphinx.emf.check.catalog.CheckCatalogPackage#getConstraint_Message()
	 * @model
	 * @generated
	 */
	String getMessage();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.emf.check.catalog.Constraint#getMessage <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Message</em>' attribute.
	 * @see #getMessage()
	 * @generated
	 */
	void setMessage(String value);

	/**
	 * Returns the value of the '<em><b>Categories</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.sphinx.emf.check.catalog.Category}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Categories</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Categories</em>' reference list.
	 * @see org.eclipse.sphinx.emf.check.catalog.CheckCatalogPackage#getConstraint_Categories()
	 * @model
	 * @generated
	 */
	EList<Category> getCategories();

	/**
	 * Returns the value of the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Comment</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Comment</em>' attribute.
	 * @see #setComment(String)
	 * @see org.eclipse.sphinx.emf.check.catalog.CheckCatalogPackage#getConstraint_Comment()
	 * @model
	 * @generated
	 */
	String getComment();

	/**
	 * Sets the value of the '{@link org.eclipse.sphinx.emf.check.catalog.Constraint#getComment <em>Comment</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Comment</em>' attribute.
	 * @see #getComment()
	 * @generated
	 */
	void setComment(String value);

} // Constraint

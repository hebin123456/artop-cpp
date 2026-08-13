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
 * A representation of the model object '<em><b>Catalog</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.sphinx.emf.check.catalog.Catalog#getCategories <em>Categories</em>}</li>
 *   <li>{@link org.eclipse.sphinx.emf.check.catalog.Catalog#getConstraints <em>Constraints</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.sphinx.emf.check.catalog.CheckCatalogPackage#getCatalog()
 * @model
 * @generated
 */
public interface Catalog extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Categories</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.sphinx.emf.check.catalog.Category}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Categories</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Categories</em>' containment reference list.
	 * @see org.eclipse.sphinx.emf.check.catalog.CheckCatalogPackage#getCatalog_Categories()
	 * @model containment="true"
	 * @generated
	 */
	EList<Category> getCategories();

	/**
	 * Returns the value of the '<em><b>Constraints</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.sphinx.emf.check.catalog.Constraint}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Constraints</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Constraints</em>' containment reference list.
	 * @see org.eclipse.sphinx.emf.check.catalog.CheckCatalogPackage#getCatalog_Constraints()
	 * @model containment="true"
	 * @generated
	 */
	EList<Constraint> getConstraints();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model annotation="http://www.eclipse.org/emf/2002/GenModel body='for (Constraint constraint : getConstraints()) {\n\tString id = constraint.getId();\n\tif (id != null && id.equals(constraintId)) {\n\t\treturn constraint.getMessage();\n\t}\n}\nreturn null;'"
	 * @generated
	 */
	String getMessage(String constraintId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model annotation="http://www.eclipse.org/emf/2002/GenModel body='for (Constraint contraint : getConstraints()) {\n\tString id = contraint.getId();\n\tif (id != null && id.equals(constraintId)) {\n\t\treturn contraint.getSeverity();\n\t}\n}\nreturn Severity.ERROR;'"
	 * @generated
	 */
	Severity getSeverity(String constraintId);

} // Catalog

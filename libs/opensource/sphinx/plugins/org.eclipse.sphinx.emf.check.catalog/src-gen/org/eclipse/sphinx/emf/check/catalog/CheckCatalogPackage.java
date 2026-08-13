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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.sphinx.emf.check.catalog.CheckCatalogFactory
 * @model kind="package"
 * @generated
 */
public interface CheckCatalogPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "catalog";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/sphinx/check/catalog";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "cc";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	CheckCatalogPackage eINSTANCE = org.eclipse.sphinx.emf.check.catalog.impl.CheckCatalogPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.emf.check.catalog.impl.IdentifiableImpl <em>Identifiable</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.emf.check.catalog.impl.IdentifiableImpl
	 * @see org.eclipse.sphinx.emf.check.catalog.impl.CheckCatalogPackageImpl#getIdentifiable()
	 * @generated
	 */
	int IDENTIFIABLE = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIABLE__ID = 0;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIABLE__LABEL = 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIABLE__DESCRIPTION = 2;

	/**
	 * The number of structural features of the '<em>Identifiable</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIABLE_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Identifiable</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIABLE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.emf.check.catalog.impl.CatalogImpl <em>Catalog</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.emf.check.catalog.impl.CatalogImpl
	 * @see org.eclipse.sphinx.emf.check.catalog.impl.CheckCatalogPackageImpl#getCatalog()
	 * @generated
	 */
	int CATALOG = 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__ID = IDENTIFIABLE__ID;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__LABEL = IDENTIFIABLE__LABEL;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__DESCRIPTION = IDENTIFIABLE__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Categories</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__CATEGORIES = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Constraints</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__CONSTRAINTS = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Catalog</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The operation id for the '<em>Get Message</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG___GET_MESSAGE__STRING = IDENTIFIABLE_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Severity</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG___GET_SEVERITY__STRING = IDENTIFIABLE_OPERATION_COUNT + 1;

	/**
	 * The number of operations of the '<em>Catalog</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG_OPERATION_COUNT = IDENTIFIABLE_OPERATION_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.emf.check.catalog.impl.ConstraintImpl <em>Constraint</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.emf.check.catalog.impl.ConstraintImpl
	 * @see org.eclipse.sphinx.emf.check.catalog.impl.CheckCatalogPackageImpl#getConstraint()
	 * @generated
	 */
	int CONSTRAINT = 2;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONSTRAINT__ID = IDENTIFIABLE__ID;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONSTRAINT__LABEL = IDENTIFIABLE__LABEL;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONSTRAINT__DESCRIPTION = IDENTIFIABLE__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Severity</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONSTRAINT__SEVERITY = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONSTRAINT__MESSAGE = IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Categories</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONSTRAINT__CATEGORIES = IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONSTRAINT__COMMENT = IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Constraint</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONSTRAINT_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The number of operations of the '<em>Constraint</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONSTRAINT_OPERATION_COUNT = IDENTIFIABLE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.emf.check.catalog.impl.CategoryImpl <em>Category</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.emf.check.catalog.impl.CategoryImpl
	 * @see org.eclipse.sphinx.emf.check.catalog.impl.CheckCatalogPackageImpl#getCategory()
	 * @generated
	 */
	int CATEGORY = 3;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATEGORY__ID = IDENTIFIABLE__ID;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATEGORY__LABEL = IDENTIFIABLE__LABEL;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATEGORY__DESCRIPTION = IDENTIFIABLE__DESCRIPTION;

	/**
	 * The number of structural features of the '<em>Category</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATEGORY_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Category</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATEGORY_OPERATION_COUNT = IDENTIFIABLE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.emf.check.catalog.Severity <em>Severity</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.emf.check.catalog.Severity
	 * @see org.eclipse.sphinx.emf.check.catalog.impl.CheckCatalogPackageImpl#getSeverity()
	 * @generated
	 */
	int SEVERITY = 4;


	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.emf.check.catalog.Identifiable <em>Identifiable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Identifiable</em>'.
	 * @see org.eclipse.sphinx.emf.check.catalog.Identifiable
	 * @generated
	 */
	EClass getIdentifiable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.emf.check.catalog.Identifiable#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.eclipse.sphinx.emf.check.catalog.Identifiable#getId()
	 * @see #getIdentifiable()
	 * @generated
	 */
	EAttribute getIdentifiable_Id();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.emf.check.catalog.Identifiable#getLabel <em>Label</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Label</em>'.
	 * @see org.eclipse.sphinx.emf.check.catalog.Identifiable#getLabel()
	 * @see #getIdentifiable()
	 * @generated
	 */
	EAttribute getIdentifiable_Label();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.emf.check.catalog.Identifiable#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.sphinx.emf.check.catalog.Identifiable#getDescription()
	 * @see #getIdentifiable()
	 * @generated
	 */
	EAttribute getIdentifiable_Description();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.emf.check.catalog.Catalog <em>Catalog</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Catalog</em>'.
	 * @see org.eclipse.sphinx.emf.check.catalog.Catalog
	 * @generated
	 */
	EClass getCatalog();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.sphinx.emf.check.catalog.Catalog#getCategories <em>Categories</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Categories</em>'.
	 * @see org.eclipse.sphinx.emf.check.catalog.Catalog#getCategories()
	 * @see #getCatalog()
	 * @generated
	 */
	EReference getCatalog_Categories();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.sphinx.emf.check.catalog.Catalog#getConstraints <em>Constraints</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Constraints</em>'.
	 * @see org.eclipse.sphinx.emf.check.catalog.Catalog#getConstraints()
	 * @see #getCatalog()
	 * @generated
	 */
	EReference getCatalog_Constraints();

	/**
	 * Returns the meta object for the '{@link org.eclipse.sphinx.emf.check.catalog.Catalog#getMessage(java.lang.String) <em>Get Message</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Message</em>' operation.
	 * @see org.eclipse.sphinx.emf.check.catalog.Catalog#getMessage(java.lang.String)
	 * @generated
	 */
	EOperation getCatalog__GetMessage__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.sphinx.emf.check.catalog.Catalog#getSeverity(java.lang.String) <em>Get Severity</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Severity</em>' operation.
	 * @see org.eclipse.sphinx.emf.check.catalog.Catalog#getSeverity(java.lang.String)
	 * @generated
	 */
	EOperation getCatalog__GetSeverity__String();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.emf.check.catalog.Constraint <em>Constraint</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Constraint</em>'.
	 * @see org.eclipse.sphinx.emf.check.catalog.Constraint
	 * @generated
	 */
	EClass getConstraint();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.emf.check.catalog.Constraint#getSeverity <em>Severity</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Severity</em>'.
	 * @see org.eclipse.sphinx.emf.check.catalog.Constraint#getSeverity()
	 * @see #getConstraint()
	 * @generated
	 */
	EAttribute getConstraint_Severity();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.emf.check.catalog.Constraint#getMessage <em>Message</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Message</em>'.
	 * @see org.eclipse.sphinx.emf.check.catalog.Constraint#getMessage()
	 * @see #getConstraint()
	 * @generated
	 */
	EAttribute getConstraint_Message();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.sphinx.emf.check.catalog.Constraint#getCategories <em>Categories</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Categories</em>'.
	 * @see org.eclipse.sphinx.emf.check.catalog.Constraint#getCategories()
	 * @see #getConstraint()
	 * @generated
	 */
	EReference getConstraint_Categories();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.emf.check.catalog.Constraint#getComment <em>Comment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Comment</em>'.
	 * @see org.eclipse.sphinx.emf.check.catalog.Constraint#getComment()
	 * @see #getConstraint()
	 * @generated
	 */
	EAttribute getConstraint_Comment();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.emf.check.catalog.Category <em>Category</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Category</em>'.
	 * @see org.eclipse.sphinx.emf.check.catalog.Category
	 * @generated
	 */
	EClass getCategory();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.sphinx.emf.check.catalog.Severity <em>Severity</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Severity</em>'.
	 * @see org.eclipse.sphinx.emf.check.catalog.Severity
	 * @generated
	 */
	EEnum getSeverity();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	CheckCatalogFactory getCheckCatalogFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.emf.check.catalog.impl.IdentifiableImpl <em>Identifiable</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.emf.check.catalog.impl.IdentifiableImpl
		 * @see org.eclipse.sphinx.emf.check.catalog.impl.CheckCatalogPackageImpl#getIdentifiable()
		 * @generated
		 */
		EClass IDENTIFIABLE = eINSTANCE.getIdentifiable();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDENTIFIABLE__ID = eINSTANCE.getIdentifiable_Id();

		/**
		 * The meta object literal for the '<em><b>Label</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDENTIFIABLE__LABEL = eINSTANCE.getIdentifiable_Label();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDENTIFIABLE__DESCRIPTION = eINSTANCE.getIdentifiable_Description();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.emf.check.catalog.impl.CatalogImpl <em>Catalog</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.emf.check.catalog.impl.CatalogImpl
		 * @see org.eclipse.sphinx.emf.check.catalog.impl.CheckCatalogPackageImpl#getCatalog()
		 * @generated
		 */
		EClass CATALOG = eINSTANCE.getCatalog();

		/**
		 * The meta object literal for the '<em><b>Categories</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CATALOG__CATEGORIES = eINSTANCE.getCatalog_Categories();

		/**
		 * The meta object literal for the '<em><b>Constraints</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CATALOG__CONSTRAINTS = eINSTANCE.getCatalog_Constraints();

		/**
		 * The meta object literal for the '<em><b>Get Message</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation CATALOG___GET_MESSAGE__STRING = eINSTANCE.getCatalog__GetMessage__String();

		/**
		 * The meta object literal for the '<em><b>Get Severity</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation CATALOG___GET_SEVERITY__STRING = eINSTANCE.getCatalog__GetSeverity__String();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.emf.check.catalog.impl.ConstraintImpl <em>Constraint</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.emf.check.catalog.impl.ConstraintImpl
		 * @see org.eclipse.sphinx.emf.check.catalog.impl.CheckCatalogPackageImpl#getConstraint()
		 * @generated
		 */
		EClass CONSTRAINT = eINSTANCE.getConstraint();

		/**
		 * The meta object literal for the '<em><b>Severity</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONSTRAINT__SEVERITY = eINSTANCE.getConstraint_Severity();

		/**
		 * The meta object literal for the '<em><b>Message</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONSTRAINT__MESSAGE = eINSTANCE.getConstraint_Message();

		/**
		 * The meta object literal for the '<em><b>Categories</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CONSTRAINT__CATEGORIES = eINSTANCE.getConstraint_Categories();

		/**
		 * The meta object literal for the '<em><b>Comment</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONSTRAINT__COMMENT = eINSTANCE.getConstraint_Comment();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.emf.check.catalog.impl.CategoryImpl <em>Category</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.emf.check.catalog.impl.CategoryImpl
		 * @see org.eclipse.sphinx.emf.check.catalog.impl.CheckCatalogPackageImpl#getCategory()
		 * @generated
		 */
		EClass CATEGORY = eINSTANCE.getCategory();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.emf.check.catalog.Severity <em>Severity</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.emf.check.catalog.Severity
		 * @see org.eclipse.sphinx.emf.check.catalog.impl.CheckCatalogPackageImpl#getSeverity()
		 * @generated
		 */
		EEnum SEVERITY = eINSTANCE.getSeverity();

	}

} //CheckCatalogPackage

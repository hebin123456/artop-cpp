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
package org.eclipse.sphinx.emf.check.catalog.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.sphinx.emf.check.catalog.Catalog;
import org.eclipse.sphinx.emf.check.catalog.Category;
import org.eclipse.sphinx.emf.check.catalog.CheckCatalogFactory;
import org.eclipse.sphinx.emf.check.catalog.CheckCatalogPackage;
import org.eclipse.sphinx.emf.check.catalog.Constraint;
import org.eclipse.sphinx.emf.check.catalog.Identifiable;
import org.eclipse.sphinx.emf.check.catalog.Severity;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class CheckCatalogPackageImpl extends EPackageImpl implements CheckCatalogPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass identifiableEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass catalogEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass constraintEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass categoryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum severityEEnum = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.sphinx.emf.check.catalog.CheckCatalogPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private CheckCatalogPackageImpl() {
		super(eNS_URI, CheckCatalogFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link CheckCatalogPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static CheckCatalogPackage init() {
		if (isInited) return (CheckCatalogPackage)EPackage.Registry.INSTANCE.getEPackage(CheckCatalogPackage.eNS_URI);

		// Obtain or create and register package
		CheckCatalogPackageImpl theCheckCatalogPackage = (CheckCatalogPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof CheckCatalogPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new CheckCatalogPackageImpl());

		isInited = true;

		// Create package meta-data objects
		theCheckCatalogPackage.createPackageContents();

		// Initialize created meta-data
		theCheckCatalogPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theCheckCatalogPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(CheckCatalogPackage.eNS_URI, theCheckCatalogPackage);
		return theCheckCatalogPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIdentifiable() {
		return identifiableEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIdentifiable_Id() {
		return (EAttribute)identifiableEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIdentifiable_Label() {
		return (EAttribute)identifiableEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIdentifiable_Description() {
		return (EAttribute)identifiableEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getCatalog() {
		return catalogEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCatalog_Categories() {
		return (EReference)catalogEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCatalog_Constraints() {
		return (EReference)catalogEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EOperation getCatalog__GetMessage__String() {
		return catalogEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EOperation getCatalog__GetSeverity__String() {
		return catalogEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getConstraint() {
		return constraintEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getConstraint_Severity() {
		return (EAttribute)constraintEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getConstraint_Message() {
		return (EAttribute)constraintEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getConstraint_Categories() {
		return (EReference)constraintEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getConstraint_Comment() {
		return (EAttribute)constraintEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getCategory() {
		return categoryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getSeverity() {
		return severityEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CheckCatalogFactory getCheckCatalogFactory() {
		return (CheckCatalogFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		identifiableEClass = createEClass(IDENTIFIABLE);
		createEAttribute(identifiableEClass, IDENTIFIABLE__ID);
		createEAttribute(identifiableEClass, IDENTIFIABLE__LABEL);
		createEAttribute(identifiableEClass, IDENTIFIABLE__DESCRIPTION);

		catalogEClass = createEClass(CATALOG);
		createEReference(catalogEClass, CATALOG__CATEGORIES);
		createEReference(catalogEClass, CATALOG__CONSTRAINTS);
		createEOperation(catalogEClass, CATALOG___GET_MESSAGE__STRING);
		createEOperation(catalogEClass, CATALOG___GET_SEVERITY__STRING);

		constraintEClass = createEClass(CONSTRAINT);
		createEAttribute(constraintEClass, CONSTRAINT__SEVERITY);
		createEAttribute(constraintEClass, CONSTRAINT__MESSAGE);
		createEReference(constraintEClass, CONSTRAINT__CATEGORIES);
		createEAttribute(constraintEClass, CONSTRAINT__COMMENT);

		categoryEClass = createEClass(CATEGORY);

		// Create enums
		severityEEnum = createEEnum(SEVERITY);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		catalogEClass.getESuperTypes().add(this.getIdentifiable());
		constraintEClass.getESuperTypes().add(this.getIdentifiable());
		categoryEClass.getESuperTypes().add(this.getIdentifiable());

		// Initialize classes, features, and operations; add parameters
		initEClass(identifiableEClass, Identifiable.class, "Identifiable", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIdentifiable_Id(), ecorePackage.getEString(), "id", null, 1, 1, Identifiable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIdentifiable_Label(), ecorePackage.getEString(), "label", null, 0, 1, Identifiable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIdentifiable_Description(), ecorePackage.getEString(), "description", null, 0, 1, Identifiable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(catalogEClass, Catalog.class, "Catalog", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getCatalog_Categories(), this.getCategory(), null, "categories", null, 0, -1, Catalog.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCatalog_Constraints(), this.getConstraint(), null, "constraints", null, 0, -1, Catalog.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		EOperation op = initEOperation(getCatalog__GetMessage__String(), ecorePackage.getEString(), "getMessage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "constraintId", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getCatalog__GetSeverity__String(), this.getSeverity(), "getSeverity", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "constraintId", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(constraintEClass, Constraint.class, "Constraint", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getConstraint_Severity(), this.getSeverity(), "severity", null, 0, 1, Constraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getConstraint_Message(), ecorePackage.getEString(), "message", null, 0, 1, Constraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getConstraint_Categories(), this.getCategory(), null, "categories", null, 0, -1, Constraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getConstraint_Comment(), ecorePackage.getEString(), "comment", null, 0, 1, Constraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(categoryEClass, Category.class, "Category", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		// Initialize enums and add enum literals
		initEEnum(severityEEnum, Severity.class, "Severity");
		addEEnumLiteral(severityEEnum, Severity.ERROR);
		addEEnumLiteral(severityEEnum, Severity.WARNING);
		addEEnumLiteral(severityEEnum, Severity.INFO);

		// Create resource
		createResource(eNS_URI);
	}

} //CheckCatalogPackageImpl

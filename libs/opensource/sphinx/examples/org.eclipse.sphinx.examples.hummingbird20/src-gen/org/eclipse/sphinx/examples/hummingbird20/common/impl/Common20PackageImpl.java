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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Factory;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Package;
import org.eclipse.sphinx.examples.hummingbird20.common.Description;
import org.eclipse.sphinx.examples.hummingbird20.common.Identifiable;
import org.eclipse.sphinx.examples.hummingbird20.common.LanguageCultureName;
import org.eclipse.sphinx.examples.hummingbird20.common.Translation;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.TypeModel20PackageImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!-- end-user-doc -->
 * @generated
 */
public class Common20PackageImpl extends EPackageImpl implements Common20Package {
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass identifiableEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass descriptionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass translationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum languageCultureNameEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType euriEDataType = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with {@link org.eclipse.emf.ecore.EPackage.Registry
	 * EPackage.Registry} by the package package URI value.
	 * <p>
	 * Note: the correct way to create the package is via the static factory method {@link #init init()}, which also
	 * performs initialization of the package, or returns the registered package, if one already exists. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.sphinx.examples.hummingbird20.common.Common20Package#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private Common20PackageImpl() {
		super(eNS_URI, Common20Factory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link Common20Package#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static Common20Package init() {
		if (isInited) return (Common20Package)EPackage.Registry.INSTANCE.getEPackage(Common20Package.eNS_URI);

		// Obtain or create and register package
		Common20PackageImpl theCommon20Package = (Common20PackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof Common20PackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new Common20PackageImpl());

		isInited = true;

		// Initialize simple dependencies
		EcorePackage.eINSTANCE.eClass();
		XMLTypePackage.eINSTANCE.eClass();

		// Obtain or create and register interdependencies
		TypeModel20PackageImpl theTypeModel20Package = (TypeModel20PackageImpl)(EPackage.Registry.INSTANCE.getEPackage(TypeModel20Package.eNS_URI) instanceof TypeModel20PackageImpl ? EPackage.Registry.INSTANCE.getEPackage(TypeModel20Package.eNS_URI) : TypeModel20Package.eINSTANCE);
		InstanceModel20PackageImpl theInstanceModel20Package = (InstanceModel20PackageImpl)(EPackage.Registry.INSTANCE.getEPackage(InstanceModel20Package.eNS_URI) instanceof InstanceModel20PackageImpl ? EPackage.Registry.INSTANCE.getEPackage(InstanceModel20Package.eNS_URI) : InstanceModel20Package.eINSTANCE);

		// Create package meta-data objects
		theCommon20Package.createPackageContents();
		theTypeModel20Package.createPackageContents();
		theInstanceModel20Package.createPackageContents();

		// Initialize created meta-data
		theCommon20Package.initializePackageContents();
		theTypeModel20Package.initializePackageContents();
		theInstanceModel20Package.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theCommon20Package.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(Common20Package.eNS_URI, theCommon20Package);
		return theCommon20Package;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIdentifiable() {
		return identifiableEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIdentifiable_Name() {
		return (EAttribute)identifiableEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIdentifiable_Description() {
		return (EReference)identifiableEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDescription() {
		return descriptionEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDescription_Mixed() {
		return (EAttribute)descriptionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDescription_Language() {
		return (EAttribute)descriptionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDescription_Translations() {
		return (EReference)descriptionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTranslation() {
		return translationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTranslation_Language() {
		return (EAttribute)translationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTranslation_ResourceURI() {
		return (EAttribute)translationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getLanguageCultureName() {
		return languageCultureNameEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getEURI() {
		return euriEDataType;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Common20Factory getCommon20Factory() {
		return (Common20Factory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		identifiableEClass = createEClass(IDENTIFIABLE);
		createEAttribute(identifiableEClass, IDENTIFIABLE__NAME);
		createEReference(identifiableEClass, IDENTIFIABLE__DESCRIPTION);

		descriptionEClass = createEClass(DESCRIPTION);
		createEAttribute(descriptionEClass, DESCRIPTION__MIXED);
		createEAttribute(descriptionEClass, DESCRIPTION__LANGUAGE);
		createEReference(descriptionEClass, DESCRIPTION__TRANSLATIONS);

		translationEClass = createEClass(TRANSLATION);
		createEAttribute(translationEClass, TRANSLATION__LANGUAGE);
		createEAttribute(translationEClass, TRANSLATION__RESOURCE_URI);

		// Create enums
		languageCultureNameEEnum = createEEnum(LANGUAGE_CULTURE_NAME);

		// Create data types
		euriEDataType = createEDataType(EURI);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
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

		// Initialize classes and features; add operations and parameters
		initEClass(identifiableEClass, Identifiable.class, "Identifiable", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getIdentifiable_Name(), ecorePackage.getEString(), "name", null, 0, 1, Identifiable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getIdentifiable_Description(), this.getDescription(), null, "description", null, 0, 1, Identifiable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(descriptionEClass, Description.class, "Description", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getDescription_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, Description.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getDescription_Language(), this.getLanguageCultureName(), "language", null, 0, 1, Description.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getDescription_Translations(), this.getTranslation(), null, "translations", null, 0, -1, Description.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(translationEClass, Translation.class, "Translation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getTranslation_Language(), this.getLanguageCultureName(), "language", null, 0, 1, Translation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getTranslation_ResourceURI(), this.getEURI(), "resourceURI", null, 0, 1, Translation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		// Initialize enums and add enum literals
		initEEnum(languageCultureNameEEnum, LanguageCultureName.class, "LanguageCultureName"); //$NON-NLS-1$
		addEEnumLiteral(languageCultureNameEEnum, LanguageCultureName.EN_US);
		addEEnumLiteral(languageCultureNameEEnum, LanguageCultureName.DE_DE);
		addEEnumLiteral(languageCultureNameEEnum, LanguageCultureName.FR_FR);

		// Initialize data types
		initEDataType(euriEDataType, URI.class, "EURI", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
		// http:///org/eclipse/emf/ecore/util/ExtendedMetaData
		createExtendedMetaDataAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/GenModel</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createGenModelAnnotations() {
		String source = "http://www.eclipse.org/emf/2002/GenModel"; //$NON-NLS-1$	
		addAnnotation
		  (identifiableEClass, 
		   source, 
		   new String[] {
			 "documentation", "Represents an Identifiable model object" //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (getIdentifiable_Name(), 
		   source, 
		   new String[] {
			 "documentation", "Specifies the name of this Identifiable model object" //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (getIdentifiable_Description(), 
		   source, 
		   new String[] {
			 "documentation", "Specifies the description of this Identifiable model object" //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (descriptionEClass, 
		   source, 
		   new String[] {
			 "documentation", "Represents a Description model object" //$NON-NLS-1$ //$NON-NLS-2$
		   });
	}

	/**
	 * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @generated
	 */
	protected void createExtendedMetaDataAnnotations() {
		String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData"; //$NON-NLS-1$	
		addAnnotation
		  (descriptionEClass, 
		   source, 
		   new String[] {
			 "kind", "mixed" //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (getDescription_Mixed(), 
		   source, 
		   new String[] {
			 "name", ":mixed", //$NON-NLS-1$ //$NON-NLS-2$
			 "kind", "elementWildcard" //$NON-NLS-1$ //$NON-NLS-2$
		   });
	}

} // Common20PackageImpl

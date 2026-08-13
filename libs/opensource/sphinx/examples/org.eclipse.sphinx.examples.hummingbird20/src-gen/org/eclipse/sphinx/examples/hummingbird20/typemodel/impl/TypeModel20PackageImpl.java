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
package org.eclipse.sphinx.examples.hummingbird20.typemodel.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Package;
import org.eclipse.sphinx.examples.hummingbird20.common.impl.Common20PackageImpl;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!-- end-user-doc -->
 * @generated
 */
public class TypeModel20PackageImpl extends EPackageImpl implements TypeModel20Package {
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass platformEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass componentTypeEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass portEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass interfaceEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass parameterEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with {@link org.eclipse.emf.ecore.EPackage.Registry
	 * EPackage.Registry} by the package package URI value.
	 * <p>
	 * Note: the correct way to create the package is via the static factory method {@link #init init()}, which also
	 * performs initialization of the package, or returns the registered package, if one already exists. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private TypeModel20PackageImpl() {
		super(eNS_URI, TypeModel20Factory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * <p>
	 * This method is used to initialize {@link TypeModel20Package#eINSTANCE} when that field is accessed. Clients
	 * should not invoke it directly. Instead, they should simply access that field to obtain the package. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static TypeModel20Package init() {
		if (isInited) return (TypeModel20Package)EPackage.Registry.INSTANCE.getEPackage(TypeModel20Package.eNS_URI);

		// Obtain or create and register package
		TypeModel20PackageImpl theTypeModel20Package = (TypeModel20PackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof TypeModel20PackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new TypeModel20PackageImpl());

		isInited = true;

		// Initialize simple dependencies
		EcorePackage.eINSTANCE.eClass();
		XMLTypePackage.eINSTANCE.eClass();

		// Obtain or create and register interdependencies
		Common20PackageImpl theCommon20Package = (Common20PackageImpl)(EPackage.Registry.INSTANCE.getEPackage(Common20Package.eNS_URI) instanceof Common20PackageImpl ? EPackage.Registry.INSTANCE.getEPackage(Common20Package.eNS_URI) : Common20Package.eINSTANCE);
		InstanceModel20PackageImpl theInstanceModel20Package = (InstanceModel20PackageImpl)(EPackage.Registry.INSTANCE.getEPackage(InstanceModel20Package.eNS_URI) instanceof InstanceModel20PackageImpl ? EPackage.Registry.INSTANCE.getEPackage(InstanceModel20Package.eNS_URI) : InstanceModel20Package.eINSTANCE);

		// Create package meta-data objects
		theTypeModel20Package.createPackageContents();
		theCommon20Package.createPackageContents();
		theInstanceModel20Package.createPackageContents();

		// Initialize created meta-data
		theTypeModel20Package.initializePackageContents();
		theCommon20Package.initializePackageContents();
		theInstanceModel20Package.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theTypeModel20Package.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(TypeModel20Package.eNS_URI, theTypeModel20Package);
		return theTypeModel20Package;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPlatform() {
		return platformEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPlatform_ComponentTypes() {
		return (EReference)platformEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPlatform_Interfaces() {
		return (EReference)platformEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPlatform_Mixed() {
		return (EAttribute)platformEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPlatform_XSISchemaLocation() {
		return (EReference)platformEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getComponentType() {
		return componentTypeEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getComponentType_ProvidedInterfaces() {
		return (EReference)componentTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getComponentType_Ports() {
		return (EReference)componentTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getComponentType_Parameters() {
		return (EReference)componentTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPort() {
		return portEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPort_Owner() {
		return (EReference)portEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPort_RequiredInterface() {
		return (EReference)portEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPort_MinProviderCount() {
		return (EAttribute)portEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPort_MaxProviderCount() {
		return (EAttribute)portEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getInterface() {
		return interfaceEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getInterface_ProvidingComponentTypes() {
		return (EReference)interfaceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getInterface_RequiringPorts() {
		return (EReference)interfaceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getParameter() {
		return parameterEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameter_DataType() {
		return (EAttribute)parameterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameter_Optional() {
		return (EAttribute)parameterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeModel20Factory getTypeModel20Factory() {
		return (TypeModel20Factory)getEFactoryInstance();
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
		platformEClass = createEClass(PLATFORM);
		createEReference(platformEClass, PLATFORM__COMPONENT_TYPES);
		createEReference(platformEClass, PLATFORM__INTERFACES);
		createEAttribute(platformEClass, PLATFORM__MIXED);
		createEReference(platformEClass, PLATFORM__XSI_SCHEMA_LOCATION);

		componentTypeEClass = createEClass(COMPONENT_TYPE);
		createEReference(componentTypeEClass, COMPONENT_TYPE__PROVIDED_INTERFACES);
		createEReference(componentTypeEClass, COMPONENT_TYPE__PORTS);
		createEReference(componentTypeEClass, COMPONENT_TYPE__PARAMETERS);

		portEClass = createEClass(PORT);
		createEReference(portEClass, PORT__OWNER);
		createEReference(portEClass, PORT__REQUIRED_INTERFACE);
		createEAttribute(portEClass, PORT__MIN_PROVIDER_COUNT);
		createEAttribute(portEClass, PORT__MAX_PROVIDER_COUNT);

		interfaceEClass = createEClass(INTERFACE);
		createEReference(interfaceEClass, INTERFACE__PROVIDING_COMPONENT_TYPES);
		createEReference(interfaceEClass, INTERFACE__REQUIRING_PORTS);

		parameterEClass = createEClass(PARAMETER);
		createEAttribute(parameterEClass, PARAMETER__DATA_TYPE);
		createEAttribute(parameterEClass, PARAMETER__OPTIONAL);
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

		// Obtain other dependent packages
		Common20Package theCommon20Package = (Common20Package)EPackage.Registry.INSTANCE.getEPackage(Common20Package.eNS_URI);
		EcorePackage theEcorePackage = (EcorePackage)EPackage.Registry.INSTANCE.getEPackage(EcorePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		platformEClass.getESuperTypes().add(theCommon20Package.getIdentifiable());
		componentTypeEClass.getESuperTypes().add(theCommon20Package.getIdentifiable());
		portEClass.getESuperTypes().add(theCommon20Package.getIdentifiable());
		interfaceEClass.getESuperTypes().add(theCommon20Package.getIdentifiable());
		parameterEClass.getESuperTypes().add(theCommon20Package.getIdentifiable());

		// Initialize classes and features; add operations and parameters
		initEClass(platformEClass, Platform.class, "Platform", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getPlatform_ComponentTypes(), this.getComponentType(), null, "componentTypes", null, 1, -1, Platform.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getPlatform_Interfaces(), this.getInterface(), null, "interfaces", null, 0, -1, Platform.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlatform_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, Platform.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getPlatform_XSISchemaLocation(), theEcorePackage.getEStringToStringMapEntry(), null, "xSISchemaLocation", null, 0, -1, Platform.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(componentTypeEClass, ComponentType.class, "ComponentType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getComponentType_ProvidedInterfaces(), this.getInterface(), this.getInterface_ProvidingComponentTypes(), "providedInterfaces", null, 0, -1, ComponentType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getComponentType_Ports(), this.getPort(), this.getPort_Owner(), "ports", null, 0, -1, ComponentType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getComponentType_Parameters(), this.getParameter(), null, "parameters", null, 0, -1, ComponentType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(portEClass, Port.class, "Port", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getPort_Owner(), this.getComponentType(), this.getComponentType_Ports(), "owner", null, 1, 1, Port.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getPort_RequiredInterface(), this.getInterface(), this.getInterface_RequiringPorts(), "requiredInterface", null, 1, 1, Port.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPort_MinProviderCount(), ecorePackage.getEInt(), "minProviderCount", null, 0, 1, Port.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPort_MaxProviderCount(), ecorePackage.getEInt(), "maxProviderCount", null, 0, 1, Port.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(interfaceEClass, Interface.class, "Interface", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getInterface_ProvidingComponentTypes(), this.getComponentType(), this.getComponentType_ProvidedInterfaces(), "providingComponentTypes", null, 0, -1, Interface.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getInterface_RequiringPorts(), this.getPort(), this.getPort_RequiredInterface(), "requiringPorts", null, 0, -1, Interface.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(parameterEClass, Parameter.class, "Parameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getParameter_DataType(), ecorePackage.getEString(), "dataType", null, 0, 1, Parameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getParameter_Optional(), ecorePackage.getEBoolean(), "optional", null, 0, 1, Parameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http:///org/eclipse/emf/ecore/util/ExtendedMetaData
		createExtendedMetaDataAnnotations();
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
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
		  (platformEClass, 
		   source, 
		   new String[] {
			 "kind", "mixed" //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (getPlatform_Mixed(), 
		   source, 
		   new String[] {
			 "name", ":mixed", //$NON-NLS-1$ //$NON-NLS-2$
			 "kind", "elementWildcard" //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (getPlatform_XSISchemaLocation(), 
		   source, 
		   new String[] {
			 "name", "xsi:schemaLocation", //$NON-NLS-1$ //$NON-NLS-2$
			 "kind", "element" //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (getComponentType_Parameters(), 
		   source, 
		   new String[] {
			 "ordered", "false" //$NON-NLS-1$ //$NON-NLS-2$
		   });
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
		  (platformEClass, 
		   source, 
		   new String[] {
			 "documentation", "Represents a Platform model object" //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (getPlatform_ComponentTypes(), 
		   source, 
		   new String[] {
			 "documentation", "Aggregates all Component Types with this Platform..." //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (getPlatform_Interfaces(), 
		   source, 
		   new String[] {
			 "documentation", "Aggregates all Interfaces with this Platform..." //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (componentTypeEClass, 
		   source, 
		   new String[] {
			 "documentation", "Represents a ComponentType model object" //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (getComponentType_Ports(), 
		   source, 
		   new String[] {
			 "documentation", "Aggregates all Ports with this ComponentType..." //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (getComponentType_Parameters(), 
		   source, 
		   new String[] {
			 "documentation", "Aggregates all Parameters with this ComponentType..." //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (portEClass, 
		   source, 
		   new String[] {
			 "documentation", "Represents a Port model object" //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (getPort_Owner(), 
		   source, 
		   new String[] {
			 "documentation", "Specifies the component type owner" //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (getPort_RequiredInterface(), 
		   source, 
		   new String[] {
			 "documentation", "Specifies the required interface" //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (interfaceEClass, 
		   source, 
		   new String[] {
			 "documentation", "Represents an Interface model object" //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (getInterface_ProvidingComponentTypes(), 
		   source, 
		   new String[] {
			 "documentation", "Specifies the providing component types" //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (getInterface_RequiringPorts(), 
		   source, 
		   new String[] {
			 "documentation", "Specifies the requiring ports" //$NON-NLS-1$ //$NON-NLS-2$
		   });	
		addAnnotation
		  (parameterEClass, 
		   source, 
		   new String[] {
			 "documentation", "Represents a Parameter model object" //$NON-NLS-1$ //$NON-NLS-2$
		   });
	}

} // TypeModel20PackageImpl

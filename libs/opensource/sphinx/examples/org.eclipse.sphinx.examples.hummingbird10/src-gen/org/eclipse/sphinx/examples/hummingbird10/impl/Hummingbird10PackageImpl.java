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
package org.eclipse.sphinx.examples.hummingbird10.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.sphinx.examples.hummingbird10.Application;
import org.eclipse.sphinx.examples.hummingbird10.Component;
import org.eclipse.sphinx.examples.hummingbird10.Connection;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Factory;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.examples.hummingbird10.Interface;
import org.eclipse.sphinx.examples.hummingbird10.Parameter;
import org.eclipse.sphinx.examples.hummingbird10.util.Hummingbird10Validator;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!-- end-user-doc -->
 * 
 * @generated
 */
public class Hummingbird10PackageImpl extends EPackageImpl implements Hummingbird10Package {
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private EClass componentEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private EClass applicationEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private EClass connectionEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private EClass interfaceEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
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
	 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private Hummingbird10PackageImpl() {
		super(eNS_URI, Hummingbird10Factory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * <p>
	 * This method is used to initialize {@link Hummingbird10Package#eINSTANCE} when that field is accessed. Clients
	 * should not invoke it directly. Instead, they should simply access that field to obtain the package. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static Hummingbird10Package init() {
		if (isInited) {
			return (Hummingbird10Package) EPackage.Registry.INSTANCE.getEPackage(Hummingbird10Package.eNS_URI);
		}

		// Obtain or create and register package
		Hummingbird10PackageImpl theHummingbird10Package = (Hummingbird10PackageImpl) (EPackage.Registry.INSTANCE.get(eNS_URI) instanceof Hummingbird10PackageImpl ? EPackage.Registry.INSTANCE
				.get(eNS_URI) : new Hummingbird10PackageImpl());

		isInited = true;

		// Create package meta-data objects
		theHummingbird10Package.createPackageContents();

		// Initialize created meta-data
		theHummingbird10Package.initializePackageContents();

		// Register package validator
		EValidator.Registry.INSTANCE.put(theHummingbird10Package, new EValidator.Descriptor() {
			@Override
			public EValidator getEValidator() {
				return Hummingbird10Validator.INSTANCE;
			}
		});

		// Mark meta-data to indicate it can't be changed
		theHummingbird10Package.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(Hummingbird10Package.eNS_URI, theHummingbird10Package);
		return theHummingbird10Package;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EClass getComponent() {
		return componentEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EAttribute getComponent_Name() {
		return (EAttribute) componentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EReference getComponent_OutgoingConnections() {
		return (EReference) componentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EReference getComponent_ProvidedInterfaces() {
		return (EReference) componentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EReference getComponent_Parameters() {
		return (EReference) componentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EReference getComponent_IncomingConnections() {
		return (EReference) componentEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EClass getApplication() {
		return applicationEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EAttribute getApplication_Name() {
		return (EAttribute) applicationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EReference getApplication_Components() {
		return (EReference) applicationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EReference getApplication_Interfaces() {
		return (EReference) applicationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EClass getConnection() {
		return connectionEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EReference getConnection_RequiredInterface() {
		return (EReference) connectionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EReference getConnection_TargetComponent() {
		return (EReference) connectionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EReference getConnection_SourceComponent() {
		return (EReference) connectionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EAttribute getConnection_Name() {
		return (EAttribute) connectionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EClass getInterface() {
		return interfaceEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EAttribute getInterface_Name() {
		return (EAttribute) interfaceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EReference getInterface_ProvidingComponents() {
		return (EReference) interfaceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EClass getParameter() {
		return parameterEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EAttribute getParameter_Name() {
		return (EAttribute) parameterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EAttribute getParameter_Value() {
		return (EAttribute) parameterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Hummingbird10Factory getHummingbird10Factory() {
		return (Hummingbird10Factory) getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package. This method is guarded to have no affect on any invocation but
	 * its first. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) {
			return;
		}
		isCreated = true;

		// Create classes and their features
		componentEClass = createEClass(COMPONENT);
		createEAttribute(componentEClass, COMPONENT__NAME);
		createEReference(componentEClass, COMPONENT__OUTGOING_CONNECTIONS);
		createEReference(componentEClass, COMPONENT__PROVIDED_INTERFACES);
		createEReference(componentEClass, COMPONENT__PARAMETERS);
		createEReference(componentEClass, COMPONENT__INCOMING_CONNECTIONS);

		applicationEClass = createEClass(APPLICATION);
		createEAttribute(applicationEClass, APPLICATION__NAME);
		createEReference(applicationEClass, APPLICATION__COMPONENTS);
		createEReference(applicationEClass, APPLICATION__INTERFACES);

		connectionEClass = createEClass(CONNECTION);
		createEReference(connectionEClass, CONNECTION__REQUIRED_INTERFACE);
		createEReference(connectionEClass, CONNECTION__TARGET_COMPONENT);
		createEReference(connectionEClass, CONNECTION__SOURCE_COMPONENT);
		createEAttribute(connectionEClass, CONNECTION__NAME);

		interfaceEClass = createEClass(INTERFACE);
		createEAttribute(interfaceEClass, INTERFACE__NAME);
		createEReference(interfaceEClass, INTERFACE__PROVIDING_COMPONENTS);

		parameterEClass = createEClass(PARAMETER);
		createEAttribute(parameterEClass, PARAMETER__NAME);
		createEAttribute(parameterEClass, PARAMETER__VALUE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model. This method is guarded to have no affect on any
	 * invocation but its first. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) {
			return;
		}
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes and features; add operations and parameters
		initEClass(componentEClass, Component.class, "Component", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(
				getComponent_Name(),
				ecorePackage.getEString(),
				"name", "", 1, 1, Component.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEReference(
				getComponent_OutgoingConnections(),
				getConnection(),
				getConnection_SourceComponent(),
				"outgoingConnections", null, 0, -1, Component.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(
				getComponent_ProvidedInterfaces(),
				getInterface(),
				getInterface_ProvidingComponents(),
				"providedInterfaces", null, 0, -1, Component.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(
				getComponent_Parameters(),
				getParameter(),
				null,
				"parameters", null, 0, -1, Component.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(
				getComponent_IncomingConnections(),
				getConnection(),
				getConnection_TargetComponent(),
				"incomingConnections", null, 0, -1, Component.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(applicationEClass, Application.class, "Application", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(
				getApplication_Name(),
				ecorePackage.getEString(),
				"name", null, 1, 1, Application.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(
				getApplication_Components(),
				getComponent(),
				null,
				"components", null, 1, -1, Application.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(
				getApplication_Interfaces(),
				getInterface(),
				null,
				"interfaces", null, 0, -1, Application.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(connectionEClass, Connection.class, "Connection", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(
				getConnection_RequiredInterface(),
				getInterface(),
				null,
				"requiredInterface", null, 1, 1, Connection.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(
				getConnection_TargetComponent(),
				getComponent(),
				getComponent_IncomingConnections(),
				"targetComponent", null, 1, 1, Connection.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(
				getConnection_SourceComponent(),
				getComponent(),
				getComponent_OutgoingConnections(),
				"sourceComponent", null, 1, 1, Connection.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(
				getConnection_Name(),
				ecorePackage.getEString(),
				"name", null, 1, 1, Connection.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(interfaceEClass, Interface.class, "Interface", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(
				getInterface_Name(),
				ecorePackage.getEString(),
				"name", null, 1, 1, Interface.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(
				getInterface_ProvidingComponents(),
				getComponent(),
				getComponent_ProvidedInterfaces(),
				"providingComponents", null, 0, -1, Interface.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(parameterEClass, Parameter.class, "Parameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(
				getParameter_Name(),
				ecorePackage.getEString(),
				"name", null, 0, 1, Parameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(
				getParameter_Value(),
				ecorePackage.getEString(),
				"value", null, 0, 1, Parameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/emf/2002/Ecore
		createEcoreAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/Ecore</b>. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected void createEcoreAnnotations() {
		String source = "http://www.eclipse.org/emf/2002/Ecore"; //$NON-NLS-1$
		addAnnotation(connectionEClass, source, new String[] { "constraints", "validateInterfacesImplementedByTargetComponent" //$NON-NLS-1$ //$NON-NLS-2$
		});
	}

} // Hummingbird10PackageImpl

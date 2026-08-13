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
package org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Package;
import org.eclipse.sphinx.examples.hummingbird20.common.impl.Common20PackageImpl;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.CustomApplication;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Formula;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.TypeModel20PackageImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!-- end-user-doc -->
 * @generated
 */
public class InstanceModel20PackageImpl extends EPackageImpl implements InstanceModel20Package {
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass applicationEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass componentEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass connectionEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass parameterValueEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass parameterExpressionEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass formulaEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass customApplicationEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with {@link org.eclipse.emf.ecore.EPackage.Registry
	 * EPackage.Registry} by the package package URI value.
	 * <p>
	 * Note: the correct way to create the package is via the static factory method {@link #init init()}, which also
	 * performs initialization of the package, or returns the registered package, if one already exists. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private InstanceModel20PackageImpl() {
		super(eNS_URI, InstanceModel20Factory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * <p>
	 * This method is used to initialize {@link InstanceModel20Package#eINSTANCE} when that field is accessed. Clients
	 * should not invoke it directly. Instead, they should simply access that field to obtain the package. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static InstanceModel20Package init() {
		if (isInited) return (InstanceModel20Package)EPackage.Registry.INSTANCE.getEPackage(InstanceModel20Package.eNS_URI);

		// Obtain or create and register package
		Object registeredInstanceModel20Package = EPackage.Registry.INSTANCE.get(eNS_URI);
		InstanceModel20PackageImpl theInstanceModel20Package = registeredInstanceModel20Package instanceof InstanceModel20PackageImpl ? (InstanceModel20PackageImpl)registeredInstanceModel20Package : new InstanceModel20PackageImpl();

		isInited = true;

		// Initialize simple dependencies
		EcorePackage.eINSTANCE.eClass();
		XMLTypePackage.eINSTANCE.eClass();

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(Common20Package.eNS_URI);
		Common20PackageImpl theCommon20Package = (Common20PackageImpl)(registeredPackage instanceof Common20PackageImpl ? registeredPackage : Common20Package.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(TypeModel20Package.eNS_URI);
		TypeModel20PackageImpl theTypeModel20Package = (TypeModel20PackageImpl)(registeredPackage instanceof TypeModel20PackageImpl ? registeredPackage : TypeModel20Package.eINSTANCE);

		// Create package meta-data objects
		theInstanceModel20Package.createPackageContents();
		theCommon20Package.createPackageContents();
		theTypeModel20Package.createPackageContents();

		// Initialize created meta-data
		theInstanceModel20Package.initializePackageContents();
		theCommon20Package.initializePackageContents();
		theTypeModel20Package.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theInstanceModel20Package.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(InstanceModel20Package.eNS_URI, theInstanceModel20Package);
		return theInstanceModel20Package;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getApplication() {
		return applicationEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getApplication_Components() {
		return (EReference)applicationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getApplication_Mixed() {
		return (EAttribute)applicationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getApplication_XSISchemaLocation() {
		return (EReference)applicationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getApplication_MixedOuterContent() {
		return (EAttribute)applicationEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getComponent() {
		return componentEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getComponent_Type() {
		return (EReference)componentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getComponent_OutgoingConnections() {
		return (EReference)componentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getComponent_IncomingConnections() {
		return (EReference)componentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getComponent_ParameterValues() {
		return (EReference)componentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getComponent_ParameterExpressions() {
		return (EReference)componentEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getConnection() {
		return connectionEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getConnection_SourceComponent() {
		return (EReference)connectionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getConnection_SourcePort() {
		return (EReference)connectionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getConnection_TargetComponent() {
		return (EReference)connectionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getParameterValue() {
		return parameterValueEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getParameterValue_Type() {
		return (EReference)parameterValueEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameterValue_Value() {
		return (EAttribute)parameterValueEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getParameterExpression() {
		return parameterExpressionEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameterExpression_Mixed() {
		return (EAttribute)parameterExpressionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getParameterExpression_Expressions() {
		return (EReference)parameterExpressionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFormula() {
		return formulaEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFormula_Value() {
		return (EAttribute)formulaEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCustomApplication() {
		return customApplicationEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public InstanceModel20Factory getInstanceModel20Factory() {
		return (InstanceModel20Factory)getEFactoryInstance();
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
		applicationEClass = createEClass(APPLICATION);
		createEReference(applicationEClass, APPLICATION__COMPONENTS);
		createEAttribute(applicationEClass, APPLICATION__MIXED);
		createEReference(applicationEClass, APPLICATION__XSI_SCHEMA_LOCATION);
		createEAttribute(applicationEClass, APPLICATION__MIXED_OUTER_CONTENT);

		componentEClass = createEClass(COMPONENT);
		createEReference(componentEClass, COMPONENT__TYPE);
		createEReference(componentEClass, COMPONENT__OUTGOING_CONNECTIONS);
		createEReference(componentEClass, COMPONENT__INCOMING_CONNECTIONS);
		createEReference(componentEClass, COMPONENT__PARAMETER_VALUES);
		createEReference(componentEClass, COMPONENT__PARAMETER_EXPRESSIONS);

		connectionEClass = createEClass(CONNECTION);
		createEReference(connectionEClass, CONNECTION__SOURCE_COMPONENT);
		createEReference(connectionEClass, CONNECTION__SOURCE_PORT);
		createEReference(connectionEClass, CONNECTION__TARGET_COMPONENT);

		parameterValueEClass = createEClass(PARAMETER_VALUE);
		createEReference(parameterValueEClass, PARAMETER_VALUE__TYPE);
		createEAttribute(parameterValueEClass, PARAMETER_VALUE__VALUE);

		parameterExpressionEClass = createEClass(PARAMETER_EXPRESSION);
		createEAttribute(parameterExpressionEClass, PARAMETER_EXPRESSION__MIXED);
		createEReference(parameterExpressionEClass, PARAMETER_EXPRESSION__EXPRESSIONS);

		formulaEClass = createEClass(FORMULA);
		createEAttribute(formulaEClass, FORMULA__VALUE);

		customApplicationEClass = createEClass(CUSTOM_APPLICATION);
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
		TypeModel20Package theTypeModel20Package = (TypeModel20Package)EPackage.Registry.INSTANCE.getEPackage(TypeModel20Package.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		applicationEClass.getESuperTypes().add(theCommon20Package.getIdentifiable());
		componentEClass.getESuperTypes().add(theCommon20Package.getIdentifiable());
		connectionEClass.getESuperTypes().add(theCommon20Package.getIdentifiable());
		parameterValueEClass.getESuperTypes().add(theCommon20Package.getIdentifiable());
		customApplicationEClass.getESuperTypes().add(this.getApplication());

		// Initialize classes and features; add operations and parameters
		initEClass(applicationEClass, Application.class, "Application", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getApplication_Components(), this.getComponent(), null, "components", null, 1, -1, Application.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getApplication_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, Application.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getApplication_XSISchemaLocation(), theEcorePackage.getEStringToStringMapEntry(), null, "xSISchemaLocation", null, 0, -1, Application.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getApplication_MixedOuterContent(), ecorePackage.getEFeatureMapEntry(), "mixedOuterContent", null, 0, -1, Application.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(componentEClass, Component.class, "Component", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getComponent_Type(), theTypeModel20Package.getComponentType(), null, "type", null, 0, 1, Component.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getComponent_OutgoingConnections(), this.getConnection(), this.getConnection_SourceComponent(), "outgoingConnections", null, 0, -1, Component.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getComponent_IncomingConnections(), this.getConnection(), this.getConnection_TargetComponent(), "incomingConnections", null, 0, -1, Component.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getComponent_ParameterValues(), this.getParameterValue(), null, "parameterValues", null, 0, -1, Component.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getComponent_ParameterExpressions(), this.getParameterExpression(), null, "parameterExpressions", null, 0, -1, Component.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(connectionEClass, Connection.class, "Connection", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getConnection_SourceComponent(), this.getComponent(), this.getComponent_OutgoingConnections(), "sourceComponent", null, 1, 1, Connection.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getConnection_SourcePort(), theTypeModel20Package.getPort(), null, "sourcePort", null, 1, 1, Connection.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getConnection_TargetComponent(), this.getComponent(), this.getComponent_IncomingConnections(), "targetComponent", null, 1, 1, Connection.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(parameterValueEClass, ParameterValue.class, "ParameterValue", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getParameterValue_Type(), theTypeModel20Package.getParameter(), null, "type", null, 1, 1, ParameterValue.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getParameterValue_Value(), ecorePackage.getEString(), "value", null, 0, 1, ParameterValue.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(parameterExpressionEClass, ParameterExpression.class, "ParameterExpression", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getParameterExpression_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, ParameterExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getParameterExpression_Expressions(), this.getFormula(), null, "expressions", null, 1, -1, ParameterExpression.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(formulaEClass, Formula.class, "Formula", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getFormula_Value(), theEcorePackage.getEString(), "value", null, 0, 1, Formula.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(customApplicationEClass, CustomApplication.class, "CustomApplication", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

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
		  (applicationEClass,
		   source,
		   new String[] {
			   "kind", "mixed" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (getApplication_Mixed(),
		   source,
		   new String[] {
			   "name", ":mixed", //$NON-NLS-1$ //$NON-NLS-2$
			   "kind", "elementWildcard" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (getApplication_XSISchemaLocation(),
		   source,
		   new String[] {
			   "name", "xsi:schemaLocation", //$NON-NLS-1$ //$NON-NLS-2$
			   "kind", "element" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (getApplication_MixedOuterContent(),
		   source,
		   new String[] {
			   "kind", "elementWildcard", //$NON-NLS-1$ //$NON-NLS-2$
			   "wildcards", "http://www.eclipse.org/emf/2003/XMLType" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (parameterExpressionEClass,
		   source,
		   new String[] {
			   "kind", "mixed" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (getParameterExpression_Mixed(),
		   source,
		   new String[] {
			   "name", ":mixed", //$NON-NLS-1$ //$NON-NLS-2$
			   "kind", "elementWildcard" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (getParameterExpression_Expressions(),
		   source,
		   new String[] {
			   "kind", "element" //$NON-NLS-1$ //$NON-NLS-2$
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
		  (applicationEClass,
		   source,
		   new String[] {
			   "documentation", "Represents an Application model object" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (getApplication_Components(),
		   source,
		   new String[] {
			   "documentation", "Aggregates all Components with this Application..." //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (getApplication_MixedOuterContent(),
		   source,
		   new String[] {
			   "suppressedGetVisibility", "true" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (componentEClass,
		   source,
		   new String[] {
			   "documentation", "Represents a Component model object" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (getComponent_Type(),
		   source,
		   new String[] {
			   "documentation", "Reference to the type of this Component" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (getComponent_ParameterValues(),
		   source,
		   new String[] {
			   "documentation", "Aggregates all ParameterValues with this Component..." //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (connectionEClass,
		   source,
		   new String[] {
			   "documentation", "Represents a Connection model object" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (getConnection_SourceComponent(),
		   source,
		   new String[] {
			   "documentation", "Specifies the source component" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (getConnection_SourcePort(),
		   source,
		   new String[] {
			   "documentation", "Specifies the source port" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (getConnection_TargetComponent(),
		   source,
		   new String[] {
			   "documentation", "Specifies the target component" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (parameterValueEClass,
		   source,
		   new String[] {
			   "documentation", "Represents a Parameter Value model object" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (getParameterValue_Type(),
		   source,
		   new String[] {
			   "documentation", "Reference to the type of this Parameter" //$NON-NLS-1$ //$NON-NLS-2$
		   });
		addAnnotation
		  (getParameterValue_Value(),
		   source,
		   new String[] {
			   "documentation", "Specifies the value of this ParameterValue" //$NON-NLS-1$ //$NON-NLS-2$
		   });
	}

} // InstanceModel20PackageImpl

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
package org.eclipse.sphinx.examples.hummingbird10;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc --> The <b>Package</b> for the model. It contains accessors for the meta objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 *
 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Factory
 * @model kind="package"
 * @generated
 */
public interface Hummingbird10Package extends EPackage {
	/**
	 * The package name. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	String eNAME = "hummingbird10"; //$NON-NLS-1$

	/**
	 * The package namespace URI. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/sphinx/examples/hummingbird/1.0.0"; //$NON-NLS-1$

	/**
	 * The package namespace name. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	String eNS_PREFIX = "hb"; //$NON-NLS-1$

	/**
	 * The package content type ID. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	String eCONTENT_TYPE = "org.eclipse.sphinx.examples.hummingbird10.hummingbird10XMIFile"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	Hummingbird10Package eINSTANCE = org.eclipse.sphinx.examples.hummingbird10.impl.Hummingbird10PackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird10.impl.ComponentImpl
	 * <em>Component</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @see org.eclipse.sphinx.examples.hummingbird10.impl.ComponentImpl
	 * @see org.eclipse.sphinx.examples.hummingbird10.impl.Hummingbird10PackageImpl#getComponent()
	 * @generated
	 */
	int COMPONENT = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int COMPONENT__NAME = 0;

	/**
	 * The feature id for the '<em><b>Outgoing Connections</b></em>' containment reference list. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int COMPONENT__OUTGOING_CONNECTIONS = 1;

	/**
	 * The feature id for the '<em><b>Provided Interfaces</b></em>' reference list. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int COMPONENT__PROVIDED_INTERFACES = 2;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int COMPONENT__PARAMETERS = 3;

	/**
	 * The feature id for the '<em><b>Incoming Connections</b></em>' reference list. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int COMPONENT__INCOMING_CONNECTIONS = 4;

	/**
	 * The number of structural features of the '<em>Component</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 *
	 * @generated
	 * @ordered
	 */
	int COMPONENT_FEATURE_COUNT = 5;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird10.impl.ApplicationImpl
	 * <em>Application</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @see org.eclipse.sphinx.examples.hummingbird10.impl.ApplicationImpl
	 * @see org.eclipse.sphinx.examples.hummingbird10.impl.Hummingbird10PackageImpl#getApplication()
	 * @generated
	 */
	int APPLICATION = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int APPLICATION__NAME = 0;

	/**
	 * The feature id for the '<em><b>Components</b></em>' containment reference list. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int APPLICATION__COMPONENTS = 1;

	/**
	 * The feature id for the '<em><b>Interfaces</b></em>' containment reference list. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int APPLICATION__INTERFACES = 2;

	/**
	 * The number of structural features of the '<em>Application</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 *
	 * @generated
	 * @ordered
	 */
	int APPLICATION_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird10.impl.ConnectionImpl
	 * <em>Connection</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @see org.eclipse.sphinx.examples.hummingbird10.impl.ConnectionImpl
	 * @see org.eclipse.sphinx.examples.hummingbird10.impl.Hummingbird10PackageImpl#getConnection()
	 * @generated
	 */
	int CONNECTION = 2;

	/**
	 * The feature id for the '<em><b>Required Interface</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 *
	 * @generated
	 * @ordered
	 */
	int CONNECTION__REQUIRED_INTERFACE = 0;

	/**
	 * The feature id for the '<em><b>Target Component</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 *
	 * @generated
	 * @ordered
	 */
	int CONNECTION__TARGET_COMPONENT = 1;

	/**
	 * The feature id for the '<em><b>Source Component</b></em>' container reference. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int CONNECTION__SOURCE_COMPONENT = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int CONNECTION__NAME = 3;

	/**
	 * The number of structural features of the '<em>Connection</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 *
	 * @generated
	 * @ordered
	 */
	int CONNECTION_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird10.impl.InterfaceImpl
	 * <em>Interface</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @see org.eclipse.sphinx.examples.hummingbird10.impl.InterfaceImpl
	 * @see org.eclipse.sphinx.examples.hummingbird10.impl.Hummingbird10PackageImpl#getInterface()
	 * @generated
	 */
	int INTERFACE = 3;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int INTERFACE__NAME = 0;

	/**
	 * The feature id for the '<em><b>Providing Components</b></em>' reference list. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int INTERFACE__PROVIDING_COMPONENTS = 1;

	/**
	 * The number of structural features of the '<em>Interface</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 *
	 * @generated
	 * @ordered
	 */
	int INTERFACE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird10.impl.ParameterImpl
	 * <em>Parameter</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @see org.eclipse.sphinx.examples.hummingbird10.impl.ParameterImpl
	 * @see org.eclipse.sphinx.examples.hummingbird10.impl.Hummingbird10PackageImpl#getParameter()
	 * @generated
	 */
	int PARAMETER = 4;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int PARAMETER__NAME = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int PARAMETER__VALUE = 1;

	/**
	 * The number of structural features of the '<em>Parameter</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 *
	 * @generated
	 * @ordered
	 */
	int PARAMETER_FEATURE_COUNT = 2;

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird10.Component <em>Component</em>}
	 * '. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for class '<em>Component</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Component
	 * @generated
	 */
	EClass getComponent();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.examples.hummingbird10.Component#getName
	 * <em>Name</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Component#getName()
	 * @see #getComponent()
	 * @generated
	 */
	EAttribute getComponent_Name();

	/**
	 * Returns the meta object for the containment reference list '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Component#getOutgoingConnections <em>Outgoing Connections</em>}
	 * '. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the containment reference list '<em>Outgoing Connections</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Component#getOutgoingConnections()
	 * @see #getComponent()
	 * @generated
	 */
	EReference getComponent_OutgoingConnections();

	/**
	 * Returns the meta object for the reference list '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Component#getProvidedInterfaces <em>Provided Interfaces</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the reference list '<em>Provided Interfaces</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Component#getProvidedInterfaces()
	 * @see #getComponent()
	 * @generated
	 */
	EReference getComponent_ProvidedInterfaces();

	/**
	 * Returns the meta object for the containment reference list '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Component#getParameters <em>Parameters</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the containment reference list '<em>Parameters</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Component#getParameters()
	 * @see #getComponent()
	 * @generated
	 */
	EReference getComponent_Parameters();

	/**
	 * Returns the meta object for the reference list '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Component#getIncomingConnections <em>Incoming Connections</em>}
	 * '. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the reference list '<em>Incoming Connections</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Component#getIncomingConnections()
	 * @see #getComponent()
	 * @generated
	 */
	EReference getComponent_IncomingConnections();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird10.Application
	 * <em>Application</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for class '<em>Application</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Application
	 * @generated
	 */
	EClass getApplication();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.examples.hummingbird10.Application#getName
	 * <em>Name</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Application#getName()
	 * @see #getApplication()
	 * @generated
	 */
	EAttribute getApplication_Name();

	/**
	 * Returns the meta object for the containment reference list '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Application#getComponents <em>Components</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the containment reference list '<em>Components</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Application#getComponents()
	 * @see #getApplication()
	 * @generated
	 */
	EReference getApplication_Components();

	/**
	 * Returns the meta object for the containment reference list '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Application#getInterfaces <em>Interfaces</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the containment reference list '<em>Interfaces</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Application#getInterfaces()
	 * @see #getApplication()
	 * @generated
	 */
	EReference getApplication_Interfaces();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird10.Connection
	 * <em>Connection</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for class '<em>Connection</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Connection
	 * @generated
	 */
	EClass getConnection();

	/**
	 * Returns the meta object for the reference '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Connection#getRequiredInterface <em>Required Interface</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the reference '<em>Required Interface</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Connection#getRequiredInterface()
	 * @see #getConnection()
	 * @generated
	 */
	EReference getConnection_RequiredInterface();

	/**
	 * Returns the meta object for the reference '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Connection#getTargetComponent <em>Target Component</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the reference '<em>Target Component</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Connection#getTargetComponent()
	 * @see #getConnection()
	 * @generated
	 */
	EReference getConnection_TargetComponent();

	/**
	 * Returns the meta object for the container reference '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Connection#getSourceComponent <em>Source Component</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the container reference '<em>Source Component</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Connection#getSourceComponent()
	 * @see #getConnection()
	 * @generated
	 */
	EReference getConnection_SourceComponent();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.examples.hummingbird10.Connection#getName
	 * <em>Name</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Connection#getName()
	 * @see #getConnection()
	 * @generated
	 */
	EAttribute getConnection_Name();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird10.Interface <em>Interface</em>}
	 * '. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for class '<em>Interface</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Interface
	 * @generated
	 */
	EClass getInterface();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.examples.hummingbird10.Interface#getName
	 * <em>Name</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Interface#getName()
	 * @see #getInterface()
	 * @generated
	 */
	EAttribute getInterface_Name();

	/**
	 * Returns the meta object for the reference list '
	 * {@link org.eclipse.sphinx.examples.hummingbird10.Interface#getProvidingComponents <em>Providing Components</em>}
	 * '. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the reference list '<em>Providing Components</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Interface#getProvidingComponents()
	 * @see #getInterface()
	 * @generated
	 */
	EReference getInterface_ProvidingComponents();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird10.Parameter <em>Parameter</em>}
	 * '. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for class '<em>Parameter</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Parameter
	 * @generated
	 */
	EClass getParameter();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.examples.hummingbird10.Parameter#getName
	 * <em>Name</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Parameter#getName()
	 * @see #getParameter()
	 * @generated
	 */
	EAttribute getParameter_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.examples.hummingbird10.Parameter#getValue
	 * <em>Value</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird10.Parameter#getValue()
	 * @see #getParameter()
	 * @generated
	 */
	EAttribute getParameter_Value();

	/**
	 * Returns the factory that creates the instances of the model. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	Hummingbird10Factory getHummingbird10Factory();

	/**
	 * <!-- begin-user-doc --> Defines literals for the meta objects that represent
	 * <ul>
	 * <li>each class,</li>
	 * <li>each feature of each class,</li>
	 * <li>each enum,</li>
	 * <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 *
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird10.impl.ComponentImpl
		 * <em>Component</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
		 *
		 * @see org.eclipse.sphinx.examples.hummingbird10.impl.ComponentImpl
		 * @see org.eclipse.sphinx.examples.hummingbird10.impl.Hummingbird10PackageImpl#getComponent()
		 * @generated
		 */
		EClass COMPONENT = eINSTANCE.getComponent();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 *
		 * @generated
		 */
		EAttribute COMPONENT__NAME = eINSTANCE.getComponent_Name();

		/**
		 * The meta object literal for the '<em><b>Outgoing Connections</b></em>' containment reference list feature.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 *
		 * @generated
		 */
		EReference COMPONENT__OUTGOING_CONNECTIONS = eINSTANCE.getComponent_OutgoingConnections();

		/**
		 * The meta object literal for the '<em><b>Provided Interfaces</b></em>' reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 *
		 * @generated
		 */
		EReference COMPONENT__PROVIDED_INTERFACES = eINSTANCE.getComponent_ProvidedInterfaces();

		/**
		 * The meta object literal for the '<em><b>Parameters</b></em>' containment reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 *
		 * @generated
		 */
		EReference COMPONENT__PARAMETERS = eINSTANCE.getComponent_Parameters();

		/**
		 * The meta object literal for the '<em><b>Incoming Connections</b></em>' reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 *
		 * @generated
		 */
		EReference COMPONENT__INCOMING_CONNECTIONS = eINSTANCE.getComponent_IncomingConnections();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird10.impl.ApplicationImpl
		 * <em>Application</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
		 *
		 * @see org.eclipse.sphinx.examples.hummingbird10.impl.ApplicationImpl
		 * @see org.eclipse.sphinx.examples.hummingbird10.impl.Hummingbird10PackageImpl#getApplication()
		 * @generated
		 */
		EClass APPLICATION = eINSTANCE.getApplication();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 *
		 * @generated
		 */
		EAttribute APPLICATION__NAME = eINSTANCE.getApplication_Name();

		/**
		 * The meta object literal for the '<em><b>Components</b></em>' containment reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 *
		 * @generated
		 */
		EReference APPLICATION__COMPONENTS = eINSTANCE.getApplication_Components();

		/**
		 * The meta object literal for the '<em><b>Interfaces</b></em>' containment reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 *
		 * @generated
		 */
		EReference APPLICATION__INTERFACES = eINSTANCE.getApplication_Interfaces();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird10.impl.ConnectionImpl
		 * <em>Connection</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
		 *
		 * @see org.eclipse.sphinx.examples.hummingbird10.impl.ConnectionImpl
		 * @see org.eclipse.sphinx.examples.hummingbird10.impl.Hummingbird10PackageImpl#getConnection()
		 * @generated
		 */
		EClass CONNECTION = eINSTANCE.getConnection();

		/**
		 * The meta object literal for the '<em><b>Required Interface</b></em>' reference feature. <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 *
		 * @generated
		 */
		EReference CONNECTION__REQUIRED_INTERFACE = eINSTANCE.getConnection_RequiredInterface();

		/**
		 * The meta object literal for the '<em><b>Target Component</b></em>' reference feature. <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 *
		 * @generated
		 */
		EReference CONNECTION__TARGET_COMPONENT = eINSTANCE.getConnection_TargetComponent();

		/**
		 * The meta object literal for the '<em><b>Source Component</b></em>' container reference feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 *
		 * @generated
		 */
		EReference CONNECTION__SOURCE_COMPONENT = eINSTANCE.getConnection_SourceComponent();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 *
		 * @generated
		 */
		EAttribute CONNECTION__NAME = eINSTANCE.getConnection_Name();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird10.impl.InterfaceImpl
		 * <em>Interface</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
		 *
		 * @see org.eclipse.sphinx.examples.hummingbird10.impl.InterfaceImpl
		 * @see org.eclipse.sphinx.examples.hummingbird10.impl.Hummingbird10PackageImpl#getInterface()
		 * @generated
		 */
		EClass INTERFACE = eINSTANCE.getInterface();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 *
		 * @generated
		 */
		EAttribute INTERFACE__NAME = eINSTANCE.getInterface_Name();

		/**
		 * The meta object literal for the '<em><b>Providing Components</b></em>' reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 *
		 * @generated
		 */
		EReference INTERFACE__PROVIDING_COMPONENTS = eINSTANCE.getInterface_ProvidingComponents();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird10.impl.ParameterImpl
		 * <em>Parameter</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
		 *
		 * @see org.eclipse.sphinx.examples.hummingbird10.impl.ParameterImpl
		 * @see org.eclipse.sphinx.examples.hummingbird10.impl.Hummingbird10PackageImpl#getParameter()
		 * @generated
		 */
		EClass PARAMETER = eINSTANCE.getParameter();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 *
		 * @generated
		 */
		EAttribute PARAMETER__NAME = eINSTANCE.getParameter_Name();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 *
		 * @generated
		 */
		EAttribute PARAMETER__VALUE = eINSTANCE.getParameter_Value();

	}

} // Hummingbird10Package

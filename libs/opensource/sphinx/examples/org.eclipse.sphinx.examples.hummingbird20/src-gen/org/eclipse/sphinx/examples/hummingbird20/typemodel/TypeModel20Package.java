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
package org.eclipse.sphinx.examples.hummingbird20.typemodel;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Package;

/**
 * <!-- begin-user-doc --> The <b>Package</b> for the model. It contains accessors for the meta objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory
 * @model kind="package"
 * @generated
 */
public interface TypeModel20Package extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "typemodel"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/sphinx/examples/hummingbird/2.0.1/typemodel"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "tm"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	TypeModel20Package eINSTANCE = org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.TypeModel20PackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PlatformImpl <em>Platform</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PlatformImpl
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.TypeModel20PackageImpl#getPlatform()
	 * @generated
	 */
	int PLATFORM = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLATFORM__NAME = Common20Package.IDENTIFIABLE__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLATFORM__DESCRIPTION = Common20Package.IDENTIFIABLE__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Component Types</b></em>' containment reference list.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLATFORM__COMPONENT_TYPES = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Interfaces</b></em>' containment reference list.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLATFORM__INTERFACES = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Mixed</b></em>' attribute list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLATFORM__MIXED = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>XSI Schema Location</b></em>' map.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLATFORM__XSI_SCHEMA_LOCATION = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Platform</em>' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLATFORM_FEATURE_COUNT = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.ComponentTypeImpl <em>Component Type</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.ComponentTypeImpl
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.TypeModel20PackageImpl#getComponentType()
	 * @generated
	 */
	int COMPONENT_TYPE = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPONENT_TYPE__NAME = Common20Package.IDENTIFIABLE__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPONENT_TYPE__DESCRIPTION = Common20Package.IDENTIFIABLE__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Provided Interfaces</b></em>' reference list.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPONENT_TYPE__PROVIDED_INTERFACES = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Ports</b></em>' containment reference list.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPONENT_TYPE__PORTS = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPONENT_TYPE__PARAMETERS = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Component Type</em>' class.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPONENT_TYPE_FEATURE_COUNT = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PortImpl <em>Port</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PortImpl
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.TypeModel20PackageImpl#getPort()
	 * @generated
	 */
	int PORT = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PORT__NAME = Common20Package.IDENTIFIABLE__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PORT__DESCRIPTION = Common20Package.IDENTIFIABLE__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' container reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PORT__OWNER = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Required Interface</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int PORT__REQUIRED_INTERFACE = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Min Provider Count</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int PORT__MIN_PROVIDER_COUNT = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Max Provider Count</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int PORT__MAX_PROVIDER_COUNT = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Port</em>' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PORT_FEATURE_COUNT = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.InterfaceImpl <em>Interface</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.InterfaceImpl
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.TypeModel20PackageImpl#getInterface()
	 * @generated
	 */
	int INTERFACE = 3;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERFACE__NAME = Common20Package.IDENTIFIABLE__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERFACE__DESCRIPTION = Common20Package.IDENTIFIABLE__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Providing Component Types</b></em>' reference list.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERFACE__PROVIDING_COMPONENT_TYPES = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Requiring Ports</b></em>' reference list.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERFACE__REQUIRING_PORTS = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Interface</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int INTERFACE_FEATURE_COUNT = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.ParameterImpl <em>Parameter</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.ParameterImpl
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.TypeModel20PackageImpl#getParameter()
	 * @generated
	 */
	int PARAMETER = 4;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER__NAME = Common20Package.IDENTIFIABLE__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER__DESCRIPTION = Common20Package.IDENTIFIABLE__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Data Type</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER__DATA_TYPE = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Optional</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER__OPTIONAL = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Parameter</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int PARAMETER_FEATURE_COUNT = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform <em>Platform</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Platform</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform
	 * @generated
	 */
	EClass getPlatform();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform#getComponentTypes <em>Component Types</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Component Types</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform#getComponentTypes()
	 * @see #getPlatform()
	 * @generated
	 */
	EReference getPlatform_ComponentTypes();

	/**
	 * Returns the meta object for the containment reference list '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform#getInterfaces <em>Interfaces</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the containment reference list '<em>Interfaces</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform#getInterfaces()
	 * @see #getPlatform()
	 * @generated
	 */
	EReference getPlatform_Interfaces();

	/**
	 * Returns the meta object for the attribute list '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform#getMixed <em>Mixed</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute list '<em>Mixed</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform#getMixed()
	 * @see #getPlatform()
	 * @generated
	 */
	EAttribute getPlatform_Mixed();

	/**
	 * Returns the meta object for the map '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform#getXSISchemaLocation <em>XSI Schema Location</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the map '<em>XSI Schema Location</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform#getXSISchemaLocation()
	 * @see #getPlatform()
	 * @generated
	 */
	EReference getPlatform_XSISchemaLocation();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType <em>Component Type</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Component Type</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType
	 * @generated
	 */
	EClass getComponentType();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType#getProvidedInterfaces <em>Provided Interfaces</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Provided Interfaces</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType#getProvidedInterfaces()
	 * @see #getComponentType()
	 * @generated
	 */
	EReference getComponentType_ProvidedInterfaces();

	/**
	 * Returns the meta object for the containment reference list '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType#getPorts <em>Ports</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the containment reference list '<em>Ports</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType#getPorts()
	 * @see #getComponentType()
	 * @generated
	 */
	EReference getComponentType_Ports();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType#getParameters <em>Parameters</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Parameters</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType#getParameters()
	 * @see #getComponentType()
	 * @generated
	 */
	EReference getComponentType_Parameters();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port <em>Port</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Port</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Port
	 * @generated
	 */
	EClass getPort();

	/**
	 * Returns the meta object for the container reference '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getOwner <em>Owner</em>}'.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Owner</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getOwner()
	 * @see #getPort()
	 * @generated
	 */
	EReference getPort_Owner();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getRequiredInterface <em>Required Interface</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Required Interface</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getRequiredInterface()
	 * @see #getPort()
	 * @generated
	 */
	EReference getPort_RequiredInterface();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getMinProviderCount <em>Min Provider Count</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Min Provider Count</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getMinProviderCount()
	 * @see #getPort()
	 * @generated
	 */
	EAttribute getPort_MinProviderCount();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getMaxProviderCount <em>Max Provider Count</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max Provider Count</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Port#getMaxProviderCount()
	 * @see #getPort()
	 * @generated
	 */
	EAttribute getPort_MaxProviderCount();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface <em>Interface</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Interface</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface
	 * @generated
	 */
	EClass getInterface();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface#getProvidingComponentTypes <em>Providing Component Types</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Providing Component Types</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface#getProvidingComponentTypes()
	 * @see #getInterface()
	 * @generated
	 */
	EReference getInterface_ProvidingComponentTypes();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface#getRequiringPorts <em>Requiring Ports</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Requiring Ports</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface#getRequiringPorts()
	 * @see #getInterface()
	 * @generated
	 */
	EReference getInterface_RequiringPorts();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter <em>Parameter</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Parameter</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter
	 * @generated
	 */
	EClass getParameter();

	/**
	 * Returns the meta object for the attribute '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter#getDataType <em>Data Type</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Data Type</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter#getDataType()
	 * @see #getParameter()
	 * @generated
	 */
	EAttribute getParameter_DataType();

	/**
	 * Returns the meta object for the attribute '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter#isOptional <em>Optional</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Optional</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter#isOptional()
	 * @see #getParameter()
	 * @generated
	 */
	EAttribute getParameter_Optional();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	TypeModel20Factory getTypeModel20Factory();

	/**
	 * <!-- begin-user-doc --> Defines literals for the meta objects that represent
	 * <ul>
	 * <li>each class,</li>
	 * <li>each feature of each class,</li>
	 * <li>each enum,</li>
	 * <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '
		 * {@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PlatformImpl <em>Platform</em>}' class. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PlatformImpl
		 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.TypeModel20PackageImpl#getPlatform()
		 * @generated
		 */
		EClass PLATFORM = eINSTANCE.getPlatform();

		/**
		 * The meta object literal for the '<em><b>Component Types</b></em>' containment reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference PLATFORM__COMPONENT_TYPES = eINSTANCE.getPlatform_ComponentTypes();

		/**
		 * The meta object literal for the '<em><b>Interfaces</b></em>' containment reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference PLATFORM__INTERFACES = eINSTANCE.getPlatform_Interfaces();

		/**
		 * The meta object literal for the '<em><b>Mixed</b></em>' attribute list feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute PLATFORM__MIXED = eINSTANCE.getPlatform_Mixed();

		/**
		 * The meta object literal for the '<em><b>XSI Schema Location</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PLATFORM__XSI_SCHEMA_LOCATION = eINSTANCE.getPlatform_XSISchemaLocation();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.ComponentTypeImpl <em>Component Type</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.ComponentTypeImpl
		 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.TypeModel20PackageImpl#getComponentType()
		 * @generated
		 */
		EClass COMPONENT_TYPE = eINSTANCE.getComponentType();

		/**
		 * The meta object literal for the '<em><b>Provided Interfaces</b></em>' reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference COMPONENT_TYPE__PROVIDED_INTERFACES = eINSTANCE.getComponentType_ProvidedInterfaces();

		/**
		 * The meta object literal for the '<em><b>Ports</b></em>' containment reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference COMPONENT_TYPE__PORTS = eINSTANCE.getComponentType_Ports();

		/**
		 * The meta object literal for the '<em><b>Parameters</b></em>' containment reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference COMPONENT_TYPE__PARAMETERS = eINSTANCE.getComponentType_Parameters();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PortImpl <em>Port</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PortImpl
		 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.TypeModel20PackageImpl#getPort()
		 * @generated
		 */
		EClass PORT = eINSTANCE.getPort();

		/**
		 * The meta object literal for the '<em><b>Owner</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PORT__OWNER = eINSTANCE.getPort_Owner();

		/**
		 * The meta object literal for the '<em><b>Required Interface</b></em>' reference feature.
		 * <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * @generated
		 */
		EReference PORT__REQUIRED_INTERFACE = eINSTANCE.getPort_RequiredInterface();

		/**
		 * The meta object literal for the '<em><b>Min Provider Count</b></em>' attribute feature.
		 * <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PORT__MIN_PROVIDER_COUNT = eINSTANCE.getPort_MinProviderCount();

		/**
		 * The meta object literal for the '<em><b>Max Provider Count</b></em>' attribute feature.
		 * <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PORT__MAX_PROVIDER_COUNT = eINSTANCE.getPort_MaxProviderCount();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.InterfaceImpl <em>Interface</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.InterfaceImpl
		 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.TypeModel20PackageImpl#getInterface()
		 * @generated
		 */
		EClass INTERFACE = eINSTANCE.getInterface();

		/**
		 * The meta object literal for the '<em><b>Providing Component Types</b></em>' reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference INTERFACE__PROVIDING_COMPONENT_TYPES = eINSTANCE.getInterface_ProvidingComponentTypes();

		/**
		 * The meta object literal for the '<em><b>Requiring Ports</b></em>' reference list feature.
		 * <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * @generated
		 */
		EReference INTERFACE__REQUIRING_PORTS = eINSTANCE.getInterface_RequiringPorts();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.ParameterImpl <em>Parameter</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.ParameterImpl
		 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.TypeModel20PackageImpl#getParameter()
		 * @generated
		 */
		EClass PARAMETER = eINSTANCE.getParameter();

		/**
		 * The meta object literal for the '<em><b>Data Type</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER__DATA_TYPE = eINSTANCE.getParameter_DataType();

		/**
		 * The meta object literal for the '<em><b>Optional</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER__OPTIONAL = eINSTANCE.getParameter_Optional();

	}

} // TypeModel20Package

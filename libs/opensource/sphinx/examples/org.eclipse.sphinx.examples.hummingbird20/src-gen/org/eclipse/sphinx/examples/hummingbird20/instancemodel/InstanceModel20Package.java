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
package org.eclipse.sphinx.examples.hummingbird20.instancemodel;

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
 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory
 * @model kind="package"
 * @generated
 */
public interface InstanceModel20Package extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "instancemodel"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/sphinx/examples/hummingbird/2.0.1/instancemodel"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "im"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	InstanceModel20Package eINSTANCE = org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ApplicationImpl <em>Application</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ApplicationImpl
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl#getApplication()
	 * @generated
	 */
	int APPLICATION = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__NAME = Common20Package.IDENTIFIABLE__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__DESCRIPTION = Common20Package.IDENTIFIABLE__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Components</b></em>' containment reference list.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__COMPONENTS = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Mixed</b></em>' attribute list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__MIXED = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>XSI Schema Location</b></em>' map.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__XSI_SCHEMA_LOCATION = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Mixed Outer Content</b></em>' attribute list.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__MIXED_OUTER_CONTENT = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Application</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int APPLICATION_FEATURE_COUNT = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ComponentImpl <em>Component</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ComponentImpl
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl#getComponent()
	 * @generated
	 */
	int COMPONENT = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPONENT__NAME = Common20Package.IDENTIFIABLE__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPONENT__DESCRIPTION = Common20Package.IDENTIFIABLE__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Type</b></em>' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPONENT__TYPE = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Outgoing Connections</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPONENT__OUTGOING_CONNECTIONS = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Incoming Connections</b></em>' reference list.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPONENT__INCOMING_CONNECTIONS = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Parameter Values</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPONENT__PARAMETER_VALUES = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Parameter Expressions</b></em>' containment reference list.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPONENT__PARAMETER_EXPRESSIONS = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>Component</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int COMPONENT_FEATURE_COUNT = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ConnectionImpl <em>Connection</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ConnectionImpl
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl#getConnection()
	 * @generated
	 */
	int CONNECTION = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONNECTION__NAME = Common20Package.IDENTIFIABLE__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONNECTION__DESCRIPTION = Common20Package.IDENTIFIABLE__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Source Component</b></em>' container reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONNECTION__SOURCE_COMPONENT = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Source Port</b></em>' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONNECTION__SOURCE_PORT = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Target Component</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int CONNECTION__TARGET_COMPONENT = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Connection</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int CONNECTION_FEATURE_COUNT = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ParameterValueImpl <em>Parameter Value</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ParameterValueImpl
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl#getParameterValue()
	 * @generated
	 */
	int PARAMETER_VALUE = 3;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_VALUE__NAME = Common20Package.IDENTIFIABLE__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_VALUE__DESCRIPTION = Common20Package.IDENTIFIABLE__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Type</b></em>' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_VALUE__TYPE = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_VALUE__VALUE = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Parameter Value</em>' class.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_VALUE_FEATURE_COUNT = Common20Package.IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ParameterExpressionImpl <em>Parameter Expression</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ParameterExpressionImpl
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl#getParameterExpression()
	 * @generated
	 */
	int PARAMETER_EXPRESSION = 4;

	/**
	 * The feature id for the '<em><b>Mixed</b></em>' attribute list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_EXPRESSION__MIXED = 0;

	/**
	 * The feature id for the '<em><b>Expressions</b></em>' containment reference list.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_EXPRESSION__EXPRESSIONS = 1;

	/**
	 * The number of structural features of the '<em>Parameter Expression</em>' class.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_EXPRESSION_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.FormulaImpl <em>Formula</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.FormulaImpl
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl#getFormula()
	 * @generated
	 */
	int FORMULA = 5;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORMULA__VALUE = 0;

	/**
	 * The number of structural features of the '<em>Formula</em>' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORMULA_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.CustomApplicationImpl <em>Custom Application</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.CustomApplicationImpl
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl#getCustomApplication()
	 * @generated
	 */
	int CUSTOM_APPLICATION = 6;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_APPLICATION__NAME = APPLICATION__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_APPLICATION__DESCRIPTION = APPLICATION__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Components</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_APPLICATION__COMPONENTS = APPLICATION__COMPONENTS;

	/**
	 * The feature id for the '<em><b>Mixed</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_APPLICATION__MIXED = APPLICATION__MIXED;

	/**
	 * The feature id for the '<em><b>XSI Schema Location</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_APPLICATION__XSI_SCHEMA_LOCATION = APPLICATION__XSI_SCHEMA_LOCATION;

	/**
	 * The feature id for the '<em><b>Mixed Outer Content</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_APPLICATION__MIXED_OUTER_CONTENT = APPLICATION__MIXED_OUTER_CONTENT;

	/**
	 * The number of structural features of the '<em>Custom Application</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CUSTOM_APPLICATION_FEATURE_COUNT = APPLICATION_FEATURE_COUNT + 0;

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application <em>Application</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Application</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application
	 * @generated
	 */
	EClass getApplication();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application#getComponents <em>Components</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Components</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application#getComponents()
	 * @see #getApplication()
	 * @generated
	 */
	EReference getApplication_Components();

	/**
	 * Returns the meta object for the attribute list '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application#getMixed <em>Mixed</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute list '<em>Mixed</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application#getMixed()
	 * @see #getApplication()
	 * @generated
	 */
	EAttribute getApplication_Mixed();

	/**
	 * Returns the meta object for the map '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application#getXSISchemaLocation <em>XSI Schema Location</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the map '<em>XSI Schema Location</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application#getXSISchemaLocation()
	 * @see #getApplication()
	 * @generated
	 */
	EReference getApplication_XSISchemaLocation();

	/**
	 * Returns the meta object for the attribute list '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application <em>Mixed Outer Content</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute list '<em>Mixed Outer Content</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application
	 * @see #getApplication()
	 * @generated
	 */
	EAttribute getApplication_MixedOuterContent();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component <em>Component</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Component</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component
	 * @generated
	 */
	EClass getComponent();

	/**
	 * Returns the meta object for the reference '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getType <em>Type</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the reference '<em>Type</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getType()
	 * @see #getComponent()
	 * @generated
	 */
	EReference getComponent_Type();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getOutgoingConnections <em>Outgoing Connections</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Outgoing Connections</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getOutgoingConnections()
	 * @see #getComponent()
	 * @generated
	 */
	EReference getComponent_OutgoingConnections();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getIncomingConnections <em>Incoming Connections</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Incoming Connections</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getIncomingConnections()
	 * @see #getComponent()
	 * @generated
	 */
	EReference getComponent_IncomingConnections();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getParameterValues <em>Parameter Values</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Parameter Values</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getParameterValues()
	 * @see #getComponent()
	 * @generated
	 */
	EReference getComponent_ParameterValues();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getParameterExpressions <em>Parameter Expressions</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Parameter Expressions</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component#getParameterExpressions()
	 * @see #getComponent()
	 * @generated
	 */
	EReference getComponent_ParameterExpressions();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection <em>Connection</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Connection</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection
	 * @generated
	 */
	EClass getConnection();

	/**
	 * Returns the meta object for the container reference '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getSourceComponent <em>Source Component</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Source Component</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getSourceComponent()
	 * @see #getConnection()
	 * @generated
	 */
	EReference getConnection_SourceComponent();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getSourcePort <em>Source Port</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Source Port</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getSourcePort()
	 * @see #getConnection()
	 * @generated
	 */
	EReference getConnection_SourcePort();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getTargetComponent <em>Target Component</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Target Component</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection#getTargetComponent()
	 * @see #getConnection()
	 * @generated
	 */
	EReference getConnection_TargetComponent();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue <em>Parameter Value</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Parameter Value</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue
	 * @generated
	 */
	EClass getParameterValue();

	/**
	 * Returns the meta object for the reference '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue#getType <em>Type</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the reference '<em>Type</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue#getType()
	 * @see #getParameterValue()
	 * @generated
	 */
	EReference getParameterValue_Type();

	/**
	 * Returns the meta object for the attribute '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue#getValue <em>Value</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue#getValue()
	 * @see #getParameterValue()
	 * @generated
	 */
	EAttribute getParameterValue_Value();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression <em>Parameter Expression</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Parameter Expression</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression
	 * @generated
	 */
	EClass getParameterExpression();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression#getMixed <em>Mixed</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Mixed</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression#getMixed()
	 * @see #getParameterExpression()
	 * @generated
	 */
	EAttribute getParameterExpression_Mixed();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression#getExpressions <em>Expressions</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Expressions</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression#getExpressions()
	 * @see #getParameterExpression()
	 * @generated
	 */
	EReference getParameterExpression_Expressions();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Formula <em>Formula</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Formula</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Formula
	 * @generated
	 */
	EClass getFormula();

	/**
	 * Returns the meta object for the attribute '
	 * {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Formula#getValue <em>Value</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.Formula#getValue()
	 * @see #getFormula()
	 * @generated
	 */
	EAttribute getFormula_Value();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.CustomApplication <em>Custom Application</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Custom Application</em>'.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.CustomApplication
	 * @generated
	 */
	EClass getCustomApplication();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	InstanceModel20Factory getInstanceModel20Factory();

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
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ApplicationImpl <em>Application</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ApplicationImpl
		 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl#getApplication()
		 * @generated
		 */
		EClass APPLICATION = eINSTANCE.getApplication();

		/**
		 * The meta object literal for the '<em><b>Components</b></em>' containment reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference APPLICATION__COMPONENTS = eINSTANCE.getApplication_Components();

		/**
		 * The meta object literal for the '<em><b>Mixed</b></em>' attribute list feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute APPLICATION__MIXED = eINSTANCE.getApplication_Mixed();

		/**
		 * The meta object literal for the '<em><b>XSI Schema Location</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference APPLICATION__XSI_SCHEMA_LOCATION = eINSTANCE.getApplication_XSISchemaLocation();

		/**
		 * The meta object literal for the '<em><b>Mixed Outer Content</b></em>' attribute list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute APPLICATION__MIXED_OUTER_CONTENT = eINSTANCE.getApplication_MixedOuterContent();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ComponentImpl <em>Component</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ComponentImpl
		 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl#getComponent()
		 * @generated
		 */
		EClass COMPONENT = eINSTANCE.getComponent();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' reference feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EReference COMPONENT__TYPE = eINSTANCE.getComponent_Type();

		/**
		 * The meta object literal for the '<em><b>Outgoing Connections</b></em>' containment reference list feature.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @generated
		 */
		EReference COMPONENT__OUTGOING_CONNECTIONS = eINSTANCE.getComponent_OutgoingConnections();

		/**
		 * The meta object literal for the '<em><b>Incoming Connections</b></em>' reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference COMPONENT__INCOMING_CONNECTIONS = eINSTANCE.getComponent_IncomingConnections();

		/**
		 * The meta object literal for the '<em><b>Parameter Values</b></em>' containment reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference COMPONENT__PARAMETER_VALUES = eINSTANCE.getComponent_ParameterValues();

		/**
		 * The meta object literal for the '<em><b>Parameter Expressions</b></em>' containment reference list feature.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @generated
		 */
		EReference COMPONENT__PARAMETER_EXPRESSIONS = eINSTANCE.getComponent_ParameterExpressions();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ConnectionImpl <em>Connection</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ConnectionImpl
		 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl#getConnection()
		 * @generated
		 */
		EClass CONNECTION = eINSTANCE.getConnection();

		/**
		 * The meta object literal for the '<em><b>Source Component</b></em>' container reference feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference CONNECTION__SOURCE_COMPONENT = eINSTANCE.getConnection_SourceComponent();

		/**
		 * The meta object literal for the '<em><b>Source Port</b></em>' reference feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EReference CONNECTION__SOURCE_PORT = eINSTANCE.getConnection_SourcePort();

		/**
		 * The meta object literal for the '<em><b>Target Component</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CONNECTION__TARGET_COMPONENT = eINSTANCE.getConnection_TargetComponent();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ParameterValueImpl <em>Parameter Value</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ParameterValueImpl
		 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl#getParameterValue()
		 * @generated
		 */
		EClass PARAMETER_VALUE = eINSTANCE.getParameterValue();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' reference feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EReference PARAMETER_VALUE__TYPE = eINSTANCE.getParameterValue_Type();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER_VALUE__VALUE = eINSTANCE.getParameterValue_Value();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ParameterExpressionImpl <em>Parameter Expression</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ParameterExpressionImpl
		 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl#getParameterExpression()
		 * @generated
		 */
		EClass PARAMETER_EXPRESSION = eINSTANCE.getParameterExpression();

		/**
		 * The meta object literal for the '<em><b>Mixed</b></em>' attribute list feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER_EXPRESSION__MIXED = eINSTANCE.getParameterExpression_Mixed();

		/**
		 * The meta object literal for the '<em><b>Expressions</b></em>' containment reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference PARAMETER_EXPRESSION__EXPRESSIONS = eINSTANCE.getParameterExpression_Expressions();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.FormulaImpl <em>Formula</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.FormulaImpl
		 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl#getFormula()
		 * @generated
		 */
		EClass FORMULA = eINSTANCE.getFormula();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute FORMULA__VALUE = eINSTANCE.getFormula_Value();

		/**
		 * The meta object literal for the '{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.CustomApplicationImpl <em>Custom Application</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.CustomApplicationImpl
		 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.InstanceModel20PackageImpl#getCustomApplication()
		 * @generated
		 */
		EClass CUSTOM_APPLICATION = eINSTANCE.getCustomApplication();

	}

} // InstanceModel20Package

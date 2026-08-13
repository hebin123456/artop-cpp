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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Parameter Expression</b></em>'. <!--
 * end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression#getMixed <em>Mixed</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression#getExpressions <em>Expressions</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getParameterExpression()
 * @model extendedMetaData="kind='mixed'"
 * @generated
 */
public interface ParameterExpression extends EObject {
	/**
	 * Returns the value of the '<em><b>Mixed</b></em>' attribute list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.util.FeatureMap.Entry}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mixed</em>' attribute list isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mixed</em>' attribute list.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getParameterExpression_Mixed()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.EFeatureMapEntry" many="true"
	 *        extendedMetaData="name=':mixed' kind='elementWildcard'"
	 * @generated
	 */
	FeatureMap getMixed();

	/**
	 * Returns the value of the '<em><b>Expressions</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Formula}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Expressions</em>' containment reference list isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Expressions</em>' containment reference list.
	 * @see org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package#getParameterExpression_Expressions()
	 * @model containment="true" required="true" transient="true" volatile="true" derived="true"
	 *        extendedMetaData="kind='element'"
	 * @generated
	 */
	EList<Formula> getExpressions();

} // ParameterExpression

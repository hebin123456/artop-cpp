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
package org.eclipse.sphinx.examples.hummingbird10.util;

import java.util.Map;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.sphinx.examples.hummingbird10.Activator;
import org.eclipse.sphinx.examples.hummingbird10.Application;
import org.eclipse.sphinx.examples.hummingbird10.Component;
import org.eclipse.sphinx.examples.hummingbird10.Connection;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.examples.hummingbird10.Interface;
import org.eclipse.sphinx.examples.hummingbird10.Parameter;

/**
 * <!-- begin-user-doc --> The <b>Validator</b> for the model. <!-- end-user-doc -->
 * 
 * @see org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package
 * @generated
 */
public class Hummingbird10Validator extends EObjectValidator {
	/**
	 * The cached model package <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public static final Hummingbird10Validator INSTANCE = new Hummingbird10Validator();

	/**
	 * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic
	 * {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @see org.eclipse.emf.common.util.Diagnostic#getSource()
	 * @see org.eclipse.emf.common.util.Diagnostic#getCode()
	 * @generated
	 */
	public static final String DIAGNOSTIC_SOURCE = "org.eclipse.sphinx.examples.hummingbird10"; //$NON-NLS-1$

	/**
	 * A constant with a fixed name that can be used as the base value for additional hand written constants. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private static final int GENERATED_DIAGNOSTIC_CODE_COUNT = 0;

	/**
	 * A constant with a fixed name that can be used as the base value for additional hand written constants in a
	 * derived class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected static final int DIAGNOSTIC_CODE_COUNT = GENERATED_DIAGNOSTIC_CODE_COUNT;

	/**
	 * Creates an instance of the switch. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Hummingbird10Validator() {
		super();
	}

	/**
	 * Returns the package of this validator switch. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EPackage getEPackage() {
		return Hummingbird10Package.eINSTANCE;
	}

	/**
	 * Calls <code>validateXXX</code> for the corresponding classifier of the model. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected boolean validate(int classifierID, Object value, DiagnosticChain diagnostics, Map<Object, Object> context) {
		switch (classifierID) {
		case Hummingbird10Package.COMPONENT:
			return validateComponent((Component) value, diagnostics, context);
		case Hummingbird10Package.APPLICATION:
			return validateApplication((Application) value, diagnostics, context);
		case Hummingbird10Package.CONNECTION:
			return validateConnection((Connection) value, diagnostics, context);
		case Hummingbird10Package.INTERFACE:
			return validateInterface((Interface) value, diagnostics, context);
		case Hummingbird10Package.PARAMETER:
			return validateParameter((Parameter) value, diagnostics, context);
		default:
			return true;
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public boolean validateComponent(Component component, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate_EveryDefaultConstraint(component, diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public boolean validateApplication(Application application, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate_EveryDefaultConstraint(application, diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public boolean validateConnection(Connection connection, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(connection, diagnostics, context)) {
			return false;
		}
		boolean result = validate_EveryMultiplicityConforms(connection, diagnostics, context);
		if (result || diagnostics != null) {
			result &= validate_EveryDataValueConforms(connection, diagnostics, context);
		}
		if (result || diagnostics != null) {
			result &= validate_EveryReferenceIsContained(connection, diagnostics, context);
		}
		if (result || diagnostics != null) {
			result &= validate_EveryBidirectionalReferenceIsPaired(connection, diagnostics, context);
		}
		if (result || diagnostics != null) {
			result &= validate_EveryProxyResolves(connection, diagnostics, context);
		}
		if (result || diagnostics != null) {
			result &= validate_UniqueID(connection, diagnostics, context);
		}
		if (result || diagnostics != null) {
			result &= validate_EveryKeyUnique(connection, diagnostics, context);
		}
		if (result || diagnostics != null) {
			result &= validate_EveryMapEntryUnique(connection, diagnostics, context);
		}
		if (result || diagnostics != null) {
			result &= validateConnection_validateInterfacesImplementedByTargetComponent(connection, diagnostics, context);
		}
		return result;
	}

	/**
	 * Validates the validateInterfacesImplementedByTargetComponent constraint of '<em>Connection</em>'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public boolean validateConnection_validateInterfacesImplementedByTargetComponent(Connection connection, DiagnosticChain diagnostics,
			Map<Object, Object> context) {
		// TODO implement the constraint
		// -> specify the condition that violates the constraint
		// -> verify the diagnostic details, including severity, code, and message
		// Ensure that you remove @generated or mark it @generated NOT
		if (false) {
			if (diagnostics != null) {
				diagnostics.add(createDiagnostic(Diagnostic.ERROR, DIAGNOSTIC_SOURCE, 0, "_UI_GenericConstraint_diagnostic", //$NON-NLS-1$
						new Object[] { "validateInterfacesImplementedByTargetComponent", getObjectLabel(connection, context) }, //$NON-NLS-1$
						new Object[] { connection }, context));
			}
			return false;
		}
		return true;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public boolean validateInterface(Interface interface_, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate_EveryDefaultConstraint(interface_, diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public boolean validateParameter(Parameter parameter, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate_EveryDefaultConstraint(parameter, diagnostics, context);
	}

	/**
	 * Returns the resource locator that will be used to fetch messages for this validator's diagnostics. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		return Activator.INSTANCE;
	}

} // Hummingbird10Validator

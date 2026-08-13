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
package org.eclipse.sphinx.examples.hummingbird20.typemodel.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.Switch;
import org.eclipse.sphinx.examples.hummingbird20.common.Identifiable;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.*;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;

/**
 * <!-- begin-user-doc --> The <b>Switch</b> for the model's inheritance hierarchy. It supports the call
 * {@link #doSwitch(EObject) doSwitch(object)} to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object and proceeding up the inheritance hierarchy until a non-null result is
 * returned, which is the result of the switch. <!-- end-user-doc -->
 * @see org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package
 * @generated
 */
public class TypeModel20Switch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected static TypeModel20Package modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public TypeModel20Switch() {
		if (modelPackage == null) {
			modelPackage = TypeModel20Package.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case TypeModel20Package.PLATFORM: {
				Platform platform = (Platform)theEObject;
				T result = casePlatform(platform);
				if (result == null) result = caseIdentifiable(platform);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TypeModel20Package.COMPONENT_TYPE: {
				ComponentType componentType = (ComponentType)theEObject;
				T result = caseComponentType(componentType);
				if (result == null) result = caseIdentifiable(componentType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TypeModel20Package.PORT: {
				Port port = (Port)theEObject;
				T result = casePort(port);
				if (result == null) result = caseIdentifiable(port);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TypeModel20Package.INTERFACE: {
				Interface interface_ = (Interface)theEObject;
				T result = caseInterface(interface_);
				if (result == null) result = caseIdentifiable(interface_);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TypeModel20Package.PARAMETER: {
				Parameter parameter = (Parameter)theEObject;
				T result = caseParameter(parameter);
				if (result == null) result = caseIdentifiable(parameter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Platform</em>'.
	 * <!-- begin-user-doc --> This
	 * implementation returns null; returning a non-null result will terminate the switch. <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Platform</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePlatform(Platform object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Component Type</em>'. <!-- begin-user-doc
	 * --> This implementation returns null; returning a non-null result will terminate the switch. <!-- end-user-doc
	 * -->
	 * 
	 * @param object
	 *            the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Component Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseComponentType(ComponentType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Port</em>'.
	 * <!-- begin-user-doc --> This
	 * implementation returns null; returning a non-null result will terminate the switch. <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Port</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePort(Port object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Interface</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null; returning a non-null result will terminate the switch. <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Interface</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseInterface(Interface object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Parameter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null; returning a non-null result will terminate the switch. <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Parameter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseParameter(Parameter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Identifiable</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null; returning a non-null result will terminate the switch. <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Identifiable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseIdentifiable(Identifiable object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc --> This
	 * implementation returns null; returning a non-null result will terminate the switch, but this is the last case
	 * anyway. <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T defaultCase(EObject object) {
		return null;
	}

} // TypeModel20Switch

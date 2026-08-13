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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.*;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!-- end-user-doc -->
 * @generated
 */
public class TypeModel20FactoryImpl extends EFactoryImpl implements TypeModel20Factory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public static TypeModel20Factory init() {
		try {
			TypeModel20Factory theTypeModel20Factory = (TypeModel20Factory)EPackage.Registry.INSTANCE.getEFactory(TypeModel20Package.eNS_URI);
			if (theTypeModel20Factory != null) {
				return theTypeModel20Factory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new TypeModel20FactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public TypeModel20FactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case TypeModel20Package.PLATFORM: return createPlatform();
			case TypeModel20Package.COMPONENT_TYPE: return createComponentType();
			case TypeModel20Package.PORT: return createPort();
			case TypeModel20Package.INTERFACE: return createInterface();
			case TypeModel20Package.PARAMETER: return createParameter();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Platform createPlatform() {
		PlatformImpl platform = new PlatformImpl();
		return platform;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ComponentType createComponentType() {
		ComponentTypeImpl componentType = new ComponentTypeImpl();
		return componentType;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Port createPort() {
		PortImpl port = new PortImpl();
		return port;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Interface createInterface() {
		InterfaceImpl interface_ = new InterfaceImpl();
		return interface_;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Parameter createParameter() {
		ParameterImpl parameter = new ParameterImpl();
		return parameter;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeModel20Package getTypeModel20Package() {
		return (TypeModel20Package)getEPackage();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static TypeModel20Package getPackage() {
		return TypeModel20Package.eINSTANCE;
	}

} // TypeModel20FactoryImpl

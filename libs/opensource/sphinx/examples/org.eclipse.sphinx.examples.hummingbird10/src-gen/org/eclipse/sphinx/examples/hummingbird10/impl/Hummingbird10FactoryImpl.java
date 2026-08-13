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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.sphinx.examples.hummingbird10.Application;
import org.eclipse.sphinx.examples.hummingbird10.Component;
import org.eclipse.sphinx.examples.hummingbird10.Connection;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Factory;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.examples.hummingbird10.Interface;
import org.eclipse.sphinx.examples.hummingbird10.Parameter;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!-- end-user-doc -->
 * 
 * @generated
 */
public class Hummingbird10FactoryImpl extends EFactoryImpl implements Hummingbird10Factory {
	/**
	 * Creates the default factory implementation. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public static Hummingbird10Factory init() {
		try {
			Hummingbird10Factory theHummingbird10Factory = (Hummingbird10Factory) EPackage.Registry.INSTANCE
					.getEFactory(Hummingbird10Package.eNS_URI);
			if (theHummingbird10Factory != null) {
				return theHummingbird10Factory;
			}
		} catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new Hummingbird10FactoryImpl();
	}

	/**
	 * Creates an instance of the factory. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Hummingbird10FactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
		case Hummingbird10Package.COMPONENT:
			return createComponent();
		case Hummingbird10Package.APPLICATION:
			return createApplication();
		case Hummingbird10Package.CONNECTION:
			return createConnection();
		case Hummingbird10Package.INTERFACE:
			return createInterface();
		case Hummingbird10Package.PARAMETER:
			return createParameter();
		default:
			throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Component createComponent() {
		ComponentImpl component = new ComponentImpl();
		return component;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Application createApplication() {
		ApplicationImpl application = new ApplicationImpl();
		return application;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Connection createConnection() {
		ConnectionImpl connection = new ConnectionImpl();
		return connection;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Interface createInterface() {
		InterfaceImpl interface_ = new InterfaceImpl();
		return interface_;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Parameter createParameter() {
		ParameterImpl parameter = new ParameterImpl();
		return parameter;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Hummingbird10Package getHummingbird10Package() {
		return (Hummingbird10Package) getEPackage();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static Hummingbird10Package getPackage() {
		return Hummingbird10Package.eINSTANCE;
	}

} // Hummingbird10FactoryImpl

/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.serialization.model.extnodes.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.sphinx.tests.emf.serialization.model.extnodes.ExtNodesFactory;
import org.eclipse.sphinx.tests.emf.serialization.model.extnodes.ExtNodesPackage;
import org.eclipse.sphinx.tests.emf.serialization.model.extnodes.ExtendedNode;
import org.eclipse.sphinx.tests.emf.serialization.model.extnodes.Extension;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!-- end-user-doc -->
 * 
 * @generated
 */
public class ExtNodesFactoryImpl extends EFactoryImpl implements ExtNodesFactory {
	/**
	 * Creates the default factory implementation. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public static ExtNodesFactory init() {
		try {
			ExtNodesFactory theExtNodesFactory = (ExtNodesFactory) EPackage.Registry.INSTANCE
					.getEFactory("http://www.eclipse.org/rmf/serialization/model/extnodes.ecore");
			if (theExtNodesFactory != null) {
				return theExtNodesFactory;
			}
		} catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new ExtNodesFactoryImpl();
	}

	/**
	 * Creates an instance of the factory. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ExtNodesFactoryImpl() {
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
		case ExtNodesPackage.EXTENDED_NODE:
			return createExtendedNode();
		case ExtNodesPackage.EXTENSION:
			return createExtension();
		default:
			throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public ExtendedNode createExtendedNode() {
		ExtendedNodeImpl extendedNode = new ExtendedNodeImpl();
		return extendedNode;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Extension createExtension() {
		ExtensionImpl extension = new ExtensionImpl();
		return extension;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public ExtNodesPackage getExtNodesPackage() {
		return (ExtNodesPackage) getEPackage();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static ExtNodesPackage getPackage() {
		return ExtNodesPackage.eINSTANCE;
	}

} // ExtNodesFactoryImpl

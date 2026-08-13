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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sphinx.examples.hummingbird10.Component;
import org.eclipse.sphinx.examples.hummingbird10.Connection;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.examples.hummingbird10.Interface;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Connection</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.impl.ConnectionImpl#getRequiredInterface <em>Required Interface
 * </em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.impl.ConnectionImpl#getTargetComponent <em>Target Component
 * </em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.impl.ConnectionImpl#getSourceComponent <em>Source Component
 * </em>}</li>
 * <li>{@link org.eclipse.sphinx.examples.hummingbird10.impl.ConnectionImpl#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ConnectionImpl extends EObjectImpl implements Connection {
	/**
	 * The cached value of the '{@link #getRequiredInterface() <em>Required Interface</em>}' reference. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getRequiredInterface()
	 * @generated
	 * @ordered
	 */
	protected Interface requiredInterface;

	/**
	 * The cached value of the '{@link #getTargetComponent() <em>Target Component</em>}' reference. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @see #getTargetComponent()
	 * @generated
	 * @ordered
	 */
	protected Component targetComponent;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ConnectionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return Hummingbird10Package.Literals.CONNECTION;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Interface getRequiredInterface() {
		if (requiredInterface != null && requiredInterface.eIsProxy()) {
			InternalEObject oldRequiredInterface = (InternalEObject) requiredInterface;
			requiredInterface = (Interface) eResolveProxy(oldRequiredInterface);
			if (requiredInterface != oldRequiredInterface) {
				if (eNotificationRequired()) {
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, Hummingbird10Package.CONNECTION__REQUIRED_INTERFACE,
							oldRequiredInterface, requiredInterface));
				}
			}
		}
		return requiredInterface;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Interface basicGetRequiredInterface() {
		return requiredInterface;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setRequiredInterface(Interface newRequiredInterface) {
		Interface oldRequiredInterface = requiredInterface;
		requiredInterface = newRequiredInterface;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, Hummingbird10Package.CONNECTION__REQUIRED_INTERFACE, oldRequiredInterface,
					requiredInterface));
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Component getTargetComponent() {
		if (targetComponent != null && targetComponent.eIsProxy()) {
			InternalEObject oldTargetComponent = (InternalEObject) targetComponent;
			targetComponent = (Component) eResolveProxy(oldTargetComponent);
			if (targetComponent != oldTargetComponent) {
				if (eNotificationRequired()) {
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, Hummingbird10Package.CONNECTION__TARGET_COMPONENT, oldTargetComponent,
							targetComponent));
				}
			}
		}
		return targetComponent;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Component basicGetTargetComponent() {
		return targetComponent;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public NotificationChain basicSetTargetComponent(Component newTargetComponent, NotificationChain msgs) {
		Component oldTargetComponent = targetComponent;
		targetComponent = newTargetComponent;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, Hummingbird10Package.CONNECTION__TARGET_COMPONENT,
					oldTargetComponent, newTargetComponent);
			if (msgs == null) {
				msgs = notification;
			} else {
				msgs.add(notification);
			}
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setTargetComponent(Component newTargetComponent) {
		if (newTargetComponent != targetComponent) {
			NotificationChain msgs = null;
			if (targetComponent != null) {
				msgs = ((InternalEObject) targetComponent).eInverseRemove(this, Hummingbird10Package.COMPONENT__INCOMING_CONNECTIONS,
						Component.class, msgs);
			}
			if (newTargetComponent != null) {
				msgs = ((InternalEObject) newTargetComponent).eInverseAdd(this, Hummingbird10Package.COMPONENT__INCOMING_CONNECTIONS,
						Component.class, msgs);
			}
			msgs = basicSetTargetComponent(newTargetComponent, msgs);
			if (msgs != null) {
				msgs.dispatch();
			}
		} else if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, Hummingbird10Package.CONNECTION__TARGET_COMPONENT, newTargetComponent,
					newTargetComponent));
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Component getSourceComponent() {
		if (eContainerFeatureID() != Hummingbird10Package.CONNECTION__SOURCE_COMPONENT) {
			return null;
		}
		return (Component) eInternalContainer();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public NotificationChain basicSetSourceComponent(Component newSourceComponent, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject) newSourceComponent, Hummingbird10Package.CONNECTION__SOURCE_COMPONENT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setSourceComponent(Component newSourceComponent) {
		if (newSourceComponent != eInternalContainer() || eContainerFeatureID() != Hummingbird10Package.CONNECTION__SOURCE_COMPONENT
				&& newSourceComponent != null) {
			if (EcoreUtil.isAncestor(this, newSourceComponent)) {
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString()); //$NON-NLS-1$
			}
			NotificationChain msgs = null;
			if (eInternalContainer() != null) {
				msgs = eBasicRemoveFromContainer(msgs);
			}
			if (newSourceComponent != null) {
				msgs = ((InternalEObject) newSourceComponent).eInverseAdd(this, Hummingbird10Package.COMPONENT__OUTGOING_CONNECTIONS,
						Component.class, msgs);
			}
			msgs = basicSetSourceComponent(newSourceComponent, msgs);
			if (msgs != null) {
				msgs.dispatch();
			}
		} else if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, Hummingbird10Package.CONNECTION__SOURCE_COMPONENT, newSourceComponent,
					newSourceComponent));
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, Hummingbird10Package.CONNECTION__NAME, oldName, name));
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case Hummingbird10Package.CONNECTION__TARGET_COMPONENT:
			if (targetComponent != null) {
				msgs = ((InternalEObject) targetComponent).eInverseRemove(this, Hummingbird10Package.COMPONENT__INCOMING_CONNECTIONS,
						Component.class, msgs);
			}
			return basicSetTargetComponent((Component) otherEnd, msgs);
		case Hummingbird10Package.CONNECTION__SOURCE_COMPONENT:
			if (eInternalContainer() != null) {
				msgs = eBasicRemoveFromContainer(msgs);
			}
			return basicSetSourceComponent((Component) otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case Hummingbird10Package.CONNECTION__TARGET_COMPONENT:
			return basicSetTargetComponent(null, msgs);
		case Hummingbird10Package.CONNECTION__SOURCE_COMPONENT:
			return basicSetSourceComponent(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
		case Hummingbird10Package.CONNECTION__SOURCE_COMPONENT:
			return eInternalContainer().eInverseRemove(this, Hummingbird10Package.COMPONENT__OUTGOING_CONNECTIONS, Component.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case Hummingbird10Package.CONNECTION__REQUIRED_INTERFACE:
			if (resolve) {
				return getRequiredInterface();
			}
			return basicGetRequiredInterface();
		case Hummingbird10Package.CONNECTION__TARGET_COMPONENT:
			if (resolve) {
				return getTargetComponent();
			}
			return basicGetTargetComponent();
		case Hummingbird10Package.CONNECTION__SOURCE_COMPONENT:
			return getSourceComponent();
		case Hummingbird10Package.CONNECTION__NAME:
			return getName();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case Hummingbird10Package.CONNECTION__REQUIRED_INTERFACE:
			setRequiredInterface((Interface) newValue);
			return;
		case Hummingbird10Package.CONNECTION__TARGET_COMPONENT:
			setTargetComponent((Component) newValue);
			return;
		case Hummingbird10Package.CONNECTION__SOURCE_COMPONENT:
			setSourceComponent((Component) newValue);
			return;
		case Hummingbird10Package.CONNECTION__NAME:
			setName((String) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case Hummingbird10Package.CONNECTION__REQUIRED_INTERFACE:
			setRequiredInterface((Interface) null);
			return;
		case Hummingbird10Package.CONNECTION__TARGET_COMPONENT:
			setTargetComponent((Component) null);
			return;
		case Hummingbird10Package.CONNECTION__SOURCE_COMPONENT:
			setSourceComponent((Component) null);
			return;
		case Hummingbird10Package.CONNECTION__NAME:
			setName(NAME_EDEFAULT);
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case Hummingbird10Package.CONNECTION__REQUIRED_INTERFACE:
			return requiredInterface != null;
		case Hummingbird10Package.CONNECTION__TARGET_COMPONENT:
			return targetComponent != null;
		case Hummingbird10Package.CONNECTION__SOURCE_COMPONENT:
			return getSourceComponent() != null;
		case Hummingbird10Package.CONNECTION__NAME:
			return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) {
			return super.toString();
		}

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: "); //$NON-NLS-1$
		result.append(name);
		result.append(')');
		return result.toString();
	}

} // ConnectionImpl

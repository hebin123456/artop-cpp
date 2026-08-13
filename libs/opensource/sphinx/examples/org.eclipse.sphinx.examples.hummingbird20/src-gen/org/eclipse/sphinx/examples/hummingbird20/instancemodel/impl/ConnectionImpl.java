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
package org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sphinx.examples.hummingbird20.common.impl.IdentifiableImpl;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Connection</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ConnectionImpl#getSourceComponent <em>Source Component</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ConnectionImpl#getSourcePort <em>Source Port</em>}</li>
 *   <li>{@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ConnectionImpl#getTargetComponent <em>Target Component</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ConnectionImpl extends IdentifiableImpl implements Connection {
	/**
	 * The cached value of the '{@link #getSourcePort() <em>Source Port</em>}' reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getSourcePort()
	 * @generated
	 * @ordered
	 */
	protected Port sourcePort;

	/**
	 * The cached value of the '{@link #getTargetComponent() <em>Target Component</em>}' reference.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @see #getTargetComponent()
	 * @generated
	 * @ordered
	 */
	protected Component targetComponent;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected ConnectionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return InstanceModel20Package.Literals.CONNECTION;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Component getSourceComponent() {
		if (eContainerFeatureID() != InstanceModel20Package.CONNECTION__SOURCE_COMPONENT) return null;
		return (Component)eInternalContainer();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSourceComponent(Component newSourceComponent, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newSourceComponent, InstanceModel20Package.CONNECTION__SOURCE_COMPONENT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSourceComponent(Component newSourceComponent) {
		if (newSourceComponent != eInternalContainer() || (eContainerFeatureID() != InstanceModel20Package.CONNECTION__SOURCE_COMPONENT && newSourceComponent != null)) {
			if (EcoreUtil.isAncestor(this, newSourceComponent))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString()); //$NON-NLS-1$
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newSourceComponent != null)
				msgs = ((InternalEObject)newSourceComponent).eInverseAdd(this, InstanceModel20Package.COMPONENT__OUTGOING_CONNECTIONS, Component.class, msgs);
			msgs = basicSetSourceComponent(newSourceComponent, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InstanceModel20Package.CONNECTION__SOURCE_COMPONENT, newSourceComponent, newSourceComponent));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Port getSourcePort() {
		if (sourcePort != null && sourcePort.eIsProxy()) {
			InternalEObject oldSourcePort = (InternalEObject)sourcePort;
			sourcePort = (Port)eResolveProxy(oldSourcePort);
			if (sourcePort != oldSourcePort) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, InstanceModel20Package.CONNECTION__SOURCE_PORT, oldSourcePort, sourcePort));
			}
		}
		return sourcePort;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Port basicGetSourcePort() {
		return sourcePort;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSourcePort(Port newSourcePort) {
		Port oldSourcePort = sourcePort;
		sourcePort = newSourcePort;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InstanceModel20Package.CONNECTION__SOURCE_PORT, oldSourcePort, sourcePort));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Component getTargetComponent() {
		if (targetComponent != null && targetComponent.eIsProxy()) {
			InternalEObject oldTargetComponent = (InternalEObject)targetComponent;
			targetComponent = (Component)eResolveProxy(oldTargetComponent);
			if (targetComponent != oldTargetComponent) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, InstanceModel20Package.CONNECTION__TARGET_COMPONENT, oldTargetComponent, targetComponent));
			}
		}
		return targetComponent;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Component basicGetTargetComponent() {
		return targetComponent;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetTargetComponent(Component newTargetComponent, NotificationChain msgs) {
		Component oldTargetComponent = targetComponent;
		targetComponent = newTargetComponent;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, InstanceModel20Package.CONNECTION__TARGET_COMPONENT, oldTargetComponent, newTargetComponent);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTargetComponent(Component newTargetComponent) {
		if (newTargetComponent != targetComponent) {
			NotificationChain msgs = null;
			if (targetComponent != null)
				msgs = ((InternalEObject)targetComponent).eInverseRemove(this, InstanceModel20Package.COMPONENT__INCOMING_CONNECTIONS, Component.class, msgs);
			if (newTargetComponent != null)
				msgs = ((InternalEObject)newTargetComponent).eInverseAdd(this, InstanceModel20Package.COMPONENT__INCOMING_CONNECTIONS, Component.class, msgs);
			msgs = basicSetTargetComponent(newTargetComponent, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InstanceModel20Package.CONNECTION__TARGET_COMPONENT, newTargetComponent, newTargetComponent));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case InstanceModel20Package.CONNECTION__SOURCE_COMPONENT:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetSourceComponent((Component)otherEnd, msgs);
			case InstanceModel20Package.CONNECTION__TARGET_COMPONENT:
				if (targetComponent != null)
					msgs = ((InternalEObject)targetComponent).eInverseRemove(this, InstanceModel20Package.COMPONENT__INCOMING_CONNECTIONS, Component.class, msgs);
				return basicSetTargetComponent((Component)otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case InstanceModel20Package.CONNECTION__SOURCE_COMPONENT:
				return basicSetSourceComponent(null, msgs);
			case InstanceModel20Package.CONNECTION__TARGET_COMPONENT:
				return basicSetTargetComponent(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
			case InstanceModel20Package.CONNECTION__SOURCE_COMPONENT:
				return eInternalContainer().eInverseRemove(this, InstanceModel20Package.COMPONENT__OUTGOING_CONNECTIONS, Component.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case InstanceModel20Package.CONNECTION__SOURCE_COMPONENT:
				return getSourceComponent();
			case InstanceModel20Package.CONNECTION__SOURCE_PORT:
				if (resolve) return getSourcePort();
				return basicGetSourcePort();
			case InstanceModel20Package.CONNECTION__TARGET_COMPONENT:
				if (resolve) return getTargetComponent();
				return basicGetTargetComponent();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case InstanceModel20Package.CONNECTION__SOURCE_COMPONENT:
				setSourceComponent((Component)newValue);
				return;
			case InstanceModel20Package.CONNECTION__SOURCE_PORT:
				setSourcePort((Port)newValue);
				return;
			case InstanceModel20Package.CONNECTION__TARGET_COMPONENT:
				setTargetComponent((Component)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case InstanceModel20Package.CONNECTION__SOURCE_COMPONENT:
				setSourceComponent((Component)null);
				return;
			case InstanceModel20Package.CONNECTION__SOURCE_PORT:
				setSourcePort((Port)null);
				return;
			case InstanceModel20Package.CONNECTION__TARGET_COMPONENT:
				setTargetComponent((Component)null);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case InstanceModel20Package.CONNECTION__SOURCE_COMPONENT:
				return getSourceComponent() != null;
			case InstanceModel20Package.CONNECTION__SOURCE_PORT:
				return sourcePort != null;
			case InstanceModel20Package.CONNECTION__TARGET_COMPONENT:
				return targetComponent != null;
		}
		return super.eIsSet(featureID);
	}

} // ConnectionImpl

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
package org.eclipse.sphinx.examples.hummingbird20.instancemodel.edit;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.StyledString;
import org.eclipse.sphinx.examples.hummingbird20.common.edit.IdentifiableItemProvider;
import org.eclipse.sphinx.examples.hummingbird20.edit.Activator;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;

/**
 * This is the item provider adapter for a {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection} object.
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 * @generated
 */
public class ConnectionItemProvider extends IdentifiableItemProvider {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ConnectionItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	/**
	 * This returns the property descriptors for the adapted class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
		if (itemPropertyDescriptors == null) {
			super.getPropertyDescriptors(object);

			addSourcePortPropertyDescriptor(object);
			addTargetComponentPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Source Port feature.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSourcePortPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Connection_sourcePort_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Connection_sourcePort_feature", "_UI_Connection_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 InstanceModel20Package.Literals.CONNECTION__SOURCE_PORT,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Target Component feature.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected void addTargetComponentPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Connection_targetComponent_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Connection_targetComponent_feature", "_UI_Connection_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 InstanceModel20Package.Literals.CONNECTION__TARGET_COMPONENT,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This returns Connection.gif.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/Connection")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		return ((StyledString)getStyledText(object)).getString();
	}

	/**
	 * This returns the label styled text for the adapted class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getStyledText(Object object) {
		String label = ((Connection)object).getName();
    	StyledString styledLabel = new StyledString();
		if (label == null || label.length() == 0) {
			styledLabel.append(getString("_UI_Connection_type"), StyledString.Style.QUALIFIER_STYLER);  //$NON-NLS-1$
		} else {
			styledLabel.append(getString("_UI_Connection_type"), StyledString.Style.QUALIFIER_STYLER).append(" " + label); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return styledLabel;
	}

	/**
	 * This handles model notifications by calling {@link #updateChildren} to update any cached children and by creating
	 * a viewer notification, which it passes to {@link #fireNotifyChanged}. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 */
	@Override
	public void notifyChanged(Notification notification) {
		updateChildren(notification);
		super.notifyChanged(notification);
	}

	/**
	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
	 * that can be created under this object.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
		super.collectNewChildDescriptors(newChildDescriptors, object);
	}

	/**
	 * Return the resource locator for this item provider's resources.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		return Activator.INSTANCE;
	}

}

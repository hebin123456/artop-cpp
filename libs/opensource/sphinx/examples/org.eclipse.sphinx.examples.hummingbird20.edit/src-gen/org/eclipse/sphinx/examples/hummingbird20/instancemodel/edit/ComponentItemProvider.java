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
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.StyledString;
import org.eclipse.emf.edit.provider.ViewerNotification;
import org.eclipse.sphinx.examples.hummingbird20.common.edit.IdentifiableItemProvider;
import org.eclipse.sphinx.examples.hummingbird20.edit.Activator;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;

/**
 * This is the item provider adapter for a {@link org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component} object.
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 * @generated
 */
public class ComponentItemProvider extends IdentifiableItemProvider {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ComponentItemProvider(AdapterFactory adapterFactory) {
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

			addTypePropertyDescriptor(object);
			addIncomingConnectionsPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Type feature.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected void addTypePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Component_type_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Component_type_feature", "_UI_Component_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 InstanceModel20Package.Literals.COMPONENT__TYPE,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Incoming Connections feature. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 */
	protected void addIncomingConnectionsPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Component_incomingConnections_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Component_incomingConnections_feature", "_UI_Component_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 InstanceModel20Package.Literals.COMPONENT__INCOMING_CONNECTIONS,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 */
	@Override
	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
		if (childrenFeatures == null) {
			super.getChildrenFeatures(object);
			childrenFeatures.add(InstanceModel20Package.Literals.COMPONENT__OUTGOING_CONNECTIONS);
			childrenFeatures.add(InstanceModel20Package.Literals.COMPONENT__PARAMETER_VALUES);
			childrenFeatures.add(InstanceModel20Package.Literals.COMPONENT__PARAMETER_EXPRESSIONS);
		}
		return childrenFeatures;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EStructuralFeature getChildFeature(Object object, Object child) {
		// Check the type of the specified child object and return the proper feature to use for
		// adding (see {@link AddCommand}) it as a child.

		return super.getChildFeature(object, child);
	}

	/**
	 * This returns Component.gif.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/Component")); //$NON-NLS-1$
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
		String label = ((Component)object).getName();
    	StyledString styledLabel = new StyledString();
		if (label == null || label.length() == 0) {
			styledLabel.append(getString("_UI_Component_type"), StyledString.Style.QUALIFIER_STYLER);  //$NON-NLS-1$
		} else {
			styledLabel.append(getString("_UI_Component_type"), StyledString.Style.QUALIFIER_STYLER).append(" " + label); //$NON-NLS-1$ //$NON-NLS-2$
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

		switch (notification.getFeatureID(Component.class)) {
			case InstanceModel20Package.COMPONENT__OUTGOING_CONNECTIONS:
			case InstanceModel20Package.COMPONENT__PARAMETER_VALUES:
			case InstanceModel20Package.COMPONENT__PARAMETER_EXPRESSIONS:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
				return;
		}
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

		newChildDescriptors.add
			(createChildParameter
				(InstanceModel20Package.Literals.COMPONENT__OUTGOING_CONNECTIONS,
				 InstanceModel20Factory.eINSTANCE.createConnection()));

		newChildDescriptors.add
			(createChildParameter
				(InstanceModel20Package.Literals.COMPONENT__PARAMETER_VALUES,
				 InstanceModel20Factory.eINSTANCE.createParameterValue()));

		newChildDescriptors.add
			(createChildParameter
				(InstanceModel20Package.Literals.COMPONENT__PARAMETER_EXPRESSIONS,
				 InstanceModel20Factory.eINSTANCE.createParameterExpression()));
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

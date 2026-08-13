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
 *     itemis - [446573] BasicExplorerContent/LabelProvider don't get refreshed upon changes on provided referenced elements
 *     itemis - [447193] Enable transient item providers to be created through adapter factories
 *     itemis - [450882] Enable navigation to ancestor tree items in Model Explorer kind of model views
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.edit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandWrapper;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.command.CreateChildCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.AttributeValueWrapperItemProvider;
import org.eclipse.emf.edit.provider.DelegatingWrapperItemProvider;
import org.eclipse.emf.edit.provider.Disposable;
import org.eclipse.emf.edit.provider.FeatureMapEntryWrapperItemProvider;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.sphinx.emf.Activator;

/**
 * A base class for transient item provider {@link Adapter adapter}s that can be used to insert non-model view objects
 * between an object and its children.
 * <p>
 * Transient item providers must be instantiated statefully wrt their target object, i.e., the same transient item
 * provider instance must not be used for multiple target objects.
 * </p>
 */
public class TransientItemProvider extends ExtendedItemProviderAdapter implements IEditingDomainItemProvider, IStructuredItemContentProvider,
		ITreeItemContentProvider, ITreeItemAncestorProvider, IItemLabelProvider, IItemPropertySource {

	public static class AdapterFactoryHelper {

		public static Object adapt(Object target, Object type, AdapterFactory adapterFactory) {
			if (type instanceof Class<?> && TransientItemProvider.class.isAssignableFrom((Class<?>) type)) {
				Adapter adapter = EcoreUtil.getExistingAdapter((Notifier) target, type);
				if (adapter != null) {
					return adapter;
				}
				return adapterFactory.adaptNew((Notifier) target, type);
			}
			return null;
		}
	}

	/**
	 * Standard constructor for creation of transient item provider instances through an {@link AdapterFactory adapter
	 * factory}.
	 *
	 * @param adapterFactory
	 *            The adapter factory which created this transient item provider instance.
	 */
	public TransientItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	/**
	 * Alternative constructor for direct creation of transient item provider instances without going through
	 * {@link org.eclipse.emf.common.notify.impl.AdapterFactoryImpl#adapt(Notifier, Object))}. Explicitly adds the
	 * freshly created transient item provider to the {@link Notifier#eAdapters() adapters} of specified
	 * <code>target</code> object.
	 *
	 * @param adapterFactory
	 *            The adapter factory for the target object of the transient item provider to be created.
	 * @param target
	 *            The target {@link EObject object} of the transient item provider to be created.
	 * @deprecated Use {@link #TransientItemProvider(AdapterFactory)} instead.
	 */
	@Deprecated
	public TransientItemProvider(AdapterFactory adapterFactory, Notifier target) {
		super(adapterFactory);
		target.eAdapters().add(this);
	}

	/**
	 * Handles model change notifications by calling {@link #updateChildren} to update any cached children and by
	 * creating a viewer notification, which it passes to {@link #fireNotifyChanged} to trigger a full or partial
	 * refresh of the underlying viewer.
	 */
	@Override
	public void notifyChanged(Notification notification) {
		updateChildren(notification);
		super.notifyChanged(notification);
	}

	/*
	 * Overridden to use the target object behind this transient item provider rather than given object (which would be
	 * the transient item provider itself) in order to (1) retrieve the store for the children of the given object (and
	 * thereby retrieve it in the same way as in #updateChildren(Notification)) and (2) obtain the values of the
	 * children features.
	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#getChildren(java.lang.Object)
	 */
	@Override
	public Collection<?> getChildren(Object object) {
		ChildrenStore store = getChildrenStore(target);
		if (store != null) {
			return store.getChildren();
		}

		store = createChildrenStore(object);
		List<Object> result = store != null ? null : new ArrayList<Object>();
		EObject eObject = (EObject) target;

		for (EStructuralFeature feature : getChildrenFeatures(object)) {
			if (feature.isMany()) {
				List<?> children = (List<?>) eObject.eGet(feature);
				int index = 0;
				for (Object unwrappedChild : children) {
					Object child = wrap(object, feature, unwrappedChild, index);
					if (store != null) {
						store.getList(feature).add(child);
					} else {
						result.add(child);
					}
					index++;
				}
			} else {
				Object child = eObject.eGet(feature);
				if (child != null) {
					child = wrap(object, feature, child, CommandParameter.NO_INDEX);
					if (store != null) {
						store.setValue(feature, child);
					} else {
						result.add(child);
					}
				}
			}
		}
		return store != null ? store.getChildren() : result;
	}

	/*
	 * Overridden to use the target object behind this transient item provider rather than given object (which would be
	 * the transient item provider itself) as key for the store for the children of the given object (and thereby match
	 * the way it is retrieved in #updateChildren(Notification)).
	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#createChildrenStore(java.lang.Object)
	 */
	@Override
	protected ChildrenStore createChildrenStore(Object object) {
		ChildrenStore store = null;

		if (isWrappingNeeded(object)) {
			if (childrenStoreMap == null) {
				childrenStoreMap = new HashMap<Object, ChildrenStore>();
			}
			store = new ChildrenStore(getChildrenFeatures(object));
			childrenStoreMap.put(target, store);
		}
		return store;
	}

	/**
	 * Wraps a value, if needed, and keeps the wrapper for disposal along with the item provider. This method actually
	 * calls {@link #createWrapper createWrapper} to determine if the given value, at the given index in the given
	 * feature of the given object, should be wrapped and to obtain the wrapper. If a wrapper is obtained, it is
	 * recorded and returned. Otherwise, the original value is returned. Subclasses may override {@link #createWrapper
	 * createWrapper} to specify when and with what to wrap values.
	 * <p>
	 * This method is very similar to {@link #wrap(EObject, EStructuralFeature, Object, int)} but accepts and handles
	 * {@link Object} rather than just {@link EObject} <code>object</code> arguments.
	 * </p>
	 */
	protected Object wrap(Object object, EStructuralFeature feature, Object value, int index) {
		if (!feature.isMany() && index != CommandParameter.NO_INDEX) {
			System.out.println("Bad wrap index."); //$NON-NLS-1$
			System.out.println("  object: " + object); //$NON-NLS-1$
			System.out.println("  feature: " + feature); //$NON-NLS-1$
			System.out.println("  value: " + value); //$NON-NLS-1$
			System.out.println("  index: " + index); //$NON-NLS-1$
			new IllegalArgumentException("Bad wrap index.").printStackTrace(); //$NON-NLS-1$
		}

		Object wrapper = createWrapper(object, feature, value, index);
		if (wrapper == null) {
			wrapper = value;
		} else if (wrapper != value) {
			if (wrappers == null) {
				wrappers = new Disposable();
			}
			wrappers.add(wrapper);
		}
		return wrapper;
	}

	/**
	 * Creates and returns a wrapper for the given value, at the given index in the given feature of the given object if
	 * such a wrapper is needed; otherwise, returns the original value.
	 * <p>
	 * This method is very similar to {@link #createWrapper(EObject, EStructuralFeature, Object, int)} but accepts and
	 * handles {@link Object} rather than just {@link EObject} <code>object</code> arguments and uses
	 * {@link ExtendedDelegatingWrapperItemProvider} instead of {@link DelegatingWrapperItemProvider} to wrap cross
	 * references.
	 * </p>
	 *
	 * @see #createWrapper(EObject, EStructuralFeature, Object, int)
	 * @see ExtendedDelegatingWrapperItemProvider
	 */
	protected Object createWrapper(Object object, EStructuralFeature feature, Object value, int index) {
		if (!isWrappingNeeded(object)) {
			return value;
		}
		if (object instanceof EObject) {
			if (FeatureMapUtil.isFeatureMap(feature)) {
				value = new FeatureMapEntryWrapperItemProvider((FeatureMap.Entry) value, (EObject) object, (EAttribute) feature, index,
						adapterFactory, getResourceLocator());
			} else if (feature instanceof EAttribute) {
				value = new AttributeValueWrapperItemProvider(value, (EObject) object, (EAttribute) feature, index, adapterFactory,
						getResourceLocator());
			}
		} else {
			if (!((EReference) feature).isContainment()) {
				value = new ExtendedDelegatingWrapperItemProvider(value, object, feature, index, adapterFactory);
			}
		}
		return value;
	}

	/**
	 * This sets the parent of the transient item provider to <code>target</code> value. The target instance variable
	 * comes from the adapter base class {@link org.eclipse.emf.common.notify.impl.AdapterImpl}.
	 */
	@Override
	public Object getParent(Object object) {
		return target;
	}

	/**
	 * This creates a primitive {@link org.eclipse.emf.edit.command.RemoveCommand}.
	 */
	@Override
	protected Command createRemoveCommand(EditingDomain domain, EObject owner, EStructuralFeature feature, Collection<?> collection) {
		return createWrappedCommand(super.createRemoveCommand(domain, owner, feature, collection), owner);
	}

	/**
	 * This creates a primitive {@link org.eclipse.emf.edit.command.AddCommand}.
	 */
	@Override
	protected Command createAddCommand(EditingDomain domain, EObject owner, EStructuralFeature feature, Collection<?> collection, int index) {
		return createWrappedCommand(super.createAddCommand(domain, owner, feature, collection, index), owner);
	}

	/**
	 * This allows creating a command and overriding the {@link
	 * org.eclipse.emf.common.command.CommandWrapper.getAffectedObjects()#getAffectedObjects()} method to return the
	 * appropriate transient item provider whenever the real affected object is the owner.
	 *
	 * @param command
	 *            an command.
	 * @param owner
	 *            the owner of the object.
	 * @return a {@link CommandWrapper}
	 */
	protected Command createWrappedCommand(Command command, final EObject owner) {
		return new CommandWrapper(command) {
			@Override
			public Collection<?> getAffectedObjects() {
				Collection<?> affected = super.getAffectedObjects();
				if (affected.contains(owner)) {
					affected = Collections.singleton(TransientItemProvider.this);
				}
				return affected;
			}
		};
	}

	/**
	 * This implements {@link IEditingDomainItemProvider#getNewChildDescriptors
	 * IEditingDomainItemProvider.getNewChildDescriptors}, returning descriptors for all the possible children that can
	 * be added to the specified <code>target</code>. The target instance variable comes from the adapter base class
	 * {@link org.eclipse.emf.common.notify.impl.AdapterImpl}.
	 */
	@Override
	public Collection<?> getNewChildDescriptors(Object object, EditingDomain editingDomain, Object sibling) {
		return super.getNewChildDescriptors(target, editingDomain, sibling);
	}

	/**
	 * This implements delegated command creation for the given transient item provider and sets its owner to
	 * <code>target</code>.
	 */
	@Override
	public Command createCommand(Object object, EditingDomain domain, Class<? extends Command> commandClass, CommandParameter commandParameter) {
		if (commandClass == CreateChildCommand.class) {
			commandParameter.setOwner(target);
		}
		return super.createCommand(object, domain, commandClass, commandParameter);
	}

	/**
	 * Returns a folder kind of icon as default image for non-model view objects between an object and its children.
	 */
	@Override
	public Object getImage(Object object) {
		if (object != null) {
			return overlayImage(object, Activator.INSTANCE.getImage("full/obj16/folder_closed")); //$NON-NLS-1$
		}
		return null;
	}

	/*
	 * Overridden to ensure statefulness of transient item provider adapters wrt their target object, i.e., to avoid
	 * that the same transient item provider instance is used for multiple target objects.
	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#setTarget(org.eclipse.emf.common.notify.Notifier)
	 */
	@Override
	public void setTarget(Notifier target) {
		Assert.isLegal(this.target == null || this.target == target);
		super.setTarget(target);
	}

	/*
	 * Overridden to ensure that an already existing instance of some transient item provider on a given target object
	 * to be retrieved by using the class of the transient item provider in question as type to adapt to (rather than
	 * the item provider adapter factory as intended by ItemProviderAdapter). In combination with
	 * TransientItemProvider.AdapterFactoryHelper#adapt(Object, Object, AdapterFactory), this behavior enables the
	 * implementation of item provider adapter factories that support the creation and retrieval transient item provider
	 * instances by calling AdapterFactory#adapt(object, TransientItemProviderSubClass.class)
	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#isAdapterForType(java.lang.Object)
	 * @see org.eclipse.emf.ecore.util.EcoreUtil#getExistingAdapter(Notifier, Object)
	 * @see org.eclipse.sphinx.emf.edit.TransientItemProvider.AdapterFactoryHelper.adapt(Object, Object, AdapterFactory)
	 */
	@Override
	public boolean isAdapterForType(Object type) {
		return type == this.getClass();
	}
}

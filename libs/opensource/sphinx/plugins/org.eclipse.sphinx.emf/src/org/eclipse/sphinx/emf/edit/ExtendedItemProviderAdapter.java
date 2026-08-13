/**
 * <copyright>
 *
 * Copyright (c) 2008-2014 See4sys, BMW Car IT, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Added/Updated javadoc
 *     itemis - [446573] BasicExplorerContent/LabelProvider don't get refreshed upon changes on provided referenced elements
 *     itemis - [450882] Enable navigation to ancestor tree items in Model Explorer kind of model views
 *     itemis - [459865] Enhance ExtendedItemProviderAdapter to support EDataType elements that are no EEnums but wrap org.eclipse.emf.common.util.Enumerator
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.edit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.MissingResourceException;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandWrapper;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.command.UnexecutableCommand;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.emf.edit.EMFEditPlugin;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.command.CreateChildCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.AttributeValueWrapperItemProvider;
import org.eclipse.emf.edit.provider.ComposedImage;
import org.eclipse.emf.edit.provider.FeatureMapEntryWrapperItemProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.ecore.DefaultEcoreTraversalHelper;
import org.eclipse.sphinx.emf.ecore.EcoreTraversalHelper;

/**
 * Extension of the default {@linkplain ItemProviderAdapter item provider adapter} implementation provided by EMF Edit.
 */
public class ExtendedItemProviderAdapter extends ItemProviderAdapter {

	private ITreeItemAncestorProvider treeItemContentProviderHelper = null;

	/**
	 * An instance is created from an adapter factory. The factory is used as a key so that we always know which factory
	 * created this adapter.
	 *
	 * @param adapterFactory
	 *            The factory which created the Adapter.
	 */
	public ExtendedItemProviderAdapter(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	protected ITreeItemAncestorProvider getTreeItemContentProviderHelper() {
		if (treeItemContentProviderHelper == null) {
			treeItemContentProviderHelper = createTreeItemContentProviderHelper();
		}
		return treeItemContentProviderHelper;
	}

	protected ITreeItemAncestorProvider createTreeItemContentProviderHelper() {
		return new TreeItemAncestorProvider(this, adapterFactory);
	}

	public List<Object> getAncestorPath(Object object, boolean unwrap) {
		return getTreeItemContentProviderHelper().getAncestorPath(object, unwrap);
	}

	public List<Object> getAncestorPath(Object beginObject, Class<?> endType, boolean unwrap) {
		return getTreeItemContentProviderHelper().getAncestorPath(beginObject, endType, unwrap);
	}

	public Object findAncestor(Object object, Class<?> ancestorType, boolean unwrap) {
		return getTreeItemContentProviderHelper().findAncestor(object, ancestorType, unwrap);
	}

	/*
	 * Overridden to enable wrapping of cross-referenced model objects by default. This is required to make sure that
	 * the latter get represented by instances of org.eclipse.emf.edit.provider.DelegatingWrapperItemProvider and may
	 * have a parent element that is different from object containing them.
	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#isWrappingNeeded(java.lang.Object)
	 */
	@Override
	protected boolean isWrappingNeeded(Object object) {
		if (wrappingNeeded == null) {
			wrappingNeeded = Boolean.FALSE;

			for (EStructuralFeature feature : getChildrenFeatures(object)) {
				if (feature instanceof EAttribute || feature instanceof EReference && !((EReference) feature).isContainment()) {
					wrappingNeeded = Boolean.TRUE;
					break;
				}
			}
		}
		return wrappingNeeded;
	}

	/*
	 * Overridden to create StyledDelegatingWrapperItemProvider instead of the default DelegatingWrapperItemProvider, so
	 * that StyledString-typed strings can be used as labels instead of ordinary text strings.
	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#createWrapper(org.eclipse.emf.ecore.EObject,
	 * org.eclipse.emf.ecore.EStructuralFeature, java.lang.Object, int)
	 */
	@Override
	protected Object createWrapper(EObject object, EStructuralFeature feature, Object value, int index) {
		if (!isWrappingNeeded(object)) {
			return value;
		}

		if (FeatureMapUtil.isFeatureMap(feature)) {
			value = new FeatureMapEntryWrapperItemProvider((FeatureMap.Entry) value, object, (EAttribute) feature, index, adapterFactory,
					getResourceLocator());
		} else if (feature instanceof EAttribute) {
			value = new AttributeValueWrapperItemProvider(value, object, (EAttribute) feature, index, adapterFactory, getResourceLocator());
		} else if (!((EReference) feature).isContainment()) {
			value = new ExtendedDelegatingWrapperItemProvider(value, object, feature, index, adapterFactory);
		}

		return value;
	}

	@Override
	protected ItemPropertyDescriptor createItemPropertyDescriptor(AdapterFactory adapterFactory, ResourceLocator resourceLocator, String displayName,
			String description, EStructuralFeature feature, boolean isSettable, boolean multiLine, boolean sortChoices, Object staticImage,
			String category, String[] filterFlags) {
		return new ExtendedItemPropertyDescriptor(adapterFactory, resourceLocator, displayName, description, feature, isSettable, multiLine,
				sortChoices, staticImage, category, filterFlags);
	}

	/*
	 * Overridden for delegating retrieval of reachable objects to EcoreTraveralHelper.
	 * @see org.eclipse.sphinx.emf.ecore.EcoreTraversalHelper
	 */
	public Collection<?> getChoiceOfValues(Object object, EReference[] parentReferences, EStructuralFeature feature) {
		if (object instanceof EObject) {
			EObject eObject = (EObject) object;
			if (parentReferences != null) {
				Collection<Object> result = new UniqueEList<Object>();
				for (EReference parentReference : parentReferences) {
					result.addAll(getTraversalHelper().getReachableEObjects(eObject, parentReference));
				}
				return result;
			} else if (feature != null) {
				if (feature instanceof EReference) {
					Collection<EObject> result = getTraversalHelper().getReachableEObjects(eObject, (EReference) feature);
					if (!feature.isMany() && !result.contains(null)) {
						result.add(null);
					}
					return result;
				} else if (feature.getEType() instanceof EEnum) {
					EEnum eEnum = (EEnum) feature.getEType();
					List<Enumerator> enumerators = new ArrayList<Enumerator>();
					for (EEnumLiteral eEnumLiteral : eEnum.getELiterals()) {
						enumerators.add(eEnumLiteral.getInstance());
					}
					return enumerators;
				} else {
					EDataType eDataType = (EDataType) feature.getEType();
					List<String> enumeration = ExtendedMetaData.INSTANCE.getEnumerationFacet(eDataType);
					if (!enumeration.isEmpty()) {
						List<Object> enumerators = new ArrayList<Object>();
						for (String enumerator : enumeration) {
							enumerators.add(EcoreUtil.createFromString(eDataType, enumerator));
						}
						return enumerators;
					} else {
						// Return enum constants from the EDataType's underlying Java enum type in case the latter is
						// such one
						Object[] javaEnumConstants = eDataType.getInstanceClass().getEnumConstants();
						if (javaEnumConstants != null) {
							return Arrays.asList(javaEnumConstants);
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the right traversal helper this item provider must use.
	 *
	 * @since 0.7.0
	 * @return The {@linkplain EcoreTraversalHelper traversal helper}
	 */
	protected EcoreTraversalHelper getTraversalHelper() {
		return new DefaultEcoreTraversalHelper();
	}

	/*
	 * Overridden to add support for the case where: (1) collection objects of given command parameter have different
	 * containers, or the same container but different features and (2) specified owner via CommandParameter
	 * (commandParameter.getEOwner()) is no EObject. In this case, try to refer to parent or owner of specified owner.
	 * @see
	 * org.eclipse.emf.edit.provider.ItemProviderAdapter#factorRemoveCommand(org.eclipse.emf.edit.domain.EditingDomain,
	 * org.eclipse.emf.edit.command.CommandParameter)
	 */
	@Override
	protected Command factorRemoveCommand(EditingDomain domain, CommandParameter commandParameter) {

		if (commandParameter.getCollection() == null || commandParameter.getCollection().isEmpty()) {
			return UnexecutableCommand.INSTANCE;
		}

		EObject eOwner = commandParameter.getEOwner();

		// Try to refer to parent or owner in case that owner specified via CommandParameter is no EObject
		if (eOwner == null) {
			Object parentOfOwner = domain.getParent(commandParameter.getOwner());
			if (parentOfOwner instanceof EObject) {
				eOwner = (EObject) parentOfOwner;
			}
		}

		final EObject eObject = eOwner;
		final List<Object> list = new ArrayList<Object>(commandParameter.getCollection());

		CompoundCommand removeCommand = new CompoundCommand(CompoundCommand.MERGE_COMMAND_ALL);

		// Iterator over all the child references to factor each child to the right reference.
		//
		for (EStructuralFeature feature : getChildrenFeatures(commandParameter.getOwner())) {
			// If it is a list type value...
			//
			if (feature.isMany()) {
				List<?> value = (List<?>) getFeatureValue(eObject, feature);

				// These will be the children belonging to this feature.
				//
				Collection<Object> childrenOfThisFeature = new ArrayList<Object>();
				for (ListIterator<Object> objects = list.listIterator(); objects.hasNext();) {
					Object o = objects.next();

					// Is this object in this feature...
					//
					if (value.contains(o)) {
						// Add it to the list and remove it from the other list.
						//
						childrenOfThisFeature.add(o);
						objects.remove();
					}
				}

				// If we have children to remove for this feature, create a command for it.
				//
				if (!childrenOfThisFeature.isEmpty()) {
					removeCommand.append(createRemoveCommand(domain, eObject, feature, childrenOfThisFeature));
				}
			} else {
				// It's just a single value
				//
				final Object value = getFeatureValue(eObject, feature);
				for (ListIterator<Object> objects = list.listIterator(); objects.hasNext();) {
					Object o = objects.next();

					// Is this object in this feature...
					//
					if (o == value) {
						// Create a command to unset this and remove the object from the other list.
						//
						Command setCommand = createSetCommand(domain, eObject, feature, SetCommand.UNSET_VALUE);
						removeCommand.append(new CommandWrapper(setCommand) {
							protected Collection<?> affected;

							@Override
							public void execute() {
								super.execute();
								affected = Collections.singleton(eObject);
							}

							@Override
							public void undo() {
								super.undo();
								affected = Collections.singleton(value);
							}

							@Override
							public void redo() {
								super.redo();
								affected = Collections.singleton(eObject);
							}

							@Override
							public Collection<?> getResult() {
								return Collections.singleton(value);
							}

							@Override
							public Collection<?> getAffectedObjects() {
								return affected;
							}
						});
						objects.remove();
						break;
					}
				}
			}
		}

		// If all the objects are used up by the above, then we can't do the command.
		//
		if (list.isEmpty()) {
			return removeCommand.unwrap();
		} else {
			// FIXME File bug to EMF: In the case where objects in the list have different container, or the same
			// container but different features we must to iterate over all container features
			for (Object object : new ArrayList<Object>(list)) {
				if (object instanceof EObject) {
					final EObject fallBackOwner = ((EObject) object).eContainer();
					EStructuralFeature containingFeature = ((EObject) object).eContainingFeature();
					if (containingFeature != null) {
						if (containingFeature.isMany()) {
							List<?> value = (List<?>) getFeatureValue(fallBackOwner, containingFeature);

							// These will be the children belonging to this feature.
							//
							Collection<Object> childrenOfThisFeature = new ArrayList<Object>();
							for (ListIterator<Object> objects = list.listIterator(); objects.hasNext();) {
								Object o = objects.next();

								// Is this object in this feature...
								//
								if (value.contains(o)) {
									// Add it to the list and remove it from the other list.
									//
									childrenOfThisFeature.add(o);
									objects.remove();
								}
							}

							// If we have children to remove for this feature, create a command for it.
							//
							if (!childrenOfThisFeature.isEmpty()) {
								removeCommand.append(createRemoveCommand(domain, fallBackOwner, containingFeature, childrenOfThisFeature));
							}

						} else {

							// It's just a single value
							//
							final Object value = getFeatureValue(fallBackOwner, containingFeature);
							for (ListIterator<Object> objects = list.listIterator(); objects.hasNext();) {
								Object o = objects.next();

								// Is this object in this feature...
								//
								if (o == value) {
									// Create a command to set this to null and remove the object from the other list.
									//
									Command setCommand = domain.createCommand(SetCommand.class, new CommandParameter(fallBackOwner,
											containingFeature, null));
									removeCommand.append(new CommandWrapper(setCommand) {
										protected Collection<?> affected;

										@Override
										public void execute() {
											affected = Collections.singleton(fallBackOwner.eContainer() != null ? fallBackOwner.eContainer()
													: fallBackOwner);
											super.execute();
										}

										@Override
										public void undo() {
											super.undo();
											affected = Collections.singleton(value);
										}

										@Override
										public void redo() {
											super.redo();
											affected = Collections.singleton(fallBackOwner.eContainer() != null ? fallBackOwner.eContainer()
													: fallBackOwner);
										}

										@Override
										public Collection<?> getResult() {
											return Collections.singleton(value);
										}

										@Override
										public Collection<?> getAffectedObjects() {
											return affected;
										}
									});
									objects.remove();
									break;
								}
							}
						}
					}
				}
			}
		}
		if (list.isEmpty()) {
			return removeCommand.unwrap();
		} else {
			removeCommand.dispose();
			return UnexecutableCommand.INSTANCE;
		}
	}

	/*
	 * Overridden to support the case where specified owner via CommandParameter (commandParameter.getEOwner()) is no
	 * EObject. In this case, try to refer to parent or owner of specified owner.
	 * @see
	 * org.eclipse.emf.edit.provider.ItemProviderAdapter#factorAddCommand(org.eclipse.emf.edit.domain.EditingDomain,
	 * org.eclipse.emf.edit.command.CommandParameter)
	 */
	@Override
	protected Command factorAddCommand(EditingDomain domain, CommandParameter commandParameter) {
		if (commandParameter.getCollection() == null || commandParameter.getCollection().isEmpty()) {
			return UnexecutableCommand.INSTANCE;
		}

		EObject eOwner = commandParameter.getEOwner();

		// Try to refer to parent or owner in case that owner specified via CommandParameter is no EObject
		if (eOwner == null) {
			Object parentOfOwner = domain.getParent(commandParameter.getOwner());
			if (parentOfOwner instanceof EObject) {
				eOwner = (EObject) parentOfOwner;
			}
		}

		final EObject eObject = eOwner;
		final List<Object> list = new ArrayList<Object>(commandParameter.getCollection());
		int index = commandParameter.getIndex();

		CompoundCommand addCommand = new CompoundCommand(CompoundCommand.MERGE_COMMAND_ALL);

		while (!list.isEmpty()) {
			Iterator<Object> children = list.listIterator();
			final Object firstChild = children.next();
			EStructuralFeature childFeature = getChildFeature(eObject, firstChild);

			if (childFeature == null) {
				break;
			}
			// If it is a list type value...
			//
			else if (childFeature.isMany()) {
				// Correct the index, if necessary.
				//
				if (index != CommandParameter.NO_INDEX) {
					for (EStructuralFeature feature : getChildrenFeatures(commandParameter.getOwner())) {
						if (feature == childFeature) {
							break;
						}

						if (feature.isMany()) {
							index -= ((List<?>) eObject.eGet(feature)).size();
						} else if (eObject.eGet(feature) != null) {
							index -= 1;
						}
					}
					if (index < 0) {
						break;
					}
				}

				// These will be the children belonging to this feature.
				//
				Collection<Object> childrenOfThisFeature = new ArrayList<Object>();
				childrenOfThisFeature.add(firstChild);
				children.remove();

				// Consume the rest of the appropriate children.
				//
				while (children.hasNext()) {
					Object child = children.next();

					// Is this child in this feature...
					//
					if (getChildFeature(eObject, child) == childFeature) {
						// Add it to the list and remove it from the other list.
						//
						childrenOfThisFeature.add(child);
						children.remove();
					}
				}

				// Create a command for this feature,
				//
				addCommand.append(createAddCommand(domain, eObject, childFeature, childrenOfThisFeature, index));

				if (index >= childrenOfThisFeature.size()) {
					index -= childrenOfThisFeature.size();
				} else {
					index = CommandParameter.NO_INDEX;
				}
			} else if (eObject.eGet(childFeature) == null) {
				Command setCommand = createSetCommand(domain, eObject, childFeature, firstChild);
				addCommand.append(new CommandWrapper(setCommand) {
					protected Collection<?> affected;

					@Override
					public void execute() {
						super.execute();
						affected = Collections.singleton(firstChild);
					}

					@Override
					public void undo() {
						super.undo();
						affected = Collections.singleton(eObject);
					}

					@Override
					public void redo() {
						super.redo();
						affected = Collections.singleton(firstChild);
					}

					@Override
					public Collection<?> getResult() {
						return Collections.singleton(firstChild);
					}

					@Override
					public Collection<?> getAffectedObjects() {
						return affected;
					}
				});
				children.remove();
			} else {
				break;
			}
		}

		// If all the objects aren't used up by the above, then we can't do the command.
		//
		if (list.isEmpty()) {
			return addCommand.unwrap();
		} else {
			addCommand.dispose();
			return UnexecutableCommand.INSTANCE;
		}
	}

	@Override
	protected Command createSetCommand(EditingDomain domain, EObject owner, EStructuralFeature feature, Object value, int index) {
		Command setCommand = CustomCommandRegistry.INSTANCE.createCustomCommand(domain, new CommandParameter(owner, feature, value, index),
				SetCommand.class);
		if (setCommand != null) {
			return setCommand;
		}
		return super.createSetCommand(domain, owner, feature, value, index);
	}

	@Override
	protected Command createAddCommand(EditingDomain domain, EObject owner, EStructuralFeature feature, Collection<?> collection, int index) {
		Command addCommand = CustomCommandRegistry.INSTANCE.createCustomCommand(domain, new CommandParameter(owner, feature, collection, index),
				AddCommand.class);
		if (addCommand != null) {
			return addCommand;
		}
		return super.createAddCommand(domain, owner, feature, collection, index);
	}

	@Override
	protected Command createRemoveCommand(EditingDomain domain, EObject owner, EStructuralFeature feature, Collection<?> collection) {
		Command removeCommand = CustomCommandRegistry.INSTANCE.createCustomCommand(domain, new CommandParameter(owner, feature, collection),
				RemoveCommand.class);
		if (removeCommand != null) {
			return removeCommand;
		}
		return super.createRemoveCommand(domain, owner, feature, collection);
	}

	/*
	 * Overridden for delegating to enhanced create child text API.
	 * @see org.eclipse.sphinx.emf.edit.ExtendedItemProviderAdapter.getCreateChildText(Object, Object, Object,
	 * Collection<?>, boolean)
	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#getCreateChildText(java.lang.Object, java.lang.Object,
	 * java.lang.Object, java.util.Collection)
	 */
	@Override
	public String getCreateChildText(Object owner, Object feature, Object child, Collection<?> selection) {
		return getCreateChildText(owner, feature, child, selection, false);
	}

	/**
	 * Returns the text to be used as label or text of {@link CreateChildCommand} or {@link ExtendedCreateChildCommand}.
	 * Enables the create child text to be retrieved in qualified form provided that the underlying metamodel's EMF Edit
	 * implementation includes supports for that (requires the metamodel's EMF Edit implementation to be generated with
	 * generator model option "Editor > Creation Sub-menus" set to true). Qualified create child texts are expected to
	 * consist of two segments separated by a vertical bar. The leading segment typically corresponds to the name of
	 * feature on the owner object that holds the child object and the trailing segment to the type of the child object
	 * to be created. They are leveraged by
	 * org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider#extractSubmenuActions(Collection<IAction>,
	 * ISelection) and
	 * org.eclipse.sphinx.emf.editors.forms.BasicTransactionalEditorActionBarContributor#extractSubmenuActions
	 * (Collection<IAction>, ISelection) so as to group the corresponding create child actions in submenus according to
	 * the qualified create child texts' leading segments.
	 * <p>
	 * Qualified create child texts are requested by {@link ExtendedCreateChildCommand#getText()} and returned to
	 * {@link CreateChildAction#configureAction(ISelection)} so as to make their {@link CreateChildAction#getText()
	 * action text}s qualified and prepare them for being grouped in creation submenus. Simple create child action texts
	 * are requested by {@link CreateChildCommand#CreateChildCommand(EditingDomain, EObject, EStructuralFeature, Object,
	 * int, Collection<?>, Helper)} and used to initialize the {@link CreateChildCommand#getLabel() command label} by
	 * which the command is displayed in any location outside the creation menus or submenus (e.g., undo and redo
	 * menus).
	 * </p>
	 *
	 * @param owner
	 *            The <code>owner</code> object to which the new <code>child</code> object will be added.
	 * @param feature
	 *            The <code>feature</code> of the <code>owner</code> object that is going to hold the new
	 *            <code>child</code> object.
	 * @param child
	 *            The new, i.e., still unassigned <code>child</code> object that will be added to the <code>owner</code>
	 *            object.
	 * @param qualified
	 *            <code>true</code> if a qualified create child text should be returned for metamodels whose EMF Edit
	 *            implementation support that, <code>false</code> otherwise.
	 * @return The qualified or simple text to be used as label or text of {@link CreateChildCommand} or
	 *         {@link ExtendedCreateChildCommand}.
	 */
	public String getCreateChildText(Object owner, Object feature, Object child, Collection<?> selection, boolean qualified) {
		if (feature instanceof EStructuralFeature && FeatureMapUtil.isFeatureMap((EStructuralFeature) feature)) {
			FeatureMap.Entry entry = (FeatureMap.Entry) child;
			feature = entry.getEStructuralFeature();
			child = entry.getValue();
		}

		String featureText = getFeatureText(feature);
		String childTypeText = feature instanceof EAttribute ? getTypeText((EAttribute) feature) : getTypeText(child);

		// Attribute feature?
		if (feature instanceof EAttribute) {
			// Use feature name as action text
			return getResourceLocator().getString("_UI_CreateChild_text3", //$NON-NLS-1$
					new Object[] { childTypeText, featureText });
		}

		// Reference feature whose name is different from target type
		else if (!childTypeText.equals(featureText) && qualified) {
			// The try/catch provides backwards compatibility with metamodels whose Edit support has no
			// extra _UI_CreateChild_text1 key
			try {
				// Use combination of feature name and target type name as action text
				return getResourceLocator().getString("_UI_CreateChild_text1", //$NON-NLS-1$
						new Object[] { childTypeText, featureText });
			} catch (MissingResourceException e) {
				return getResourceLocator().getString("_UI_CreateChild_text", //$NON-NLS-1$
						new Object[] { childTypeText, featureText });
			}
		}

		// Reference feature whose name is equal to target type
		else {
			// Use only target type name as action text
			return getResourceLocator().getString("_UI_CreateChild_text", //$NON-NLS-1$
					new Object[] { childTypeText, featureText });
		}
	}

	/*
	 * Overriden to avoid NullPointerException upon retrieval of image for XMLTypeDocumentRoot#processingInstruction
	 * entry in mixed attributes.
	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#getCreateChildImage(java.lang.Object, java.lang.Object,
	 * java.lang.Object, java.util.Collection)
	 */
	@Override
	public Object getCreateChildImage(Object owner, Object feature, Object child, Collection<?> selection) {
		// FIXME File bug to EMF: NPE is raised on attempt to retrieve image for
		// XMLTypeDocumentRoot#processingInstruction entry in mixed attributes
		try {
			return super.getCreateChildImage(owner, feature, child, selection);
		} catch (NullPointerException ex) {
			// Ignore exception
		}
		return EMFEditPlugin.INSTANCE.getImage("full/ctool16/CreateChild"); //$NON-NLS-1$
	}

	@Override
	protected Command createCreateChildCommand(EditingDomain domain, EObject owner, EStructuralFeature feature, Object value, int index,
			Collection<?> collection) {
		return new ExtendedCreateChildCommand(domain, owner, feature, value, index, collection, this);
	}

	/*
	 * Overridden to add an exclamation mark as overlay to icons of EObjects that are proxies so as to facilitate their
	 * identification in the UI.
	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#overlayImage(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected Object overlayImage(Object object, Object image) {
		if (object instanceof EObject && ((EObject) object).eIsProxy()) {
			List<Object> images = new ArrayList<Object>(2);
			images.add(image);
			images.add(Activator.INSTANCE.getImage("full/ovr16/exclampt_ovr")); //$NON-NLS-1$
			image = new ComposedImage(images);
			return image;
		}

		return super.overlayImage(object, image);
	}
}

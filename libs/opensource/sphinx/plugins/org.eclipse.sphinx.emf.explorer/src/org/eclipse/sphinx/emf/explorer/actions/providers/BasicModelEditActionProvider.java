/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.explorer.actions.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.ui.action.CopyAction;
import org.eclipse.emf.edit.ui.action.CreateChildAction;
import org.eclipse.emf.edit.ui.action.CreateSiblingAction;
import org.eclipse.emf.edit.ui.action.CutAction;
import org.eclipse.emf.edit.ui.action.DeleteAction;
import org.eclipse.emf.edit.ui.action.PasteAction;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.explorer.actions.filters.BasicCommandParameterFilter;
import org.eclipse.sphinx.emf.explorer.actions.filters.ICommandParameterFilter;
import org.eclipse.sphinx.emf.ui.actions.ExtendedCopyAction;
import org.eclipse.sphinx.emf.ui.actions.ExtendedCutAction;
import org.eclipse.sphinx.emf.ui.actions.ExtendedDeleteAction;
import org.eclipse.sphinx.emf.ui.actions.ExtendedPasteAction;
import org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.navigator.ICommonMenuConstants;

public class BasicModelEditActionProvider extends BasicActionProvider {

	/**
	 * This is the action used to implement cut.
	 */
	protected CutAction cutAction;

	/**
	 * This is the action used to implement copy.
	 */
	protected CopyAction copyAction;

	/**
	 * This is the action used to implement paste.
	 */
	protected PasteAction pasteAction;

	/**
	 * This is the action used to implement delete.
	 */
	protected DeleteAction deleteAction;

	/**
	 * This will contain one {@link org.eclipse.emf.edit.ui.action.CreateChildAction} corresponding to each descriptor
	 * generated for the current selection by the item provider.
	 */
	protected Collection<IAction> createChildActions;

	/**
	 * This will contain a map of {@link org.eclipse.emf.edit.ui.action.CreateChildAction}s, keyed by sub-menu text.
	 */
	protected Map<String, Collection<IAction>> createChildSubmenuActions;

	/**
	 * This will contain one {@link org.eclipse.emf.edit.ui.action.CreateSiblingAction} corresponding to each descriptor
	 * generated for the current selection by the item provider.
	 */
	protected Collection<IAction> createSiblingActions;

	/**
	 * This will contain a map of {@link org.eclipse.emf.edit.ui.action.CreateSiblingAction}s, keyed by submenu text.
	 */
	protected Map<String, Collection<IAction>> createSiblingSubmenuActions;

	private ICommandParameterFilter newChildOrSiblingItemFilter;

	@Override
	public void doInit() {
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();

		cutAction = createCutAction();
		Assert.isNotNull(cutAction);
		cutAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		cutAction.setActiveWorkbenchPart(workbenchPart);
		cutAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_CUT);

		copyAction = createCopyAction();
		Assert.isNotNull(copyAction);
		copyAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		copyAction.setActiveWorkbenchPart(workbenchPart);
		copyAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);

		pasteAction = createPasteAction();
		Assert.isNotNull(pasteAction);
		pasteAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		pasteAction.setActiveWorkbenchPart(workbenchPart);
		pasteAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_PASTE);

		deleteAction = createDeleteAction();
		Assert.isNotNull(deleteAction);
		deleteAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		deleteAction.setActiveWorkbenchPart(workbenchPart);
		deleteAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_DELETE);
	}

	@Override
	public void fillContextMenu(IMenuManager menuManager) {
		super.fillContextMenu(menuManager);
		updateActions(getContext().getSelection());

		// Add New Child sub menu
		MenuManager createChildMenuManager = new MenuManager("New Child"); //$NON-NLS-1$
		populateManager(createChildMenuManager, createChildSubmenuActions, null);
		populateManager(createChildMenuManager, createChildActions, null);
		menuManager.appendToGroup(ICommonMenuConstants.GROUP_NEW, createChildMenuManager);

		// Add New Sibling sub menu
		MenuManager createSiblingMenuManager = new MenuManager("New Sibling"); //$NON-NLS-1$
		populateManager(createSiblingMenuManager, createSiblingSubmenuActions, null);
		populateManager(createSiblingMenuManager, createSiblingActions, null);
		menuManager.appendToGroup(ICommonMenuConstants.GROUP_NEW, createSiblingMenuManager);

		// Add the edit menu actions
		menuManager.appendToGroup(ICommonMenuConstants.GROUP_EDIT, new ActionContributionItem(cutAction));
		menuManager.appendToGroup(ICommonMenuConstants.GROUP_EDIT, new ActionContributionItem(copyAction));
		menuManager.appendToGroup(ICommonMenuConstants.GROUP_EDIT, new ActionContributionItem(pasteAction));
		menuManager.appendToGroup(ICommonMenuConstants.GROUP_EDIT, new ActionContributionItem(deleteAction));
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);

		// Propagate new selection to actions
		updateActions(getContext().getSelection());

		// Redirect retargetable actions
		if (!isActivePropertySheet()) {
			actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), cutAction);
			actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
			actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), pasteAction);
			actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (cutAction != null) {
			cutAction.setEditingDomain(null);
		}
		if (copyAction != null) {
			copyAction.setEditingDomain(null);
		}
		if (pasteAction != null) {
			pasteAction.setEditingDomain(null);
		}
		if (deleteAction != null) {
			deleteAction.setEditingDomain(null);
		}
	}

	protected void updateActions(ISelection selection) {
		// Switch actions to editing domain behind current selection
		TransactionalEditingDomain editingDomain = getEditingDomainFromSelection(selection);
		cutAction.setEditingDomain(editingDomain);
		copyAction.setEditingDomain(editingDomain);
		pasteAction.setEditingDomain(editingDomain);
		deleteAction.setEditingDomain(editingDomain);

		// Update action states according to current selection
		IStructuredSelection structuredSelection = SelectionUtil.getStructuredSelection(selection);
		cutAction.selectionChanged(structuredSelection);
		copyAction.selectionChanged(structuredSelection);
		pasteAction.selectionChanged(structuredSelection);
		deleteAction.selectionChanged(structuredSelection);

		// Query new child/sibling descriptors for current selection
		/*
		 * !! Important Note !! This need to be redone upon each time the context menu is about to be shown (but not
		 * only when the selection changes). The new child/sibling descriptors already contain the new objects to be
		 * added as new child/sibling to the selected object when the corresponding new child/sibling action gets
		 * invoked. These new objects need to be brand new instances in each context menu instance so as to make sure
		 * that users can successively create multiple new children/siblings with the same type for same selected object
		 * and feature. If new child/sibling descriptors were only recreated upon selection change it could happen that
		 * users end up with the same new child/sibling object instance being added again and again instead.
		 */
		if (shouldCreateCreateChildActions(structuredSelection.getFirstElement())) {
			Collection<?> newChildDescriptors = null;
			Collection<?> newSiblingDescriptors = null;

			if (editingDomain != null && structuredSelection.size() == 1) {
				newChildDescriptors = getNewChildDescriptors(editingDomain, structuredSelection.getFirstElement(), null);
				newSiblingDescriptors = getNewChildDescriptors(editingDomain, null, structuredSelection.getFirstElement());
			}

			// Generate new create child/sibling actions
			createChildActions = generateCreateChildActions(editingDomain, newChildDescriptors, selection);
			createChildSubmenuActions = extractSubmenuActions(createChildActions, selection);
			createSiblingActions = generateCreateSiblingActions(editingDomain, newSiblingDescriptors, selection);
			createSiblingSubmenuActions = extractSubmenuActions(createSiblingActions, selection);
		}
	}

	/**
	 * Returns if create child/sibling menus and actions should be shown for the given object.
	 * <p>
	 * This default implementation returns always <code>true</code>. It may be overridden by subclasses as appropriate.
	 * </p>
	 * 
	 * @param object
	 *            The object under investigation.
	 * @return <code>true</code> if create child/sibling menus and actions should be shown for the given object,
	 *         <code>false</code> otherwise.
	 */
	protected boolean shouldCreateCreateChildActions(Object object) {
		return true;
	}

	protected DeleteAction createDeleteAction() {
		return new ExtendedDeleteAction(removeAllReferencesOnDelete(), getCustomAdapterFactory());
	}

	protected PasteAction createPasteAction() {
		return new ExtendedPasteAction(getCustomAdapterFactory());
	}

	protected CopyAction createCopyAction() {
		return new ExtendedCopyAction(getCustomAdapterFactory());
	}

	protected CutAction createCutAction() {
		return new ExtendedCutAction(getCustomAdapterFactory());
	}

	protected boolean removeAllReferencesOnDelete() {
		return true;
	}

	/**
	 * Returns descriptors for all the possible children that can be added to the specified <code>object</code>. For
	 * that purpose, it adapts the given <code>object</code> to {@link IEditingDomainItemProvider} and delegates the
	 * actual calculation of possible child descriptors to
	 * {@link IEditingDomainItemProvider#getNewChildDescriptors(Object, EditingDomain, Object)}.
	 * <p>
	 * The {@link AdapterFactory adapter factory} required for adapting given <code>object</code> to
	 * {@link IEditingDomainItemProvider} is retrieved by invoking
	 * {@link #getAdapterFactory(TransactionalEditingDomain)} which returns the {@link AdapterFactory adapter factory}
	 * behind given <code>editingDomain</code> by default. Clients which want the calculation of possible child
	 * descriptors to be based on {@link IEditingDomainItemProvider}s from a custom {@link AdapterFactory adapter
	 * factory} instead can override {@link #getCustomAdapterFactory()} and return any {@link AdapterFactory adapter
	 * factory} of their choice. This custom {@link AdapterFactory adapter factory} will then be the result returned by
	 * {@link #getAdapterFactory(TransactionalEditingDomain)} and consequently also used by this method.
	 * <p>
	 * 
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} behind given <code>object</code>.
	 * @param object
	 *            The <code>object</code> to return the new child descriptors for.
	 * @param sibling
	 *            If <code>sibling</code> is non-null, an index is added to each new child descriptor with a
	 *            multi-valued feature, to ensure that the new child object gets added in the right position.
	 * @return A collection of new child descriptors for given <code>object</code>
	 * @see #getAdapterFactory(TransactionalEditingDomain)
	 * @see #getCustomAdapterFactory()
	 */
	protected Collection<?> getNewChildDescriptors(TransactionalEditingDomain editingDomain, Object object, Object sibling) {
		AdapterFactory adapterFactory = getAdapterFactory(editingDomain);
		if (adapterFactory != null) {
			IEditingDomainItemProvider editingDomainItemProvider = (IEditingDomainItemProvider) adapterFactory.adapt(object,
					IEditingDomainItemProvider.class);
			if (editingDomainItemProvider != null) {
				return editingDomainItemProvider.getNewChildDescriptors(object, editingDomain, sibling);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Returns the {@link AdapterFactory adapter factory} to be used by this {@link BasicModelEditActionProvider action
	 * provider} for creating {@link ItemProviderAdapter item provider}s which control the way how {@link EObject model
	 * element}s from given <code>editingDomain</code> are displayed and can be edited.
	 * <p>
	 * This implementation returns the {@link AdapterFactory adapter factory} which is embedded in the given
	 * <code>editingDomain</code> by default. Clients which want to use an alternative {@link AdapterFactory adapter
	 * factory} (e.g., an {@link AdapterFactory adapter factory} that creates {@link ItemProviderAdapter item provider}s
	 * which are specifically designed for the {@link IEditorPart editor} in which this
	 * {@link BasicModelEditActionProvider action provider} is used) may override {@link #getCustomAdapterFactory()} and
	 * return any {@link AdapterFactory adapter factory} of their choice. This custom {@link AdapterFactory adapter
	 * factory} will then be returned as result by this method.
	 * </p>
	 * 
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} whose embedded {@link AdapterFactory adapter
	 *            factory} is to be returned as default. May be left <code>null</code> if
	 *            {@link #getCustomAdapterFactory()} has been overridden and returns a non-<code>null</code> result.
	 * @return The {@link AdapterFactory adapter factory} that will be used by this {@link BasicModelEditActionProvider
	 *         action provider}. <code>null</code> if no custom {@link AdapterFactory adapter factory} is provided
	 *         through {@link #getCustomAdapterFactory()} and no <code>editingDomain</code> has been specified.
	 * @see #getCustomAdapterFactory()
	 */
	protected AdapterFactory getAdapterFactory(TransactionalEditingDomain editingDomain) {
		AdapterFactory customAdapterFactory = getCustomAdapterFactory();
		if (customAdapterFactory != null) {
			return customAdapterFactory;
		} else if (editingDomain != null) {
			return ((AdapterFactoryEditingDomain) editingDomain).getAdapterFactory();
		}
		return null;
	}

	/**
	 * Returns a custom {@link AdapterFactory adapter factory} to be used by this {@link BasicModelEditActionProvider
	 * action provider} for creating {@link ItemProviderAdapter item provider}s which control the way how
	 * {@link EObject model element}s from given <code>editingDomain</code> are displayed and can be edited.
	 * <p>
	 * This implementation returns <code>null</code> as default. Clients which want to use their own
	 * {@link AdapterFactory adapter factory} (e.g., an {@link AdapterFactory adapter factory} that creates
	 * {@link ItemProviderAdapter item provider}s which are specifically designed for the {@link IEditorPart editor} in
	 * which this {@link BasicModelEditActionProvider action provider} is used) may override this method and return any
	 * {@link AdapterFactory adapter factory} of their choice. This custom {@link AdapterFactory adapter factory} will
	 * then be returned as result by {@link #getAdapterFactory(TransactionalEditingDomain)}.
	 * </p>
	 * 
	 * @return The custom {@link AdapterFactory adapter factory} that is to be used by this
	 *         {@link BasicModelEditActionProvider action provider}. <code>null</code> the default
	 *         {@link AdapterFactory adapter factory} returned by {@link #getAdapterFactory(TransactionalEditingDomain)}
	 *         should be used instead.
	 * @see #getAdapterFactory(TransactionalEditingDomain)
	 */
	protected AdapterFactory getCustomAdapterFactory() {
		return null;
	}

	/**
	 * This generates a {@link org.eclipse.emf.edit.ui.action.CreateChildAction} for each object in
	 * <code>descriptors</code>, and returns the collection of these actions.
	 */
	protected Collection<IAction> generateCreateChildActions(TransactionalEditingDomain editingDomain, Collection<?> descriptors, ISelection selection) {
		List<IAction> actions = new ArrayList<IAction>();
		if (descriptors != null && selection instanceof IStructuredSelection) {
			for (Object descriptor : descriptors) {
				if (descriptor instanceof CommandParameter
						&& getNewChildOrSiblingItemFilter().accept((IStructuredSelection) selection, (CommandParameter) descriptor)) {
					actions.add(createCreateChildAction(editingDomain, selection, descriptor));
				}
			}
			Collections.sort(actions, new Comparator<IAction>() {
				@Override
				public int compare(IAction a1, IAction a2) {
					if (a1.getText() == null && a2.getText() != null) {
						return -1;
					} else if (a1.getText() == null && a2.getText() == null) {
						return 0;
					} else if (a1.getText() != null && a2.getText() == null) {
						return 1;
					} else {
						return CommonPlugin.INSTANCE.getComparator().compare(a1.getText(), a2.getText());
					}
				}
			});
		}
		return actions;
	}

	/**
	 * This generates a {@link org.eclipse.emf.edit.ui.action.CreateSiblingAction} for each object in
	 * <code>descriptors</code>, and returns the collection of these actions.
	 */
	protected Collection<IAction> generateCreateSiblingActions(TransactionalEditingDomain editingDomain, Collection<?> descriptors,
			ISelection selection) {
		List<IAction> actions = new ArrayList<IAction>();
		if (descriptors != null && selection instanceof IStructuredSelection) {
			for (Object descriptor : descriptors) {
				if (descriptor instanceof CommandParameter
						&& getNewChildOrSiblingItemFilter().accept((IStructuredSelection) selection, (CommandParameter) descriptor)) {
					actions.add(createCreateSiblingAction(editingDomain, selection, descriptor));
				}
			}
			Collections.sort(actions, new Comparator<IAction>() {
				@Override
				public int compare(IAction a1, IAction a2) {
					if (a1.getText() == null && a2.getText() != null) {
						return -1;
					} else if (a1.getText() == null && a2.getText() == null) {
						return 0;
					} else if (a1.getText() != null && a2.getText() == null) {
						return 1;
					} else {
						return CommonPlugin.INSTANCE.getComparator().compare(a1.getText(), a2.getText());
					}
				}
			});
		}
		return actions;
	}

	protected ICommandParameterFilter getNewChildOrSiblingItemFilter() {
		if (newChildOrSiblingItemFilter == null) {
			newChildOrSiblingItemFilter = createNewChildOrSiblingItemFilter();
		}
		return newChildOrSiblingItemFilter;
	}

	protected ICommandParameterFilter createNewChildOrSiblingItemFilter() {
		return new BasicCommandParameterFilter();
	}

	protected CreateChildAction createCreateChildAction(TransactionalEditingDomain editingDomain, ISelection selection, Object descriptor) {
		return new CreateChildAction(editingDomain, selection, descriptor);
	}

	protected IAction createCreateSiblingAction(TransactionalEditingDomain editingDomain, ISelection selection, Object descriptor) {
		return new CreateSiblingAction(editingDomain, selection, descriptor);
	}
}
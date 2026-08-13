/**
 * <copyright>
 * 
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [406298] Sphinx example editors should offer 'Show Properties View' action in context menu
 *     
 * </copyright>
 */
package org.eclipse.sphinx.emf.editors.forms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.ui.viewer.IViewerProvider;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.ui.action.CopyAction;
import org.eclipse.emf.edit.ui.action.CreateChildAction;
import org.eclipse.emf.edit.ui.action.CreateSiblingAction;
import org.eclipse.emf.edit.ui.action.CutAction;
import org.eclipse.emf.edit.ui.action.DeleteAction;
import org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor;
import org.eclipse.emf.edit.ui.action.PasteAction;
import org.eclipse.emf.edit.ui.action.RedoAction;
import org.eclipse.emf.edit.ui.action.UndoAction;
import org.eclipse.emf.edit.ui.action.ValidateAction;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.ui.actions.RedoActionWrapper;
import org.eclipse.emf.workspace.ui.actions.UndoActionWrapper;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.editors.forms.internal.Activator;
import org.eclipse.sphinx.emf.ui.actions.ExtendedCopyAction;
import org.eclipse.sphinx.emf.ui.actions.ExtendedCutAction;
import org.eclipse.sphinx.emf.ui.actions.ExtendedDeleteAction;
import org.eclipse.sphinx.emf.ui.actions.ExtendedPasteAction;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/**
 * This is the action bar contributor for the Generic Eclipse Forms-based model editor. It is customized from its
 * EMF-generated form to provide alternative implementations of the undo and redo menu actions (delegating to the
 * operation history) and additional example actions such as initiation of long-running background reader jobs.
 */
public class BasicTransactionalEditorActionBarContributor extends EditingDomainActionBarContributor implements ISelectionChangedListener {

	public static final String VIEW_POINT = "org.eclipse.sphinx.emf.editors.forms.actionBarValidateAction"; //$NON-NLS-1$

	/**
	 * The validateAction node name for a configuration element.
	 * <p>
	 * Equal to the word: <code>validateAction</code>
	 * </p>
	 */
	public static final String VALIDATEACTION = "validateAction"; //$NON-NLS-1$

	/**
	 * The class node name for a configuration element.
	 * <p>
	 * Equal to the word: <code>class</code>
	 * </p>
	 */
	public static final String CLASS_ATTR = "class"; //$NON-NLS-1$

	private static String getStringFromKey(String key) {
		try {
			return Activator.INSTANCE.getString(key);
		} catch (MissingResourceException ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
		return key + " (not found)"; //$NON-NLS-1$
	}

	public class RefreshAction extends Action {

		public RefreshAction() {
			super(getStringFromKey("_UI_Refresh_menu_item")); //$NON-NLS-1$
		}

		@Override
		public boolean isEnabled() {
			return activeEditor instanceof IViewerProvider;
		}

		@Override
		public void run() {
			if (activeEditor instanceof IViewerProvider) {
				Viewer viewer = ((IViewerProvider) activeEditor).getViewer();
				if (viewer != null) {
					viewer.refresh();
				}
			}
		}
	};

	public class ShowPropertiesViewAction extends Action {

		public ShowPropertiesViewAction() {
			super(getStringFromKey("_UI_ShowPropertiesView_menu_item")); //$NON-NLS-1$
		}

		@Override
		public void run() {
			try {
				getPage().showView("org.eclipse.ui.views.PropertySheet"); //$NON-NLS-1$
			} catch (PartInitException exception) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), exception);
			}
		}
	};

	/**
	 * This keeps track of the current selection provider.
	 */
	protected ISelectionProvider selectionProvider;

	/**
	 * This action opens the Properties view.
	 */
	protected IAction showPropertiesViewAction;

	/**
	 * This action refreshes the viewer of the current editor if the editor implements
	 * {@link org.eclipse.emf.common.ui.viewer.IViewerProvider}.
	 */
	protected IAction refreshAction;

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
	 * This is the menu manager into which menu contribution items should be added for CreateChild actions.
	 */
	protected IMenuManager createChildMenuManager;

	/**
	 * This will contain one {@link org.eclipse.emf.edit.ui.action.CreateSiblingAction} corresponding to each descriptor
	 * generated for the current selection by the item provider.
	 */
	protected Collection<IAction> createSiblingActions;

	/**
	 * This will contain a map of {@link org.eclipse.emf.edit.ui.action.CreateSiblingAction}s, keyed by submenu text.
	 */
	protected Map<String, Collection<IAction>> createSiblingSubmenuActions;

	/**
	 * This is the menu manager into which menu contribution items should be added for CreateSibling actions.
	 */
	protected IMenuManager createSiblingMenuManager;

	/**
	 * This creates an instance of the contributor.
	 */
	public BasicTransactionalEditorActionBarContributor() {
		super(ADDITIONS_LAST_STYLE);
		showPropertiesViewAction = new ShowPropertiesViewAction();
	}

	/**
	 * Sets the global action handler for the action contributed by this
	 * {@link BasicTransactionalEditorActionBarContributor}.
	 */
	public void setGlobalActionHandlers() {
		getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
		getActionBars().setGlobalActionHandler(ActionFactory.CUT.getId(), cutAction);
		getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
		getActionBars().setGlobalActionHandler(ActionFactory.PASTE.getId(), pasteAction);
		getActionBars().setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);
		getActionBars().setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);
	}

	/**
	 * Clears the global action handler list.
	 */
	public void clearGlobalActionHandlers() {
		getActionBars().clearGlobalActionHandlers();
	}

	@Override
	public void init(IActionBars actionBars) {
		super.init(actionBars);
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		deleteAction = createDeleteAction();
		deleteAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		deleteAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_DELETE);

		cutAction = createCutAction();
		cutAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		cutAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_CUT);

		copyAction = createCopyAction();
		copyAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		copyAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);

		pasteAction = createPasteAction();
		pasteAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		pasteAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_PASTE);

		undoAction = createUndoAction();
		undoAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
		undoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_UNDO);

		redoAction = createRedoAction();
		redoAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
		redoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_REDO);

		setGlobalActionHandlers();

		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(VIEW_POINT);
		for (IConfigurationElement cfgElem : extension.getConfigurationElements()) {
			if (VALIDATEACTION.equals(cfgElem.getName())) {
				try {
					validateAction = (ValidateAction) cfgElem.createExecutableExtension(CLASS_ATTR);
				} catch (CoreException ex) {
					PlatformLogUtil.logAsError(Activator.getPlugin(), ex.getMessage());
				}
			}
		}
	}

	@Override
	protected DeleteAction createDeleteAction() {
		return new ExtendedDeleteAction(removeAllReferencesOnDelete(), getCustomAdapterFactory());
	}

	@Override
	protected PasteAction createPasteAction() {
		return new ExtendedPasteAction(getCustomAdapterFactory());
	}

	@Override
	protected CopyAction createCopyAction() {
		return new ExtendedCopyAction(getCustomAdapterFactory());
	}

	@Override
	protected CutAction createCutAction() {
		return new ExtendedCutAction(getCustomAdapterFactory());
	}

	@Override
	protected RedoAction createRedoAction() {
		return new RedoActionWrapper();
	}

	@Override
	protected UndoAction createUndoAction() {
		return new UndoActionWrapper();
	}

	/**
	 * Determines whether or not the delete action should clean up all references to the deleted objects.
	 * <p>
	 * This implementation returns <code>true</code> by default. Clients may override this method and return
	 * <code>false</code> if appropriate.
	 * </p>
	 * 
	 * @return <code>true</code> if delete action should clean up all references to deleted model objects,
	 *         <code>false</code> otherwise.
	 * @since 0.7.0
	 */
	@Override
	protected boolean removeAllReferencesOnDelete() {
		return true;
	}

	/**
	 * This adds to the menu bar a menu and some separators for editor additions, as well as the sub-menus for object
	 * creation items.
	 */
	@Override
	public void contributeToMenu(IMenuManager menuManager) {
		super.contributeToMenu(menuManager);

		IMenuManager submenuManager = new MenuManager(getStringFromKey("_UI_Editor_menu")); //$NON-NLS-1$
		menuManager.insertAfter("additions", submenuManager); //$NON-NLS-1$
		submenuManager.add(new Separator("settings")); //$NON-NLS-1$
		submenuManager.add(new Separator("actions")); //$NON-NLS-1$
		submenuManager.add(new Separator("additions")); //$NON-NLS-1$
		submenuManager.add(new Separator("additions-end")); //$NON-NLS-1$

		// Prepare for CreateChild item addition or removal.
		//
		createChildMenuManager = new MenuManager(getStringFromKey("_UI_CreateChild_menu_item")); //$NON-NLS-1$
		submenuManager.insertBefore("additions", createChildMenuManager); //$NON-NLS-1$

		// Prepare for CreateSibling item addition or removal.
		//
		createSiblingMenuManager = new MenuManager(getStringFromKey("_UI_CreateSibling_menu_item")); //$NON-NLS-1$
		submenuManager.insertBefore("additions", createSiblingMenuManager); //$NON-NLS-1$

		// Force an update because Eclipse hides empty menus now.
		//
		submenuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager menuManager) {
				menuManager.updateAll(true);
			}
		});

		addGlobalActions(submenuManager);
	}

	/**
	 * When the active editor changes, this remembers the change and registers with it as a selection provider.
	 */
	@Override
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);

		// Switch to the new selection provider.
		//
		if (selectionProvider != null) {
			selectionProvider.removeSelectionChangedListener(this);
		}
		if (part == null) {
			selectionProvider = null;
		} else {
			selectionProvider = part.getSite().getSelectionProvider();
			if (selectionProvider != null) {
				selectionProvider.addSelectionChangedListener(this);

				// Fake a selection changed event to update the menus.
				//
				if (selectionProvider.getSelection() != null) {
					selectionChanged(new SelectionChangedEvent(selectionProvider, selectionProvider.getSelection()));
				}
			}
		}
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionChangedListener}, handling
	 * {@link org.eclipse.jface.viewers.SelectionChangedEvent}s by querying for the children and siblings that can be
	 * added to the selected object and updating the menus accordingly.
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		IStructuredSelection structuredSelection = SelectionUtil.getStructuredSelection(selection);

		// Make sure that editor input object cannot be removed from within underlying editor
		List<Object> nonEditorInputObjects = new ArrayList<Object>();
		for (Object selected : structuredSelection.toList()) {
			if (activeEditor instanceof BasicTransactionalFormEditor) {
				BasicTransactionalFormEditor activeFormEditor = (BasicTransactionalFormEditor) activeEditor;
				if (selected != activeFormEditor.getEditorInputObject()) {
					nonEditorInputObjects.add(selected);
				}
			}
		}
		IStructuredSelection nonModelRootSelection = new StructuredSelection(nonEditorInputObjects);
		deleteAction.selectionChanged(nonModelRootSelection);
		cutAction.selectionChanged(nonModelRootSelection);

		// Remove any menu items for old selection.
		//
		if (createChildMenuManager != null) {
			depopulateManager(createChildMenuManager, createChildSubmenuActions);
			depopulateManager(createChildMenuManager, createChildActions);
		}
		if (createSiblingMenuManager != null) {
			depopulateManager(createSiblingMenuManager, createSiblingSubmenuActions);
			depopulateManager(createSiblingMenuManager, createSiblingActions);
		}

		// Query the new selection for appropriate new child/sibling descriptors
		//
		Collection<?> newChildDescriptors = null;
		Collection<?> newSiblingDescriptors = null;

		if (structuredSelection.size() == 1) {
			Object object = ((IStructuredSelection) selection).getFirstElement();

			EditingDomain domain = ((IEditingDomainProvider) activeEditor).getEditingDomain();
			if (domain instanceof TransactionalEditingDomain) {
				newChildDescriptors = getNewChildDescriptors((TransactionalEditingDomain) domain, object, null);
				newSiblingDescriptors = getNewChildDescriptors((TransactionalEditingDomain) domain, null, object);
			}
		}

		// Generate actions for selection; populate and redraw the menus.
		//
		createChildActions = generateCreateChildActions(newChildDescriptors, selection);
		createChildSubmenuActions = extractSubmenuActions(createChildActions, selection);
		createSiblingActions = generateCreateSiblingActions(newSiblingDescriptors, selection);
		createSiblingSubmenuActions = extractSubmenuActions(createSiblingActions, selection);

		if (createChildMenuManager != null) {
			populateManager(createChildMenuManager, createChildSubmenuActions, null);
			populateManager(createChildMenuManager, createChildActions, null);
			createChildMenuManager.update(true);
		}
		if (createSiblingMenuManager != null) {
			populateManager(createSiblingMenuManager, createSiblingSubmenuActions, null);
			populateManager(createSiblingMenuManager, createSiblingActions, null);
			createSiblingMenuManager.update(true);
		}
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
	 * descriptors to be based on {@link IEditingDomainItemProvider}s from an alternative {@link AdapterFactory adapter
	 * factory} instead may override {@link #getCustomAdapterFactory()} and return any {@link AdapterFactory adapter
	 * factory} of their choice. This custom {@link AdapterFactory adapter factory} will then be returned as result by
	 * {@link #getAdapterFactory(TransactionalEditingDomain)} and consequently also be used by this method.
	 * <p>
	 * 
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} behind given <code>object</code>.
	 * @param object
	 *            The <code>object</code> for which the new child descriptors are to be returned.
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
	 * Returns the {@link AdapterFactory adapter factory} to be used by this
	 * {@link BasicTransactionalEditorActionBarContributor action bar contributor} for creating
	 * {@link ItemProviderAdapter item provider}s which control the way how {@link EObject model element}s from given
	 * <code>editingDomain</code> are displayed and can be edited.
	 * <p>
	 * This implementation returns the {@link AdapterFactory adapter factory} which is embedded in the given
	 * <code>editingDomain</code> by default. Clients which want to use an alternative {@link AdapterFactory adapter
	 * factory} (e.g., an {@link AdapterFactory adapter factory} that creates {@link ItemProviderAdapter item provider}s
	 * which are specifically designed for the {@link IEditorPart editor} in which this
	 * {@link BasicTransactionalEditorActionBarContributor action bar contributor} is used) may override
	 * {@link #getCustomAdapterFactory()} and return any {@link AdapterFactory adapter factory} of their choice. This
	 * custom {@link AdapterFactory adapter factory} will then be returned as result by this method.
	 * </p>
	 * 
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} whose embedded {@link AdapterFactory adapter
	 *            factory} is to be returned as default. May be left <code>null</code> if
	 *            {@link #getCustomAdapterFactory()} has been overridden and returns a non-<code>null</code> result.
	 * @return The {@link AdapterFactory adapter factory} that will be used by this
	 *         {@link BasicTransactionalEditorActionBarContributor action bar contributor}. <code>null</code> if no
	 *         custom {@link AdapterFactory adapter factory} is provided through {@link #getCustomAdapterFactory()} and
	 *         no <code>editingDomain</code> has been specified.
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
	 * Returns a custom {@link AdapterFactory adapter factory} to be used by this
	 * {@link BasicTransactionalEditorActionBarContributor action bar contributor} for creating
	 * {@link ItemProviderAdapter item provider}s which control the way how {@link EObject model element}s from given
	 * <code>editingDomain</code> are displayed and can be edited.
	 * <p>
	 * This implementation returns <code>null</code> as default. Clients which want to use their own
	 * {@link AdapterFactory adapter factory} (e.g., an {@link AdapterFactory adapter factory} that creates
	 * {@link ItemProviderAdapter item provider}s which are specifically designed for the {@link IEditorPart editor} in
	 * which this {@link BasicTransactionalEditorActionBarContributor action bar contributor} is used) may override this
	 * method and return any {@link AdapterFactory adapter factory} of their choice. This custom {@link AdapterFactory
	 * adapter factory} will then be returned as result by {@link #getAdapterFactory(TransactionalEditingDomain)}.
	 * </p>
	 * 
	 * @return The custom {@link AdapterFactory adapter factory} that is to be used by this
	 *         {@link BasicTransactionalEditorActionBarContributor action bar contributor}. <code>null</code> the
	 *         default {@link AdapterFactory adapter factory} returned by
	 *         {@link #getAdapterFactory(TransactionalEditingDomain)} should be used instead.
	 * @see #getAdapterFactory(TransactionalEditingDomain)
	 */
	protected AdapterFactory getCustomAdapterFactory() {
		return null;
	}

	/**
	 * This populates the pop-up menu before it appears.
	 */
	@Override
	public void menuAboutToShow(IMenuManager menuManager) {
		super.menuAboutToShow(menuManager);
		MenuManager submenuManager = null;

		submenuManager = new MenuManager(getStringFromKey("_UI_CreateChild_menu_item")); //$NON-NLS-1$
		populateManager(submenuManager, createChildSubmenuActions, null);
		populateManager(submenuManager, createChildActions, null);
		menuManager.insertBefore("edit", submenuManager); //$NON-NLS-1$

		submenuManager = new MenuManager(getStringFromKey("_UI_CreateSibling_menu_item")); //$NON-NLS-1$
		populateManager(submenuManager, createSiblingSubmenuActions, null);
		populateManager(submenuManager, createSiblingActions, null);
		menuManager.insertBefore("edit", submenuManager); //$NON-NLS-1$
	}

	/**
	 * This generates a {@link org.eclipse.emf.edit.ui.action.CreateChildAction} for each object in
	 * <code>descriptors</code>, and returns the collection of these actions.
	 */
	protected Collection<IAction> generateCreateChildActions(Collection<?> descriptors, ISelection selection) {
		List<IAction> actions = new ArrayList<IAction>();
		if (descriptors != null) {
			for (Object descriptor : descriptors) {
				actions.add(createCreateChildAction(activeEditor, selection, descriptor));
			}
			Collections.sort(actions, new Comparator<IAction>() {
				@Override
				public int compare(IAction a1, IAction a2) {
					return CommonPlugin.INSTANCE.getComparator().compare(a1.getText(), a2.getText());
				}
			});
		}
		return actions;
	}

	/**
	 * This generates a {@link org.eclipse.emf.edit.ui.action.CreateSiblingAction} for each object in
	 * <code>descriptors</code>, and returns the collection of these actions.
	 */
	protected Collection<IAction> generateCreateSiblingActions(Collection<?> descriptors, ISelection selection) {
		List<IAction> actions = new ArrayList<IAction>();
		if (descriptors != null) {
			for (Object descriptor : descriptors) {
				actions.add(createCreateSiblingAction(activeEditor, selection, descriptor));
			}
			Collections.sort(actions, new Comparator<IAction>() {
				@Override
				public int compare(IAction a1, IAction a2) {
					return CommonPlugin.INSTANCE.getComparator().compare(a1.getText(), a2.getText());
				}
			});
		}
		return actions;
	}

	/**
	 * This populates the specified <code>manager</code> with {@link org.eclipse.jface.action.ActionContributionItem}s
	 * based on the {@link org.eclipse.jface.action.IAction}s contained in the <code>actions</code> collection, by
	 * inserting them before the specified contribution item <code>contributionId</code>. If <code>ID</code> is
	 * <code>null</code>, they are simply added.
	 */
	protected void populateManager(IContributionManager manager, Collection<? extends IAction> actions, String contributionId) {
		if (actions != null) {
			for (IAction action : actions) {
				if (contributionId != null) {
					manager.insertBefore(contributionId, action);
				} else {
					manager.add(action);
				}
			}
		}
	}

	/**
	 * This populates the specified <code>manager</code> with {@link org.eclipse.jface.action.MenuManager}s containing
	 * {@link org.eclipse.jface.action.ActionContributionItem}s based on the {@link org.eclipse.jface.action.IAction}s
	 * contained in the <code>submenuActions</code> collection, by inserting them before the specified contribution item
	 * <code>contributionId</code>. If <code>contributionId</code> is <code>null</code>, they are simply added.
	 */
	protected void populateManager(IContributionManager manager, Map<String, Collection<IAction>> submenuActions, String contributionId) {
		if (submenuActions != null) {
			for (Map.Entry<String, Collection<IAction>> entry : submenuActions.entrySet()) {
				MenuManager submenuManager = new MenuManager(entry.getKey());
				if (contributionId != null) {
					manager.insertBefore(contributionId, submenuManager);
				} else {
					manager.add(submenuManager);
				}
				populateManager(submenuManager, entry.getValue(), null);
			}
		}
	}

	/**
	 * This removes from the specified <code>manager</code> all {@link org.eclipse.jface.action.ActionContributionItem}s
	 * based on the {@link org.eclipse.jface.action.IAction}s contained in the <code>actions</code> collection.
	 */
	protected void depopulateManager(IContributionManager manager, Collection<? extends IAction> actions) {
		if (actions != null) {
			IContributionItem[] items = manager.getItems();
			for (IContributionItem contributionItem : items) {
				while (contributionItem instanceof SubContributionItem) {
					contributionItem = ((SubContributionItem) contributionItem).getInnerItem();
				}

				// Delete the ActionContributionItems with matching action.
				//
				if (contributionItem instanceof ActionContributionItem) {
					IAction action = ((ActionContributionItem) contributionItem).getAction();
					if (actions.contains(action)) {
						manager.remove(contributionItem);
					}
				}
			}
		}
	}

	/**
	 * This removes from the specified <code>manager</code> all {@link org.eclipse.jface.action.MenuManager}s and their
	 * {@link org.eclipse.jface.action.ActionContributionItem}s based on the {@link org.eclipse.jface.action.IAction}s
	 * contained in the <code>submenuActions</code> map.
	 */
	protected void depopulateManager(IContributionManager manager, Map<String, Collection<IAction>> submenuActions) {
		if (submenuActions != null) {
			IContributionItem[] items = manager.getItems();
			for (IContributionItem contributionItem : items) {
				if (contributionItem instanceof MenuManager) {
					MenuManager submenuManager = (MenuManager) contributionItem;
					if (submenuActions.containsKey(submenuManager.getMenuText())) {
						depopulateManager(submenuManager, submenuActions.get(contributionItem));
						manager.remove(contributionItem);
					}
				}
			}
		}
	}

	/**
	 * This extracts those actions in the <code>submenuActions</code> collection whose text is qualified and returns a
	 * map of these actions, keyed by submenu text (see GenModel option "Editor > Creation Sub-menus" for details).
	 */
	protected Map<String, Collection<IAction>> extractSubmenuActions(Collection<IAction> createActions, ISelection selection) {
		Map<String, Collection<IAction>> createSubmenuActions = new LinkedHashMap<String, Collection<IAction>>();
		if (createActions != null) {
			for (Iterator<IAction> actions = createActions.iterator(); actions.hasNext();) {
				IAction action = actions.next();
				StringTokenizer st = new StringTokenizer(action.getText(), "|"); //$NON-NLS-1$
				if (st.countTokens() == 2) {
					String text = st.nextToken().trim();
					IStructuredSelection structuredSelection = SelectionUtil.getStructuredSelection(selection);
					if (structuredSelection.getFirstElement() instanceof TransientItemProvider) {
						// Suppress submenus if we are on an intermediate category node
						action.setText(st.nextToken().trim());
					} else {
						// Decompose qualified action label in to submenu item and simple action text
						Collection<IAction> submenuActions = createSubmenuActions.get(text);
						if (submenuActions == null) {
							createSubmenuActions.put(text, submenuActions = new ArrayList<IAction>());
						}
						action.setText(st.nextToken().trim());
						submenuActions.add(action);
						actions.remove();
					}
				}
			}
		}
		return createSubmenuActions;
	}

	/**
	 * This inserts global actions before the "additions-end" separator.
	 */
	@Override
	protected void addGlobalActions(IMenuManager menuManager) {
		if (showPropertiesViewAction != null || refreshAction != null) {
			menuManager.insertAfter("additions-end", new Separator("ui-actions")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (showPropertiesViewAction != null) {

			menuManager.insertAfter("ui-actions", showPropertiesViewAction); //$NON-NLS-1$
		}
		if (refreshAction != null) {
			refreshAction.setEnabled(refreshAction.isEnabled());
			menuManager.insertAfter("ui-actions", refreshAction); //$NON-NLS-1$
		}

		super.addGlobalActions(menuManager);
	}

	protected CreateChildAction createCreateChildAction(IEditorPart editorPart, ISelection selection, Object descriptor) {
		return new CreateChildAction(activeEditor, selection, descriptor);
	}

	protected CreateSiblingAction createCreateSiblingAction(IEditorPart editorPart, ISelection selection, Object descriptor) {
		return new CreateSiblingAction(activeEditor, selection, descriptor);
	}
}

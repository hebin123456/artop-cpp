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
package org.eclipse.sphinx.emf.ui.actions.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.messages.EMFMessages;
import org.eclipse.sphinx.emf.ui.internal.Activator;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.ui.views.properties.PropertySheet;

/**
 * Basic implementation of {@linkplain CommonActionProvider action provider} that provides default implementations for
 * initialization and for filling (and emptying) the menu managed by this action provider.
 * <p>
 * Initialization is ensured by the method {@linkplain #init(ICommonActionExtensionSite)} that is not supposed to
 * overridden by client implementations (override {@linkplain #doInit()} instead).
 * <p>
 * Filling and emptying contextual menu (maintained by {@linkplain IContributionManager manager}) with
 * {@linkplain IAction action}s are respectively supported by
 * {@linkplain #populateManager(IContributionManager, Collection, String)} and
 * {@linkplain #depopulateManager(IContributionManager, Collection)}.
 */
public class BasicActionProvider extends CommonActionProvider {

	protected IWorkbenchPart workbenchPart;
	protected Viewer viewer;
	protected String viewerId;
	protected ISelectionProvider selectionProvider;

	/*
	 * Clients should avoid overriding this method; providing an implementation of doInit() is preferable.
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	@Override
	public final void init(ICommonActionExtensionSite site) {
		super.init(site);

		// Assume that action provider is only used in workbench parts but not in dialogs
		ICommonViewerSite viewSite = site.getViewSite();
		if (viewSite instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite workbenchSite = (ICommonViewerWorkbenchSite) viewSite;

			// Retrieve underlying viewer and selection provider
			workbenchPart = workbenchSite.getPart();
			if (workbenchPart instanceof CommonNavigator) {
				CommonNavigator navigator = (CommonNavigator) workbenchPart;
				viewer = navigator.getCommonViewer();
				selectionProvider = navigator.getCommonViewer();
			} else {
				selectionProvider = workbenchSite.getSelectionProvider();
			}

			// Retrieve underlying viewer id
			viewerId = workbenchSite.getId();

			doInit();
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void fillContextMenu(IMenuManager contextMenuManager) {
		// Retrieve sub menu
		IMenuManager subMenuManager = addSubMenu(contextMenuManager);

		// Fill sub menu
		fillSubMenu(subMenuManager != null ? subMenuManager : contextMenuManager);
	}

	/**
	 * Method to override in order to provide a custom initialization of this {@linkplain BasicActionProvider action
	 * provider}.
	 */
	protected void doInit() {
		// Do nothing by default
	}

	/**
	 * Returns true when the property sheet is the current active page.
	 * 
	 * @return as specified above.
	 */
	protected boolean isActivePropertySheet() {
		IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		return activePart instanceof PropertySheet;
	}

	/**
	 * Returns the {@link IMenuManager sub menu} into which the {@linkplain org.eclipse.jface.action.IAction action}s
	 * provided by this {@link BasicActionProvider action provider} implementation are to be placed. Creates a new
	 * {@link IMenuManager sub menu} and appends it to specified {@link IMenuManager context menu} if no such is present
	 * yet. Returns <code>null</code> if the {@linkplain org.eclipse.jface.action.IAction action}s should go directly
	 * into the {@link IMenuManager context menu}. The default implementation returns <code>null</code>. Subclasses may
	 * override or extend this method.
	 * 
	 * @param contextMenuManager
	 *            The {@link IMenuManager context menu} for which the {@link IMenuManager sub menu} is to be retrieved
	 *            or created.
	 * @return The {@link IMenuManager sub menu} into which the {@linkplain org.eclipse.jface.action.IAction action}s
	 *         provided by this {@link BasicActionProvider action provider} implementation are to be placed or
	 *         <code>null</code> if they should go directly into the {@link IMenuManager context menu}.
	 * @see #fillSubMenu(IMenuManager)
	 */
	protected IMenuManager addSubMenu(IMenuManager contextMenuManager) {
		return null;
	}

	/**
	 * Adds the applicable {@linkplain org.eclipse.jface.action.IAction action}s to the {@link IMenuManager sub menu} of
	 * the {@link IMenuManager context menu} resulting from previous invocation of {@link #addSubMenu(IMenuManager)}.
	 * The default implementation does nothing. Subclasses may override or extend this method.
	 * 
	 * @param subMenu
	 *            The {@link IMenuManager sub menu} to which applicable {@linkplain org.eclipse.jface.action.IAction
	 *            action}s are to be added.
	 * @see #addSubMenu(IMenuManager)
	 */
	protected void fillSubMenu(IMenuManager subMenuManager) {
		// Do nothing by default
	}

	/**
	 * A convenience method to get the editing domain from an {@link ISelection}. This method will return null in
	 * different cases :
	 * <ul>
	 * <li>selection isn't instance of {@link IStructuredSelection}.
	 * <li>selection is empty.
	 * <li>selection contains unresolved model element(s).
	 * <li>selection contains model elements which belong to more than one editing domain (In case of multi-selection).
	 * </ul>
	 * <p>
	 * 
	 * @since 0.7.0
	 * @param selection
	 * @return the editing domain from the selected model objects or null in either empty selection or when the selected
	 *         model objects belong to more than one editing domain.
	 */
	protected TransactionalEditingDomain getEditingDomainFromSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (!structuredSelection.isEmpty()) {
				TransactionalEditingDomain previousEditingDomain = null;
				TransactionalEditingDomain currentEditingDomain = null;
				for (Object seletedObject : structuredSelection.toList()) {
					if (seletedObject instanceof EObject && ((EObject) seletedObject).eIsProxy()) {
						PlatformLogUtil.logAsWarning(Activator.getPlugin(),
								new RuntimeException(NLS.bind(EMFMessages.warning_selectionContainsUnresolvedModelElement, seletedObject)));
					}
					currentEditingDomain = WorkspaceEditingDomainUtil.getEditingDomain(seletedObject);
					if (currentEditingDomain == null) {
						return null;
					} else if (previousEditingDomain == null) {
						previousEditingDomain = currentEditingDomain;
					} else if (!previousEditingDomain.equals(currentEditingDomain)) {
						return null;
					}
				}
				return currentEditingDomain;
			}
		}
		return null;
	}

	/**
	 * Populates the specified {@link IContributionManager manager} with
	 * {@linkplain org.eclipse.jface.action.ActionContributionItem contribution item}s based on the
	 * {@linkplain org.eclipse.jface.action.IAction action}s contained in the <code>actions</code> collection, by
	 * inserting them before the specified contribution item <code>contributionId</code>.<br>
	 * If <code>contributionId</code> is <code>null</code>, they are simply added.
	 * 
	 * @param manager
	 *            The {@linkplain IContributionManager contribution manager} to which specified {@linkplain IAction
	 *            action}s must be populated.
	 * @param actions
	 *            The {@linkplain IAction action}s to insert in the menu described by the given
	 *            {@link IContributionManager manager}.
	 * @param contributionId
	 *            The identifier of the contribution before which actions must be inserted.
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
	 * 
	 * @param manager
	 *            The {@linkplain IContributionManager contribution manager} to which specified {@linkplain IAction
	 *            action}s must be populated.
	 * @param submenuActions
	 *            The {@linkplain IAction submenu actions}s to insert in the menu described by the given
	 *            {@link IContributionManager manager}.
	 * @param contributionId
	 *            The identifier of the contribution before which actions must be inserted.
	 * @since 0.7.0
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
	 * Removes from the specified {@link IContributionManager manager} all
	 * {@linkplain org.eclipse.jface.action.ActionContributionItem contribution item}s based on the
	 * {@linkplain org.eclipse.jface.action.IAction action}s contained in the <code>actions</code> collection.
	 * 
	 * @param manager
	 *            The {@linkplain IContributionManager contribution manager} from which specified {@linkplain IAction
	 *            action}s must be removed.
	 * @param actions
	 *            The {@linkplain IAction action}s to remove from the menu described by the given
	 *            {@link IContributionManager manager}.
	 */
	protected void depopulateManager(IContributionManager manager, Collection<? extends IAction> actions) {
		if (actions != null) {
			IContributionItem[] items = manager.getItems();
			for (IContributionItem contributionItem : items) {
				while (contributionItem instanceof SubContributionItem) {
					contributionItem = ((SubContributionItem) contributionItem).getInnerItem();
				}

				// Delete the ActionContributionItems with matching action
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
	 * 
	 * @param manager
	 *            The {@linkplain IContributionManager contribution manager} from which specified {@linkplain IAction
	 *            action}s must be removed.
	 * @param submenuActions
	 *            The {@linkplain IAction submenu actions}s to remove from the menu described by the given
	 *            {@link IContributionManager manager}.
	 * @since 0.7.0
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
	 * 
	 * @since 0.7.0
	 */
	protected Map<String, Collection<IAction>> extractSubmenuActions(Collection<IAction> createActions, ISelection selection) {
		Map<String, Collection<IAction>> createSubmenuActions = new LinkedHashMap<String, Collection<IAction>>();
		if (createActions != null) {
			for (Iterator<IAction> actions = createActions.iterator(); actions.hasNext();) {
				IAction action = actions.next();
				if (action.getText() != null) {
					StringTokenizer st = new StringTokenizer(action.getText(), "|"); //$NON-NLS-1$
					if (st.countTokens() == 2) {
						String text = st.nextToken().trim();
						IStructuredSelection structuredSelection = SelectionUtil.getStructuredSelection(selection);
						if (isTransient(structuredSelection.getFirstElement())) {
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
		}
		return createSubmenuActions;
	}

	/**
	 * Returns true if the given object is a transient item provider i.e. an intermediary node, false else.
	 * 
	 * @param object
	 *            an object.
	 * @return true if the given object is a transient item provider i.e. an intermediary node, false else.
	 */
	protected boolean isTransient(Object object) {
		if (object instanceof TransientItemProvider) {
			return true;
		}
		return false;
	}
}
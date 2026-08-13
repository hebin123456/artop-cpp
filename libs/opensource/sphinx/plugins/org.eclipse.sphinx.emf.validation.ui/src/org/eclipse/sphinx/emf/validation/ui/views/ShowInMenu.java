/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     See4sys - added support for problem markers on model objects (rather than
 *               only on workspace resources). Unfortunately, there was no other
 *               choice than copying the whole code from
 *               org.eclipse.ui.views.markers.internal for that purpose because
 *               many of the relevant classes, methods, and fields are private or
 *               package private.
 *******************************************************************************/
package org.eclipse.sphinx.emf.validation.ui.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.NavigatorContentServiceFactory;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * A <code>ShowInMenu</code> is used to populate a menu manager with Show In actions. The items to show are determined
 * from the active perspective and active part.
 */
public class ShowInMenu extends ContributionItem {

	private static final String NO_TARGETS_MSG = WorkbenchMessages.Workbench_showInNoTargets;

	private IWorkbenchWindow window;

	private Map actions = new HashMap(21);

	private boolean dirty = true;

	private String targetObjectURI = null;

	private IMenuListener menuListener = new IMenuListener() {
		@Override
		public void menuAboutToShow(IMenuManager manager) {
			manager.markDirty();
			dirty = true;
		}
	};

	/**
	 * Creates a Show In menu.
	 *
	 * @param window
	 *            the window containing the menu
	 */
	public ShowInMenu(IWorkbenchWindow window, String id, String uri) {
		super(id);
		this.window = window;
		targetObjectURI = uri;
	}

	protected IWorkbenchWindow getWindow() {
		return window;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public void fill(Menu menu, int index) {
		if (getParent() instanceof MenuManager) {
			((MenuManager) getParent()).addMenuListener(menuListener);
		}

		if (!dirty) {
			return;
		}

		MenuManager manager = new MenuManager();
		fillMenu(manager);
		IContributionItem[] items = manager.getItems();
		if (items.length == 0) {
			MenuItem item = new MenuItem(menu, SWT.NONE, index++);
			item.setText(NO_TARGETS_MSG);
			item.setEnabled(false);
		} else {
			for (IContributionItem element : items) {
				element.fill(menu, index++);
			}
		}
		dirty = false;
	}

	/**
	 * Fills the menu with Show In actions.
	 */
	private void fillMenu(IMenuManager innerMgr) {
		// Remove all.
		innerMgr.removeAll();

		IWorkbenchPart sourcePart = getSourcePart();
		if (sourcePart == null) {
			return;
		}
		ShowInContext context = getContext(sourcePart);
		if (context == null) {
			return;
		}
		if (context.getInput() == null && (context.getSelection() == null || context.getSelection().isEmpty())) {
			return;
		}

		IViewDescriptor[] viewDescs = getViewDescriptors(sourcePart);
		if (viewDescs.length == 0) {
			return;
		}

		for (IViewDescriptor viewDesc : viewDescs) {
			IAction action = getAction(viewDesc);
			if (action != null) {
				innerMgr.add(action);
			}
		}
	}

	/**
	 * Returns the action for the given view id, or null if not found.
	 */
	private IAction getAction(IViewDescriptor desc) {
		// Keep a cache, rather than creating a new action each time,
		// so that image caching in ActionContributionItem works.
		IAction action = (IAction) actions.get(desc.getId());
		if (action == null) {
			if (desc != null) {
				action = new ShowInAction(window, desc);
				actions.put(desc.getId(), action);
			}
		}
		return action;
	}

	/**
	 * Returns the Show In... target part ids for the given source part. Merges the contributions from the current
	 * perspective and the source part.
	 */
	private ArrayList getShowInPartIds(IWorkbenchPart sourcePart) {
		ArrayList targetIds = new ArrayList();
		WorkbenchPage page = (WorkbenchPage) getWindow().getActivePage();
		if (page != null) {
			targetIds.addAll(page.getShowInPartIds());
		}
		IShowInTargetList targetList = getShowInTargetList(sourcePart);
		if (targetList != null) {
			String[] partIds = targetList.getShowInTargetIds();
			if (partIds != null) {
				for (int i = 0; i < partIds.length; ++i) {
					if (!targetIds.contains(partIds[i])) {
						targetIds.add(partIds[i]);
					}
				}
			}
		}
		page.sortShowInPartIds(targetIds);
		return targetIds;
	}

	/**
	 * Returns the source part, or <code>null</code> if there is no applicable source part
	 * <p>
	 * This implementation returns the current part in the window. Subclasses may extend or reimplement.
	 *
	 * @return the source part or <code>null</code>
	 */
	private IWorkbenchPart getSourcePart() {
		IWorkbenchPage page = getWindow().getActivePage();
		if (page != null) {
			return page.getActivePart();
		}
		return null;
	}

	/**
	 * Returns the <code>IShowInSource</code> provided by the source part, or <code>null</code> if it does not provide
	 * one.
	 *
	 * @param sourcePart
	 *            the source part
	 * @return an <code>IShowInSource</code> or <code>null</code>
	 */
	private IShowInSource getShowInSource(IWorkbenchPart sourcePart) {
		return (IShowInSource) sourcePart.getAdapter(IShowInSource.class);
	}

	/**
	 * Returns the <code>IShowInTargetList</code> for the given source part, or <code>null</code> if it does not provide
	 * one.
	 *
	 * @param sourcePart
	 *            the source part
	 * @return the <code>IShowInTargetList</code> or <code>null</code>
	 */
	private IShowInTargetList getShowInTargetList(IWorkbenchPart sourcePart) {
		return (IShowInTargetList) sourcePart.getAdapter(IShowInTargetList.class);
	}

	/**
	 * Returns the <code>ShowInContext</code> to show in the selected target, or <code>null</code> if there is no valid
	 * context to show.
	 * <p>
	 * This implementation obtains the context from the <code>IShowInSource</code> of the source part (if provided), or,
	 * if the source part is an editor, it creates the context from the editor's input and selection.
	 * <p>
	 * Subclasses may extend or reimplement.
	 *
	 * @return the <code>ShowInContext</code> to show or <code>null</code>
	 */
	private ShowInContext getContext(IWorkbenchPart sourcePart) {
		IShowInSource source = getShowInSource(sourcePart);
		if (source != null) {
			ShowInContext context = source.getShowInContext();
			if (context != null) {
				return context;
			}
		} else if (sourcePart instanceof IEditorPart) {
			Object input = ((IEditorPart) sourcePart).getEditorInput();
			ISelectionProvider sp = sourcePart.getSite().getSelectionProvider();
			ISelection sel = sp == null ? null : sp.getSelection();
			return new ShowInContext(input, sel);
		}
		return null;
	}

	/**
	 * Returns the view descriptors to show in the dialog.
	 */
	private IViewDescriptor[] getViewDescriptors(IWorkbenchPart sourcePart) {
		String srcId = sourcePart.getSite().getId();
		ArrayList ids = getShowInPartIds(sourcePart);
		ArrayList descs = new ArrayList();
		IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
		for (Iterator i = ids.iterator(); i.hasNext();) {
			String id = (String) i.next();
			if (!id.equals(srcId)) {
				IViewDescriptor desc = reg.find(id);
				if (desc != null) {
					// construct EObject from targetObjectURI
					TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(URI.createURI(targetObjectURI, true));
					if (editingDomain != null) {
						EObject targetObject = editingDomain.getResourceSet().getEObject(URI.createURI(targetObjectURI, true), false);
						if (targetObject != null) {
							INavigatorContentService contentService = NavigatorContentServiceFactory.INSTANCE.createContentService(id);
							INavigatorContentDescriptor[] navigatorContentDesList = contentService.getVisibleExtensions();

							boolean ableToShow = false;
							for (INavigatorContentDescriptor des : navigatorContentDesList) {
								if (des.isPossibleChild(targetObject)) {
									ableToShow = true;
									break;
								}
							}

							if (ableToShow) {
								descs.add(desc);
							}
						} else {
							// all views are acceptable if targetObject is null
							descs.add(desc);
						}
					} else {
						// all views are acceptable if editingDomain is not existed
						descs.add(desc);
					}
				}
			}
		}
		return (IViewDescriptor[]) descs.toArray(new IViewDescriptor[descs.size()]);
	}
}

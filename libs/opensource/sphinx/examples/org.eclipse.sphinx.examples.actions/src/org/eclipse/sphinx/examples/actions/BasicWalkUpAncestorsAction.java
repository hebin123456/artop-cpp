/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.actions;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sphinx.emf.edit.ITreeItemAncestorProvider;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.examples.actions.internal.messages.Messages;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public class BasicWalkUpAncestorsAction extends BaseSelectionListenerAction {

	public static long WALK_UP_ANCESTORS_DELAY = 500;

	protected Viewer viewer;
	protected Object selectedObject = null;
	protected ITreeItemAncestorProvider treeItemAncestorProvider = null;

	public BasicWalkUpAncestorsAction(Viewer viewer) {
		this(Messages.act_WalkUpAncestors_label, viewer);
	}

	public BasicWalkUpAncestorsAction(String text, Viewer viewer) {
		super(text);

		Assert.isNotNull(viewer);
		this.viewer = viewer;
	}

	@Override
	public boolean updateSelection(IStructuredSelection selection) {
		if (selection.size() == 1) {
			selectedObject = selection.getFirstElement();
			TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(selectedObject);
			AdapterFactory adapterFactory = getAdapterFactory(editingDomain);
			if (adapterFactory != null) {
				treeItemAncestorProvider = (ITreeItemAncestorProvider) adapterFactory.adapt(selectedObject, ITreeItemAncestorProvider.class);
			}
		}
		return treeItemAncestorProvider != null;
	}

	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		List<Object> ancestorPath = treeItemAncestorProvider.getAncestorPath(selectedObject, false);

		for (Object ancestor : ancestorPath) {
			if (!skipAncestor(ancestor)) {
				continue;
			}
			if (viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed()) {
				break;
			}

			// TODO Surround with appropriate tracing option
			System.out.println(ancestor);

			viewer.setSelection(new StructuredSelection(ancestor));

			try {
				Thread.sleep(WALK_UP_ANCESTORS_DELAY);
			} catch (InterruptedException ex) {
				// Ignore exception
			}
		}

		// TODO Surround with appropriate tracing option
		System.out.println();
	}

	protected boolean skipAncestor(Object ancestor) {
		// Skip Resource and ResourceSet objects as they are normally not directly visible in the Sphinx Model Explorer
		return !(ancestor instanceof Resource) && !(ancestor instanceof ResourceSet);
	}

	/**
	 * Returns the {@link AdapterFactory adapter factory} to be used by this {@link BasicExplorerContentProvider content
	 * provider} for creating {@link ItemProviderAdapter item provider}s which control the way how {@link EObject model
	 * element}s from given <code>editingDomain</code> are displayed and can be edited.
	 * <p>
	 * This implementation returns the {@link AdapterFactory adapter factory} which is embedded in the given
	 * <code>editingDomain</code> by default. Clients which want to use an alternative {@link AdapterFactory adapter
	 * factory} (e.g., an {@link AdapterFactory adapter factory} that creates {@link ItemProviderAdapter item provider}s
	 * which are specifically designed for the {@link IEditorPart editor} in which this
	 * {@link BasicExplorerContentProvider content provider} is used) may override {@link #getCustomAdapterFactory()}
	 * and return any {@link AdapterFactory adapter factory} of their choice. This custom {@link AdapterFactory adapter
	 * factory} will then be returned as result by this method.
	 * </p>
	 *
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} whose embedded {@link AdapterFactory adapter
	 *            factory} is to be returned as default. May be left <code>null</code> if
	 *            {@link #getCustomAdapterFactory()} has been overridden and returns a non-<code>null</code> result.
	 * @return The {@link AdapterFactory adapter factory} that will be used by this {@link BasicExplorerContentProvider
	 *         content provider}. <code>null</code> if no custom {@link AdapterFactory adapter factory} is provided
	 *         through {@link #getCustomAdapterFactory()} and no <code>editingDomain</code> has been specified.
	 * @see #getCustomAdapterFactory()
	 */
	protected AdapterFactory getAdapterFactory(TransactionalEditingDomain editingDomain) {
		AdapterFactory customAdapterFactory = getCustomAdapterFactory();
		if (customAdapterFactory != null) {
			return customAdapterFactory;
		} else if (editingDomain instanceof AdapterFactoryEditingDomain) {
			return ((AdapterFactoryEditingDomain) editingDomain).getAdapterFactory();
		}
		return null;
	}

	/**
	 * Returns a custom {@link AdapterFactory adapter factory} to be used by this {@link BasicExplorerContentProvider
	 * content provider} for creating {@link ItemProviderAdapter item provider}s which control the way how
	 * {@link EObject model element}s from given <code>editingDomain</code> are displayed and can be edited.
	 * <p>
	 * This implementation returns <code>null</code> as default. Clients which want to use their own
	 * {@link AdapterFactory adapter factory} (e.g., an {@link AdapterFactory adapter factory} that creates
	 * {@link ItemProviderAdapter item provider}s which are specifically designed for the {@link IEditorPart editor} in
	 * which this {@link BasicExplorerContentProvider content provider} is used) may override this method and return any
	 * {@link AdapterFactory adapter factory} of their choice. This custom {@link AdapterFactory adapter factory} will
	 * then be returned as result by {@link #getAdapterFactory(TransactionalEditingDomain)}.
	 * </p>
	 *
	 * @return The custom {@link AdapterFactory adapter factory} that is to be used by this
	 *         {@link BasicExplorerContentProvider content provider}. <code>null</code> the default
	 *         {@link AdapterFactory adapter factory} returned by {@link #getAdapterFactory(TransactionalEditingDomain)}
	 *         should be used instead.
	 * @see #getAdapterFactory(TransactionalEditingDomain)
	 */
	protected AdapterFactory getCustomAdapterFactory() {
		return null;
	}
}
/**
 * <copyright>
 *
 * Copyright (c) 2011 itemis and others.
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
package org.eclipse.sphinx.emf.workspace.ui.actions;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.workspace.ui.internal.Activator;
import org.eclipse.sphinx.emf.workspace.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.workspace.ui.util.ICommonModelUIConstants;
import org.eclipse.sphinx.emf.workspace.ui.views.ReferencesView;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public class BasicOpenReferencesAction extends BaseSelectionListenerAction {

	public BasicOpenReferencesAction() {
		super(Messages.action_basicOpenReferencesAction_label);
	}

	public BasicOpenReferencesAction(String text) {
		super(text);
	}

	protected EObject getSelectedModelObject() {
		Object selected = getStructuredSelection().getFirstElement();

		// Ignore intermediate category and other non-model element nodes
		if (isTransient(selected)) {
			return null;
		}

		Object unwrapped = AdapterFactoryEditingDomain.unwrap(selected);
		if (unwrapped instanceof EObject) {
			return (EObject) unwrapped;
		}
		Resource resource = EcorePlatformUtil.getResource(selected);
		if (resource != null && !resource.getContents().isEmpty()) {
			return resource.getContents().get(0);
		}
		return null;
	}

	/**
	 * Tests if given object represents an intermediate category node i.e. a non-modeled object. This returns true if
	 * the given object is a transient item provider i.e. an intermediate node, false else.
	 *
	 * @param object
	 *            an object.
	 * @return true if the given object is a transient item provider i.e. an intermediate node, false else.
	 */
	protected boolean isTransient(Object object) {
		if (object instanceof TransientItemProvider) {
			return true;
		}
		return false;
	}

	@Override
	public boolean updateSelection(IStructuredSelection selection) {
		return selection.size() == 1 && getSelectedModelObject() != null;
	}

	@Override
	public void run() {
		// TODO Retrieve page from viewPart to be passed in from action provider
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page != null) {
			try {
				// TODO Replace constant by algorithm that attempts at finding a subclass of ReferencesView contributed
				// to org.eclipse.ui.views
				ReferencesView view = (ReferencesView) page.showView(ICommonModelUIConstants.VIEW_REFERENCES_ID);
				view.setViewInput(getSelectedModelObject());
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
	}
}

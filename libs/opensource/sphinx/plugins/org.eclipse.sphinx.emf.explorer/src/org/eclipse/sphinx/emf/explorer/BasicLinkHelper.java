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
package org.eclipse.sphinx.emf.explorer;

import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.explorer.internal.Activator;
import org.eclipse.sphinx.emf.ui.util.EcoreUIUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;

public class BasicLinkHelper implements ILinkHelper {

	@Override
	public void activateEditor(IWorkbenchPage page, IStructuredSelection selection) {
		if (selection != null && selection.size() == 1) {
			Object selected = selection.getFirstElement();
			if (!isTransient(selected)) {
				URIEditorInput input = EcoreUIUtil.createURIEditorInput(selected);
				if (input != null) {
					IEditorPart editor = page.findEditor(input);
					if (editor != null) {
						page.bringToTop(editor);
					}
				}
			}
		}
	}

	@Override
	public IStructuredSelection findSelection(IEditorInput anInput) {
		if (anInput instanceof URIEditorInput) {
			URIEditorInput input = (URIEditorInput) anInput;
			URI uri = input.getURI();
			if (uri != null) {
				EObject object = getEObject(uri);
				if (object != null) {
					return new StructuredSelection(object);
				}
			}
		}
		return StructuredSelection.EMPTY;
	}

	protected EObject getEObject(final URI uri) {
		if (uri != null) {
			final TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(uri);
			if (editingDomain != null) {
				try {
					return TransactionUtil.runExclusive(editingDomain, new RunnableWithResult.Impl<EObject>() {
						@Override
						public void run() {
							setResult(EcoreResourceUtil.loadEObject(editingDomain.getResourceSet(), uri));
						}
					});
				} catch (InterruptedException ex) {
					PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
				}
			}
		}
		return null;
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

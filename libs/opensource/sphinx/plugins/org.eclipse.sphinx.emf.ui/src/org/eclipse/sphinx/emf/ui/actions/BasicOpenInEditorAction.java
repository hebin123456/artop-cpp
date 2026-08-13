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
package org.eclipse.sphinx.emf.ui.actions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.ui.internal.Activator;
import org.eclipse.sphinx.emf.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.ui.util.EcoreUIUtil;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public class BasicOpenInEditorAction extends BaseSelectionListenerAction {

	protected Map<String, Set<Object>> editorIdToEditorInputObjectsMap = new HashMap<String, Set<Object>>();

	public BasicOpenInEditorAction() {
		super(Messages.action_openInEditor_label);
	}

	@Override
	public boolean updateSelection(IStructuredSelection selection) {
		editorIdToEditorInputObjectsMap.clear();

		for (Object object : selection.toList()) {
			// Disable action if one of the selected objects is an intermediate category and other non-model element
			// node
			if (isTransient(object)) {
				editorIdToEditorInputObjectsMap.clear();
				return false;
			}

			// Determine default editor associated with selected object
			IEditorDescriptor defaultEditor = EcoreUIUtil.getDefaultEditor(object);
			if (defaultEditor != null) {
				String editorId = defaultEditor.getId();
				if (editorId != null) {
					// Remember selected object and associated default editor
					Set<Object> editorInputObjects = editorIdToEditorInputObjectsMap.get(editorId);
					if (editorInputObjects == null) {
						editorInputObjects = new HashSet<Object>();
						editorIdToEditorInputObjectsMap.put(editorId, editorInputObjects);
					}
					editorInputObjects.add(object);
				}
			}
		}

		// Enable action if a default editor for at least one of the selected objects has been found
		return editorIdToEditorInputObjectsMap.keySet().size() > 0;
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
	public void run() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page != null) {
			for (String editorId : editorIdToEditorInputObjectsMap.keySet()) {
				for (Object object : editorIdToEditorInputObjectsMap.get(editorId)) {
					try {
						// Create editor input pointing at selected object
						IEditorInput editorInput = EcoreUIUtil.createURIEditorInput(object);
						if (editorInput != null) {
							page.openEditor(editorInput, editorId);
						}
					} catch (Exception ex) {
						PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
					}
				}
			}
		}
	}
}

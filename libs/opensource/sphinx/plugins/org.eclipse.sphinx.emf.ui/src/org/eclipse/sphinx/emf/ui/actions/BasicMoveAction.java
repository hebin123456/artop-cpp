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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.ui.internal.messages.Messages;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public class BasicMoveAction extends BaseSelectionListenerAction {

	public BasicMoveAction() {
		super(Messages.action_move_label);
	}

	public BasicMoveAction(String text) {
		super(text);
	}

	@Override
	public boolean updateSelection(IStructuredSelection selection) {
		return isMoveAvailable(selection);
	}

	@Override
	public void run() {
		MessageDialog.openInformation(ExtendedPlatformUI.getActiveShell(), Messages.action_move_label, "Not supported yet..."); //$NON-NLS-1$
	}

	protected boolean isMoveAvailable(IStructuredSelection selection) {
		return SelectionUtil.hasOnlyElementsOfSameType(selection) && selection.getFirstElement() instanceof EObject;
	}
}

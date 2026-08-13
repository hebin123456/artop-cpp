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
package org.eclipse.sphinx.emf.explorer.internal.actions.providers;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.emf.workspace.ResourceUndoContext;
import org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.navigator.resources.actions.UndoRedoActionProvider;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;

/**
 * Customized undo/redo action provider that is intended to override the {@link UndoRedoActionProvider original one}
 * from Eclipse. It replaces the originally used {@link ResourceUndoContext} (which works only resource oriented) by an
 * {@link ObjectUndoContext} (which works also model object oriented).
 */
@SuppressWarnings("restriction")
public class UndoRedoOverrideActionProvider extends BasicActionProvider {

	protected UndoActionHandler undoActionHandler;

	protected RedoActionHandler redoActionHandler;

	/*
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	@Override
	public void doInit() {
		IUndoContext undoContext = getUndoContext(workbenchPart);

		// Create the undo action handler
		undoActionHandler = new UndoActionHandler(workbenchPart.getSite(), undoContext);
		undoActionHandler.setPruneHistory(true);

		// Create the redo action handler
		redoActionHandler = new RedoActionHandler(workbenchPart.getSite(), undoContext);
		redoActionHandler.setPruneHistory(true);

		updateActionBars();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);

		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undoActionHandler);
		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), redoActionHandler);
	}

	@Override
	public void updateActionBars() {
		undoActionHandler.update();
		redoActionHandler.update();
	}

	/**
	 * Retrieves the right {@linkplain IUndoContext undo context} according to the specified {@linkplain IWorkbenchPart
	 * workbench part}. Typically this method returns an instance of {@linkplain ObjectUndoContext} which is used
	 * instead of a {@linkplain ResourceUndoContext} as done in the {@link UndoRedoActionProvider undo/redo action
	 * provider} provided by Eclipse does.
	 * 
	 * @param workbenchPart
	 *            The {@linkplain IWorkbenchPart workbench part} (i.e. view or editor) for which
	 *            {@linkplain IUndoContext undo context} must be returned.
	 * @return The right {@linkplain IUndoContext undo context} according to the given {@link IWorkbenchPart
	 *         workbenchPart}.
	 */
	private IUndoContext getUndoContext(IWorkbenchPart workbenchPart) {
		if (workbenchPart != null) {
			Object adapter = workbenchPart.getAdapter(IUndoContext.class);
			if (adapter != null) {
				return (IUndoContext) adapter;
			}
		}
		return IOperationHistory.GLOBAL_UNDO_CONTEXT;
	}
}

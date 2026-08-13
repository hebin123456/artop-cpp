/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.emf.editors;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.edit.provider.IDisposable;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.IWorkspaceCommandStack;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheet;

public class ModelEditorUndoContextManager implements IDisposable {

	protected IWorkbenchPartSite site;
	protected IWorkbenchPart targetPart;
	protected IPropertySheetPage targetPropertySheetPage;
	protected TransactionalEditingDomain editingDomain;

	private IUndoContext undoContext = null;
	private IOperationHistoryListener undoableOperationBeginListener;

	public ModelEditorUndoContextManager(IWorkbenchPartSite site, IWorkbenchPart targetPart, TransactionalEditingDomain editingDomain) {
		Assert.isNotNull(site);
		Assert.isNotNull(targetPart);

		this.site = site;
		this.targetPart = targetPart;
		this.editingDomain = editingDomain;

		undoableOperationBeginListener = createUndoableOperationBeginListener();
		Assert.isNotNull(undoableOperationBeginListener);
		getOperationHistory().addOperationHistoryListener(undoableOperationBeginListener);
	}

	// FIXME This method is never called (see
	// org.eclipse.sphinx.gmf.runtime.ui.editor.BasicDiagramDocumentEditor.getAdapter(Class) for details)
	public void setTargetPropertySheetPage(IPropertySheetPage targetPropertySheetPage) {
		this.targetPropertySheetPage = targetPropertySheetPage;
	}

	protected boolean isTargetPartActive() {
		return targetPart == site.getWorkbenchWindow().getPartService().getActivePart();
	}

	protected boolean isTargetPropertySheetPageActive() {
		if (targetPropertySheetPage != null) {
			IWorkbenchPart activePart = site.getWorkbenchWindow().getPartService().getActivePart();
			if (activePart instanceof PropertySheet) {
				return targetPropertySheetPage == ((PropertySheet) activePart).getCurrentPage();
			}
		}
		return false;
	}

	protected IOperationHistory getOperationHistory() {
		if (editingDomain != null) {
			CommandStack commandStack = editingDomain.getCommandStack();
			if (commandStack instanceof IWorkspaceCommandStack) {
				return ((IWorkspaceCommandStack) commandStack).getOperationHistory();
			}
		}
		return OperationHistoryFactory.getOperationHistory();
	}

	public IUndoContext getUndoContext() {
		if (undoContext == null) {
			undoContext = createUndoContext(targetPart);
		}
		return undoContext;
	}

	protected IUndoContext createUndoContext(IWorkbenchPart targetPart) {
		return new ObjectUndoContext(targetPart);
	}

	protected IUndoContext getDefaultUndoContext() {
		if (editingDomain != null) {
			CommandStack commandStack = editingDomain.getCommandStack();
			if (commandStack instanceof IWorkspaceCommandStack) {
				return ((IWorkspaceCommandStack) commandStack).getDefaultUndoContext();
			}
		}
		return null;
	}

	protected IOperationHistoryListener createUndoableOperationBeginListener() {
		return new IOperationHistoryListener() {
			@Override
			public void historyNotification(final OperationHistoryEvent event) {
				IUndoableOperation operation = event.getOperation();
				if (event.getEventType() == OperationHistoryEvent.ABOUT_TO_EXECUTE) {
					if (operation.canUndo()) {
						handleUndoableOperationBegin(operation);
					}
				}
			}

			private void handleUndoableOperationBegin(final IUndoableOperation operation) {
				if (site != null && site.getShell() != null && !site.getShell().isDisposed()) {
					site.getShell().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							if (isTargetPartActive() || isTargetPropertySheetPageActive()) {
								// Remove default undo context such is available, and add the underlying editor's undo
								// context
								IUndoContext defaultUndoContext = getDefaultUndoContext();
								if (defaultUndoContext != null) {
									operation.removeContext(defaultUndoContext);
								}
								operation.addContext(undoContext);
							}
						}
					});
				}
			}
		};
	}

	/*
	 * @see org.eclipse.emf.edit.provider.IDisposable#dispose()
	 */
	@Override
	public void dispose() {
		getOperationHistory().removeOperationHistoryListener(undoableOperationBeginListener);

		if (undoContext != null) {
			getOperationHistory().dispose(undoContext, true, true, true);
		}
	}
}

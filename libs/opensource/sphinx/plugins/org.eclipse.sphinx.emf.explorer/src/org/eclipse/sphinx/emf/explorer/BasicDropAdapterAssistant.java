/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - Reviewed and improved fix for [355368] Drag and drop tree-item/element from file A to file B got problem
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.explorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.DragAndDropCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.explorer.internal.Activator;
import org.eclipse.sphinx.emf.explorer.internal.messages.Messages;
import org.eclipse.sphinx.platform.util.StatusUtil;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;

public class BasicDropAdapterAssistant extends CommonDropAdapterAssistant {

	public static final int DND_OPERATIONS_DROP_COPY_DROP_MOVE_DROP_LINK = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;

	/**
	 * Drop location constant for obtaining the following drop behavior: Always try to DROP_ON first, and, if this is
	 * not possible, try to DROP_INSERT.
	 * <ul>
	 * <li>DROP_ON: paste drop source as child of drop target => dropped source will be a child of drop target</li>
	 * <li>DROP_INSERT: find the parent of the drop target and paste drop source as child of that parent => dropped
	 * source will be a sibling of drop target</li>
	 * </ul>
	 */
	public static final float DND_LOCATION_DROP_ON_FIRST = 0.5f;

	/*
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#handleDrop(org.eclipse.ui.navigator.CommonDropAdapter,
	 * org.eclipse.swt.dnd.DropTargetEvent, java.lang.Object)
	 */
	@Override
	public IStatus handleDrop(CommonDropAdapter dropAdapter, DropTargetEvent event, Object target) {
		TransferData transferType = dropAdapter.getCurrentTransfer();
		if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
			switch (dropAdapter.getCurrentOperation()) {
			case DND.DROP_MOVE:
				handleDropMove(target, dropAdapter, event);
				break;
			case DND.DROP_COPY:
				handleDropCopy(target, dropAdapter, event);
				break;
			case DND.DROP_LINK:
				break;
			}
			return Status.OK_STATUS;
		}
		return new Status(IStatus.ERROR, Activator.getPlugin().getBundle().getSymbolicName(), Messages.error_transferTypeNotSupported);
	}

	/*
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#validateDrop(java.lang.Object, int,
	 * org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public IStatus validateDrop(Object target, int operation, TransferData transferType) {
		if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
			List<EObject> selectedEObjects = getSelectedEObjects();

			if (!(target instanceof EObject) && !(target instanceof IWrapperItemProvider)) {
				return StatusUtil.createStatus(IStatus.INFO, 0, Messages.info_targetObjectType, Activator.getPlugin().getSymbolicName(),
						new RuntimeException());
			}

			TransactionalEditingDomain editingDomain = getEditingDomain(target);
			if (editingDomain == null) {
				return StatusUtil.createStatus(IStatus.ERROR, 0, Messages.error_targetNoEditingDomain, Activator.getPlugin().getBundle()
						.getSymbolicName(), new RuntimeException());
			}

			Object unwrappedTarget = AdapterFactoryEditingDomain.unwrap(target);
			Command command = DragAndDropCommand.create(editingDomain, unwrappedTarget, getDefaultDragAndDropLocation(),
					getDefaultDragAndDropOperations(), operation, selectedEObjects);

			if (command.canExecute()) {
				return Status.OK_STATUS;
			} else if (!command.canExecute() && operation == DND.DROP_NONE) {
				Command command2 = DragAndDropCommand.create(editingDomain, unwrappedTarget, getDefaultDragAndDropLocation(),
						getDefaultDragAndDropOperations(), getDefaultDragAndDropOperations(), selectedEObjects);
				if (command2.canExecute()) {
					return Status.OK_STATUS;
				}
			} else {
				return StatusUtil.createStatus(IStatus.INFO, 0, Messages.info_dropCommandCannotExecute, Activator.getPlugin().getBundle()
						.getSymbolicName(), new RuntimeException());
			}
		}
		return StatusUtil.createStatus(IStatus.ERROR, 0, Messages.error_transferTypeNotSupported,
				Activator.getPlugin().getBundle().getSymbolicName(), new RuntimeException());
	}

	protected IStatus handleDropCopy(Object target, CommonDropAdapter dropAdapter, DropTargetEvent event) {
		TransactionalEditingDomain domain = getEditingDomain(target);
		if (domain == null) {
			return StatusUtil.createStatus(IStatus.ERROR, 0, Messages.error_targetNoEditingDomain, Activator.getPlugin().getBundle()
					.getSymbolicName(), new RuntimeException());
		}
		List<EObject> selectedEObjects = getSelectedEObjects();
		Command command = DragAndDropCommand.create(domain, target, getDefaultDragAndDropLocation(), event.operations, DND.DROP_COPY,
				selectedEObjects);
		return promptAndExecuteDropCopy(domain, target, command, Messages.label_confirmCopy, Messages.label_OKToCopy);
	}

	protected IStatus handleDropMove(Object target, CommonDropAdapter dropAdapter, DropTargetEvent event) {
		TransactionalEditingDomain domain = getEditingDomain(target);
		if (domain == null) {
			return StatusUtil.createStatus(IStatus.ERROR, 0, Messages.error_targetNoEditingDomain, Activator.getPlugin().getBundle()
					.getSymbolicName(), new RuntimeException());
		}

		List<EObject> selectedEObjects = getSelectedEObjects();

		Object unwrappedTarget = AdapterFactoryEditingDomain.unwrap(target);
		Command command = DragAndDropCommand.create(domain, unwrappedTarget, getDefaultDragAndDropLocation(), event.operations, DND.DROP_MOVE,
				selectedEObjects);

		return promptAndExecuteDropMove(domain, target, command, Messages.label_confirmMove, Messages.label_OKToMove);
	}

	/**
	 * Returns the default drop location to be used for creating {@link DragAndDropCommand}s in situations where this
	 * information cannot be obtained from the context. It should be in the range of 0.0 to 1.0, indicating the relative
	 * vertical location of the drop operation, where 0.0 is at the top and 1.0 is at the bottom.
	 * <p>
	 * This implementation returns {@link #DND_LOCATION_DROP_ON_FIRST} as default. Clients may override this method and
	 * return other values as appropriate.
	 * </p>
	 * 
	 * @return The default drag and drop location to be used for creating {@link DragAndDropCommand}s.
	 */
	protected float getDefaultDragAndDropLocation() {
		return DND_LOCATION_DROP_ON_FIRST;
	}

	/**
	 * Returns the default drag and drop operations bit mask to be used for creating {@link DragAndDropCommand}s in
	 * situations where this information cannot be obtained from the context. It is intended to be a mask of bitwise
	 * or-ed {@link DND#DROP_*} values indicating the desired drag and drop operation types.
	 * <p>
	 * This implementation returns {@link #DND_OPERATIONS_DROP_COPY_DROP_MOVE_DROP_LINK} as default. Clients may
	 * override this method and return other values as appropriate.
	 * </p>
	 * 
	 * @return The default drag and drop operations bit mask to be used for creating {@link DragAndDropCommand}s.
	 */
	protected int getDefaultDragAndDropOperations() {
		return DND_OPERATIONS_DROP_COPY_DROP_MOVE_DROP_LINK;
	}

	@SuppressWarnings("unchecked")
	protected List<Object> getSelection() {
		ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
		if (selection instanceof IStructuredSelection) {
			return ((IStructuredSelection) selection).toList();
		}
		return Collections.emptyList();
	}

	protected List<EObject> getSelectedEObjects() {
		ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
		if (selection instanceof IStructuredSelection) {
			return getSelectedEObjects((IStructuredSelection) selection);
		}
		return Collections.emptyList();
	}

	protected List<EObject> getSelectedEObjects(IStructuredSelection selection) {
		List<EObject> selectedEObject = new ArrayList<EObject>();

		for (Iterator<?> i = selection.iterator(); i.hasNext();) {
			Object o = i.next();
			if (o instanceof EObject) {
				selectedEObject.add((EObject) o);
			} else if (o instanceof IWrapperItemProvider) {
				Object obj = ((IWrapperItemProvider) o).getValue();
				if (obj instanceof EObject) {
					// interested in EObject only
					selectedEObject.add((EObject) obj);
				}
				// obj can be TransientItemProvider, skip this case
			} else if (o instanceof IAdaptable) {
				IAdaptable a = (IAdaptable) o;
				EObject eo = (EObject) a.getAdapter(EObject.class);
				if (eo != null) {
					selectedEObject.add(eo);
				}
			}
		}
		return selectedEObject;
	}

	protected Object getParent(Object object) {
		return getEditingDomain(object).getParent(object);
	}

	protected Collection<?> getChildren(Object object) {
		return getEditingDomain(object).getChildren(object);
	}

	protected TransactionalEditingDomain getEditingDomain(Object target) {
		return TransactionUtil.getEditingDomain(target instanceof IWrapperItemProvider ? ((IWrapperItemProvider) target).getValue() : target);
	}

	protected IStatus promptAndExecuteDropCopy(TransactionalEditingDomain domain, Object target, Command command, String title, String msg) {
		return promptAndExecute(domain, command, title, msg);
	}

	protected IStatus promptAndExecuteDropMove(TransactionalEditingDomain domain, Object target, Command command, String title, String msg) {
		// If the dragged objects share a parent... don't ask for confirmation
		if (command instanceof DragAndDropCommand) {
			DragAndDropCommand dndCommand = (DragAndDropCommand) command;
			Object parent = getParent(dndCommand.getOwner());
			Collection<?> children = getChildren(parent);
			if (children.containsAll(dndCommand.getCollection())) {
				return doExecute(domain, dndCommand);
			} else if (target instanceof IWrapperItemProvider) {
				IWrapperItemProvider wrapedTarget = (IWrapperItemProvider) target;
				Collection<?> wrapedChildren = getChildren(wrapedTarget);
				if (wrapedChildren.containsAll(getSelection())) {
					return promptAndExecute(domain, command, Messages.label_confirmDuplicate, Messages.label_OKToHaveDuplicate);
				}
			}
		}
		return promptAndExecute(domain, command, title, msg);
	}

	protected IStatus promptAndExecute(TransactionalEditingDomain domain, Command command, String title, String msg) {
		Shell parentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		if (MessageDialog.openConfirm(parentShell, title, msg)) {
			return doExecute(domain, command);
		}
		return Status.CANCEL_STATUS;
	}

	protected IStatus doExecute(EditingDomain domain, Command command) {
		// If the command can execute...
		if (command.canExecute()) {
			// Execute it
			domain.getCommandStack().execute(command);
		} else {
			// Otherwise, let's call the whole thing off
			command.dispose();
		}
		return Status.OK_STATUS;
	}
}

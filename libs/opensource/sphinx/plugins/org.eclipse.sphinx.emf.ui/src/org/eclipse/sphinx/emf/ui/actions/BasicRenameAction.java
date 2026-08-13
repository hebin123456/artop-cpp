/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 Continental, See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     Continental - Initial API and implementation
 *     See4sys - Enhance name feature handling, JavaDoc adds
 *     itemis - [408536] BasicRenameAction not working for non-String attributes
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.ui.actions;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.sphinx.emf.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.ui.util.RetrieveNameAttributeHelper;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * Basic implementation of action responsible for renaming a selected model element.
 */
public class BasicRenameAction extends BaseSelectionListenerAction {

	private RetrieveNameAttributeHelper helper = new RetrieveNameAttributeHelper();

	private TreeViewer viewer;
	private Composite textEditorParent;
	private Text textEditor;
	private TreeEditor treeEditor;
	private boolean saving;

	public BasicRenameAction() {
		super(Messages.action_rename_label);
	}

	public BasicRenameAction(String text) {
		super(text);
	}

	public BasicRenameAction(TreeViewer viewer) {
		this(Messages.action_rename_label);
		this.viewer = viewer;
	}

	public BasicRenameAction(String text, TreeViewer viewer) {
		this(text);
		this.viewer = viewer;
	}

	@Override
	public void run() {
		IStructuredSelection selection = getStructuredSelection();
		if (selection.size() == 1) {
			Object object = AdapterFactoryEditingDomain.unwrap(selection.getFirstElement());
			if (object instanceof EObject) {
				EObject target = (EObject) object;
				if (viewer == null) {
					// use external dialog to perform the renaming
					String oldName = getOldName(target);
					if (oldName == null) {
						oldName = ""; //$NON-NLS-1$
					}
					String newName = oldName;
					newName = changeNameDialog(ExtendedPlatformUI.getActiveShell(), oldName);
					if (!oldName.equals(newName)) {
						execRename(target, newName);
					}
				} else {
					inlineEditor(target);
				}
			}
		}
	}

	@Override
	public boolean updateSelection(IStructuredSelection selection) {
		// Enable only for single selection, which are instance of EObject or IWrapperItemProvider wrapping an EObject
		if (selection.size() == 1) {
			Object selected = selection.getFirstElement();
			if (selected instanceof EObject && helper.hasNameAttribute((EObject) selected)) {
				return true;
			} else if (selected instanceof IWrapperItemProvider) {
				Object unwrap = AdapterFactoryEditingDomain.unwrap(selected);
				return updateSelection(new StructuredSelection(unwrap));
			}
		}
		return false;
	}

	protected String getOldName(EObject object) {
		if (helper.hasNameAttribute(object)) {
			Object nameAttributeValue = object.eGet(helper.getNameAttribute(object));
			return nameAttributeValue.toString();
		}
		return ""; //$NON-NLS-1$
	}

	protected void execRename(EObject objectToRename, String newName) {
		TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(objectToRename);
		EAttribute attribute = helper.getNameAttribute(objectToRename);
		EFactory attributeValueFactory = attribute.getEType().getEPackage().getEFactoryInstance();
		Object attributeValue = attributeValueFactory.createFromString((EDataType) attribute.getEType(), newName);
		Command command = editingDomain.createCommand(SetCommand.class, new CommandParameter(objectToRename, attribute, attributeValue));
		if (command.canExecute()) {
			editingDomain.getCommandStack().execute(command);
		}
	}

	/**
	 * @param shell
	 * @param oldName
	 * @return
	 */
	protected String changeNameDialog(Shell shell, String oldName) {
		InputDialog dialog = new InputDialog(shell, Messages.dialog_rename_title, Messages.dialog_rename_message, oldName, null);
		dialog.setBlockOnOpen(true);
		int result = dialog.open();
		if (result == Window.OK) {
			return dialog.getValue();
		} else {
			return oldName;
		}

	}

	protected void inlineEditor(final EObject object) {
		// Make sure text editor is created only once. Simply reset text
		// editor when action is executed more than once. Fixes bug 22269.
		if (textEditorParent == null) {
			createTextEditor(object);
		}
		String oldName = getOldName(object);
		if (oldName != null) {
			textEditor.setText(oldName);
		}

		// Open text editor with initial size.
		textEditorParent.setVisible(true);
		Point textSize = textEditor.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		textSize.x += textSize.y; // Add extra space for new characters.
		Point parentSize = textEditorParent.getSize();
		int inset = 1;
		textEditor.setBounds(2, inset, Math.min(textSize.x, parentSize.x - 4), parentSize.y - 2 * inset);
		textEditorParent.redraw();
		textEditor.selectAll();
		textEditor.setFocus();
	}

	/**
	 * Create the text editor widget.
	 *
	 * @param objectToRename
	 *            the resource to rename
	 */
	protected void createTextEditor(final EObject objectToRename) {
		// Create text editor parent. This draws a nice bounding rectangle.
		textEditorParent = createParent();
		textEditorParent.setVisible(false);
		final int inset = 1;
		if (inset > 0) {
			textEditorParent.addListener(SWT.Paint, new Listener() {
				@Override
				public void handleEvent(Event e) {
					Point textSize = textEditor.getSize();
					Point parentSize = textEditorParent.getSize();
					e.gc.drawRectangle(0, 0, Math.min(textSize.x + 4, parentSize.x - 1), parentSize.y - 1);
				}
			});
		}
		// Create inner text editor.
		textEditor = new Text(textEditorParent, SWT.NONE);
		textEditor.setFont(viewer.getTree().getFont());
		textEditorParent.setBackground(textEditor.getBackground());
		textEditor.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event e) {
				Point textSize = textEditor.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				textSize.x += textSize.y; // Add extra space for new
				// characters.
				Point parentSize = textEditorParent.getSize();
				textEditor.setBounds(2, inset, Math.min(textSize.x, parentSize.x - 4), parentSize.y - 2 * inset);
				textEditorParent.redraw();
			}
		});
		textEditor.addListener(SWT.Traverse, new Listener() {
			@Override
			public void handleEvent(Event event) {

				// Workaround for Bug 20214 due to extra
				// traverse events
				switch (event.detail) {
				case SWT.TRAVERSE_ESCAPE:
					// Do nothing in this case
					disposeTextWidget();
					event.doit = true;
					event.detail = SWT.TRAVERSE_NONE;
					break;
				case SWT.TRAVERSE_RETURN:
					saveChangesAndDispose(objectToRename);
					event.doit = true;
					event.detail = SWT.TRAVERSE_NONE;
					break;
				}
			}
		});
		textEditor.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent fe) {
				saveChangesAndDispose(objectToRename);
			}
		});
	}

	/**
	 * Close the text widget and reset the editorText field.
	 */
	protected void disposeTextWidget() {

		if (textEditorParent != null) {
			textEditorParent.dispose();
			textEditorParent = null;
			textEditor = null;
			if (treeEditor != null) {
				treeEditor.dispose();
				treeEditor = null;
			}
		}
	}

	protected void saveChangesAndDispose(EObject object) {
		if (saving == true) {
			return;
		}
		saving = true;

		// Cache the resource to avoid selection loss since a selection of
		// another item can trigger this method
		final EObject inlinedObject = object;
		final String newName = textEditor.getText();

		// Run this in an async to make sure that the operation that triggered
		// this action is completed. Otherwise this leads to problems when the
		// icon of the item being renamed is clicked (i.e., which causes the
		// rename text widget to lose focus and trigger this method).
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					String oldName = getOldName(inlinedObject);
					if (oldName == null) {
						oldName = ""; //$NON-NLS-1$
					}
					if (!oldName.equals(newName)) {
						execRename(inlinedObject, newName);
						if (viewer != null) {
							viewer.refresh(inlinedObject, true);
						}
					}

					// Dispose the text widget regardless
					disposeTextWidget();
					// Ensure the viewer tree has focus, which it may not if the
					// text widget previously had focus.
					if (viewer != null) {
						viewer.refresh(inlinedObject, true);
					}
				} finally {
					saving = false;
				}
			}
		};
		ExtendedPlatformUI.getDisplay().asyncExec(runnable);
	}

	protected Composite createParent() {
		Tree tree = viewer.getTree();
		Composite result = new Composite(tree, SWT.NONE);
		TreeItem[] selectedItems = tree.getSelection();
		treeEditor = new TreeEditor(tree);
		treeEditor.horizontalAlignment = SWT.LEFT;
		treeEditor.grabHorizontal = true;
		treeEditor.setEditor(result, selectedItems[0]);
		return result;
	}
}

/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys and others.
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
package org.eclipse.sphinx.xtendxpand.ui.groups;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.sphinx.platform.ui.groups.AbstractGroup;
import org.eclipse.sphinx.platform.ui.util.SWTUtil;
import org.eclipse.sphinx.xtendxpand.outlet.ExtendedOutlet;
import org.eclipse.sphinx.xtendxpand.ui.dialogs.EditOutletDialog;
import org.eclipse.sphinx.xtendxpand.ui.internal.messages.Messages;
import org.eclipse.sphinx.xtendxpand.ui.outlet.providers.OutletProvider;
import org.eclipse.sphinx.xtendxpand.ui.outlet.providers.OutletTableContentProvider;
import org.eclipse.sphinx.xtendxpand.ui.outlet.providers.OutletTableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.xpand2.output.Outlet;

public class OutletsGroup extends AbstractGroup {

	/**
	 * The table presenting the outlets.
	 */
	private TableViewer tableViewer;

	/**
	 * The outlet provider.
	 */
	private OutletProvider outletProvider;

	/**
	 * The add outlets button. to add, edit and remove outlets.
	 */
	private Button addButton;

	/**
	 * The edit outlets button.
	 */
	private Button editButton;

	/**
	 * The remove outlets button.
	 */
	private Button removeButton;

	/**
	 * The boolean that indicates if add, edit and remove outlets button should be shown or not. These buttons are
	 * displayed by default.
	 */
	private boolean addButtons = true;

	private Listener listener = new Listener() {

		@Override
		public void handleEvent(Event event) {
			if (event.widget == addButton) {
				add();
			} else if (event.widget == editButton) {
				edit();
			} else if (event.widget == removeButton) {
				remove();
			}
		}
	};

	public OutletsGroup(String groupName, OutletProvider outletProvider, boolean addButtons) {
		this(groupName, outletProvider, addButtons, null);
	}

	public OutletsGroup(String groupName, OutletProvider outletProvider) {
		this(groupName, outletProvider, true, null);
	}

	public OutletsGroup(String groupName, OutletProvider outletProvider, boolean addButtons, IDialogSettings dialogSettings) {
		super(groupName, dialogSettings);

		this.groupName = groupName;
		this.outletProvider = outletProvider;
		this.addButtons = addButtons;
	}

	@Override
	protected void doCreateContent(Composite parent, int numColumns) {
		Assert.isNotNull(parent.getShell());

		parent.setLayout(new GridLayout(numColumns, false));

		GC gc = new GC(parent.getShell());
		gc.setFont(JFaceResources.getDialogFont());

		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 360;
		data.heightHint = convertHeightInCharsToPixels(10, gc);
		tableComposite.setLayoutData(data);

		TableColumnLayout columnLayout = new TableColumnLayout();
		tableComposite.setLayout(columnLayout);
		Table table = new Table(tableComposite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn nameColumn = new TableColumn(table, SWT.NONE);
		nameColumn.setText(Messages.label_OutletsGroup_TableColumn_Name);
		int minWidth = computeMinimumColumnWidth(gc, Messages.label_OutletsGroup_TableColumn_Name);
		columnLayout.setColumnData(nameColumn, new ColumnWeightData(2, minWidth, true));

		TableColumn pathColumn = new TableColumn(table, SWT.NONE);
		pathColumn.setText(Messages.label_OutletsGroup_TableColumn_Path);
		minWidth = computeMinimumColumnWidth(gc, Messages.label_OutletsGroup_TableColumn_Path);
		columnLayout.setColumnData(pathColumn, new ColumnWeightData(4, minWidth, true));

		TableColumn protectedRegionColumn = new TableColumn(table, SWT.NONE);
		protectedRegionColumn.setText(Messages.label_Protected_Region);
		minWidth = computeMinimumColumnWidth(gc, Messages.label_OutletsGroup_TableColumn_Path);
		columnLayout.setColumnData(protectedRegionColumn, new ColumnWeightData(2, minWidth, true));

		gc.dispose();

		tableViewer = new TableViewer(table);
		tableViewer.setLabelProvider(new OutletTableLabelProvider());
		tableViewer.setContentProvider(new OutletTableContentProvider());
		tableViewer.setInput(outletProvider);

		if (addButtons) {
			addTableViewerListener();
			addButtons(parent);
		}
	}

	protected void addTableViewerListener() {
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent e) {
				edit();
			}
		});

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				updateButtons();
			}
		});
	}

	/**
	 * Adds the add, edit and remove outlets buttons.
	 */
	protected void addButtons(Composite parent) {
		Composite buttonsComposite = new Composite(parent, SWT.NONE);
		buttonsComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		GridLayout blayout = new GridLayout();
		blayout.marginHeight = 0;
		blayout.marginWidth = 0;
		buttonsComposite.setLayout(blayout);

		addButton = SWTUtil.createButton(buttonsComposite, Messages.label_AddButton, SWT.PUSH);
		addButton.addListener(SWT.Selection, listener);

		editButton = SWTUtil.createButton(buttonsComposite, Messages.label_EditButton, SWT.PUSH);
		editButton.addListener(SWT.Selection, listener);

		removeButton = SWTUtil.createButton(buttonsComposite, Messages.label_RemoveButton, SWT.PUSH);
		removeButton.addListener(SWT.Selection, listener);
		updateButtons();
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}

	public void setEnabled(boolean enabled) {
		tableViewer.getTable().setEnabled(enabled);
	}

	public Composite getButtonsComposite() {
		if (addButton != null) {
			return addButton.getParent();
		}
		return null;
	}

	/**
	 * Updates the buttons.
	 */
	protected void updateButtons() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		int selectionCount = selection.size();
		int itemCount = tableViewer.getTable().getItemCount();
		editButton.setEnabled(selectionCount == 1);
		removeButton.setEnabled(selectionCount > 0 && selectionCount <= itemCount && !containsDefaultOutlet(selection));
	}

	protected void add() {
		ExtendedOutlet outlet = editOutlet(new ExtendedOutlet(), false, true);
		if (outlet != null) {
			outletProvider.addOutlet(outlet);
			tableViewer.refresh();
			tableViewer.setSelection(new StructuredSelection(outlet));
		}
	}

	protected void edit() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		ExtendedOutlet selectedOutlet = (ExtendedOutlet) selection.getFirstElement();
		ExtendedOutlet outletToEdit = new ExtendedOutlet(selectedOutlet.getPathExpression(), outletProvider.getProject());
		outletToEdit.setName(selectedOutlet.getName());
		outletToEdit.setProtectedRegion(selectedOutlet.isProtectedRegion());
		ExtendedOutlet editedOutlet = editOutlet(outletToEdit, true, selectedOutlet.getName() != null);
		if (editedOutlet != null) {
			selectedOutlet.setPathExpression(editedOutlet.getPathExpression(), outletProvider.getProject());
			selectedOutlet.setName(editedOutlet.getName());
			selectedOutlet.setProtectedRegion(editedOutlet.isProtectedRegion());
			tableViewer.refresh();
		}
	}

	protected void remove() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		for (Object element : selection.toList()) {
			outletProvider.removeOutlet((ExtendedOutlet) element);
		}
		tableViewer.refresh();
	}

	protected ExtendedOutlet editOutlet(ExtendedOutlet outlet, boolean edit, boolean isNameModifiable) {
		EditOutletDialog dialog = new EditOutletDialog(getTableViewer().getControl().getShell(), outlet, edit, isNameModifiable, outletProvider);
		if (dialog.open() == Window.OK) {
			return dialog.getOutlet();
		}
		return null;
	}

	/**
	 * Verify if the selected element has an existing default outlet or not.
	 */
	protected boolean containsDefaultOutlet(IStructuredSelection selection) {
		for (Object element : selection.toList()) {
			if (((Outlet) element).getName() == null) {
				return true;
			}
		}
		return false;
	}

	protected int convertHeightInCharsToPixels(int chars, GC gc) {
		if (gc.getFontMetrics() == null) {
			return 0;
		}
		return Dialog.convertHeightInCharsToPixels(gc.getFontMetrics(), chars);
	}

	private int computeMinimumColumnWidth(GC gc, String string) {
		return gc.stringExtent(string).x + 10;
	}

	public void setToDefault() {
		outletProvider.setToDefault();
	}

	public void store() {
		outletProvider.store();
	}

	public void dispose() {
		outletProvider.dispose();
	}

	public Button getAddButton() {
		return addButton;
	}

	public Button getEditButton() {
		return editButton;
	}

	public Button getRemoveButton() {
		return removeButton;
	}
}

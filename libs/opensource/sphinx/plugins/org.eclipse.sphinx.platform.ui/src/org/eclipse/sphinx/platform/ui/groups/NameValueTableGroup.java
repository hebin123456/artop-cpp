/**
 * <copyright>
 *
 * Copyright (c) 2016-2017 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [506671] Add support for specifying and injecting user-defined arguments for workflows through workflow launch configurations
 *     itemis - [511105] Ensure Mars compatibility & fix compilation errors under Oxygen due to internal API usage
 *
 * </copyright>
 */
package org.eclipse.sphinx.platform.ui.groups;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.sphinx.platform.ui.groups.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class NameValueTableGroup extends AbstractGroup {

	protected static final String DEFAULT_NAME = Messages.cell_name_default;

	/**
	 * Content provider for the name/value table
	 */
	protected static class NameValueViewerContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, String> entries = (Map<String, String>) inputElement;
				return entries.entrySet().toArray(new Object[entries.size()]);
			}
			return new Object[0];
		}

		@Override
		public void dispose() {
			// Do nothing
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput == null) {
				return;
			}
			if (viewer instanceof TableViewer) {
				TableViewer tableViewer = (TableViewer) viewer;
				if (tableViewer.getTable().isDisposed()) {
					return;
				}
				tableViewer.setComparator(new ViewerComparator() {
					@Override
					@SuppressWarnings("unchecked")
					public int compare(Viewer viewer, Object e1, Object e2) {
						if (e1 == null) {
							return -1;
						} else if (e2 == null) {
							return 1;
						} else {
							Map.Entry<String, String> entry1 = (Map.Entry<String, String>) e1;
							Map.Entry<String, String> entry2 = (Map.Entry<String, String>) e2;
							if (entry1.getKey() == null) {
								return -1;
							} else if (entry2.getKey() == null) {
								return 1;
							} else {
								return entry1.getKey().compareToIgnoreCase(entry2.getKey());
							}
						}
					}
				});
			}
		}
	}

	protected static class NameColumnLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			@SuppressWarnings("unchecked")
			Map.Entry<String, String> entry = (Map.Entry<String, String>) element;
			return entry.getKey();
		}
	}

	protected static class ValueColumnLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			@SuppressWarnings("unchecked")
			Map.Entry<String, String> entry = (Map.Entry<String, String>) element;
			return entry.getValue();
		}
	}

	protected abstract class AbstractTextEditingSupport extends EditingSupport {
		protected TableViewer viewer;
		protected CellEditor editor;

		public AbstractTextEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
			editor = new TextCellEditor(viewer.getTable());
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected final void setValue(Object element, Object value) {
			// Perform value change
			doSetValue(element, value);

			// Notify listeners to enclosing group
			notifyGroupChanged(null);
		}

		protected abstract void doSetValue(Object element, Object value);
	}

	protected class NameEditingSupport extends AbstractTextEditingSupport {

		public NameEditingSupport(TableViewer viewer) {
			super(viewer);
		}

		@Override
		@SuppressWarnings("unchecked")
		protected Object getValue(Object element) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) element;
			return entry.getKey();
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void doSetValue(Object element, Object value) {
			Map.Entry<String, String> oldEntry = (Map.Entry<String, String>) element;
			final String newName = String.valueOf(value);

			Map<String, String> entries = (Map<String, String>) viewer.getInput();
			entries.remove(oldEntry.getKey());
			entries.put(newName, oldEntry.getValue());

			viewer.refresh();

			/*
			 * Usability tweak: Start editing of an entry's value when done with the editing of its name; this avoids
			 * unnecessary mouse clicks in between and facilitates the localization of the entry when it "jumps" at
			 * another table position due to automatic sorting of the entries based on their names
			 */
			/*
			 * !! Important Note !! Make sure that editing of the entry's value is started after a little delay only so
			 * that the editing of the entry's name can be fully completed before. Otherwise, the editing of the entry's
			 * value would be started anyway but the resulting new value wouldn't be saved and the old value would
			 * remain in place and reappear in the table cell when done with the editing.
			 */
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
						Entry<String, String> newEntry = findEntry(newName);
						nameValueViewer.editElement(newEntry, 1);
					}
				}
			});
		}
	}

	protected class ValueEditingSupport extends AbstractTextEditingSupport {

		public ValueEditingSupport(TableViewer viewer) {
			super(viewer);
		}

		@Override
		@SuppressWarnings("unchecked")
		protected Object getValue(Object element) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) element;
			return entry.getValue();
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void doSetValue(Object element, Object value) {
			String newName = String.valueOf(value);

			Map.Entry<String, String> entry = (Map.Entry<String, String>) element;
			entry.setValue(newName);

			viewer.update(element, null);
		}
	}

	protected TableViewer nameValueViewer;
	protected Button addButton;
	protected Button removeButton;

	public NameValueTableGroup(String groupName) {
		super(groupName);
	}

	public NameValueTableGroup(String groupName, IDialogSettings dialogSettings) {
		super(groupName, dialogSettings);
	}

	@Override
	protected void doCreateContent(Composite parent, int numColumns) {
		// Create and parent layout with 2 column
		// TODO Consider to remove numColumns parameters from createContents()/doCreateContents and make it a
		// responsibility of the group implementations to determine how many columns they want to provide in their
		// layout (as it is done right here already)
		parent.setLayout(new GridLayout(2, false));

		// Create name/value table area
		createTableArea(parent);

		// Create add/remove button area
		createButtonArea(parent);

		// Trigger update of add/remove button enablement
		handleTableSelectionChanged(StructuredSelection.EMPTY);
	}

	protected void createTableArea(Composite parent) {
		// Create table composite
		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComposite);
		tableComposite.setFont(parent.getFont());

		// Create table and table viewer
		nameValueViewer = new TableViewer(tableComposite, getTableStyle());
		Table nameValueTable = nameValueViewer.getTable();
		GridDataFactory.fillDefaults().applyTo(nameValueTable);
		nameValueTable.setFont(parent.getFont());
		nameValueTable.setHeaderVisible(true);
		nameValueTable.setLinesVisible(true);
		nameValueViewer.setContentProvider(new NameValueViewerContentProvider());
		nameValueViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				handleTableSelectionChanged(event.getSelection());
			}
		});

		// Create table columns
		TableViewerColumn nameColumn = new TableViewerColumn(nameValueViewer, SWT.NONE);
		nameColumn.getColumn().setText(Messages.column_name_label);
		nameColumn.setLabelProvider(new NameColumnLabelProvider());
		nameColumn.setEditingSupport(new NameEditingSupport(nameValueViewer));

		TableViewerColumn valueColumn = new TableViewerColumn(nameValueViewer, SWT.NONE);
		valueColumn.getColumn().setText(Messages.column_value_label);
		valueColumn.setLabelProvider(new ValueColumnLabelProvider());
		valueColumn.setEditingSupport(new ValueEditingSupport(nameValueViewer));

		// Create table column layout
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		PixelConverter pixelConverter = new PixelConverter(parent.getFont());
		tableColumnLayout.setColumnData(nameColumn.getColumn(), new ColumnWeightData(1, pixelConverter.convertWidthInCharsToPixels(20)));
		tableColumnLayout.setColumnData(valueColumn.getColumn(), new ColumnWeightData(2, pixelConverter.convertWidthInCharsToPixels(20)));
		tableComposite.setLayout(tableColumnLayout);
	}

	protected int getTableStyle() {
		return SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION;
	}

	protected void createButtonArea(Composite parent) {
		// Create button composite
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(GridData.END, GridData.BEGINNING).applyTo(buttonComposite);
		buttonComposite.setFont(parent.getFont());
		buttonComposite.setLayout(new GridLayout(1, false));

		// Create buttons
		addButton = new Button(buttonComposite, SWT.PUSH);
		GridDataFactory.swtDefaults().align(GridData.FILL, GridData.BEGINNING).hint(computeButtonHint(addButton)).applyTo(addButton);
		addButton.setFont(parent.getFont());
		addButton.setText(Messages.button_add_label);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleAddButtonSelected();
			}
		});

		removeButton = new Button(buttonComposite, SWT.PUSH);
		GridDataFactory.swtDefaults().align(GridData.FILL, GridData.BEGINNING).hint(computeButtonHint(removeButton)).applyTo(removeButton);
		removeButton.setFont(parent.getFont());
		removeButton.setText(Messages.button_remove_label);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleRemoveButtonSelected();
			}
		});
	}

	protected Point computeButtonHint(Button button) {
		PixelConverter converter = new PixelConverter(button);
		int dialogWidthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point preferredSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		return new Point(Math.max(dialogWidthHint, preferredSize.x), preferredSize.y);
	}

	/**
	 * Responds to a selection changed event in the name/value table
	 *
	 * @param selection
	 *            the selection
	 */
	protected void handleTableSelectionChanged(ISelection selection) {
		/*
		 * Usability tweak: Make sure that Remove button gets only enabled when one or multiple name/value entries have
		 * been selected
		 */
		int size = ((IStructuredSelection) selection).size();
		removeButton.setEnabled(size > 0);

		/*
		 * Usability tweak: Make sure that Add button remains disabled when a new name/value entry has been added but
		 * its name has not yet been changed away from its default
		 */
		@SuppressWarnings("unchecked")
		Map<String, String> entries = (Map<String, String>) nameValueViewer.getInput();
		addButton.setEnabled(entries != null && !entries.containsKey(DEFAULT_NAME));
	}

	/**
	 * Adds a new name/value entry to the table.
	 */
	protected void handleAddButtonSelected() {
		// Add new name/value entry using default values
		addEntry(DEFAULT_NAME, Messages.cell_value_default);

		/*
		 * Usability tweak: Select newly added name/value entry
		 */
		Entry<String, String> entry = findEntry(DEFAULT_NAME);
		nameValueViewer.setSelection(new StructuredSelection(entry));
	}

	/**
	 * Removes the selected name/value entry from the table.
	 */
	@SuppressWarnings("unchecked")
	protected void handleRemoveButtonSelected() {
		Assert.isNotNull(nameValueViewer);

		int oldSelectionIdx = nameValueViewer.getTable().getSelectionIndex();

		// Remove selected name/value entries
		Map<String, String> entries = (Map<String, String>) nameValueViewer.getInput();
		for (Object selected : ((IStructuredSelection) nameValueViewer.getSelection()).toList()) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) selected;
			entries.remove(entry.getKey());
		}
		nameValueViewer.refresh();

		/*
		 * Usability tweak: Select name/value entry next to remove name/value entries
		 */
		int newSelectionIdx = oldSelectionIdx < entries.size() ? oldSelectionIdx : entries.size() - 1;
		if (newSelectionIdx != -1) {
			TableItem newSelected = nameValueViewer.getTable().getItem(newSelectionIdx);
			nameValueViewer.setSelection(new StructuredSelection(newSelected.getData()));
		}
	}

	public void setInput(Map<String, String> entries) {
		Assert.isNotNull(nameValueViewer);

		// Set new name/value entries as viewer input
		nameValueViewer.setInput(entries);

		// Trigger update of add/remove button enablement
		handleTableSelectionChanged(StructuredSelection.EMPTY);
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getInput() {
		Assert.isNotNull(nameValueViewer);

		// Return current name/value entries by retrieving viewer input
		return (Map<String, String>) nameValueViewer.getInput();
	}

	public void addEntry(String name, String value) {
		Assert.isNotNull(nameValueViewer);

		@SuppressWarnings("unchecked")
		Map<String, String> entries = (Map<String, String>) nameValueViewer.getInput();
		entries.put(name, value);

		Entry<String, String> entry = findEntry(name);
		nameValueViewer.add(entry);
	}

	protected Map.Entry<String, String> findEntry(String name) {
		Assert.isNotNull(nameValueViewer);

		@SuppressWarnings("unchecked")
		Map<String, String> entries = (Map<String, String>) nameValueViewer.getInput();
		for (Map.Entry<String, String> entry : entries.entrySet()) {
			if (name.equals(entry.getKey())) {
				return entry;
			}
		}
		return null;
	}
}

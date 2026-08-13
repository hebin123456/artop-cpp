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
package org.eclipse.sphinx.platform.ui.fields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.sphinx.platform.ui.fields.adapters.IListAdapter;
import org.eclipse.sphinx.platform.ui.internal.util.LayoutUtil;
import org.eclipse.sphinx.platform.ui.internal.util.PixelConverter;
import org.eclipse.sphinx.platform.ui.internal.util.TableLayoutComposite;
import org.eclipse.sphinx.platform.ui.util.SWTUtil;
import org.eclipse.sphinx.platform.ui.widgets.IWidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * A list with a button bar. Typical buttons are 'Add', 'Remove', 'Up' and 'Down'. List model is independent of widget
 * creation. DialogFields controls are: Label, List and Composite containing buttons.
 */
public class ListField extends BasicField {

	public static class ColumnsDescription {
		private ColumnLayoutData[] columns;
		private String[] headers;
		private boolean drawLines;

		public ColumnsDescription(ColumnLayoutData[] columns, String[] headers, boolean drawLines) {
			this.columns = columns;
			this.headers = headers;
			this.drawLines = drawLines;
		}

		public ColumnsDescription(String[] headers, boolean drawLines) {
			this(createColumnWeightData(headers.length), headers, drawLines);
		}

		public ColumnsDescription(int nColumns, boolean drawLines) {
			this(createColumnWeightData(nColumns), null, drawLines);
		}

		private static ColumnLayoutData[] createColumnWeightData(int nColumns) {
			ColumnLayoutData[] data = new ColumnLayoutData[nColumns];
			for (int i = 0; i < nColumns; i++) {
				data[i] = new ColumnWeightData(1);
			}
			return data;
		}
	}

	protected TableViewer fTable;
	protected Control fTableControl;
	protected ILabelProvider fLabelProvider;
	protected ListViewerAdapter fListViewerAdapter;
	protected List<Object> fElements;
	protected ViewerComparator fViewerComparator;

	protected String[] fButtonLabels;
	private Button[] fButtonControls;

	private boolean[] fButtonsEnabled;

	private int fRemoveButtonIndex;
	private int fUpButtonIndex;
	private int fDownButtonIndex;

	private Label fLastSeparator;

	private Composite fButtonsControl;
	private ISelection fSelectionWhenEnabled;

	private IListAdapter fListAdapter;

	private Object fParentElement;

	private ColumnsDescription fTableColumns;

	/**
	 * Creates the <code>ListField</code>.
	 * 
	 * @param listAdapter
	 *            A listener for button invocation, selection changes. Can be <code>null</code>.
	 * @param buttonLabels
	 *            The labels of all buttons: <code>null</code> is a valid array entry and marks a separator.
	 * @param labelProvider
	 *            The label provider to render the table entries
	 */
	public ListField(IListAdapter listAdapter, String[] buttonLabels, ILabelProvider labelProvider) {
		this(null, listAdapter, buttonLabels, labelProvider);
	}

	public ListField(IWidgetFactory widgetFactory, IListAdapter listAdapter, String[] buttonLabels, ILabelProvider labelProvider) {
		super(widgetFactory);
		fListAdapter = listAdapter;

		fLabelProvider = labelProvider;
		fListViewerAdapter = new ListViewerAdapter();
		fParentElement = this;

		fElements = new ArrayList<Object>(10);

		fButtonLabels = buttonLabels;
		if (fButtonLabels != null && fButtonLabels.length > 0) {
			int nButtons = fButtonLabels.length;
			fButtonsEnabled = new boolean[nButtons];
			for (int i = 0; i < nButtons; i++) {
				fButtonsEnabled[i] = true;
			}
		}

		fTable = null;
		fTableControl = null;
		fButtonsControl = null;
		fTableColumns = null;

		fRemoveButtonIndex = -1;
		fUpButtonIndex = -1;
		fDownButtonIndex = -1;
	}

	/**
	 * Sets the index of the 'remove' button in the button label array passed in the constructor. The behavior of the
	 * button marked as the 'remove' button will then be handled internally. (enable state, button invocation behavior)
	 */
	public void setRemoveButtonIndex(int removeButtonIndex) {
		Assert.isTrue(removeButtonIndex < fButtonLabels.length);
		fRemoveButtonIndex = removeButtonIndex;
	}

	/**
	 * Sets the index of the 'up' button in the button label array passed in the constructor. The behavior of the button
	 * marked as the 'up' button will then be handled internally. (enable state, button invocation behavior)
	 */
	public void setUpButtonIndex(int upButtonIndex) {
		Assert.isTrue(upButtonIndex < fButtonLabels.length);
		fUpButtonIndex = upButtonIndex;
	}

	/**
	 * Sets the index of the 'down' button in the button label array passed in the constructor. The behavior of the
	 * button marked as the 'down' button will then be handled internally. (enable state, button invocation behavior)
	 */
	public void setDownButtonIndex(int downButtonIndex) {
		Assert.isTrue(downButtonIndex < fButtonLabels.length);
		fDownButtonIndex = downButtonIndex;
	}

	/**
	 * Sets the viewer comparator.
	 * 
	 * @param viewerComparator
	 *            The viewer comparator to set
	 */
	public void setViewerComparator(ViewerComparator viewerComparator) {
		fViewerComparator = viewerComparator;
	}

	public void setTableColumns(ColumnsDescription column) {
		fTableColumns = column;
	}

	// ------ adapter communication

	private void buttonPressed(int index) {
		if (!managedButtonPressed(index) && fListAdapter != null) {
			fListAdapter.customButtonPressed(this, index);
		}
	}

	/**
	 * Checks if the button pressed is handled internally
	 * 
	 * @return Returns true if button has been handled.
	 */
	protected boolean managedButtonPressed(int index) {
		if (index == fRemoveButtonIndex) {
			remove();
		} else if (index == fUpButtonIndex) {
			up();
			if (!fButtonControls[index].isEnabled() && fDownButtonIndex != -1) {
				fButtonControls[fDownButtonIndex].setFocus();
			}
		} else if (index == fDownButtonIndex) {
			down();
			if (!fButtonControls[index].isEnabled() && fUpButtonIndex != -1) {
				fButtonControls[fUpButtonIndex].setFocus();
			}
		} else {
			return false;
		}
		return true;
	}

	// ------ layout helpers

	/*
	 * @see BasicField#doFillIntoGrid
	 */
	@Override
	protected Control[] doFillIntoGrid(Composite parent, int nColumns) {
		PixelConverter converter = new PixelConverter(parent);

		Control label = getLabelControl(parent, 1);

		Control list = getListControl(parent);
		// Leave last column for buttons if such are defined, span over buttons column otherwise
		int nListColumns = hasButtons() ? nColumns - 2 : nColumns - 1;
		if (fUseFormLayout) {
			list.setLayoutData(LayoutUtil.tableWrapDataForList(nListColumns, converter));
		} else {
			list.setLayoutData(LayoutUtil.gridDataForList(nListColumns, converter));
		}

		Composite buttons = getButtonBox(parent);
		if (buttons != null) {
			if (fUseFormLayout) {
				buttons.setLayoutData(LayoutUtil.tableWrapDataForButtons(1));
			} else {
				buttons.setLayoutData(LayoutUtil.gridDataForButtons(1));
			}
		}

		return buttons != null ? new Control[] { label, list, buttons } : new Control[] { label, list };
	}

	/*
	 * @see BasicField#getNumberOfControls
	 */
	@Override
	public int getNumberOfControls() {
		return 3;
	}

	/**
	 * Sets the minimal width of the buttons. Must be called after widget creation.
	 */
	public void setButtonsMinWidth(int minWidth) {
		if (fLastSeparator != null) {
			((GridData) fLastSeparator.getLayoutData()).widthHint = minWidth;
		}
	}

	// ------ UI creation

	/**
	 * Returns the list control. When called the first time, the control will be created.
	 * 
	 * @param parent
	 *            The parent composite when called the first time, or <code>null</code> after.
	 */
	public Control getListControl(Composite parent) {
		if (fTableControl == null) {
			assertCompositeNotNull(parent);

			if (fTableColumns == null) {
				fTable = createTableViewer(parent);
				Table tableControl = fTable.getTable();

				fTableControl = tableControl;
				tableControl.setLayout(new TableLayout());
			} else {
				TableLayoutComposite composite = new TableLayoutComposite(parent, SWT.NONE);
				composite.setFont(parent.getFont());
				fTableControl = composite;

				fTable = createTableViewer(composite);
				Table tableControl = fTable.getTable();

				tableControl.setHeaderVisible(fTableColumns.headers != null);
				tableControl.setLinesVisible(fTableColumns.drawLines);
				ColumnLayoutData[] columns = fTableColumns.columns;
				for (int i = 0; i < columns.length; i++) {
					composite.addColumnData(columns[i]);
					TableColumn column = new TableColumn(tableControl, SWT.NONE);
					// tableLayout.addColumnData(columns[i]);
					if (fTableColumns.headers != null) {
						column.setText(fTableColumns.headers[i]);
					}
				}
			}

			fTable.getTable().addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					handleKeyPressed(e);
				}
			});

			// fTableControl.setLayout(tableLayout);

			fTable.setContentProvider(fListViewerAdapter);
			fTable.setLabelProvider(fLabelProvider);
			fTable.addSelectionChangedListener(fListViewerAdapter);
			fTable.addDoubleClickListener(fListViewerAdapter);

			fTable.setInput(fParentElement);

			if (fViewerComparator != null) {
				fTable.setComparator(fViewerComparator);
			}

			fTableControl.setEnabled(isEnabled());
			if (fSelectionWhenEnabled != null) {
				selectElements(fSelectionWhenEnabled);
			}
		}
		return fTableControl;
	}

	// TODO Rework this in order to be compliant with others fields.
	public Control getListControl() {
		Control control = null;
		if (isOkToUse(fTableControl)) {
			control = fTableControl;
		}
		return control;
	}

	/**
	 * Returns the internally used table viewer.
	 */
	public TableViewer getTableViewer() {
		return fTable;
	}

	/*
	 * Subclasses may override to specify a different style.
	 */
	protected int getListStyle() {
		int style = SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL;
		if (fTableColumns != null) {
			style |= SWT.FULL_SELECTION;
		}
		return style;
	}

	protected TableViewer createTableViewer(Composite parent) {
		Table table = new Table(parent, getListStyle());
		table.setFont(parent.getFont());
		return new TableViewer(table);
	}

	protected Button createButton(Composite parent, String label, SelectionListener listener) {
		Button button = new Button(parent, fUseFormLayout ? SWT.FLAT : SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText(label);
		button.addSelectionListener(listener);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.widthHint = SWTUtil.getButtonWidthHint(button);

		button.setLayoutData(gd);

		return button;
	}

	private Label createSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setFont(parent.getFont());
		separator.setVisible(false);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.verticalIndent = 4;
		separator.setLayoutData(gd);
		return separator;
	}

	/**
	 * Returns weather this ListField contains any buttons or not.
	 * 
	 * @return <tt>true</tt> if this ListField contains any buttons, <tt>false<tt/> otherwise.
	 */
	protected boolean hasButtons() {
		return fButtonLabels != null && fButtonLabels.length > 0;
	}

	/**
	 * Returns the composite containing the buttons. When called the first time, the control will be created.
	 * 
	 * @param parent
	 *            The parent composite when called the first time, or <code>null</code> after.
	 */
	public Composite getButtonBox(Composite parent) {
		if (fButtonsControl == null && fButtonLabels != null && fButtonLabels.length > 0) {
			assertCompositeNotNull(parent);

			SelectionListener listener = new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					doButtonSelected(e);
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					doButtonSelected(e);
				}
			};

			Composite contents = new Composite(parent, SWT.NONE);
			contents.setFont(parent.getFont());
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			contents.setLayout(layout);

			fButtonControls = new Button[fButtonLabels.length];
			for (int i = 0; i < fButtonLabels.length; i++) {
				String currLabel = fButtonLabels[i];
				if (currLabel != null) {
					fButtonControls[i] = createButton(contents, currLabel, listener);
					fButtonControls[i].setEnabled(isEnabled() && fButtonsEnabled[i]);
				} else {
					fButtonControls[i] = null;
					createSeparator(contents);
				}
			}

			fLastSeparator = createSeparator(contents);

			updateButtonState();
			fButtonsControl = contents;
		}

		return fButtonsControl;
	}

	private void doButtonSelected(SelectionEvent e) {
		if (fButtonControls != null) {
			for (int i = 0; i < fButtonControls.length; i++) {
				if (e.widget == fButtonControls[i]) {
					buttonPressed(i);
					return;
				}
			}
		}
	}

	/**
	 * Handles key events in the table viewer. Specifically when the delete key is pressed.
	 */
	protected void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0) {
			if (fRemoveButtonIndex != -1 && isButtonEnabled(fTable.getSelection(), fRemoveButtonIndex)) {
				managedButtonPressed(fRemoveButtonIndex);
			}
		}
	}

	// ------ enable / disable and dispose management

	/*
	 * @see BasicField#dialogFieldChanged
	 */
	@Override
	public void dialogFieldChanged() {
		super.dialogFieldChanged();
		updateButtonState();
	}

	/*
	 * Updates the enable state of the all buttons
	 */
	protected void updateButtonState() {
		if (fButtonControls != null && isOkToUse(fTableControl) && fTableControl.isEnabled()) {
			ISelection sel = fTable.getSelection();
			for (int i = 0; i < fButtonControls.length; i++) {
				Button button = fButtonControls[i];
				if (isOkToUse(button)) {
					button.setEnabled(isButtonEnabled(sel, i));
				}
			}
		}
	}

	protected boolean getManagedButtonState(ISelection sel, int index) {
		if (index == fRemoveButtonIndex) {
			return !sel.isEmpty();
		} else if (index == fUpButtonIndex) {
			return !sel.isEmpty() && canMoveUp();
		} else if (index == fDownButtonIndex) {
			return !sel.isEmpty() && canMoveDown();
		}
		return true;
	}

	/*
	 * @see BasicField#updateEnableState
	 */
	@Override
	protected void updateEnableState() {
		super.updateEnableState();

		boolean enabled = isEnabled();
		if (isOkToUse(fTableControl)) {
			if (!enabled) {
				if (fSelectionWhenEnabled == null) {
					fSelectionWhenEnabled = fTable.getSelection();
					selectElements(null);
				}
			} else if (fSelectionWhenEnabled != null) {
				selectElements(fSelectionWhenEnabled);
				fSelectionWhenEnabled = null;
			}
			fTableControl.setEnabled(enabled);
		}
		updateButtonState();
	}

	/**
	 * Sets a button enabled or disabled.
	 */
	public void enableButton(int index, boolean enable) {
		if (fButtonsEnabled != null && index < fButtonsEnabled.length) {
			fButtonsEnabled[index] = enable;
			updateButtonState();
		}
	}

	private boolean isButtonEnabled(ISelection sel, int index) {
		boolean extraState = getManagedButtonState(sel, index);
		return isEnabled() && extraState && fButtonsEnabled != null && fButtonsEnabled[index];
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.BasicField#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (isOkToUse(fTableControl)) {
			fTableControl.dispose();
		}
		for (Button control : fButtonControls) {
			if (isOkToUse(control)) {
				control.dispose();
			}
		}
	}

	// ------ model access

	/**
	 * Sets the elements shown in the list.
	 */
	public void setElements(Collection<Object> elements) {
		fElements = new ArrayList<Object>(elements);
		if (isOkToUse(fTableControl)) {
			fTable.refresh();
		}
		dialogFieldChanged();
	}

	/**
	 * Gets the elements shown in the list. The list returned is a copy, so it can be modified by the user.
	 */
	public List<Object> getElements() {
		return new ArrayList<Object>(fElements);
	}

	/**
	 * Gets the elements shown at the given index.
	 */
	public Object getElement(int index) {
		return fElements.get(index);
	}

	/**
	 * Gets the index of an element in the list or -1 if element is not in list.
	 */
	public int getIndexOfElement(Object elem) {
		return fElements.indexOf(elem);
	}

	/**
	 * Replaces an element.
	 */
	public void replaceElement(Object oldElement, Object newElement) throws IllegalArgumentException {
		int idx = fElements.indexOf(oldElement);
		if (idx != -1) {
			fElements.set(idx, newElement);
			if (isOkToUse(fTableControl)) {
				List<Object> selected = getSelectedElements();
				if (selected.remove(oldElement)) {
					selected.add(newElement);
				}
				fTable.refresh();
				selectElements(new StructuredSelection(selected));
			}
			dialogFieldChanged();
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Notifies clients that the element has changed.
	 */
	public void elementChanged(Object element) throws IllegalArgumentException {
		if (fElements.contains(element)) {
			if (isOkToUse(fTableControl)) {
				fTable.update(element, null);
			}
			dialogFieldChanged();
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Adds an element at the end of the list.
	 */
	public boolean addElement(Object element) {
		return addElement(element, fElements.size());
	}

	/**
	 * Adds an element at a position.
	 */
	public boolean addElement(Object element, int index) {
		if (fElements.contains(element)) {
			return false;
		}
		fElements.add(index, element);
		if (isOkToUse(fTableControl)) {
			fTable.refresh();
			fTable.setSelection(new StructuredSelection(element));
		}

		dialogFieldChanged();
		return true;
	}

	/**
	 * Adds elements at the given index
	 */
	public boolean addElements(List<Object> elements, int index) {

		int nElements = elements.size();

		if (nElements > 0 && index >= 0 && index <= fElements.size()) {
			// filter duplicated
			ArrayList<Object> elementsToAdd = new ArrayList<Object>(nElements);

			for (int i = 0; i < nElements; i++) {
				Object elem = elements.get(i);
				if (!fElements.contains(elem)) {
					elementsToAdd.add(elem);
				}
			}
			if (!elementsToAdd.isEmpty()) {
				fElements.addAll(index, elementsToAdd);
				if (isOkToUse(fTableControl)) {
					if (index == fElements.size()) {
						fTable.add(elementsToAdd.toArray());
					} else {
						for (int i = elementsToAdd.size() - 1; i >= 0; i--) {
							fTable.insert(elementsToAdd.get(i), index);
						}
					}
					fTable.setSelection(new StructuredSelection(elementsToAdd));
				}
				dialogFieldChanged();
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds elements at the end of the list.
	 */
	public boolean addElements(List<Object> elements) {
		return addElements(elements, fElements.size());
	}

	/**
	 * Adds an element at a position.
	 */
	public void removeAllElements() {
		if (fElements.size() > 0) {
			fElements.clear();
			if (isOkToUse(fTableControl)) {
				fTable.refresh();
			}
			dialogFieldChanged();
		}
	}

	/**
	 * Removes an element from the list.
	 */
	public void removeElement(Object element) throws IllegalArgumentException {
		if (fElements.remove(element)) {
			if (isOkToUse(fTableControl)) {
				fTable.remove(element);
			}
			dialogFieldChanged();
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Removes elements from the list.
	 */
	public void removeElements(List<Object> elements) {
		if (elements.size() > 0) {
			fElements.removeAll(elements);
			if (isOkToUse(fTableControl)) {
				fTable.remove(elements.toArray());
			}
			dialogFieldChanged();
		}
	}

	/**
	 * Gets the number of elements
	 */
	public int getSize() {
		return fElements.size();
	}

	public void selectElements(ISelection selection) {
		fSelectionWhenEnabled = selection;
		if (isOkToUse(fTableControl)) {
			fTable.setSelection(selection, true);
		}
	}

	public void selectFirstElement() {
		Object element = null;
		if (fViewerComparator != null) {
			Object[] arr = fElements.toArray();
			fViewerComparator.sort(fTable, arr);
			if (arr.length > 0) {
				element = arr[0];
			}
		} else {
			if (fElements.size() > 0) {
				element = fElements.get(0);
			}
		}
		if (element != null) {
			selectElements(new StructuredSelection(element));
		}
	}

	public void editElement(Object element) {
		if (isOkToUse(fTableControl)) {
			fTable.refresh(element);
			fTable.editElement(element, 0);
		}
	}

	public void postSetSelection(final ISelection selection) {
		if (isOkToUse(fTableControl)) {
			Display d = fTableControl.getDisplay();
			d.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (isOkToUse(fTableControl)) {
						selectElements(selection);
					}
				}
			});
		}
	}

	/**
	 * Refreshes the table.
	 */
	@Override
	public void refresh() {
		super.refresh();
		if (isOkToUse(fTableControl)) {
			fTable.refresh();
		}
	}

	// ------- list maintenance

	private List<Object> moveUp(List<Object> elements, List<Object> move) {
		int nElements = elements.size();
		List<Object> res = new ArrayList<Object>(nElements);
		Object floating = null;
		for (int i = 0; i < nElements; i++) {
			Object curr = elements.get(i);
			if (move.contains(curr)) {
				res.add(curr);
			} else {
				if (floating != null) {
					res.add(floating);
				}
				floating = curr;
			}
		}
		if (floating != null) {
			res.add(floating);
		}
		return res;
	}

	private void moveUp(List<Object> toMoveUp) {
		if (toMoveUp.size() > 0) {
			setElements(moveUp(fElements, toMoveUp));
			fTable.reveal(toMoveUp.get(0));
		}
	}

	private void moveDown(List<Object> toMoveDown) {
		if (toMoveDown.size() > 0) {
			setElements(reverse(moveUp(reverse(fElements), toMoveDown)));
			fTable.reveal(toMoveDown.get(toMoveDown.size() - 1));
		}
	}

	private List<Object> reverse(List<Object> p) {
		List<Object> reverse = new ArrayList<Object>(p.size());
		for (int i = p.size() - 1; i >= 0; i--) {
			reverse.add(p.get(i));
		}
		return reverse;
	}

	private void remove() {
		List<Object> selection = getSelectedElements();
		removeElements(selection);
	}

	private void up() {
		List<Object> selection = getSelectedElements();
		moveUp(selection);
	}

	private void down() {
		List<Object> selection = getSelectedElements();
		moveDown(selection);
	}

	public boolean canMoveUp() {
		/* <<< */
		// if (isOkToUse(fTableControl)) {
		// int[] indc = fTable.getTable().getSelectionIndices();
		// for (int i = 0; i < indc.length; i++) {
		// if (indc[i] != i) { return true; } } } return false;
		/* --- */
		boolean canMoveUp = true;

		if (isOkToUse(fTableControl)) {
			int[] indc = fTable.getTable().getSelectionIndices();
			for (int i = 0; i < indc.length; i++) {
				if (indc[i] == i) {
					canMoveUp = false;
					break;
				}
			}
		} else {
			canMoveUp = false;
		}

		return canMoveUp;
		/* >>> */
	}

	public boolean canMoveDown() {
		/* <<< */
		// if (isOkToUse(fTableControl)) {
		// int[] indc = fTable.getTable().getSelectionIndices();
		// int k = fElements.size() - 1;
		// for (int i = indc.length - 1; i >= 0; i--, k--) {
		// if (indc[i] != k) { return true; } } } return false;
		/* --- */
		boolean canMoveDown = true;

		if (isOkToUse(fTableControl)) {
			int[] indc = fTable.getTable().getSelectionIndices();
			int k = fElements.size() - 1;
			for (int i = indc.length - 1; i >= 0; i--, k--) {
				if (indc[i] == k) {
					canMoveDown = false;
					break;
				}
			}
		} else {
			canMoveDown = false;
		}

		return canMoveDown;
		/* >>> */
	}

	/**
	 * Returns the selected elements.
	 */
	public List<Object> getSelectedElements() {
		List<Object> result = new ArrayList<Object>();
		if (isOkToUse(fTableControl)) {
			ISelection selection = fTable.getSelection();
			if (selection instanceof IStructuredSelection) {
				Iterator<?> iter = ((IStructuredSelection) selection).iterator();
				while (iter.hasNext()) {
					result.add(iter.next());
				}
			}
		}
		return result;
	}

	// ------- ListViewerAdapter

	private class ListViewerAdapter implements IStructuredContentProvider, ISelectionChangedListener, IDoubleClickListener {

		// ------- ITableContentProvider Interface ------------

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// will never happen
		}

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getElements(Object obj) {
			return fElements.toArray();
		}

		// ------- ISelectionChangedListener Interface ------------

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			doListSelected(event);
		}

		/*
		 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
		 */
		@Override
		public void doubleClick(DoubleClickEvent event) {
			doDoubleClick(event);
		}
	}

	protected void doListSelected(SelectionChangedEvent event) {
		updateButtonState();
		if (fListAdapter != null) {
			fListAdapter.selectionChanged(this);
		}
	}

	protected void doDoubleClick(DoubleClickEvent event) {
		if (fListAdapter != null) {
			fListAdapter.doubleClicked(this);
		}
	}
}

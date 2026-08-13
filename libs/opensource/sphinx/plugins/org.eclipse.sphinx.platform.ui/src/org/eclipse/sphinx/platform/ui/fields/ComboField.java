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

import org.eclipse.sphinx.platform.ui.fields.messages.FieldsMessages;
import org.eclipse.sphinx.platform.ui.internal.util.LayoutUtil;
import org.eclipse.sphinx.platform.ui.widgets.IWidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <p align=center>
 * <b><em>Combo Field</em></b>
 * </p>
 * <p align=justify>
 * Field containing a label and a combo control.
 * </p>
 */
public class ComboField extends BasicField implements IComboField {

	public static String[] EMPTY_LIST = new String[] { FieldsMessages.field_EmptyListItem };

	private String fText;

	private int fSelectionIndex;

	private String[] fItems;

	private Combo fComboControl;

	private ModifyListener fModifyListener;

	private int fStyle;

	private boolean firesChangeEvent = true;

	public ComboField() {
		this(false);
	}

	public ComboField(IWidgetFactory widgetFactory) {
		this(widgetFactory, false);
	}

	public ComboField(boolean isEditable) {
		this(null, isEditable);
	}

	public ComboField(int style) {
		this(null, style);
	}

	public ComboField(IWidgetFactory widgetFactory, boolean isEditable) {
		this(widgetFactory, isEditable ? SWT.NONE : SWT.READ_ONLY);
	}

	public ComboField(IWidgetFactory widgetFactory, int style) {
		super(widgetFactory);
		fText = ""; //$NON-NLS-1$
		fItems = new String[0];
		fStyle = style;
		fSelectionIndex = -1;
	}

	// ------- layout helpers

	/*
	 * @see BasicField#doFillIntoGrid
	 */
	@Override
	protected Control[] doFillIntoGrid(Composite parent, int nColumns) {

		Control label = getLabelControl(parent, false, 1);

		Combo combo = getComboControl(parent, nColumns - 1);

		return new Control[] { label, combo };
	}

	/*
	 * @see BasicField#getNumberOfControls
	 */
	@Override
	protected int getNumberOfControls() {
		return 2;
	}

	// ------- focus methods

	/*
	 * @see BasicField#setFocus
	 */
	@Override
	public boolean setFocus() {
		if (isOkToUse(fComboControl)) {
			fComboControl.setFocus();
		}
		return true;
	}

	// ------- ui creation

	/**
	 * Creates combo control.
	 * 
	 * @param parent
	 *            The parent composite (supposed to be not <tt>null</tt>).
	 * @param hspan
	 *            The number of columns the combo widget must span.
	 * @return The created combo widget.
	 */
	protected final Combo getComboControl(Composite parent, int hspan) {
		Combo combo = createComboControl(parent, hspan);
		if (combo.getLayoutData() == null) {
			if (fUseFormLayout) {
				combo.setLayoutData(LayoutUtil.tableWrapDataForCombo(hspan));
			} else {
				combo.setLayoutData(LayoutUtil.gridDataForCombo(hspan));
			}
		}
		return combo;
	}

	/**
	 * Creates or returns the created combo control.
	 * 
	 * @param parent
	 *            The parent composite (supposed to be not <tt>null</tt>).
	 * @param hspan
	 *            The number of columns the combo widget must span.
	 */
	private Combo createComboControl(Composite parent, int hspan) {
		if (fComboControl == null) {
			assertCompositeNotNull(parent);
			fModifyListener = new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (firesChangeEvent) {
						doModifyText(e);
					}
				}
			};
			SelectionListener selectionListener = new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (firesChangeEvent) {
						doSelectionChanged(e);
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			};

			if (fWidgetFactory != null) {
				fComboControl = fWidgetFactory.createCombo(parent, fStyle, true, hspan);
			} else {
				fComboControl = new Combo(parent, fStyle);
			}
			// moved up due to 1GEUNW2
			fComboControl.setItems(fItems);
			if (fSelectionIndex != -1) {
				fComboControl.select(fSelectionIndex);
			} else {
				fComboControl.setText(fText);
			}

			fComboControl.setFont(parent.getFont());
			fComboControl.addModifyListener(fModifyListener);
			fComboControl.addSelectionListener(selectionListener);
			fComboControl.setEnabled(isEnabled());
		}
		return fComboControl;
	}

	private void doModifyText(ModifyEvent e) {
		boolean firesDialogChangedEvent = false;
		if (isOkToUse(fComboControl)) {
			firesDialogChangedEvent = !fComboControl.getText().equals(fText) || fComboControl.getSelectionIndex() != fSelectionIndex;
			fText = fComboControl.getText();
			fSelectionIndex = fComboControl.getSelectionIndex();
		}
		if (firesDialogChangedEvent) {
			dialogFieldChanged();
		}
	}

	private void doSelectionChanged(SelectionEvent e) {
		boolean firesDialogChangedEvent = false;
		if (isOkToUse(fComboControl)) {
			firesDialogChangedEvent = !fComboControl.getText().equals(fText) || fComboControl.getSelectionIndex() != fSelectionIndex;
			fItems = fComboControl.getItems();
			fText = fComboControl.getText();
			fSelectionIndex = fComboControl.getSelectionIndex();
		}
		if (firesDialogChangedEvent) {
			dialogFieldChanged();
		}
	}

	// ------ enable / disable and dispose management

	/*
	 * @see BasicField#updateEnableState
	 */
	@Override
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fComboControl)) {
			fComboControl.setEnabled(isEnabled());
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.BasicField#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (isOkToUse(fComboControl)) {
			fComboControl.dispose();
		}
	}

	// ------ text access

	/**
	 * Gets the combo items.
	 */
	@Override
	public String[] getItems() {
		return fItems;
	}

	/**
	 * Sets the combo items. Triggers a dialog-changed event.
	 */
	@Override
	public void setItems(String[] items) {
		setItems(items, false);
	}

	public void setItems(String[] items, boolean silent) {
		boolean previousState = firesChangeEvent;
		firesChangeEvent = !silent;

		fItems = items;
		if (isOkToUse(fComboControl)) {
			fComboControl.setItems(items);
		}
		if (firesChangeEvent) {
			dialogFieldChanged();
		}
		firesChangeEvent = previousState;
	}

	/**
	 * Gets the text.
	 */
	public String getText() {
		return fText;
	}

	/**
	 * Sets the text. Triggers a dialog-changed event.
	 */
	@Override
	public void setText(String text) {
		fText = text;
		if (isOkToUse(fComboControl)) {
			fComboControl.setText(text);
		} else {
			dialogFieldChanged();
		}
	}

	/**
	 * Selects an item.
	 */
	@Override
	public boolean selectItem(int index) {
		return selectItem(index, false);
	}

	public boolean selectItem(int index, boolean silent) {
		boolean success = false;
		if (isOkToUse(fComboControl)) {
			boolean previousState = firesChangeEvent;
			firesChangeEvent = !silent;
			fComboControl.select(index);
			firesChangeEvent = previousState;
			success = fComboControl.getSelectionIndex() == index;
		} else {
			if (index >= 0 && index < fItems.length) {
				fText = fItems[index];
				fSelectionIndex = index;
				success = true;
			}
		}
		if (success && !silent) {
			dialogFieldChanged();
		}
		return success;
	}

	/**
	 * Selects an item.
	 */
	public boolean selectItem(String name) {
		for (int i = 0; i < fItems.length; i++) {
			if (fItems[i].equals(name)) {
				return selectItem(i);
			}
		}
		return false;
	}

	@Override
	public int getSelectionIndex() {
		return fSelectionIndex;
	}

	/**
	 * Sets the text without triggering a dialog-changed event.
	 */
	public void setTextWithoutUpdate(String text) {
		fText = text;
		if (isOkToUse(fComboControl)) {
			fComboControl.removeModifyListener(fModifyListener);
			fComboControl.setText(text);
			fComboControl.addModifyListener(fModifyListener);
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField#refresh()
	 */
	@Override
	public void refresh() {
		super.refresh();
		setTextWithoutUpdate(fText);
	}

	@Override
	public Control getComboControl() {
		Control control = null;
		if (isOkToUse(fComboControl)) {
			control = fComboControl;
		}
		return control;
	}
}

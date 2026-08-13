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
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.HyperlinkAdapter;

public class HyperlinkCComboField extends BasicHyperlinkField implements ICComboField {

	// TODO Calculate with according to page width, number of columns horizontal spacing, etc.
	public static String[] EMPTY_LIST = new String[] { FieldsMessages.field_EmptyListItem };

	private String fText;

	private int fSelectionIndex;

	private String[] fItems;

	private CCombo fCComboControl;

	private ModifyListener fModifyListener;

	private int fStyle;

	public HyperlinkCComboField(int style, IWidgetFactory widgetFactory, HyperlinkAdapter hyperlinkAdapter) {
		super(widgetFactory, hyperlinkAdapter);
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

		CCombo combo = getCComboControl(parent, nColumns - 1);

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
		if (isOkToUse(fCComboControl)) {
			fCComboControl.setFocus();
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
	protected final CCombo getCComboControl(Composite parent, int hspan) {
		CCombo combo = createComboControl(parent, hspan);
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
	 * Creates the combo control.
	 * 
	 * @param parent
	 *            The parent composite (supposed to be not <tt>null</tt>).
	 * @param hspan
	 *            The number of columns the combo widget must span.
	 */
	private CCombo createComboControl(Composite parent, int hspan) {
		if (fCComboControl == null) {
			assertCompositeNotNull(parent);
			fModifyListener = new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					doModifyText(e);
				}
			};
			SelectionListener selectionListener = new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					doSelectionChanged(e);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			};

			if (fWidgetFactory != null) {
				fCComboControl = fWidgetFactory.createCCombo(parent, fStyle, true, hspan);
			} else {
				fCComboControl = new CCombo(parent, fStyle);
			}
			// moved up due to 1GEUNW2
			fCComboControl.setItems(fItems);
			if (fSelectionIndex != -1) {
				fCComboControl.select(fSelectionIndex);
			} else {
				fCComboControl.setText(fText);
			}

			fCComboControl.setFont(parent.getFont());
			fCComboControl.addModifyListener(fModifyListener);
			fCComboControl.addSelectionListener(selectionListener);
			fCComboControl.setEnabled(isEnabled());
		}
		return fCComboControl;
	}

	private void doModifyText(ModifyEvent e) {
		if (isOkToUse(fCComboControl)) {
			fText = fCComboControl.getText();
			fSelectionIndex = fCComboControl.getSelectionIndex();
		}
		dialogFieldChanged();
	}

	private void doSelectionChanged(SelectionEvent e) {
		if (isOkToUse(fCComboControl)) {
			fItems = fCComboControl.getItems();
			fText = fCComboControl.getText();
			fSelectionIndex = fCComboControl.getSelectionIndex();
		}
		dialogFieldChanged();
	}

	// ------ enable / disable and dispose management

	/*
	 * @see BasicField#updateEnableState
	 */
	@Override
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fCComboControl)) {
			fCComboControl.setEnabled(isEnabled());
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.BasicField#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (isOkToUse(fCComboControl)) {
			fCComboControl.dispose();
		}
	}

	// ------ text access

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.ICComboField#getItems()
	 */
	@Override
	public String[] getItems() {
		return fItems;
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.ICComboField#setItems(java.lang.String[])
	 */
	@Override
	public void setItems(String[] items) {
		fItems = items;
		if (isOkToUse(fCComboControl)) {
			fCComboControl.setItems(items);
		}
		dialogFieldChanged();
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
		if (isOkToUse(fCComboControl)) {
			fCComboControl.setText(text);
		} else {
			dialogFieldChanged();
		}
	}

	/**
	 * Selects an item.
	 */
	@Override
	public boolean selectItem(int index) {
		boolean success = false;
		if (isOkToUse(fCComboControl)) {
			fCComboControl.select(index);
			success = fCComboControl.getSelectionIndex() == index;
		} else {
			if (index >= 0 && index < fItems.length) {
				fText = fItems[index];
				fSelectionIndex = index;
				success = true;
			}
		}
		if (success) {
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

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.ICComboField#getSelectionIndex()
	 */
	@Override
	public int getSelectionIndex() {
		if (isOkToUse(fCComboControl)) {
			return fCComboControl.getSelectionIndex();
		} else {
			return fSelectionIndex;
		}
	}

	/**
	 * Sets the text without triggering a dialog-changed event.
	 */
	public void setTextWithoutUpdate(String text) {
		fText = text;
		if (isOkToUse(fCComboControl)) {
			fCComboControl.removeModifyListener(fModifyListener);
			fCComboControl.setText(text);
			fCComboControl.addModifyListener(fModifyListener);
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

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.ICComboField#getCComboControl()
	 */
	@Override
	public Control getCComboControl() {
		Control control = null;
		if (isOkToUse(fCComboControl)) {
			control = fCComboControl;
		}
		return control;
	}
}

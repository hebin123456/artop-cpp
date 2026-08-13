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

import org.eclipse.sphinx.platform.ui.internal.util.LayoutUtil;
import org.eclipse.sphinx.platform.ui.widgets.IWidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <p align=center>
 * <b><em>Selection Button Dialog Field</em></b>
 * </p>
 * <p align=justify>
 * Dialog Field containing a single button such as a radio or checkbox button.
 * </p>
 */
public class SelectionButtonField extends BasicField {

	private Button fButton;

	private boolean fIsSelected;

	private BasicField[] fAttachedDialogFields;

	private int fButtonStyle;

	private String fButtonToolTip;

	/**
	 * Creates a selection button. Several styles are allowed for this button: SWT.RADIO, SWT.CHECK, SWT.TOGGLE,
	 * SWT.PUSH
	 * 
	 * @param buttonStyle
	 *            Style to be applied to the button.
	 */
	public SelectionButtonField(int buttonStyle) {
		this(null, buttonStyle);
	}

	public SelectionButtonField(IWidgetFactory widgetFactory, int buttonStyle) {
		super(widgetFactory);
		fIsSelected = false;
		fAttachedDialogFields = null;
		fButtonStyle = buttonStyle;
	}

	/**
	 * Attaches a field to the selection state of the selection button. The attached field will be disabled if the
	 * selection button is not selected.
	 */
	public void attachDialogField(BasicField basicField) {
		attachDialogFields(new BasicField[] { basicField });
	}

	/**
	 * Attaches fields to the selection state of the selection button. The attached fields will be disabled if the
	 * selection button is not selected.
	 */
	public void attachDialogFields(BasicField[] dialogFields) {
		fAttachedDialogFields = dialogFields;
		for (BasicField element : dialogFields) {
			element.setEnabled(fIsSelected);
		}
	}

	/**
	 * Returns <code>true</code> is the given field is attached to the selection button.
	 */
	public boolean isAttached(BasicField editor) {
		if (fAttachedDialogFields != null) {
			for (BasicField element : fAttachedDialogFields) {
				if (element == editor) {
					return true;
				}
			}
		}

		return false;
	}

	// ------- layout helpers

	/*
	 * @see BasicField#doFillIntoGrid
	 */
	@Override
	protected Control[] doFillIntoGrid(Composite parent, int nColumns) {

		Button button = getSelectionButton(parent);

		if (fUseFormLayout) {
			button.setLayoutData(LayoutUtil.tableWrapDataForButton(button, nColumns));
		} else {
			button.setLayoutData(LayoutUtil.gridDataForButton(button, nColumns));
		}

		return new Control[] { button };
	}

	/*
	 * @see BasicField#getNumberOfControls
	 */
	@Override
	protected int getNumberOfControls() {
		return 1;
	}

	// ------- UI creation

	/**
	 * Returns the selection button widget. When called the first time, the widget will be created.
	 * 
	 * @param group
	 *            The parent composite when called the first time, or <code>null</code> after.
	 */
	public Button getSelectionButton(Composite group) {
		if (fButton == null) {
			assertCompositeNotNull(group);

			fButton = new Button(group, fButtonStyle);
			fButton.setFont(group.getFont());
			fButton.setText(fLabelText);
			fButton.setToolTipText(fButtonToolTip);
			fButton.setEnabled(isEnabled());
			fButton.setSelection(fIsSelected);
			fButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					doWidgetSelected(e);
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					doWidgetSelected(e);
				}
			});
		}
		return fButton;
	}

	// TODO Rework this part in order to be compliant with others basic fields
	public Control getButtonControl() {
		Control control = null;
		if (isOkToUse(fButton)) {
			control = fButton;
		}
		return control;
	}

	private void doWidgetSelected(SelectionEvent e) {
		if (isOkToUse(fButton)) {
			changeValue(fButton.getSelection());
		}
	}

	private void changeValue(boolean newState) {
		if (fIsSelected != newState) {
			fIsSelected = newState;
			if (fAttachedDialogFields != null) {
				boolean focusSet = false;
				for (BasicField element : fAttachedDialogFields) {
					element.setEnabled(fIsSelected);
					if (fIsSelected && !focusSet) {
						focusSet = element.setFocus();
					}
				}
			}
			dialogFieldChanged();
		} else if (fButtonStyle == SWT.PUSH) {
			dialogFieldChanged();
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField#setLabelText(java.lang.String)
	 */
	@Override
	public void setLabelText(String labeltext) {
		fLabelText = labeltext;
		if (isOkToUse(fButton)) {
			fButton.setText(labeltext);
		}
	}

	@Override
	public void setToolTipText(String toolTip) {
		fButtonToolTip = toolTip;
		if (isOkToUse(fButton)) {
			fButton.setToolTipText(toolTip);
		}
	}

	// ------ model access

	/**
	 * Returns the selection state of the button.
	 */
	public boolean isSelected() {
		return fIsSelected;
	}

	private void setButtonSelection(boolean selected) {
		if (isOkToUse(fButton)) {
			fButton.setSelection(selected);
		}
	}

	/**
	 * Sets the selection state of the button.
	 */
	public void setSelection(boolean selected) {
		changeValue(selected);
		setButtonSelection(selected);
	}

	/**
	 * Set button selection without triggering "dialog field changed" event.
	 * 
	 * @param selected
	 *            The expected selection state of the button.
	 */
	public void setSelectionWithoutEvent(boolean selected) {
		fIsSelected = selected;
		setButtonSelection(selected);
	}

	// ------ enable / disable and dispose management

	/*
	 * @see BasicField#updateEnableState
	 */
	@Override
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fButton)) {
			fButton.setEnabled(isEnabled());
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.BasicField#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (isOkToUse(fButton)) {
			fButton.dispose();
		}
		if (fAttachedDialogFields != null) {
			for (BasicField element : fAttachedDialogFields) {
				element.dispose();
			}
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField#refresh()
	 */
	@Override
	public void refresh() {
		super.refresh();
		if (isOkToUse(fButton)) {
			fButton.setSelection(fIsSelected);
		}
	}
}
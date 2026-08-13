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

import org.eclipse.sphinx.platform.ui.fields.adapters.IButtonAdapter;
import org.eclipse.sphinx.platform.ui.internal.util.LayoutUtil;
import org.eclipse.sphinx.platform.ui.widgets.IWidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * <p align=center>
 * <b><em>String Button Dialog Field</em></b>
 * </p>
 * <p align=justify>
 * Dialog field containing a label, text control and a button control.
 * </p>
 */
public class StringButtonField extends StringField implements IStringButtonField {

	private Button fBrowseButton;

	private String fBrowseButtonLabel;

	private IButtonAdapter fStringButtonAdapter;

	private boolean fButtonEnabled;

	public StringButtonField(IButtonAdapter adapter) {
		super();
		fStringButtonAdapter = adapter;
		fBrowseButtonLabel = "!Browse...!"; //$NON-NLS-1$
		fButtonEnabled = true;
	}

	public StringButtonField(IButtonAdapter adapter, IWidgetFactory widgetFactory) {
		// FIXME By default, string field with a button cannot be multi-line.
		super(widgetFactory, false);
		fStringButtonAdapter = adapter;
		fBrowseButtonLabel = "!Browse...!"; //$NON-NLS-1$
		fButtonEnabled = true;
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.IStringButtonField#setButtonLabel(java.lang.String)
	 */
	@Override
	public void setButtonLabel(String label) {
		fBrowseButtonLabel = label;
	}

	// ------ adapter communication

	/**
	 * Programmatical pressing of the button
	 */
	private void changeControlPressed() {
		fStringButtonAdapter.changeControlPressed(this);
	}

	// ------- layout helpers

	/*
	 * @see BasicField#doFillIntoGrid
	 */
	@Override
	protected Control[] doFillIntoGrid(Composite parent, int nColumns) {

		Control label = getLabelControl(parent, 1);

		// Creates a specific composite so that button could correctly be created (size & position)
		Composite composite = createSpecificComposite(parent, getNumberOfControls() - 1, nColumns - 1, true, false, fUseFormLayout);

		Text text = getTextControl(composite, 1);

		Button button = getChangeControl(composite, 1);

		return new Control[] { label, text, button };
	}

	/*
	 * @see BasicField#getNumberOfControls
	 */
	@Override
	protected int getNumberOfControls() {
		return 3;
	}

	// ------- ui creation

	/**
	 * Creates button control.
	 * 
	 * @param parent
	 *            The parent composite (supposed to be not <tt>null</tt>).
	 * @param hspan
	 *            The number of columns the button widget must span.
	 * @return The created button control.
	 */
	protected final Button getChangeControl(Composite parent, int hspan) {
		Button button = fBrowseButton;
		if (button == null) {
			button = createChangeControl(parent);
			if (button.getLayoutData() == null) {
				if (fUseFormLayout) {
					button.setLayoutData(LayoutUtil.tableWrapDataForButton(button, hspan));
				} else {
					button.setLayoutData(LayoutUtil.gridDataForButton(button, hspan));
				}
			}
		}
		return button;
	}

	/**
	 * Creates or returns the created button widget.
	 * 
	 * @param parent
	 *            The parent composite or <code>null</code> if the widget has already been created.
	 */
	private Button createChangeControl(Composite parent) {
		if (fBrowseButton == null) {
			assertCompositeNotNull(parent);

			int style = -1;
			if (fUseFormLayout) {
				style = SWT.FLAT;
			} else {
				style = SWT.PUSH;
			}
			fBrowseButton = new Button(parent, style);
			fBrowseButton.setFont(parent.getFont());
			fBrowseButton.setText(fBrowseButtonLabel);
			fBrowseButton.setEnabled(isEnabled() && fButtonEnabled);
			fBrowseButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					changeControlPressed();
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					changeControlPressed();
				}
			});

		}
		return fBrowseButton;
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.IStringButtonField#getButtonControl()
	 */
	@Override
	public Control getButtonControl() {
		Control control = null;
		if (isOkToUse(fBrowseButton)) {
			control = fBrowseButton;
		}
		return control;
	}

	// ------ enable / disable and dispose management

	/**
	 * Sets the enable state of the button.
	 */
	public void enableButton(boolean enable) {
		if (isOkToUse(fBrowseButton)) {
			fBrowseButton.setEnabled(isEnabled() && enable);
		}
		fButtonEnabled = enable;
	}

	/*
	 * @see BasicField#updateEnableState
	 */
	@Override
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fBrowseButton)) {
			fBrowseButton.setEnabled(isEnabled() && fButtonEnabled);
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.BasicField#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (isOkToUse(fBrowseButton)) {
			fBrowseButton.dispose();
		}
	}
}

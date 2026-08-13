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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * <p align=justify>
 * Dialog field containing a label and a text control.
 * </p>
 * <table>
 * <td valign=top>
 * <p>
 * <b>Note&nbsp;&nbsp;</b></td>
 * <td valign=top>
 * <p align=justify>
 * Parts have been commented (content assist for example). These parts are not needed for the moment, but one day, they
 * could be useful!
 * </p>
 * </td>
 * </table>
 */
public class StringField extends BasicField implements IStringField {

	private String fText;

	private Text fTextControl;

	private ModifyListener fModifyListener;

	private boolean fEditable;
	private boolean fMultiLine;

	// private IContentAssistProcessor fContentAssistProcessor;

	// private int fStyle = SWT.SINGLE | SWT.BORDER;
	private int fStyle = -1;

	public StringField() {
		this(null);
	}

	public StringField(IWidgetFactory widgetFactory) {
		super(widgetFactory);
		fText = ""; //$NON-NLS-1$
		fEditable = true;
	}

	private int getStyle() {
		if (fStyle == -1) {
			fStyle = SWT.SINGLE;
			if (!fUseFormLayout) {
				fStyle |= SWT.BORDER;
			}
		}
		return fStyle;
	}

	/**
	 * @param style
	 *            Default value: <tt>SWT.SINGLE | SWT.BORDER</tt>
	 */
	public StringField(int style) {
		this();
		fStyle = style;
	}

	public StringField(IWidgetFactory widgetFactory, boolean multiLine) {
		super(widgetFactory);
		fText = ""; //$NON-NLS-1$
		fEditable = true;
		fMultiLine = multiLine;
	}

	public StringField(int style, IWidgetFactory widgetFactory) {
		super(widgetFactory);
		fText = ""; //$NON-NLS-1$
		fEditable = true;
		fMultiLine = false;
		fStyle = style;
	}

	// public void setContentAssistProcessor(IContentAssistProcessor processor)
	// { fContentAssistProcessor = processor;
	// if (fContentAssistProcessor != null && isOkToUse(fTextControl)) {
	// ControlContentAssistHelper.createTextContentAssistant(fTextControl,
	// fContentAssistProcessor); }}
	// public IContentAssistProcessor getContentAssistProcessor() {
	// return fContentAssistProcessor; }

	// ------- layout helpers

	/*
	 * @see BasicField#doFillIntoGrid
	 */
	@Override
	protected Control[] doFillIntoGrid(Composite parent, int nColumns) {

		Control label = getLabelControl(parent, fMultiLine, 1);

		Text text = getTextControl(parent, nColumns - 1);

		return new Control[] { label, text };
	}

	// ------- ui creation

	/**
	 * @param parent
	 *            The parent composite (supposed to be not <tt>null</tt>).
	 * @param hspan
	 *            The number of columns the text widget must span.
	 * @return The created text widget.
	 */
	protected final Text getTextControl(Composite parent, int hspan) {
		Text text = createTextControl(parent, hspan);
		if (text.getLayoutData() == null) {
			if (fUseFormLayout) {
				text.setLayoutData(LayoutUtil.tableWrapDataForText(hspan));
			} else {
				text.setLayoutData(LayoutUtil.gridDataForText(hspan));
			}
		}
		return text;
	}

	/**
	 * Creates the text control.
	 * 
	 * @param parent
	 *            The parent composite (supposed to be not <tt>null</tt>).
	 * @param hspan
	 *            The number of columns the text widget must span.
	 */
	private Text createTextControl(Composite parent, int hspan) {
		if (fTextControl == null) {
			assertCompositeNotNull(parent);
			fModifyListener = new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					doModifyText(e);
				}
			};

			if (fWidgetFactory != null) {
				fTextControl = fWidgetFactory.createText(parent, fMultiLine, hspan, true);
			} else {
				fTextControl = new Text(parent, getStyle());
			}
			// moved up due to 1GEUNW2
			fTextControl.setText(fText);
			fTextControl.setFont(parent.getFont());
			fTextControl.addModifyListener(fModifyListener);

			fTextControl.setEnabled(isEnabled());
			fTextControl.setEditable(fEditable);

			// Commented since not supported.
			// if (fContentAssistProcessor != null) {
			// ControlContentAssistHelper.createTextContentAssistant(
			// fTextControl, fContentAssistProcessor);}
		}
		return fTextControl;
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
		if (isOkToUse(fTextControl)) {
			fTextControl.setFocus();
			fTextControl.setSelection(0, fTextControl.getText().length());
		}
		return true;
	}

	private void doModifyText(ModifyEvent e) {
		if (isOkToUse(fTextControl)) {
			boolean firesDialogChangedEvent = !fTextControl.getText().equals(fText);
			fText = fTextControl.getText();
			if (firesDialogChangedEvent) {
				dialogFieldChanged();
			}
		}
	}

	// ------ enable / disable management and dispose

	/*
	 * @see BasicField#updateEnableState
	 */
	@Override
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fTextControl)) {
			fTextControl.setEnabled(isEnabled());
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.BasicField#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (isOkToUse(fTextControl)) {
			fTextControl.dispose();
		}
	}

	// ------ text access

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.IStringField#getText()
	 */
	@Override
	public String getText() {
		return fText;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField#refresh()
	 */
	@Override
	public void refresh() {
		super.refresh();
		if (isOkToUse(fTextControl)) {
			setTextWithoutUpdate(fText);
		}
		updateEditableState();
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.IStringField#setText(java.lang.String)
	 */
	@Override
	public void setText(String text) {
		fText = text;
		if (isOkToUse(fTextControl)) {
			fTextControl.setText(text);
		} else {
			dialogFieldChanged();
		}
	}

	/**
	 * <p>
	 * <table>
	 * <td valign=top><b>/!\&nbsp;&nbsp;</b></td>
	 * <td valign=top><em>This method only concerns the encapsulated text control.</em></td>
	 * <td valign=top><b>&nbsp;&nbsp;/!\</b></td>
	 * </table>
	 * <p>
	 * Enables the text control if the parameter is <font color="#7F0055"><tt><b>true</b></tt></font>. Otherwise,
	 * disables it.
	 * 
	 * @param b
	 *            The new enabled state.
	 */
	public void setTextEnabled(boolean b) {
		if (isOkToUse(fTextControl)) {
			fTextControl.setEnabled(b);
		}
	}

	/**
	 * Sets the text without triggering a dialog-changed event.
	 */
	@Override
	public void setTextWithoutUpdate(String text) {
		fText = text;
		if (isOkToUse(fTextControl)) {
			fTextControl.removeModifyListener(fModifyListener);
			fTextControl.setText(text);
			fTextControl.addModifyListener(fModifyListener);
		}
	}

	@Override
	public Control getTextControl() {
		Control control = null;
		if (isOkToUse(fTextControl)) {
			control = fTextControl;
		}
		return control;
	}

	@Override
	public void setEditable(boolean editable) {
		if (editable != fEditable) {
			fEditable = editable;
			updateEditableState();
		}
	}

	protected void updateEditableState() {
		if (isOkToUse(fTextControl)) {
			fTextControl.setEditable(fEditable);
		}
	}

	protected final boolean isEditable() {
		return fEditable;
	}
}
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
import java.util.List;

import org.eclipse.sphinx.platform.messages.PlatformMessages;
import org.eclipse.sphinx.platform.ui.internal.Activator;
import org.eclipse.sphinx.platform.ui.internal.util.LayoutUtil;
import org.eclipse.sphinx.platform.ui.widgets.IWidgetFactory;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RangeField extends BasicField {

	private int fItemCount;

	private Label[] fLabelsControls;

	private String[] fLabels;

	private String[] fTexts;

	private Text[] fTextsControls;

	private ModifyListener[] fModifyListeners;

	private int fStyle = SWT.SINGLE | SWT.BORDER;

	private void doModifyText(ModifyEvent e) {
		for (int i = 0; i < fItemCount; i++) {
			if (e.widget.equals(fTextsControls[i])) {
				if (isOkToUse(fTextsControls[i])) {
					fTexts[i] = fTextsControls[i].getText();
					break;
				}
			}
		}

		dialogFieldChanged();
	}

	private Label createLabelControl(Composite parent, int i) {
		if (fLabelsControls[i] == null) {
			assertCompositeNotNull(parent);

			fLabelsControls[i] = new Label(parent, SWT.LEFT | SWT.WRAP);
			fLabelsControls[i].setFont(parent.getFont());
			fLabelsControls[i].setEnabled(isEnabled());
			if (fLabels[i] != null && !"".equals(fLabels[i])) { //$NON-NLS-1$
				fLabelsControls[i].setText(fLabels[i]);
			} else {
				// XXX: to avoid a 16 pixel wide empty label - revisit
				fLabelsControls[i].setText("."); //$NON-NLS-1$
				fLabelsControls[i].setVisible(false);
			}
		}
		return fLabelsControls[i];
	}

	/**
	 * Creates or returns the created text control.
	 * 
	 * @param parent
	 *            The parent composite or <code>null</code> when the widget has already been created.
	 */
	private Text getTextControl(Composite parent, int i) {
		if (fTextsControls[i] == null) {
			assertCompositeNotNull(parent);
			fModifyListeners[i] = new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					doModifyText(e);
				}
			};

			fTextsControls[i] = new Text(parent, fStyle);
			fTextsControls[i].setText(fTexts[i]);
			fTextsControls[i].setFont(parent.getFont());
			fTextsControls[i].addModifyListener(fModifyListeners[i]);

			fTextsControls[i].setEnabled(isEnabled());
		}
		return fTextsControls[i];
	}

	private void setLabelText(String text, int i) {
		fLabels[i] = text;
		if (isOkToUse(fLabelsControls[i])) {
			fLabelsControls[i].setText(text);
		}
	}

	private void setText(String text, int i) {
		fTexts[i] = text;
		if (isOkToUse(fTextsControls[i])) {
			fTextsControls[i].setText(text);
		} else {
			dialogFieldChanged();
		}
	}

	/**
	 * Sets the text without triggering a dialog-changed event.
	 */
	private void setTextWithoutUpdate(String text, int i) {
		fTexts[i] = text;
		if (isOkToUse(fTextsControls[i])) {
			fTextsControls[i].removeModifyListener(fModifyListeners[i]);
			fTextsControls[i].setText(text);
			fTextsControls[i].addModifyListener(fModifyListeners[i]);
		}
	}

	@Override
	protected Control[] doFillIntoGrid(Composite parent, int nColumns) {

		List<Control> controls = new ArrayList<Control>();

		for (int i = 0; i < fItemCount; i++) {
			Label label = createLabelControl(parent, i);
			if (fUseFormLayout) {
				label.setLayoutData(LayoutUtil.tableWrapDataForLabel(1));
			} else {
				label.setLayoutData(LayoutUtil.gridDataForLabel(1));
			}
			controls.add(label);
			Text text = getTextControl(parent, i);
			if (fUseFormLayout) {
				text.setLayoutData(LayoutUtil.tableWrapDataForText(nColumns / 2 - 1));
			} else {
				text.setLayoutData(LayoutUtil.gridDataForText(nColumns / 2 - 1));
			}
			controls.add(text);
		}
		return controls.toArray(new Control[controls.size()]);
	}

	@Override
	protected int getNumberOfControls() {
		return 4;
	}

	@Override
	protected void updateEnableState() {
		super.updateEnableState();
		for (int i = 0; i < fItemCount; i++) {
			if (isOkToUse(fTextsControls[i])) {
				fTextsControls[i].setEnabled(isEnabled());
			}
		}
	}

	public RangeField(int itemCount) {
		this(null, itemCount);
	}

	public RangeField(IWidgetFactory widgetFactory, int itemCount) {
		super(widgetFactory);
		fItemCount = itemCount;
		fTexts = new String[itemCount];
		fLabels = new String[itemCount];
		fTextsControls = new Text[itemCount];
		fLabelsControls = new Label[itemCount];
		fModifyListeners = new ModifyListener[itemCount];

		for (int i = 0; i < itemCount; i++) {
			fTexts[i] = ""; //$NON-NLS-1$
			fLabels[i] = ""; //$NON-NLS-1$
		}
	}

	/**
	 * @param style
	 * <br>
	 *            Default value: <tt>SWT.SINGLE | SWT.BORDER</tt>
	 */
	public RangeField(int itemCount, int style) {
		this(itemCount);
		fStyle = style;
	}

	public String[] getTexts() {
		String[] copy = new String[fItemCount];
		for (int i = 0; i < fItemCount; i++) {
			copy[i] = fTexts[i];
		}
		return copy;
	}

	public Text[] getTextControls() {
		return fTextsControls;
	}

	@Override
	public void refresh() {
		super.refresh();
		for (int i = 0; i < fItemCount; i++) {
			if (isOkToUse(fTextsControls[i])) {
				setTextWithoutUpdate(fTexts[i], i);
			}
		}
	}

	@Override
	public boolean setFocus() {
		// FIXME Should the focus be given to any text control?
		return true;
	}

	public void setLabels(String[] labels) {
		if (labels.length != fItemCount) {
			PlatformLogUtil.logAsWarning(Activator.getDefault(), PlatformMessages.warning_unexpectedArrayLength);
		}

		for (int i = 0; i < labels.length && i < fItemCount; i++) {
			setLabelText(labels[i], i);
		}
	}

	/**
	 * Sets the text. Triggers a dialog-changed event.
	 */
	public void setTexts(String[] texts) {
		if (texts.length != fItemCount) {
			PlatformLogUtil.logAsWarning(Activator.getDefault(), PlatformMessages.warning_unexpectedArrayLength);
		}

		for (int i = 0; i < texts.length && i < fItemCount; i++) {
			setText(texts[i], i);
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
		for (int i = 0; i < fItemCount; i++) {
			if (isOkToUse(fTextsControls[i])) {
				fTextsControls[i].setEnabled(b);
			}
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.BasicField#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		for (int i = 0; i < fItemCount; i++) {
			if (isOkToUse(fTextsControls[i])) {
				fTextsControls[i].dispose();
			}
			if (isOkToUse(fLabelsControls[i])) {
				fLabelsControls[i].dispose();
			}
		}
	}
}

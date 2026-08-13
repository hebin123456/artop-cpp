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
package org.eclipse.sphinx.platform.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class WidgetFactory implements IWidgetFactory {

	@Override
	public Button createButton(Composite parent, String text) {
		return createButton(parent, text, SWT.PUSH, SWT.FILL, SWT.BEGINNING, false, false, 1, 1);
	}

	@Override
	public Button createButton(Composite parent, String text, int style) {
		return createButton(parent, text, style, SWT.FILL, SWT.BEGINNING, false, false, 1, 1);
	}

	@Override
	public Button createButton(Composite parent, String text, int style, int valign, boolean hgrab, int hspan) {
		return createButton(parent, text, style, SWT.FILL, valign, hgrab, false, hspan, 1);
	}

	@Override
	public Button createButton(Composite parent, String text, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan, int vspan) {
		Button button = new Button(parent, style);
		button.setText(text);

		GridData data = new GridData(halign, valign, hgrab, vgrab);
		data.horizontalSpan = hspan;
		data.verticalSpan = vspan;
		button.setLayoutData(data);

		return button;
	}

	@Override
	public Label createLabel(Composite parent, String text) {
		return createLabel(parent, text, SWT.NONE, 1, false, SWT.CENTER);
	}

	@Override
	public Label createLabel(Composite parent, String text, int hspan, boolean hgrab) {
		return createLabel(parent, text, SWT.NONE, hspan, hgrab, SWT.CENTER);
	}

	@Override
	public Label createLabel(Composite parent, String text, boolean multiLine, int hspan, boolean hgrab) {
		return createLabel(parent, text, SWT.NONE, SWT.FILL, multiLine ? SWT.BEGINNING : SWT.CENTER, hgrab, false, hspan, 1);
	}

	@Override
	public Label createLabel(Composite parent, String text, int valign) {
		return createLabel(parent, text, SWT.NONE, 1, false, valign);
	}

	@Override
	public Label createLabel(Composite parent, String text, int hspan, boolean grabHorizontal, int valign) {
		return createLabel(parent, text, SWT.NONE, hspan, grabHorizontal, valign);
	}

	@Override
	public Label createLabel(Composite parent, String text, int style, int hspan, boolean hgrab, int valign) {
		return createLabel(parent, text, style, SWT.FILL, valign, hgrab, false, hspan, 1);
	}

	@Override
	public Label createLabel(Composite parent, String text, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan, int vspan) {
		Label label = new Label(parent, style);

		GridData data = new GridData(halign, valign, hgrab, vgrab);
		data.horizontalSpan = hspan;
		data.verticalSpan = vspan;
		label.setLayoutData(data);

		label.setText(text);

		return label;
	}

	@Override
	public Text createText(Composite parent) {
		return createText(parent, false, SWT.NONE, 1, true);
	}

	@Override
	public Text createText(Composite parent, int style) {
		return createText(parent, false, style, 1, true);
	}

	@Override
	public Text createText(Composite parent, boolean multiLine) {
		return createText(parent, multiLine, SWT.NONE, 1, true);
	}

	@Override
	public Text createText(Composite parent, int hspan, boolean grabHorizontal) {
		return createText(parent, false, SWT.NONE, hspan, grabHorizontal);
	}

	@Override
	public Text createText(Composite parent, boolean multiLine, int hspan, boolean grabHorizontal) {
		return createText(parent, multiLine, SWT.NONE, hspan, grabHorizontal);
	}

	@Override
	public Text createText(Composite parent, boolean multiLine, int style, int hspan, boolean hgrab) {
		return createText(parent, multiLine, style, SWT.FILL, SWT.FILL, hgrab, false, hspan, 1);
	}

	@Override
	public Text createText(Composite parent, boolean multiLine, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan, int vspan) {
		style |= SWT.BORDER;
		if (multiLine) {
			style |= SWT.MULTI | SWT.WRAP | SWT.V_SCROLL;
		} else {
			style |= SWT.SINGLE;
		}
		Text text = new Text(parent, style);

		GridData data = new GridData(halign, valign, hgrab, vgrab);
		if (multiLine) {
			data.heightHint = text.getLineHeight() * 5 + 6;
		} else {
			data.heightHint = text.getLineHeight() + 4;
		}
		data.horizontalSpan = hspan;
		data.verticalSpan = vspan;
		text.setLayoutData(data);

		return text;
	}

	@Override
	public Combo createCombo(Composite parent, int style, boolean hgrab, int hspan) {
		return createCombo(parent, style, SWT.FILL, SWT.FILL, hgrab, false, hspan, 1);
	}

	@Override
	public Combo createCombo(Composite parent, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan, int vspan) {
		Combo combo = new Combo(parent, style | SWT.BORDER);

		GridData data = new GridData(halign, valign, hgrab, vgrab);
		data.horizontalSpan = hspan;
		data.verticalSpan = vspan;
		combo.setLayoutData(data);

		return combo;
	}

	@Override
	public CCombo createCCombo(Composite parent) {
		return createCCombo(parent, false, SWT.NONE, 1, true);
	}

	@Override
	public CCombo createCCombo(Composite parent, boolean editable) {
		return createCCombo(parent, editable, SWT.NONE, 1, true);
	}

	@Override
	public CCombo createCCombo(Composite parent, int colspan, boolean grabHorizontal) {
		return createCCombo(parent, false, SWT.NONE, colspan, grabHorizontal);
	}

	@Override
	public CCombo createCCombo(Composite parent, int style, boolean hgrab, int hspan) {
		return createCCombo(parent, style, SWT.FILL, SWT.FILL, hgrab, false, hspan, 1);
	}

	@Override
	public CCombo createCCombo(Composite parent, boolean editable, int colspan, boolean grabHorizontal) {
		return createCCombo(parent, editable, SWT.NONE, colspan, grabHorizontal);
	}

	@Override
	public CCombo createCCombo(Composite parent, boolean editable, int style, int hspan, boolean hgrab) {
		return createCCombo(parent, editable, style, SWT.FILL, SWT.FILL, hgrab, false, hspan, 1);
	}

	@Override
	public CCombo createCCombo(Composite parent, boolean editable, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan,
			int vspan) {
		// If not editable, add READ_ONLY in combo style
		if (!editable) {
			style |= SWT.READ_ONLY;
		}
		return createCCombo(parent, style, halign, valign, hgrab, vgrab, hspan, vspan);
	}

	@Override
	public CCombo createCCombo(Composite parent, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan, int vspan) {
		// boolean readOnly = (style & SWT.READ_ONLY) != 0;
		// style &= ~SWT.READ_ONLY;

		CCombo combo = new CCombo(parent, style | SWT.BORDER);

		// combo.setEditable(!readOnly);

		GridData data = new GridData(halign, valign, hgrab, vgrab);
		data.horizontalSpan = hspan;
		data.verticalSpan = vspan;
		combo.setLayoutData(data);

		return combo;
	}

	@Override
	public Button createCheckBoxButton(Composite parent, String text) {
		return createCheckBoxButton(parent, text, SWT.NONE, 1, true);
	}

	@Override
	public Button createCheckBoxButton(Composite parent, String text, int colspan, boolean grabHorizontal) {
		return createCheckBoxButton(parent, text, SWT.NONE, colspan, grabHorizontal);
	}

	@Override
	public Button createCheckBoxButton(Composite parent, String text, int style, int hspan, boolean hgrab) {
		return createCheckBoxButton(parent, text, style, SWT.FILL, SWT.FILL, hgrab, false, hspan, 1);
	}

	@Override
	public Button createCheckBoxButton(Composite parent, String text, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan,
			int vspan) {
		Button button = new Button(parent, SWT.CHECK | style);
		button.setText(text);

		GridData data = new GridData(halign, valign, hgrab, vgrab);
		data.horizontalSpan = hspan;
		data.verticalSpan = vspan;
		button.setLayoutData(data);

		return button;
	}

	@Override
	public Hyperlink createHyperlink(Composite parent, String text) {
		return null;
	}

	@Override
	public Hyperlink createHyperlink(Composite parent, String text, int colspan, boolean grabHorizontal) {
		return null;
	}

	@Override
	public Hyperlink createHyperlink(Composite parent, String text, int valign) {
		return null;
	}

	@Override
	public Hyperlink createHyperlink(Composite parent, String text, int colspan, boolean grabHorizontal, int valign) {
		return null;
	}

	@Override
	public Hyperlink createHyperlink(Composite parent, String text, int style, int hspan, boolean grabHorizontal, int valign) {
		return null;
	}

	@Override
	public Hyperlink createHyperlink(Composite parent, String text, int style, int halign, int valign, boolean hgrab, boolean vgrab, int colspan,
			int rowspan) {
		return null;
	}
}

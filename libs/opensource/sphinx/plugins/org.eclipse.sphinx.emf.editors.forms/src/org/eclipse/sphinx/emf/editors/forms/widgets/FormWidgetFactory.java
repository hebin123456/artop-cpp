/**
 * <copyright>
 * 
 * Copyright (c) 2012 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     itemis - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.editors.forms.widgets;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.TableWrapData;

public class FormWidgetFactory implements IFormWidgetFactory {

	protected static final int MAX_WITDH = 175;

	protected FormToolkit toolkit;

	public FormWidgetFactory(FormToolkit toolkit) {
		Assert.isNotNull(toolkit);
		this.toolkit = toolkit;
	}

	@Override
	public Button createButton(Composite parent, String text) {
		return createButton(parent, text, SWT.NONE, TableWrapData.FILL, TableWrapData.TOP, false, false, 1, 1);
	}

	@Override
	public Button createButton(Composite parent, String text, int style) {
		return createButton(parent, text, style, TableWrapData.FILL, TableWrapData.TOP, false, false, 1, 1);
	}

	@Override
	public Button createButton(Composite parent, String text, int style, int valign, boolean hgrab, int hspan) {
		return createButton(parent, text, style, TableWrapData.FILL, valign, hgrab, false, hspan, 1);
	}

	@Override
	public Button createButton(Composite parent, String text, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan, int vspan) {
		Button button = toolkit.createButton(parent, text, style);

		TableWrapData data = new TableWrapData(TableWrapData.FILL);
		data.grabHorizontal = hgrab;
		data.grabVertical = vgrab;
		data.align = halign;
		data.valign = valign;
		data.colspan = hspan;
		data.rowspan = vspan;
		button.setLayoutData(data);

		return button;
	}

	@Override
	public Label createLabel(Composite parent, String text) {
		return createLabel(parent, text, SWT.NONE, 1, false, TableWrapData.TOP);
	}

	@Override
	public Label createLabel(Composite parent, String text, int colspan, boolean grabHorizontal) {
		return createLabel(parent, text, SWT.NONE, colspan, grabHorizontal, TableWrapData.TOP);
	}

	@Override
	public Label createLabel(Composite parent, String text, int valign) {
		return createLabel(parent, text, SWT.NONE, 1, false, valign);
	}

	@Override
	public Label createLabel(Composite parent, String text, int colspan, boolean grabHorizontal, int valign) {
		return createLabel(parent, text, SWT.NONE, colspan, grabHorizontal, valign);
	}

	@Override
	public Label createLabel(Composite parent, String text, boolean multiLine, int hspan, boolean hgrab) {
		return createLabel(parent, text, SWT.NONE, TableWrapData.FILL, TableWrapData.TOP, hgrab, false, hspan, 1);
	}

	@Override
	public Label createLabel(Composite parent, String text, int style, int colspan, boolean grabHorizontal, int valign) {
		return createLabel(parent, text, style, TableWrapData.FILL, valign, grabHorizontal, false, colspan, 1);
	}

	@Override
	public Label createLabel(Composite parent, String text, int style, int halign, int valign, boolean hgrab, boolean vgrab, int colspan, int rowspan) {
		Label label = toolkit.createLabel(parent, text, style);

		TableWrapData data = new TableWrapData(TableWrapData.FILL);
		data.grabHorizontal = hgrab;
		data.grabVertical = vgrab;
		data.align = halign;
		data.valign = valign;
		data.colspan = colspan;
		data.rowspan = rowspan;
		label.setLayoutData(data);

		return label;
	}

	@Override
	public Hyperlink createHyperlink(Composite parent, String text) {
		return createHyperlink(parent, text, SWT.NONE, 1, false, TableWrapData.TOP);
	}

	@Override
	public Hyperlink createHyperlink(Composite parent, String text, int colspan, boolean grabHorizontal) {
		return createHyperlink(parent, text, SWT.NONE, colspan, grabHorizontal, TableWrapData.TOP);
	}

	@Override
	public Hyperlink createHyperlink(Composite parent, String text, int valign) {
		return createHyperlink(parent, text, SWT.NONE, 1, false, valign);
	}

	@Override
	public Hyperlink createHyperlink(Composite parent, String text, int colspan, boolean grabHorizontal, int valign) {
		return createHyperlink(parent, text, SWT.NONE, colspan, grabHorizontal, valign);
	}

	@Override
	public Hyperlink createHyperlink(Composite parent, String text, int style, int colspan, boolean grabHorizontal, int valign) {
		return createHyperlink(parent, text, style, TableWrapData.FILL, valign, grabHorizontal, false, colspan, 1);
	}

	@Override
	public Hyperlink createHyperlink(Composite parent, String text, int style, int halign, int valign, boolean hgrab, boolean vgrab, int colspan,
			int rowspan) {
		Hyperlink link = toolkit.createHyperlink(parent, text, style);

		TableWrapData data = new TableWrapData(TableWrapData.FILL);
		data.grabHorizontal = hgrab;
		data.grabVertical = vgrab;
		data.align = halign;
		data.valign = valign;
		data.colspan = colspan;
		data.rowspan = rowspan;
		link.setLayoutData(data);

		return link;
	}

	@Override
	public FormText createFormText(Composite parent) {
		return createFormText(parent, 1, true);
	}

	@Override
	public FormText createFormText(Composite parent, int colspan, boolean grabHorizontal) {
		FormText text = toolkit.createFormText(parent, true);

		TableWrapData data = new TableWrapData(TableWrapData.FILL);
		data.colspan = colspan;
		data.grabHorizontal = grabHorizontal;
		text.setLayoutData(data);

		return text;
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
	public Text createText(Composite parent, int colspan, boolean grabHorizontal) {
		return createText(parent, false, SWT.NONE, colspan, grabHorizontal);
	}

	@Override
	public Text createText(Composite parent, boolean multiLine, int colspan, boolean grabHorizontal) {
		return createText(parent, multiLine, SWT.NONE, colspan, grabHorizontal);
	}

	@Override
	public Text createText(Composite parent, boolean multiLine, int style, int colspan, boolean grabHorizontal) {
		return createText(parent, multiLine, style, TableWrapData.FILL, TableWrapData.TOP, grabHorizontal, false, colspan, 1);
	}

	@Override
	public Text createText(Composite parent, boolean multiLine, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan, int vspan) {
		if (multiLine) {
			style |= SWT.MULTI | SWT.WRAP | SWT.V_SCROLL;
		} else {
			style |= SWT.SINGLE;
		}
		Text text = toolkit.createText(parent, null, style);

		TableWrapData data = new TableWrapData(TableWrapData.FILL);
		if (multiLine) {
			data.heightHint = text.getLineHeight() * 5 + 6;
		} else {
			data.heightHint = text.getLineHeight() + 4;
		}
		// TODO Calculate with according to page width, number of columns horizontal spacing, etc.
		data.maxWidth = MAX_WITDH;
		data.grabHorizontal = hgrab;
		data.grabVertical = vgrab;
		data.align = halign;
		data.valign = valign;
		data.colspan = hspan;
		data.rowspan = vspan;
		text.setLayoutData(data);

		return text;
	}

	@Override
	public Combo createCombo(Composite parent, int style, boolean hgrab, int hspan) {
		return createCombo(parent, style, TableWrapData.FILL, TableWrapData.FILL, hgrab, false, hspan, 1);
	}

	@Override
	public Combo createCombo(Composite parent, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan, int vspan) {
		Combo combo = new Combo(parent, style);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		toolkit.adapt(combo, true, true);

		TableWrapData data = new TableWrapData(TableWrapData.FILL);
		// TODO Calculate with according to page width, number of columns horizontal spacing, etc.
		data.maxWidth = MAX_WITDH;
		data.grabHorizontal = hgrab;
		data.grabVertical = vgrab;
		data.align = halign;
		data.valign = valign;
		data.colspan = hspan;
		data.rowspan = vspan;
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
	public CCombo createCCombo(Composite parent, boolean editable, int colspan, boolean grabHorizontal) {
		return createCCombo(parent, editable, SWT.NONE, colspan, grabHorizontal);
	}

	@Override
	public CCombo createCCombo(Composite parent, boolean editable, int style, int colspan, boolean grabHorizontal) {
		return createCCombo(parent, editable, style, TableWrapData.FILL, TableWrapData.TOP, grabHorizontal, false, colspan, 1);
	}

	@Override
	public CCombo createCCombo(Composite parent, int style, boolean hgrab, int hspan) {
		return createCCombo(parent, style, TableWrapData.FILL, TableWrapData.FILL, hgrab, false, hspan, 1);
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
		CCombo combo = new CCombo(parent, style);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		toolkit.adapt(combo, true, true);

		TableWrapData data = new TableWrapData(TableWrapData.FILL);
		// TODO Calculate with according to page width, number of columns horizontal spacing, etc.
		data.maxWidth = MAX_WITDH;
		data.grabHorizontal = hgrab;
		data.grabVertical = vgrab;
		data.align = halign;
		data.valign = valign;
		data.colspan = hspan;
		data.rowspan = vspan;
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
	public Button createCheckBoxButton(Composite parent, String text, int style, int colspan, boolean grabHorizontal) {
		return createCheckBoxButton(parent, text, style, TableWrapData.FILL, TableWrapData.TOP, grabHorizontal, false, colspan, 1);
	}

	@Override
	public Button createCheckBoxButton(Composite parent, String text, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan,
			int vspan) {
		Button button = toolkit.createButton(parent, text, SWT.CHECK | style);

		TableWrapData data = new TableWrapData(TableWrapData.FILL);
		data.grabHorizontal = hgrab;
		data.grabVertical = vgrab;
		data.align = halign;
		data.valign = valign;
		data.colspan = hspan;
		data.rowspan = vspan;
		button.setLayoutData(data);

		return button;
	}
}

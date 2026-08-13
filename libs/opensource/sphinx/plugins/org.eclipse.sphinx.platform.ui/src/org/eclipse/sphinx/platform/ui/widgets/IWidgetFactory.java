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

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Hyperlink;

public interface IWidgetFactory {

	Button createButton(Composite parent, String text);

	Button createButton(Composite parent, String text, int style);

	Button createButton(Composite parent, String text, int style, int valign, boolean hgrab, int hspan);

	Button createButton(Composite parent, String text, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan, int vspan);

	Label createLabel(Composite parent, String text);

	Label createLabel(Composite parent, String text, int valign);

	Label createLabel(Composite parent, String text, int colspan, boolean grabHorizontal);

	Label createLabel(Composite parent, String text, boolean multiLine, int hspan, boolean hgrab);

	Label createLabel(Composite parent, String text, int colspan, boolean grabHorizontal, int valign);

	Label createLabel(Composite parent, String text, int style, int hspan, boolean grabHorizontal, int valign);

	Label createLabel(Composite parent, String text, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan, int vspan);

	Text createText(Composite parent);

	Text createText(Composite parent, int style);

	Text createText(Composite parent, boolean multiLine);

	Text createText(Composite parent, int colspan, boolean grabHorizontal);

	Text createText(Composite parent, boolean multiLine, int colspan, boolean grabHorizontal);

	Text createText(Composite parent, boolean multiLine, int style, int colspan, boolean grabHorizontal);

	Text createText(Composite parent, boolean multiLine, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan, int vspan);

	Combo createCombo(Composite parent, int style, boolean hgrab, int hspan);

	Combo createCombo(Composite parent, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan, int vspan);

	CCombo createCCombo(Composite parent);

	CCombo createCCombo(Composite parent, boolean editable);

	CCombo createCCombo(Composite parent, int colspan, boolean grabHorizontal);

	CCombo createCCombo(Composite parent, int style, boolean hgrab, int hspan);

	CCombo createCCombo(Composite parent, boolean editable, int colspan, boolean grabHorizontal);

	CCombo createCCombo(Composite parent, boolean editable, int style, int colspan, boolean grabHorizontal);

	CCombo createCCombo(Composite parent, boolean editable, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan, int vspan);

	CCombo createCCombo(Composite parent, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan, int vspan);

	Button createCheckBoxButton(Composite parent, String text);

	Button createCheckBoxButton(Composite parent, String text, int colspan, boolean grabHorizontal);

	Button createCheckBoxButton(Composite parent, String text, int style, int colspan, boolean grabHorizontal);

	Button createCheckBoxButton(Composite parent, String text, int style, int halign, int valign, boolean hgrab, boolean vgrab, int hspan, int vspan);

	Hyperlink createHyperlink(Composite parent, String text);

	Hyperlink createHyperlink(Composite parent, String text, int colspan, boolean grabHorizontal);

	Hyperlink createHyperlink(Composite parent, String text, int valign);

	Hyperlink createHyperlink(Composite parent, String text, int colspan, boolean grabHorizontal, int valign);

	Hyperlink createHyperlink(Composite parent, String text, int style, int hspan, boolean grabHorizontal, int valign);

	Hyperlink createHyperlink(Composite parent, String text, int style, int halign, int valign, boolean hgrab, boolean vgrab, int colspan, int rowspan);
}
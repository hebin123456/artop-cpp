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

import org.eclipse.sphinx.platform.ui.widgets.IWidgetFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * 
 */
public class BasicHyperlinkField extends BasicField {

	/**
	 * The hyperlink adapter
	 */
	private HyperlinkAdapter fHyperlinkAdapter;

	/**
	 * @param widgetFactory
	 * @param hyperlinkAdapter
	 */
	public BasicHyperlinkField(IWidgetFactory widgetFactory, HyperlinkAdapter hyperlinkAdapter) {
		super(widgetFactory);
		fHyperlinkAdapter = hyperlinkAdapter;
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.BasicField#createLabelControl(org.eclipse.swt.widgets.Composite,
	 * boolean, int)
	 */
	@Override
	protected Control createLabelControl(Composite parent, boolean multiLine, int hspan) {
		assertCompositeNotNull(parent);
		Control labelControl = null;
		if (fWidgetFactory != null) {
			labelControl = fWidgetFactory.createHyperlink(parent, fLabelText != null ? fLabelText : ""); //$NON-NLS-1$
			if (labelControl != null) {
				labelControl.setFont(parent.getFont());
				// FIXME Commented since link should always be enabled.
				// labelControl.setEnabled(isEnabled());
				if (fHyperlinkAdapter != null) {
					((Hyperlink) labelControl).addHyperlinkListener(fHyperlinkAdapter);
				}
			}
		} else {
			labelControl = super.createLabelControl(parent, multiLine, hspan);
		}
		return labelControl;
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.BasicField#setLabelText(java.lang.String)
	 */
	@Override
	public void setLabelText(String labeltext) {
		super.setLabelText(labeltext);
		if (isOkToUse(fLabelControl)) {
			if (fLabelControl instanceof Hyperlink) {
				((Hyperlink) fLabelControl).setText(labeltext);
			}
		}
	}
}

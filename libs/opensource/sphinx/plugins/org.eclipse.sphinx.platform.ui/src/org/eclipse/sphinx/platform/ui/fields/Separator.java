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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * <p align=center>
 * <b><em>Separator</em></b>
 * </p>
 * <p align=justify>
 * Dialog field describing a separator.
 * </p>
 */
public class Separator extends BasicField {

	private Label fSeparator;

	private int fStyle;

	public Separator() {
		this(SWT.NONE);
	}

	/**
	 * @param style
	 *            of the separator. See <code>Label</code> for possible styles.
	 */
	public Separator(int style) {
		this(null, style);
	}

	public Separator(IWidgetFactory widgetFactory, int style) {
		super(widgetFactory);
		fStyle = style;
	}

	// ------- layout helpers

	/**
	 * Creates the separator and fills it in a <code>MGridLayout</code>.
	 * 
	 * @param parent
	 *            The parent composite on this separator.
	 * @param columns
	 *            The number of columns of this separator.
	 * @param height
	 *            The height of this separator.
	 * @return The controls of this separator.
	 */
	public Control[] fillIntoGrid(Composite parent, int columns, int height) {

		Control separator = getSeparator(parent);
		separator.setLayoutData(LayoutUtil.gridDataForSeperator(columns, height));

		return new Control[] { separator };
	}

	/*
	 * @see BasicField#getNumberOfControls
	 */
	@Override
	protected int getNumberOfControls() {
		return 1;
	}

	// ------- ui creation

	/**
	 * Creates or returns the created separator.
	 * 
	 * @param parent
	 *            The parent composite or <code>null</code> if the widget has already been created.
	 */
	public Control getSeparator(Composite parent) {
		if (fSeparator == null) {
			assertCompositeNotNull(parent);
			fSeparator = new Label(parent, fStyle);
		}
		return fSeparator;
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.BasicField#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (isOkToUse(fSeparator)) {
			fSeparator.dispose();
		}
	}
}

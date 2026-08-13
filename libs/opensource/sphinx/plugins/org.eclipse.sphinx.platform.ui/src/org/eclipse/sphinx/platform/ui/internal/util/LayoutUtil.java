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
package org.eclipse.sphinx.platform.ui.internal.util;

import org.eclipse.sphinx.platform.ui.util.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * <p align=center>
 * <b><em>UI Layout Utility</em></b>
 * </p>
 * <p align=justify>
 * Provides utility methods to set parameters of UI layouts.
 * </p>
 */
public class LayoutUtil {

	/**
	 * The maximum width allowed for this field.
	 */
	protected static final int MAX_WITDH = 175;

	/**
	 * Sets the width hint of a control. Assumes that GridData is used.
	 */
	public static void setWidthHint(Control control, int widthHint) {
		Object ld = control.getLayoutData();
		if (ld instanceof GridData) {
			((GridData) ld).widthHint = widthHint;
		}
	}

	/**
	 * Sets the horizontal grabbing of a control to true. Assumes that GridData is used.
	 */
	public static void setHorizontalGrabbing(Control control) {
		Object ld = control.getLayoutData();
		if (ld instanceof GridData) {
			((GridData) ld).grabExcessHorizontalSpace = true;
		}
	}

	/**
	 * Sets the span of a control. Assumes that GridData is used.
	 */
	public static void setHorizontalSpan(Control control, int span) {
		Object ld = control.getLayoutData();
		if (ld instanceof GridData) {
			((GridData) ld).horizontalSpan = span;
		} else if (span != 1) {
			GridData gd = new GridData();
			gd.horizontalSpan = span;
			control.setLayoutData(gd);
		}
	}

	// ------------------------------------------------------------------------
	// Layout helpers
	// ------------------------------------------------------------------------

	/*
	 * Combo
	 */

	public static final GridData gridDataForCombo(int span) {
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = false;
		gd.horizontalSpan = span;
		return gd;
	}

	public static final TableWrapData tableWrapDataForCombo(int span) {
		TableWrapData twd = new TableWrapData();
		twd.align = TableWrapData.FILL;
		twd.grabHorizontal = false;
		twd.colspan = span;
		return twd;
	}

	public static final GridData gridDataForCCombo(int span) {
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = span;
		return gd;
	}

	public static final TableWrapData tableWrapDataForCCombo(int span) {
		TableWrapData twd = new TableWrapData(TableWrapData.FILL, TableWrapData.MIDDLE);
		twd.grabHorizontal = true;
		twd.colspan = span;
		// TODO Calculate with according to page width, number of columns horizontal spacing, etc.
		twd.maxWidth = MAX_WITDH;
		return twd;
	}

	/*
	 * Button
	 */

	public static final GridData gridDataForButtons(int span) {
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = false;
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalSpan = span;
		return gd;
	}

	public static final TableWrapData tableWrapDataForButtons(int span) {
		TableWrapData twd = new TableWrapData();
		twd.align = TableWrapData.FILL;
		twd.grabHorizontal = false;
		twd.valign = TableWrapData.FILL;
		twd.grabVertical = true;
		twd.colspan = span;
		return twd;
	}

	/**
	 * Creates a {@link GridData} for the specified {@link Button button} widget.
	 * <p>
	 * This method initializes grid data as follows:
	 * <ul>
	 * <li>fills horizontally;</li>
	 * <li>does not grab horizontal space;</li>
	 * <li>spans the right number of columns;</li>
	 * <li>computes the right width if button style is <em>push</em>.</li>
	 * </ul>
	 * 
	 * @param button
	 *            The button widget for which a grid data must be created and initialized.
	 * @param span
	 *            The number of columns the button must take up.
	 * @return The created {@link GridData} for the specified {@link Button button}, correctly initialized.
	 */
	public static final GridData gridDataForButton(Button button, int span) {
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = false;
		gd.horizontalSpan = span;
		if ((button.getStyle() & SWT.PUSH) != 0) {
			gd.widthHint = SWTUtil.getButtonWidthHint(button);
		}
		return gd;
	}

	/**
	 * Creates a {@link TableWrapData} for the specified {@link Button button} widget.
	 * <p>
	 * This method initializes table wrap data as follows:
	 * <ul>
	 * <li>fills horizontally;</li>
	 * <li>does not grab horizontal space;</li>
	 * <li>spans the right number of columns;</li>
	 * <li>computes the right width if button style is <em>push</em>.</li>
	 * </ul>
	 * 
	 * @param button
	 *            The button widget for which a table wrap data must be created and initialized.
	 * @param span
	 *            The number of columns the button must take up.
	 * @return The created {@link TableWrapData} for the specified {@link Button button}, correctly initialized.
	 */
	public static final TableWrapData tableWrapDataForButton(Button button, int span) {
		// FIXME Find the right one
		// TableWrapData twd = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		TableWrapData twd = new TableWrapData();
		twd.align = TableWrapData.FILL;
		twd.grabHorizontal = false;
		twd.colspan = span;
		if ((button.getStyle() & SWT.PUSH) != 0) {
			twd.maxWidth = SWTUtil.getButtonWidthHint(button);
		}
		return twd;
	}

	/*
	 * Label
	 */

	public static final GridData gridDataForLabel(int span) {
		return gridDataForLabel(span, false);
	}

	public static final GridData gridDataForLabel(int span, boolean multiLine) {
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = span;
		gd.verticalAlignment = multiLine ? GridData.BEGINNING : GridData.CENTER;
		return gd;
	}

	public static final TableWrapData tableWrapDataForLabel(int span) {
		return tableWrapDataForLabel(span, false);
	}

	public static final TableWrapData tableWrapDataForLabel(int span, boolean multiLine) {
		TableWrapData twd = new TableWrapData();
		twd.colspan = span;
		twd.valign = multiLine ? TableWrapData.TOP : TableWrapData.MIDDLE;
		return twd;
	}

	/*
	 * List
	 */

	public static final GridData gridDataForList(int span, PixelConverter converter) {
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalSpan = span;
		gd.widthHint = converter.convertWidthInCharsToPixels(50);
		gd.heightHint = converter.convertHeightInCharsToPixels(6);
		return gd;
	}

	public static final TableWrapData tableWrapDataForList(int span, PixelConverter converter) {
		TableWrapData twd = new TableWrapData();
		twd.align = TableWrapData.FILL;
		twd.grabHorizontal = true;
		twd.valign = TableWrapData.FILL;
		twd.grabVertical = true;
		twd.colspan = span;
		twd.maxWidth = converter.convertWidthInCharsToPixels(50);
		twd.heightHint = converter.convertHeightInCharsToPixels(6);
		return twd;
	}

	/*
	 * Text
	 */

	public static final GridData gridDataForText(int span) {
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = span;
		return gd;
	}

	public static final TableWrapData tableWrapDataForText(int span) {
		TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB);
		// twd.align = TableWrapData.FILL;
		// twd.grabHorizontal = true;
		twd.colspan = span;
		twd.valign = TableWrapData.MIDDLE;
		return twd;
	}

	/*
	 * Separator
	 */

	public static final GridData gridDataForSeperator(int span, int height) {
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.heightHint = height;
		gd.horizontalSpan = span;
		return gd;
	}

	/*
	 * Specific composite
	 */

	/**
	 * Convenient method for creating a {@link GridData} dedicated to a specific {@linkplain Composite composite} (e.g.
	 * for list button field or string button field).
	 * 
	 * @param span
	 *            The number of column the composite (for which the grid data is created) will take up.
	 * @return The grid data to use for a specific composite.
	 */
	public static final GridData gridDataForSpecificComposite(int span) {
		return gridDataForSpecificComposite(span, true, true);
	}

	/**
	 * Convenient method for creating a {@link GridData} dedicated to a specific {@linkplain Composite composite} (e.g.
	 * for list button field or string button field).
	 * 
	 * @param span
	 *            The number of column the composite (for which the grid data is created) will take up.
	 * @param hgrab
	 *            Flag indicating whether composite (for which the grid data is created) should be made wide enough to
	 *            fit the remaining horizontal space.
	 * @param vgrab
	 *            Flag indicating whether composite (for which the grid data is created) should be made wide enough to
	 *            fit the remaining vertical space.
	 * @return The grid data to use for a specific composite.
	 */
	public static final GridData gridDataForSpecificComposite(int span, boolean hgrab, boolean vgrab) {
		GridData gd = new GridData(SWT.FILL, SWT.FILL, hgrab, vgrab);
		gd.horizontalSpan = span;
		return gd;
	}

	/**
	 * Convenient method for creating a {@link GridLayout} dedicated to a specific {@linkplain Composite composite}
	 * (e.g. for list button field or string button field).
	 * 
	 * @param numColumns
	 *            The number of columns in the layout.
	 * @return The grid layout to use for a specific composite, with the right number of columns.
	 */
	public static final GridLayout gridLayoutForSpecificComposite(int numColumns) {
		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.numColumns = numColumns;
		return gl;
	}

	/**
	 * Convenient method for creating a {@link TableWrapData} dedicated to a specific {@linkplain Composite composite}
	 * (e.g. for list button field or string button field).
	 * 
	 * @param span
	 *            The number of columns the composite (for which the table wrap data is created) will take up.
	 * @return The table wrap data to use for a specific composite.
	 */
	public static final TableWrapData tableWrapDataForSpecificComposite(int span) {
		return tableWrapDataForSpecificComposite(span, true, true);
	}

	/**
	 * Convenient method for creating a {@link TableWrapData} dedicated to a specific {@linkplain Composite composite}
	 * (e.g. for list button field or string button field).
	 * 
	 * @param span
	 *            The number of columns the composite (for which the table wrap data is created) will take up.
	 * @param hgrab
	 *            Flag indicating whether composite (for which the table wrap data is created) should be made wide
	 *            enough to fit the remaining horizontal space.
	 * @param vgrab
	 *            Flag indicating whether composite (for which the table wrap data is created) should be made wide
	 *            enough to fit the remaining vertical space.
	 * @return The table wrap data to use for a specific composite.
	 */
	public static final TableWrapData tableWrapDataForSpecificComposite(int span, boolean hgrab, boolean vgrab) {
		TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB);
		twd.colspan = span;
		twd.grabHorizontal = hgrab;
		twd.grabVertical = vgrab;
		return twd;
	}

	/**
	 * Convenient method for creating a {@link TableWrapLayout} dedicated to a specific {@linkplain Composite composite}
	 * (e.g. for list button field or string button field).
	 * 
	 * @param numColumns
	 *            The number of columns in the layout.
	 * @return The table wrap layout to use for a specific composite, with the right number of columns.
	 */
	public static final TableWrapLayout tableWrapLayoutForSpecificComposite(int numColumns) {
		TableWrapLayout twl = new TableWrapLayout();
		twl.topMargin = 0;
		twl.bottomMargin = 0;
		twl.leftMargin = 0;
		twl.rightMargin = 0;
		twl.numColumns = numColumns;
		return twl;
	}
}

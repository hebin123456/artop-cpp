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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <p align=center>
 * <b><em>Sphinx Platform Field </em></b>
 * </p>
 * <p>
 * A platform field is made of several controls (i.e. widgets such as: <em>label</em>, <em>text</em>, <em>combo</em>,
 * <em>button</em>, <em>list</em>, etc.). From the association of such controls result a field whose behavior is
 * specialized according to the widgets it owns. The field content creation is supposed to be performed inside method
 * <tt>fillIntoGrid</tt> that must be implemented by every field.
 * <p>
 * Every field contains at least a label; this label displays a text that is owned by the field. This interface provides
 * methods allowing: to set the text of this label control, to set the tool tip of this label control, or to get this
 * label control.
 * <p>
 * A platform field may have listeners that can be added or removed thanks to methods provided by this interface. Inside
 * concrete implementation of this interface, it is recommended to use a listener list (<tt>ListenerList</tt>) in order
 * to store listeners associated to this field.
 * 
 * @see org.eclipse.swt.widgets.Label
 * @see org.eclipse.core.runtime.ListenerList
 */
public interface IField {

	/**
	 * Creates all controls of the dialog field and fills it to a composite. The composite is assumed to have a
	 * <tt>GridLayout</tt> or a <tt>FormLayout</tt> as layout. The field will adjust its controls' spans to the given
	 * number of columns.
	 * 
	 * @see org.eclipse.swt.layout.GridLayout
	 * @see org.eclipse.swt.layout.FormLayout
	 * @param parent
	 *            The parent composite into which this field must be created.
	 * @param nColumns
	 *            The number of columns over which this field must span.
	 * @return The created controls constituting this field.
	 */
	Control[] fillIntoGrid(Composite parent, int nColumns);

	/**
	 * @return The label control of this field.
	 */
	Control getLabelControl();

	/**
	 * Sets the enable state of the field.
	 * 
	 * @param enabled
	 *            The new enable state.
	 */
	void setEnabled(boolean enabled);

	/**
	 * Gets the enable state of the dialog field.
	 */
	boolean isEnabled();

	/**
	 * Sets the label of the dialog field.
	 * 
	 * @param labeltext
	 *            The text of the label.
	 */
	void setLabelText(String labeltext);

	/**
	 * Sets the tool tip of the dialog field.
	 * 
	 * @param toolTip
	 *            The text to use as tool tip.
	 */
	void setToolTipText(String toolTip);

	/**
	 * Adds the given field listener in the listeners list of this field.
	 * 
	 * @param listener
	 *            The field listener to add.
	 */
	void addFieldListener(IFieldListener listener);

	/**
	 * Removes the given field listener from the listeners list of this field.
	 * 
	 * @param listener
	 *            The field listener to remove.
	 */
	void removeFieldListener(IFieldListener listener);

	/**
	 * Disposes of the operating system resources associated with the field.
	 */
	void dispose();
}
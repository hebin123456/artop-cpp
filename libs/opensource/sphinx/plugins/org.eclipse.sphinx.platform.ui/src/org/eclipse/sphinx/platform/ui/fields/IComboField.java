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

import org.eclipse.swt.widgets.Control;

/**
 * 
 */
public interface IComboField extends IField {

	/**
	 * @return
	 */
	Control getComboControl();

	/**
	 * Gets the combo items.
	 */
	String[] getItems();

	/**
	 * @return
	 */
	int getSelectionIndex();

	/**
	 * Selects an item.
	 */
	boolean selectItem(int index);

	/**
	 * Sets the combo items. Triggers a dialog-changed event.
	 */
	void setItems(String[] items);

	/**
	 * Sets the text. Triggers a dialog-changed event.
	 */
	void setText(String text);
}
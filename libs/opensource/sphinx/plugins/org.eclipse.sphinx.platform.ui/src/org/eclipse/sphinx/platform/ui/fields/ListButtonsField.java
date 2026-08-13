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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.sphinx.platform.ui.fields.adapters.IListAdapter;
import org.eclipse.sphinx.platform.ui.internal.util.LayoutUtil;
import org.eclipse.sphinx.platform.ui.internal.util.PixelConverter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * 
 */
public class ListButtonsField extends ListField {

	/**
	 * Constructor.
	 * 
	 * @param adapter
	 *            The list adapter.
	 * @param buttonLabels
	 *            The labels of the buttons to create next to the list.
	 * @param provider
	 *            The label provider to use for the list.
	 */
	public ListButtonsField(IListAdapter adapter, String[] buttonLabels, ILabelProvider provider) {
		super(adapter, buttonLabels, provider);
	}

	@Override
	protected Control[] doFillIntoGrid(Composite parent, int nColumns) {
		PixelConverter converter = new PixelConverter(parent);

		Control label = getLabelControl(parent, true, 1);

		// Creates a specific composite so that button could correctly be created (size & position)
		Composite composite = createSpecificComposite(parent, getNumberOfControls() - 1, nColumns - 1, true, true, fUseFormLayout);

		Control list = getListControl(composite);
		// Leave last column for buttons if such are defined, span over buttons column otherwise
		int nListColumns = hasButtons() ? 1 : 2;
		if (fUseFormLayout) {
			list.setLayoutData(LayoutUtil.tableWrapDataForList(nListColumns, converter));
		} else {
			list.setLayoutData(LayoutUtil.gridDataForList(nListColumns, converter));
		}

		Composite buttons = getButtonBox(composite);
		if (buttons != null) {
			if (fUseFormLayout) {
				buttons.setLayoutData(LayoutUtil.tableWrapDataForButtons(1));
			} else {
				buttons.setLayoutData(LayoutUtil.gridDataForButtons(1));
			}
		}

		return buttons != null ? new Control[] { label, list, buttons } : new Control[] { label, list };
	}
}

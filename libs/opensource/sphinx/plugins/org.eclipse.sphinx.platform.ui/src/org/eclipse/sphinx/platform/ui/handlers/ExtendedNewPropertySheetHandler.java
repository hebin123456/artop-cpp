/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.platform.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.views.properties.NewPropertySheetHandler;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertyShowInContext;

public class ExtendedNewPropertySheetHandler extends NewPropertySheetHandler {

	/**
	 * Whether new PVs are pinned when newly opened
	 */
	private static final boolean PIN_NEW_PROPERTY_VIEW = Boolean.valueOf(
			System.getProperty("org.eclipse.ui.views.properties.pinNewPV", Boolean.FALSE.toString())).booleanValue(); //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePartChecked(event);

		PropertyShowInContext context = getShowInContext(event);
		try {
			PropertySheet sheet = findPropertySheet(event, context);
			Object input = context.getInput();
			if (input instanceof IStructuredSelection && !((IStructuredSelection) input).isEmpty()) {
				context.setSelection((IStructuredSelection) input);
			}
			sheet.show(context);
			if (activePart instanceof PropertySheet) {
				PropertySheet parent = (PropertySheet) activePart;
				parent.setPinned(true);
			} else if (!sheet.isPinned()) {
				sheet.setPinned(PIN_NEW_PROPERTY_VIEW);
			}
		} catch (PartInitException e) {
			throw new ExecutionException("Part could not be initialized", e); //$NON-NLS-1$
		}
		return null;
	}
}

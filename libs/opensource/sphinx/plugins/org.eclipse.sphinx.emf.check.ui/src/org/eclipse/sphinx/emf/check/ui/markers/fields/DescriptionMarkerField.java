/**
 * <copyright>
 *
 * Copyright (c) 2014-2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [458976] Validators are not singleton when they implement checks for different EPackages
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.ui.markers.fields;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.sphinx.emf.check.ui.internal.Activator;
import org.eclipse.sphinx.emf.check.ui.internal.CheckValidationImageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

public class DescriptionMarkerField extends MarkerField {

	public DescriptionMarkerField() {
	}

	@Override
	public String getValue(MarkerItem item) {
		String message = item.getAttributeValue(IMarker.MESSAGE, ""); //$NON-NLS-1$
		String prefix = getCheckPrefix(item);
		if (prefix == null) {
			return message;
		}
		return prefix.concat(message);
	}

	private String getCheckPrefix(MarkerItem item) {
		if (item.getMarker() == null) {
			return null;
		}
		IMarker marker = item.getMarker();
		// If there is no image get the full image rather than the decorated one.
		if (marker != null) {
			if (IDE.getMarkerHelpRegistry().hasResolutions(marker)) {
				return "[@Check] "; //$NON-NLS-1$
			}
		}
		return null;
	}

	@Override
	public String getColumnHeaderText() {
		return "Description"; //$NON-NLS-1$
	}

	@Override
	public int getDefaultColumnWidth(Control control) {
		return 250;
	}

	/**
	 * Get the image for the receiver.
	 *
	 * @param item
	 * @return Image
	 */
	private Image getImage(MarkerItem item) {
		if (item.getMarker() == null) {
			// FIXME: could not make a test instanceof MarkerCategory as it is private.
			// !! workaround !! warning/error groups shall have the same icon
			return Activator.getDefault().getImageRegistry().get(CheckValidationImageProvider.GROUP_ICO);
		}
		int severity = item.getAttributeValue(IMarker.SEVERITY, -1);
		if (severity == IMarker.SEVERITY_ERROR) {
			return Activator.getDefault().getImageRegistry().get(CheckValidationImageProvider.ERROR_ICO);
		} else if (severity == IMarker.SEVERITY_WARNING) {
			return Activator.getDefault().getImageRegistry().get(CheckValidationImageProvider.WARNING_ICO);
		} else if (severity == IMarker.SEVERITY_INFO) {
			return Activator.getDefault().getImageRegistry().get(CheckValidationImageProvider.INFO_ICO);
		}
		return null;
	}

	@Override
	public void update(ViewerCell cell) {
		super.update(cell);
		MarkerItem item = (MarkerItem) cell.getElement();
		cell.setImage(annotateImage(item, getImage(item)));
		cell.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		cell.setForeground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
	}
}

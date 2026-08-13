/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     See4sys - added support for problem markers on model objects (rather than 
 *               only on workspace resources). Unfortunately, there was no other 
 *               choice than copying the whole code from 
 *               org.eclipse.ui.views.markers.internal for that purpose because 
 *               many of the relevant classes, methods, and fields are private or
 *               package private.
 *******************************************************************************/
package org.eclipse.sphinx.emf.validation.ui.views;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

class DialogProblemProperties extends DialogMarkerProperties {

	private Label severityLabel;

	private Label severityImage;

	DialogProblemProperties(Shell parentShell) {
		super(parentShell);
		setType(IMarker.PROBLEM);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.views.markerview.MarkerPropertiesDialog#createAttributesArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createAttributesArea(Composite parent) {
		createSeperator(parent);
		super.createAttributesArea(parent);

		new Label(parent, SWT.NONE).setText(MarkerMessages.propertiesDialog_severityLabel);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		severityImage = new Label(composite, SWT.NONE);
		severityLabel = new Label(composite, SWT.NONE);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerPropertiesDialog#updateDialogFromMarker()
	 */
	@Override
	protected void updateDialogFromMarker() {
		super.updateDialogFromMarker();
		IMarker marker = getMarker();
		if (marker == null) {
			return;
		}

		severityImage.setImage(Util.getImage(marker.getAttribute(IMarker.SEVERITY, -1)));
		int severity = marker.getAttribute(IMarker.SEVERITY, -1);
		if (severity == IMarker.SEVERITY_ERROR) {
			severityLabel.setText(MarkerMessages.propertiesDialog_errorLabel);
		} else if (severity == IMarker.SEVERITY_WARNING) {
			severityLabel.setText(MarkerMessages.propertiesDialog_warningLabel);
		} else if (severity == IMarker.SEVERITY_INFO) {
			severityLabel.setText(MarkerMessages.propertiesDialog_infoLabel);
		} else {
			severityLabel.setText(MarkerMessages.propertiesDialog_noseverityLabel);
		}
	}
}

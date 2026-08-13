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
package org.eclipse.sphinx.emf.editors.forms.pages;

import org.eclipse.sphinx.emf.editors.forms.layouts.LayoutFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class MessagePage extends FormPage {

	protected String message;

	public MessagePage(FormEditor formEditor, String message) {
		super(formEditor, "007", ""); //$NON-NLS-1$ //$NON-NLS-2$
		this.message = message;
	}

	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		// Create single columned page layout
		Composite body = managedForm.getForm().getBody();
		body.setLayout(LayoutFactory.createFormBodyGridLayout(false, 1));

		// Display message indicating that editor input is being loaded
		FormToolkit toolkit = managedForm.getToolkit();
		Label label = toolkit.createLabel(body, message);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
}
/**
 * <copyright>
 * 
 * Copyright (c) 2012 itemis and others.
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
package org.eclipse.sphinx.examples.hummingbird20.editors.nebula.pages;

import org.eclipse.sphinx.emf.editors.forms.layouts.LayoutFactory;
import org.eclipse.sphinx.emf.editors.forms.pages.AbstractFormPage;
import org.eclipse.sphinx.examples.hummingbird20.editors.nebula.factory.ParameterValuesXViewerFactory;
import org.eclipse.sphinx.examples.hummingbird20.editors.nebula.messages.Messages;
import org.eclipse.sphinx.examples.hummingbird20.editors.nebula.sections.EditableParameterValuesXViewerSection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;

public class EditableParameterValuesOverviewPage extends AbstractFormPage {

	EditableParameterValuesXViewerSection parameterValuesSection;

	public EditableParameterValuesOverviewPage(FormEditor editor) {
		super(editor, Messages.title_EditableParameterValues_OverviewPage);
	}

	public EditableParameterValuesOverviewPage(FormEditor editor, String title) {
		super(editor, title);
	}

	@Override
	protected void doCreateFormContent(IManagedForm managedForm) {
		// Create single columned page layout
		Composite body = managedForm.getForm().getBody();
		body.setLayout(LayoutFactory.createFormBodyGridLayout(false, 1));

		// Create editable parameter values section
		ParameterValuesXViewerFactory xViewerFactory = new ParameterValuesXViewerFactory();
		parameterValuesSection = new EditableParameterValuesXViewerSection(this, pageInput, xViewerFactory);
		parameterValuesSection.createContent(managedForm, body);
		addSection(parameterValuesSection);
	}
}

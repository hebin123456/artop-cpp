/**
 * <copyright>
 *
 * Copyright (c) 2012-2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [460260] Expanded paths are collapsed on resource reload
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.editors.nebula.sections;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerFactory;
import org.eclipse.nebula.widgets.xviewer.edit.DefaultXViewerControlFactory;
import org.eclipse.nebula.widgets.xviewer.edit.XViewerEditAdapter;
import org.eclipse.sphinx.emf.editors.forms.nebula.sections.BasicXViewerSection;
import org.eclipse.sphinx.emf.editors.forms.pages.AbstractFormPage;
import org.eclipse.sphinx.examples.hummingbird20.editors.nebula.factory.ParameterValuesXViewerConverter;
import org.eclipse.sphinx.examples.hummingbird20.editors.nebula.messages.Messages;
import org.eclipse.sphinx.examples.hummingbird20.editors.nebula.providers.ParameterValuesXViewerContentProvider;
import org.eclipse.sphinx.examples.hummingbird20.editors.nebula.providers.ParameterValuesXViewerLabelProvider;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

public class EditableParameterValuesXViewerSection extends BasicXViewerSection {

	public EditableParameterValuesXViewerSection(AbstractFormPage formPage, Object sectionInput, XViewerFactory xViewerFactory) {
		this(formPage, sectionInput, xViewerFactory, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
	}

	public EditableParameterValuesXViewerSection(AbstractFormPage formPage, Object sectionInput, XViewerFactory xViewerFactory, int style) {
		super(formPage, sectionInput, xViewerFactory, style);

		title = Messages.title_ParameterValues_Section;
		description = Messages.desc_ParameterValues_Section;
	}

	@Override
	protected XViewerEditAdapter createXViewerEditAdapter() {
		return new XViewerEditAdapter(new DefaultXViewerControlFactory(), new ParameterValuesXViewerConverter());
	}

	@Override
	protected IContentProvider createContentProvider() {
		return new ParameterValuesXViewerContentProvider();
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new ParameterValuesXViewerLabelProvider((XViewer) getViewer());
	}
}

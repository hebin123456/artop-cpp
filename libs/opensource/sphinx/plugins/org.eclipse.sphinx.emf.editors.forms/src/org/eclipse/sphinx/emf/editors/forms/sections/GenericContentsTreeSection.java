/**
 * <copyright>
 *
 * Copyright (c) 2008-2015 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [393310] Viewer input for GenericContentsTreeSection should be calculated using content provider
 *     itemis - [421585] Form Editor silently closes if model is not loaded via Sphinx
 *     itemis - [425173] Form editor closes when the input resource are changed externally
 *     itemis - [460260] Expanded paths are collapsed on resource reload
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.editors.forms.sections;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.editors.forms.BasicTransactionalFormEditor;
import org.eclipse.sphinx.emf.editors.forms.internal.messages.Messages;
import org.eclipse.sphinx.emf.editors.forms.pages.AbstractFormPage;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class GenericContentsTreeSection extends AbstractViewerFormSection {

	public GenericContentsTreeSection(AbstractFormPage formPage, Object sectionInput) {
		this(formPage, sectionInput, SWT.NONE);
	}

	public GenericContentsTreeSection(AbstractFormPage formPage, Object sectionInput, int style) {
		super(formPage, sectionInput, style);
		description = Messages.section_genericContentsTree_description;
	}

	@Override
	public void setSectionInput(Object sectionInput) {
		super.setSectionInput(sectionInput);

		// Refresh section title
		refreshSectionTitle();
	}

	protected Object getSectionInputParent() {
		IContentProvider contentProvider = getContentProvider();
		if (contentProvider instanceof ITreeContentProvider) {
			return ((ITreeContentProvider) contentProvider).getParent(sectionInput);
		}
		return null;
	}

	@Override
	public Object getViewerInput() {
		// Don't display resource behind section input in viewer
		if (!(sectionInput instanceof Resource)) {
			return getSectionInputParent();
		}
		return sectionInput;
	}

	@Override
	protected int getNumberOfColumns() {
		return 1;
	}

	@Override
	protected void createSectionClientContent(final IManagedForm managedForm, final SectionPart sectionPart, Composite sectionClient) {
		Assert.isNotNull(managedForm);
		Assert.isNotNull(sectionPart);
		Assert.isNotNull(sectionClient);

		// Create model contents tree
		FormToolkit toolkit = managedForm.getToolkit();
		Tree tree = toolkit.createTree(sectionClient, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create model contents tree viewer
		final BasicTransactionalFormEditor formEditor = formPage.getTransactionalFormEditor();
		TreeViewer treeViewer = new TreeViewer(tree) {
			@Override
			public void setSelection(ISelection selection) {
				selection = !SelectionUtil.getStructuredSelection(selection).isEmpty() ? selection : formEditor.getDefaultSelection();
				super.setSelection(selection);
			}

			@Override
			public void refresh(Object element, boolean updateLabels) {
				GenericContentsTreeSection.this.recordViewerState();
				super.refresh(element, updateLabels);
			}
		};

		IContentProvider contentProvider = getContentProvider();
		if (contentProvider != null) {
			treeViewer.setContentProvider(getContentProvider());
		}
		IBaseLabelProvider labelProvider = getLabelProvider();
		if (labelProvider != null) {
			treeViewer.setLabelProvider(labelProvider);
		}

		treeViewer.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				// Show only sectionInput but not its siblings
				return parentElement != getSectionInputParent() || element == sectionInput;
			}
		});

		treeViewer.setInput(getViewerInput());

		setViewer(treeViewer);

		formEditor.createContextMenuFor(treeViewer);
	}

	@Override
	public void refreshSection() {
		refreshSectionTitle();
	}

	protected void refreshSectionTitle() {
		if (title == null && isControlAccessible(section)) {
			section.setText(NLS.bind(Messages.section_genericContentsTree_title, getSectionInputName()));
		}
	}
}

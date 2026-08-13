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
 *     itemis - [460260] Expanded paths are collapsed on resource reload
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.editors.forms.sections;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.ui.viewer.IViewerProvider;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.ui.provider.TransactionalAdapterFactoryContentProvider;
import org.eclipse.emf.transaction.ui.provider.TransactionalAdapterFactoryLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sphinx.emf.editors.forms.BasicTransactionalEditorActionBarContributor;
import org.eclipse.sphinx.emf.editors.forms.pages.AbstractFormPage;
import org.eclipse.sphinx.emf.workspace.ui.viewers.state.ITreeViewerState;
import org.eclipse.sphinx.emf.workspace.ui.viewers.state.TreeViewerStateRecorder;
import org.eclipse.sphinx.platform.util.ReflectUtil;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;

public abstract class AbstractViewerFormSection extends AbstractFormSection implements IViewerProvider {

	/**
	 * @deprecated Use {@link #getViewer()} instead.
	 */
	@Deprecated
	protected StructuredViewer viewer;

	// TODO Rename to "viewer" once deprectated protected viewer field can be removed
	private Viewer privateViewer;
	private ITreeViewerState viewerState = null;
	private TreeViewerStateRecorder treeViewerStateRecorder = new TreeViewerStateRecorder();
	private IContentProvider contentProvider;
	private IBaseLabelProvider labelProvider;

	public AbstractViewerFormSection(AbstractFormPage formPage, Object sectionInput) {
		super(formPage, sectionInput);
	}

	public AbstractViewerFormSection(AbstractFormPage formPage, Object sectionInput, int style) {
		super(formPage, sectionInput, style);
	}

	protected void recordViewerState() {
		if (viewerState == null) {
			viewerState = treeViewerStateRecorder.recordState();
		}
	}

	protected void applyViewerState() {
		treeViewerStateRecorder.applyState(viewerState);
		viewerState = null;
	}

	@Override
	public void setSectionInput(Object sectionInput) {
		super.setSectionInput(sectionInput);

		if (privateViewer != null) {
			Object oldViewerInput = privateViewer.getInput();
			Object newViewerInput = getViewerInput();
			if (newViewerInput != oldViewerInput) {
				privateViewer.setInput(newViewerInput);
			}
		}

		applyViewerState();
	}

	@Override
	public Viewer getViewer() {
		return privateViewer;
	}

	public void setViewer(Viewer viewer) {
		privateViewer = viewer;
		if (viewer instanceof StructuredViewer) {
			this.viewer = (StructuredViewer) viewer;
		}

		if (viewer instanceof TreeViewer) {
			treeViewerStateRecorder.setViewer((TreeViewer) viewer);
		}
	}

	public Object getViewerInput() {
		return sectionInput;
	}

	@Override
	protected Composite doCreateSectionClient(final IManagedForm managedForm, final SectionPart sectionPart) {
		Composite composite = super.doCreateSectionClient(managedForm, sectionPart);

		if (privateViewer != null) {
			// Register viewer as selection provider
			formPage.getTransactionalFormEditor().setSelectionProvider(privateViewer);
			privateViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					managedForm.fireSelectionChanged(sectionPart, event.getSelection());
					formPage.getTransactionalFormEditor().setSelectionProvider(privateViewer);
				}
			});

			// Create viewer context menu
			createViewerContextMenu();
		}
		return composite;
	}

	@Override
	public boolean isEmpty() {
		if (privateViewer != null) {
			try {
				Object[] filteredChildren = (Object[]) ReflectUtil.invokeInvisibleMethod(privateViewer, "getFilteredChildren", getViewerInput()); //$NON-NLS-1$
				return filteredChildren.length == 0;
			} catch (Exception ex) {
				// Ignore exception
			}
		}
		return false;
	}

	@Override
	protected void focusGained(FocusEvent e) {
		super.focusGained(e);
		EditingDomainActionBarContributor actionBarContributor = formPage.getTransactionalFormEditor().getActionBarContributor();
		if (actionBarContributor instanceof BasicTransactionalEditorActionBarContributor) {
			((BasicTransactionalEditorActionBarContributor) actionBarContributor).setGlobalActionHandlers();
		}
	}

	public IContentProvider getContentProvider() {
		if (contentProvider == null) {
			contentProvider = createContentProvider();
		}
		return contentProvider;
	}

	protected IContentProvider createContentProvider() {
		AdapterFactory adapterFactory = getCustomAdapterFactory();
		if (adapterFactory != null) {
			EditingDomain editingDomain = formPage.getTransactionalFormEditor().getEditingDomain();
			if (editingDomain instanceof TransactionalEditingDomain) {
				return new TransactionalAdapterFactoryContentProvider((TransactionalEditingDomain) editingDomain, adapterFactory);
			}
		}
		return formPage.getContentProvider();
	}

	public IBaseLabelProvider getLabelProvider() {
		if (labelProvider == null) {
			labelProvider = createLabelProvider();
		}
		return labelProvider;
	}

	protected IBaseLabelProvider createLabelProvider() {
		AdapterFactory adapterFactory = getCustomAdapterFactory();
		if (adapterFactory != null) {
			EditingDomain editingDomain = formPage.getTransactionalFormEditor().getEditingDomain();
			if (editingDomain instanceof TransactionalEditingDomain) {
				return new TransactionalAdapterFactoryLabelProvider((TransactionalEditingDomain) editingDomain, adapterFactory);
			}
		}
		return formPage.getLabelProvider();
	}

	protected AdapterFactory getCustomAdapterFactory() {
		return null;
	}

	/**
	 * Creates context menu for viewer.
	 */
	protected void createViewerContextMenu() {
		// Do nothing by default.
	}
}

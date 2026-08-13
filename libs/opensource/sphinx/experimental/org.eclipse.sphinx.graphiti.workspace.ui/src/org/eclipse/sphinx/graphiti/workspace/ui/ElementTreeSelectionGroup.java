/**
 * <copyright>
 * 
 * Copyright (c) 2008-2012 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [392424] Migrate Sphinx integration of Graphiti to Graphiti 0.9.x
 * 
 * </copyright>
 */
package org.eclipse.sphinx.graphiti.workspace.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.DrillDownComposite;

/**
 * Workbench-level composite for choosing a container.
 */
public class ElementTreeSelectionGroup extends Composite {

	// A collection of the initially-selected elements
	private List<Object> initialSelections = new ArrayList<Object>();

	private TreeViewer treeViewer;

	private ILabelProvider labelProvider;
	private ITreeContentProvider contentProvider;

	private ViewerComparator comparator;

	private List<ViewerFilter> fFilters;

	private static final String DEFAULT_MSG_SELECT_ONLY = "Select the element:"; //$NON-NLS-1$

	// Sizing constants
	private static final int SIZING_SELECTION_PANE_WIDTH = 320;
	private static final int SIZING_SELECTION_PANE_HEIGHT = 300;

	/**
	 * @param parent
	 * @param contentProvider
	 * @param labelProvider
	 * @param listener
	 */
	public ElementTreeSelectionGroup(Composite parent, ITreeContentProvider contentProvider, ILabelProvider labelProvider) {
		this(parent, contentProvider, labelProvider, null);
	}

	public ElementTreeSelectionGroup(Composite parent, ITreeContentProvider contentProvider, ILabelProvider labelProvider, String message) {
		this(parent, contentProvider, labelProvider, message, SIZING_SELECTION_PANE_HEIGHT, SIZING_SELECTION_PANE_WIDTH);
	}

	public ElementTreeSelectionGroup(Composite parent, ITreeContentProvider contentProvider, ILabelProvider labelProvider, String message,
			int heightHint, int widthHint) {
		super(parent, SWT.NONE);
		this.contentProvider = contentProvider;
		this.labelProvider = labelProvider;
		if (message != null) {
			createContents(message, heightHint, widthHint);
		} else {
			createContents(DEFAULT_MSG_SELECT_ONLY, heightHint, widthHint);
		}
	}

	public void createContents(String message) {
		createContents(message, SIZING_SELECTION_PANE_HEIGHT, SIZING_SELECTION_PANE_WIDTH);
	}

	public void createContents(String message, int heightHint, int widthHint) {
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		setLayout(layout);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label = new Label(this, SWT.WRAP);
		label.setText(message);
		label.setFont(getFont());

		createTreeViewer(heightHint);
		Dialog.applyDialogFont(this);
	}

	protected void createTreeViewer(int heightHint) {
		// Create drill down.
		DrillDownComposite drillDown = new DrillDownComposite(this, SWT.BORDER);
		GridData spec = new GridData(SWT.FILL, SWT.FILL, true, true);
		spec.widthHint = SIZING_SELECTION_PANE_WIDTH;
		spec.heightHint = heightHint;
		drillDown.setLayoutData(spec);

		// Create tree viewer inside drill down.
		treeViewer = new TreeViewer(drillDown, SWT.NONE);
		drillDown.setChildTree(treeViewer);

		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setComparator(comparator);
		treeViewer.setUseHashlookup(true);
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					Object item = ((IStructuredSelection) selection).getFirstElement();
					if (item == null) {
						return;
					}
					if (treeViewer.getExpandedState(item)) {
						treeViewer.collapseToLevel(item, 1);
					} else {
						treeViewer.expandToLevel(item, 1);
					}
				}
			}
		});

		if (fFilters != null) {
			for (int i = 0; i != fFilters.size(); i++) {
				treeViewer.addFilter(fFilters.get(i));
			}
		}

		// This has to be done after the viewer has been laid out
		treeViewer.setInput(ResourcesPlugin.getWorkspace());
	}

	/**
	 * Gives focus to TreeViewer.
	 */
	public void setInitialFocus() {
		treeViewer.getTree().setFocus();
	}

	/**
	 * Sets the initial selection. Convenience method.
	 * 
	 * @param selection
	 *            the initial selection.
	 */
	public void setInitialSelection(Object selection) {
		setInitialSelections(new Object[] { selection });
	}

	/**
	 * Sets the initial selection in this selection dialog to the given elements.
	 * 
	 * @param selectedElements
	 *            the array of elements to select
	 */
	public void setInitialSelections(Object[] selectedElements) {
		initialSelections = new ArrayList<Object>(selectedElements.length);
		for (Object selectedElement : selectedElements) {
			initialSelections.add(selectedElement);
		}
	}

	/**
	 * Adds a filter to the tree viewer.
	 * 
	 * @param filter
	 *            a filter.
	 */
	public void addFilter(ViewerFilter filter) {
		if (fFilters == null) {
			fFilters = new ArrayList<ViewerFilter>(4);
		}

		fFilters.add(filter);
	}

	/**
	 * Sets the comparator used by the tree viewer.
	 * 
	 * @param comparator
	 */
	public void setComparator(ViewerComparator comparator) {
		this.comparator = comparator;
	}

	public ISelection getSelection() {
		return treeViewer.getSelection();
	}

	public TreeViewer getViewer() {
		return treeViewer;
	}
}

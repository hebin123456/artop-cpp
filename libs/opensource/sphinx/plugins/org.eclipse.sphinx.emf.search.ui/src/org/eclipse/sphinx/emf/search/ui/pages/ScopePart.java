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
package org.eclipse.sphinx.emf.search.ui.pages;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.internal.ui.SearchDialog;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.sphinx.emf.search.ui.internal.Activator;
import org.eclipse.sphinx.emf.search.ui.internal.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class ScopePart {

	// Settings store
	private static final String DIALOG_SETTINGS_KEY = "SearchDialog.ScopePart"; //$NON-NLS-1$
	private static final String STORE_SCOPE = "scope"; //$NON-NLS-1$

	private IDialogSettings fSettingsStore;
	private Group fPart;

	// Scope radio buttons
	private Button fUseWorkspace;
	private Button fUseSelection;
	private Button fUseProject;

	private int fScope;
	private boolean fCanSearchEnclosingProjects;

	// Reference to its search page container (can be null)
	private SearchDialog fSearchDialog;

	private boolean fActiveEditorCanProvideScopeSelection;

	/**
	 * Returns a new scope part with workspace as initial scope. The part is not yet created.
	 *
	 * @param searchDialog
	 *            The parent container
	 * @param searchEnclosingProjects
	 *            If true, add the 'search enclosing project' radio button
	 */
	public ScopePart(SearchDialog searchDialog, boolean searchEnclosingProjects) {
		fSearchDialog = searchDialog;
		fCanSearchEnclosingProjects = searchEnclosingProjects;

		fSettingsStore = Activator.getDefault().getDialogSettingsSection(DIALOG_SETTINGS_KEY);
		fScope = getStoredScope(fSettingsStore, searchEnclosingProjects);
	}

	private static int getStoredScope(IDialogSettings settingsStore, boolean canSearchEnclosingProjects) {
		int scope;
		try {
			scope = settingsStore.getInt(STORE_SCOPE);
		} catch (NumberFormatException ex) {
			scope = ISearchPageContainer.WORKSPACE_SCOPE;
		}
		if (scope != ISearchPageContainer.WORKING_SET_SCOPE && scope != ISearchPageContainer.SELECTION_SCOPE
				&& scope != ISearchPageContainer.SELECTED_PROJECTS_SCOPE && scope != ISearchPageContainer.WORKSPACE_SCOPE) {
			scope = ISearchPageContainer.WORKSPACE_SCOPE;
		}

		if (!canSearchEnclosingProjects && scope == ISearchPageContainer.SELECTED_PROJECTS_SCOPE) {
			scope = ISearchPageContainer.WORKSPACE_SCOPE;
		}

		return scope;
	}

	/**
	 * Returns the scope selected in this part
	 *
	 * @return the selected scope
	 */
	public int getSelectedScope() {
		return fScope;
	}

	/**
	 * Sets the selected scope. This method must only be called on a created part.
	 *
	 * @param scope
	 *            the scope to be selected in this part
	 */
	public void setSelectedScope(int scope) {
		Assert.isLegal(scope >= 0 && scope <= 3);
		Assert.isNotNull(fUseWorkspace);
		Assert.isNotNull(fUseSelection);
		Assert.isNotNull(fUseProject);

		fSettingsStore.put(STORE_SCOPE, scope);

		if (scope == ISearchPageContainer.SELECTED_PROJECTS_SCOPE) {
			if (!fCanSearchEnclosingProjects) {
				SearchPlugin.log(new Status(IStatus.WARNING, NewSearchUI.PLUGIN_ID, IStatus.WARNING,
						"Enclosing projects scope set on search page that does not support it", null)); //$NON-NLS-1$
				scope = ISearchPageContainer.WORKSPACE_SCOPE;
			} else if (!fUseProject.isEnabled()) {
				scope = ISearchPageContainer.WORKSPACE_SCOPE;
			}
		} else if (scope == ISearchPageContainer.SELECTION_SCOPE && !fUseSelection.isEnabled()) {
			scope = fUseProject.isEnabled() ? ISearchPageContainer.SELECTED_PROJECTS_SCOPE : ISearchPageContainer.WORKSPACE_SCOPE;
		}
		fScope = scope;

		fUseWorkspace.setSelection(scope == ISearchPageContainer.WORKSPACE_SCOPE);
		fUseSelection.setSelection(scope == ISearchPageContainer.SELECTION_SCOPE);
		fUseProject.setSelection(scope == ISearchPageContainer.SELECTED_PROJECTS_SCOPE);

		updateSearchPageContainerActionPerformedEnablement();

	}

	public void setActiveEditorCanProvideScopeSelection(boolean state) {
		fActiveEditorCanProvideScopeSelection = state;
		fUseSelection.setEnabled(canSearchInSelection());

		// Reinitialize the controls
		fScope = getStoredScope(fSettingsStore, fCanSearchEnclosingProjects);
		setSelectedScope(fScope);
	}

	private void updateSearchPageContainerActionPerformedEnablement() {
		fSearchDialog.notifyScopeSelectionChanged();
	}

	/**
	 * Creates this scope part.
	 *
	 * @param parent
	 *            a widget which will be the parent of the new instance (cannot be null)
	 * @return Returns the created part control
	 */
	public Composite createPart(Composite parent) {
		fPart = new Group(parent, SWT.NONE);
		fPart.setText(Messages.ScopePart_group_text);

		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		fPart.setLayout(layout);
		fPart.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fUseWorkspace = new Button(fPart, SWT.RADIO);
		fUseWorkspace.setData(new Integer(ISearchPageContainer.WORKSPACE_SCOPE));
		fUseWorkspace.setText(Messages.ScopePart_workspaceScope_text);

		fUseSelection = new Button(fPart, SWT.RADIO);
		fUseSelection.setData(new Integer(ISearchPageContainer.SELECTION_SCOPE));
		fUseSelection.setText(Messages.ScopePart_selectedResourcesScope_text);

		boolean canSearchInSelection = canSearchInSelection();
		fUseSelection.setEnabled(canSearchInSelection);

		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent = 8;
		fUseSelection.setLayoutData(gd);

		fUseProject = new Button(fPart, SWT.RADIO);
		fUseProject.setData(new Integer(ISearchPageContainer.SELECTED_PROJECTS_SCOPE));
		fUseProject.setText(Messages.ScopePart_enclosingModelsScope_text);
		fUseProject.setEnabled(fSearchDialog.getEnclosingProjectNames().length > 0);

		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		gd.horizontalIndent = 8;
		fUseProject.setLayoutData(gd);
		if (!fCanSearchEnclosingProjects) {
			fUseProject.setVisible(false);
		}

		// Add scope change listeners
		SelectionAdapter scopeChangedLister = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleScopeChanged(e);
			}
		};
		fUseWorkspace.addSelectionListener(scopeChangedLister);
		fUseSelection.addSelectionListener(scopeChangedLister);
		fUseProject.addSelectionListener(scopeChangedLister);

		// Set initial scope
		setSelectedScope(fScope);

		return fPart;
	}

	private boolean canSearchInSelection() {
		ISelection selection = fSearchDialog.getSelection();
		return selection instanceof IStructuredSelection && !selection.isEmpty() || fActiveEditorCanProvideScopeSelection
				&& fSearchDialog.getActiveEditorInput() != null;

	}

	private void handleScopeChanged(SelectionEvent e) {
		Object source = e.getSource();
		if (source instanceof Button) {
			Button button = (Button) source;
			if (button.getSelection()) {
				setSelectedScope(((Integer) button.getData()).intValue());
			}
		}
	}

	void setVisible(boolean state) {
		if (state) {
			fPart.layout();
		}
		fPart.setVisible(state);
	}
}

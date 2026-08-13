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
 *     itemis - [473260] Progress indication of check framework
 *     itemis - [473261] Check Validation: Cancel button unresponsive
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.sphinx.emf.check.ui.internal.Activator;
import org.eclipse.sphinx.emf.check.ui.internal.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;

public class CheckValidationOptionsSelectionDialog extends ListSelectionDialog {

	private static final String SECTION_CATEGORY_SELECTION_DIALOG = "CheckValidationOptionsSelectionDialog"; //$NON-NLS-1$
	private static final String ENABLE_INTRINSIC_MODEL_INTEGRITY_CONSTRAINTS = "EnableIntrinsicModelIntegrityConstraints"; //$NON-NLS-1$

	public CheckValidationOptionsSelectionDialog(Shell parentShell, Object input, IStructuredContentProvider contentProvider,
			ILabelProvider labelProvider, String message) {
		super(parentShell, input, contentProvider, labelProvider, message);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);

		Button button = new Button((Composite) control, SWT.CHECK);
		button.setText(Messages.text_enableIntrinsicModelIntegrityConstraints);
		button.setSelection(getSelectionFromSettings());
		button.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Button source = (Button) e.getSource();
				setSelectionToSettings(source.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		// Sort categories alphabetically
		getViewer().setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return super.compare(viewer, e1, e2);
			}
		});
		return control;
	}

	public boolean enableIntrinsicModelIntegrityConstraints() {
		return getSelectionFromSettings();
	}

	protected Boolean getSelectionFromSettings() {
		IDialogSettings section = getDialogSettings().getSection(SECTION_CATEGORY_SELECTION_DIALOG);
		if (section != null) {
			return section.getBoolean(ENABLE_INTRINSIC_MODEL_INTEGRITY_CONSTRAINTS);
		}
		return false;
	}

	protected void setSelectionToSettings(boolean selected) {
		IDialogSettings section = getDialogSettings().getSection(SECTION_CATEGORY_SELECTION_DIALOG);
		if (section == null) {
			section = getDialogSettings().addNewSection(SECTION_CATEGORY_SELECTION_DIALOG);
		}
		section.put(ENABLE_INTRINSIC_MODEL_INTEGRITY_CONSTRAINTS, selected);
	}

	protected IDialogSettings getDialogSettings() {
		return Activator.getDefault().getDialogSettings();
	}
}

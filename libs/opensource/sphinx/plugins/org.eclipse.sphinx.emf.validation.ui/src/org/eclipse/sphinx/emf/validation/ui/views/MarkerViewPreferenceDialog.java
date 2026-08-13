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

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.preferences.ViewSettingsDialog;

/**
 * The MarkerViewPreferenceDialog is the dialog that is used for preference settings in a markers view.
 * 
 * @since 0.7.0
 */
public class MarkerViewPreferenceDialog extends ViewSettingsDialog {

	String enablementKey;

	String limitKey;

	String dialogTitle;

	private IntegerFieldEditor limitEditor;

	private Button enablementButton;

	private Composite editArea;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parentShell
	 * @param enablementPreference
	 *            The key for the enablement preference.
	 * @param limitPreference
	 *            The key for the limit preference.
	 * @param title
	 *            The title for the dialog.
	 */
	public MarkerViewPreferenceDialog(Shell parentShell, String enablementPreference, String limitPreference, String title) {
		super(parentShell);
		enablementKey = enablementPreference;
		limitKey = limitPreference;
		dialogTitle = title;

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(dialogTitle);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite topComposite = (Composite) super.createDialogArea(parent);

		boolean checked = IDEWorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(enablementKey);
		enablementButton = new Button(topComposite, SWT.CHECK);
		enablementButton.setText(MarkerMessages.MarkerPreferences_MarkerLimits);
		enablementButton.setSelection(checked);

		editArea = new Composite(topComposite, SWT.NONE);
		editArea.setLayout(new GridLayout());
		GridData editData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		editData.horizontalIndent = 10;
		editArea.setLayoutData(editData);

		limitEditor = new IntegerFieldEditor("limit", MarkerMessages.MarkerPreferences_VisibleItems, editArea);//$NON-NLS-1$
		limitEditor.setPreferenceStore(IDEWorkbenchPlugin.getDefault().getPreferenceStore());
		limitEditor.setPreferenceName(limitKey);
		limitEditor.load();

		GridData checkedData = new GridData(SWT.FILL, SWT.NONE, true, false);
		checkedData.horizontalSpan = limitEditor.getNumberOfControls();
		enablementButton.setLayoutData(checkedData);

		enablementButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setLimitEditorEnablement(editArea, enablementButton.getSelection());
			}
		});

		setLimitEditorEnablement(editArea, checked);

		return topComposite;
	}

	/**
	 * Enable the limitEditor based on checked.
	 * 
	 * @param control
	 *            The parent of the editor
	 * @param checked
	 */
	private void setLimitEditorEnablement(Composite control, boolean checked) {
		limitEditor.setEnabled(checked, control);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		limitEditor.store();
		IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(enablementKey, enablementButton.getSelection());
		IDEWorkbenchPlugin.getDefault().savePluginPreferences();
		super.okPressed();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.preferences.ViewSettingsDialog#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		super.performDefaults();
		limitEditor.loadDefault();
		boolean checked = IDEWorkbenchPlugin.getDefault().getPreferenceStore().getDefaultBoolean(enablementKey);
		enablementButton.setSelection(checked);
		setLimitEditorEnablement(editArea, checked);
	}

}

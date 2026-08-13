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
package org.eclipse.sphinx.emf.validation.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.sphinx.emf.validation.preferences.IValidationPreferences;
import org.eclipse.sphinx.emf.validation.ui.SphinxValidationUiActivator;
import org.eclipse.sphinx.emf.validation.ui.util.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ValidationPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor fAutomaticValidationField;
	private IntegerFieldEditor fMaxNumberOfErrorsField;
	private BooleanFieldEditor fEmfRulesField;

	public ValidationPreferencesPage() {
		super(GRID);
		setPreferenceStore(SphinxValidationUiActivator.getDefault().getPreferenceStore());
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected void createFieldEditors() {
		addAutomaticValidationGroup();
		addEmfRulesGroup();
		addProblemIndicationGroup();
	}

	protected void addAutomaticValidationGroup() {
		// Create a Group to hold the version field
		Group automaticValidationGroup = new Group(getFieldEditorParent(), SWT.NONE);
		automaticValidationGroup.setText(Messages._UI_automaticValidation_groupText);

		GridLayout gridLayout = new GridLayout(2, false);
		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		gridData.horizontalSpan = 2;

		fAutomaticValidationField = new BooleanFieldEditor(IValidationPreferences.PREF_ENABLE_AUTOMATIC_VALIDATION,
				Messages._UI_enableDisableAutomaticValidationPreferencesMsg, BooleanFieldEditor.DEFAULT, automaticValidationGroup);
		addField(fAutomaticValidationField);

		automaticValidationGroup.setLayoutData(gridData);
		automaticValidationGroup.setLayout(gridLayout);
	}

	protected void addEmfRulesGroup() {
		Group emfRulesGroup = new Group(getFieldEditorParent(), SWT.NONE);
		emfRulesGroup.setText(Messages._UI_EMFConstraintsGroupText);

		GridLayout gridLayout = new GridLayout(2, false);
		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		gridData.horizontalSpan = 2;

		fEmfRulesField = new BooleanFieldEditor(IValidationPreferences.PREF_ENABLE_EMF_DEFAULT_RULES,
				Messages._UI_EMFConstraintsEnabledPreferencesMsg, BooleanFieldEditor.DEFAULT, emfRulesGroup);
		addField(fEmfRulesField);

		emfRulesGroup.setLayoutData(gridData);
		emfRulesGroup.setLayout(gridLayout);
	}

	protected void addProblemIndicationGroup() {
		Group problemIndicationGroup = new Group(getFieldEditorParent(), SWT.NONE);
		problemIndicationGroup.setText(Messages._UI_ProblemIndicationGroupText);

		GridLayout gridLayout = new GridLayout(2, false);
		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		gridData.horizontalSpan = 2;

		fMaxNumberOfErrorsField = new IntegerFieldEditor(IValidationPreferences.PREF_MAX_NUMBER_OF_ERRORS,
				Messages._UI_ProblemIndicationFieldLabelText, problemIndicationGroup);
		addField(fMaxNumberOfErrorsField);

		problemIndicationGroup.setLayoutData(gridData);
		problemIndicationGroup.setLayout(gridLayout);
	}
}

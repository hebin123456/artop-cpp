/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys and others.
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
package org.eclipse.sphinx.platform.ui.preferences;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.sphinx.platform.ui.dialogs.ProjectSelectionDialog;
import org.eclipse.sphinx.platform.ui.fields.IField;
import org.eclipse.sphinx.platform.ui.fields.IFieldListener;
import org.eclipse.sphinx.platform.ui.fields.SelectionButtonField;
import org.eclipse.sphinx.platform.ui.internal.util.LayoutUtil;
import org.eclipse.sphinx.platform.ui.preferences.messages.AbstractPreferenceAndPropertyMessages;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PreferencesUtil;

public abstract class AbstractPreferenceAndPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, IWorkbenchPropertyPage {

	/**
	 * Project nature that is required by the preference or property page.
	 */
	protected String requiredProjectNatureId;

	/**
	 * The parent composite of this preference or property page.
	 */
	private Composite parentComposite;

	/**
	 * The workspace settings link.
	 */
	private Link configureOtherSettingsLink;

	/**
	 * The project settings button.
	 */
	private SelectionButtonField enableProjectSpecificSettingsField;

	/**
	 * The project or null when used as preference page.
	 */
	private IProject project;

	/**
	 * The Map that contain the page data.
	 */
	private Map<String, Object> data;

	public static final String DATA_NO_LINK = "AbstractPreferenceAndPropertyPage.nolink"; //$NON-NLS-1$

	public AbstractPreferenceAndPropertyPage(String requiredProjectNatureId) {
		this(requiredProjectNatureId, FLAT);
	}

	public AbstractPreferenceAndPropertyPage(String requiredProjectNatureId, int style) {
		super(style);

		this.requiredProjectNatureId = requiredProjectNatureId;
		project = null;
		data = null;
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	public IAdaptable getElement() {
		return project;
	}

	@Override
	public void setElement(IAdaptable element) {
		project = (IProject) element.getAdapter(IResource.class);
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(composite);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = 0;
		layout.verticalSpacing = convertVerticalDLUsToPixels(10);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addFields(composite);
	}

	/**
	 * Add fields to the given parent.
	 */
	protected abstract void addFields(Composite parent);

	/**
	 * Enable/Disable the fields to edit for a project.
	 */
	protected abstract void enablePreferenceContent(boolean useProjectSpecificSettings);

	protected String getPreferencePageID() {
		return null;
	}

	protected String getPropertyPageID() {
		return null;
	}

	@Override
	protected Label createDescriptionLabel(Composite parent) {
		parentComposite = parent;

		if (isProjectPreferencePage()) {
			if (supportsWorkspaceSpecificOptions()) {
				Composite composite = new Composite(parent, SWT.NONE);
				composite.setFont(parent.getFont());
				GridLayout layout = new GridLayout();
				layout.marginHeight = 0;
				layout.marginWidth = 0;
				layout.numColumns = 2;
				composite.setLayout(layout);
				composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

				IFieldListener listener = new IFieldListener() {
					@Override
					public void dialogFieldChanged(IField field) {
						boolean enabled = ((SelectionButtonField) field).isSelected();
						enableProjectSpecificSettings(enabled);

						if (enabled && getData() != null) {
							applyData(getData());
						}
					}
				};

				enableProjectSpecificSettingsField = new SelectionButtonField(SWT.CHECK);
				enableProjectSpecificSettingsField.addFieldListener(listener);
				enableProjectSpecificSettingsField
						.setLabelText(AbstractPreferenceAndPropertyMessages.AbstractPreferenceAndPropertyPage_enableProjectSpecificSettings);
				enableProjectSpecificSettingsField.fillIntoGrid(composite, 1);
				enableProjectSpecificSettingsField.setSelectionWithoutEvent(true);
				LayoutUtil.setHorizontalGrabbing(enableProjectSpecificSettingsField.getSelectionButton(null));

				if (offerLink()) {
					// Access the workspace settings
					configureOtherSettingsLink = createLink(composite,
							AbstractPreferenceAndPropertyMessages.AbstractPreferenceAndPropertyPage_configureWorkspaceSettings);
					configureOtherSettingsLink.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
					// Enable changeWorkspaceSettings field only if useProjectSettings is not selected
					if (enableProjectSpecificSettingsField != null) {
						enableProjectSpecificSettings(enableProjectSpecificSettingsField.isSelected());
					}
				} else {
					LayoutUtil.setHorizontalSpan(enableProjectSpecificSettingsField.getSelectionButton(null), 2);
				}

				Label horizontalLine = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
				horizontalLine.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
				horizontalLine.setFont(composite.getFont());
			}
		} else if (supportsProjectSpecificOptions() && offerLink()) {
			// Access the project specific settings
			configureOtherSettingsLink = createLink(parent,
					AbstractPreferenceAndPropertyMessages.AbstractPreferenceAndPropertyPage_configureProjectSpecificSettings);
			configureOtherSettingsLink.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		}

		return super.createDescriptionLabel(parent);
	}

	protected boolean supportsWorkspaceSpecificOptions() {
		return getPreferencePageID() != null;
	}

	protected boolean supportsProjectSpecificOptions() {
		return getPropertyPageID() != null;
	}

	protected boolean isProjectPreferencePage() {
		return project != null;
	}

	protected final void openWorkspacePreferences(Object data) {
		String id = getPreferencePageID();
		PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, data).open();
	}

	protected final void openProjectProperties(IProject project, Object data) {
		String id = getPropertyPageID();
		if (id != null) {
			PreferencesUtil.createPropertyDialogOn(getShell(), project, id, new String[] { id }, data).open();
		}
	}

	protected void enableProjectSpecificSettings(boolean useProjectSpecificSettings) {
		enableProjectSpecificSettingsField.setSelection(useProjectSpecificSettings);
		enablePreferenceContent(useProjectSpecificSettings);
		updateLinkVisibility();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void applyData(Object data) {
		if (data instanceof Map<?, ?>) {
			this.data = (Map<String, Object>) data;
		}
		if (configureOtherSettingsLink != null) {
			if (!offerLink()) {
				configureOtherSettingsLink.dispose();
				parentComposite.layout(true, true);
			}
		}
	}

	protected Map<String, Object> getData() {
		return data;
	}

	protected boolean useProjectSettings() {
		return isProjectPreferencePage() && enableProjectSpecificSettingsField != null && enableProjectSpecificSettingsField.isSelected();
	}

	protected boolean offerLink() {
		return data == null || !Boolean.TRUE.equals(data.get(DATA_NO_LINK));
	}

	private final void doLinkActivated(Link link) {
		Map<String, Object> data = getData();
		if (data == null) {
			data = new HashMap<String, Object>();
		}
		data.put(DATA_NO_LINK, Boolean.TRUE);

		if (isProjectPreferencePage()) {
			openWorkspacePreferences(data);
		} else {
			Set<IProject> projectsWithSpecifics = new HashSet<IProject>();
			Collection<IProject> projects = ExtendedPlatform.getProjects(requiredProjectNatureId);
			for (IProject project : projects) {
				projectsWithSpecifics.add(project);
			}
			ProjectSelectionDialog dialog = new ProjectSelectionDialog(getShell(), projectsWithSpecifics);
			if (dialog.open() == Window.OK) {
				IProject project = (IProject) dialog.getFirstResult();
				openProjectProperties(project, data);
			}
		}
	}

	private Link createLink(Composite composite, String text) {
		Link link = new Link(composite, SWT.NONE);
		link.setFont(composite.getFont());
		link.setText("<A>" + text + "</A>"); //$NON-NLS-1$//$NON-NLS-2$
		link.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doLinkActivated((Link) e.widget);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				doLinkActivated((Link) e.widget);
			}
		});
		return link;
	}

	private void updateLinkVisibility() {
		if (configureOtherSettingsLink == null || configureOtherSettingsLink.isDisposed()) {
			return;
		}

		if (isProjectPreferencePage()) {
			configureOtherSettingsLink.setEnabled(!useProjectSettings());
		}
	}
}

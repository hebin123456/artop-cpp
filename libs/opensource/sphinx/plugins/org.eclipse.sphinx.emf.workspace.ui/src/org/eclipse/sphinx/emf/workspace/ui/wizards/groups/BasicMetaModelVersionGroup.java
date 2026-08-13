/**
 * <copyright>
 *
 * Copyright (c) 2013 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [405059] Enable BasicMetaModelVersionGroup to open appropriate model version preference page
 *     itemis - [405075] Improve type safety of NewModelProjectCreationPage and BasicMetaModelVersionGroup wrt base metamodel descriptor and metamodel version preference
 *     itemis - [406053] Separate class construction of BasicMetaModelVersionGroup and creation of its group content in different methods
 *     itemis - [406194] Enable title and descriptions of model project and file creation wizards to be calculated automatically
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.wizards.groups;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.workspace.ui.internal.Activator;
import org.eclipse.sphinx.emf.workspace.ui.internal.messages.Messages;
import org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference;
import org.eclipse.sphinx.platform.ui.fields.ComboField;
import org.eclipse.sphinx.platform.ui.fields.IField;
import org.eclipse.sphinx.platform.ui.fields.IFieldListener;
import org.eclipse.sphinx.platform.ui.fields.SelectionButtonField;
import org.eclipse.sphinx.platform.ui.groups.AbstractGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * A basic metamodel version group which is composed of buttons for the workspace default metamodel version and
 * alternate metamodel version, configureWorkspaceSettingsLink, and a combo for different metamodel versions.
 * <p>
 * This class is useful for the creation of a new model project wizard page. It can be overridden by clients.
 */
public class BasicMetaModelVersionGroup<T extends IMetaModelDescriptor> extends AbstractGroup {

	protected static final String LAST_SELECTED_METAMODEL_VERSION_OPTION = Activator.getPlugin().getSymbolicName()
			+ "last.selected.metamodel.version"; //$NON-NLS-1$
	protected static final String LAST_SELECTED_METAMODEL_VERSION_KEY = Activator.getPlugin().getSymbolicName()
			+ "last.selected.project.metamodel.version"; //$NON-NLS-1$

	protected static final int WORKSPACE_DEFAULT_METAMODEL_VERSION = 0;
	protected static final int ALTERNATE_METAMODEL_VERSION = 1;

	protected T baseMetaModelDescriptor;
	protected IProjectWorkspacePreference<T> metaModelVersionPreference;
	protected String metaModelVersionPreferencePageId;

	protected SelectionButtonField workspaceDefaultMetaModelVersionButton, alternateMetaModelVersionButton;
	protected ComboField metaModelVersionCombo;
	protected Link configureWorkspaceSettingsLink;

	protected List<T> supportedMetaModelVersions = new ArrayList<T>();

	private String metaModelVersionLabel = null;

	private IFieldListener fieldListener = new IFieldListener() {
		/*
		 * @see
		 * org.eclipse.sphinx.platform.ui.fields.IFieldListener#dialogFieldChanged(org.eclipse.sphinx.platform.ui.fields
		 * .IField)
		 */
		@Override
		public void dialogFieldChanged(IField field) {
			updateEnableState();
			if (field == workspaceDefaultMetaModelVersionButton) {
				Activator.getPlugin().getDialogSettings().put(LAST_SELECTED_METAMODEL_VERSION_OPTION, WORKSPACE_DEFAULT_METAMODEL_VERSION);
			} else if (field == alternateMetaModelVersionButton) {
				Activator.getPlugin().getDialogSettings().put(LAST_SELECTED_METAMODEL_VERSION_OPTION, ALTERNATE_METAMODEL_VERSION);
			} else if (field == metaModelVersionCombo) {
				if (alternateMetaModelVersionButton.isSelected()) {
					storeSelectionState((ComboField) field);
				}
			}
		}
	};

	private SelectionListener selectionListener = new SelectionListener() {
		/*
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			PreferencesUtil.createPreferenceDialogOn(null, metaModelVersionPreferencePageId, new String[] { metaModelVersionPreferencePageId }, null)
					.open();
		}

		/*
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent event) {
			widgetDefaultSelected(event);
		}
	};

	/**
	 * Creates a new instance of the metamodel version group. The buttons for the workspace default metamodel version
	 * and alternate metamodel version, configureWorkspaceSettingsLink, and a combo for different metamodel versions are
	 * added in this group.
	 * 
	 * @param groupName
	 *            the name of the group
	 * @param baseMetaModelDescriptor
	 *            the base {@linkplain IMetaModelDescriptor metamodel} of the model project to be created, must not be
	 *            <code>null</code>
	 * @param metaModelVersionPreference
	 *            the meta-model version that the model project will be used for, must not be <code>null</code>
	 * @param metaModelVersionPreferencePageId
	 *            the id of the metamodel version preference page
	 */
	public BasicMetaModelVersionGroup(String groupName, T baseMetaModelDescriptor, IProjectWorkspacePreference<T> metaModelVersionPreference,
			String metaModelVersionPreferencePageId) {
		super(groupName);
		Assert.isNotNull(baseMetaModelDescriptor);
		Assert.isLegal(baseMetaModelDescriptor != MetaModelDescriptorRegistry.ANY_MM);
		Assert.isLegal(baseMetaModelDescriptor != MetaModelDescriptorRegistry.NO_MM);
		Assert.isNotNull(metaModelVersionPreference);

		this.baseMetaModelDescriptor = baseMetaModelDescriptor;
		this.metaModelVersionPreference = metaModelVersionPreference;
		this.metaModelVersionPreferencePageId = metaModelVersionPreferencePageId;
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.groups.AbstractGroup#doCreateContent(org.eclipse.swt.widgets.Composite, int)
	 */
	@Override
	protected void doCreateContent(Composite group, int numColumns) {

		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gridLayout = new GridLayout(3, false);
		group.setLayout(gridLayout);
		((Group) group).setText(getMetaModelVersionGroupLabel());

		// add a selection button widget for the workspace default metamodel version
		workspaceDefaultMetaModelVersionButton = new SelectionButtonField(SWT.RADIO);
		workspaceDefaultMetaModelVersionButton.setLabelText(NLS.bind(Messages.button_workspaceDefaultMetaModelVersion_label,
				getWorkspaceDefaultMetaModelVersionLabelPrefix(), metaModelVersionPreference.getFromWorkspace().getName()));
		workspaceDefaultMetaModelVersionButton.fillIntoGrid(group, 2);
		workspaceDefaultMetaModelVersionButton.addFieldListener(fieldListener);

		// add a link widget for the workspace settings link
		if (metaModelVersionPreferencePageId != null) {
			configureWorkspaceSettingsLink = new Link(group, SWT.NONE);
			configureWorkspaceSettingsLink.setFont(group.getFont());
			configureWorkspaceSettingsLink.setText(MessageFormat.format("<a>{0}</a>", Messages.link_configureWorkspaceSettings_label)); //$NON-NLS-1$
			configureWorkspaceSettingsLink.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
			configureWorkspaceSettingsLink.addSelectionListener(selectionListener);
		}

		// add a selection button widget for the alternate metamodel version
		alternateMetaModelVersionButton = new SelectionButtonField(SWT.RADIO);
		alternateMetaModelVersionButton.setLabelText(getAlternateMetaModelVersionLabel());
		alternateMetaModelVersionButton.fillIntoGrid(group, 1);
		alternateMetaModelVersionButton.addFieldListener(fieldListener);

		// add a combo widget for the metamodel versions
		metaModelVersionCombo = new ComboField(SWT.READ_ONLY);
		metaModelVersionCombo.fillIntoGrid(group, 2);
		metaModelVersionCombo.addFieldListener(fieldListener);

		// set the supported metamodel versions in the combo field as the available items, and set the given
		// metaModelDescriptor as the selected item
		fillMetaModelVersionCombo(baseMetaModelDescriptor);

		Combo comboControl = (Combo) metaModelVersionCombo.getComboControl();
		comboControl.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		comboControl.setVisibleItemCount(10);

		switch (getLastSelectedMetaModelVersionOption()) {
		case WORKSPACE_DEFAULT_METAMODEL_VERSION:
			workspaceDefaultMetaModelVersionButton.setSelection(true);
			break;
		case ALTERNATE_METAMODEL_VERSION:
			alternateMetaModelVersionButton.setSelection(true);
			break;
		default:
			workspaceDefaultMetaModelVersionButton.setSelection(true);
			break;
		}

		updateEnableState();
	}

	/**
	 * Gets the label of this metamodel version, "version" by default.
	 */
	protected String getMetaModelVersionLabel() {
		if (metaModelVersionLabel == null) {
			metaModelVersionLabel = Messages.default_metaModelVersionLabel;
		}
		return metaModelVersionLabel;
	}

	/**
	 * Sets the label of this metamodel version. This method can be overridden by AUTOSAR, to set the label to "release"
	 */
	public void setMetaModelVersionLabel(String metaModelVersionLabel) {
		this.metaModelVersionLabel = metaModelVersionLabel;
	}

	/**
	 * Returns the label of this metamodel version group.
	 * <p>
	 * For example, Hummingbird version options, AUTOSAR release options, etc.
	 */
	protected String getMetaModelVersionGroupLabel() {
		return NLS.bind(Messages.group_metaModelVersion_label, baseMetaModelDescriptor.getName(), getMetaModelVersionLabel());
	}

	/**
	 * Returns the label for the alternate metamodel version.
	 * <p>
	 * For example, "Alternate version", "Alternate release", etc.
	 */
	protected String getAlternateMetaModelVersionLabel() {
		return NLS.bind(Messages.button_alternateMetaModelVersion_label, getMetaModelVersionLabel());
	}

	/**
	 * Returns the prefix of the label for the workspace default metamodel version.
	 * <p>
	 * For example, "Workspace default version", "Workspace default release", etc.
	 */
	protected String getWorkspaceDefaultMetaModelVersionLabelPrefix() {
		return NLS.bind(Messages.button_workspaceDefaultMetaModelVersion_labelPrefix, getMetaModelVersionLabel());
	}

	/**
	 * Sets the combo items using all the metamodel version descriptors of given {@link T metaModelDescriptor}. Sets the
	 * last selected metamodel version descriptor as the selected item of the combo if it exists, otherwise uses the
	 * default metamodel version descriptor from the workspace.
	 */
	protected void fillMetaModelVersionCombo(T metaModelDescriptor) {
		supportedMetaModelVersions = MetaModelDescriptorRegistry.INSTANCE.getDescriptors(metaModelDescriptor, true);
		if (!supportedMetaModelVersions.isEmpty()) {
			String[] items = new String[supportedMetaModelVersions.size()];
			for (int index = 0; index < supportedMetaModelVersions.size(); index++) {
				T descriptor = supportedMetaModelVersions.get(index);
				items[index] = NLS.bind(Messages.combo_metaModelVersion_item, descriptor.getName(), descriptor.getNamespace());
			}
			metaModelVersionCombo.setItems(items);
		}

		String selectedMetaModelVersionId = getLastSelectedMetaModelVersionIdentifier();
		IMetaModelDescriptor descriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(selectedMetaModelVersionId);
		if (supportedMetaModelVersions.contains(descriptor)) {
			metaModelVersionCombo.selectItem(supportedMetaModelVersions.indexOf(descriptor));
		} else {
			T workspaceMetaModelVersion = metaModelVersionPreference.getFromWorkspace();
			if (supportedMetaModelVersions.contains(workspaceMetaModelVersion)) {
				metaModelVersionCombo.selectItem(supportedMetaModelVersions.indexOf(workspaceMetaModelVersion));
			}
		}
	}

	protected void updateEnableState() {
		metaModelVersionCombo.setEnabled(alternateMetaModelVersionButton.isSelected());
		configureWorkspaceSettingsLink.setEnabled(workspaceDefaultMetaModelVersionButton.isSelected());
	}

	/**
	 * Gets the metamodel version descriptor depending on the selection.
	 * <p>
	 * If the workspaceDefaultMetaModelVersionButton is selected, returns the default metamodel version descriptor from
	 * the workspace. If the alternateMetaModelVersionButton is selected, returns the item selected in the combo field.
	 * 
	 * @return the metamodel version descriptor selected from the Combo field if the alternateMetaModelVersionButton
	 *         button is selected, otherwise return the default metamodel version descriptor
	 */
	public T getMetaModelVersionDescriptor() {
		if (workspaceDefaultMetaModelVersionButton.isSelected()) {
			return metaModelVersionPreference.getFromWorkspace();
		} else if (alternateMetaModelVersionButton.isSelected()) {
			int index = metaModelVersionCombo.getSelectionIndex();
			if (index > -1) {
				T descriptor = supportedMetaModelVersions.get(index);
				return descriptor;
			}
		}
		return null;
	}

	/**
	 * Gets the last selected metamodel versions from DialogSettings.
	 */
	protected int getLastSelectedMetaModelVersionOption() {
		IDialogSettings dialogSettings = Activator.getPlugin().getDialogSettings();
		if (dialogSettings.get(LAST_SELECTED_METAMODEL_VERSION_OPTION) == null) {
			return WORKSPACE_DEFAULT_METAMODEL_VERSION;
		}
		return dialogSettings.getInt(LAST_SELECTED_METAMODEL_VERSION_OPTION);
	}

	/**
	 * Gets the last selected metamodel version identifier from DialogSettings.
	 */
	protected String getLastSelectedMetaModelVersionIdentifier() {
		IDialogSettings dialogSettings = Activator.getPlugin().getDialogSettings();
		return dialogSettings.get(LAST_SELECTED_METAMODEL_VERSION_KEY);
	}

	/**
	 * Stores the comboField selected item in DialogSettings.
	 * 
	 * @param comboField
	 *            the {@link ComboField combo field} for the metamodel version resource
	 */
	protected void storeSelectionState(ComboField comboField) {
		int index = comboField.getSelectionIndex();
		if (index > -1) {
			T descriptor = supportedMetaModelVersions.get(index);
			if (descriptor != null) {
				String item = descriptor.getIdentifier();
				Activator.getPlugin().getDialogSettings().put(LAST_SELECTED_METAMODEL_VERSION_KEY, item);
			}
		}
	}
}

/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [358706] Default output path never initialized when opening M2TConfigurationWizard
 * 
 * </copyright>
 */
package org.eclipse.sphinx.xtendxpand.ui.groups;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.platform.ui.fields.IField;
import org.eclipse.sphinx.platform.ui.fields.IFieldListener;
import org.eclipse.sphinx.platform.ui.fields.SelectionButtonField;
import org.eclipse.sphinx.platform.ui.fields.StringButtonField;
import org.eclipse.sphinx.platform.ui.fields.adapters.IButtonAdapter;
import org.eclipse.sphinx.platform.ui.groups.AbstractGroup;
import org.eclipse.sphinx.platform.ui.preferences.AbstractPreferenceAndPropertyPage;
import org.eclipse.sphinx.platform.ui.preferences.IPropertyPageIdProvider;
import org.eclipse.sphinx.xtendxpand.outlet.ExtendedOutlet;
import org.eclipse.sphinx.xtendxpand.preferences.OutletsPreference;
import org.eclipse.sphinx.xtendxpand.ui.internal.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class OutputGroup extends AbstractGroup {

	/**
	 * The use default output path button field.
	 */
	protected SelectionButtonField useDefaultPathButtonField;

	/**
	 * The output path to be use.
	 */
	protected StringButtonField outputPathField;

	/**
	 * The default outlet to be use.
	 */
	protected ExtendedOutlet defaultOutlet;

	/**
	 * The outlets preference to be use.
	 */
	protected OutletsPreference outletsPreference;

	/**
	 * The selected model object.
	 */
	protected EObject modelObject;

	public OutputGroup(String groupName, EObject modelObject, OutletsPreference outletsPreference, ExtendedOutlet defaultOutlet) {
		this(groupName, modelObject, outletsPreference, defaultOutlet, null);
	}

	public OutputGroup(String groupName, EObject modelObject, OutletsPreference outletsPreference, ExtendedOutlet defaultOutlet,
			IDialogSettings dialogSettings) {
		super(groupName, dialogSettings);

		this.groupName = groupName;
		this.modelObject = modelObject;
		this.outletsPreference = outletsPreference;
		this.defaultOutlet = defaultOutlet;
	}

	@Override
	protected void doCreateContent(final Composite parent, int numColumns) {
		parent.setLayout(new GridLayout(numColumns, false));

		// if outlets preference is not null then create a link toward this preference page.
		if (outletsPreference != null) {
			Link link = createLink(parent, Messages.label_configureProjectSpecificSettings);
			link.setLayoutData(new GridData(GridData.END, GridData.END, true, true));
		} else {
			useDefaultPathButtonField = new SelectionButtonField(SWT.CHECK);
			useDefaultPathButtonField.setLabelText(Messages.label_useDefaultPath);
			useDefaultPathButtonField.fillIntoGrid(parent, numColumns);
			useDefaultPathButtonField.setSelection(true);

			useDefaultPathButtonField.addFieldListener(new IFieldListener() {

				@Override
				public void dialogFieldChanged(IField field) {
					updateEnableState(!useDefaultPathButtonField.isSelected());
				}

			});

			outputPathField = new StringButtonField(new IButtonAdapter() {

				@Override
				public void changeControlPressed(IField field) {
					ContainerSelectionDialog dialog = new ContainerSelectionDialog(parent.getShell(), ResourcesPlugin.getWorkspace().getRoot(), true,
							""); //$NON-NLS-1$
					if (dialog.open() == Window.OK) {
						Object[] result = dialog.getResult();
						if (result.length == 0) {
							return;
						}
						IPath path = (IPath) result[0];
						outputPathField.setText(path.makeRelative().toString());
					}
				}
			});
			outputPathField.setLabelText(Messages.label_path);
			outputPathField.setButtonLabel(Messages.label_browse);
			if (defaultOutlet != null) {
				IContainer defaultOutletContainer = defaultOutlet.getContainer();
				if (defaultOutletContainer != null) {
					outputPathField.setText(defaultOutletContainer.getFullPath().makeRelative().toString());
				}
			}
			outputPathField.fillIntoGrid(parent, numColumns);
			outputPathField.addFieldListener(new IFieldListener() {

				@Override
				public void dialogFieldChanged(IField field) {
					notifyGroupChanged(outputPathField);
				}
			});
			updateEnableState(!useDefaultPathButtonField.isSelected());
		}
	}

	protected Link createLink(final Composite composite, String text) {
		Link link = new Link(composite, SWT.NONE);
		link.setFont(composite.getFont());
		link.setText("<A>" + text + "</A>"); //$NON-NLS-1$//$NON-NLS-2$
		link.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doLinkActivated(composite.getShell(), (Link) e.widget);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				doLinkActivated(composite.getShell(), (Link) e.widget);
			}
		});
		return link;
	}

	protected void doLinkActivated(Shell shell, Link link) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(AbstractPreferenceAndPropertyPage.DATA_NO_LINK, Boolean.TRUE);
		IFile file = EcorePlatformUtil.getFile(modelObject);
		if (file != null) {
			openProjectProperties(shell, file.getProject(), data);
		}
	}

	protected void openProjectProperties(Shell shell, IProject project, Object data) {
		if (outletsPreference != null) {
			IPropertyPageIdProvider provider = (IPropertyPageIdProvider) Platform.getAdapterManager().loadAdapter(outletsPreference,
					IPropertyPageIdProvider.class.getName());
			if (provider != null) {
				String outletsPropertyPageId = provider.getPropertyPageId();
				if (outletsPropertyPageId != null) {
					PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(shell, project, outletsPropertyPageId,
							new String[] { outletsPropertyPageId }, data);
					dialog.open();
				}
			}
		}
	}

	protected void updateEnableState(boolean enabled) {
		outputPathField.setEnabled(enabled);
		if (!enabled) {
			if (defaultOutlet != null) {
				IContainer defaultOutletContainer = defaultOutlet.getContainer();
				if (defaultOutletContainer != null) {
					outputPathField.setText(defaultOutletContainer.getFullPath().makeRelative().toString());
				}
			} else {
				outputPathField.setText(""); //$NON-NLS-1$
			}
		}
	}

	public SelectionButtonField getUseDefaultPathButtonField() {
		return useDefaultPathButtonField;
	}

	public StringButtonField getOutputPathField() {
		return outputPathField;
	}

	public Collection<ExtendedOutlet> getOutlets() {
		if (outletsPreference != null) {
			IFile file = EcorePlatformUtil.getFile(modelObject);
			if (file != null && file.getProject() != null) {
				return outletsPreference.get(file.getProject());
			}
		}

		IContainer defaultOutletContainer = getContainer(outputPathField.getText());
		if (defaultOutletContainer != null) {
			defaultOutlet = new ExtendedOutlet(defaultOutletContainer);
			return Collections.singletonList(defaultOutlet);
		}

		return Collections.<ExtendedOutlet> emptyList();
	}

	protected IContainer getContainer(String fullPath) {
		if (fullPath != null && fullPath.length() > 0) {
			IPath path = new Path(fullPath);
			path.makeAbsolute();
			if (path.segmentCount() == 1) {
				return ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
			} else {
				return ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.groups.AbstractGroup#isGroupComplete()
	 */
	@Override
	public boolean isGroupComplete() {
		if (outletsPreference == null) {
			if (useDefaultPathButtonField != null && !useDefaultPathButtonField.isSelected()) {
				return getContainer(outputPathField.getText()) != null;
			}
		}
		return super.isGroupComplete();
	}
}

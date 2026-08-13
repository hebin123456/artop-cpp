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
package org.eclipse.sphinx.xtendxpand.ui.dialogs;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.sphinx.platform.ui.fields.IField;
import org.eclipse.sphinx.platform.ui.fields.IFieldListener;
import org.eclipse.sphinx.platform.ui.fields.SelectionButtonField;
import org.eclipse.sphinx.platform.util.StatusUtil;
import org.eclipse.sphinx.xtendxpand.outlet.ExtendedOutlet;
import org.eclipse.sphinx.xtendxpand.preferences.OutletsPreference;
import org.eclipse.sphinx.xtendxpand.ui.internal.Activator;
import org.eclipse.sphinx.xtendxpand.ui.internal.messages.Messages;
import org.eclipse.sphinx.xtendxpand.ui.outlet.providers.OutletProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;
import org.eclipse.xpand2.output.Outlet;

/**
 * This class implements dialog which allows an outlet to be created or edited
 * 
 * @author See4sys
 */
public class EditOutletDialog extends StatusDialog {

	private ExtendedOutlet outlet;
	private OutletProvider outletProvider;
	private String initialOutletName;
	private boolean editableName;

	private Text nameText;
	private Text locationText;
	private SelectionButtonField protectedRegionField;

	private Button workspaceBrowse;
	private Button fileBrowse;
	private Button variables;

	protected Listener listener = new Listener();

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            the parent shell, or <code>null</code> to create a top-level shell
	 * @param outlet
	 *            the outlet to edit
	 * @param edit
	 *            set to true is outlet is being edited, false if it is a outlet creation
	 * @param editableName
	 *            set to true if outlet name can be editable, false otherwise
	 * @param outletProvider
	 *            the {@link OutletProvider} created for an {@link OutletsPreference} and may refers to project
	 */
	public EditOutletDialog(Shell parent, ExtendedOutlet outlet, boolean edit, boolean editableName, OutletProvider outletProvider) {
		super(parent);
		String title = edit ? Messages.title_editOutletDialog : Messages.title_newOutletDialog;
		setTitle(title);
		this.outlet = outlet;
		this.editableName = editableName;
		this.outletProvider = outletProvider;
		initialOutletName = outlet.getName();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite ancestor) {
		Composite parent = new Composite(ancestor, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		parent.setLayout(layout);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		parent.setLayoutData(layoutData);

		createLabel(parent, Messages.label_name, 1);
		nameText = createText(parent, GridData.FILL_HORIZONTAL, 1);
		nameText.setEditable(editableName);
		if (outlet.getName() != null) {
			nameText.setText(outlet.getName());
		}
		if (editableName) {
			nameText.addModifyListener(listener);
		} else {
			if (outlet.getName() == null) {
				nameText.setText(Messages.label_default);
			}
		}

		createLabel(parent, Messages.label_location, 1);

		locationText = createText(parent, GridData.FILL_HORIZONTAL, 1);
		if (outlet.getPathExpression() != null) {
			locationText.setText(outlet.getPathExpression());
		}
		locationText.addModifyListener(listener);

		createLabel(parent, "", 1); //$NON-NLS-1$

		Composite buttonsParent = new Composite(parent, SWT.None);
		GridLayout blayout = new GridLayout();
		blayout.numColumns = 3;
		buttonsParent.setLayout(blayout);
		GridData bLayoutData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		buttonsParent.setLayoutData(bLayoutData);

		workspaceBrowse = createPushButton(buttonsParent, Messages.label_workspaceBrowse, null);
		workspaceBrowse.addSelectionListener(listener);

		fileBrowse = createPushButton(buttonsParent, Messages.label_fileSystemBrowse, null);
		fileBrowse.addSelectionListener(listener);

		variables = createPushButton(buttonsParent, Messages.label_variablesBrowse, null);
		variables.addSelectionListener(listener);

		protectedRegionField = new SelectionButtonField(SWT.CHECK);
		protectedRegionField.setLabelText(Messages.label_useAsProtectedRegion);
		protectedRegionField.fillIntoGrid(parent, 3);
		protectedRegionField.setSelectionWithoutEvent(outlet.isProtectedRegion());
		protectedRegionField.addFieldListener(listener);
		return parent;
	}

	/**
	 * This method is called when user changes outlet name. It checks its consistency and refreshes buttons accordingly.
	 * 
	 * @See {@link #validateOutletName()}
	 */
	protected void handleNameChanged() {
		IStatus nameStatus = validateOutletName();
		if (nameStatus.isOK()) {
			outlet.setName(nameText.getText());
		}
		// Updates dialog status with name and location validation status
		updateStatus(validateOutletInputs());
	}

	/**
	 * This method is called when user changes outlet location. It checks its consistency and refreshes buttons
	 * accordingly.
	 * 
	 * @see #validateOutletLocation()
	 */
	protected void handleLocationChanged() {
		IStatus locationStatus = validateOutletLocation();
		if (locationStatus.isOK()) {
			String location = locationText.getText();
			outlet.setPathExpression(location, outletProvider.getProject());
		}
		// Updates dialog status with name and location validation status
		updateStatus(validateOutletInputs());
	}

	/**
	 * This method is called when user clicks on file system button. It will display a folder selection dialog.
	 */
	protected void handleBrowseFileSystem() {
		String filePath = locationText.getText();
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		filePath = dialog.open();
		if (filePath != null) {
			locationText.setText(filePath);
		}
	}

	/**
	 * This method is called when user clicks on Workspace button. It will display Workspace selection dialog.
	 */
	protected void handleBrowseWorkspace() {
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		dialog.setTitle(Messages.title_containerSelection);
		dialog.setMessage(Messages.msg_containerSelection);
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
		if (outletProvider.getProject() != null) {
			dialog.setInitialSelection(outletProvider.getProject());
		}
		if (dialog.open() == IDialogConstants.OK_ID) {
			IResource resource = (IResource) dialog.getFirstResult();
			if (resource != null) {
				String location;
				IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();
				if (resource.getProject() == outletProvider.getProject()) {
					location = variableManager.generateVariableExpression(ExtendedOutlet.VARIABLE_PROJECT_LOC, null) + IPath.SEPARATOR
							+ resource.getProjectRelativePath().toString();
				} else {
					location = variableManager.generateVariableExpression(ExtendedOutlet.VARIABLE_WORKSPACE_LOC, null)
							+ resource.getFullPath().toString();

				}
				locationText.setText(location);
			}
		}
	}

	protected void handleInsertVariable() {
		// Displays string variables dialog
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		if (dialog.open() == IDialogConstants.OK_ID || dialog.getResult().length == 1) {
			String variableName = dialog.getVariableExpression();
			if (variableName != null) {
				locationText.setText(variableName);
			}
		}
	}

	/**
	 * Checks that all outlet properties are valid, by checking its name and its location.
	 * 
	 * @return Status OK if name and location are valid
	 * @see #validateOutletName()
	 * @see #validateOutletLocation()
	 */

	protected IStatus validateOutletInputs() {
		IStatus status = validateOutletName();
		if (!status.isOK()) {
			return status;
		} else {
			return validateOutletLocation();
		}
	}

	/**
	 * Checks outlet name by checking it is not empty, or name is not already used by other ones.
	 * 
	 * @return Returns status of outlet name validity
	 */
	protected IStatus validateOutletName() {
		String name = nameText.getText();
		if (name.trim().length() == 0) {
			return StatusUtil.createStatus(IStatus.ERROR, IStatus.ERROR, Messages.msg_outletNameEmptyValidationError, Activator.getPlugin()
					.getSymbolicName(), null);
		}
		if (name.equals(initialOutletName)) {
			return Status.OK_STATUS;
		}
		for (Outlet outlet : outletProvider.getOutlets()) {
			if (name.equals(outlet.getName())) {
				return StatusUtil.createStatus(IStatus.ERROR, IStatus.ERROR, Messages.msg_outletNameExistValidationError, Activator.getPlugin()
						.getSymbolicName(), null);
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Validates that the location is not empty and Is a valid path. We don't validate the container existence, it will
	 * be created by Xpand during the generation if it does not exist.
	 * 
	 * @return status of outlet location validity
	 */
	protected IStatus validateOutletLocation() {
		String location = locationText.getText();

		// Check if path is valid
		if (location.trim().length() == 0 || !Path.ROOT.isValidPath(location)) {
			return StatusUtil.createStatus(IStatus.ERROR, IStatus.ERROR, Messages.msg_outletLocationEmptyValidationError, Activator.getPlugin()
					.getSymbolicName(), null);
		}
		return Status.OK_STATUS;
	}

	private Label createLabel(Composite parent, String text, int hspan) {
		Label label = new Label(parent, SWT.NONE);
		label.setFont(parent.getFont());
		label.setText(text);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = hspan;
		gd.grabExcessHorizontalSpace = false;
		label.setLayoutData(gd);
		return label;
	}

	private Button createPushButton(Composite parent, String label, Image image) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		if (image != null) {
			button.setImage(image);
		}
		if (label != null) {
			button.setText(label);
		}
		GridData gd = new GridData();
		button.setLayoutData(gd);
		setButtonDimensionHint(button);
		return button;
	}

	private Text createText(Composite parent, int style, int hspan) {
		Text text = new Text(parent, SWT.BORDER);
		text.setFont(parent.getFont());
		GridData gd = new GridData(style);
		gd.horizontalSpan = hspan;
		text.setLayoutData(gd);
		return text;
	}

	private void setButtonDimensionHint(Button button) {
		Assert.isNotNull(button);
		Object gd = button.getLayoutData();
		if (gd instanceof GridData) {
			((GridData) gd).widthHint = getButtonWidthHint(button);
			((GridData) gd).horizontalAlignment = GridData.FILL;
		}
	}

	/**
	 * @return Returns outlet being edited or created
	 */
	public ExtendedOutlet getOutlet() {
		return outlet;
	}

	/**
	 * Sets validation buttons state before dialog is displayed.
	 * 
	 * @see org.eclipse.jface.dialogs.StatusDialog#create()
	 */
	@Override
	public void create() {
		super.create();
		// Update initial OK button to be disabled for new Outlet
		updateButtonsEnableState(validateOutletInputs());
	}

	private int getButtonWidthHint(Button button) {
		button.setFont(JFaceResources.getDialogFont());
		PixelConverter converter = new PixelConverter(button);
		int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}

	class Listener extends SelectionAdapter implements ModifyListener, IFieldListener {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == fileBrowse) {
				handleBrowseFileSystem();
			} else if (source == workspaceBrowse) {
				handleBrowseWorkspace();
			} else if (source == variables) {
				handleInsertVariable();
			}
		}

		@Override
		public void modifyText(ModifyEvent e) {
			Object source = e.getSource();
			if (source == nameText) {
				handleNameChanged();
			} else if (source == locationText) {
				handleLocationChanged();
			}
		}

		@Override
		public void dialogFieldChanged(IField field) {
			if (field == protectedRegionField) {
				outlet.setProtectedRegion(protectedRegionField.isSelected());
			}
		}
	}
}

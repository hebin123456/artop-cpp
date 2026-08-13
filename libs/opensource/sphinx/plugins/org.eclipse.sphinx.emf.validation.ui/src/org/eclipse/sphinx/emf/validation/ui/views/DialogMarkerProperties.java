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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.validation.preferences.EMFModelValidationPreferences;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.validation.markers.IValidationMarker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateMarkersOperation;
import org.eclipse.ui.ide.undo.UpdateMarkersOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Shows the properties of a new or existing marker In 3.3, this class was refactored to allow pre-existing public
 * dialog classes to share the implementation. Note that certain methods are exposed as API in public subclasses, so
 * changes to the methods in this class should be treated carefully as they may affect API methods in subclasses. The
 * specific methods affected are documented in the method comment.
 */
public class DialogMarkerProperties extends TrayDialog {

	private static final String DIALOG_SETTINGS_SECTION = "DialogMarkerPropertiesDialogSettings"; //$NON-NLS-1$

	/**
	 * The marker being shown, or <code>null</code> for a new marker
	 */
	private IMarker marker = null;

	/**
	 * The resource on which to create a new marker
	 */
	private IResource resource = null;

	/**
	 * The type of marker to be created
	 */
	private String type = IMarker.MARKER;

	/**
	 * The initial attributes to use when creating a new marker
	 */
	private Map initialAttributes = null;

	/**
	 * The text control for the Description field.
	 */
	private Text descriptionText;

	/**
	 * The control for the Creation Time field.
	 */
	private Label creationTime;

	/**
	 * The text control for the Resource field.
	 */
	private Text resourceText;

	/**
	 * The text control for the Folder field.
	 */
	private Text folderText;

	/**
	 * The styled text for rule info
	 */
	private StyledText ruleInfoText = null;

	/**
	 * the rule state
	 */
	private Button ruleActivatedToggleButton = null;

	/**
	 * Dirty flag. True if any changes have been made.
	 */
	private boolean dirty;

	private String title;

	/**
	 * The name used to describe the specific kind of marker. Used when creating an undo command for the dialog, so that
	 * a specific name such as "Undo Create Task" or "Undo Modify Bookmark" can be used.
	 */
	private String markerName;

	/**
	 * Creates the dialog. By default this dialog creates a new marker. To set the resource and initial attributes for
	 * the new marker, use <code>setResource</code> and <code>setInitialAttributes</code>. To show or modify an existing
	 * marker, use <code>setMarker</code>.
	 * 
	 * @param parentShell
	 *            the parent shell
	 */
	public DialogMarkerProperties(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/**
	 * Creates the dialog. By default this dialog creates a new marker. To set the resource and initial attributes for
	 * the new marker, use <code>setResource</code> and <code>setInitialAttributes</code>. To show or modify an existing
	 * marker, use <code>setMarker</code>.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param title
	 *            the title of the dialog
	 */
	public DialogMarkerProperties(Shell parentShell, String title) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.title = title;
	}

	/**
	 * Creates the dialog. By default this dialog creates a new marker. To set the resource and initial attributes for
	 * the new marker, use <code>setResource</code> and <code>setInitialAttributes</code>. To show or modify an existing
	 * marker, use <code>setMarker</code>.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param title
	 *            the title of the dialog
	 * @param markerName
	 *            the name used to describe the specific kind of marker shown
	 * @since 0.7.0
	 */
	public DialogMarkerProperties(Shell parentShell, String title, String markerName) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.title = title;
		this.markerName = markerName;
	}

	/**
	 * Sets the marker to show or modify.
	 * <p>
	 * IMPORTANT: Although this class is internal, there are public subclasses that expose this method as API. Changes
	 * in this implementation should be treated as API changes.
	 * 
	 * @param marker
	 *            the marker, or <code>null</code> to create a new marker
	 * @since 0.7.0
	 */
	public void setMarker(IMarker marker) {
		this.marker = marker;
		if (marker != null) {
			try {
				type = marker.getType();
			} catch (CoreException e) {
			}
		}
	}

	/**
	 * Returns the marker being created or modified. For a new marker, this returns <code>null</code> until the dialog
	 * returns, but is non-null after.
	 * <p>
	 * IMPORTANT: Although this method is protected and the class is internal, there are public subclasses that expose
	 * this method as API. Changes in this implementation should be treated as API changes.
	 * 
	 * @return the marker
	 * @since 0.7.0
	 */
	protected IMarker getMarker() {
		return marker;
	}

	/**
	 * Sets the resource to use when creating a new task. If not set, the new task is created on the workspace root.
	 * <p>
	 * IMPORTANT: Although this class is internal, there are public subclasses that expose this method as API. Changes
	 * in this implementation should be treated as API changes.
	 * 
	 * @param resource
	 *            the resource
	 */
	public void setResource(IResource resource) {
		this.resource = resource;
	}

	/**
	 * Returns the resource to use when creating a new task, or <code>null</code> if none has been set. If not set, the
	 * new task is created on the workspace root.
	 * <p>
	 * IMPORTANT: Although this method is protected and the class is internal, there are public subclasses that expose
	 * this method as API. Changes in this implementation should be treated as API changes.
	 * 
	 * @return the resource
	 * @since 0.7.0
	 */
	protected IResource getResource() {
		return resource;
	}

	/**
	 * Sets initial attributes to use when creating a new task. If not set, the new task is created with default
	 * attributes.
	 * <p>
	 * IMPORTANT: Although this method is protected and the class is internal, there are public subclasses that expose
	 * this method as API. Changes in this implementation should be treated as API changes.
	 * 
	 * @param initialAttributes
	 *            the initial attributes
	 * @since 0.7.0
	 */
	protected void setInitialAttributes(Map initialAttributes) {
		this.initialAttributes = initialAttributes;
	}

	/**
	 * Returns the initial attributes to use when creating a new task, or <code>null</code> if not set. If not set, the
	 * new task is created with default attributes.
	 * <p>
	 * IMPORTANT: Although this method is protected and the class is internal, there are public subclasses that expose
	 * this method as API. Changes in this implementation should be treated as API changes.
	 * 
	 * @return the initial attributes
	 * @since 0.7.0
	 */
	protected Map getInitialAttributes() {
		if (initialAttributes == null) {
			initialAttributes = new HashMap();
		}
		return initialAttributes;
	}

	/**
	 * Method declared on Window.
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (title == null) {
			newShell.setText(MarkerMessages.propertiesDialog_title);
		} else {
			newShell.setText(title);
		}
	}

	/**
	 * Method declared on Dialog.
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		// initialize resources/properties
		if (marker != null) {
			resource = marker.getResource();
			try {
				initialAttributes = marker.getAttributes();
			} catch (CoreException e) {
			}
		} else if (resource == null) {
			resource = ResourcesPlugin.getWorkspace().getRoot();
		}

		Composite comp = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(comp, SWT.NULL);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gridData);

		initializeDialogUnits(composite);

		createDescriptionArea(composite);
		if (marker != null) {
			createSeperator(composite);
			createCreationTimeArea(composite);
		}
		createAttributesArea(composite);
		if (resource != null) {
			createSeperator(composite);
			createResourceArea(composite);
		}

		// Rules specific Information
		if (initialAttributes.containsKey(IValidationMarker.RULE_ID_ATTRIBUTE)) {

			createSeperator(composite);
			createRuleInfoArea(composite);
		}

		updateDialogFromMarker();
		updateEnablement();

		Dialog.applyDialogFont(composite);

		return composite;
	}

	/**
	 * Creates the area for the RuleName field.
	 */
	private void createRuleInfoArea(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(MarkerMessages.propertiesDialog_RuleInfo_text);
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		label.setLayoutData(gd);

		ruleInfoText = new StyledText(parent, SWT.READ_ONLY | SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		ruleInfoText.setLayoutData(gridData);

		String ruleId = (String) initialAttributes.get(IValidationMarker.RULE_ID_ATTRIBUTE);

		List styles = new java.util.ArrayList(32); // lots of style info
		String text = MarkerConstraintDetailsHelper.formatConstraintDescription(ruleId, styles);

		ruleInfoText.setText(text);
		ruleInfoText.setStyleRanges((StyleRange[]) styles.toArray(new StyleRange[styles.size()]));

		Label activatedLabel = new Label(parent, SWT.NONE);
		activatedLabel.setText(MarkerMessages.propertiesDialog_RuleActivatedInfo_text);

		ruleActivatedToggleButton = new Button(parent, SWT.CHECK | SWT.FLAT);
		ruleActivatedToggleButton.setText(MarkerMessages.propertiesDialog_RuleActivatedInfo);
		gridData = new GridData(GridData.BEGINNING);

		ruleActivatedToggleButton.setSelection(!EMFModelValidationPreferences.isConstraintDisabled(ruleId));

		ruleActivatedToggleButton.setLayoutData(gridData);
	}

	/**
	 * Creates a seperator.
	 */
	protected void createSeperator(Composite parent) {
		Label seperator = new Label(parent, SWT.NULL);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		seperator.setLayoutData(gridData);
	}

	/**
	 * Method createCreationTimeArea.
	 * 
	 * @param parent
	 */
	private void createCreationTimeArea(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(MarkerMessages.propertiesDialog_creationTime_text);

		creationTime = new Label(parent, SWT.NONE);
	}

	/**
	 * Creates the OK and Cancel buttons.
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Creates the area for the Description field.
	 */
	private void createDescriptionArea(Composite parent) {
		Label label = new Label(parent, SWT.VERTICAL | SWT.UP);
		label.setText(MarkerMessages.propertiesDialog_description_text);
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		label.setLayoutData(gd);

		descriptionText = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.widthHint = convertHorizontalDLUsToPixels(400);
		gridData.heightHint = 80;
		descriptionText.setLayoutData(gridData);
		descriptionText.setBackground(new Color(null, 255, 255, 255));

		descriptionText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				markDirty();
			}
		});
	}

	/**
	 * This method is intended to be overridden by subclasses. The attributes area is created between the creation time
	 * area and the resource area.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected void createAttributesArea(Composite parent) {
	}

	/**
	 * Creates the area for the Resource field.
	 */
	private void createResourceArea(Composite parent) {
		Label resourceLabel = new Label(parent, SWT.NONE);
		resourceLabel.setText(MarkerMessages.propertiesDialog_resource_text);
		resourceText = new Text(parent, SWT.SINGLE | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		resourceText.setLayoutData(gridData);

		Label folderLabel = new Label(parent, SWT.NONE);
		folderLabel.setText(MarkerMessages.propertiesDialog_folder_text);
		folderText = new Text(parent, SWT.SINGLE | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		folderText.setLayoutData(gridData);

	}

	/**
	 * Updates the dialog from the marker state.
	 */
	protected void updateDialogFromMarker() {
		if (marker == null) {
			updateDialogForNewMarker();
			return;
		}
		descriptionText.setText(Util.getProperty(IMarker.MESSAGE, marker));
		if (creationTime != null) {
			creationTime.setText(Util.getCreationTime(marker));
		}
		if (resourceText != null) {
			resourceText.setText(Util.getResourceName(marker));
		}
		if (folderText != null) {
			folderText.setText(Util.getContainerName(marker));
		}

		descriptionText.selectAll();
	}

	/**
	 * Updates the dialog from the predefined attributes.
	 */
	protected void updateDialogForNewMarker() {
		if (resource != null && resourceText != null && folderText != null) {
			resourceText.setText(resource.getName());

			IPath path = resource.getFullPath();
			int n = path.segmentCount() - 1; // n is the number of segments in container, not path
			if (n > 0) {
				int len = 0;
				for (int i = 0; i < n; ++i) {
					len += path.segment(i).length();
				}
				// account for /'s
				if (n > 1) {
					len += n - 1;
				}
				StringBuffer sb = new StringBuffer(len);
				for (int i = 0; i < n; ++i) {
					if (i != 0) {
						sb.append('/');
					}
					sb.append(path.segment(i));
				}
				folderText.setText(sb.toString());
			}
		}

		if (initialAttributes != null) {
			Object description = initialAttributes.get(IMarker.MESSAGE);
			if (description != null && description instanceof String) {
				descriptionText.setText((String) description);
			}
			descriptionText.selectAll();

		}
	}

	/**
	 * Method declared on Dialog
	 */
	@Override
	protected void okPressed() {
		if (marker == null || Util.isEditable(marker)) {
			saveChanges();
		}

		// Changes on rule activation
		if (ruleActivatedToggleButton != null) {
			boolean isActivated = ruleActivatedToggleButton.getSelection();

			String ruleId = (String) initialAttributes.get(IValidationMarker.RULE_ID_ATTRIBUTE);

			EMFModelValidationPreferences.setConstraintDisabled(ruleId, !isActivated);
			EMFModelValidationPreferences.save();

		}

		super.okPressed();
	}

	/**
	 * Sets the dialog's dirty flag to <code>true</code>
	 */
	protected void markDirty() {
		dirty = true;
	}

	/**
	 * @return <ul>
	 *         <li><code>true</code> if the dirty flag has been set to true.</li>
	 *         <li><code>false</code> otherwise.</li>
	 *         </ul>
	 */
	protected boolean isDirty() {
		return dirty;
	}

	/**
	 * Saves the changes made in the dialog if needed. Creates a new marker if needed. Updates the existing marker only
	 * if there have been changes.
	 */
	private void saveChanges() {
		Map attrs = getMarkerAttributes();
		IUndoableOperation op = null;
		if (marker == null) {
			if (resource == null) {
				return;
			}
			op = new CreateMarkersOperation(type, attrs, resource, getCreateOperationTitle());
		} else {
			if (isDirty()) {
				op = new UpdateMarkersOperation(marker, attrs, getModifyOperationTitle(), true);
			}
		}
		if (op != null) {
			try {
				PlatformUI.getWorkbench().getOperationSupport().getOperationHistory()
						.execute(op, null, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
			} catch (ExecutionException e) {
				if (e.getCause() instanceof CoreException) {
					ErrorDialog.openError(getShell(), MarkerMessages.Error, null, ((CoreException) e.getCause()).getStatus());
				} else {
					IDEWorkbenchPlugin.log(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Returns the marker attributes to save back to the marker, based on the current dialog fields.
	 */
	protected Map getMarkerAttributes() {
		Map attrs = getInitialAttributes();
		attrs.put(IMarker.MESSAGE, descriptionText.getText());
		return attrs;
	}

	/**
	 * Updates widget enablement for the dialog. Should be overridden by subclasses.
	 */
	protected void updateEnablement() {
		descriptionText.setEditable(isEditable());
	}

	/**
	 * @return <ul>
	 *         <li><code>true</code> if the marker is editable or the dialog is creating a new marker.</li>
	 *         <li><code>false</code> if the marker is not editable.</li>
	 *         </ul>
	 */
	protected boolean isEditable() {
		if (marker == null) {
			return true;
		}
		return Util.isEditable(marker);
	}

	/**
	 * Sets the marker type when creating a new marker.
	 * 
	 * @param type
	 *            the marker type
	 * @since 0.7.0
	 */
	protected void setType(String type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Dialog#getDialogBoundsSettings()
	 * @since 0.7.0
	 */
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = IDEWorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_SETTINGS_SECTION);
		if (section == null) {
			section = settings.addNewSection(DIALOG_SETTINGS_SECTION);
		}
		return section;
	}

	/**
	 * Return the string that describes a modify marker operation. Subclasses may override to more specifically describe
	 * the marker.
	 * 
	 * @since 0.7.0
	 */
	protected String getModifyOperationTitle() {
		if (markerName == null) {
			// we don't know what kind of marker is being modified
			return MarkerMessages.DialogMarkerProperties_ModifyMarker;
		}
		return NLS.bind(MarkerMessages.qualifiedMarkerCommand_title, MarkerMessages.DialogMarkerProperties_Modify, markerName);
	}

	/**
	 * Return the string that describes a create marker operation. Subclasses may override to more specifically describe
	 * the marker.
	 * 
	 * @since 0.7.0
	 */
	protected String getCreateOperationTitle() {
		if (markerName == null) {
			// we don't know what kind of marker is being created
			return MarkerMessages.DialogMarkerProperties_CreateMarker;
		}
		return NLS.bind(MarkerMessages.qualifiedMarkerCommand_title, MarkerMessages.DialogMarkerProperties_Create, markerName);

	}
}

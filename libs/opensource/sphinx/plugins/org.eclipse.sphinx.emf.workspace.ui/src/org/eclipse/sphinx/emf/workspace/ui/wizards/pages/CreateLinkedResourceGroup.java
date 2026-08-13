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
package org.eclipse.sphinx.emf.workspace.ui.wizards.pages;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.sphinx.emf.workspace.ui.internal.Activator;
import org.eclipse.sphinx.emf.workspace.ui.internal.messages.Messages;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ide.dialogs.PathVariableSelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.dialogs.FileSystemSelectionArea;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;
import org.eclipse.ui.internal.ide.filesystem.FileSystemConfiguration;
import org.eclipse.ui.internal.ide.filesystem.FileSystemSupportRegistry;

/**
 * Widget group for specifying a linked file or folder target.
 */
@SuppressWarnings("restriction")
public class CreateLinkedResourceGroup {
	private Listener listener;

	// linkTarget can contain either:
	// 1) A URI, ex: foo://bar/file.txt
	// 2) A path, ex: c:\foo\bar\file.txt
	// 3) A path variable relative path, ex: VAR\foo\bar\file.txt
	private String linkTarget = ""; //$NON-NLS-1$

	private String[] filterExtensions = new String[0];

	private int type;

	// Used to compute layout sizes
	private FontMetrics fontMetrics;

	// Widgets
	private Composite groupComposite;

	private Text linkTargetField;

	private Button browseButton;

	private Button variablesButton;

	private Label resolvedPathLabelText;

	private Label resolvedPathLabelData;

	private final IStringValue updatableResourceName;

	/**
	 * Helper interface intended for updating a string value based on the currently selected link target.
	 */
	public static interface IStringValue {
		/**
		 * Sets the String value.
		 * 
		 * @param string
		 *            a non-null String
		 */
		void setValue(String string);

		/**
		 * Gets the String value.
		 * 
		 * @return the current value, or <code>null</code>
		 */
		String getValue();
	}

	private String lastUpdatedValue;

	private FileSystemSelectionArea fileSystemSelectionArea;

	/**
	 * Creates a link target group
	 * 
	 * @param type
	 *            specifies the type of resource to link to. <code>IResource.FILE</code> or
	 *            <code>IResource.FOLDER</code>
	 * @param listener
	 *            listener to notify when one of the widgets' value is changed.
	 * @param updatableResourceName
	 *            an updatable string value that will be updated to reflect the link target's last segment, or
	 *            <code>null</code>. Updating will only happen if the current value of that string is null or the empty
	 *            string, or if it has not been changed since the last time it was updated.
	 */
	public CreateLinkedResourceGroup(int type, Listener listener, IStringValue updatableResourceName) {
		this.type = type;
		this.listener = listener;
		this.updatableResourceName = updatableResourceName;
		if (updatableResourceName != null) {
			lastUpdatedValue = updatableResourceName.getValue();
		}
	}

	/**
	 * Creates the widgets
	 * 
	 * @param parent
	 *            parent composite of the widget group
	 * @return the widget group
	 */
	public Composite createContents(Composite parent) {
		Font font = parent.getFont();
		initializeDialogUnits(parent);
		// Top level group
		groupComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		groupComposite.setLayout(layout);
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		gd.heightHint = 250;
		gd.widthHint = 250;
		groupComposite.setLayoutData(gd);
		groupComposite.setFont(font);

		if (type == IResource.FILE) {
			new Label(groupComposite, SWT.SINGLE).setText(Messages.wizardNewLinkedFileCreationPage_targetFileLabel);
		} else {
			new Label(groupComposite, SWT.SINGLE).setText(Messages.wizardNewLinkedFolderCreationPage_targetFolderLabel);
		}
		createLinkLocationGroup(groupComposite, true);
		return groupComposite;
	}

	/**
	 * Creates the link target location widgets.
	 * 
	 * @param locationGroup
	 *            the parent composite
	 * @param enabled
	 *            sets the initial enabled state of the widgets
	 */
	private void createLinkLocationGroup(Composite locationGroup, boolean enabled) {
		// LinkTargetGroup is necessary to decouple layout from
		// resolvedPathGroup layout
		Composite linkTargetGroup = new Composite(locationGroup, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		linkTargetGroup.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		linkTargetGroup.setLayoutData(data);

		// Link target location entry field
		linkTargetField = new Text(linkTargetGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.horizontalSpan = 2;
		linkTargetField.setLayoutData(data);
		linkTargetField.setEnabled(enabled);
		linkTargetField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				linkTarget = linkTargetField.getText();
				resolveVariable();
				if (updatableResourceName != null) {
					String value = updatableResourceName.getValue();
					if (value == null || value.equals("") || value.equals(lastUpdatedValue)) { //$NON-NLS-1$
						IPath linkTargetPath = new Path(linkTarget);
						String lastSegment = linkTargetPath.lastSegment();
						lastSegment = lastSegment == null ? "" : lastSegment; //$NON-NLS-1$
						lastUpdatedValue = lastSegment;
						updatableResourceName.setValue(lastSegment);
					}
				}
				if (listener != null) {
					listener.handleEvent(new Event());
				}
			}
		});

		// Browse button
		browseButton = new Button(linkTargetGroup, SWT.PUSH);
		browseButton.setText(IDEWorkbenchMessages.CreateLinkedResourceGroup_browseButton);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleLinkTargetBrowseButtonPressed();
			}
		});
		browseButton.setEnabled(enabled);
		setButtonLayoutData(browseButton);

		// Variables button
		variablesButton = new Button(linkTargetGroup, SWT.PUSH);
		variablesButton.setText(IDEWorkbenchMessages.CreateLinkedResourceGroup_variablesButton);
		variablesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleVariablesButtonPressed();
			}
		});
		variablesButton.setEnabled(enabled);
		setButtonLayoutData(variablesButton);

		createFileSystemSelection(linkTargetGroup, enabled);

		createResolvedPathGroup(locationGroup, 0);

		if (linkTarget != null && linkTarget.length() > 0) {
			linkTargetField.setText(linkTarget);
		}
	}

	/**
	 * Create the file system selection area.
	 * 
	 * @param composite
	 * @param enabled
	 *            the initial enablement state.
	 */
	private void createFileSystemSelection(Composite composite, boolean enabled) {

		// Always use the default if that is all there is.
		if (FileSystemSupportRegistry.getInstance().hasOneFileSystem()) {
			return;
		}

		fileSystemSelectionArea = new FileSystemSelectionArea();
		fileSystemSelectionArea.createContents(composite);
		fileSystemSelectionArea.setEnabled(enabled);
	}

	/**
	 * Create the composite for the resolved path.
	 * 
	 * @param locationGroup
	 * @param indent
	 */
	private void createResolvedPathGroup(Composite locationGroup, int indent) {
		GridLayout layout;
		GridData data;
		Composite resolvedPathGroup = new Composite(locationGroup, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		resolvedPathGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = indent;
		resolvedPathGroup.setLayoutData(data);

		resolvedPathLabelText = new Label(resolvedPathGroup, SWT.SINGLE);
		resolvedPathLabelText.setText(IDEWorkbenchMessages.CreateLinkedResourceGroup_resolvedPathLabel);
		resolvedPathLabelText.setVisible(false);

		resolvedPathLabelData = new Label(resolvedPathGroup, SWT.SINGLE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		resolvedPathLabelData.setLayoutData(data);
		resolvedPathLabelData.setVisible(false);
	}

	/**
	 * Returns a new status object with the given severity and message.
	 * 
	 * @return a new status object with the given severity and message.
	 */
	private IStatus createStatus(int severity, String message) {
		return new Status(severity, Activator.getPlugin().getBundle().getSymbolicName(), severity, message, null);
	}

	/**
	 * Disposes the group's widgets.
	 */
	public void dispose() {
		if (groupComposite != null && groupComposite.isDisposed() == false) {
			groupComposite.dispose();
		}
	}

	/**
	 * Returns the link target location entered by the user.
	 * 
	 * @return the link target location entered by the user. null if the user chose not to create a link.
	 */
	public URI getLinkTargetURI() {
		// Resolve path variable if we have a relative path
		if (!linkTarget.startsWith("/")) { //$NON-NLS-1$
			IPathVariableManager pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
			try {

				URI path = new URI(linkTarget.replace(java.io.File.separatorChar, '/'));
				URI resolved = pathVariableManager.resolveURI(path);
				if (path != resolved) {
					// we know this is a path variable, but return unresolved
					// path so resource will be created with variable intact
					return path;
				}
			} catch (URISyntaxException e) {
				// link target is not a valid URI. Fall through to handle this
				// below
			}
		}

		FileSystemConfiguration configuration = getSelectedConfiguration();
		if (configuration == null) {
			return URIUtil.toURI(linkTarget);
		}
		// Validate non-local file system location
		return configuration.getContributor().getURI(linkTarget);
	}

	/**
	 * Opens a file or directory browser depending on the link type.
	 */
	private void handleLinkTargetBrowseButtonPressed() {
		IFileStore store = null;
		String selection = null;
		FileSystemConfiguration config = getSelectedConfiguration();
		boolean isDefault = config == null || FileSystemSupportRegistry.getInstance().getDefaultConfiguration().equals(config);

		if (linkTarget.length() > 0) {
			store = IDEResourceInfoUtils.getFileStore(linkTarget);
			if (!store.fetchInfo().exists()) {
				store = null;
			}
		}
		if (type == IResource.FILE) {
			if (isDefault) {
				FileDialog dialog = new FileDialog(linkTargetField.getShell());
				dialog.setText(IDEWorkbenchMessages.CreateLinkedResourceGroup_targetSelectionTitle);
				if (store != null) {
					if (store.fetchInfo().isDirectory()) {
						dialog.setFilterPath(linkTarget);
					} else {
						dialog.setFileName(linkTarget);
					}
				} else {
					dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
				}
				if (filterExtensions != null && filterExtensions.length != 0) {
					dialog.setFilterExtensions(filterExtensions);
				}
				selection = dialog.open();
			} else {
				URI uri = config.getContributor().browseFileSystem(linkTarget, linkTargetField.getShell());
				if (uri != null) {
					selection = uri.toString();
				}
			}
		} else {
			String filterPath = null;
			if (store != null) {
				IFileStore path = store;
				if (!store.fetchInfo().isDirectory()) {
					path = store.getParent();
				}
				if (path != null) {
					filterPath = store.toString();
				}
			}

			if (isDefault) {
				DirectoryDialog dialog = new DirectoryDialog(linkTargetField.getShell());
				dialog.setMessage(IDEWorkbenchMessages.CreateLinkedResourceGroup_targetSelectionLabel);
				if (filterPath != null) {
					dialog.setFilterPath(filterPath);
				} else {
					dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
				}
				selection = dialog.open();
			} else {
				String initialPath = IDEResourceInfoUtils.EMPTY_STRING;
				if (filterPath != null) {
					initialPath = filterPath;
				}
				URI uri = config.getContributor().browseFileSystem(initialPath, linkTargetField.getShell());
				if (uri != null) {
					selection = uri.toString();
				}
			}
		}
		if (selection != null) {
			linkTargetField.setText(selection);
		}
	}

	/**
	 * Return the selected configuration or <code>null</code> if there is not one selected.
	 * 
	 * @return FileSystemConfiguration or <code>null</code>
	 */
	private FileSystemConfiguration getSelectedConfiguration() {
		if (fileSystemSelectionArea == null) {
			return null;
		}
		return fileSystemSelectionArea.getSelectedConfiguration();
	}

	/**
	 * Opens a path variable selection dialog
	 */
	private void handleVariablesButtonPressed() {
		int variableTypes = IResource.FOLDER;

		// Allow selecting file and folder variables when creating a
		// linked file
		if (type == IResource.FILE) {
			variableTypes |= IResource.FILE;
		}

		PathVariableSelectionDialog dialog = new PathVariableSelectionDialog(linkTargetField.getShell(), variableTypes);
		if (dialog.open() == IDialogConstants.OK_ID) {
			String[] variableNames = (String[]) dialog.getResult();
			if (variableNames != null && variableNames.length == 1) {
				linkTargetField.setText(variableNames[0]);
			}
		}
	}

	/**
	 * Initializes the computation of horizontal and vertical dialog units based on the size of current font.
	 * <p>
	 * This method must be called before <code>setButtonLayoutData</code> is called.
	 * </p>
	 * 
	 * @param control
	 *            a control from which to obtain the current font
	 */
	protected void initializeDialogUnits(Control control) {
		// Compute and store a font metric
		GC gc = new GC(control);
		gc.setFont(control.getFont());
		fontMetrics = gc.getFontMetrics();
		gc.dispose();
	}

	/**
	 * Tries to resolve the value entered in the link target field as a variable, if the value is a relative path.
	 * Displays the resolved value if the entered value is a variable.
	 */
	private void resolveVariable() {
		try {
			URI uri = new URI(linkTarget);

			IPathVariableManager pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
			URI resolvedURI = pathVariableManager.resolveURI(uri);

			if (uri.equals(resolvedURI)) {
				resolvedPathLabelText.setVisible(false);
				resolvedPathLabelData.setVisible(false);
			} else {
				resolvedPathLabelText.setVisible(true);
				resolvedPathLabelData.setVisible(true);

				IPath resolvedPath = URIUtil.toPath(resolvedURI);
				resolvedPathLabelData.setText(resolvedPath.toOSString());
			}
		} catch (URISyntaxException ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
	}

	/**
	 * Sets the <code>GridData</code> on the specified button to be one that is spaced for the current dialog page
	 * units. The method <code>initializeDialogUnits</code> must be called once before calling this method for the first
	 * time.
	 * 
	 * @param button
	 *            the button to set the <code>GridData</code>
	 * @return the <code>GridData</code> set on the specified button
	 */
	private GridData setButtonLayoutData(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		return data;
	}

	/**
	 * Sets the value of the link target field
	 * 
	 * @param target
	 *            the value of the link target field
	 */
	public void setLinkTarget(String target) {
		linkTarget = target;
		if (linkTargetField != null && linkTargetField.isDisposed() == false) {
			linkTargetField.setText(target);
		}
	}

	/**
	 * Validates the type of the given file against the link type specified in the constructor.
	 * 
	 * @param linkTargetFile
	 *            file to validate
	 * @return IStatus indicating the validation result. IStatus.OK if the given file is valid.
	 */
	private IStatus validateFileType(IFileInfo linkTargetFile) {
		if (type == IResource.FILE && linkTargetFile.isDirectory()) {
			return createStatus(IStatus.ERROR, IDEWorkbenchMessages.CreateLinkedResourceGroup_linkTargetNotFile);
		} else if (type == IResource.FOLDER && !linkTargetFile.isDirectory()) {
			return createStatus(IStatus.ERROR, IDEWorkbenchMessages.CreateLinkedResourceGroup_linkTargetNotFolder);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Validates this page's controls.
	 * 
	 * @param linkHandle
	 *            The target to check
	 * @return IStatus indicating the validation result. IStatus.OK if the specified link target is valid given the
	 *         linkHandle.
	 */
	public IStatus validateLinkLocation(IResource linkHandle) {
		if (linkTargetField == null || linkTargetField.isDisposed()) {
			return Status.OK_STATUS;
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		FileSystemConfiguration configuration = getSelectedConfiguration();
		if (configuration == null || EFS.SCHEME_FILE.equals(configuration.getScheme())) {
			// Special handling for UNC paths. See bug 90825
			IPath location = new Path(linkTarget);
			if (location.isUNC()) {
				return createStatus(IStatus.WARNING, IDEWorkbenchMessages.CreateLinkedResourceGroup_unableToValidateLinkTarget);
			}
		}
		URI locationURI = getLinkTargetURI();
		if (locationURI == null) {
			return createStatus(IStatus.WARNING, IDEWorkbenchMessages.CreateLinkedResourceGroup_unableToValidateLinkTarget);
		}
		IStatus locationStatus = workspace.validateLinkLocationURI(linkHandle, locationURI);
		if (locationStatus.getSeverity() == IStatus.ERROR || linkTarget.trim().equals("")) { //$NON-NLS-1$
			return locationStatus;
		}

		// Use the resolved link target name
		URI resolved = workspace.getPathVariableManager().resolveURI(locationURI);
		IFileInfo linkTargetFile = IDEResourceInfoUtils.getFileInfo(resolved);
		if (linkTargetFile != null && linkTargetFile.exists()) {
			IStatus fileTypeStatus = validateFileType(linkTargetFile);
			if (!fileTypeStatus.isOK()) {
				return fileTypeStatus;
			}
		} else if (locationStatus.isOK()) {
			// locationStatus takes precedence over missing location warning.
			return createStatus(IStatus.WARNING, IDEWorkbenchMessages.CreateLinkedResourceGroup_linkTargetNonExistent);
		}
		return locationStatus;
	}

	/**
	 * Set the file extensions which the {@link FileDialog} will use to filter the files it shows to the argument, which
	 * may be null.
	 * <p>
	 * The strings are platform specific. For example, on some platforms, an extension filter string is typically of the
	 * form "*.extension", where "*.*" matches all files. For filters with multiple extensions, use semicolon as a
	 * separator, e.g. "*.jpg;*.png".
	 * </p>
	 * 
	 * @param filterExtensions
	 *            the filterExtensions to set on the {@link FileDialog} opened when the user select the browse button.
	 */
	public void setFilterExtensions(String[] filterExtensions) {
		this.filterExtensions = filterExtensions;
	}
}

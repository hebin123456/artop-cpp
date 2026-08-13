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

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Iterator;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.workspace.ui.internal.Activator;
import org.eclipse.sphinx.emf.workspace.ui.internal.messages.Messages;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.undo.CreateFileOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;

public class LinkedFileCreationMainPage extends WizardPage implements Listener {

	// The current resource selection
	private IStructuredSelection currentSelection;

	// Cache of newly-created file
	private IFile newFile;

	private URI linkTargetPath;

	// Widgets
	private CreateLinkedResourceGroup linkedResourceGroup;

	private Composite linkedResourceParent;

	private Composite linkedResourceComposite;

	// Initial value stores
	private String initialFileName;

	/**
	 * The file extension to use for this page's file name field when it does not exist yet.
	 * 
	 * @see WizardNewFileCreationPage#setFileExtension(String)
	 * @since 0.7.0
	 */
	private String initialFileExtension;

	private IPath initialContainerFullPath;

	/**
	 * Height of the "advanced" linked resource group. Set when the advanced group is first made visible.
	 */
	private int linkedResourceGroupHeight = -1;

	/**
	 * First time the advanced group is validated.
	 */
	private boolean firstLinkCheck = true;

	/**
	 * File extensions which the {@link FileDialog} will use to filter the files it shows.
	 */
	private String[] filterExtensions = new String[0];

	/**
	 * Creates a new file creation wizard page. If the initial resource selection contains exactly one container
	 * resource then it will be used as the default container resource.
	 * 
	 * @param selection
	 *            the current resource selection
	 */
	public LinkedFileCreationMainPage(IStructuredSelection selection) {
		super(Messages.wizardNewLinkedFileCreationPage_title.replaceAll("[^A-Z]", ""));//$NON-NLS-1$ //$NON-NLS-2$
		setTitle(Messages.wizardNewLinkedFileCreationPage_title);
		setDescription(Messages.wizardNewLinkedFileCreationPage_description);
		setPageComplete(false);
		currentSelection = selection;
		setContainerFullPath(currentSelection);
	}

	/**
	 * (non-Javadoc) Method declared on IDialogPage.
	 */
	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		// Top level group
		Composite topLevel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		topLevel.setLayout(layout);
		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
		topLevel.setLayoutData(gridData);
		topLevel.setFont(parent.getFont());
		doCreateControls(topLevel);
		if (initialFileName != null) {
			setFileName(initialFileName);
		}
		if (initialFileExtension != null) {
			setFileExtension(initialFileExtension);
		}
		validatePage();
		setControl(topLevel);
	}

	/**
	 * Creates the widget for advanced options.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected void doCreateControls(Composite parent) {
		boolean disableLinking = Platform.getPreferencesService().getBoolean(ResourcesPlugin.getPlugin().getBundle().getSymbolicName(),
				ResourcesPlugin.PREF_DISABLE_LINKING, false, null);
		if (!disableLinking) {
			linkedResourceParent = new Composite(parent, SWT.NONE);
			linkedResourceParent.setFont(parent.getFont());
			linkedResourceParent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			linkedResourceParent.setLayout(layout);
		}
		linkedResourceGroup = new CreateLinkedResourceGroup(IResource.FILE, new Listener() {
			@Override
			public void handleEvent(Event e) {
				firstLinkCheck = false;
				setPageComplete(validatePage());
			}
		}, new CreateLinkedResourceGroup.IStringValue() {
			@Override
			public void setValue(String string) {
				setFileName(string);
			}

			@Override
			public String getValue() {
				return getFileName();
			}
		});
		if (filterExtensions != null && filterExtensions.length != 0) {
			linkedResourceGroup.setFilterExtensions(filterExtensions);
		}
		doShellResizing(parent);
	}

	/**
	 * Creates a file resource handle for the file with the given workspace path. This method does not create the file
	 * resource; this is the responsibility of <code>createFile</code>.
	 * 
	 * @param filePath
	 *            the path of the file resource to create a handle for
	 * @return the new file resource handle
	 */
	protected IFile createFileHandle(IPath filePath) {
		return ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
	}

	/**
	 * Creates the link target path if a link target has been specified.
	 */
	protected void createLinkTarget() {
		linkTargetPath = linkedResourceGroup.getLinkTargetURI();
	}

	/**
	 * Creates a new file resource in the selected container and with the selected name. Creates any missing resource
	 * containers along the path; does nothing if the container resources already exist.
	 * <p>
	 * In normal usage, this method is invoked after the user has pressed Finish on the wizard; the enablement of the
	 * Finish button implies that all controls on on this page currently contain valid values.
	 * </p>
	 * <p>
	 * Note that this page caches the new file once it has been successfully created; subsequent invocations of this
	 * method will answer the same file resource without attempting to create it again.
	 * </p>
	 * <p>
	 * This method should be called within a workspace modify operation since it creates resources.
	 * </p>
	 * 
	 * @return the created file resource, or <code>null</code> if the file was not created
	 */
	public IFile createNewFile() {
		if (newFile != null) {
			return newFile;
		}

		// Create the new file and cache it if successful

		final IPath containerPath = getContainerFullPath();
		IPath newFilePath = containerPath.append(getFileName());
		final IFile newFileHandle = createFileHandle(newFilePath);
		final InputStream initialContents = getInitialContents();

		createLinkTarget();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				CreateFileOperation op = new CreateFileOperation(newFileHandle, linkTargetPath, initialContents,
						Messages.wizardNewLinkedFileCreationPage_title);
				try {
					PlatformUI.getWorkbench().getOperationSupport().getOperationHistory()
							.execute(op, monitor, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
				} catch (final ExecutionException e) {
					getContainer().getShell().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							if (e.getCause() instanceof CoreException) {
								ErrorDialog.openError(getContainer().getShell(), // Was
										// Utilities.getFocusShell()
										Messages.wizardNewLinkedFileCreationPage_errorTitle, null, // no
										// Special message
										((CoreException) e.getCause()).getStatus());
							} else {
								PlatformLogUtil.logAsError(Activator.getPlugin(), "createLinkedFile(): " + e.getCause()); //$NON-NLS-1$
								MessageDialog.openError(getContainer().getShell(), Messages.wizardNewLinkedFileCreationPage_internalErrorTitle,
										NLS.bind(Messages.wizardNewLinkedFileCreationPage_internalErrorMessage, e.getCause().getMessage()));
							}
						}
					});
				}
			}
		};
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return null;
		} catch (InvocationTargetException e) {
			// Execution Exceptions are handled above but we may still get
			// unexpected runtime errors.
			PlatformLogUtil.logAsError(Activator.getPlugin(), "createLinkedFile()" + e.getTargetException()); //$NON-NLS-1$
			MessageDialog.openError(getContainer().getShell(), Messages.wizardNewLinkedFileCreationPage_internalErrorTitle,
					NLS.bind(Messages.wizardNewLinkedFileCreationPage_internalErrorMessage, e.getTargetException().getMessage()));

			return null;
		}

		newFile = newFileHandle;

		return newFile;
	}

	/**
	 * Returns the current full path of the containing resource as entered or selected by the user, or its anticipated
	 * initial value.
	 * 
	 * @return the container's full path, anticipated initial value, or <code>null</code> if no path is known
	 */
	public IPath getContainerFullPath() {
		return initialContainerFullPath;
	}

	/**
	 * Returns the current file name as entered by the user, or its anticipated initial value. <br>
	 * <br>
	 * The current file name will include the file extension if the preconditions are met.
	 * 
	 * @see WizardNewFileCreationPage#setFileExtension(String)
	 * @return the file name, its anticipated initial value, or <code>null</code> if no file name is known
	 */
	public String getFileName() {
		return initialFileName;
	}

	/**
	 * Returns a stream containing the initial contents to be given to new file resource instances. <b>Subclasses</b>
	 * may wish to override. This default implementation provides no initial contents.
	 * 
	 * @return initial contents to be given to new file resource instances
	 */
	protected InputStream getInitialContents() {
		return null;
	}

	/**
	 * Resize the shell.
	 */
	protected void doShellResizing(Composite parent) {
		Shell shell = getShell();
		Point shellSize = shell.getSize();
		Composite composite = parent;
		if (linkedResourceComposite != null) {
			linkedResourceComposite.dispose();
			linkedResourceComposite = null;
			composite.layout();
			shell.setSize(shellSize.x, shellSize.y - linkedResourceGroupHeight);
		} else {
			linkedResourceComposite = linkedResourceGroup.createContents(linkedResourceParent);
			if (linkedResourceGroupHeight == -1) {
				Point groupSize = linkedResourceComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
				linkedResourceGroupHeight = groupSize.y;
			}
			shell.setSize(600, 300 + linkedResourceGroupHeight);
			composite.layout();
		}
	}

	/**
	 * The <code>WizardNewFileCreationPage</code> implementation of this <code>Listener</code> method handles all events
	 * and enablements for controls on this page. Subclasses may extend.
	 */
	@Override
	public void handleEvent(Event event) {
		setPageComplete(validatePage());
	}

	// TODO : refactoring remove this to createlinked resource group
	private void setContainerFullPath(IStructuredSelection currentSelection) {

		Iterator<?> it = currentSelection.iterator();
		if (it.hasNext()) {
			Object object = it.next();
			IResource selectedResource = null;
			if (object instanceof IResource) {
				selectedResource = (IResource) object;
			} else if (object instanceof IAdaptable) {
				selectedResource = (IResource) ((IAdaptable) object).getAdapter(IResource.class);
			}
			if (selectedResource != null) {
				if (selectedResource.getType() == IResource.FILE) {
					selectedResource = selectedResource.getParent();
				}
				initialContainerFullPath = selectedResource.getFullPath();
			}
		}
	}

	/**
	 * Called when the user select his file to link
	 */
	public void setFileName(String value) {
		initialFileName = value;
	}

	/**
	 * Set the only file extension allowed for this page's file name field. If this page's controls do not exist yet,
	 * store it for future use. <br>
	 * <br>
	 * If a file extension is specified, then it will always be appended with a '.' to the text from the file name field
	 * for validation when the following conditions are met: <br>
	 * <br>
	 * (1) File extension length is greater than 0 <br>
	 * (2) File name field text length is greater than 0 <br>
	 * (3) File name field text does not already end with a '.' and the file extension specified (case sensitive) <br>
	 * <br>
	 * The file extension will not be reflected in the actual file name field until the file name field loses focus.
	 * 
	 * @param value
	 *            The file extension without the '.' prefix (e.g. 'java', 'xml')
	 * @since 0.7.0
	 */
	public void setFileExtension(String value) {
		initialFileExtension = value;
	}

	/**
	 * Checks whether the linked resource target is valid. Sets the error message accordingly and returns the status.
	 * 
	 * @return IStatus validation result from the CreateLinkedResourceGroup
	 */
	protected IStatus validateLinkedResource() {
		IPath containerPath = getContainerFullPath();
		IPath newFilePath = containerPath.append(getFileName());
		IFile newFileHandle = createFileHandle(newFilePath);
		IStatus status = linkedResourceGroup.validateLinkLocation(newFileHandle);

		if (status.getSeverity() == IStatus.ERROR) {
			if (firstLinkCheck) {
				setMessage(status.getMessage());
			} else {
				setErrorMessage(status.getMessage());
			}
		} else if (status.getSeverity() == IStatus.WARNING) {
			setMessage(status.getMessage(), WARNING);
			setErrorMessage(null);
		}
		return status;
	}

	/**
	 * Returns whether this page's controls currently all contain valid values.
	 * 
	 * @return <code>true</code> if all controls are valid, and <code>false</code> if at least one is invalid
	 */
	protected boolean validatePage() {
		boolean valid = true;
		setMessage(null);
		setErrorMessage(null);

		IPath containerFullPath = getContainerFullPath();
		if (containerFullPath == null || containerFullPath.segmentCount() == 0) {
			valid = false;
			setErrorMessage(Messages.wizardNewLinkedFileCreationPage_noParentSelected);
		} else {
			String containerProjectName = containerFullPath.segment(0);
			IProject containerProject = ResourcesPlugin.getWorkspace().getRoot().getProject(containerProjectName);
			if (!containerProject.isOpen()) {
				valid = false;
				setErrorMessage(Messages.wizardNewLinkedFileCreationPage_selectedParentProjectNotOpen);
			}
		}

		if (valid) {
			String fileName = getFileName();
			IStatus result = ResourcesPlugin.getWorkspace().validateName(fileName, IResource.FILE);
			if (!result.isOK()) {
				valid = false;
				if (!firstLinkCheck) {
					setErrorMessage(result.getMessage());
				}
			}
		}

		IStatus linkedResourceStatus = null;
		if (valid) {
			linkedResourceStatus = validateLinkedResource();
			if (linkedResourceStatus.getSeverity() == IStatus.ERROR) {
				valid = false;
			}
		}

		return valid;
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

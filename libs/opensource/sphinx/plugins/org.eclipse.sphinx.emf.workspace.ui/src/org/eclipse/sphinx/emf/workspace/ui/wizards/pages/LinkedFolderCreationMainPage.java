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

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Iterator;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateFolderOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;

/**
 * Standard main page for a wizard that creates a folder resource.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * </p>
 * <p>
 * Subclasses may extend
 * <ul>
 * <li><code>handleEvent</code></li>
 * </ul>
 * </p>
 */
public class LinkedFolderCreationMainPage extends WizardPage implements Listener {

	private IStructuredSelection currentSelection;

	private IFolder newFolder;

	// TODO : Like file name refactor this to createLinkedResourceGroup
	private String folderName;

	// link target location
	private URI linkTargetPath;

	private IPath initialContainerFullPath;

	// widgets
	// private ResourceAndContainerGroup resourceGroup;

	// private Button advancedButton;

	private CreateLinkedResourceGroup linkedResourceGroup;

	private Composite linkedResourceParent;

	private Composite linkedResourceComposite;

	/**
	 * Height of the "advanced" linked resource group. Set when the advanced group is first made visible.
	 */
	private int linkedResourceGroupHeight = -1;

	/**
	 * First time the advanced group is validated.
	 */
	private boolean firstLinkCheck = true;

	/**
	 * Creates a new folder creation wizard page. If the initial resource selection contains exactly one container
	 * resource then it will be used as the default container resource.
	 * 
	 * @param pageName
	 *            the name of the page
	 * @param selection
	 *            the current resource selection
	 */
	public LinkedFolderCreationMainPage(IStructuredSelection selection) {
		super(Messages.wizardNewLinkedFolderCreationPage_title.replaceAll("[^A-Z]", ""));//$NON-NLS-1$ //$NON-NLS-2$
		setTitle(Messages.wizardNewLinkedFolderCreationPage_title);
		setDescription(Messages.wizardNewLinkedFolderCreationPage_description);
		setPageComplete(false);
		currentSelection = selection;
		initializeContainerPath();
	}

	/**
	 * (non-Javadoc) Method declared on IDialogPage.
	 */
	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		// Top level group
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		doCreateControls(composite);
		validatePage();
		setControl(composite);
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
		linkedResourceGroup = new CreateLinkedResourceGroup(IResource.FOLDER, new Listener() {
			@Override
			public void handleEvent(Event e) {
				firstLinkCheck = false;
				setPageComplete(validatePage());
			}
		}, new CreateLinkedResourceGroup.IStringValue() {
			@Override
			public String getValue() {
				return getFolderName();
			}

			@Override
			public void setValue(String string) {
				setFolderName(string);
			}
		});
		doShellResizing(parent);
	}

	private void setFolderName(String string) {
		folderName = string;

	}

	protected String getFolderName() {
		return folderName;
	}

	/**
	 * Creates a folder resource handle for the folder with the given workspace path. This method does not create the
	 * folder resource; this is the responsibility of <code>createFolder</code>.
	 * 
	 * @param folderPath
	 *            the path of the folder resource to create a handle for
	 * @return the new folder resource handle
	 */
	protected IFolder createFolderHandle(IPath folderPath) {
		return ResourcesPlugin.getWorkspace().getRoot().getFolder(folderPath);
	}

	/**
	 * Creates the link target path if a link target has been specified.
	 */
	protected void createLinkTarget() {
		linkTargetPath = linkedResourceGroup.getLinkTargetURI();
	}

	/**
	 * Creates a new folder resource in the selected container and with the selected name. Creates any missing resource
	 * containers along the path; does nothing if the container resources already exist.
	 * <p>
	 * In normal usage, this method is invoked after the user has pressed Finish on the wizard; the enablement of the
	 * Finish button implies that all controls on this page currently contain valid values.
	 * </p>
	 * <p>
	 * Note that this page caches the new folder once it has been successfully created; subsequent invocations of this
	 * method will answer the same folder resource without attempting to create it again.
	 * </p>
	 * <p>
	 * This method should be called within a workspace modify operation since it creates resources.
	 * </p>
	 * 
	 * @return the created folder resource, or <code>null</code> if the folder was not created
	 */
	public IFolder createNewLinkedFolder() {
		if (newFolder != null) {
			return newFolder;
		}

		// create the new folder and cache it if successful
		final IPath containerPath = getContainerFullPath();
		IPath newFolderPath = containerPath.append(getFolderName());
		final IFolder newFolderHandle = createFolderHandle(newFolderPath);

		createLinkTarget();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				CreateFolderOperation createFolderOperation = new CreateFolderOperation(newFolderHandle, linkTargetPath,
						Messages.wizardNewLinkedFolderCreationPage_title);
				try {
					PlatformUI.getWorkbench().getOperationSupport().getOperationHistory()
							.execute(createFolderOperation, monitor, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
				} catch (final ExecutionException e) {
					getContainer().getShell().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							if (e.getCause() instanceof CoreException) {
								ErrorDialog.openError(getContainer().getShell(), // Was Utilities.getFocusShell()
										Messages.wizardNewLinkedFolderCreationPage_errorTitle, null, // no
										// special
										// message
										((CoreException) e.getCause()).getStatus());
							} else {
								PlatformLogUtil.logAsError(Activator.getPlugin(), "createNewLinkedFolder()" + e.getCause()); //$NON-NLS-1$
								MessageDialog.openError(getContainer().getShell(), Messages.wizardNewLinkedFolderCreationPage_internalErrorTitle,
										NLS.bind(Messages.wizardNewLinkedFolderCreationPage_internalErrorMessage, e.getCause().getMessage()));
							}
						}
					});
				}
			}
		};

		try {
			getContainer().run(true, true, runnable);
		} catch (InterruptedException e) {
			return null;
		} catch (InvocationTargetException e) {
			// ExecutionExceptions are handled above, but unexpected runtime
			// exceptions and errors may still occur.
			PlatformLogUtil.logAsError(Activator.getPlugin(), "createNewFolder()" + e.getTargetException()); //$NON-NLS-1$
			MessageDialog.openError(getContainer().getShell(), Messages.wizardNewLinkedFolderCreationPage_internalErrorTitle,
					NLS.bind(Messages.wizardNewLinkedFolderCreationPage_internalErrorMessage, e.getTargetException().getMessage()));
			return null;
		}

		newFolder = newFolderHandle;

		return newFolder;
	}

	/**
	 * Shows/hides the advanced option widgets.
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
	 * The <code>WizardNewFolderCreationPage</code> implementation of this <code>Listener</code> method handles all
	 * events and enablements for controls on this page. Subclasses may extend.
	 */
	@Override
	public void handleEvent(Event ev) {
		setPageComplete(validatePage());
	}

	/**
	 * Initializes this page's container path.
	 */
	protected void initializeContainerPath() {
		Iterator<?> it = currentSelection.iterator();
		if (it.hasNext()) {
			Object next = it.next();
			IResource selectedResource = null;
			if (next instanceof IResource) {
				selectedResource = (IResource) next;
			} else if (next instanceof IAdaptable) {
				selectedResource = (IResource) ((IAdaptable) next).getAdapter(IResource.class);
			}
			if (selectedResource != null) {
				if (selectedResource.getType() == IResource.FILE) {
					selectedResource = selectedResource.getParent();
				}
				setContainerFullPath(selectedResource.getFullPath());
			}
		}

		setPageComplete(false);
	}

	/**
	 * Checks whether the linked resource target is valid. Sets the error message accordingly and returns the status.
	 * 
	 * @return IStatus validation result from the CreateLinkedResourceGroup
	 */
	protected IStatus validateLinkedResource() {
		IPath containerPath = getContainerFullPath();
		IPath newFolderPath = containerPath.append(getFolderName());
		IFolder newFolderHandle = createFolderHandle(newFolderPath);
		IStatus status = linkedResourceGroup.validateLinkLocation(newFolderHandle);

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

		IPath containerFullPath = getContainerFullPath();
		if (containerFullPath == null || containerFullPath.segmentCount() == 0) {
			valid = false;
			setErrorMessage(Messages.wizardNewLinkedFolderCreationPage_noParentSelected);
		} else {
			String containerProjectName = containerFullPath.segment(0);
			IProject containerProject = ResourcesPlugin.getWorkspace().getRoot().getProject(containerProjectName);
			if (!containerProject.isOpen()) {
				valid = false;
				setErrorMessage(Messages.wizardNewLinkedFolderCreationPage_selectedParentProjectNotOpen);
			}
		}

		if (valid) {
			String folderName = getFolderName();
			IStatus result = ResourcesPlugin.getWorkspace().validateName(folderName, IResource.FOLDER);
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

		if (valid && (linkedResourceStatus == null || linkedResourceStatus.isOK())) {
			setMessage(null);
			setErrorMessage(null);
		}
		return valid;
	}

	// TODO : refactor this remove to createlinkedResourceGroup
	protected IPath getContainerFullPath() {
		return initialContainerFullPath;
	}

	// TODO refactor this remove to createlinkedResourceGroup
	private void setContainerFullPath(IPath path) {
		initialContainerFullPath = path;
	}

}

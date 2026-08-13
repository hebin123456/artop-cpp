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
 *     itemis - [403728] NewModelProjectCreationPage and NewModelFileCreationPage should provided hooks for creating additional controls
 *     itemis - [405023] Enable NewModelFileCreationPage to be used without having to pass an instance of NewModelFileProperties to its constructor
 *     itemis - [406062] Removal of the required project nature parameter in NewModelFileCreationPage constructor and CreateNewModelProjectJob constructor
 *     itemis - [406194] Enable title and descriptions of model project and file creation wizards to be calculated automatically
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.wizards.pages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ListIterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.workspace.ui.internal.Activator;
import org.eclipse.sphinx.emf.workspace.ui.internal.messages.Messages;
import org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

/**
 * Basic main page for a wizard that creates a file resource. The new model file is to be created based on the given
 * {@linkplain InitialModelProperties new model file properties} (metamodel, ePackage and eClassifier).
 * <p>
 * This page may be used by clients as it is; it may also be subclassed to suit. Subclasses may override validatePage(),
 * getFileExtensionErrorMessage(), getRequiredProjectNatureErrorMessage(), hasRequiredProjectNature(), setVisible(),
 * getDefaultFileExtension(), etc.
 */
public class NewModelFileCreationPage<T extends IMetaModelDescriptor> extends WizardNewFileCreationPage {

	protected IStructuredSelection selection;
	protected IProjectWorkspacePreference<T> metaModelVersionPreference = null;
	protected InitialModelProperties<T> initialModelProperties = null;

	private String requiredProjectTypeName = null;

	protected boolean noValidFileExtensionsForContentTypeIdFoundProblemLoggedOnce = false;

	/**
	 * Creates a new instance of the new model file creation wizard page.
	 *
	 * @param pageId
	 *            the name of the page
	 * @param selection
	 *            the current resource selection
	 * @param metaModelVersionPreference
	 *            the metamodel version {@linkplain IProjectWorkspacePreference preference}
	 */
	public NewModelFileCreationPage(String pageId, IStructuredSelection selection, IProjectWorkspacePreference<T> metaModelVersionPreference) {
		super(pageId, selection);

		this.selection = selection;
		this.metaModelVersionPreference = metaModelVersionPreference;

		String metaModelName = getMetaModelName();
		setTitle(NLS.bind(Messages.page_newModelFileCreation_title, metaModelName != null ? metaModelName : Messages.default_metamodelName_cap));
		setDescription(NLS.bind(Messages.page_newModelFileCreation_description, metaModelName != null ? metaModelName
				: Messages.default_metamodelName));
	}

	/**
	 * Creates a new instance of new model file creation wizard page.
	 *
	 * @param pageId
	 *            the name of the page
	 * @param selection
	 *            the current resource selection
	 * @param initialModelProperties
	 *            the chosen {@linkplain InitialModelProperties initial model properties} (metamodel, EPackage and
	 *            EClassifier) to be used as basis for creating the initial model of the new model file
	 */
	public NewModelFileCreationPage(String pageId, IStructuredSelection selection, InitialModelProperties<T> initialModelProperties) {
		this(pageId, selection, (IProjectWorkspacePreference<T>) null, initialModelProperties);
	}

	/**
	 * Creates a new instance of new model file creation wizard page.
	 *
	 * @param pageId
	 *            the name of the page
	 * @param selection
	 *            the current resource selection
	 * @param metaModelVersionPreference
	 *            the metamodel version {@linkplain IProjectWorkspacePreference preference}
	 * @param initialModelProperties
	 *            the chosen {@linkplain InitialModelProperties initial model properties} (metamodel, EPackage and
	 *            EClassifier) to be used as basis for creating the initial model of the new model file
	 */
	public NewModelFileCreationPage(String pageId, IStructuredSelection selection, IProjectWorkspacePreference<T> metaModelVersionPreference,
			InitialModelProperties<T> initialModelProperties) {
		this(pageId, selection, metaModelVersionPreference);
		this.initialModelProperties = initialModelProperties;
	}

	/*
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		createAdditionalControls((Composite) getControl());
		Dialog.applyDialogFont(getControl());
	}

	/**
	 * Creates controls for specific project creation options to be placed behind those for file name, container and
	 * advanced options (which are created by {@link WizardNewFileCreationPage#createControl(Composite)}).
	 * <p>
	 * This implementation does nothing.
	 * </p>
	 * This method may be overridden by subclasses to provide custom implementations.
	 *
	 * @param parent
	 *            the parent composite
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createControl(Composite)
	 */
	protected void createAdditionalControls(Composite parent) {
		// Do nothing by default
	}

	/**
	 * Returns the {@linkplain IProject project} behind the containing resource as entered or selected by the user.
	 *
	 * @return the {@linkplain IProject container project} or <code>null</code> if it is not yet known
	 */
	public IProject getContainerProject() {
		IPath containerFullPath = getContainerFullPath();
		if (containerFullPath != null && containerFullPath.segmentCount() >= 1) {
			// Return project behind current container path
			return ResourcesPlugin.getWorkspace().getRoot().getProject(containerFullPath.segment(0));
		}
		return null;
	}

	/**
	 * Returns the new {@linkplain IFile file} behind the current file name as entered or selected by the user.
	 *
	 * @return the new {@linkplain IFile file} or <code>null</code> if it is not yet known
	 */
	public IFile getNewFile() {
		return ResourcesPlugin.getWorkspace().getRoot().getFile(getContainerFullPath().append(getFileName()));
	}

	/**
	 * Returns a unique default name for the new file to be created which is composed of its
	 * {@link #getDefaultBaseName() default base name} and the {@link #getDefaultFileExtension() default extension}. If
	 * this file name already exists new file names are generated by appending an integer number to the default base
	 * name (or to the default extension if the default base name is omitted) until a new file name has been found that
	 * is not yet used.
	 *
	 * @see #getDefaultBaseName()
	 * @see #getDefaultFileExtension()
	 */
	protected String getUniqueDefaultFileName(IContainer container) {
		String baseName = getDefaultBaseName();
		String extension = getDefaultFileExtension();
		if (baseName != null || extension != null) {
			String fileName = createFileName(baseName, extension, -1);
			for (int i = 1; container.findMember(fileName) != null; ++i) {
				fileName = createFileName(baseName, extension, i);
			}
			return fileName;
		}
		return null;
	}

	/**
	 * Creates a name for the new file to be created using provided base name, extension and number.
	 *
	 * @param baseName
	 *            the base name of the new file, or <code>null</code> if new file should have no base name
	 * @param extension
	 *            the extension of the new file, or <code>null</code> if new file should have no extension
	 * @param number
	 *            a non-negative number to be appended to the new file's base name - or to its extension in case that
	 *            the former is omitted - or -1 if no number should be appended at all
	 * @return the new file name
	 */
	protected String createFileName(String baseName, String extension, int number) {
		StringBuilder fileName = new StringBuilder();
		if (baseName != null) {
			fileName.append(baseName);
			if (number != -1) {
				fileName.append(number);
			}
		}
		if (extension != null) {
			fileName.append("."); //$NON-NLS-1$
			fileName.append(extension);
			if (baseName == null && number != -1) {
				fileName.append(number);
			}
		}
		return fileName.toString();
	}

	/**
	 * Gets the default base name of the new file, a string "default" by default.
	 */
	protected String getDefaultBaseName() {
		return Messages.default_modelFileBaseName;
	}

	/**
	 * Returns the default file extension of the new model file to be created.
	 *
	 * @return the first string value of File Extension defined for the content type by default. If more than one file
	 *         extensions are defined for the content type, the clients should provide their specific overridden
	 *         methods.
	 */
	protected String getDefaultFileExtension() {
		Collection<String> validFileExtensions = getValidFileExtensions();
		if (!validFileExtensions.isEmpty()) {
			return validFileExtensions.iterator().next();
		}
		return null;
	}

	/**
	 * Returns the content type identifier for the metamodel descriptor behind the {@linkplain newModelFileProperties
	 * new model file properties}
	 */
	protected String getContentTypeId() {
		T mmDescriptor = getMetaModelDescriptor();
		if (mmDescriptor != null) {
			return mmDescriptor.getDefaultContentTypeId();
		}
		return null;
	}

	protected String getRequiredProjectTypeName() {
		if (requiredProjectTypeName == null) {
			String metaModelName = getMetaModelName();
			requiredProjectTypeName = metaModelName != null ? metaModelName : Messages.default_metamodelName;
		}
		return requiredProjectTypeName;
	}

	public void setRequiredProjectTypeName(String requiredProjectTypeName) {
		this.requiredProjectTypeName = requiredProjectTypeName;
	}

	/**
	 * Returns the {@link IMetaModelDescriptor metamodel descriptor} of the new file to be created.
	 *
	 * @return the new file's metamodel descriptor
	 */
	protected T getMetaModelDescriptor() {
		if (initialModelProperties != null) {
			return initialModelProperties.getMetaModelDescriptor();
		}
		if (metaModelVersionPreference != null) {
			return metaModelVersionPreference.get(getContainerProject());
		}
		return null;
	}

	protected String getMetaModelName() {
		IMetaModelDescriptor mmDescriptor = null;
		if (initialModelProperties != null) {
			mmDescriptor = initialModelProperties.getMetaModelDescriptor();
		}
		if (metaModelVersionPreference != null) {
			mmDescriptor = metaModelVersionPreference.getFromWorkspace();
		}

		if (mmDescriptor != null) {
			if (mmDescriptor.getBaseDescriptor() != null) {
				return mmDescriptor.getBaseDescriptor().getName();
			}
			return mmDescriptor.getName();
		}

		return null;
	}

	/*
	 * Overridden to initialize wizard page with default file name
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			// Try to determine container behind current selection
			if (selection != null && !selection.isEmpty()) {
				Object selected = selection.iterator().next();

				IFile file = EcorePlatformUtil.getFile(selected);
				if (file != null) {
					selected = file.getParent();
				}

				if (selected instanceof IContainer) {
					// Focus wizard on parent folder or project
					setContainerFullPath(((IContainer) selected).getFullPath());

					// Initialize file name control with default file name
					String fileName = getUniqueDefaultFileName((IContainer) selected);
					if (fileName != null) {
						setFileName(fileName);
					}
				}
			}
		}
	}

	/*
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#validatePage()
	 */
	@Override
	protected boolean validatePage() {
		// Let Eclipse check if the model file has a container project or folder, etc.
		if (!super.validatePage()) {
			return false;
		}

		// Make sure that we are in a (model) project that has the required nature
		IProject containerProject = getContainerProject();
		if (containerProject != null && !hasRequiredProjectNature(containerProject)) {
			setErrorMessage(getRequiredProjectNatureErrorMessage());
			return false;
		}

		// Make sure that the (model) project's metamodel version - if any - matches that of the model file to be
		// created
		if (containerProject != null && !hasMatchingMetaModelVersion(containerProject)) {
			setErrorMessage(getMatchingMetaModelVersionErrorMessage());
			return false;
		}

		// Make sure the model file has a valid extension
		String fileExtension = new Path(getFileName()).getFileExtension();
		Collection<String> validFileExtensions = getValidFileExtensions();
		if (!validFileExtensions.isEmpty() && !validFileExtensions.contains(fileExtension)) {
			setErrorMessage(getFileExtensionErrorMessage(validFileExtensions));
			return false;
		}

		return true;
	}

	/**
	 * Checks if the specified {@linkplain IProject project} has the required {@linkplain IProjectNature nature} that
	 * has been provided to this {@linkplain NewModelFileCreationPage}.
	 *
	 * @param project
	 *            the {@linkplain IProject project} to be checked; must not be <code>null</code> and must be
	 *            <em>accessible</em>
	 * @return <code>true</code> if specified {@linkplain IProject project} has the required {@linkplain IProjectNature
	 *         nature} or no nature is required, <code>false</code> otherwise.
	 */
	protected boolean hasRequiredProjectNature(IProject project) {
		Assert.isNotNull(project);
		Assert.isTrue(project.isAccessible());

		if (metaModelVersionPreference == null) {
			return true;
		}

		try {
			return project.hasNature(metaModelVersionPreference.getRequiredProjectNatureId());
		} catch (CoreException ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
		return false;
	}

	/**
	 * Checks if the metamodel version of the specified {@linkplain IProject project} matches that of the model file to
	 * be created
	 *
	 * @param project
	 *            the {@linkplain IProject project} to be checked; must not be <code>null</code> and must be
	 *            <em>accessible</em>
	 * @return <code>true</code> if specified {@linkplain IProject project}'s metamodel version matches that of the
	 *         model file to be created or no metamodel version preference, <code>false</code> otherwise.
	 */
	protected boolean hasMatchingMetaModelVersion(IProject project) {
		Assert.isNotNull(project);
		Assert.isTrue(project.isAccessible());

		if (metaModelVersionPreference == null) {
			return true;
		}

		T mmDescriptor = getMetaModelDescriptor();
		if (mmDescriptor != null) {
			if (!mmDescriptor.equals(metaModelVersionPreference.get(project))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns the valid file extensions for model files. They are retrieved from the content type to be used when
	 * creating new model file.
	 *
	 * @return A collection of valid file extensions for model files
	 */
	protected Collection<String> getValidFileExtensions() {
		String contentTypeId = getContentTypeId();
		if (contentTypeId != null) {
			Collection<String> validFileExtensions = ExtendedPlatform.getContentTypeFileExtensions(contentTypeId);
			if (validFileExtensions.isEmpty()) {
				if (!noValidFileExtensionsForContentTypeIdFoundProblemLoggedOnce) {
					PlatformLogUtil.logAsWarning(Activator.getPlugin(), new RuntimeException(
							"No valid file extensions for content type identifer '" + contentTypeId + "' found.")); //$NON-NLS-1$ //$NON-NLS-2$
					noValidFileExtensionsForContentTypeIdFoundProblemLoggedOnce = true;
				}
			}
			return validFileExtensions;
		}
		return Collections.emptySet();
	}

	protected String getRequiredProjectNatureErrorMessage() {
		String requiredProjectTypeName = getRequiredProjectTypeName();
		return NLS.bind(Messages.error_requiredProjectNature, requiredProjectTypeName != null ? requiredProjectTypeName
				: Messages.default_requiredProjectType);
	}

	protected String getMatchingMetaModelVersionErrorMessage() {
		T mmDescriptor = getMetaModelDescriptor();
		return NLS.bind(Messages.error_matchingMetaModelVersion, mmDescriptor != null ? mmDescriptor.getName() : ""); //$NON-NLS-1$
	}

	protected String getFileExtensionErrorMessage(Collection<String> validFileExtensions) {
		return NLS.bind(Messages.error_fileExtension, convertFileExtensionsToString(validFileExtensions));
	}

	/**
	 * Converts the given collection of file extensions into a string.
	 *
	 * @param fileExtensions
	 *            the collection of file extensions to be converted
	 * @return the file extensions as string
	 */
	protected String convertFileExtensionsToString(Collection<String> fileExtensions) {
		Assert.isNotNull(fileExtensions);

		StringBuilder buf = new StringBuilder();
		ListIterator<String> iter = new ArrayList<String>(fileExtensions).listIterator();
		while (iter.hasNext()) {
			if (iter.hasPrevious()) {
				buf.append(", "); //$NON-NLS-1$
			}
			buf.append("*."); //$NON-NLS-1$
			buf.append(iter.next());
		}
		return buf.toString();
	}
}

/**
 * <copyright>
 * 
 * Copyright (c) 2012 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     itemis - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.ui.wizards.pages;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.ui.internal.messages.Messages;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

/**
 * This is the one page of the wizard.
 */
public class AbstractModelNewFileCreationPage extends WizardNewFileCreationPage {

	/**
	 * The file extensions
	 */
	private List<String> fileExtensions;

	/**
	 * Constructor of the wizard's second page
	 * 
	 * @param pageId
	 * @param selection
	 */
	public AbstractModelNewFileCreationPage(String pageId, IStructuredSelection selection) {
		super(pageId, selection);
	}

	public void setFileExtensions(List<String> fileExtensions) {
		this.fileExtensions = fileExtensions;
	}

	public List<String> getFileExtensions() {
		return fileExtensions;
	}

	/**
	 * The framework calls this to see if the file is correct.
	 */
	@Override
	protected boolean validatePage() {
		if (super.validatePage()) {
			String extension = new Path(getFileName()).getFileExtension();
			if (extension == null || !fileExtensions.contains(extension)) {
				if (fileExtensions.size() > 1) {
					setErrorMessage(NLS.bind(Messages._WARN_FilenameExtensions, new Object[] { fileExtensions }));
				} else {
					setErrorMessage(NLS.bind(Messages._WARN_FilenameExtension, new Object[] { fileExtensions }));
				}
				return false;
			}
			return true;
		}
		return false;
	}

	public IFile getModelFile() {
		return ResourcesPlugin.getWorkspace().getRoot().getFile(getContainerFullPath().append(getFileName()));
	}
}
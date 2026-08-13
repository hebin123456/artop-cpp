/**
 * <copyright>
 * 
 * Copyright (c) 2008-2011 See4sys and others.
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
package org.eclipse.sphinx.graphiti.workspace.ui.wizards.pages;

import java.util.MissingResourceException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.graphiti.workspace.ui.internal.Activator;
import org.eclipse.sphinx.graphiti.workspace.ui.internal.messages.Messages;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public abstract class AbstractDiagramContainerWizardPage extends WizardNewFileCreationPage {

	private String diagramFileExtension;

	public AbstractDiagramContainerWizardPage(String pageName, IStructuredSelection selection, String diagramFileExtension) {
		super(pageName, selection);
		this.diagramFileExtension = diagramFileExtension;
		setTitle(getPageTitle());
		setDescription(getPageDescription());
	}

	private String getPageTitle() {
		String title = ""; //$NON-NLS-1$
		try {
			title = doGetTitle();
		} catch (MissingResourceException e) {
			PlatformLogUtil.logAsError(Activator.getDefault(), e);
		}
		return title;
	}

	protected abstract String doGetTitle() throws MissingResourceException;

	private String getPageDescription() {
		String description = ""; //$NON-NLS-1$
		try {
			description = doGetDescription();
		} catch (MissingResourceException e) {
			PlatformLogUtil.logAsError(Activator.getDefault(), e);
		}
		return description;
	}

	protected abstract String doGetDescription() throws MissingResourceException;

	public URI getURI() {
		return URI.createPlatformResourceURI(getFilePath().toString(), false);
	}

	protected String getExtension() {
		return diagramFileExtension;
	}

	protected IPath getFilePath() {
		IPath path = getContainerFullPath();
		if (path == null) {
			path = new Path(""); //$NON-NLS-1$
		}
		String fileName = getFileName();
		if (fileName != null) {
			path = path.append(fileName);
		}
		return path;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		setFileName(ExtendedPlatform.createUniqueFileName(getContainerFullPath(), "default." + getExtension())); //$NON-NLS-1$
		setPageComplete(validatePage());
	}

	@Override
	protected boolean validatePage() {
		if (!super.validatePage()) {
			return false;
		}
		String extension = getExtension();
		if (extension != null && !getFilePath().getFileExtension().equals(extension)) {
			setErrorMessage(NLS.bind(Messages.DiagramContainerWizardPage_PageExtensionError, extension));
			return false;
		}
		return true;
	}
}

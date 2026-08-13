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
 *     itemis - [343844] Enable multiple Xtend MetaModels to be configured on BasicM2xAction, M2xConfigurationWizard, and Xtend/Xpand/CheckJob
 *
 * </copyright>
 */
package org.eclipse.sphinx.xtendxpand.ui.wizards.pages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sphinx.emf.mwe.IXtendXpandConstants;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.platform.ui.groups.FileSelectionGroup;
import org.eclipse.sphinx.platform.ui.wizards.pages.AbstractWizardPage;
import org.eclipse.sphinx.xtendxpand.CheckEvaluationRequest;
import org.eclipse.sphinx.xtendxpand.ui.internal.Activator;
import org.eclipse.sphinx.xtendxpand.ui.internal.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CheckConfigurationPage extends AbstractWizardPage {

	// Check model Group
	protected FileSelectionGroup checkGroup;

	protected EObject modelObject;

	public CheckConfigurationPage(String pageName) {
		super(pageName);
	}

	public void init(EObject modelObject) {
		this.modelObject = modelObject;
	}

	@Override
	protected Control doCreateControl(Composite parent) {
		initializeDialogUnits(parent);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayout(layout);

		// Create page content
		createPageContent(composite);
		return composite;
	}

	protected void createPageContent(Composite parent) {
		createCheckGroup(parent);
	}

	protected void createCheckGroup(Composite parent) {
		IFile modelFile = EcorePlatformUtil.getFile(modelObject);
		if (modelFile != null) {
			checkGroup = new FileSelectionGroup(Messages.label_checkModelBlock, Messages.label_useCheckModelButton, Messages.label_checkModelBlock,
					IXtendXpandConstants.CHECK_EXTENSION, modelFile.getProject(), getDialogSettings());
			checkGroup.setSectionName(getCheckFileSelectionSectionName(modelObject));
			checkGroup.createContent(parent, 3);
		}
	}

	protected String getCheckFileSelectionSectionName(EObject object) {
		Assert.isNotNull(object);
		return Activator.getDefault().getBundle().getSymbolicName() + ".SECTION" + EcoreResourceUtil.getURI(object).toString(); //$NON-NLS-1$
	}

	@Override
	protected String doGetDescription() throws MissingResourceException {
		return Messages.desc_checkConfigurationPage;
	}

	@Override
	protected String doGetTitle() throws MissingResourceException {
		return Messages.title_checkConfigurationPage;
	}

	@Override
	protected boolean doIsPageComplete() {
		return true;
	}

	@Override
	protected IStatus doValidateRules() {
		return null;
	}

	public Collection<CheckEvaluationRequest> getCheckEvaluationRequests() {
		List<CheckEvaluationRequest> requests = new ArrayList<CheckEvaluationRequest>();
		if (modelObject != null) {
			Collection<IFile> checkFiles = checkGroup.getFiles();
			if (!checkFiles.isEmpty()) {
				requests.add(new CheckEvaluationRequest(checkFiles, modelObject));
			}
		}
		return requests;
	}

	public boolean isCheckEnabled() {
		return checkGroup.getEnableButtonState();
	}

	@Override
	public void finish() {
		if (checkGroup != null) {
			checkGroup.saveGroupSettings();
		}
	}
}

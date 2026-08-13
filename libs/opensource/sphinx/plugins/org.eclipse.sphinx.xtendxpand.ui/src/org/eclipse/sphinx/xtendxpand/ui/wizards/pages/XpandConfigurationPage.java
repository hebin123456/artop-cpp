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
 *     itemis - [357813] Risk of NullPointerException when transforming models using M2MConfigurationWizard
 *     itemis - [358706] Default output path never initialized when opening M2TConfigurationWizard
 *      
 * </copyright>
 */
package org.eclipse.sphinx.xtendxpand.ui.wizards.pages;

import java.util.Collection;
import java.util.MissingResourceException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sphinx.platform.ui.fields.IField;
import org.eclipse.sphinx.platform.ui.groups.IGroupListener;
import org.eclipse.sphinx.platform.ui.wizards.pages.AbstractWizardPage;
import org.eclipse.sphinx.xtendxpand.XpandEvaluationRequest;
import org.eclipse.sphinx.xtendxpand.outlet.ExtendedOutlet;
import org.eclipse.sphinx.xtendxpand.preferences.OutletsPreference;
import org.eclipse.sphinx.xtendxpand.ui.groups.OutputGroup;
import org.eclipse.sphinx.xtendxpand.ui.groups.TemplateGroup;
import org.eclipse.sphinx.xtendxpand.ui.internal.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.xtend.expression.TypeSystem;

public class XpandConfigurationPage extends AbstractWizardPage {

	protected TemplateGroup templateGroup;
	protected OutputGroup outputGroup;

	protected EObject modelObject;
	protected TypeSystem typeSystem;

	protected OutletsPreference outletsPreference;
	protected ExtendedOutlet defaultOutlet;

	public XpandConfigurationPage(String pageName) {
		super(pageName);
	}

	public void init(EObject modelObject, TypeSystem typeSystem, OutletsPreference outletsPreference, ExtendedOutlet defaultOutlet) {
		Assert.isNotNull(typeSystem);

		this.typeSystem = typeSystem;
		this.modelObject = modelObject;
		this.outletsPreference = outletsPreference;
		this.defaultOutlet = defaultOutlet;
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
		// Creates the template group field and load dialog settings.
		createTemplateGroup(parent);
		// Creates the output group field.
		createOutputGroup(parent);
	}

	/**
	 * Creates the template group field and load dialog settings.
	 */
	protected void createTemplateGroup(Composite parent) {
		templateGroup = new TemplateGroup(Messages.label_template, modelObject, typeSystem, getDialogSettings());
		templateGroup.createContent(parent, 3);
		templateGroup.addGroupListener(new IGroupListener() {

			@Override
			public void groupChanged(IField field) {
				getWizard().getContainer().updateButtons();
			}
		});
	}

	/**
	 * Creates the output group field.
	 */
	protected void createOutputGroup(Composite parent) {
		outputGroup = new OutputGroup(Messages.label_output, modelObject, outletsPreference, defaultOutlet);
		outputGroup.createContent(parent, 3);
		outputGroup.addGroupListener(new IGroupListener() {

			@Override
			public void groupChanged(IField field) {
				getWizard().getContainer().updateButtons();
			}
		});
	}

	@Override
	protected String doGetDescription() throws MissingResourceException {
		return Messages.desc_config;
	}

	@Override
	protected String doGetTitle() throws MissingResourceException {
		return Messages.title_launchGen;
	}

	@Override
	protected boolean doIsPageComplete() {
		return templateGroup.isGroupComplete() && outputGroup.isGroupComplete();

	}

	@Override
	protected IStatus doValidateRules() {
		return null;
	}

	public Collection<XpandEvaluationRequest> getXpandEvaluationRequests() {
		return templateGroup.getXpandEvaluationRequests();
	}

	public Collection<? extends ExtendedOutlet> getOutlets() {
		return outputGroup.getOutlets();
	}

	@Override
	public void finish() {
		if (templateGroup != null) {
			templateGroup.saveGroupSettings();
		}
	}
}

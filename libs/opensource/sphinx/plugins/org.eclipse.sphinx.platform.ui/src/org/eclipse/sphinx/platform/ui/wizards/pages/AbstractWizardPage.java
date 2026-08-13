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
package org.eclipse.sphinx.platform.ui.wizards.pages;

import java.util.MissingResourceException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.sphinx.platform.ui.fields.Separator;
import org.eclipse.sphinx.platform.ui.internal.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Abstraction for wizard pages responsible of the creation of new elements.
 */
public abstract class AbstractWizardPage extends WizardPage implements IWizardPage {

	/**
	 * @param pageName
	 */
	protected AbstractWizardPage(String pageName) {
		super(pageName);
		setTitle(getPageTitle());
		setDescription(getPageDescription());
	}

	@Override
	public final void createControl(Composite parent) {
		Control control = doCreateControl(parent);
		setControl(control);
	}

	@Override
	public final boolean isPageComplete() {
		return doIsPageComplete();
	}

	protected abstract Control doCreateControl(Composite parent);

	protected abstract String doGetDescription() throws MissingResourceException;

	private String getPageDescription() {
		String description = ""; //$NON-NLS-1$
		try {
			description = doGetDescription();
		} catch (MissingResourceException e) {
			PlatformLogUtil.logAsError(Activator.getDefault(), e);
		}
		return description;
	}

	protected abstract String doGetTitle() throws MissingResourceException;

	private String getPageTitle() {
		String title = ""; //$NON-NLS-1$
		try {
			title = doGetTitle();
		} catch (MissingResourceException e) {
			PlatformLogUtil.logAsError(Activator.getDefault(), e);
		}
		return title;
	}

	protected abstract boolean doIsPageComplete();

	protected abstract IStatus doValidateRules();

	protected final void updateStatus(IStatus status) {
		switch (status.getSeverity()) {
		case IStatus.OK:
			setMessage(null);
			break;
		case IStatus.INFO:
			setMessage(status.getMessage(), IMessageProvider.INFORMATION);
			break;
		case IStatus.WARNING:
			setMessage(status.getMessage(), IMessageProvider.WARNING);
			break;
		case IStatus.ERROR:
			setMessage(status.getMessage(), IMessageProvider.ERROR);
			break;
		default:
			break;
		}
	}

	protected final IStatus validateRules() {
		return doValidateRules();
	}

	/**
	 * Returns the recommended maximum width for text fields (in pixels). This method requires that createContent has
	 * been called before this method is call. Subclasses may override to change the maximum width for text fields.
	 * 
	 * @return The recommended maximum width for text fields.
	 */
	protected int getMaxFieldWidth() {
		return convertWidthInCharsToPixels(40);
	}

	/**
	 * Creates a separator line. Expects a <code>GridLayout</code> with at least 1 column.
	 * 
	 * @param composite
	 *            The parent composite
	 * @param nbCol
	 *            Number of columns to span
	 */
	protected final void createSeparator(Composite composite, int nbCol) {
		new Separator(SWT.SEPARATOR | SWT.HORIZONTAL).fillIntoGrid(composite, nbCol, convertHeightInCharsToPixels(1));
	}

	/**
	 * Called when the wizard is closed by selecting the finish button. Implementers typically override this method to
	 * store the page result.
	 */
	public void finish() {
		// Do nothing by default.
	}
}

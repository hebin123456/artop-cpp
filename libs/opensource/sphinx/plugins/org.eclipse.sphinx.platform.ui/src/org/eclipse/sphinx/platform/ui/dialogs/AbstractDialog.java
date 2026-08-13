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
package org.eclipse.sphinx.platform.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 */
// FIXME Make this class inherit from TrayDialog.
public abstract class AbstractDialog extends Dialog {

	/**
	 * Dialog title (a localised string).
	 */
	private String fTitle;

	/**
	 * Dialog title image.
	 */
	private Image fTitleImage;

	/**
	 * Message (a localised string).
	 */
	private String fMessage;

	/**
	 * Message label is the label the message is shown on.
	 */
	private Label fMessageLabel;

	/**
	 * Constructor.
	 * 
	 * @param parentShell
	 *            The parent shell, or <code>null</code> to create a top-level shell.
	 * @param title
	 *            The title to use for the dialog.
	 * @param message
	 *            The message to display inside this dialog.
	 */
	protected AbstractDialog(Shell parentShell, String title, String message) {
		super(parentShell);
		fTitle = title;
		fMessage = message;
	}

	/*
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (fTitle != null) {
			shell.setText(fTitle);
		}
		if (fTitleImage != null) {
			shell.setImage(fTitleImage);
		}
	}

	/**
	 * Returns the style for the message label.
	 * 
	 * @return the style for the message label
	 */
	protected int getMessageLabelStyle() {
		return SWT.WRAP;
	}

	/*
	 * @see Dialog.createButtonBar()
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		// this is incremented by createButton
		GridLayoutFactory.fillDefaults().numColumns(0).equalWidth(true).applyTo(composite);

		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).span(2, 1).applyTo(composite);
		composite.setFont(parent.getFont());
		// Add the buttons to the button bar.
		createButtonsForButtonBar(composite);
		return composite;
	}

	/*
	 * @see Dialog.createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		// initialize the dialog units
		initializeDialogUnits(parent);
		Point defaultMargins = LayoutConstants.getMargins();
		Point defaultSpacing = LayoutConstants.getSpacing();
		GridLayoutFactory.fillDefaults().margins(defaultMargins.x, defaultMargins.y * 3 / 2).spacing(defaultSpacing.x * 2, defaultSpacing.y)
				.numColumns(getColumnCount()).applyTo(parent);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
		createDialogAndButtonArea(parent);
		return parent;
	}

	/**
	 * Get the number of columns in the layout of the Shell of the dialog.
	 * 
	 * @return int
	 */
	protected int getColumnCount() {
		return 2;
	}

	/**
	 * Create the dialog area and the button bar for the receiver.
	 * 
	 * @param parent
	 */
	protected void createDialogAndButtonArea(Composite parent) {
		// create the dialog area and button bar
		dialogArea = createDialogArea(parent);
		buttonBar = createButtonBar(parent);
		// Apply to the parent so that the message gets it too.
		applyDialogFont(parent);
	}

	/**
	 * The custom dialog area.
	 */
	private Control customArea;

	/**
	 * Creates and returns the contents of an area of the dialog which appears below the message and above the button
	 * bar.
	 * <p>
	 * The default implementation of this framework method returns <code>null</code>. Subclasses may override.
	 * </p>
	 * 
	 * @param parent
	 *            parent composite to contain the custom area
	 * @return the custom area control, or <code>null</code>
	 */
	protected Control createCustomArea(Composite parent) {
		return null;
	}

	/**
	 * This implementation of the <code>Dialog</code> framework method creates and lays out a composite and calls
	 * <code>createMessageArea</code> and <code>createCustomArea</code> to populate it. Subclasses should override
	 * <code>createCustomArea</code> to add contents below the message.
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		// create message area
		createMessageArea(parent);
		// create the top level composite for the dialog area
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		composite.setLayoutData(data);
		// allow subclasses to add custom controls
		customArea = createCustomArea(composite);
		// If it is null create a dummy label for spacing purposes
		if (customArea == null) {
			customArea = new Label(composite, SWT.NULL);
		}
		return composite;
	}

	/**
	 * Create the area the message will be shown in.
	 * <p>
	 * The parent composite is assumed to use GridLayout as its layout manager, since the parent is typically the
	 * composite created in {@link Dialog#createDialogArea}.
	 * </p>
	 * 
	 * @param composite
	 *            The composite to parent from.
	 * @return Control
	 */
	protected Control createMessageArea(Composite composite) {
		// create message
		if (fMessage != null) {
			fMessageLabel = new Label(composite, getMessageLabelStyle());
			fMessageLabel.setText(fMessage);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false)
					.hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT).applyTo(fMessageLabel);
		}
		return composite;
	}

	@Override
	protected int getShellStyle() {
		/* The default shell style is augmented with the resize capability. */
		return super.getShellStyle() | SWT.RESIZE;
	}

}

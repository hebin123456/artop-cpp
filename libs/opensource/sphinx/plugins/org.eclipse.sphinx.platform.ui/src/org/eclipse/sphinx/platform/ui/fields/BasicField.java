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
package org.eclipse.sphinx.platform.ui.fields;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.platform.messages.PlatformMessages;
import org.eclipse.sphinx.platform.ui.fields.messages.FieldsMessages;
import org.eclipse.sphinx.platform.ui.internal.util.LayoutUtil;
import org.eclipse.sphinx.platform.ui.widgets.IWidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * <p align=center>
 * <b><em> Basic Platform Field </em></b>
 * </p>
 * <p>
 * Base class of all dialog fields. Dialog fields manage controls together with the model, independently from the
 * creation time of the widgets.
 * </p>
 * <ul>
 * <li>support for automated layouting.</li>
 * <li>enable / disable, set focus a concept of the base class.</li>
 * </ul>
 */
public class BasicField implements IField {

	/**
	 * The maximum width allowed for this field.
	 */
	protected static final int MAX_WITDH = 175;

	/**
	 * The listeners of this field. These listeners are notified when field changes append.
	 * <p>
	 * Listeners added to this list must be instances of {@link IFieldListener}.
	 */
	private ListenerList fFieldListeners = new ListenerList();

	/**
	 * Flag indicating if field is enabled or not.
	 */
	private boolean fEnabled;

	/**
	 * Flag indicating if field is used with a form layout (in a form editor for instance).
	 */
	protected boolean fUseFormLayout = false;

	/**
	 * The widget factory to use for widgets creation.
	 */
	protected IWidgetFactory fWidgetFactory;

	/**
	 * The label widget of this field.
	 */
	// TODO Let this field be private.
	protected Control fLabelControl;

	/**
	 * The text for the label widget of this field.
	 */
	// TODO Let this field be private.
	protected String fLabelText;

	/**
	 * Constructor.
	 * 
	 * @param widgetFactory
	 *            The widget factory to use for widgets creation.
	 */
	public BasicField(IWidgetFactory widgetFactory) {
		fEnabled = true;
		fLabelControl = null;
		fLabelText = new String();
		fWidgetFactory = widgetFactory;
	}

	// ------------------------------------------------------------------------
	// UI utilities
	// ------------------------------------------------------------------------

	/**
	 * Checks if the specified {@link Control control} is not <code>null</code> neither disposed.
	 * 
	 * @param control
	 *            The {@link Control} whose usability must be verified.
	 * @return <ul>
	 *         <li><code><b>true</b>&nbsp;&nbsp;</code> if the specified control is ok to be used;</li>
	 *         <li><code><b>false</b>&nbsp;</code> otherwise.</li>
	 *         </ul>
	 */
	protected final boolean isOkToUse(Control control) {
		return control != null && Display.getCurrent() != null && !control.isDisposed();
	}

	/**
	 * Asserts the specified number of columns is compatible with the expected number of columns for this field.
	 * 
	 * @param nColumns
	 *            The number of columns of the composite in which field must be created.
	 */
	private final void assertEnoughColumns(int nColumns) {
		String msg = NLS.bind(FieldsMessages.error_assert_LayoutNumberOfColumnsIsTooSmall, getNumberOfControls());
		Assert.isTrue(nColumns >= getNumberOfControls(), msg);
	}

	/**
	 * Asserts the specified {@link Composite composite} is not <code>null</code>.
	 * 
	 * @param composite
	 *            The composite that is expected to be not <code>null</code>.
	 */
	protected final void assertCompositeNotNull(Composite composite) {
		Assert.isNotNull(composite, "uncreated control requested with composite null"); //$NON-NLS-1$
	}

	/**
	 * Creates and returns a specific {@link Composite} that can be used by subclasses in some specific cases (e.g. list
	 * button field or string button field).
	 * 
	 * @param parent
	 *            The parent of the composite to create.
	 * @param nColumns
	 *            The number of columns inside the created composite itself.
	 * @param span
	 *            The number of columns the created composite will take up.
	 * @param hgrab
	 *            Flag indicating whether composite should be made wide enough to fit the remaining horizontal space.
	 * @param vgrab
	 *            Flag indicating whether composite should be made wide enough to fit the remaining vertical space.
	 * @param useFormLayout
	 *            Flag indicating if a form layout is expected to be used (e.g. in form editor).
	 * @return The newly created specific composite, whom layout and layout data are correctly set.
	 */
	protected static final Composite createSpecificComposite(Composite parent, int nColumns, int span, boolean hgrab, boolean vgrab,
			boolean useFormLayout) {
		// The (table wrap or grid) layout to set on the being created specific composite
		Layout layout;
		if (useFormLayout) {
			layout = LayoutUtil.tableWrapLayoutForSpecificComposite(nColumns);
		} else {
			layout = LayoutUtil.gridLayoutForSpecificComposite(nColumns);
		}

		// The (table wrap or grid) data to set on the being created specific composite
		Object data;
		if (useFormLayout) {
			data = LayoutUtil.tableWrapDataForSpecificComposite(span, hgrab, vgrab);
		} else {
			data = LayoutUtil.gridDataForSpecificComposite(span, hgrab, vgrab);
		}

		// The new specific composite, with the right layout, data, and font
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(layout);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());

		return composite;
	}

	/**
	 * Creates a spacer control with the given <tt>span</tt>.
	 * <p>
	 * The specified {@link Composite composite} is assumed to have either {@link TableWrapLayout} or {@link GridLayout}
	 * layout.
	 * 
	 * @param parent
	 *            The parent composite.
	 * @param span
	 *            The number of column cells the empty space control will take up.
	 */
	protected static final Control createEmptySpace(Composite parent, int span) {
		Label label = new Label(parent, SWT.LEFT);
		Object data = null;
		if (parent.getLayout() instanceof TableWrapLayout) {
			TableWrapData twd = new TableWrapData();
			twd.align = TableWrapData.LEFT;
			twd.grabHorizontal = false;
			twd.colspan = span;
			twd.indent = 0;
			twd.maxWidth = 0;
			twd.heightHint = 0;
			data = twd;
		} else {
			GridData gd = new GridData();
			gd.horizontalAlignment = GridData.BEGINNING;
			gd.grabExcessHorizontalSpace = false;
			gd.horizontalSpan = span;
			gd.horizontalIndent = 0;
			gd.widthHint = 0;
			gd.heightHint = 0;
			data = gd;
		}
		label.setLayoutData(data);
		return label;
	}

	/**
	 * Creates a spacer control.
	 * 
	 * @param parent
	 *            The parent composite.
	 */
	public static final Control createEmptySpace(Composite parent) {
		return createEmptySpace(parent, 1);
	}

	// ------------------------------------------------------------------------
	// UI creation (controls instantiation)
	// ------------------------------------------------------------------------

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.IField#fillIntoGrid(org.eclipse.swt.widgets.Composite, int)
	 */
	@Override
	public Control[] fillIntoGrid(Composite parent, int nColumns) {
		Assert.isNotNull(parent, NLS.bind(PlatformMessages.arg_mustNotBeNull, "parent")); //$NON-NLS-1$
		Layout layout = parent.getLayout();
		Assert.isNotNull(layout, NLS.bind(PlatformMessages.error_mustNotBeNull, "layout")); //$NON-NLS-1$
		fUseFormLayout = layout instanceof TableWrapLayout;

		// Verifies first that actual number of columns is compatible with expected one
		assertEnoughColumns(nColumns);
		// Delegate field filling
		return doFillIntoGrid(parent, nColumns);
	}

	/**
	 * <p>
	 * <table>
	 * <td valign=top><b>/!\&nbsp;&nbsp;</b></td>
	 * <td valign=top><em>To be re-implemented by platform field implementors.</em></td>
	 * <td valign=top><b>&nbsp;&nbsp;/!\</b></td>
	 * </table>
	 * 
	 * @param parent
	 *            The parent composite of this dialog field.
	 * @param nColumns
	 *            The number of columns of this dialog field.
	 * @return The controls of this dialog field.
	 */
	protected Control[] doFillIntoGrid(Composite parent, int nColumns) {
		Control label = getLabelControl(parent, 1);
		return new Control[] { label };
	}

	/**
	 * @param parent
	 *            The parent composite inside which label control must exist.
	 * @param hspan
	 *            The number of columns the label control is supposed to take up.
	 * @return The label control of this field.
	 */
	protected final Control getLabelControl(Composite parent, int hspan) {
		return getLabelControl(parent, false, hspan);
	}

	/**
	 * Returns the label control of this field, creates it if required.
	 * <p>
	 * After having requested label control creation (when label does not already exist), method sets layout data on
	 * label according to parent layout (form layout or not).
	 * 
	 * @param parent
	 *            The parent composite inside which label control must be created.
	 * @param multiLine
	 *            Flag indicating if field is made of several lines or not.
	 * @param hspan
	 *            The number of columns the label control is supposed to take up.
	 * @return The label control of this field.
	 */
	protected final Control getLabelControl(Composite parent, boolean multiLine, int hspan) {
		if (fLabelControl == null) {
			// Creates the label control
			fLabelControl = createLabelControl(parent, multiLine, hspan);
			// Set the right layout according the context (form or not)
			if (fUseFormLayout) {
				fLabelControl.setLayoutData(LayoutUtil.tableWrapDataForLabel(hspan, multiLine));
			} else {
				fLabelControl.setLayoutData(LayoutUtil.gridDataForLabel(hspan, multiLine));
			}
		}
		return fLabelControl;
	}

	/**
	 * Creates the label control.
	 * <p>
	 * For the creation of the label control, widget factory is used if possible.
	 * 
	 * @param parent
	 *            The parent composite (supposed to be not <tt>null</tt>).
	 * @param multiLine
	 *            <tt>true</tt> if this field is multi-line.
	 * @param hspan
	 *            The number of columns the text widget must span.
	 */
	protected Control createLabelControl(Composite parent, boolean multiLine, int hspan) {
		assertCompositeNotNull(parent);
		Control labelControl = null;
		if (fWidgetFactory != null) {
			labelControl = fWidgetFactory.createLabel(parent, fLabelText, multiLine, hspan, false);
		} else {
			labelControl = new Label(parent, SWT.LEFT | SWT.WRAP);
			if (fLabelText != null && !"".equals(fLabelText)) { //$NON-NLS-1$
				((Label) labelControl).setText(fLabelText);
			} else {
				// XXX: to avoid a 16 pixel wide empty label - revisit
				((Label) labelControl).setText("."); //$NON-NLS-1$
				labelControl.setVisible(false);
			}
		}
		labelControl.setFont(parent.getFont());
		labelControl.setEnabled(fEnabled);
		return labelControl;
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.IField#getLabelControl()
	 */
	@Override
	public Control getLabelControl() {
		Control control = null;
		if (isOkToUse(fLabelControl)) {
			control = fLabelControl;
		}
		return control;
	}

	// ------------------------------------------------------------------------
	// Layout helpers
	// ------------------------------------------------------------------------

	/**
	 * Returns the number of columns of the dialog field. To be re-implemented by dialog field implementors.
	 */
	protected int getNumberOfControls() {
		return 1;
	}

	// ------------------------------------------------------------------------
	// Label control attributes setting
	// ------------------------------------------------------------------------

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.IField#setLabelText(java.lang.String)
	 */
	@Override
	public void setLabelText(String labeltext) {
		fLabelText = labeltext;
		if (isOkToUse(fLabelControl)) {
			if (fLabelControl instanceof Label) {
				((Label) fLabelControl).setText(labeltext);
			}
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.IField#setToolTipText(java.lang.String)
	 */
	@Override
	public void setToolTipText(String toolTip) {
		if (isOkToUse(fLabelControl)) {
			fLabelControl.setToolTipText(toolTip);
		}
	}

	// ------------------------------------------------------------------------
	// Enable/Disable state management and Dispose field
	// ------------------------------------------------------------------------

	/**
	 * Gets the enable state of the dialog field.
	 */
	@Override
	public final boolean isEnabled() {
		return fEnabled;
	}

	/**
	 * Sets the enable state of the dialog field.
	 */
	@Override
	public final void setEnabled(boolean enabled) {
		if (enabled != fEnabled) {
			fEnabled = enabled;
			updateEnableState();
		}
	}

	/**
	 * Called when the enable state changed. To be extended by dialog field implementors.
	 */
	protected void updateEnableState() {
		if (fLabelControl != null) {
			fLabelControl.setEnabled(fEnabled);
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.ui.fields.BasicField#dispose()
	 */
	@Override
	public void dispose() {
		if (isOkToUse(fLabelControl)) {
			fLabelControl.dispose();
		}
	}

	// ------------------------------------------------------------------------
	// Field focus management
	// ------------------------------------------------------------------------

	/**
	 * Tries to set the focus to this field. Returns <code>true</code> if the dialog field can take focus. To be
	 * re-implemented by dialog field implementors.
	 * 
	 * @return <ul>
	 *         <li><b><tt>true&nbsp;</tt>&nbsp;</b> if field got the focus;</li>
	 *         <li><b><tt>false</tt>&nbsp;</b> if it was unable to.</li>
	 *         </ul>
	 */
	protected boolean setFocus() {
		// Basic field can not the focus be given to any control
		return false;
	}

	// ------------------------------------------------------------------------
	// Field changes listeners management
	// ------------------------------------------------------------------------

	/*
	 * @see
	 * org.eclipse.sphinx.platform.ui.fields.IField#addFieldListener(org.eclipse.sphinx.platform.ui.fields.IFieldListener
	 * )
	 */
	@Override
	public final void addFieldListener(IFieldListener listener) {
		fFieldListeners.add(listener);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.platform.ui.fields.IField#removeFieldListener(org.eclipse.sphinx.platform.ui.fields.IFieldListener
	 * )
	 */
	@Override
	public void removeFieldListener(IFieldListener listener) {
		fFieldListeners.remove(listener);
	}

	/**
	 * Programatical invocation of a dialog field change.
	 */
	protected void dialogFieldChanged() {
		// Iterates over all listeners
		for (Object listener : fFieldListeners.getListeners()) {
			// Fires dialog field changed notification
			((IFieldListener) listener).dialogFieldChanged(this);
		}
	}

	// ------------------------------------------------------------------------
	// Refresh handling
	// ------------------------------------------------------------------------

	/**
	 * Brings the UI in sync with the model. Only needed when model was changed in different thread while UI was already
	 * created.
	 */
	public void refresh() {
		updateEnableState();
	}
}
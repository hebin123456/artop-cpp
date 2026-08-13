/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys and others.
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
package org.eclipse.sphinx.xtendxpand.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.sphinx.platform.ui.preferences.AbstractPreferenceAndPropertyPage;
import org.eclipse.sphinx.xtendxpand.preferences.OutletsPreference;
import org.eclipse.sphinx.xtendxpand.ui.groups.OutletsGroup;
import org.eclipse.sphinx.xtendxpand.ui.groups.ProtectedRegionGroup;
import org.eclipse.sphinx.xtendxpand.ui.internal.messages.Messages;
import org.eclipse.sphinx.xtendxpand.ui.outlet.providers.OutletProvider;
import org.eclipse.sphinx.xtendxpand.util.XtendXpandUtil;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public abstract class AbstractOutletsPreferencePage extends AbstractPreferenceAndPropertyPage {

	private OutletsGroup outletsGroup;
	private ProtectedRegionGroup protectedRegionGroup;

	private TableViewer tableViewer;

	private OutletProvider outletProvider;

	public AbstractOutletsPreferencePage() {
		super(XtendXpandUtil.XTEND_XPAND_NATURE_ID, GRID);
	}

	public AbstractOutletsPreferencePage(String requiredProjectNatureId, int style) {
		super(requiredProjectNatureId, style);
	}

	protected OutletsPreference getOutletsPreference() {
		return OutletsPreference.INSTANCE;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		if (isProjectPreferencePage()) {
			ScopedPreferenceStore scopedPreferenceStore = new ScopedPreferenceStore(new ProjectScope((IProject) getElement()), getOutletsPreference()
					.getQualifier());
			return scopedPreferenceStore;
		}
		return null;
	}

	@Override
	protected void addFields(Composite parent) {
		addOutletsGroup(parent);
		addProtectedRegionGroup(parent);
	}

	protected void addOutletsGroup(Composite parent) {
		outletProvider = new OutletProvider((IProject) getElement(), getOutletsPreference());
		outletsGroup = new OutletsGroup(Messages.label_outletsGroupName, outletProvider);
		outletsGroup.createContent(parent, 2);
		tableViewer = outletsGroup.getTableViewer();
		Dialog.applyDialogFont(parent);
	}

	protected void addProtectedRegionGroup(Composite parent) {
		protectedRegionGroup = new ProtectedRegionGroup(Messages.label_protectedRegionGroupName, (IProject) getElement());
		protectedRegionGroup.createContent(parent, 3);
	}

	@Override
	protected void enablePreferenceContent(boolean useProjectSpecificSettings) {
		if (outletsGroup != null) {
			outletsGroup.setEnabled(useProjectSpecificSettings);
		}
	}

	@Override
	protected void performDefaults() {
		outletsGroup.setToDefault();
		protectedRegionGroup.setToDefault();
		tableViewer.refresh();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		outletsGroup.store();
		protectedRegionGroup.store();
		return super.performOk();
	}

	@Override
	public void dispose() {
		outletsGroup.dispose();
		protectedRegionGroup.dispose();
		super.dispose();
	}

	@Override
	protected void adjustGridLayout() {
		super.adjustGridLayout();
		if (((GridLayout) getFieldEditorParent().getLayout()).numColumns == 0) {
			((GridLayout) getFieldEditorParent().getLayout()).numColumns = 1;
		}
	}
}
